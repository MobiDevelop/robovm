/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.digest;

import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.digests.SHA384Digest;
import com.android.org.bouncycastle.crypto.macs.HMac;
// Android-removed: Unsupported algorithms
// import org.bouncycastle.crypto.macs.OldHMac;
import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseKeyGenerator;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.BaseMac;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class SHA384
{
    private SHA384()
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
            super(new SHA384Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Digest d = (Digest)super.clone();
            d.digest = new SHA384Digest((SHA384Digest)digest);

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
            super(new HMac(new SHA384Digest()));
        }
    }

    /**
     * HMACSHA384
     * @hide This class is not part of the Android public SDK API
     */
    public static class KeyGenerator
        extends BaseKeyGenerator
    {
        public KeyGenerator()
        {
            super("HMACSHA384", 384, new CipherKeyGenerator());
        }
    }

    // BEGIN Android-removed: Unsupported algorithms
    /*
    public static class OldSHA384
        extends BaseMac
    {
        public OldSHA384()
        {
            super(new OldHMac(new SHA384Digest()));
        }
    }
    */
    // END Android-removed: Unsupported algorithms

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends DigestAlgorithmProvider
    {
        private static final String PREFIX = SHA384.class.getName();

        public Mappings()
        {
        }

        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("MessageDigest.SHA-384", PREFIX + "$Digest");
            provider.addAlgorithm("Alg.Alias.MessageDigest.SHA384", "SHA-384");
            provider.addAlgorithm("Alg.Alias.MessageDigest." + NISTObjectIdentifiers.id_sha384, "SHA-384");
            // Android-removed: Unsupported algorithms
            // provider.addAlgorithm("Mac.OLDHMACSHA384", PREFIX + "$OldSHA384");

            provider.addAlgorithm("Mac.PBEWITHHMACSHA384", PREFIX + "$HashMac");

            addHMACAlgorithm(provider, "SHA384", PREFIX + "$HashMac",  PREFIX + "$KeyGenerator");
            addHMACAlias(provider, "SHA384", PKCSObjectIdentifiers.id_hmacWithSHA384);
        }
    }
}
