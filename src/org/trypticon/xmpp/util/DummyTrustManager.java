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

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * A trust manager which trusts everyone equally.
 */
public class DummyTrustManager implements X509TrustManager
{
    /**
     * Returns an empty array of issuers, as issuers are irrelevant for this implementation.
     *
     * @return an empty array.
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }

    /**
     * Does nothing.
     *
     * @param chain    the certificate chain.
     * @param authType the authentication type.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
    {
        // Always trusted.
    }

    /**
     * Does nothing.
     *
     * @param chain    the certificate chain.
     * @param authType the authentication type.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
    {
        // Always trusted.
    }

    /**
     * Gets a dummy trust manager as a single element inside an array.
     *
     * @return the array of trust managers, containing only a dummy.
     */
    public static X509TrustManager[] asArray()
    {
        return new X509TrustManager[]
                {
                    new DummyTrustManager()
                };
    }
}
