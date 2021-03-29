/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import com.android.org.bouncycastle.crypto.KeyGenerationParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ECKeyGenerationParameters
    extends KeyGenerationParameters
{
    private ECDomainParameters  domainParams;

    public ECKeyGenerationParameters(
        ECDomainParameters      domainParams,
        SecureRandom            random)
    {
        super(random, domainParams.getN().bitLength());

        this.domainParams = domainParams;
    }

    public ECDomainParameters getDomainParameters()
    {
        return domainParams;
    }
}
