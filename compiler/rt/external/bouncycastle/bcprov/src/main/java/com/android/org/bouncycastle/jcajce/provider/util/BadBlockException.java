/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.util;

import javax.crypto.BadPaddingException;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class BadBlockException
    extends BadPaddingException
{
    private final Throwable cause;

    public BadBlockException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
