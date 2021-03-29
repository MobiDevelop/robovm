/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.math.ec.ECPoint;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ECPublicKeyParameters
    extends ECKeyParameters
{
    private final ECPoint Q;

    public ECPublicKeyParameters(
        ECPoint             Q,
        ECDomainParameters  params)
    {
        super(false, params);

        this.Q = ECDomainParameters.validate(params.getCurve(), Q);
    }

    public ECPoint getQ()
    {
        return Q;
    }
}
