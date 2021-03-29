/* GENERATED SOURCE. DO NOT MODIFY. */
package com.android.org.bouncycastle.util;

/**
 * Interface a selector from a store should conform to.
 *
 * @param <T> the type stored in the store.
 * @hide This class is not part of the Android public SDK API
 */
public interface Selector<T>
    extends Cloneable
{
    /**
     * Match the passed in object, returning true if it would be selected by this selector, false otherwise.
     *
     * @param obj the object to be matched.
     * @return true if the object is a match for this selector, false otherwise.
     */
    boolean match(T obj);

    Object clone();
}
