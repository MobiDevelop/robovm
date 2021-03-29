/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.io;

import java.io.IOException;
import java.io.OutputStream;

import com.android.org.bouncycastle.crypto.Digest;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class DigestOutputStream
    extends OutputStream
{
    protected Digest digest;

    public DigestOutputStream(
        Digest          Digest)
    {
        this.digest = Digest;
    }

    public void write(int b)
        throws IOException
    {
        digest.update((byte)b);
    }

    public void write(
        byte[] b,
        int off,
        int len)
        throws IOException
    {
        digest.update(b, off, len);
    }

    public byte[] getDigest()
    {
        byte[] res = new byte[digest.getDigestSize()];
        
        digest.doFinal(res, 0);
        
        return res;
    }
}
