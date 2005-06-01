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

import org.jabberstudio.jso.Presence;
import org.jabberstudio.jso.JID;

/**
 * Interface representing the bot's roster.
 */
public interface Roster
{
    /**
     * Gets the highest priority presence for a JID.
     *
     * @param jid the JID
     * @return the highest priority presence for that JID, or <code>null</code> if it is not available.
     */
    Presence getPresence(JID jid);
}
