/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream that outputs encoding based on distinguished encoding rules.
 * @hide This class is not part of the Android public SDK API
 */
public class DEROutputStream
    extends ASN1OutputStream
{
    @dalvik.annotation.compat.UnsupportedAppUsage
    public DEROutputStream(
        OutputStream    os)
    {
        super(os);
    }

    public void writeObject(
        ASN1Encodable obj)
        throws IOException
    {
        if (obj != null)
        {
            obj.toASN1Primitive().toDERObject().encode(this);
        }
        else
        {
            throw new IOException("null object detected");
        }
    }

    ASN1OutputStream getDERSubStream()
    {
        return this;
    }

    ASN1OutputStream getDLSubStream()
    {
        return this;
    }
}
