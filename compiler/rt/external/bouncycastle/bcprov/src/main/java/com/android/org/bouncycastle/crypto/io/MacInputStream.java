/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.crypto.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.android.org.bouncycastle.crypto.Mac;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class MacInputStream
    extends FilterInputStream
{
    protected Mac mac;

    public MacInputStream(
        InputStream stream,
        Mac         mac)
    {
        super(stream);
        this.mac = mac;
    }

    public int read()
        throws IOException
    {
        int b = in.read();

        if (b >= 0)
        {
            mac.update((byte)b);
        }
        return b;
    }

    public int read(
        byte[] b,
        int off,
        int len)
        throws IOException
    {
        int n = in.read(b, off, len);
        if (n >= 0)
        {
            mac.update(b, off, n);
        }
        return n;
    }

    public Mac getMac()
    {
        return mac;
    }
}
