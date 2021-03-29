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
 * Corresponds to Linux' __user_cap_data_struct for capget and capset.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class StructCapUserData {
    /** Effective capability mask. */
    @libcore.api.CorePlatformApi
    public final int effective; /* __u32 */

    /** Permitted capability mask. */
    @libcore.api.CorePlatformApi
    public final int permitted; /* __u32 */

    /** Inheritable capability mask. */
    @libcore.api.CorePlatformApi
    public final int inheritable; /* __u32 */

    /**
     * Constructs an instance with the given field values.
     */
    @libcore.api.CorePlatformApi
    public StructCapUserData(int effective, int permitted, int inheritable) {
        this.effective = effective;
        this.permitted = permitted;
        this.inheritable = inheritable;
    }

    @Override public String toString() {
        return Objects.toString(this);
    }
}
