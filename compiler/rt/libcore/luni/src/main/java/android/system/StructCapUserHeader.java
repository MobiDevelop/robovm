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

package android.system;

import libcore.util.Objects;

/**
 * Corresponds to Linux' __user_cap_header_struct for capget and capset.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class StructCapUserHeader {
    /**
     * Version of the header. Note this is not final as capget() may mutate the field when an
     * invalid version is provided. See
     * <a href="http://man7.org/linux/man-pages/man2/capget.2.html">capget(2)</a>.
     */
    public int version; /* __u32 */

    /** Pid of the header. The pid a call applies to. */
    public final int pid;

    /**
     * Constructs an instance with the given field values.
     */
    @libcore.api.CorePlatformApi
    public StructCapUserHeader(int version, int pid) {
        this.version = version;
        this.pid = pid;
    }

    @Override public String toString() {
        return Objects.toString(this);
    }
}
