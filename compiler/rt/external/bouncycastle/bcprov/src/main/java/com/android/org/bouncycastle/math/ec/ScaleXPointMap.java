/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.math.ec;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ScaleXPointMap implements ECPointMap
{
    protected final ECFieldElement scale;

    public ScaleXPointMap(ECFieldElement scale)
    {
        this.scale = scale;
    }

    public ECPoint map(ECPoint p)
    {
        return p.scaleX(scale);
    }
}
