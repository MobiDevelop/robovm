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

import libcore.net.event.NetworkEventDispatcher;
import libcore.net.event.NetworkEventListener;

/**
 * A provider of the shared Android {@link ConnectionPool}. This class is aware of network
 * configuration change events: When the network configuration changes the pool object is discarded
 * and a later calls to {@link #get()} will return a new pool.
 * @hide This class is not part of the Android public SDK API
 */
public class ConfigAwareConnectionPool {

  private static final long CONNECTION_POOL_DEFAULT_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min

  private static final int CONNECTION_POOL_MAX_IDLE_CONNECTIONS;
  private static final long CONNECTION_POOL_KEEP_ALIVE_DURATION_MS;
  static {
    String keepAliveProperty = System.getProperty("http.keepAlive");
    String keepAliveDurationProperty = System.getProperty("http.keepAliveDuration");
    String maxIdleConnectionsProperty = System.getProperty("http.maxConnections");
    CONNECTION_POOL_KEEP_ALIVE_DURATION_MS = (keepAliveDurationProperty != null
        ? Long.parseLong(keepAliveDurationProperty)
        : CONNECTION_POOL_DEFAULT_KEEP_ALIVE_DURATION_MS);
    if (keepAliveProperty != null && !Boolean.parseBoolean(keepAliveProperty)) {
      CONNECTION_POOL_MAX_IDLE_CONNECTIONS = 0;
    } else if (maxIdleConnectionsProperty != null) {
      CONNECTION_POOL_MAX_IDLE_CONNECTIONS = Integer.parseInt(maxIdleConnectionsProperty);
    } else {
      CONNECTION_POOL_MAX_IDLE_CONNECTIONS = 5;
    }
  }

  private static final ConfigAwareConnectionPool instance = new ConfigAwareConnectionPool();

  private final NetworkEventDispatcher networkEventDispatcher;

  /**
   * {@code true} if the ConnectionPool reset has been registered with the
   * {@link NetworkEventDispatcher}.
   */
  private boolean networkEventListenerRegistered;

  private ConnectionPool connectionPool;

  /** Visible for testing. Use {@link #getInstance()} */
  protected ConfigAwareConnectionPool(NetworkEventDispatcher networkEventDispatcher) {
    this.networkEventDispatcher = networkEventDispatcher;
  }

  private ConfigAwareConnectionPool() {
    networkEventDispatcher = NetworkEventDispatcher.getInstance();
  }

  public static ConfigAwareConnectionPool getInstance() {
    return instance;
  }

  /**
   * Returns the current {@link ConnectionPool} to use.
   */
  public synchronized ConnectionPool get() {
    if (connectionPool == null) {
      // Only register the listener once the first time a ConnectionPool is created.
      if (!networkEventListenerRegistered) {
        networkEventDispatcher.addListener(new NetworkEventListener() {
          @Override
          public void onNetworkConfigurationChanged() {
            synchronized (ConfigAwareConnectionPool.this) {
              // If the network config has changed then existing pooled connections should not be
              // re-used. By setting connectionPool to null it ensures that the next time
              // getConnectionPool() is called a new pool will be created.
              connectionPool = null;
            }
          }
        });
        networkEventListenerRegistered = true;
      }
      connectionPool = new ConnectionPool(
          CONNECTION_POOL_MAX_IDLE_CONNECTIONS, CONNECTION_POOL_KEEP_ALIVE_DURATION_MS);
    }
    return connectionPool;
  }
}
