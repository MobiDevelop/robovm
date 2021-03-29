/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.keystore;

import com.android.org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import com.android.org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class PKCS12
{
    private static final String PREFIX = "com.android.org.bouncycastle.jcajce.provider.keystore" + ".pkcs12.";

    /**
     * @hide This class is not part of the Android public SDK API
     */
    public static class Mappings
        extends AsymmetricAlgorithmProvider
    {
        public Mappings()
        {
        }
        
        public void configure(ConfigurableProvider provider)
        {
            provider.addAlgorithm("KeyStore.PKCS12", PREFIX + "PKCS12KeyStoreSpi$BCPKCS12KeyStore");
            // BEGIN Android-removed: Unsupported algorithms
            /*
            provider.addAlgorithm("KeyStore.BCPKCS12", PREFIX + "PKCS12KeyStoreSpi$BCPKCS12KeyStore");
            provider.addAlgorithm("KeyStore.PKCS12-DEF", PREFIX + "PKCS12KeyStoreSpi$DefPKCS12KeyStore");

            provider.addAlgorithm("KeyStore.PKCS12-3DES-40RC2", PREFIX + "PKCS12KeyStoreSpi$BCPKCS12KeyStore");
            provider.addAlgorithm("KeyStore.PKCS12-3DES-3DES", PREFIX + "PKCS12KeyStoreSpi$BCPKCS12KeyStore3DES");
    
            provider.addAlgorithm("KeyStore.PKCS12-DEF-3DES-40RC2", PREFIX + "PKCS12KeyStoreSpi$DefPKCS12KeyStore");
            provider.addAlgorithm("KeyStore.PKCS12-DEF-3DES-3DES", PREFIX + "PKCS12KeyStoreSpi$DefPKCS12KeyStore3DES");
            */
            // END Android-removed: Unsupported algorithms
        }
    }
}
