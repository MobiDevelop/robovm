/**
 * ******************************************************************************
 * Copyright (C) 1996-2005, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 * ******************************************************************************
 *
 *
 * ******************************************************************************
 */

package libcore.icu;

import java.text.CollationKey;

/**
 * A concrete implementation of the abstract java.text.CollationKey.
 */
public final class CollationKeyICU extends CollationKey {
    /**
     * The key.
     */
    private final android.icu.text.CollationKey key;

    public CollationKeyICU(String source, android.icu.text.CollationKey key) {
        super(source);
        this.key = key;
    }

    @Override
    public int compareTo(CollationKey other) {
        final android.icu.text.CollationKey otherKey;
        if (other instanceof CollationKeyICU) {
            otherKey = ((CollationKeyICU) other).key;
        } else {
            otherKey = new android.icu.text.CollationKey(other.getSourceString(),
                    other.toByteArray());
        }

        return key.compareTo(otherKey);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof CollationKey)) {
            return false;
        }
        return compareTo((CollationKey) object) == 0;
    }

    /**
     * Creates a hash code for this CollationKey.
     * Compute the hash by iterating sparsely over about 32 (up to 63) bytes
     * spaced evenly through the string.  For each byte, multiply the previous
     * hash value by a prime number and add the new byte in, like a linear
     * congruential random number generator, producing a pseudo-random
     * deterministic value well distributed over the output range.
     *
     * @return hash value of collation key. Hash value is never 0.
     * @stable ICU 2.4
     */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public byte[] toByteArray() {
        return key.toByteArray();
    }
}
