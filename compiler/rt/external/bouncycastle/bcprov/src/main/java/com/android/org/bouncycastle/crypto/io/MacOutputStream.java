/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.io;

import java.io.IOException;
import java.io.OutputStream;

import com.android.org.bouncycastle.crypto.Mac;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class MacOutputStream
    extends OutputStream
{
    protected Mac mac;

    public MacOutputStream(
        Mac          mac)
    {
        this.mac = mac;
    }

    public void write(int b)
        throws IOException
    {
        mac.update((byte)b);
    }

    public void write(
        byte[] b,
        int off,
        int len)
        throws IOException
    {
        mac.update(b, off, len);
    }

    public byte[] getMac()
    {
        byte[] res = new byte[mac.getMacSize()];

        mac.doFinal(res, 0);

        return res;
    }
}
