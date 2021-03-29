/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;

import javax.crypto.spec.DHParameterSpec;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.CryptoServicesRegistrar;
import com.android.org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import com.android.org.bouncycastle.crypto.generators.DHParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import com.android.org.bouncycastle.jcajce.spec.DHDomainParameterSpec;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Integers;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class KeyPairGeneratorSpi
    extends java.security.KeyPairGenerator
{
    private static Hashtable params = new Hashtable();
    private static Object    lock = new Object();

    DHKeyGenerationParameters param;
    DHBasicKeyPairGenerator engine = new DHBasicKeyPairGenerator();
    int strength = 2048;
    SecureRandom random = CryptoServicesRegistrar.getSecureRandom();
    boolean initialised = false;

    public KeyPairGeneratorSpi()
    {
        super("DH");
    }

    public void initialize(
        int strength,
        SecureRandom random)
    {
        this.strength = strength;
        this.random = random;
        this.initialised = false;
    }

    public void initialize(
        AlgorithmParameterSpec params,
        SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        if (!(params instanceof DHParameterSpec))
        {
            throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec");
        }
        DHParameterSpec dhParams = (DHParameterSpec)params;

        try
        {
            param = convertParams(random, dhParams);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidAlgorithmParameterException(e.getMessage(), e);
        }
        
        engine.init(param);
        initialised = true;
    }

    private DHKeyGenerationParameters convertParams(SecureRandom random, DHParameterSpec dhParams)
    {
        // BEGIN Android-removed: Don't special-case DHDomainParameterSpec
        // When DHDomainParameterSpec is special-cased here, it supplies a value for q that
        // ultimately results in a smaller value of x, which runs afoul of the Wycheproof test
        // com.google.security.wycheproof.DhTest.testKeyPairGenerator().  See the docs in DhTest
        // for more details of why that requirement is made.
        //
        // While we believe this code would be safe (and likely somewhat faster), in the interest
        // of being conservative we've disabled it to preserve the old behavior that also passes
        // the Wycheproof test.
        /*
        if (dhParams instanceof DHDomainParameterSpec)
        {
            return new DHKeyGenerationParameters(random, ((DHDomainParameterSpec)dhParams).getDomainParameters());
        }
        */
        // END Android-removed: Don't special-case DHDomainParameterSpec
        return new DHKeyGenerationParameters(random, new DHParameters(dhParams.getP(), dhParams.getG(), null, dhParams.getL()));
    }

    public KeyPair generateKeyPair()
    {
        if (!initialised)
        {
            Integer paramStrength = Integers.valueOf(strength);

            if (params.containsKey(paramStrength))
            {
                param = (DHKeyGenerationParameters)params.get(paramStrength);
            }
            else
            {
                DHParameterSpec dhParams = BouncyCastleProvider.CONFIGURATION.getDHDefaultParameters(strength);

                if (dhParams != null)
                {   
                    param = convertParams(random, dhParams);
                }
                else
                {
                    synchronized (lock)
                    {
                        // we do the check again in case we were blocked by a generator for
                        // our key size.
                        if (params.containsKey(paramStrength))
                        {
                            param = (DHKeyGenerationParameters)params.get(paramStrength);
                        }
                        else
                        {

                            DHParametersGenerator pGen = new DHParametersGenerator();

                            pGen.init(strength, PrimeCertaintyCalculator.getDefaultCertainty(strength), random);

                            param = new DHKeyGenerationParameters(random, pGen.generateParameters());

                            params.put(paramStrength, param);
                        }
                    }
                }
            }

            engine.init(param);

            initialised = true;
        }

        AsymmetricCipherKeyPair pair = engine.generateKeyPair();
        DHPublicKeyParameters pub = (DHPublicKeyParameters)pair.getPublic();
        DHPrivateKeyParameters priv = (DHPrivateKeyParameters)pair.getPrivate();

        return new KeyPair(new BCDHPublicKey(pub), new BCDHPrivateKey(priv));
    }
}
