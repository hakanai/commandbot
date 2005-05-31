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

/**
 * Represents the command-line options to the application.
 */
public class MainOptions
{
    /**
     * The path to the configuration file.
     */
    private String configPath = "conf/bot-config.xml";

    /**
     * Creates the main options, parsing the provided command-line options.
     *
     * @param args the command-line options to parse.
     */
    public MainOptions(String[] args)
    {
        parse(args);
    }

    /**
     * Parses the provided command-line options.
     *
     * @param args the command-line options to parse.
     */
    private void parse(String[] args)
    {
        int i = 0;
        while (i < args.length)
        {
            String arg = args[i++];
            if ("-config".equals(arg))
            {
                if (i >= args.length || args[i].startsWith("-"))
                {
                    System.err.println("Option -config requires a path to a configuration file");
                    usage();
                }

                configPath = args[i++];
            }
            else if ("-help".equals(arg))
            {
                usage();
            }
            else
            {
                System.err.println("Unknown command-line option: " + arg);
            }
        }
    }

    /**
     * Prints the usage information and exits.
     */
    private static void usage()
    {
        System.err.println("Usage: java " + Main.class.getName() + " [options]");
        System.err.println("  -help                Shows this list of options");
        System.err.println("  -config configfile   Specifies the configuration file [conf/bot-config.xml]");
        System.exit(-1);
    }

    /**
     * Gets the path to the configuration file.
     *
     * @return the path to the configuration file.
     */
    public String getConfigPath()
    {
        return configPath;
    }
}
