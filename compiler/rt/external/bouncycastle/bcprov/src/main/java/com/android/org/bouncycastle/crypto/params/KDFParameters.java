/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.DerivationParameters;

/**
 * parameters for Key derivation functions for IEEE P1363a
 * @hide This class is not part of the Android public SDK API
 */
public class KDFParameters
    implements DerivationParameters
{
    byte[]  iv;
    byte[]  shared;

    public KDFParameters(
        byte[]  shared,
        byte[]  iv)
    {
        this.shared = shared;
        this.iv = iv;
    }

    public byte[] getSharedSecret()
    {
        return shared;
    }

    public byte[] getIV()
    {
        return iv;
    }
}
