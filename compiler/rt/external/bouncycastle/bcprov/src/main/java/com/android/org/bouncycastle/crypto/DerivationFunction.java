/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

/**
 * base interface for general purpose byte derivation functions.
 * @hide This class is not part of the Android public SDK API
 */
public interface DerivationFunction
{
    public void init(DerivationParameters param);

    public int generateBytes(byte[] out, int outOff, int len)
        throws DataLengthException, IllegalArgumentException;
}
