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

import org.trypticon.xmpp.disco.Discoverable;

import org.jabberstudio.jso.x.commands.CommandQuery;
import org.jabberstudio.jso.util.PacketException;
import org.jdom.Element;

/**
 * A handler for a single command.
 */
public interface CommandHandler extends Discoverable
{
    /**
     * Configures the command.
     *
     * @param config the configuration element.
     */
    void configure(Element config);

    /**
     * Gets the name of the command.
     *
     * @return the name of the command.
     */
    String getName();

    /**
     * Handles the command.
     *
     * @param request  the request query.
     * @param response the response query.
     * @throws PacketException if an error occurs handling the command.
     */
    void handleCommand(CommandQuery request, CommandQuery response) throws PacketException;
}
