/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class KeyDerivationFunc
    extends ASN1Object
{
    private AlgorithmIdentifier algId;

    public KeyDerivationFunc(
        ASN1ObjectIdentifier objectId,
        ASN1Encodable parameters)
    {
        this.algId = new AlgorithmIdentifier(objectId, parameters);
    }

    private KeyDerivationFunc(
        ASN1Sequence seq)
    {
        this.algId = AlgorithmIdentifier.getInstance(seq);
    }

    public static KeyDerivationFunc getInstance(Object obj)
    {
        if (obj instanceof KeyDerivationFunc)
        {
            return (KeyDerivationFunc)obj;
        }
        else if (obj != null)
        {
            return new KeyDerivationFunc(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public ASN1ObjectIdentifier getAlgorithm()
    {
        return algId.getAlgorithm();
    }

    public ASN1Encodable getParameters()
    {
        return algId.getParameters();
    }

    public ASN1Primitive toASN1Primitive()
    {
        return algId.toASN1Primitive();
    }
}
