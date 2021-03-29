/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import java.security.spec.InvalidKeySpecException;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ExtendedInvalidKeySpecException
    extends InvalidKeySpecException
{
    private Throwable cause;

    public ExtendedInvalidKeySpecException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
