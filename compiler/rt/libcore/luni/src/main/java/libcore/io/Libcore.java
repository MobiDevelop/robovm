/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.io;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.util.Objects;

/** @hide */
public final class Libcore {
    private Libcore() { }

    /**
     * Direct access to syscalls. Code should strongly prefer using {@link #os}
     * unless it has a strong reason to bypass the helpful checks/guards that it
     * provides.
     */
    public static final Os rawOs = new Posix(); // RoboVM Note: was: new Linux();

    /**
     * Access to syscalls with helpful checks/guards.
     * For read access only; the only supported way to update this field is via
     * {@link #compareAndSetOs}.
     */
    @UnsupportedAppUsage
    public static volatile Os os = new BlockGuardOs(rawOs);

    public static Os getOs() {
        return os;
    }

    /**
     * Updates {@link #os} if {@code os == expect}. The update is atomic with
     * respect to other invocations of this method.
     */
    public static boolean compareAndSetOs(Os expect, Os update) {
        Objects.requireNonNull(update);
        if (os != expect) {
            return false;
        }
        synchronized (Libcore.class) {
            boolean result = (os == expect);
            if (result) {
                os = update;
            }
            return result;
        }
    }
}
