/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.util.Arrays;

/**
 * <a href="http://tools.ietf.org/html/rfc5084">RFC 5084</a>: GCMParameters object.
 * <p>
 * <pre>
 GCMParameters ::= SEQUENCE {
   aes-nonce        OCTET STRING, -- recommended size is 12 octets
   aes-ICVlen       AES-GCM-ICVlen DEFAULT 12 }
 * </pre>
 * @hide This class is not part of the Android public SDK API
 */
public class GCMParameters
    extends ASN1Object
{
    private byte[] nonce;
    private int icvLen;

    /**
     * Return an GCMParameters object from the given object.
     * <p>
     * Accepted inputs:
     * <ul>
     * <li> null &rarr; null
     * <li> {@link com.android.org.bouncycastle.asn1.cms.GCMParameters} object
     * <li> {@link com.android.org.bouncycastle.asn1.ASN1Sequence#getInstance(Object) ASN1Sequence} input formats with GCMParameters structure inside
     * </ul>
     *
     * @param obj the object we want converted.
     * @exception IllegalArgumentException if the object cannot be converted.
     */
    public static GCMParameters getInstance(
        Object  obj)
    {
        if (obj instanceof GCMParameters)
        {
            return (GCMParameters)obj;
        }
        else if (obj != null)
        {
            return new GCMParameters(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    private GCMParameters(
        ASN1Sequence seq)
    {
        this.nonce = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();

        if (seq.size() == 2)
        {
            this.icvLen = ASN1Integer.getInstance(seq.getObjectAt(1)).getValue().intValue();
        }
        else
        {
            this.icvLen = 12;
        }
    }

    public GCMParameters(
        byte[] nonce,
        int    icvLen)
    {
        this.nonce = Arrays.clone(nonce);
        this.icvLen = icvLen;
    }

    public byte[] getNonce()
    {
        return Arrays.clone(nonce);
    }

    public int getIcvLen()
    {
        return icvLen;
    }

    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector    v = new ASN1EncodableVector();

        v.add(new DEROctetString(nonce));

        if (icvLen != 12)
        {
            v.add(new ASN1Integer(icvLen));
        }

        return new DERSequence(v);
    }
}
