/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.okhttp.internalandroidapi;

import com.android.okhttp.ConnectionPool;
import com.android.okhttp.HttpHandler;
import com.android.okhttp.HttpsHandler;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.OkUrlFactories;
import com.android.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;

/**
 * A way to construct {@link java.net.HttpURLConnection}s that supports some
 * configuration on a per-factory or per-connection basis rather than only via
 * global static state such as {@link java.net.CookieHandler#setDefault(java.net.CookieHandler)}.
 * The per-factory configuration is <b>optional</b>; if not set, global
 * configuration or default behavior is used.
 *
 * This facade prevents tight coupling with the underlying implementation (on
 * top of a particular version of OkHttp). Android code outside of libcore
 * should never depend directly on OkHttp.
 *
 * This abstraction is not suitable for general use. Talk to the maintainers of
 * this class before modifying it or adding additional dependencies.
 *
 * @hide
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public final class HttpURLConnectionFactory {

    private ConnectionPool connectionPool;
    private com.android.okhttp.Dns dns;

    @libcore.api.CorePlatformApi
    public HttpURLConnectionFactory() {
    }

    /**
     * Sets a new ConnectionPool, specific to this URLFactory and not shared with
     * any other connections, with the given configuration.
     */
    @libcore.api.CorePlatformApi
    public void setNewConnectionPool(int maxIdleConnections, long keepAliveDuration,
            TimeUnit timeUnit) {
        this.connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    @libcore.api.CorePlatformApi
    public void setDns(Dns dns) {
        Objects.requireNonNull(dns);
        this.dns = new DnsAdapter(dns);
    }

    /**
     * Opens a connection that uses the system default proxy settings and SocketFactory.
     */
    public URLConnection openConnection(URL url) throws IOException {
        return internalOpenConnection(url, null /* socketFactory */, null /* proxy */);
    }

    /**
     * Opens a connection that uses the system default SocketFactory and the specified
     * proxy settings.
     */
    public URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        Objects.requireNonNull(proxy);
        return internalOpenConnection(url, null /* socketFactory */, proxy);
    }

    /**
     * Opens a connection that uses the specified SocketFactory and the system default
     * proxy settings.
     */
    public URLConnection openConnection(URL url, SocketFactory socketFactory) throws IOException {
        Objects.requireNonNull(socketFactory);
        return internalOpenConnection(url, socketFactory, null /* proxy */);
    }

    /**
     * Opens a connection using the specified SocketFactory and the specified proxy
     * settings, overriding any system wide configuration.
     */
    @libcore.api.CorePlatformApi
    public URLConnection openConnection(URL url, SocketFactory socketFactory, Proxy proxy)
            throws IOException {
        Objects.requireNonNull(socketFactory);
        Objects.requireNonNull(proxy);
        return internalOpenConnection(url, socketFactory, proxy);
    }

    private URLConnection internalOpenConnection(URL url, SocketFactory socketFactoryOrNull,
            Proxy proxyOrNull) throws IOException {
        String protocol = url.getProtocol();
        OkUrlFactory okUrlFactory;
        // TODO: HttpHandler creates OkUrlFactory instances that share the default ResponseCache.
        // Could this cause unexpected behavior?
        if (protocol.equals("http")) {
            okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxyOrNull);
        } else if (protocol.equals("https")) {
            okUrlFactory = HttpsHandler.createHttpsOkUrlFactory(proxyOrNull);
        } else {
            // OkHttp only supports HTTP and HTTPS.
            throw new MalformedURLException("Invalid URL or unrecognized protocol " + protocol);
        }

        OkHttpClient client = okUrlFactory.client();
        if (connectionPool != null) {
            client.setConnectionPool(connectionPool);
        }
        if (dns != null) {
            client.setDns(dns);
        }
        if (socketFactoryOrNull != null) {
            client.setSocketFactory(socketFactoryOrNull);
        }
        if (proxyOrNull == null) {
            return okUrlFactory.open(url);
        } else {
            return OkUrlFactories.open(okUrlFactory, url, proxyOrNull);
        }
    }

    /**
     * Adapts a {@link Dns} as a {@link com.android.okhttp.Dns}.
     */
    static final class DnsAdapter implements com.android.okhttp.Dns {
        private final Dns adaptee;

        DnsAdapter(Dns adaptee) {
            this.adaptee = Objects.requireNonNull(adaptee);
        }

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            return adaptee.lookup(hostname);
        }

        @Override
        public int hashCode() {
            return 31 * DnsAdapter.class.hashCode() + adaptee.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DnsAdapter)) {
                return false;
            }
            return adaptee.equals(((DnsAdapter) obj).adaptee);
        }

        @Override
        public String toString() {
            return adaptee.toString();
        }
    }

}
