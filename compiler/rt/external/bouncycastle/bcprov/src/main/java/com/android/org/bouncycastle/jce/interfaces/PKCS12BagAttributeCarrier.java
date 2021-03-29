/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.interfaces;

import java.util.Enumeration;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * allow us to set attributes on objects that can go into a PKCS12 store.
 * @hide This class is not part of the Android public SDK API
 */
public interface PKCS12BagAttributeCarrier
{
    void setBagAttribute(
        ASN1ObjectIdentifier oid,
        ASN1Encodable attribute);

    ASN1Encodable getBagAttribute(
        ASN1ObjectIdentifier oid);

    Enumeration getBagAttributeKeys();
}
