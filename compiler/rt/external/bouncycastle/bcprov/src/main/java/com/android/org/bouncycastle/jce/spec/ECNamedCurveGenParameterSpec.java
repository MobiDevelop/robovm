/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;

/**
 * Named curve generation spec
 * <p>
 * If you are using JDK 1.5 you should be looking at ECGenParameterSpec.
 * @hide This class is not part of the Android public SDK API
 */
public class ECNamedCurveGenParameterSpec
    implements AlgorithmParameterSpec
{
    private String  name;

    public ECNamedCurveGenParameterSpec(
        String name)
    {
        this.name = name;
    }

    /**
     * return the name of the curve the EC domain parameters belong to.
     */
    public String getName()
    {
        return name;
    }
}
