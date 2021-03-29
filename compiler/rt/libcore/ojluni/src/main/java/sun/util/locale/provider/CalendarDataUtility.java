/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.util.locale.provider;

import android.icu.text.DateFormatSymbols;
import android.icu.util.ULocale;

import static java.util.Calendar.*;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Android-changed: remove mention of CalendarDataProvider that's not used on Android.
/**
 * {@code CalendarDataUtility} is a utility class for getting calendar field name values.
 *
 * @author Masayoshi Okutsu
 * @author Naoto Sato
 */
public class CalendarDataUtility {
    // Android-note: This class has been rewritten from scratch and is effectively forked.
    // The API (names of public constants, method signatures etc.) generally derives
    // from OpenJDK so that other OpenJDK code that refers to this class doesn't need to
    // be changed, but the implementation has been rewritten; logic / identifiers
    // that weren't used from anywhere else have been dropped altogether.

    // Android-removed: Dead code, unused on Android.
    // public final static String FIRST_DAY_OF_WEEK = "firstDayOfWeek";
    // public final static String MINIMAL_DAYS_IN_FIRST_WEEK = "minimalDaysInFirstWeek";

    // Android-added: Calendar name constants for use in retrievFieldValueName.
    private static final String ISLAMIC_CALENDAR = "islamic";
    private static final String GREGORIAN_CALENDAR = "gregorian";
    private static final String BUDDHIST_CALENDAR = "buddhist";
    private static final String JAPANESE_CALENDAR = "japanese";

    // Android-added: REST_OF_STYLES array for use in retrieveFieldValueNames.
    // ALL_STYLES implies SHORT_FORMAT and all of these values.
    private static int[] REST_OF_STYLES = {
            SHORT_STANDALONE, LONG_FORMAT, LONG_STANDALONE,
            NARROW_FORMAT, NARROW_STANDALONE
    };

    // No instantiation
    private CalendarDataUtility() {
    }

    // BEGIN Android-removed: Dead code, unused on Android.
    // Clients should use libcore.icu.LocaleData or android.icu.util.Calendar.WeekData instead.
    /*
    public static int retrieveFirstDayOfWeek(Locale locale) {
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarDataProvider.class);
        Integer value = pool.getLocalizedObject(CalendarWeekParameterGetter.INSTANCE,
                                                locale, FIRST_DAY_OF_WEEK);
        return (value != null && (value >= SUNDAY && value <= SATURDAY)) ? value : SUNDAY;
    }

    public static int retrieveMinimalDaysInFirstWeek(Locale locale) {
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarDataProvider.class);
        Integer value = pool.getLocalizedObject(CalendarWeekParameterGetter.INSTANCE,
                                                locale, MINIMAL_DAYS_IN_FIRST_WEEK);
        return (value != null && (value >= 1 && value <= 7)) ? value : 1;
    }
    */
    // END Android-removed: Dead code, unused on Android.

    // BEGIN Android-changed: Implement on top of ICU.
    /*
    public static String retrieveFieldValueName(String id, int field, int value, int style, Locale locale) {
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                       field, value, style, false);
    }
    */
    public static String retrieveFieldValueName(String id, int field, int value, int style,
            Locale locale) {
        if (field == Calendar.ERA) {
            // For era the field value does not always equal the index into the names array.
            switch (normalizeCalendarType(id)) {
                // These calendars have only one era, but represented it by the value 1.
                case BUDDHIST_CALENDAR:
                case ISLAMIC_CALENDAR:
                    value -= 1;
                    break;
                case JAPANESE_CALENDAR:
                    // CLDR contains full data for historical eras, java.time only supports the 4
                    // modern eras and numbers the modern eras starting with 1 (MEIJI). There are
                    // 232 historical eras in CLDR/ICU so to get the real offset, we add 231.
                    value += 231;
                    break;
                default:
                    // Other eras use 0-based values (e.g. 0=BCE, 1=CE for gregorian).
                    break;
            }
        }
        if (value < 0) {
            return null;
        }
        String[] names = getNames(id, field, style, locale);
        if (value >= names.length) {
            return null;
        }
        return names[value];
    }
    // END Android-changed: Implement on top of ICU.

    // BEGIN Android-changed: Implement on top of ICU.
    /*
    public static String retrieveJavaTimeFieldValueName(String id, int field, int value, int style, Locale locale) {
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        String name;
        name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                       field, value, style, true);
        if (name == null) {
            name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, normalizeCalendarType(id),
                                           field, value, style, false);
        }
        return name;
    }
    */
    public static String retrieveJavaTimeFieldValueName(String id, int field, int value, int style,
            Locale locale) {
        // Don't distinguish between retrieve* and retrieveJavaTime* methods.
        return retrieveFieldValueName(id, field, value, style, locale);
    }
    // END Android-changed: Implement on top of ICU.

    // BEGIN Android-changed: Implement on top of ICU.
    /*
    public static Map<String, Integer> retrieveFieldValueNames(String id, int field, int style, Locale locale) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                       normalizeCalendarType(id), field, style, false);
    }
    */
    public static Map<String, Integer> retrieveFieldValueNames(String id, int field, int style,
            Locale locale) {
        Map<String, Integer> names;
        if (style == ALL_STYLES) {
            names = retrieveFieldValueNamesImpl(id, field, SHORT_FORMAT, locale);
            for (int st : REST_OF_STYLES) {
                names.putAll(retrieveFieldValueNamesImpl(id, field, st, locale));
            }
        } else {
            // specific style
            names = retrieveFieldValueNamesImpl(id, field, style, locale);
        }
        return names.isEmpty() ? null : names;
    }
    // END Android-changed: Implement on top of ICU.

    // BEGIN Android-changed: Implement on top of ICU.
    /*
    public static Map<String, Integer> retrieveJavaTimeFieldValueNames(String id, int field, int style, Locale locale) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        Map<String, Integer> map;
        map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                       normalizeCalendarType(id), field, style, true);
        if (map == null) {
            map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale,
                                           normalizeCalendarType(id), field, style, false);
        }
        return map;
    }
    */
    public static Map<String, Integer> retrieveJavaTimeFieldValueNames(String id, int field,
            int style, Locale locale) {
        // Don't distinguish between retrieve* and retrieveJavaTime* methods.
        return retrieveFieldValueNames(id, field, style, locale);
    }
    // END Android-changed: Implement on top of ICU.

    // Android-changed: Added private modifier for normalizeCalendarType().
    // static String normalizeCalendarType(String requestID) {
    private static String normalizeCalendarType(String requestID) {
        String type;
        // Android-changed: normalize "gregory" to "gregorian", not the other way around.
        // See android.icu.text.DateFormatSymbols.CALENDAR_CLASSES for reference.
        // if (requestID.equals("gregorian") || requestID.equals("iso8601")) {
        //    type = "gregory";
        // } else if (requestID.startsWith("islamic")) {
        //    type = "islamic";
        if (requestID.equals("gregory") || requestID.equals("iso8601")) {
            type = GREGORIAN_CALENDAR;
        } else if (requestID.startsWith(ISLAMIC_CALENDAR)) {
            type = ISLAMIC_CALENDAR;
        } else {
            type = requestID;
        }
        return type;
    }

    // BEGIN Android-added: Various private helper methods.
    private static Map<String, Integer> retrieveFieldValueNamesImpl(String id, int field, int style,
            Locale locale) {
        String[] names = getNames(id, field, style, locale);
        int skipped = 0;
        int offset = 0;
        if (field == Calendar.ERA) {
            // See retrieveFieldValueName() for explanation of this code and the values used.
            switch (normalizeCalendarType(id)) {
                case BUDDHIST_CALENDAR:
                case ISLAMIC_CALENDAR:
                    offset = 1;
                    break;
                case JAPANESE_CALENDAR:
                    skipped = 232;
                    offset = -231;
                    break;
                default:
                    break;
            }
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = skipped; i < names.length; i++) {
            if (names[i].isEmpty()) {
                continue;
            }

            if (result.put(names[i], i + offset) != null) {
                // Duplicate names indicate that the names would be ambiguous. Skip this style for
                // ALL_STYLES. In other cases this results in null being returned in
                // retrieveValueNames(), which is required by Calendar.getDisplayNames().
                return new LinkedHashMap<>();
            }
        }
        return result;
    }

    private static String[] getNames(String id, int field, int style, Locale locale) {
        int context = toContext(style);
        int width = toWidth(style);
        DateFormatSymbols symbols = getDateFormatSymbols(id, locale);
        switch (field) {
            case Calendar.MONTH:
                return symbols.getMonths(context, width);
            case Calendar.ERA:
                switch (width) {
                    case DateFormatSymbols.NARROW:
                        return symbols.getNarrowEras();
                    case DateFormatSymbols.ABBREVIATED:
                        return symbols.getEras();
                    case DateFormatSymbols.WIDE:
                        return symbols.getEraNames();
                    default:
                        throw new UnsupportedOperationException("Unknown width: " + width);
                }
            case Calendar.DAY_OF_WEEK:
                return symbols.getWeekdays(context, width);
            case Calendar.AM_PM:
                return symbols.getAmPmStrings();
            default:
                throw new UnsupportedOperationException("Unknown field: " + field);
        }
    }

    private static DateFormatSymbols getDateFormatSymbols(String id, Locale locale) {
        String calendarType = normalizeCalendarType(id);
        return new DateFormatSymbols(ULocale.forLocale(locale), calendarType);
    }

    /**
     * Transform a {@link Calendar} style constant into an ICU width value.
     */
    private static int toWidth(int style) {
        switch (style) {
            case Calendar.SHORT_FORMAT:
            case Calendar.SHORT_STANDALONE:
                return DateFormatSymbols.ABBREVIATED;
            case Calendar.NARROW_FORMAT:
            case Calendar.NARROW_STANDALONE:
                return DateFormatSymbols.NARROW;
            case Calendar.LONG_FORMAT:
            case Calendar.LONG_STANDALONE:
                return DateFormatSymbols.WIDE;
            default:
                throw new IllegalArgumentException("Invalid style: " + style);
        }
    }

    /**
     * Transform a {@link Calendar} style constant into an ICU context value.
     */
    private static int toContext(int style) {
        switch (style) {
            case Calendar.SHORT_FORMAT:
            case Calendar.NARROW_FORMAT:
            case Calendar.LONG_FORMAT:
                return DateFormatSymbols.FORMAT;
            case Calendar.SHORT_STANDALONE:
            case Calendar.NARROW_STANDALONE:
            case Calendar.LONG_STANDALONE:
                return DateFormatSymbols.STANDALONE;
            default:
                throw new IllegalArgumentException("Invalid style: " + style);
        }
    }
    // END Android-added: Various private helper methods.

    // BEGIN Android-removed: Dead code, unused on Android.
    /*
    /**
     * Obtains a localized field value string from a CalendarDataProvider
     * implementation.
     *
    private static class CalendarFieldValueNameGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarNameProvider,
                                                                   String> {
        private static final CalendarFieldValueNameGetter INSTANCE =
            new CalendarFieldValueNameGetter();

        @Override
        public String getObject(CalendarNameProvider calendarNameProvider,
                                Locale locale,
                                String requestID, // calendarType
                                Object... params) {
            assert params.length == 4;
            int field = (int) params[0];
            int value = (int) params[1];
            int style = (int) params[2];
            boolean javatime = (boolean) params[3];

            // If javatime is true, resources from CLDR have precedence over JRE
            // native resources.
            if (javatime && calendarNameProvider instanceof CalendarNameProviderImpl) {
                String name;
                name = ((CalendarNameProviderImpl)calendarNameProvider)
                        .getJavaTimeDisplayName(requestID, field, value, style, locale);
                return name;
            }
            return calendarNameProvider.getDisplayName(requestID, field, value, style, locale);
        }
    }

    /**
     * Obtains a localized field-value pairs from a CalendarDataProvider
     * implementation.
     *
    private static class CalendarFieldValueNamesMapGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarNameProvider,
                                                                   Map<String, Integer>> {
        private static final CalendarFieldValueNamesMapGetter INSTANCE =
            new CalendarFieldValueNamesMapGetter();

        @Override
        public Map<String, Integer> getObject(CalendarNameProvider calendarNameProvider,
                                              Locale locale,
                                              String requestID, // calendarType
                                              Object... params) {
            assert params.length == 3;
            int field = (int) params[0];
            int style = (int) params[1];
            boolean javatime = (boolean) params[2];

            // If javatime is true, resources from CLDR have precedence over JRE
            // native resources.
            if (javatime && calendarNameProvider instanceof CalendarNameProviderImpl) {
                Map<String, Integer> map;
                map = ((CalendarNameProviderImpl)calendarNameProvider)
                        .getJavaTimeDisplayNames(requestID, field, style, locale);
                return map;
            }
            return calendarNameProvider.getDisplayNames(requestID, field, style, locale);
        }
    }

     private static class CalendarWeekParameterGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarDataProvider,
                                                                   Integer> {
        private static final CalendarWeekParameterGetter INSTANCE =
            new CalendarWeekParameterGetter();

        @Override
        public Integer getObject(CalendarDataProvider calendarDataProvider,
                                 Locale locale,
                                 String requestID,    // resource key
                                 Object... params) {
            assert params.length == 0;
            int value;
            switch (requestID) {
            case FIRST_DAY_OF_WEEK:
                value = calendarDataProvider.getFirstDayOfWeek(locale);
                break;
            case MINIMAL_DAYS_IN_FIRST_WEEK:
                value = calendarDataProvider.getMinimalDaysInFirstWeek(locale);
                break;
            default:
                throw new InternalError("invalid requestID: " + requestID);
            }
            return (value != 0) ? value : null;
        }
    }
    */
    // END Android-removed: Dead code, unused on Android.
}
