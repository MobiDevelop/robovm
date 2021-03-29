/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.digest;

import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.digests.SHA224Digest;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class SHA224
{
    private SHA224()
    {

    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    static public class Digest
        extends BCMessageDigest
        implements Cloneable
    {
        public Digest()
        {
            super(new SHA224Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new SHA224Digest((SHA224Digest)digest);

            return d;
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new SHA224Digest()));
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        public KeyGenerator()
        {
            super("HMACSHA224", 224, new CipherKeyGenerator());
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = SHA224.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.SHA-224", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest.SHA224", "SHA-224");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + NISTObjectIdentifiers.id_sha224, "SHA-224");

            provider.addAlgorithm("Mac.PBEWITHHMACSHA224", PREFIX + "$HashMac");

            addHMACAlgorithm(provider, "SHA224", PREFIX + "$HashMac",  PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "SHA224", PKCSObjectIdentifiers.id_hmacWithSHA224);

        }
    }
}
