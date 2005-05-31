/*
 * Copyright 2004-2005 Trypticon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trypticon.xmpp.bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

import org.trypticon.xmpp.util.DummyTrustManager;
import org.trypticon.xmpp.util.FixedCallbackHandler;
import org.trypticon.xmpp.util.SrvLookup;

import net.outer_planes.jso.JSO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.JSOImplementation;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.Presence;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.features.FeaturesetConsumerManager;
import org.jabberstudio.jso.sasl.SASLFeatureConsumer;
import org.jabberstudio.jso.tls.StartTLSSocketFeatureConsumer;
import org.jabberstudio.jso.tls.StartTLSSocketStreamSource;
import org.jabberstudio.jso.util.Monitor;
import org.jabberstudio.jso.util.PacketException;
import org.jabberstudio.jso.util.Utilities;
import org.jabberstudio.jso.x.core.AuthFeatureConsumer;
import org.jabberstudio.jso.x.core.BindFeatureConsumer;
import org.jabberstudio.jso.x.core.SessionFeatureConsumer;
import org.jdom.Element;

/**
 * Base class for Jabber bots.  Tries to provide a layer of convenience over the top of JSO.
 */
public class BaseBot implements Bot
{
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(BaseBot.class);

    /**
     * The configuration for the bot.
     */
    private Element config;

    /**
     * The thread running the main processing loop.
     */
    private Thread runnerThread;

    /**
     * The stream source.
     */
    private StartTLSSocketStreamSource streamSource;

    /**
     * The XMPP stream to the server.
     */
    private Stream stream;

    /**
     * Constructs the bot.
     *
     * @param config the configuration for the bot.
     */
    public BaseBot(Element config)
    {
        this.config = config;
    }

    /**
     * Starts the bot.
     */
    public synchronized void start()
    {
        if (runnerThread == null)
        {
            runnerThread = new Thread(new BotRunner());
            runnerThread.start();
        }
    }

    /**
     * Stops the bot.
     */
    public synchronized void stop()
    {
        if (runnerThread != null)
        {
            runnerThread = null;
        }
    }

    /**
     * Attach listeners to the bot.  Default implementation does nothing.
     */
    protected void attachListeners()
    {
    }

    /**
     * Connects to the server, opens a stream and logs in.
     *
     * @return <code>true</code> if connection is successful.
     */
    public boolean connect()
    {
        JSOImplementation jso = JSO.getInstance();

        Element connectionElement = config.getChild("connection");

        JID clientJID = JID.valueOf(connectionElement.getChildTextTrim("jid"));
        JID serverJID = new JID(null, clientJID.getDomain(), null);

        try
        {
            InetSocketAddress address = SrvLookup.resolveXmppClient(clientJID.getDomain());

            String hostname = connectionElement.getChildTextTrim("hostname");
            if (hostname == null)
            {
                hostname = address.getHostName();
            }

            boolean tls = "true".equals(connectionElement.getChildTextTrim("tls"));

            String portString = connectionElement.getChildTextTrim("port");
            int port = (portString == null) ? -1 : Integer.parseInt(portString);
            if (port == -1)
            {
                if (tls)
                {
                    port = 5223;
                }
                else
                {
                    port = address.getPort();
                }
            }

            streamSource = new StartTLSSocketStreamSource(hostname, port);
            streamSource.getTLSContext().init(null, DummyTrustManager.asArray(), null);

            // Old-style TLS requires negotiation before sending any data.
            if (tls)
            {
                streamSource.negotiateClientTLS();
            }
        }
        catch (IOException e)
        {
            log.error("Failure to create stream source", e);
            return false;
        }
        catch (GeneralSecurityException e)
        {
            log.error("TLS is not supported by your JRE", e);
            return false;
        }

        // Create the stream...
        stream = jso.createStream(Utilities.CLIENT_NAMESPACE);

        // Attach listeners to the stream.
        attachListeners();

        // Try to connect.
        try
        {
            stream.connect(streamSource);
            stream.getOutboundContext().setTo(serverJID);
            stream.getOutboundContext().setVersion("1.0");
            stream.open(5000);
        }
        catch (StreamException e)
        {
            log.error("Failure to connect to stream", e);
            return false;
        }

        // Consume stream features.
        String password = connectionElement.getChildTextTrim("password");

        try
        {
            String version = stream.getInboundContext().getVersion();
            if ("1.0".equals(version))
            {
                saslLogin(clientJID, password);
            }
            else if ("".equals(version))
            {
                authLogin(clientJID, password);
            }
            else
            {
                log.warn("Version '" + version + "' was not an expected version number");
            }
        }
        catch (PacketException e)
        {
            log.error("Packet error while authenticating", e);
            return false;
        }
        catch (StreamException e)
        {
            log.error("Stream error while authenticating", e);
            return false;
        }

        // Set presence to online, but with negative priority.  Servers compliant with XMPP will therefore
        // not send us any messages which were sent to the bare JID.
        try
        {
            Presence presence = (Presence)
                    stream.getDataFactory().createPacketNode(new NSI("presence", Utilities.CLIENT_NAMESPACE));
            presence.setPriority(-1);
            stream.send(presence);
        }
        catch (StreamException e)
        {
            log.error("Failure to set presence", e);
            return false;
        }

        return true;
    }

    /**
     * Performs login using SASL.
     *
     * @throws StreamException if a stream-level exception occurred.
     * @throws PacketException if a packet-level exception occurred.
     */
    private void saslLogin(JID client, String password) throws StreamException, PacketException
    {
        FeaturesetConsumerManager manager = new FeaturesetConsumerManager();

        StartTLSSocketFeatureConsumer tls = new StartTLSSocketFeatureConsumer(streamSource);
        manager.registerFeatureConsumer(tls);

        SASLFeatureConsumer sasl = new SASLFeatureConsumer();
        sasl.getClientInfo().setServer(client.getDomain());
        sasl.getClientInfo().setCallbackHandler(new FixedCallbackHandler(client.getNode(), password));
        manager.registerFeatureConsumer(sasl);

        BindFeatureConsumer bind = new BindFeatureConsumer(client.getResource());
        manager.registerFeatureConsumer(bind);

        SessionFeatureConsumer session = new SessionFeatureConsumer(false);
        manager.registerFeatureConsumer(session);

        try
        {
            // Attach and run the consumers.
            manager.attach(stream);
            manager.run();

            // Check to see if an error occurred.
            Throwable failure = manager.getFailure();
            if (failure != null)
            {
                log.error("Authentication failed", failure);

                if (failure instanceof StreamException)
                {
                    throw (StreamException) failure;
                }
                else if (failure instanceof PacketException)
                {
                    throw (PacketException) failure;
                }
                else
                {
                    throw new PacketException(failure.getLocalizedMessage(), PacketError.CANCEL,
                                              PacketError.UNDEFINED_CONDITION);
                }
            }

            // If the SASL consumer didn't complete, try an older-style login.
            if (!manager.isFeatureConsumerCompleted(sasl))
            {
                authLogin(client, password);
            }
            else
            {
                // Make sure the resource was bound.
                if (!manager.isFeatureConsumerCompleted(bind))
                {
                    throw new PacketException(PacketError.AUTH, PacketError.RESOURCE_CONSTRAINT_CONDITION);
                }

                // Make sure the session is active.
                if (!manager.isFeatureConsumerCompleted(session))
                {
                    throw new PacketException(PacketError.CANCEL, PacketError.SERVICE_UNAVAILABLE_CONDITION);
                }
            }
        }
        finally
        {
            manager.detach();
        }
    }

    /**
     * Performs login using the older-style authentication.
     *
     * @throws StreamException if a stream-level exception occurred.
     * @throws PacketException if a packet-level exception occurred.
     */
    private void authLogin(JID client, String password) throws StreamException, PacketException
    {
        AuthFeatureConsumer auth =
                new AuthFeatureConsumer(JID.valueOf(client.getDomain()), client.getNode(), password, client.getResource());
        auth.authenticate(stream, stream);
    }

    /**
     * Closes the stream and disconnects from the server.
     */
    private void disconnect()
    {
        try
        {
            if (stream != null)
            {
                stream.close();
                stream.disconnect();
            }
        }
        catch (StreamException e)
        {
            log.warn("Failed to close and disconnect, stream is probably already closed", e);
        }
    }

    /**
     * Gets a reference to the XMPP Stream.
     *
     * @return the XMPP stream reference.
     */
    protected Stream getStream()
    {
        return stream;
    }

    /**
     * <code>Runnable</code> which runs the bot.
     */
    private class BotRunner implements Runnable
    {
        public void run()
        {
            Thread currentThread = Thread.currentThread();
            while (runnerThread == currentThread)
            {
                try
                {
                    if (!connect())
                    {
                        try
                        {
                            log.info("Sleeping for a while before trying again.");
                            Thread.sleep(20000);
                            continue;
                        }
                        catch (InterruptedException e)
                        {
                            break;
                        }
                    }

                    // Main processing loop.
                    while (runnerThread == currentThread)
                    {
                        // Process a single packet.
                        try
                        {
                            stream.process();
                        }
                        catch (StreamException e)
                        {
                            log.error("Error processing stream", e);
                            break;
                        }

                        // Surely there is a better way to do this by now... can't we _wait_ for data?
                        try
                        {
                            Thread.sleep(Monitor.DEFAULT_PROC_INTERVAL);
                        }
                        catch (InterruptedException e)
                        {
                            log.warn("Thread interrupted", e);
                            break;
                        }
                    }
                }
                finally
                {
                    disconnect();
                }
            }
        }
    }
}
