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

package org.trypticon.xmpp.version;

import org.trypticon.xmpp.util.AbstractQueryHandler;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.util.PacketException;
import org.jabberstudio.jso.x.info.VersionQuery;;

/**
 * A handler for sending back version queries.
 */
public class VersionQueryHandler extends AbstractQueryHandler
{
    /**
     * The client name.
     */
    private String name;

    /**
     * The client version.
     */
    private String version;

    /**
     * Constructs the query handler.
     *
     * @param name    the client name.
     * @param version the client version.
     */
    public VersionQueryHandler(String name, String version)
    {
        this.name = name;
        this.version = version;
    }

    /**
     * Checks for support for the query.
     *
     * @param request the request query.
     * @return <code>true</code> if the query is supported, <code>false</code> otherwise.
     */
    protected boolean supports(Extension request)
    {
        return (request instanceof VersionQuery);
    }

    /**
     * Performs the query.
     *
     * @param request  the request query.
     * @param response the response query.
     * @throws org.jabberstudio.jso.util.PacketException
     *          if there is an application-level error.
     */
    protected void doQuery(Extension request, Extension response) throws PacketException
    {
        VersionQuery versionResponse = (VersionQuery) response;

        versionResponse.setName(name);
        versionResponse.setVersion(version);
        versionResponse.setOS(System.getProperty("os.name") + " " +
                System.getProperty("os.version") + " " +
                System.getProperty("os.arch"));
    }
}
