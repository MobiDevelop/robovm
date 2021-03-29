/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto;

/**
 * @hide This class is not part of the Android public SDK API
 */
public interface ExtendedDigest 
    extends Digest
{
    /**
     * Return the size in bytes of the internal buffer the digest applies it's compression
     * function to.
     * 
     * @return byte length of the digests internal buffer.
     */
    public int getByteLength();
}
