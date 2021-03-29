/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class AsymmetricKeyParameter
    implements CipherParameters
{
    boolean privateKey;

    public AsymmetricKeyParameter(
        boolean privateKey)
    {
        this.privateKey = privateKey;
    }

    public boolean isPrivate()
    {
        return privateKey;
    }
}
