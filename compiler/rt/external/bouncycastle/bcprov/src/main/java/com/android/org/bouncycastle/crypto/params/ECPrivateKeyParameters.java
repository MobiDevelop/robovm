/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import java.math.BigInteger;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ECPrivateKeyParameters
    extends ECKeyParameters
{
    BigInteger d;

    public ECPrivateKeyParameters(
        BigInteger          d,
        ECDomainParameters  params)
    {
        super(true, params);
        this.d = d;
    }

    public BigInteger getD()
    {
        return d;
    }
}
