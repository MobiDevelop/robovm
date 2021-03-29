/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.util;

import java.security.Provider;
import java.security.Security;

import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * A JCA/JCE helper that refers to the BC provider for all it's needs.
 * @hide This class is not part of the Android public SDK API
 */
public class BCJcaJceHelper
    extends ProviderJcaJceHelper
{
    private static volatile Provider bcProvider;

    private static synchronized Provider getBouncyCastleProvider()
    {
        if (Security.getProvider("BC") != null)
        {
            return Security.getProvider("BC");
        }
        else if (bcProvider != null)
        {
            return bcProvider;
        }
        else
        {
            bcProvider = new BouncyCastleProvider();

            return bcProvider;
        }
    }

    public BCJcaJceHelper()
    {
        super(getBouncyCastleProvider());
    }
}
