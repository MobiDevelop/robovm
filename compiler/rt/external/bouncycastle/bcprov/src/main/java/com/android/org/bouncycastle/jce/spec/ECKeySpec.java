/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.spec;

import java.security.spec.KeySpec;

/**
 * base class for an Elliptic Curve Key Spec
 * @hide This class is not part of the Android public SDK API
 */
public class ECKeySpec
    implements KeySpec
{
    private ECParameterSpec     spec;

    protected ECKeySpec(
        ECParameterSpec spec)
    {
        this.spec = spec;
    }

    /**
     * return the domain parameters for the curve
     */
    public ECParameterSpec getParams()
    {
        return spec;
    }
}
