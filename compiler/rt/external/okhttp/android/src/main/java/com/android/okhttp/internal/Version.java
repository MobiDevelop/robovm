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

import dalvik.annotation.compat.UnsupportedAppUsage;

/**
 * @hide This class is not part of the Android public SDK API
 */
public final class Version {
  @UnsupportedAppUsage
  public static String userAgent() {
    String agent = System.getProperty("http.agent");
    return agent != null ? agent : ("Java" + System.getProperty("java.version"));
  }

  private Version() {
  }
}
