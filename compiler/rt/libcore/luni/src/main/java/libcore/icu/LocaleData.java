/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.text.NumberingSystem;
import android.icu.util.UResourceBundle;
import dalvik.annotation.compat.UnsupportedAppUsage;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import libcore.util.Objects;

/**
 * Passes locale-specific from ICU native code to Java.
 * <p>
 * Note that you share these; you must not alter any of the fields, nor their array elements
 * in the case of arrays. If you ever expose any of these things to user code, you must give
 * them a clone rather than the original.
 * @hide
 */
@libcore.api.CorePlatformApi
public final class LocaleData {
    // A cache for the locale-specific data.
    private static final HashMap<String, LocaleData> localeDataCache = new HashMap<String, LocaleData>();
    static {
        // Ensure that we pull in the locale data for the root locale, en_US, and the
        // user's default locale. (All devices must support the root locale and en_US,
        // and they're used for various system things like HTTP headers.) Pre-populating
        // the cache is especially useful on Android because we'll share this via the Zygote.
        get(Locale.ROOT);
        get(Locale.US);
        get(Locale.getDefault());
    }

    // Used by Calendar.
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public Integer firstDayOfWeek;
    @UnsupportedAppUsage
    public Integer minimalDaysInFirstWeek;

    // Used by DateFormatSymbols.
    @libcore.api.CorePlatformApi
    public String[] amPm; // "AM", "PM".
    public String[] eras; // "BC", "AD".

    @libcore.api.CorePlatformApi
    public String[] longMonthNames; // "January", ...
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String[] shortMonthNames; // "Jan", ...
    @libcore.api.CorePlatformApi
    public String[] tinyMonthNames; // "J", ...
    @libcore.api.CorePlatformApi
    public String[] longStandAloneMonthNames; // "January", ...
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String[] shortStandAloneMonthNames; // "Jan", ...
    @libcore.api.CorePlatformApi
    public String[] tinyStandAloneMonthNames; // "J", ...

    @libcore.api.CorePlatformApi
    public String[] longWeekdayNames; // "Sunday", ...
    @libcore.api.CorePlatformApi
    public String[] shortWeekdayNames; // "Sun", ...
    @libcore.api.CorePlatformApi
    public String[] tinyWeekdayNames; // "S", ...
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String[] longStandAloneWeekdayNames; // "Sunday", ...
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String[] shortStandAloneWeekdayNames; // "Sun", ...
    @libcore.api.CorePlatformApi
    public String[] tinyStandAloneWeekdayNames; // "S", ...

    // Used by frameworks/base DateSorter and DateUtils.
    @libcore.api.CorePlatformApi
    public String yesterday; // "Yesterday".
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String today; // "Today".
    @UnsupportedAppUsage
    public String tomorrow; // "Tomorrow".

    public String fullTimeFormat;
    public String longTimeFormat;
    public String mediumTimeFormat;
    public String shortTimeFormat;

    public String fullDateFormat;
    public String longDateFormat;
    public String mediumDateFormat;
    public String shortDateFormat;

    // Used by TimePicker. Not currently used by UTS#35.
    @libcore.api.CorePlatformApi
    public String narrowAm; // "a".
    @libcore.api.CorePlatformApi
    public String narrowPm; // "p".

    // Used by DateFormat to implement 12- and 24-hour SHORT and MEDIUM.
    // They are also used directly by frameworks code.
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String timeFormat_hm;
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public String timeFormat_Hm;
    @libcore.api.CorePlatformApi
    public String timeFormat_hms;
    @libcore.api.CorePlatformApi
    public String timeFormat_Hms;

    // Used by DecimalFormatSymbols.
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public char zeroDigit;
    public char decimalSeparator;
    public char groupingSeparator;
    public char patternSeparator;
    public String percent;
    public String perMill;
    public char monetarySeparator;
    public String minusSign;
    public String exponentSeparator;
    public String infinity;
    public String NaN;
    // Also used by Currency.
    public String currencySymbol;
    public String internationalCurrencySymbol;

    // Used by DecimalFormat and NumberFormat.
    public String numberPattern;
    public String integerPattern;
    public String currencyPattern;
    public String percentPattern;

    private LocaleData() {
    }

    @UnsupportedAppUsage
    public static Locale mapInvalidAndNullLocales(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }

        if ("und".equals(locale.toLanguageTag())) {
            return Locale.ROOT;
        }

        return locale;
    }

    /**
     * Returns a shared LocaleData for the given locale.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static LocaleData get(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }

        final String languageTag = locale.toLanguageTag();
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
        }
        LocaleData newLocaleData = initLocaleData(locale);
        synchronized (localeDataCache) {
            LocaleData localeData = localeDataCache.get(languageTag);
            if (localeData != null) {
                return localeData;
            }
            localeDataCache.put(languageTag, newLocaleData);
            return newLocaleData;
        }
    }

    @Override public String toString() {
        return Objects.toString(this);
    }

    @libcore.api.CorePlatformApi
    public String getDateFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            return shortDateFormat;
        case DateFormat.MEDIUM:
            return mediumDateFormat;
        case DateFormat.LONG:
            return longDateFormat;
        case DateFormat.FULL:
            return fullDateFormat;
        }
        throw new AssertionError();
    }

    public String getTimeFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            if (DateFormat.is24Hour == null) {
                return shortTimeFormat;
            } else {
                return DateFormat.is24Hour ? timeFormat_Hm : timeFormat_hm;
            }
        case DateFormat.MEDIUM:
            if (DateFormat.is24Hour == null) {
                return mediumTimeFormat;
            } else {
                return DateFormat.is24Hour ? timeFormat_Hms : timeFormat_hms;
            }
        case DateFormat.LONG:
            // CLDR doesn't really have anything we can use to obey the 12-/24-hour preference.
            return longTimeFormat;
        case DateFormat.FULL:
            // CLDR doesn't really have anything we can use to obey the 12-/24-hour preference.
            return fullTimeFormat;
        }
        throw new AssertionError();
    }

    private static LocaleData initLocaleData(Locale locale) {
        LocaleData localeData = new LocaleData();
        if (!ICU.initLocaleDataNative(locale.toLanguageTag(), localeData)) {
            throw new AssertionError("couldn't initialize LocaleData for locale " + locale);
        }

        // Libcore localizes pattern separator while ICU doesn't. http://b/112080617
        initializePatternSeparator(localeData, locale);

        // Get the SHORT and MEDIUM 12- and 24-hour time format strings.
        localeData.timeFormat_hm = ICU.getBestDateTimePattern("hm", locale);
        localeData.timeFormat_Hm = ICU.getBestDateTimePattern("Hm", locale);
        localeData.timeFormat_hms = ICU.getBestDateTimePattern("hms", locale);
        localeData.timeFormat_Hms = ICU.getBestDateTimePattern("Hms", locale);

        // Fix up a couple of patterns.
        if (localeData.fullTimeFormat != null) {
            // There are some full time format patterns in ICU that use the pattern character 'v'.
            // Java doesn't accept this, so we replace it with 'z' which has about the same result
            // as 'v', the timezone name.
            // 'v' -> "PT", 'z' -> "PST", v is the generic timezone and z the standard tz
            // "vvvv" -> "Pacific Time", "zzzz" -> "Pacific Standard Time"
            localeData.fullTimeFormat = localeData.fullTimeFormat.replace('v', 'z');
        }
        if (localeData.numberPattern != null) {
            // The number pattern might contain positive and negative subpatterns. Arabic, for
            // example, might look like "#,##0.###;#,##0.###-" because the minus sign should be
            // written last. Macedonian supposedly looks something like "#,##0.###;(#,##0.###)".
            // (The negative subpattern is optional, though, and not present in most locales.)
            // By only swallowing '#'es and ','s after the '.', we ensure that we don't
            // accidentally eat too much.
            localeData.integerPattern = localeData.numberPattern.replaceAll("\\.[#,]*", "");
        }
        return localeData;
    }

    // Libcore localizes pattern separator while ICU doesn't. http://b/112080617
    private static void initializePatternSeparator(LocaleData localeData, Locale locale) {
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        // A numbering system could be numeric or algorithmic. DecimalFormat can only use
        // a numeric and decimal-based (radix == 10) system. Fallback to a Latin, a known numeric
        // and decimal-based if the default numbering system isn't. All locales should have data
        // for Latin numbering system after locale data fallback. See Numbering system section
        // in Unicode Technical Standard #35 for more details.
        String nsName = ns != null && ns.getRadix() == 10 && !ns.isAlgorithmic()
            ? ns.getName() : "latn";
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(
            ICUData.ICU_BASE_NAME, locale);
        String patternSeparator = null;
        // The fallback of number format data isn't well-specified in the spec.
        // But the separator can't be null / empty, and ICU uses Latin numbering system
        // as fallback.
        if (!"latn".equals(nsName)) {
            try {
                patternSeparator = rb.getStringWithFallback(
                    "NumberElements/" + nsName +"/symbols/list");
            } catch (MissingResourceException e) {
                // Try Latin numbering system later
            }
        }

        if (patternSeparator == null) {
            try {
                patternSeparator = rb.getStringWithFallback("NumberElements/latn/symbols/list");
            } catch (MissingResourceException e) {
                // Fallback to the default separator ';'.
            }
        }

        if (patternSeparator == null || patternSeparator.isEmpty()) {
            patternSeparator = ";";
        }

        // Pattern separator in libcore supports single java character only.
        localeData.patternSeparator = patternSeparator.charAt(0);
    }
}
