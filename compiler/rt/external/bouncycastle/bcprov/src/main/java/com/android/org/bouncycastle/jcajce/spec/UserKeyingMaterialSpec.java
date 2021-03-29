/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;

import com.android.org.bouncycastle.util.Arrays;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class UserKeyingMaterialSpec
    implements AlgorithmParameterSpec
{
    private final byte[] userKeyingMaterial;

    public UserKeyingMaterialSpec(byte[] userKeyingMaterial)
    {
        this.userKeyingMaterial = Arrays.clone(userKeyingMaterial);
    }

    public byte[] getUserKeyingMaterial()
    {
        return Arrays.clone(userKeyingMaterial);
    }
}
