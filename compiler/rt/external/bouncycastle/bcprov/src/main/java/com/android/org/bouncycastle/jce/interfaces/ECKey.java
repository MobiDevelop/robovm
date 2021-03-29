/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.interfaces;

import com.android.org.bouncycastle.jce.spec.ECParameterSpec;

/**
 * generic interface for an Elliptic Curve Key.
 * @hide This class is not part of the Android public SDK API
 */
public interface ECKey
{
    /**
     * return a parameter specification representing the EC domain parameters
     * for the key.
     */
    public ECParameterSpec getParameters();
}
