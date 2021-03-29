/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.digest;

import com.android.org.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.digests.SHA1Digest;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBESecretKeyFactory;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class SHA1
{
    private SHA1()
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
            super(new SHA1Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new SHA1Digest((SHA1Digest)digest);

            return d;
        }
    }

    /**
     * SHA1 HMac
     * @hide This class is not part of the Android public SDK API
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new SHA1Digest()));
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
            super("HMACSHA1", 160, new CipherKeyGenerator());
        }
    }

    /**
     * SHA1 HMac
     * @hide This class is not part of the Android public SDK API
     */
    public static class SHA1Mac
        extends BaseMac
    {
        public SHA1Mac()
        {
            super(new HMac(new SHA1Digest()));
        }
    }

    /**
     * PBEWithHmacSHA
     * @hide This class is not part of the Android public SDK API
     */
    public static class PBEWithMacKeyFactory
        extends PBESecretKeyFactory
    {
        public PBEWithMacKeyFactory()
        {
            super("PBEwithHmacSHA", null, false, PKCS12, SHA1, 160, 0);
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = SHA1.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.SHA-1", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest.SHA1", "SHA-1");
            provider.addAlgorithm("Alg.Alias.MessageDigest.SHA", "SHA-1");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + OIWObjectIdentifiers.idSHA1, "SHA-1");

            addHMACAlgorithm(provider, "SHA1", PREFIX + "$HashMac", PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "SHA1", PKCSObjectIdentifiers.id_hmacWithSHA1);
            addHMACAlias(provider, "SHA1", IANAObjectIdentifiers.hmacSHA1);

            provider.addAlgorithm("Mac.PBEWITHHMACSHA", PREFIX + "$SHA1Mac");
            provider.addAlgorithm("Mac.PBEWITHHMACSHA1", PREFIX + "$SHA1Mac");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory.PBEWITHHMACSHA", "PBEWITHHMACSHA1");
            provider.addAlgorithm("Alg.Alias.SecretKeyFactory." + OIWObjectIdentifiers.idSHA1, "PBEWITHHMACSHA1");
            provider.addAlgorithm("Alg.Alias.Mac." + OIWObjectIdentifiers.idSHA1, "PBEWITHHMACSHA");

            provider.addAlgorithm("SecretKeyFactory.PBEWITHHMACSHA1", PREFIX + "$PBEWithMacKeyFactory");
          }
    }
}
