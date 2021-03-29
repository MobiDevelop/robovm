/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public class KeyParameter
    implements CipherParameters
{
    private byte[]  key;

    public KeyParameter(
        byte[]  key)
    {
        this(key, 0, key.length);
    }

    public KeyParameter(
        byte[]  key,
        int     keyOff,
        int     keyLen)
    {
        this.key = new byte[keyLen];

        System.arraycopy(key, keyOff, this.key, 0, keyLen);
    }

    @libcore.api.CorePlatformApi
    public byte[] getKey()
    {
        return key;
    }
}
