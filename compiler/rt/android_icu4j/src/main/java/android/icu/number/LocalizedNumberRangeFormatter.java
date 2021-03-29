/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2018 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.number;

import android.icu.impl.number.DecimalQuantity;
import android.icu.impl.number.DecimalQuantity_DualStorageBCD;

/**
 * A NumberRangeFormatter that has a locale associated with it; this means .formatRange() methods are available.
 *
 * @author sffc
 * @see NumberRangeFormatter
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class LocalizedNumberRangeFormatter extends NumberRangeFormatterSettings<LocalizedNumberRangeFormatter> {

    private volatile NumberRangeFormatterImpl fImpl;

    LocalizedNumberRangeFormatter(NumberRangeFormatterSettings<?> parent, int key, Object value) {
        super(parent, key, value);
    }

    /**
     * Format the given integers to a string using the settings specified in the NumberRangeFormatter fluent setting
     * chain.
     *
     * @param first
     *            The first number in the range, usually to the left in LTR locales.
     * @param second
     *            The second number in the range, usually to the right in LTR locales.
     * @return A FormattedNumberRange object; call .toString() to get the string.
     * @see NumberRangeFormatter
     * @hide draft / provisional / internal are hidden on Android
     */
    public FormattedNumberRange formatRange(int first, int second) {
        DecimalQuantity dq1 = new DecimalQuantity_DualStorageBCD(first);
        DecimalQuantity dq2 = new DecimalQuantity_DualStorageBCD(second);
        return formatImpl(dq1, dq2, first == second);
    }

    /**
     * Format the given doubles to a string using the settings specified in the NumberRangeFormatter fluent setting
     * chain.
     *
     * @param first
     *            The first number in the range, usually to the left in LTR locales.
     * @param second
     *            The second number in the range, usually to the right in LTR locales.
     * @return A FormattedNumberRange object; call .toString() to get the string.
     * @see NumberRangeFormatter
     * @hide draft / provisional / internal are hidden on Android
     */
    public FormattedNumberRange formatRange(double first, double second) {
        DecimalQuantity dq1 = new DecimalQuantity_DualStorageBCD(first);
        DecimalQuantity dq2 = new DecimalQuantity_DualStorageBCD(second);
        // Note: double equality could be changed to epsilon equality later if there is demand.
        // The epsilon should be set via an API method.
        return formatImpl(dq1, dq2, first == second);
    }

    /**
     * Format the given Numbers to a string using the settings specified in the NumberRangeFormatter fluent setting
     * chain.
     *
     * @param first
     *            The first number in the range, usually to the left in LTR locales.
     * @param second
     *            The second number in the range, usually to the right in LTR locales.
     * @return A FormattedNumberRange object; call .toString() to get the string.
     * @see NumberRangeFormatter
     * @hide draft / provisional / internal are hidden on Android
     */
    public FormattedNumberRange formatRange(Number first, Number second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Cannot format null values in range");
        }
        DecimalQuantity dq1 = new DecimalQuantity_DualStorageBCD(first);
        DecimalQuantity dq2 = new DecimalQuantity_DualStorageBCD(second);
        return formatImpl(dq1, dq2, first.equals(second));
    }

    FormattedNumberRange formatImpl(DecimalQuantity first, DecimalQuantity second, boolean equalBeforeRounding) {
        if (fImpl == null) {
            fImpl = new NumberRangeFormatterImpl(resolve());
        }
        return fImpl.format(first, second, equalBeforeRounding);
    }

    @Override
    LocalizedNumberRangeFormatter create(int key, Object value) {
        return new LocalizedNumberRangeFormatter(this, key, value);
    }

}
