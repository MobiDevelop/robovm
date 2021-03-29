/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.util;

import android.icu.number.NumberFormatter;

/**
 * Dimensionless unit for percent and permille.
 * @see NumberFormatter
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class NoUnit extends MeasureUnit {
    private static final long serialVersionUID = 2467174286237024095L;

    /**
     * Constant for the base unit (dimensionless and no scaling).
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final NoUnit BASE =
        (NoUnit) MeasureUnit.internalGetInstance("none", "base");

    /**
     * Constant for the percent unit, or 1/100 of a base unit.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final NoUnit PERCENT =
        (NoUnit) MeasureUnit.internalGetInstance("none", "percent");

    /**
     * Constant for the permille unit, or 1/100 of a base unit.
     *
     * @hide draft / provisional / internal are hidden on Android
     */
    public static final NoUnit PERMILLE =
        (NoUnit) MeasureUnit.internalGetInstance("none", "permille");


    /**
     * Package local constructor. This class is not designed for subclassing
     * by ICU users.
     *
     * @param subType   The unit subtype.
     */
    NoUnit(String subType) {
        super("none", subType);
    }
}
