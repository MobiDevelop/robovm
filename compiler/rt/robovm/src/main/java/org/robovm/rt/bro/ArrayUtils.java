package org.robovm.rt.bro;

public class ArrayUtils {
    /**
     * Checks that the range described by {@code offset} and {@code count} doesn't exceed
     * {@code arrayLength}.
     *
     * @hide
     */
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            // TODO: fixme better message
            throw new ArrayIndexOutOfBoundsException();
        }
    }

}
