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

import java.util.Collections;
import java.util.List;

import org.trypticon.xmpp.disco.Discoverable;

import org.jabberstudio.jso.x.commands.CommandQuery;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;
import org.jdom.Element;

/**
 * Abstract implementation of {@link CommandHandler}.
 */
public abstract class AbstractCommandHandler implements CommandHandler
{
    /**
     * The node.
     */
    private String node;

    /**
     * The name.
     */
    private String name;

    /**
     * Construct the command handler.
     *
     * @param node the node of the command.  Should be fairly unique, so implementors should use
     *             a URI in their own namespace in order not to clash with other implementors.
     * @param name the name of the command.
     */
    public AbstractCommandHandler(String node, String name)
    {
        this.name = name;
        this.node = node;
    }

    /**
     * Configures the command.
     * <p/>
     * This default implementation does nothing.
     *
     * @param config the configuration element.
     */
    public void configure(Element config)
    {
        // Does nothing.
    }

    /**
     * Gets the node.
     *
     * @return the node.
     */
    public String getNode()
    {
        return node;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Populates info into the provided query, to form the response.
     *
     * @param query the query.
     */
    public void populateDiscoInfo(DiscoInfoQuery query)
    {
        query.addIdentity("automation", "command-node", getName());
        query.addFeature(CommandQuery.NAMESPACE);
    }

    /**
     * Gets child items for the provided query.
     */
    public final List<Discoverable> getDiscoChildren()
    {
        // Commands have no child items.
        return Collections.emptyList();
    }
}
