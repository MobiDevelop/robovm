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

package libcore.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A container class for debug information.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public class DebugInfo {
    private final List<DebugEntry> entries;

    @libcore.api.CorePlatformApi
    public DebugInfo() {
        this.entries = new ArrayList<>();
    }

    /**
     * Adds a key / string value.
     *
     * @return {@code this} for chaining calls
     */
    @libcore.api.CorePlatformApi
    public DebugInfo addStringEntry(String key, String value) {
        entries.add(new DebugEntry(key, value));
        return this;
    }

    /**
     * Adds a key / string value. Converts the supplied int value to a String.
     *
     * @return {@code this} for chaining calls
     */
    @libcore.api.CorePlatformApi
    public DebugInfo addStringEntry(String key, int value) {
        addStringEntry(key, Integer.toString(value));
        return this;
    }

    /** Returns all the debug entries. */
    @libcore.api.CorePlatformApi
    public List<DebugEntry> getDebugEntries() {
        return entries;
    }

    /** Returns the first debug entry with the given key, or {@code null} if it does not exist. */
    public DebugEntry getDebugEntry(String key) {
        for (DebugEntry entry : getDebugEntries()) {
            if (key.equals(entry.getKey())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * A generic key/value for a single piece of debug information.
     *
     * @hide
     */
    @libcore.api.CorePlatformApi
    public static class DebugEntry {
        private final String key;
        private final String stringValue;

        @libcore.api.CorePlatformApi
        public DebugEntry(String key, String stringValue) {
            this.key = key;
            this.stringValue = stringValue;
        }

        @libcore.api.CorePlatformApi
        public String getKey() {
            return key;
        }

        @libcore.api.CorePlatformApi
        public String getStringValue() {
            return stringValue;
        }
    }
}
