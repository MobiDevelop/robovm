/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSAExt;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.NullDigest;
// Android-added: Check DSA keys when generated
import com.android.org.bouncycastle.crypto.params.DSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.crypto.signers.DSAEncoding;
// Android-removed: Unsupported algorithm
// import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import com.android.org.bouncycastle.crypto.signers.StandardDSAEncoding;
// Android-changed: Use Android digests
// import org.bouncycastle.crypto.util.DigestFactory;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.util.Arrays;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class DSASigner
    extends SignatureSpi
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
    private Digest                  digest;
    private DSAExt                  signer;
    private DSAEncoding             encoding = StandardDSAEncoding.INSTANCE;
    private SecureRandom            random;

    protected DSASigner(
        Digest digest,
        DSAExt signer)
    {
        this.digest = digest;
        this.signer = signer;
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = DSAUtil.generatePublicKeyParameter(publicKey);

        digest.reset();
        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        this.random = random;
        engineInitSign(privateKey);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = DSAUtil.generatePrivateKeyParameter(privateKey);

        // Android-added: Check DSA keys when generated
        DSAParameters dsaParam = ((DSAKeyParameters) param).getParameters();
        checkKey(dsaParam);

        if (random != null)
        {
            param = new ParametersWithRandom(param, random);
        }

        digest.reset();
        signer.init(true, param);
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        digest.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        digest.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        try
        {
            BigInteger[] sig = signer.generateSignature(hash);

            return encoding.encode(signer.getOrder(), sig[0], sig[1]);
        }
        catch (Exception e)
        {
            throw new SignatureException(e.toString());
        }
    }

    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        BigInteger[] sig;

        try
        {
            sig = encoding.decode(signer.getOrder(), sigBytes);
        }
        catch (Exception e)
        {
            throw new SignatureException("error decoding signature bytes.");
        }

        return signer.verifySignature(hash, sig[0], sig[1]);
    }

    protected void engineSetParameter(
        AlgorithmParameterSpec params)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    // BEGIN Android-added: Check DSA keys when generated
    protected void checkKey(DSAParameters params) throws InvalidKeyException {
        int valueL = params.getP().bitLength();
        int valueN = params.getQ().bitLength();
        int digestSize = digest.getDigestSize();

        // The checks are consistent with DSAParametersGenerator's init method.
        if ((valueL < 1024 || valueL > 3072) || valueL % 1024 != 0) {
            throw new InvalidKeyException("valueL values must be between 1024 and 3072 and a multiple of 1024");
        } else if (valueL == 1024 && valueN != 160) {
            throw new InvalidKeyException("valueN must be 160 for valueL = 1024");
        } else if (valueL == 2048 && (valueN != 224 && valueN != 256)) {
            throw new InvalidKeyException("valueN must be 224 or 256 for valueL = 2048");
        } else if (valueL == 3072 && valueN != 256) {
            throw new InvalidKeyException("valueN must be 256 for valueL = 3072");
        }
        if (!(digest instanceof NullDigest) && valueN > digestSize * 8) {
            throw new InvalidKeyException("Key is too strong for this signature algorithm");
        }
    }

    // END Android-added: Check DSA keys when generated
    /**
     * @deprecated replaced with #engineSetParameter(java.security.spec.AlgorithmParameterSpec)
     */
    protected void engineSetParameter(
        String  param,
        Object  value)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated
     */
    protected Object engineGetParameter(
        String      param)
    {
        throw new UnsupportedOperationException("engineGetParameter unsupported");
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class stdDSA
        extends DSASigner
    {
        public stdDSA()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA1(), new org.bouncycastle.crypto.signers.DSASigner());
            super(AndroidDigestFactory.getSHA1(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class detDSA
        extends DSASigner
    {
        public detDSA()
        {
            super(DigestFactory.createSHA1(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA1())));
        }
    }
    */
    // END Android-removed: Unsupported algorithm

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class dsa224
        extends DSASigner
    {
        public dsa224()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA224(), new org.bouncycastle.crypto.signers.DSASigner());
            super(AndroidDigestFactory.getSHA224(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class detDSA224
        extends DSASigner
    {
        public detDSA224()
        {
            super(DigestFactory.createSHA224(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA224())));
        }
    }
    */
    // END Android-removed: Unsupported algorithm

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class dsa256
        extends DSASigner
    {
        public dsa256()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA256(), new org.bouncycastle.crypto.signers.DSASigner());
            super(AndroidDigestFactory.getSHA256(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    // BEGIN Android-removed: Unsupported algorithms
    /*
    static public class detDSA256
        extends DSASigner
    {
        public detDSA256()
        {
            super(DigestFactory.createSHA256(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA256())));
        }
    }

    static public class dsa384
        extends DSASigner
    {
        public dsa384()
        {
            super(DigestFactory.createSHA384(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSA384
        extends DSASigner
    {
        public detDSA384()
        {
            super(DigestFactory.createSHA384(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA384())));
        }
    }

    static public class dsa512
        extends DSASigner
    {
        public dsa512()
        {
            super(DigestFactory.createSHA512(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSA512
        extends DSASigner
    {
        public detDSA512()
        {
            super(DigestFactory.createSHA512(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA512())));
        }
    }

    static public class dsaSha3_224
        extends DSASigner
    {
        public dsaSha3_224()
        {
            super(DigestFactory.createSHA3_224(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSASha3_224
        extends DSASigner
    {
        public detDSASha3_224()
        {
            super(DigestFactory.createSHA3_224(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_224())));
        }
    }

    static public class dsaSha3_256
        extends DSASigner
    {
        public dsaSha3_256()
        {
            super(DigestFactory.createSHA3_256(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSASha3_256
        extends DSASigner
    {
        public detDSASha3_256()
        {
            super(DigestFactory.createSHA3_256(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_256())));
        }
    }

    static public class dsaSha3_384
        extends DSASigner
    {
        public dsaSha3_384()
        {
            super(DigestFactory.createSHA3_384(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSASha3_384
        extends DSASigner
    {
        public detDSASha3_384()
        {
            super(DigestFactory.createSHA3_384(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_384())));
        }
    }

    static public class dsaSha3_512
        extends DSASigner
    {
        public dsaSha3_512()
        {
            super(DigestFactory.createSHA3_512(), new org.bouncycastle.crypto.signers.DSASigner());
        }
    }

    static public class detDSASha3_512
        extends DSASigner
    {
        public detDSASha3_512()
        {
            super(DigestFactory.createSHA3_512(), new org.bouncycastle.crypto.signers.DSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_512())));
        }
    }
    */
    // END Android-removed: Unsupported algorithms

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class noneDSA
        extends DSASigner
    {
        public noneDSA()
        {
            super(new NullDigest(), new com.android.org.bouncycastle.crypto.signers.DSASigner());
        }
    }
}
