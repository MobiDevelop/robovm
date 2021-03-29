/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.crypto.CipherParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class PBESecretKeyFactory
    extends BaseSecretKeyFactory
    implements PBE
{
    private boolean forCipher;
    private int scheme;
    private int digest;
    private int keySize;
    private int ivSize;

    public PBESecretKeyFactory(
        String algorithm,
        ASN1ObjectIdentifier oid,
        boolean forCipher,
        int scheme,
        int digest,
        int keySize,
        int ivSize)
    {
        super(algorithm, oid);

        this.forCipher = forCipher;
        this.scheme = scheme;
        this.digest = digest;
        this.keySize = keySize;
        this.ivSize = ivSize;
    }

    protected SecretKey engineGenerateSecret(
        KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (keySpec instanceof PBEKeySpec)
        {
            PBEKeySpec pbeSpec = (PBEKeySpec)keySpec;
            CipherParameters param;

            if (pbeSpec.getSalt() == null)
            {
                return new BCPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, null);
            }

            if (forCipher)
            {
                param = PBE.Util.makePBEParameters(pbeSpec, scheme, digest, keySize, ivSize);
            }
            else
            {
                param = PBE.Util.makePBEMacParameters(pbeSpec, scheme, digest, keySize);
            }

            return new BCPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, param);
        }

        throw new InvalidKeySpecException("Invalid KeySpec");
    }
}
