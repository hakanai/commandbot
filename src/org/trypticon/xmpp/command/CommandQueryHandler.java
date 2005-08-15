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

package org.trypticon.xmpp.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.trypticon.xmpp.disco.DiscoQueryHandler;
import org.trypticon.xmpp.disco.Discoverable;
import org.trypticon.xmpp.util.AbstractQueryHandler;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.util.IdentityGenerator;
import org.jabberstudio.jso.util.PacketException;
import org.jabberstudio.jso.x.commands.CommandQuery;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;
import org.jdom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A packet listener which handles Command queries.
 */
public class CommandQueryHandler extends AbstractQueryHandler
    implements Discoverable
{
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(CommandQueryHandler.class);

    /**
     * A map of nodes to commands.
     */
    private Map<String, CommandHandler> commandMap = new HashMap<String, CommandHandler>();

    /**
     * The disco query handler.
     */
    private DiscoQueryHandler discoHandler;

    /**
     * Constructs the command query handler.
     *
     * @param discoHandler the disco query handler.
     */
    public CommandQueryHandler(DiscoQueryHandler discoHandler)
    {
        this.discoHandler = discoHandler;
        discoHandler.addDiscoverable(this);
    }

    /**
     * Configures the command query handler.
     *
     * @param config the XML configuration element containing the commands.
     */
    public void configure(Element config)
    {
        commandMap.clear();

        if (config != null)
        {
            for (Element commandElement : (List<Element>) config.getChildren("command"))
            {
                String commandClassName = commandElement.getAttributeValue("classname");

                try
                {
                    CommandHandler handler = (CommandHandler) Class.forName(commandClassName).newInstance();
                    handler.configure(commandElement.getChild("config"));

                    addCommand(handler);
                }
                catch (Throwable t)
                {
                    log.error("Error loading command class " + commandClassName, t);
                }
            }
        }
    }

    /**
     * Adds a supported command to the handler.
     *
     * @param handler the handler for the command.
     */
    public void addCommand(CommandHandler handler)
    {
        commandMap.put(handler.getNode(), handler);
        discoHandler.addDiscoverable(handler);
    }

    /**
     * Checks for support for the query.
     *
     * @param request the request query.
     * @return <code>true</code> if the query is supported, <code>false</code> otherwise.
     */
    protected boolean supports(Extension request)
    {
        return (request instanceof CommandQuery);
    }

    /**
     * Performs the query.
     *
     * @param request  the request query.
     * @param response the response query.
     * @throws PacketException if there is an application-level error.
     */
    protected void doQuery(Extension request, Extension response) throws PacketException
    {
        CommandQuery commandRequest = (CommandQuery) request;
        CommandQuery commandResponse = (CommandQuery) response;

        CommandHandler handler = commandMap.get(commandRequest.getNode());

        if (handler == null)
        {
            throw new PacketException(PacketError.CANCEL,
                                      PacketError.ITEM_NOT_FOUND_CONDITION);
        }

        commandResponse.setNode(commandRequest.getNode());

        String sessionID = commandRequest.getSessionID();
        if (sessionID == null)
        {
            // TODO: Create and pass around some kind of session object for commands to use as state.
            sessionID = IdentityGenerator.generateGlobal();
        }
        commandResponse.setSessionID(sessionID);

        handler.handleCommand(commandRequest, commandResponse);
    }

    /**
     * Gets the node which the discoverable is located at.
     *
     * @return the node which the discoverable is located at.
     */
    public String getNode()
    {
        return CommandQuery.NAMESPACE;
    }

    /**
     * Populates info into the provided query, to form the response.
     *
     * @param query the query.
     */
    public void populateDiscoInfo(DiscoInfoQuery query)
    {
        query.addIdentity("automation", "command-list", "Ad-hoc Commands");
    }

    /**
     * Gets disco items which exist at a child of this item.
     */
    public List<Discoverable> getDiscoChildren()
    {
        List<Discoverable> result = new ArrayList<Discoverable>();
        result.addAll(commandMap.values());
        return Collections.unmodifiableList(result);
    }
}
