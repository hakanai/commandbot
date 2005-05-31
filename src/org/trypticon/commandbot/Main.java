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

import org.trypticon.xmpp.bot.Bot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;;

/**
 * Main class.  Does nothing but create and launch a {@link Bot}.
 */
public class Main
{
    /**
     * Logging channel.
     */
    private static final Log log;

    static
            {
                // Bootstrap the logging.
                Appender appender = new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_ERR);
                Logger.getRootLogger().addAppender(appender);

                log = LogFactory.getLog(Main.class);
            }

    /**
     * Main method.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            MainOptions options = new MainOptions(args);

            Element config = new SAXBuilder().build(options.getConfigPath()).getRootElement();

            final Bot bot = new CommandBot(config);

            Runtime.getRuntime().addShutdownHook(new Thread()
                    {
                        public void run()
                        {
                            bot.stop();
                        }
                    });

            bot.start();
        }
        catch (Throwable t)
        {
            log.error("Unexpected exception in Main.main()", t);
        }
    }
}
