/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

/**
 * interface that a public/private key pair generator should conform to.
 * @hide This class is not part of the Android public SDK API
 */
public interface AsymmetricCipherKeyPairGenerator
{
    /**
     * intialise the key pair generator.
     *
     * @param param the parameters the key pair is to be initialised with.
     */
    public void init(KeyGenerationParameters param);

    /**
     * return an AsymmetricCipherKeyPair containing the generated keys.
     *
     * @return an AsymmetricCipherKeyPair containing the generated keys.
     */
    public AsymmetricCipherKeyPair generateKeyPair();
}

