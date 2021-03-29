/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.params;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ECKeyParameters
    extends AsymmetricKeyParameter
{
    ECDomainParameters params;

    protected ECKeyParameters(
        boolean             isPrivate,
        ECDomainParameters  params)
    {
        super(isPrivate);

        this.params = params;
    }

    public ECDomainParameters getParameters()
    {
        return params;
    }
}
