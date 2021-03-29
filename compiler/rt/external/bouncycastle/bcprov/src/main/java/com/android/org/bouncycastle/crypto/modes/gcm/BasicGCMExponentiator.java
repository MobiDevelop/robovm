/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.modes.gcm;

import com.android.org.bouncycastle.util.Arrays;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class BasicGCMExponentiator
    implements GCMExponentiator
{
    private long[] x;

    public void init(byte[] x)
    {
        this.x = GCMUtil.asLongs(x);
    }

    public void exponentiateX(long pow, byte[] output)
    {
        // Initial value is little-endian 1
        long[] y = GCMUtil.oneAsLongs();

        if (pow > 0)
        {
            long[] powX = Arrays.clone(x);
            do
            {
                if ((pow & 1L) != 0)
                {
                    GCMUtil.multiply(y, powX);
                }
                GCMUtil.square(powX, powX);
                pow >>>= 1;
            }
            while (pow > 0);
        }

        GCMUtil.asBytes(y, output);
    }
}
