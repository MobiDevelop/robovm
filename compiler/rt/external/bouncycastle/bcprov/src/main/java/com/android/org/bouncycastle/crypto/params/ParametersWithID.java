/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.crypto.CipherParameters;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ParametersWithID
    implements CipherParameters
{
    private CipherParameters  parameters;
    private byte[] id;

    public ParametersWithID(
        CipherParameters parameters,
        byte[] id)
    {
        this.parameters = parameters;
        this.id = id;
    }

    public byte[] getID()
    {
        return id;
    }

    public CipherParameters getParameters()
    {
        return parameters;
    }
}
