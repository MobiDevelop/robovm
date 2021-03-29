/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface Wrapper
{
    public void init(boolean forWrapping, CipherParameters param);

    /**
     * Return the name of the algorithm the wrapper implements.
     *
     * @return the name of the algorithm the wrapper implements.
     */
    public String getAlgorithmName();

    public byte[] wrap(byte[] in, int inOff, int inLen);

    public byte[] unwrap(byte[] in, int inOff, int inLen)
        throws InvalidCipherTextException;
}
