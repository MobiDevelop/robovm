/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.asn1.oiw.ElGamalParameter;
// import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
// import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
// import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
// import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.DHParameter;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.pkcs.RSAPrivateKey;
// import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import com.android.org.bouncycastle.asn1.sec.ECPrivateKey;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.DSAParameter;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.ec.CustomNamedCurves;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import com.android.org.bouncycastle.crypto.params.ECNamedDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.params.ElGamalParameters;
// import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
// import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
// import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
// import org.bouncycastle.crypto.params.X448PrivateKeyParameters;

/**
 * Factory for creating private key objects from PKCS8 PrivateKeyInfo objects.
 * @hide This class is not part of the Android public SDK API
 */
public class PrivateKeyFactory
{
    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding.
     *
     * @param privateKeyInfoData the PrivateKeyInfo encoding
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(byte[] privateKeyInfoData)
        throws IOException
    {
        return createKey(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(privateKeyInfoData)));
    }

    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding read from a
     * stream.
     *
     * @param inStr the stream to read the PrivateKeyInfo encoding from
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(InputStream inStr)
        throws IOException
    {
        return createKey(PrivateKeyInfo.getInstance(new ASN1InputStream(inStr).readObject()));
    }

    /**
     * Create a private key parameter from the passed in PKCS8 PrivateKeyInfo object.
     *
     * @param keyInfo the PrivateKeyInfo object containing the key material
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(PrivateKeyInfo keyInfo)
        throws IOException
    {
        AlgorithmIdentifier algId = keyInfo.getPrivateKeyAlgorithm();
        ASN1ObjectIdentifier algOID = algId.getAlgorithm();

        if (algOID.equals(PKCSObjectIdentifiers.rsaEncryption)
            || algOID.equals(PKCSObjectIdentifiers.id_RSASSA_PSS)
            || algOID.equals(X509ObjectIdentifiers.id_ea_rsa))
        {
            RSAPrivateKey keyStructure = RSAPrivateKey.getInstance(keyInfo.parsePrivateKey());

            return new RSAPrivateCrtKeyParameters(keyStructure.getModulus(),
                keyStructure.getPublicExponent(), keyStructure.getPrivateExponent(),
                keyStructure.getPrime1(), keyStructure.getPrime2(), keyStructure.getExponent1(),
                keyStructure.getExponent2(), keyStructure.getCoefficient());
        }
        // TODO?
//      else if (algOID.equals(X9ObjectIdentifiers.dhpublicnumber))
        else if (algOID.equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            DHParameter params = DHParameter.getInstance(algId.getParameters());
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();

            BigInteger lVal = params.getL();
            int l = lVal == null ? 0 : lVal.intValue();
            DHParameters dhParams = new DHParameters(params.getP(), params.getG(), null, l);

            return new DHPrivateKeyParameters(derX.getValue(), dhParams);
        }
        // BEGIN Android-removed: Unsupported algorithms
        /*
        else if (algOID.equals(OIWObjectIdentifiers.elGamalAlgorithm))
        {
            ElGamalParameter params = ElGamalParameter.getInstance(algId.getParameters());
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();

            return new ElGamalPrivateKeyParameters(derX.getValue(), new ElGamalParameters(
                params.getP(), params.getG()));
        }
        */
        // END Android-removed: Unsupported algorithms
        else if (algOID.equals(X9ObjectIdentifiers.id_dsa))
        {
            ASN1Integer derX = (ASN1Integer)keyInfo.parsePrivateKey();
            ASN1Encodable de = algId.getParameters();

            DSAParameters parameters = null;
            if (de != null)
            {
                DSAParameter params = DSAParameter.getInstance(de.toASN1Primitive());
                parameters = new DSAParameters(params.getP(), params.getQ(), params.getG());
            }

            return new DSAPrivateKeyParameters(derX.getValue(), parameters);
        }
        else if (algOID.equals(X9ObjectIdentifiers.id_ecPublicKey))
        {
            X962Parameters params = new X962Parameters((ASN1Primitive)algId.getParameters());

            X9ECParameters x9;
            ECDomainParameters dParams;

            if (params.isNamedCurve())
            {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)params.getParameters();

                x9 = CustomNamedCurves.getByOID(oid);
                if (x9 == null)
                {
                    x9 = ECNamedCurveTable.getByOID(oid);
                }
                dParams = new ECNamedDomainParameters(
                    oid, x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            }
            else
            {
                x9 = X9ECParameters.getInstance(params.getParameters());
                dParams = new ECDomainParameters(
                    x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            }

            ECPrivateKey ec = ECPrivateKey.getInstance(keyInfo.parsePrivateKey());
            BigInteger d = ec.getKey();

            return new ECPrivateKeyParameters(d, dParams);
        }
        // BEGIN Android-removed: Unsupported algorithms
        /*
        else if (algOID.equals(EdECObjectIdentifiers.id_X25519))
        {
            return new X25519PrivateKeyParameters(getRawKey(keyInfo, X25519PrivateKeyParameters.KEY_SIZE), 0);
        }
        else if (algOID.equals(EdECObjectIdentifiers.id_X448))
        {
            return new X448PrivateKeyParameters(getRawKey(keyInfo, X448PrivateKeyParameters.KEY_SIZE), 0);
        }
        else if (algOID.equals(EdECObjectIdentifiers.id_Ed25519))
        {
            return new Ed25519PrivateKeyParameters(getRawKey(keyInfo, Ed25519PrivateKeyParameters.KEY_SIZE), 0);
        }
        else if (algOID.equals(EdECObjectIdentifiers.id_Ed448))
        {
            return new Ed448PrivateKeyParameters(getRawKey(keyInfo, Ed448PrivateKeyParameters.KEY_SIZE), 0);
        }
        else if (
            algOID.equals(CryptoProObjectIdentifiers.gostR3410_2001) ||
                algOID.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512) ||
                algOID.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256))
        {
            GOST3410PublicKeyAlgParameters gostParams = GOST3410PublicKeyAlgParameters.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ECGOST3410Parameters ecSpec = null;
            BigInteger d = null;
            ASN1Primitive p = keyInfo.getPrivateKeyAlgorithm().getParameters().toASN1Primitive();
            if (p instanceof ASN1Sequence && (ASN1Sequence.getInstance(p).size() == 2 || ASN1Sequence.getInstance(p).size() == 3))
            {

                ECDomainParameters ecP = ECGOST3410NamedCurves.getByOID(gostParams.getPublicKeyParamSet());

                ecSpec = new ECGOST3410Parameters(
                    new ECNamedDomainParameters(
                        gostParams.getPublicKeyParamSet(), ecP),
                    gostParams.getPublicKeyParamSet(),
                    gostParams.getDigestParamSet(),
                    gostParams.getEncryptionParamSet());
                ASN1Encodable privKey = keyInfo.parsePrivateKey();
                if (privKey instanceof ASN1Integer)
                {
                    d = ASN1Integer.getInstance(privKey).getPositiveValue();
                }
                else
                {
                    byte[] encVal = ASN1OctetString.getInstance(privKey).getOctets();
                    byte[] dVal = new byte[encVal.length];

                    for (int i = 0; i != encVal.length; i++)
                    {
                        dVal[i] = encVal[encVal.length - 1 - i];
                    }

                    d = new BigInteger(1, dVal);
                }


            }
            else
            {
                X962Parameters params = X962Parameters.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());

                if (params.isNamedCurve())
                {
                    ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(params.getParameters());
                    X9ECParameters ecP = ECNamedCurveTable.getByOID(oid);
                    if (ecP == null)
                    {
                        ECDomainParameters gParam = ECGOST3410NamedCurves.getByOID(oid);
                        ecSpec = new ECGOST3410Parameters(new ECNamedDomainParameters(
                            oid,
                            gParam.getCurve(),
                            gParam.getG(),
                            gParam.getN(),
                            gParam.getH(),
                            gParam.getSeed()), gostParams.getPublicKeyParamSet(), gostParams.getDigestParamSet(), gostParams.getEncryptionParamSet());
                    }
                    else
                    {
                        ecSpec = new ECGOST3410Parameters(new ECNamedDomainParameters(
                            oid,
                            ecP.getCurve(),
                            ecP.getG(),
                            ecP.getN(),
                            ecP.getH(),
                            ecP.getSeed()), gostParams.getPublicKeyParamSet(), gostParams.getDigestParamSet(), gostParams.getEncryptionParamSet());
                    }
                }
                else if (params.isImplicitlyCA())
                {
                    ecSpec = null;
                }
                else
                {
                    X9ECParameters ecP = X9ECParameters.getInstance(params.getParameters());
                    ecSpec = new ECGOST3410Parameters(new ECNamedDomainParameters(
                        algOID,
                        ecP.getCurve(),
                        ecP.getG(),
                        ecP.getN(),
                        ecP.getH(),
                        ecP.getSeed()), gostParams.getPublicKeyParamSet(), gostParams.getDigestParamSet(), gostParams.getEncryptionParamSet());
                }

                ASN1Encodable privKey = keyInfo.parsePrivateKey();
                if (privKey instanceof ASN1Integer)
                {
                    ASN1Integer derD = ASN1Integer.getInstance(privKey);

                    d = derD.getValue();
                }
                else
                {
                    org.bouncycastle.asn1.sec.ECPrivateKey ec = org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(privKey);

                    d = ec.getKey();
                }

            }

            return new ECPrivateKeyParameters(
                d,
                new ECGOST3410Parameters(
                    ecSpec,
                    gostParams.getPublicKeyParamSet(),
                    gostParams.getDigestParamSet(),
                    gostParams.getEncryptionParamSet()));

        }
        */
        // END Android-removed: Unsupported algorithms
        else
        {
            throw new RuntimeException("algorithm identifier in private key not recognised");
        }
    }

    private static byte[] getRawKey(PrivateKeyInfo keyInfo, int expectedSize)
        throws IOException
    {
        byte[] result = ASN1OctetString.getInstance(keyInfo.parsePrivateKey()).getOctets();
        if (expectedSize != result.length)
        {
            throw new RuntimeException("private key encoding has incorrect length");
        }
        return result;
    }
}
