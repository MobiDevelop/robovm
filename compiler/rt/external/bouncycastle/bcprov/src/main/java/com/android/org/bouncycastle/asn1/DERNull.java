/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.io.IOException;

/**
 * An ASN.1 DER NULL object.
 * <p>
 * Preferably use the constant:  DERNull.INSTANCE.
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public class DERNull
    extends ASN1Null
{
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static final DERNull INSTANCE = new DERNull();

    private static final byte[]  zeroBytes = new byte[0];

    /**
     * @deprecated use DERNull.INSTANCE
     */
    // Android-changed: Reduce visibility to protected.
    protected DERNull()
    {
    }

    boolean isConstructed()
    {
        return false;
    }

    int encodedLength()
    {
        return 2;
    }

    void encode(
        ASN1OutputStream out)
        throws IOException
    {
        out.writeEncoded(BERTags.NULL, zeroBytes);
    }
}
