/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * interface for Elliptic Curve Private keys.
 * @hide This class is not part of the Android public SDK API
 */
public interface ECPrivateKey
    extends ECKey, PrivateKey
{
    /**
     * return the private value D.
     */
    public BigInteger getD();
}
