/*
 * Copyright (C) 2015 The Android Open Source Project
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

package libcore.util;

/**
 * Provides a hacky method that always throws {@code t} even if {@code t} is a checked exception.
 * and is not declared to be thrown.
 *
 * See
 * http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public class SneakyThrow {

    private SneakyThrow() {
    }

    /**
     * A hacky method that always throws {@code t} even if {@code t} is a checked exception,
     * and is not declared to be thrown.
     */
    @libcore.api.CorePlatformApi
    public static void sneakyThrow(Throwable t) {
        SneakyThrow.<RuntimeException>sneakyThrow_(t);
    }

    private static <T extends Throwable> void sneakyThrow_(Throwable t) throws T {
       throw (T) t;
    }
}
