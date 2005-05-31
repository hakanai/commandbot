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

package org.trypticon.xmpp.util;

import java.net.InetSocketAddress;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for looking up XMPP hosts.
 */
public class SrvLookup
{
    // Constants
    private static final String XMPP_CLIENT_SERVICE = "xmpp-client";
    private static final String XMPP_SERVER_SERVICE = "xmpp-server";
    private static final String JABBER_SERVICE = "jabber";
    private static final String TCP_PROTOCOL = "tcp";

    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(SrvLookup.class);

    /**
     * The JNDI context.
     */
    private static DirContext context;

    static
            {
                try
                {
                    Hashtable<String, String> options = new Hashtable<String, String>();
                    options.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                    context = new InitialDirContext(options);
                }
                catch (NamingException e)
                {
                    log.warn("Problem getting JNDI context for DNS lookups.  SRV lookups will not work.", e);
                }
            }

    /**
     * Prevent instantiation.
     */
    private SrvLookup()
    {
    }

    /**
     * Resolves an XMPP server for client-to-server communications.
     *
     * @param domain the server domain.
     * @return the server's host and port for client-to-server communications.
     */
    public static InetSocketAddress resolveXmppClient(String domain)
    {
        InetSocketAddress result = resolveSRV(domain, XMPP_CLIENT_SERVICE, TCP_PROTOCOL);

        // Fallback
        if (result == null)
        {
            result = new InetSocketAddress(domain, 5222);
        }

        return result;
    }

    /**
     * Resolves an XMPP server for server-to-server communications.
     *
     * @param domain the server domain.
     * @return the server's host and port for server-to-server communications.
     */
    public static InetSocketAddress resolveXmppServer(String domain)
    {
        InetSocketAddress result = resolveSRV(domain, XMPP_SERVER_SERVICE, TCP_PROTOCOL);

        // Backwards compatibility
        if (result == null)
        {
            result = resolveSRV(domain, JABBER_SERVICE, TCP_PROTOCOL);
        }

        // Fallback
        if (result == null)
        {
            result = new InetSocketAddress(domain, 5269);
        }

        return result;
    }

    /**
     * Resolves a domain name to a host and port, by looking up the SRV record.
     *
     * @param domain   the domain to look up.
     * @param service  the service to look up.
     * @param protocol the protocol to look up.
     * @return the address, or <code>null</code> if it couldn't be resolved.
     */
    private static InetSocketAddress resolveSRV(String domain, String service, String protocol)
    {
        if (context == null)
        {
            return null;
        }

        try
        {
            Attributes dnsLookup = context.getAttributes("_" + service + "._" + protocol + "." + domain);
            Attribute srvAttribute = dnsLookup.get("SRV");

            // The attribute is null if there was no record in DNS.
            if (srvAttribute == null)
            {
                return null;
            }

            String srvRecord = (String) srvAttribute.get();

            // The attribute value is null if there was somehow a record with a null value.
            if (srvRecord == null)
            {
                return null;
            }

            String[] srvRecordEntries = srvRecord.split(" ");
            String host = srvRecordEntries[srvRecordEntries.length - 1];
            int port = Integer.parseInt(srvRecordEntries[srvRecordEntries.length - 2]);
            return new InetSocketAddress(host, port);
        }
        catch (NamingException e)
        {
            log.warn("Problem looking up SRV record for domain " + domain + ", service " + service +
                    ", protocol " + protocol, e);
            return null;
        }
    }
}
