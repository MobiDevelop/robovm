/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.okhttp;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * @hide This class is not part of the Android public SDK API
 */
public final class HttpsHandler extends HttpHandler {

    /**
     * The connection spec to use when connecting to an https:// server. Note that Android does
     * not set the cipher suites or TLS versions to use so the socket's defaults will be used
     * instead. When the SSLSocketFactory is provided by the app or GMS core we will not
     * override the enabled ciphers or TLS versions set on the sockets it produces with a
     * list hardcoded at release time. This is deliberate.
     */
    private static final ConnectionSpec TLS_CONNECTION_SPEC = ConnectionSpecs.builder(true)
            .allEnabledCipherSuites()
            .allEnabledTlsVersions()
            .supportsTlsExtensions(true)
            .build();

    private static final List<Protocol> HTTP_1_1_ONLY =
            Collections.singletonList(Protocol.HTTP_1_1);

    private final ConfigAwareConnectionPool configAwareConnectionPool =
            ConfigAwareConnectionPool.getInstance();

    @UnsupportedAppUsage
    public HttpsHandler() {
    }

    @Override protected int getDefaultPort() {
        return 443;
    }

    @Override
    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpsOkUrlFactory(proxy);
        // For HttpsURLConnections created through java.net.URL Android uses a connection pool that
        // is aware when the default network changes so that pooled connections are not re-used when
        // the default network changes.
        okUrlFactory.client().setConnectionPool(configAwareConnectionPool.get());
        return okUrlFactory;
    }

    /**
     * Creates an OkHttpClient suitable for creating {@link HttpsURLConnection} instances on
     * Android.
     */
    // Visible for android.net.Network.
    public static OkUrlFactory createHttpsOkUrlFactory(Proxy proxy) {
        // The HTTPS OkHttpClient is an HTTP OkHttpClient with extra configuration.
        OkUrlFactory okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxy);

        // All HTTPS requests are allowed.
        OkUrlFactories.setUrlFilter(okUrlFactory, null);

        OkHttpClient okHttpClient = okUrlFactory.client();

        // Only enable HTTP/1.1 (implies HTTP/1.0). Disable SPDY / HTTP/2.0.
        okHttpClient.setProtocols(HTTP_1_1_ONLY);

        okHttpClient.setConnectionSpecs(Collections.singletonList(TLS_CONNECTION_SPEC));

        // Android support certificate pinning via NetworkSecurityConfig so there is no need to
        // also expose OkHttp's mechanism. The OkHttpClient underlying https HttpsURLConnections
        // in Android should therefore always use the default certificate pinner, whose set of
        // {@code hostNamesToPin} is empty.
        okHttpClient.setCertificatePinner(CertificatePinner.DEFAULT);

        // OkHttp does not automatically honor the system-wide HostnameVerifier set with
        // HttpsURLConnection.setDefaultHostnameVerifier().
        okUrlFactory.client().setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        // OkHttp does not automatically honor the system-wide SSLSocketFactory set with
        // HttpsURLConnection.setDefaultSSLSocketFactory().
        // See https://github.com/square/okhttp/issues/184 for details.
        okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());

        return okUrlFactory;
    }
}
