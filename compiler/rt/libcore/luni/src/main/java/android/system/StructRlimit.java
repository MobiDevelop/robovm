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
 * limitations under the License
 */

package android.system;

import libcore.util.Objects;

/**
 * Information returned by {@link Os#getrlimit}. Corresponds to C's {@code struct rlimit} from
 * {@code <sys/resource.h>}.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class StructRlimit {
    @libcore.api.CorePlatformApi
    public final long rlim_cur;
    public final long rlim_max;

    public StructRlimit(long rlim_cur, long rlim_max) {
        this.rlim_cur = rlim_cur;
        this.rlim_max = rlim_max;
    }

    @Override public String toString() {
        return Objects.toString(this);
    }
}
