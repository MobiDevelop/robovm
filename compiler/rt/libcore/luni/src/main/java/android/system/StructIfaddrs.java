/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.system;

import java.net.InetAddress;

/**
 * Information returned by {@link Os#getifaddrs}. Loosely corresponds to C's
 * {@code struct ifaddrs} from {@code <ifaddrs.h>}.
 *
 * @hide
 */
public final class StructIfaddrs {
    public final String ifa_name;
    public final int ifa_flags;
    public final InetAddress ifa_addr;
    public final InetAddress ifa_netmask;
    public final InetAddress ifa_broadaddr;
    public final byte[] hwaddr;

    /**
     * Constructs an instance with the given field values.
     */
    public StructIfaddrs(String ifa_name, int ifa_flags, InetAddress ifa_addr, InetAddress ifa_netmask,
                         InetAddress ifa_broadaddr, byte[] hwaddr) {
        this.ifa_name = ifa_name;
        this.ifa_flags = ifa_flags;
        this.ifa_addr = ifa_addr;
        this.ifa_netmask = ifa_netmask;
        this.ifa_broadaddr = ifa_broadaddr;
        this.hwaddr = hwaddr;
    }
}
