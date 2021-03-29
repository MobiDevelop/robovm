/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1.pkcs;

import java.math.BigInteger;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.BERSequence;

/**
 * the infamous Pfx from PKCS12
 * @hide This class is not part of the Android public SDK API
 */
public class Pfx
    extends ASN1Object
    implements PKCSObjectIdentifiers
{
    private ContentInfo             contentInfo;
    private MacData                 macData = null;

    private Pfx(
        ASN1Sequence   seq)
    {
        BigInteger  version = ASN1Integer.getInstance(seq.getObjectAt(0)).getValue();
        if (version.intValue() != 3)
        {
            throw new IllegalArgumentException("wrong version for PFX PDU");
        }

        contentInfo = ContentInfo.getInstance(seq.getObjectAt(1));

        if (seq.size() == 3)
        {
            macData = MacData.getInstance(seq.getObjectAt(2));
        }
    }

    public static Pfx getInstance(
        Object  obj)
    {
        if (obj instanceof Pfx)
        {
            return (Pfx)obj;
        }

        if (obj != null)
        {
            return new Pfx(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public Pfx(
        ContentInfo     contentInfo,
        MacData         macData)
    {
        this.contentInfo = contentInfo;
        this.macData = macData;
    }

    public ContentInfo getAuthSafe()
    {
        return contentInfo;
    }

    public MacData getMacData()
    {
        return macData;
    }

    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new ASN1Integer(3));
        v.add(contentInfo);

        if (macData != null)
        {
            v.add(macData);
        }

        return new BERSequence(v);
    }
}
