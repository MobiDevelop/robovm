/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util;

/**
 * Exception thrown if there's an issue doing a match in store.
 * @hide This class is not part of the Android public SDK API
 */
public class StoreException
    extends RuntimeException
{
    private Throwable _e;

    /**
     * Basic Constructor.
     *
     * @param msg message to be associated with this exception.
     * @param cause the throwable that caused this exception to be raised.
     */
    public StoreException(String msg, Throwable cause)
    {
        super(msg);
        _e = cause;
    }

    public Throwable getCause()
    {
        return _e;
    }
}
