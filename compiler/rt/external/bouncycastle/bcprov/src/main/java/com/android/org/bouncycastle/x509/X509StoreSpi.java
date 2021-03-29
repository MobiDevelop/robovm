/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.util.Selector;

import java.util.Collection;

/**
 * @hide This class is not part of the Android public SDK API
 */
public abstract class X509StoreSpi
{
    public abstract void engineInit(X509StoreParameters parameters);

    public abstract Collection engineGetMatches(Selector selector);
}
