/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.symmetric;

import com.android.org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.engines.BlowfishEngine;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.macs.CMac;
import com.android.org.bouncycastle.crypto.modes.CBCBlockCipher;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.IvAlgorithmParameters;
import com.android.org.bouncycastle.jcajce.provider.util.AlgorithmProvider;

/**
 * @hide This class is not part of the Android public SDK API
 */
public final class Blowfish
{
    private Blowfish()
    {
    }
    
    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class ECB
        extends BaseBlockCipher
    {
        public ECB()
        {
            super(new BlowfishEngine());
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class CBC
        extends BaseBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new BlowfishEngine()), 64);
        }
    }

    // BEGIN Android-removed: Unsupported algorithms
    /*
    public static class CMAC
        extends BaseMac
    {
        public CMAC()
        {
            super(new CMac(new BlowfishEngine()));
        }
    }
    */
    // END Android-removed: Unsupported algorithms

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class KeyGen
        extends BaseKeyGenerator
    {
        public KeyGen()
        {
            super("Blowfish", 128, new CipherKeyGenerator());
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class AlgParams
        extends IvAlgorithmParameters
    {
        protected String engineToString()
        {
            return "Blowfish IV";
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends AlgorithmProvider
    {
        private static final String PREFIX = Blowfish.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            // Android-removed: Unsupported algorithms
            // provider.addAlgorithm("Mac.BLOWFISHCMAC", PREFIX + "$CMAC");
            provider.addAlgorithm("Cipher.BLOWFISH", PREFIX + "$ECB");
            // Android-removed: Unsupported algorithms
            // provider.addAlgorithm("Cipher", MiscObjectIdentifiers.cryptlib_algorithm_blowfish_CBC, PREFIX + "$CBC");
            provider.addAlgorithm("KeyGenerator.BLOWFISH", PREFIX + "$KeyGen");
            provider.addAlgorithm("Alg.Alias.KeyGenerator", MiscObjectIdentifiers.cryptlib_algorithm_blowfish_CBC, "BLOWFISH");
            provider.addAlgorithm("AlgorithmParameters.BLOWFISH", PREFIX + "$AlgParams");
            provider.addAlgorithm("Alg.Alias.AlgorithmParameters", MiscObjectIdentifiers.cryptlib_algorithm_blowfish_CBC, "BLOWFISH");

        }
    }
}
