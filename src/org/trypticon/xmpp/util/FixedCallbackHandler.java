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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A callback handler which responds to authentication callbacks with fixed values.
 */
public class FixedCallbackHandler implements CallbackHandler
{
    /**
     * Logging channel.
     */
    private static final Log log = LogFactory.getLog(FixedCallbackHandler.class);

    /**
     * The username.
     */
    private String name;

    /**
     * The password.
     */
    private char[] password;

    /**
     * Constructs the fixed callback handler.
     *
     * @param name     the username.
     * @param password the password.
     */
    public FixedCallbackHandler(String name, String password)
    {
        this.name = name;
        this.password = password.toCharArray();
    }

    /**
     * Handles multiple callbacks.
     *
     * @param callbacks the callbacks to handle.
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        for (Callback callback : callbacks)
        {
            if (callback instanceof NameCallback)
            {
                log.debug("Name callback being responded to with name '" + name + "'.");
                ((NameCallback) callback).setName(name);
            }
            else if (callback instanceof PasswordCallback)
            {
                log.debug("Password callback being responded to.");
                ((PasswordCallback) callback).setPassword(password);
            }
            else
            {
                log.warn("Unknown callback of type " + callback.getClass().getName());
            }
        }
    }
}
