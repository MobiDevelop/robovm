/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1UTCTime;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x500.X500Name;

/**
 * Generator for Version 3 TBSCertificateStructures.
 * <pre>
 * TBSCertificate ::= SEQUENCE {
 *      version          [ 0 ]  Version DEFAULT v1(0),
 *      serialNumber            CertificateSerialNumber,
 *      signature               AlgorithmIdentifier,
 *      issuer                  Name,
 *      validity                Validity,
 *      subject                 Name,
 *      subjectPublicKeyInfo    SubjectPublicKeyInfo,
 *      issuerUniqueID    [ 1 ] IMPLICIT UniqueIdentifier OPTIONAL,
 *      subjectUniqueID   [ 2 ] IMPLICIT UniqueIdentifier OPTIONAL,
 *      extensions        [ 3 ] Extensions OPTIONAL
 *      }
 * </pre>
 * @hide This class is not part of the Android public SDK API
 *
 */
@libcore.api.CorePlatformApi
public class V3TBSCertificateGenerator
{
    DERTaggedObject         version = new DERTaggedObject(true, 0, new ASN1Integer(2));

    ASN1Integer              serialNumber;
    AlgorithmIdentifier     signature;
    X500Name                issuer;
    Time                    startDate, endDate;
    X500Name                subject;
    SubjectPublicKeyInfo    subjectPublicKeyInfo;
    Extensions              extensions;

    private boolean altNamePresentAndCritical;
    private DERBitString issuerUniqueID;
    private DERBitString subjectUniqueID;

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public V3TBSCertificateGenerator()
    {
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setSerialNumber(
        ASN1Integer  serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setSignature(
        AlgorithmIdentifier    signature)
    {
        this.signature = signature;
    }

        /**
     * @deprecated use X500Name method
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setIssuer(
        X509Name    issuer)
    {
        this.issuer = X500Name.getInstance(issuer);
    }

    public void setIssuer(
        X500Name issuer)
    {
        this.issuer = issuer;
    }
    
    public void setStartDate(
        ASN1UTCTime startDate)
    {
        this.startDate = new Time(startDate);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setStartDate(
        Time startDate)
    {
        this.startDate = startDate;
    }

    public void setEndDate(
        ASN1UTCTime endDate)
    {
        this.endDate = new Time(endDate);
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setEndDate(
        Time endDate)
    {
        this.endDate = endDate;
    }

        /**
     * @deprecated use X500Name method
     */
    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setSubject(
        X509Name    subject)
    {
        this.subject = X500Name.getInstance(subject.toASN1Primitive());
    }

    public void setSubject(
        X500Name subject)
    {
        this.subject = subject;
    }

    public void setIssuerUniqueID(
        DERBitString uniqueID)
    {
        this.issuerUniqueID = uniqueID;
    }

    public void setSubjectUniqueID(
        DERBitString uniqueID)
    {
        this.subjectUniqueID = uniqueID;
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void setSubjectPublicKeyInfo(
        SubjectPublicKeyInfo    pubKeyInfo)
    {
        this.subjectPublicKeyInfo = pubKeyInfo;
    }

    /**
     * @deprecated use method taking Extensions
     * @param extensions
     */
    public void setExtensions(
        X509Extensions    extensions)
    {
        setExtensions(Extensions.getInstance(extensions));
    }

    public void setExtensions(
        Extensions    extensions)
    {
        this.extensions = extensions;
        if (extensions != null)
        {
            Extension altName = extensions.getExtension(Extension.subjectAlternativeName);

            if (altName != null && altName.isCritical())
            {
                altNamePresentAndCritical = true;
            }
        }
    }

    @dalvik.annotation.compat.UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public TBSCertificate generateTBSCertificate()
    {
        if ((serialNumber == null) || (signature == null)
            || (issuer == null) || (startDate == null) || (endDate == null)
            || (subject == null && !altNamePresentAndCritical) || (subjectPublicKeyInfo == null))
        {
            throw new IllegalStateException("not all mandatory fields set in V3 TBScertificate generator");
        }

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(version);
        v.add(serialNumber);
        v.add(signature);
        v.add(issuer);

        //
        // before and after dates
        //
        ASN1EncodableVector  validity = new ASN1EncodableVector();

        validity.add(startDate);
        validity.add(endDate);

        v.add(new DERSequence(validity));

        if (subject != null)
        {
            v.add(subject);
        }
        else
        {
            v.add(new DERSequence());
        }

        v.add(subjectPublicKeyInfo);

        if (issuerUniqueID != null)
        {
            v.add(new DERTaggedObject(false, 1, issuerUniqueID));
        }

        if (subjectUniqueID != null)
        {
            v.add(new DERTaggedObject(false, 2, subjectUniqueID));
        }

        if (extensions != null)
        {
            v.add(new DERTaggedObject(true, 3, extensions));
        }

        return TBSCertificate.getInstance(new DERSequence(v));
    }
}
