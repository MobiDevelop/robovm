/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util;

import java.io.IOException;

/**
 * Interface implemented by objects that can be converted into byte arrays.
 * @hide This class is not part of the Android public SDK API
 */
public interface Encodable
{
    /**
     * Return a byte array representing the implementing object.
     *
     * @return a byte array representing the encoding.
     * @throws java.io.IOException if an issue arises generation the encoding.
     */
    byte[] getEncoded()
        throws IOException;
}
