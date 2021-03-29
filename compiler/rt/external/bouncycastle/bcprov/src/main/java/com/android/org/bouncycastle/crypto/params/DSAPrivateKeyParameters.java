/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class DSAPrivateKeyParameters
    extends DSAKeyParameters
{
    private BigInteger      x;

    public DSAPrivateKeyParameters(
        BigInteger      x,
        DSAParameters   params)
    {
        super(true, params);

        this.x = x;
    }   

    public BigInteger getX()
    {
        return x;
    }
}
