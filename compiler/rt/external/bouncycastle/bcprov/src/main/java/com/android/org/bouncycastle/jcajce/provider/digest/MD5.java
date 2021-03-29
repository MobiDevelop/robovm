/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.digest;

import com.android.org.bouncycastle.asn1.iana.IANAObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.digests.MD5Digest;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class MD5
{
    private MD5()
    {

    }

    /**
     * MD5 HashMac
     * @hide This class is not part of the Android public SDK API
     */
    public static class HashMac
        extends BaseMac
    {
        public HashMac()
        {
            super(new HMac(new MD5Digest()));
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
            super("HMACMD5", 128, new CipherKeyGenerator());
        }
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
            super(new MD5Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new MD5Digest((MD5Digest)digest);

            return d;
        }
    }

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = MD5.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.MD5", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + PKCSObjectIdentifiers.md5, "MD5");

            addHMACAlgorithm(provider, "MD5", PREFIX + "$HashMac", PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "MD5", IANAObjectIdentifiers.hmacMD5);
        }
    }
}
