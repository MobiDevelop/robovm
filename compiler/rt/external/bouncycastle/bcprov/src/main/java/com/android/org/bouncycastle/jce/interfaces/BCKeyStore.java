/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.interfaces;

import java.security.SecureRandom;

/**
 * all BC provider keystores implement this interface.
 * @hide This class is not part of the Android public SDK API
 */
public interface BCKeyStore
{
    /**
     * set the random source for the key store
     */
    public void setRandom(SecureRandom random);
}
