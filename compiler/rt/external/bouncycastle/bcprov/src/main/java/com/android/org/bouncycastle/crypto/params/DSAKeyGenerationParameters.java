/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class DSAKeyGenerationParameters
    extends KeyGenerationParameters
{
    private DSAParameters    params;

    public DSAKeyGenerationParameters(
        SecureRandom    random,
        DSAParameters   params)
    {
        super(random, params.getP().bitLength() - 1);

        this.params = params;
    }

    public DSAParameters getParameters()
    {
        return params;
    }
}
