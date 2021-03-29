/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2017 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.impl.number;

import android.icu.number.IntegerWidth;
import android.icu.number.NumberFormatter.DecimalSeparatorDisplay;
import android.icu.number.NumberFormatter.SignDisplay;
import android.icu.number.Precision;
import android.icu.text.DecimalFormatSymbols;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class MicroProps implements Cloneable, MicroPropsGenerator {
    // Populated globally:
    public SignDisplay sign;
    public DecimalFormatSymbols symbols;
    public Padder padding;
    public DecimalSeparatorDisplay decimal;
    public IntegerWidth integerWidth;

    // Populated by notation/unit:
    public Modifier modOuter;
    public Modifier modMiddle;
    public Modifier modInner;
    public Precision rounder;
    public Grouper grouping;
    public boolean useCurrency;

    // Internal fields:
    private final boolean immutable;
    private volatile boolean exhausted;

    /**
     * @param immutable
     *            Whether this MicroProps should behave as an immutable after construction with respect
     *            to the quantity chain.
     */
    public MicroProps(boolean immutable) {
        this.immutable = immutable;
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        if (immutable) {
            return (MicroProps) this.clone();
        } else if (exhausted) {
            // Safety check
            throw new AssertionError("Cannot re-use a mutable MicroProps in the quantity chain");
        } else {
            exhausted = true;
            return this;
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
