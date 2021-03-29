/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2014 Square, Inc.
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
package com.android.okhttp.internal;

import com.android.okhttp.Address;
import com.android.okhttp.Call;
import com.android.okhttp.Callback;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.ConnectionSpec;
import com.android.okhttp.Headers;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.internal.http.StreamAllocation;
import com.android.okhttp.internal.io.RealConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;

/**
 * Escalate internal APIs in {@code com.squareup.okhttp} so they can be used
 * from OkHttp's implementation packages. The only implementation of this
 * interface is in {@link com.android.okhttp.OkHttpClient}.
 * @hide This class is not part of the Android public SDK API
 */
public abstract class Internal {
  public static final Logger logger = Logger.getLogger(OkHttpClient.class.getName());

  @dalvik.annotation.compat.UnsupportedAppUsage
  public Internal() {
  }

  public static void initializeInstanceForTests() {
    // Needed in tests to ensure that the instance is actually pointing to something.
    new OkHttpClient();
  }

  @dalvik.annotation.compat.UnsupportedAppUsage
  public static Internal instance;

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void addLenient(Headers.Builder builder, String line);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void addLenient(Headers.Builder builder, String name, String value);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void setCache(OkHttpClient client, InternalCache internalCache);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract InternalCache internalCache(OkHttpClient client);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract RealConnection get(
      ConnectionPool pool, Address address, StreamAllocation streamAllocation);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void put(ConnectionPool pool, RealConnection connection);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket,
      boolean isFallback);

  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract HttpUrl getHttpUrlChecked(String url)
      throws MalformedURLException, UnknownHostException;

  // TODO delete the following when web sockets move into the main package.
  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract void callEnqueue(Call call, Callback responseCallback, boolean forWebSocket);
  @dalvik.annotation.compat.UnsupportedAppUsage
  public abstract StreamAllocation callEngineGetStreamAllocation(Call call);
}
