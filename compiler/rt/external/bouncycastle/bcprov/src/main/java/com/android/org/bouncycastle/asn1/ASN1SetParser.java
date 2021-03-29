/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A basic parser for a SET object
 * @hide This class is not part of the Android public SDK API
 */
public interface ASN1SetParser
    extends ASN1Encodable, InMemoryRepresentable
{
    /**
     * Read the next object from the underlying object representing a SET.
     *
     * @throws IOException for bad input stream.
     * @return the next object, null if we are at the end.
     */
    public ASN1Encodable readObject()
        throws IOException;
}
