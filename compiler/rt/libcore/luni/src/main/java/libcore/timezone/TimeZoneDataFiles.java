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

package libcore.timezone;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods associated with finding updateable time zone data files.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class TimeZoneDataFiles {
    private static final String ANDROID_ROOT_ENV = "ANDROID_ROOT";
    private static final String ANDROID_RUNTIME_ROOT_ENV = "ANDROID_RUNTIME_ROOT";
    private static final String ANDROID_TZDATA_ROOT_ENV = "ANDROID_TZDATA_ROOT";
    private static final String ANDROID_DATA_ENV = "ANDROID_DATA";

    private TimeZoneDataFiles() {}

    /**
     * Returns time zone file paths for the specified file name in an array in the order they
     * should be tried. See {@link #generateIcuDataPath()} for ICU files instead.
     * <ul>
     * <li>[0] - the location of the file in the /data partition (may not exist).</li>
     * <li>[1] - the location of the file from the time zone module under /apex (may not exist).
     * </li>
     * <li>[2] - the location of the file from the runtime module under /apex (should exist).</li>
     * </ul>
     * <li>[3] - the location of the file in the /system partition (should exist).</li>
     */
    // VisibleForTesting
    public static String[] getTimeZoneFilePaths(String fileName) {
        return new String[] {
                getDataTimeZoneFile(fileName),
                getTimeZoneModuleFile("tz/" + fileName),
                getRuntimeModuleFile("tz/" + fileName),
                getSystemTimeZoneFile(fileName)
        };
    }

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    @libcore.api.CorePlatformApi
    public static String getDataTimeZoneRootDir() {
        return System.getenv(ANDROID_DATA_ENV) + "/misc/zoneinfo/";
    }

    @libcore.api.CorePlatformApi
    public static String getDataTimeZoneFile(String fileName) {
        return getDataTimeZoneRootDir() + "current/" + fileName;
    }

    public static String getTimeZoneModuleFile(String fileName) {
        return System.getenv(ANDROID_TZDATA_ROOT_ENV) + "/etc/" + fileName;
    }

    // Remove from CorePlatformApi when all users in platform code are removed. http://b/123398797
    @libcore.api.CorePlatformApi
    public static String getRuntimeModuleTzVersionFile() {
        return getRuntimeModuleFile("tz/" + TzDataSetVersion.DEFAULT_FILE_NAME);
    }

    public static String getRuntimeModuleFile(String fileName) {
        return System.getenv(ANDROID_RUNTIME_ROOT_ENV) + "/etc/" + fileName;
    }

    public static String getSystemTimeZoneFile(String fileName) {
        return getEnvironmentPath(ANDROID_ROOT_ENV, "/usr/share/zoneinfo/" + fileName);
    }

    public static String getSystemIcuFile(String fileName) {
        return getEnvironmentPath(ANDROID_ROOT_ENV, "/usr/icu/" + fileName);
    }

    public static String generateIcuDataPath() {
        List<String> paths = new ArrayList<>(3);

        // ICU should first look in ANDROID_DATA. This is used for (optional) time zone data
        // delivered by APK (https://source.android.com/devices/tech/config/timezone-rules)
        String dataIcuDataPath =
                getEnvironmentPath(ANDROID_DATA_ENV, "/misc/zoneinfo/current/icu/");
        if (dataIcuDataPath != null) {
            paths.add(dataIcuDataPath);
        }

        // ICU should then look for a mounted time zone module file in /apex. This is used for
        // (optional) time zone data that can be updated with an APEX file.
        String timeZoneModuleIcuDataPath = getTimeZoneModuleFile("icu/");
        if (timeZoneModuleIcuDataPath != null) {
            paths.add(timeZoneModuleIcuDataPath);
        }

        // ICU should always look in the runtime module path as this is where most of the data
        // can be found.
        String runtimeModuleIcuDataPath = getRuntimeModuleFile("icu/");
        if (runtimeModuleIcuDataPath != null) {
            paths.add(runtimeModuleIcuDataPath);
        }
        return String.join(":", paths);
    }

    /**
     * Creates a path by combining the value of an environment variable with a relative path.
     * Returns {@code null} if the environment variable is not set.
     */
    private static String getEnvironmentPath(String environmentVariable, String path) {
        String variable = System.getenv(environmentVariable);
        if (variable == null) {
            return null;
        }
        return variable + path;
    }
}
