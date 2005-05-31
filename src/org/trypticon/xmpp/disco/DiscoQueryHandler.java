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

package org.trypticon.xmpp.disco;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.trypticon.xmpp.util.AbstractQueryHandler;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.util.PacketException;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;
import org.jabberstudio.jso.x.disco.DiscoItemsQuery;;

/**
 * A packet listener which handles service discovery info requests.
 */
public class DiscoQueryHandler extends AbstractQueryHandler
{
    /**
     * A mapping from nodes to discoverable objects.
     */
    private Map<String, Discoverable> discoverableMap = new HashMap<String, Discoverable>();

    /**
     * Creates the query handler.
     *
     * @param topLevel the top level discoverable object, which has the empty node.
     */
    public DiscoQueryHandler(Discoverable topLevel)
    {
        addDiscoverable(topLevel);
    }

    /**
     * Adds a discoverable object for handling queries on a node.
     *
     * @param discoverable the discoverable object.
     */
    public void addDiscoverable(Discoverable discoverable)
    {
        discoverableMap.put(discoverable.getNode(), discoverable);
    }

    /**
     * Checks for support for the query.
     *
     * @param request the request query.
     * @return <code>true</code> if the query is supported, <code>false</code> otherwise.
     */
    protected boolean supports(Extension request)
    {
        return (request instanceof DiscoInfoQuery ||
                request instanceof DiscoItemsQuery);
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
        if (request instanceof DiscoInfoQuery)
        {
            DiscoInfoQuery infoRequest = (DiscoInfoQuery) request;
            DiscoInfoQuery infoResponse = (DiscoInfoQuery) response;

            Discoverable support = discoverableMap.get(infoRequest.getNode());

            if (support == null)
            {
                throw new PacketException(PacketError.CANCEL,
                                          PacketError.ITEM_NOT_FOUND_CONDITION);
            }

            support.populateDiscoInfo(infoResponse);
        }
        else
        {
            DiscoItemsQuery itemsRequest = (DiscoItemsQuery) request;
            DiscoItemsQuery itemsResponse = (DiscoItemsQuery) response;

            Discoverable support = discoverableMap.get(itemsRequest.getNode());

            if (support == null)
            {
                throw new PacketException(PacketError.CANCEL,
                                          PacketError.ITEM_NOT_FOUND_CONDITION);
            }

            List items = support.getDiscoChildren();

            JID self = ((Packet) itemsResponse.getParent()).getFrom();

            // Query each of the children in term, using the same mechanism as the disco#info.
            // This generates a nice and consistent naming for the discovery tree.
            Iterator itemIterator = items.iterator();
            while (itemIterator.hasNext())
            {
                Discoverable item = (Discoverable) itemIterator.next();

                DiscoInfoQuery info = (DiscoInfoQuery)
                        itemsResponse.getDataFactory().createExtensionNode(DiscoInfoQuery.NAME);
                info.setNode(item.getNode());
                item.populateDiscoInfo(info);

                itemsResponse.addItem(self, info.getNode(), info.getName());
            }
        }
    }
}
