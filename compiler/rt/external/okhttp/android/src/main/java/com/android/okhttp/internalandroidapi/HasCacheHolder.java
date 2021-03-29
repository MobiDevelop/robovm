/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 * Copyright (C) 2018 The Android Open Source Project
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

import com.android.okhttp.Cache;

import java.io.File;

/**
 * An interface used to indicate a class can return a {@link CacheHolder} object.
 * @hide
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public interface HasCacheHolder {

    /**
     * Returns the {@link CacheHolder} object.
     */
    @libcore.api.CorePlatformApi
    CacheHolder getCacheHolder();

    /**
     * A holder for an OkHttp internal Cache object. This class exists as an opaque layer over
     * OkHttp internal classes.
     * @hide
     */
    @libcore.api.CorePlatformApi
    final class CacheHolder {

        private final Cache okHttpCache;

        private CacheHolder(Cache okHttpCache) {
            this.okHttpCache = okHttpCache;
        }

        // This constructor is required for @libcore.api.CorePlatformApi stubs generation. Without
        // it the constructor above is included in the stubs code which adds a dependency on
        // okhttp.Cache that we don't want.
        @SuppressWarnings("unused")
        private CacheHolder() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the underlying {@link Cache} object.
         * @hide
         */
        public Cache getCache() {
            return okHttpCache;
        }

        /**
         * Returns a new {@link CacheHolder} containing an OKHttp Cache with the specified settings.
         *
         * @param directory a writable directory
         * @param maxSizeBytes the maximum number of bytes this cache should use to store
         */
        @libcore.api.CorePlatformApi
        public static CacheHolder create(File directory, long maxSizeBytes) {
            Cache cache = new Cache(directory, maxSizeBytes);
            return new CacheHolder(cache);
        }

        /**
         * Returns true if the arguments supplied would result in an equivalent cache to this one
         * being created if they were passed to {@link #create(File, long)}.
         */
        @libcore.api.CorePlatformApi
        public boolean isEquivalent(File directory, long maxSizeBytes) {
            return (okHttpCache.getDirectory().equals(directory)
                    && okHttpCache.getMaxSize() == maxSizeBytes
                    && !okHttpCache.isClosed());
        }
    }
}
