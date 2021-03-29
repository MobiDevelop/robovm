/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.modes.gcm;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface GCMExponentiator
{
    void init(byte[] x);
    void exponentiateX(long pow, byte[] output);
}
