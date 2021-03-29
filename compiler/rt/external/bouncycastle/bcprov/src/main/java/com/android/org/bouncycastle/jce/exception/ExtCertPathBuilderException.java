/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.exception;

import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class ExtCertPathBuilderException
    extends CertPathBuilderException
    implements ExtException
{
    private Throwable cause;

    public ExtCertPathBuilderException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public ExtCertPathBuilderException(String msg, Throwable cause, 
        CertPath certPath, int index)
    {
        super(msg, cause);
        this.cause = cause;
    }
    
    public Throwable getCause()
    {
        return cause;
    }
}
