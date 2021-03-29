/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream that outputs encoding based on definite length.
 * @hide This class is not part of the Android public SDK API
 */
public class DLOutputStream
    extends ASN1OutputStream
{
    public DLOutputStream(
        OutputStream os)
    {
        super(os);
    }

    public void writeObject(
        ASN1Encodable obj)
        throws IOException
    {
        if (obj != null)
        {
            obj.toASN1Primitive().toDLObject().encode(this);
        }
        else
        {
            throw new IOException("null object detected");
        }
    }
}
