/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.math.BigInteger;

/**
 * @deprecated  Use ASN1Integer instead of this,
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public class DERInteger
    extends ASN1Integer
{
    /**
     * Constructor from a byte array containing a signed representation of the number.
     *
     * @param bytes a byte array containing the signed number.A copy is made of the byte array.
     */
    public DERInteger(byte[] bytes)
    {
        super(bytes, true);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    public DERInteger(BigInteger value)
    {
        super(value);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public DERInteger(long value)
    {
        super(value);
    }
}
