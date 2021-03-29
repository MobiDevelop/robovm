/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util.io.pem;

import java.io.IOException;

/**
 * Exception thrown on failure to generate a PEM object.
 * @hide This class is not part of the Android public SDK API
 */
public class PemGenerationException
    extends IOException
{
    private Throwable cause;

    public PemGenerationException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public PemGenerationException(String message)
    {
        super(message);
    }

    public Throwable getCause()
    {
        return cause;
    }
}
