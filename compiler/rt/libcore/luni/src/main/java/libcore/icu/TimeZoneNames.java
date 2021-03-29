/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.icu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import libcore.util.BasicLruCache;
import libcore.timezone.ZoneInfoDB;

/**
 * Provides access to ICU's time zone name data.
 * @hide
 */
public final class TimeZoneNames {
    private static final String[] availableTimeZoneIds = TimeZone.getAvailableIDs();

    /*
     * Offsets into the arrays returned by DateFormatSymbols.getZoneStrings.
     */
    public static final int OLSON_NAME = 0;
    public static final int LONG_NAME = 1;
    public static final int SHORT_NAME = 2;
    public static final int LONG_NAME_DST = 3;
    public static final int SHORT_NAME_DST = 4;
    public static final int NAME_COUNT = 5;

    private static final ZoneStringsCache cachedZoneStrings = new ZoneStringsCache();

    /** @hide */
    public static class ZoneStringsCache extends BasicLruCache<Locale, String[][]> {
        public ZoneStringsCache() {
            super(5); // Room for a handful of locales.
        }

        @Override protected String[][] create(Locale locale) {
            long start = System.nanoTime();

            // Set up the 2D array used to hold the names. The first column contains the Olson ids.
            String[][] result = new String[availableTimeZoneIds.length][5];
            for (int i = 0; i < availableTimeZoneIds.length; ++i) {
                result[i][0] = availableTimeZoneIds[i];
            }

            long nativeStart = System.nanoTime();
            fillZoneStrings(locale.toLanguageTag(), result);
            long nativeEnd = System.nanoTime();

            addOffsetStrings(result);
            internStrings(result);
            // Ending up in this method too often is an easy way to make your app slow, so we ensure
            // it's easy to tell from the log (a) what we were doing, (b) how long it took, and
            // (c) that it's all ICU's fault.
            long end = System.nanoTime();
            long nativeDuration = TimeUnit.NANOSECONDS.toMillis(nativeEnd - nativeStart);
            long duration = TimeUnit.NANOSECONDS.toMillis(end - start);
            System.logI("Loaded time zone names for \"" + locale + "\" in " + duration + "ms" +
                        " (" + nativeDuration + "ms in ICU)");
            return result;
        }

        /**
         * Generate offset strings for cases where we don't have a name. Note that this is a
         * potentially slow operation, as we need to load the timezone data for all affected
         * time zones.
         */
        private void addOffsetStrings(String[][] result) {
            for (int i = 0; i < result.length; ++i) {
                TimeZone tz = null;
                for (int j = 1; j < NAME_COUNT; ++j) {
                    if (result[i][j] != null) {
                        continue;
                    }
                    if (tz == null) {
                        tz = TimeZone.getTimeZone(result[i][0]);
                    }
                    int offsetMillis = tz.getRawOffset();
                    if (j == LONG_NAME_DST || j == SHORT_NAME_DST) {
                        offsetMillis += tz.getDSTSavings();
                    }
                    result[i][j] = TimeZone.createGmtOffsetString(
                            /* includeGmt */ true, /*includeMinuteSeparator */true, offsetMillis);
                }
            }
        }

        // De-duplicate the strings (http://b/2672057).
        private void internStrings(String[][] result) {
            HashMap<String, String> internTable = new HashMap<String, String>();
            for (int i = 0; i < result.length; ++i) {
                for (int j = 1; j < NAME_COUNT; ++j) {
                    String original = result[i][j];
                    String nonDuplicate = internTable.get(original);
                    if (nonDuplicate == null) {
                        internTable.put(original, original);
                    } else {
                        result[i][j] = nonDuplicate;
                    }
                }
            }
        }
    }

    private static final Comparator<String[]> ZONE_STRINGS_COMPARATOR = new Comparator<String[]>() {
        public int compare(String[] lhs, String[] rhs) {
            return lhs[OLSON_NAME].compareTo(rhs[OLSON_NAME]);
        }
    };

    private TimeZoneNames() {}

    /**
     * Returns the appropriate string from 'zoneStrings'. Used with getZoneStrings.
     */
    public static String getDisplayName(String[][] zoneStrings, String id, boolean daylight, int style) {
        String[] needle = new String[] { id };
        int index = Arrays.binarySearch(zoneStrings, needle, ZONE_STRINGS_COMPARATOR);
        if (index >= 0) {
            String[] row = zoneStrings[index];
            if (daylight) {
                return (style == TimeZone.LONG) ? row[LONG_NAME_DST] : row[SHORT_NAME_DST];
            } else {
                return (style == TimeZone.LONG) ? row[LONG_NAME] : row[SHORT_NAME];
            }
        }
        return null;
    }

    /**
     * Returns an array of time zone strings, as used by DateFormatSymbols.getZoneStrings.
     */
    public static String[][] getZoneStrings(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return cachedZoneStrings.get(locale);
    }

    private static native void fillZoneStrings(String locale, String[][] result);
}
