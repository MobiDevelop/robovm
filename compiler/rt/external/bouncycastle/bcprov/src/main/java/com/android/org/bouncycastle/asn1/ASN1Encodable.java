/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1;

/**
 * Basic interface to produce serialisers for ASN.1 encodings.
 * @hide This class is not part of the Android public SDK API
 */
@libcore.api.CorePlatformApi
public interface ASN1Encodable
{
    /**
     * Return an object, possibly constructed, of ASN.1 primitives
     * @return an ASN.1 primitive.
     */
    ASN1Primitive toASN1Primitive();
}
