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

package org.trypticon.commandbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.trypticon.xmpp.bot.BaseBot;
import org.trypticon.xmpp.command.CommandHandler;
import org.trypticon.xmpp.command.CommandQueryHandler;
import org.trypticon.xmpp.disco.DiscoQueryHandler;
import org.trypticon.xmpp.disco.Discoverable;
import org.trypticon.xmpp.logging.LoggingPacketListener;
import org.trypticon.xmpp.logging.LoggingStreamStatusListener;
import org.trypticon.xmpp.util.FeatureNotImplementedHandler;
import org.trypticon.xmpp.util.FirstPacketListenerRelay;
import org.trypticon.xmpp.version.VersionQueryHandler;
import org.trypticon.commandbot.conversation.ThreadManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabberstudio.jso.event.PacketEvent;
import org.jabberstudio.jso.x.commands.CommandQuery;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;
import org.jdom.Element;

/**
 * A simple XMPP bot.
 */
public class CommandBot extends BaseBot
        implements Discoverable
{
    /**
     * The client name.
     */
    private static final String CLIENT_NAME = "CommandBot";

    /**
     * The client version.
     */
    private static final String CLIENT_VERSION = "0.2-prototype";

    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(CommandBot.class);

    /**
     * The handler for service discovery queries.
     */
    private DiscoQueryHandler discoHandler;

    /**
     * The handler for ad-hoc command queries.
     */
    private CommandQueryHandler commandHandler;

    /**
     * Constructs the bot.
     *
     * @param config the configuration for the bot.
     */
    public CommandBot(Element config)
    {
        super(config);

        discoHandler = new DiscoQueryHandler(this);
        commandHandler = new CommandQueryHandler(discoHandler);

        Element commandsElement = config.getChild("commands");
        for (Element commandElement : (List<Element>) commandsElement.getChildren("command"))
        {
            String commandClassName = commandElement.getAttributeValue("classname");

            try
            {
                CommandHandler handler = (CommandHandler) Class.forName(commandClassName).newInstance();
                handler.configure(commandElement.getChild("config"));

                commandHandler.addCommand(handler);
            }
            catch (Throwable t)
            {
                log.error("Error loading command class " + commandClassName, t);
            }
        }
    }

    /**
     * Attach listeners to the bot.  Subclasses should remember to call this method.
     */
    protected void attachListeners()
    {
        super.attachListeners();

        // Attach logging stuff.
        getStream().addStreamStatusListener(new LoggingStreamStatusListener());
        getStream().addPacketListener(new LoggingPacketListener());

        // Attach the InfoQuery chain.
        FirstPacketListenerRelay queryRelay = new FirstPacketListenerRelay();
        queryRelay.addPacketListener(new VersionQueryHandler(CLIENT_NAME, CLIENT_VERSION));
        queryRelay.addPacketListener(discoHandler);
        queryRelay.addPacketListener(commandHandler);
        queryRelay.addPacketListener(new FeatureNotImplementedHandler());
        getStream().addPacketListener(PacketEvent.RECEIVED, queryRelay);

        // Attach the conversation handler.
        ThreadManager threadManager = new ThreadManager();
        getStream().addPacketListener(PacketEvent.RECEIVED, threadManager);
    }

    /**
     * Gets the node which the discoverable is located at.
     *
     * @return the node which the discoverable is located at.
     */
    public String getNode()
    {
        // This is the root node for the resource.
        return "";
    }

    /**
     * Populates info into the provided query, to form the response.
     *
     * @param query the query.
     */
    public void populateDiscoInfo(DiscoInfoQuery query)
    {
        query.addIdentity("client", "bot", CLIENT_NAME);
        query.addFeature(CommandQuery.NAMESPACE);
    }

    /**
     * Gets disco items which exist at a child of this item.
     */
    public List<Discoverable> getDiscoChildren()
    {
        List<Discoverable> result = new ArrayList<Discoverable>();
        result.add(commandHandler);
        return Collections.unmodifiableList(result);
    }

}
