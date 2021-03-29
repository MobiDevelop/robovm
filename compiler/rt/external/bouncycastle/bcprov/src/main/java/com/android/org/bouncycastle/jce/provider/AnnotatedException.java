/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.jce.exception.ExtException;

/**
 * @hide This class is not part of the Android public SDK API
 */
public class AnnotatedException
    extends Exception
    implements ExtException
{
    private Throwable _underlyingException;

    public AnnotatedException(String string, Throwable e)
    {
        super(string);

        _underlyingException = e;
    }

    public AnnotatedException(String string)
    {
        this(string, null);
    }

    Throwable getUnderlyingException()
    {
        return _underlyingException;
    }

    public Throwable getCause()
    {
        return _underlyingException;
    }
}
