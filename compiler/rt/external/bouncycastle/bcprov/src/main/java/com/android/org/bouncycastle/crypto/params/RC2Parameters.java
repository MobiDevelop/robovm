/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class RC2Parameters
    extends KeyParameter
{
    private int     bits;

    public RC2Parameters(
        byte[]  key)
    {
        this(key, (key.length > 128) ? 1024 : (key.length * 8));
    }

    public RC2Parameters(
        byte[]  key,
        int     bits)
    {
        super(key);
        this.bits = bits;
    }

    public int getEffectiveKeyBits()
    {
        return bits;
    }
}
