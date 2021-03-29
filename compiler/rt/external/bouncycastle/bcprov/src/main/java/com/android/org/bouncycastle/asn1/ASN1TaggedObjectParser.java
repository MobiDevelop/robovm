/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

import java.io.IOException;

/**
 * Interface for the parsing of a generic tagged ASN.1 object.
 * @hide This class is not part of the Android public SDK API
 */
public interface ASN1TaggedObjectParser
    extends ASN1Encodable, InMemoryRepresentable
{
    /**
     * Return the tag number associated with the underlying tagged object.
     * @return the object's tag number.
     */
    int getTagNo();

    /**
     * Return a parser for the actual object tagged.
     *
     * @param tag the primitive tag value for the object tagged originally.
     * @param isExplicit true if the tagging was done explicitly.
     * @return a parser for the tagged object.
     * @throws IOException if a parser cannot be constructed.
     */
    ASN1Encodable getObjectParser(int tag, boolean isExplicit)
        throws IOException;
}
