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

package org.trypticon.xmpp.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabberstudio.jso.event.StreamStatusEvent;
import org.jabberstudio.jso.event.StreamStatusListener;

/**
 * A strean status listener which prints the packets to the log.
 */
public class LoggingStreamStatusListener implements StreamStatusListener
{
    /**
     * Logger.
     */
    private Log log = LogFactory.getLog(LoggingStreamStatusListener.class);

    /**
     * Logs the stream status change.
     *
     * @param event the stream status event.
     */
    public void statusChanged(StreamStatusEvent event)
    {
        String direction = event.getContext().isInbound() ? "Inbound" : "Outbound";
        log.info(direction + " : " + event.getPreviousStatus() + " -> " + event.getNextStatus());
    }
}
