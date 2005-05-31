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

import java.util.Iterator;

import org.jabberstudio.jso.event.PacketEvent;
import org.jabberstudio.jso.event.PacketListener;
import org.jabberstudio.jso.util.PacketListenerRelay;

/**
 * A specialised {@link PacketListenerRelay} which stops passing around the packet once
 * a listener takes responsibility for it.
 */
public class FirstPacketListenerRelay extends PacketListenerRelay
{
    /**
     * Overridden to stop after the event is marked as handled.
     *
     * @param event the packet event.
     */
    public void packetTransferred(PacketEvent event)
    {
        obtainLogger().debug("relaying packet transferred for " + this);

        Iterator iterator = getPacketListeners(event.getType()).iterator();
        while (iterator.hasNext())
        {
            PacketListener listener = (PacketListener) iterator.next();

            listener.packetTransferred(event);

            // Here's the extra logic.
            if (event.isHandled())
            {
                break;
            }
        }
    }
}
