/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.asymmetric.ec;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DSAExt;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.digests.NullDigest;
// BEGIN Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.digests.RIPEMD160Digest;
// END Android-removed: Unsupported algorithms
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.crypto.signers.DSAEncoding;
import com.android.org.bouncycastle.crypto.signers.ECDSASigner;
// BEGIN Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.signers.ECNRSigner;
// import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
// END Android-removed: Unsupported algorithms
import com.android.org.bouncycastle.crypto.signers.PlainDSAEncoding;
import com.android.org.bouncycastle.crypto.signers.StandardDSAEncoding;
// BEGIN Android-changed: Use Android digests
// import org.bouncycastle.crypto.util.DigestFactory;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.DSABase;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class SignatureSpi
    extends DSABase
{
    SignatureSpi(Digest digest, DSAExt signer, DSAEncoding encoding)
    {
        super(digest, signer, encoding);
    }

    protected void engineInitVerify(PublicKey publicKey)
        throws InvalidKeyException
    {
        CipherParameters param = ECUtils.generatePublicKeyParameter(publicKey);

        digest.reset();
        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey privateKey)
        throws InvalidKeyException
    {
        CipherParameters param = ECUtil.generatePrivateKeyParameter(privateKey);

        digest.reset();

        if (appRandom != null)
        {
            signer.init(true, new ParametersWithRandom(param, appRandom));
        }
        else
        {
            signer.init(true, param);
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSA
        extends SignatureSpi
    {
        public ecDSA()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA1(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
            super(AndroidDigestFactory.getSHA1(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class ecDetDSA
        extends SignatureSpi
    {
        public ecDetDSA()
        {
            super(DigestFactory.createSHA1(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA1())), StandardDSAEncoding.INSTANCE);
        }
    }
    */
    // END Android-removed: Unsupported algorithm

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSAnone
        extends SignatureSpi
    {
        public ecDSAnone()
        {
            super(new NullDigest(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSA224
        extends SignatureSpi
    {
        public ecDSA224()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA224(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
            super(AndroidDigestFactory.getSHA224(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class ecDetDSA224
        extends SignatureSpi
    {
        public ecDetDSA224()
        {
            super(DigestFactory.createSHA224(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA224())), StandardDSAEncoding.INSTANCE);
        }
    }
    */
    // END Android-removed: Unsupported algorithm

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSA256
        extends SignatureSpi
    {
        public ecDSA256()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA256(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
            super(AndroidDigestFactory.getSHA256(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class ecDetDSA256
        extends SignatureSpi
    {
        public ecDetDSA256()
        {
            super(DigestFactory.createSHA256(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA256())), StandardDSAEncoding.INSTANCE);
        }
    }
    */
    // END Android-removed: Unsupported algorithm

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSA384
        extends SignatureSpi
    {
        public ecDSA384()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA384(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
            super(AndroidDigestFactory.getSHA384(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    // BEGIN Android-removed: Unsupported algorithm
    /*
    static public class ecDetDSA384
        extends SignatureSpi
    {
        public ecDetDSA384()
        {
            super(DigestFactory.createSHA384(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA384())), StandardDSAEncoding.INSTANCE);
        }
    }
    */
    // END Android-removed: Unsupported algorithms

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class ecDSA512
        extends SignatureSpi
    {
        public ecDSA512()
        {
            // Android-changed: Use Android digests
            // super(DigestFactory.createSHA512(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
            super(AndroidDigestFactory.getSHA512(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    // BEGIN Android-removed: Unsupported algorithms
    /*
    static public class ecDetDSA512
        extends SignatureSpi
    {
        public ecDetDSA512()
        {
            super(DigestFactory.createSHA512(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA512())), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDSASha3_224
        extends SignatureSpi
    {
        public ecDSASha3_224()
        {
            super(DigestFactory.createSHA3_224(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDetDSASha3_224
        extends SignatureSpi
    {
        public ecDetDSASha3_224()
        {
            super(DigestFactory.createSHA3_224(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_224())), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDSASha3_256
        extends SignatureSpi
    {
        public ecDSASha3_256()
        {
            super(DigestFactory.createSHA3_256(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDetDSASha3_256
        extends SignatureSpi
    {
        public ecDetDSASha3_256()
        {
            super(DigestFactory.createSHA3_256(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_256())), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDSASha3_384
        extends SignatureSpi
    {
        public ecDSASha3_384()
        {
            super(DigestFactory.createSHA3_384(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDetDSASha3_384
        extends SignatureSpi
    {
        public ecDetDSASha3_384()
        {
            super(DigestFactory.createSHA3_384(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_384())), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDSASha3_512
        extends SignatureSpi
    {
        public ecDSASha3_512()
        {
            super(DigestFactory.createSHA3_512(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDetDSASha3_512
        extends SignatureSpi
    {
        public ecDetDSASha3_512()
        {
            super(DigestFactory.createSHA3_512(), new ECDSASigner(new HMacDSAKCalculator(DigestFactory.createSHA3_512())), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecDSARipeMD160
        extends SignatureSpi
    {
        public ecDSARipeMD160()
        {
            super(new RIPEMD160Digest(), new ECDSASigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecNR
        extends SignatureSpi
    {
        public ecNR()
        {
            super(DigestFactory.createSHA1(), new ECNRSigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecNR224
        extends SignatureSpi
    {
        public ecNR224()
        {
            super(DigestFactory.createSHA224(), new ECNRSigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecNR256
        extends SignatureSpi
    {
        public ecNR256()
        {
            super(DigestFactory.createSHA256(), new ECNRSigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecNR384
        extends SignatureSpi
    {
        public ecNR384()
        {
            super(DigestFactory.createSHA384(), new ECNRSigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecNR512
        extends SignatureSpi
    {
        public ecNR512()
        {
            super(DigestFactory.createSHA512(), new ECNRSigner(), StandardDSAEncoding.INSTANCE);
        }
    }

    static public class ecCVCDSA
        extends SignatureSpi
    {
        public ecCVCDSA()
        {
            super(DigestFactory.createSHA1(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }

    static public class ecCVCDSA224
        extends SignatureSpi
    {
        public ecCVCDSA224()
        {
            super(DigestFactory.createSHA224(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }

    static public class ecCVCDSA256
        extends SignatureSpi
    {
        public ecCVCDSA256()
        {
            super(DigestFactory.createSHA256(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }

    static public class ecCVCDSA384
        extends SignatureSpi
    {
        public ecCVCDSA384()
        {
            super(DigestFactory.createSHA384(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }

    static public class ecCVCDSA512
        extends SignatureSpi
    {
        public ecCVCDSA512()
        {
            super(DigestFactory.createSHA512(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }

    static public class ecPlainDSARP160
        extends SignatureSpi
    {
        public ecPlainDSARP160()
        {
            super(new RIPEMD160Digest(), new ECDSASigner(), PlainDSAEncoding.INSTANCE);
        }
    }
    */
    // END Android-removed: Unsupported algorithms
}
