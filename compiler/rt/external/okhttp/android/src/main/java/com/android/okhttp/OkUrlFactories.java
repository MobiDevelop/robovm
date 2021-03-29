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

package com.android.okhttp;

import com.android.okhttp.internal.URLFilter;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * Exposes nonpublic {@link OkUrlFactory} API for internal use by libcore.
 *
 * @hide
 * @hide This class is not part of the Android public SDK API
 */
public class OkUrlFactories {

    /** uninstantiable */
    private OkUrlFactories() {
    }

    public static HttpURLConnection open(OkUrlFactory okUrlFactory, URL url, Proxy proxy) {
        return okUrlFactory.open(url, proxy);
    }

    public static void setUrlFilter(OkUrlFactory okUrlFactory, URLFilter urlFilter) {
        okUrlFactory.setUrlFilter(urlFilter);
    }

}
