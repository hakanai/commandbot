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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.event.PacketEvent;
import org.jabberstudio.jso.event.PacketListener;
import org.jabberstudio.jso.util.PacketException;

/**
 * Contains the common factor of all handlers of {@link InfoQuery} packets.
 */
public abstract class AbstractQueryHandler implements PacketListener
{
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(AbstractQueryHandler.class);

    /**
     * Method called when a <tt>Packet</tt> is received or sent.</p>
     *
     * @param event The <tt>PacketEvent</tt> object.
     */
    public void packetTransferred(PacketEvent event)
    {
        // Only handle received packets.
        if (event.getType() != PacketEvent.RECEIVED)
        {
            return;
        }

        // Only handle <iq> packets with type="get" or type="set".
        Packet packet = event.getData();
        if (!(packet instanceof InfoQuery) || (packet.getType() != InfoQuery.GET && packet.getType() != InfoQuery.SET))
        {
            return;
        }

        InfoQuery requestPacket = (InfoQuery) packet;
        Extension request = requestPacket.getExtension();

        if (supports(request))
        {
            // Whether we succeed or not, we did handle this packet.
            event.setHandled(true);
        }
        else
        {
            return;
        }

        StreamDataFactory factory = event.getContext().getDataFactory();
        InfoQuery responsePacket = (InfoQuery) factory.createPacketNode(requestPacket.getNSI());
        Extension response = (Extension) request.copy();

        responsePacket.setTo(requestPacket.getFrom());
        responsePacket.setFrom(requestPacket.getTo());
        responsePacket.setID(requestPacket.getID());
        responsePacket.addExtension(response);

        try
        {
            // Attempt to perform the query.
            doQuery(request, response);

            // The query succeeded, so we'll set type to RESULT.
            responsePacket.setType(InfoQuery.RESULT);
        }
        catch (PacketException e)
        {
            // The query failed, so set in the error and make sure the type is ERROR.
            responsePacket.setError(e.getPacketError());
            responsePacket.setType(InfoQuery.ERROR);
        }

        // Attempt to send the response packet.
        try
        {
            event.getContext().getRouter().send(responsePacket);
        }
        catch (StreamException e)
        {
            log.error("Error sending back response packet", e);
        }
    }

    /**
     * Checks for support for the query.
     *
     * @param request the request query.
     * @return <code>true</code> if the query is supported, <code>false</code> otherwise.
     */
    protected abstract boolean supports(Extension request);

    /**
     * Performs the query.
     *
     * @param request  the request query.
     * @param response the response query.
     * @throws PacketException if there is an application-level error.
     */
    protected abstract void doQuery(Extension request, Extension response) throws PacketException;
}
