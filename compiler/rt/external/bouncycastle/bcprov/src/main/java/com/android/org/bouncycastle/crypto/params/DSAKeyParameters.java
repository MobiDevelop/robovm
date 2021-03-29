/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class DSAKeyParameters
    extends AsymmetricKeyParameter
{
    private DSAParameters    params;

    public DSAKeyParameters(
        boolean         isPrivate,
        DSAParameters   params)
    {
        super(isPrivate);

        this.params = params;
    }   

    public DSAParameters getParameters()
    {
        return params;
    }
}
