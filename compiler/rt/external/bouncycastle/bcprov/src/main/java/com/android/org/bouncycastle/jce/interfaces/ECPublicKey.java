/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.interfaces;

import java.security.PublicKey;

import com.android.org.bouncycastle.math.ec.ECPoint;

/**
 * interface for elliptic curve public keys.
 * @hide This class is not part of the Android public SDK API
 */
public interface ECPublicKey
    extends ECKey, PublicKey
{
    /**
     * return the public point Q
     */
    public ECPoint getQ();
}
