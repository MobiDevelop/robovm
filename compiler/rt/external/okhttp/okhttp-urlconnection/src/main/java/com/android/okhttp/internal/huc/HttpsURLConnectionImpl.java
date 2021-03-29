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
package com.android.okhttp.internal.huc;

import com.android.okhttp.Handshake;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.internal.URLFilter;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * @hide This class is not part of the Android public SDK API
 */
public final class HttpsURLConnectionImpl extends DelegatingHttpsURLConnection {
  @dalvik.annotation.compat.UnsupportedAppUsage
  private final HttpURLConnectionImpl delegate;

  public HttpsURLConnectionImpl(URL url, OkHttpClient client) {
    this(new HttpURLConnectionImpl(url, client));
  }

  public HttpsURLConnectionImpl(URL url, OkHttpClient client, URLFilter filter) {
    this(new HttpURLConnectionImpl(url, client, filter));
  }

  public HttpsURLConnectionImpl(HttpURLConnectionImpl delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @Override protected Handshake handshake() {
    if (delegate.httpEngine == null) {
      throw new IllegalStateException("Connection has not yet been established");
    }

    // If there's a response, get the handshake from there so that caching
    // works. Otherwise get the handshake from the connection because we might
    // have not connected yet.
    return delegate.httpEngine.hasResponse()
        ? delegate.httpEngine.getResponse().handshake()
        : delegate.handshake;
  }

  @Override public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    delegate.client.setHostnameVerifier(hostnameVerifier);
  }

  @Override public HostnameVerifier getHostnameVerifier() {
    return delegate.client.getHostnameVerifier();
  }

  @Override public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
    // BEGIN Android-added: Integrate upstream change: Let setSSLSocketFactory(null) throw.
    // https://github.com/square/okhttp/commit/f704f9d30e941ebdbdc95843c931cfc9d34bcba6
    // This method is documented to throw if sslSocketFactory == null. Setting the client's
    // sslSocketFactory to null instead would cause it to fall back to the default factory.
    // http://b/73702052
    if (sslSocketFactory == null) {
      throw new IllegalArgumentException("sslSocketFactory == null");
    }
    // END Android-added: Integrate upstream change: Let setSSLSocketFactory(null) throw.
    delegate.client.setSslSocketFactory(sslSocketFactory);
  }

  @Override public SSLSocketFactory getSSLSocketFactory() {
    return delegate.client.getSslSocketFactory();
  }

  @Override public long getContentLengthLong() {
    return delegate.getContentLengthLong();
  }

  @Override public void setFixedLengthStreamingMode(long contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override public long getHeaderFieldLong(String field, long defaultValue) {
    return delegate.getHeaderFieldLong(field, defaultValue);
  }
}
