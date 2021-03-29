/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class PrimeCertaintyCalculator
{
    private PrimeCertaintyCalculator()
    {

    }

    /**
     * Return the current wisdom on prime certainty requirements.
     *
     * @param keySizeInBits size of the key being generated.
     * @return a certainty value.
     */
    public static int getDefaultCertainty(int keySizeInBits)
    {
        // Based on FIPS 186-4 Table C.1
        return keySizeInBits <= 1024 ? 80 : (96 + 16 * ((keySizeInBits - 1) / 1024));
    }
}
