// RoboVM Note: FIXME: using existing (old) implementation in rt/robovm folder
// RoboVM Note:        to reuse native code for quicker start-up
///*
// * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// *
// * This code is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 only, as
// * published by the Free Software Foundation.  Oracle designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Oracle in the LICENSE file that accompanied this code.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// * version 2 for more details (a copy is included in the LICENSE file that
// * accompanied this code).
// *
// * You should have received a copy of the GNU General Public License version
// * 2 along with this work; if not, write to the Free Software Foundation,
// * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// *
// * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
// * or visit www.oracle.com if you need additional information or have any
// * questions.
// */
//
//package sun.misc;
//
//import dalvik.annotation.optimization.FastNative;
//import sun.reflect.Reflection;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//
///**
// * A collection of methods for performing low-level, unsafe operations.
// * Although the class and all methods are public, use of this class is
// * limited because only trusted code can obtain instances of it.
// *
// * @author John R. Rose
// * @see #getUnsafe
// */
//public final class Unsafe {
//    /** Traditional dalvik name. */
//    private static final Unsafe THE_ONE = new Unsafe();
//
//    private static final Unsafe theUnsafe = THE_ONE;
//    public static final int INVALID_FIELD_OFFSET   = -1;
//
//    /**
//     * This class is only privately instantiable.
//     */
//    private Unsafe() {}
//
//    /**
//     * Gets the unique instance of this class. This is only allowed in
//     * very limited situations.
//     */
//    public static Unsafe getUnsafe() {
//        Class<?> caller = Reflection.getCallerClass();
//        /*
//         * Only code on the bootclasspath is allowed to get at the
//         * Unsafe instance.
//         */
//        ClassLoader calling = (caller == null) ? null : caller.getClassLoader();
//        if ((calling != null) && (calling != Unsafe.class.getClassLoader())) {
//            throw new SecurityException("Unsafe access denied");
//        }
//
//        return THE_ONE;
//    }
//
//    /**
//     * Gets the raw byte offset from the start of an object's memory to
//     * the memory used to store the indicated instance field.
//     *
//     * @param field non-null; the field in question, which must be an
//     * instance field
//     * @return the offset to the field
//     */
//    public long objectFieldOffset(Field field) {
//        if (Modifier.isStatic(field.getModifiers())) {
//            throw new IllegalArgumentException("valid for instance fields only");
//        }
//        return field.getOffset();
//    }
//
//    /**
//     * Gets the offset from the start of an array object's memory to
//     * the memory used to store its initial (zeroeth) element.
//     *
//     * @param clazz non-null; class in question; must be an array class
//     * @return the offset to the initial element
//     */
//    public int arrayBaseOffset(Class clazz) {
//        Class<?> component = clazz.getComponentType();
//        if (component == null) {
//            throw new IllegalArgumentException("Valid for array classes only: " + clazz);
//        }
//        return getArrayBaseOffsetForComponentType(component);
//    }
//
//    /**
//     * Gets the size of each element of the given array class.
//     *
//     * @param clazz non-null; class in question; must be an array class
//     * @return &gt; 0; the size of each element of the array
//     */
//    public int arrayIndexScale(Class clazz) {
//      Class<?> component = clazz.getComponentType();
//      if (component == null) {
//          throw new IllegalArgumentException("Valid for array classes only: " + clazz);
//      }
//      return getArrayIndexScaleForComponentType(component);
//    }
//
//    @FastNative
//    private static native int getArrayBaseOffsetForComponentType(Class component_class);
//    @FastNative
//    private static native int getArrayIndexScaleForComponentType(Class component_class);
//
//    /**
//     * Performs a compare-and-set operation on an <code>int</code>
//     * field within the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param expectedValue expected value of the field
//     * @param newValue new value to store in the field if the contents are
//     * as expected
//     * @return <code>true</code> if the new value was in fact stored, and
//     * <code>false</code> if not
//     */
//    @FastNative
//    public native boolean compareAndSwapInt(Object obj, long offset,
//            int expectedValue, int newValue);
//
//    /**
//     * Performs a compare-and-set operation on a <code>long</code>
//     * field within the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param expectedValue expected value of the field
//     * @param newValue new value to store in the field if the contents are
//     * as expected
//     * @return <code>true</code> if the new value was in fact stored, and
//     * <code>false</code> if not
//     */
//    @FastNative
//    public native boolean compareAndSwapLong(Object obj, long offset,
//            long expectedValue, long newValue);
//
//    /**
//     * Performs a compare-and-set operation on an <code>Object</code>
//     * field (that is, a reference field) within the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param expectedValue expected value of the field
//     * @param newValue new value to store in the field if the contents are
//     * as expected
//     * @return <code>true</code> if the new value was in fact stored, and
//     * <code>false</code> if not
//     */
//    @FastNative
//    public native boolean compareAndSwapObject(Object obj, long offset,
//            Object expectedValue, Object newValue);
//
//    /**
//     * Gets an <code>int</code> field from the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native int getIntVolatile(Object obj, long offset);
//
//    /**
//     * Stores an <code>int</code> field into the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putIntVolatile(Object obj, long offset, int newValue);
//
//    /**
//     * Gets a <code>long</code> field from the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native long getLongVolatile(Object obj, long offset);
//
//    /**
//     * Stores a <code>long</code> field into the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putLongVolatile(Object obj, long offset, long newValue);
//
//    /**
//     * Gets an <code>Object</code> field from the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native Object getObjectVolatile(Object obj, long offset);
//
//    /**
//     * Stores an <code>Object</code> field into the given object,
//     * using <code>volatile</code> semantics.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putObjectVolatile(Object obj, long offset,
//            Object newValue);
//
//    /**
//     * Gets an <code>int</code> field from the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native int getInt(Object obj, long offset);
//
//    /**
//     * Stores an <code>int</code> field into the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putInt(Object obj, long offset, int newValue);
//
//    /**
//     * Lazy set an int field.
//     */
//    @FastNative
//    public native void putOrderedInt(Object obj, long offset, int newValue);
//
//    /**
//     * Gets a <code>long</code> field from the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native long getLong(Object obj, long offset);
//
//    /**
//     * Stores a <code>long</code> field into the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putLong(Object obj, long offset, long newValue);
//
//    /**
//     * Lazy set a long field.
//     */
//    @FastNative
//    public native void putOrderedLong(Object obj, long offset, long newValue);
//
//    /**
//     * Gets an <code>Object</code> field from the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @return the retrieved value
//     */
//    @FastNative
//    public native Object getObject(Object obj, long offset);
//
//    /**
//     * Stores an <code>Object</code> field into the given object.
//     *
//     * @param obj non-null; object containing the field
//     * @param offset offset to the field within <code>obj</code>
//     * @param newValue the value to store
//     */
//    @FastNative
//    public native void putObject(Object obj, long offset, Object newValue);
//
//    /**
//     * Lazy set an object field.
//     */
//    @FastNative
//    public native void putOrderedObject(Object obj, long offset,
//            Object newValue);
//
//
//    @FastNative
//    public native boolean getBoolean(Object obj, long offset);
//    @FastNative
//    public native void putBoolean(Object obj, long offset, boolean newValue);
//    @FastNative
//    public native byte getByte(Object obj, long offset);
//    @FastNative
//    public native void putByte(Object obj, long offset, byte newValue);
//    @FastNative
//    public native char getChar(Object obj, long offset);
//    @FastNative
//    public native void putChar(Object obj, long offset, char newValue);
//    @FastNative
//    public native short getShort(Object obj, long offset);
//    @FastNative
//    public native void putShort(Object obj, long offset, short newValue);
//    @FastNative
//    public native float getFloat(Object obj, long offset);
//    @FastNative
//    public native void putFloat(Object obj, long offset, float newValue);
//    @FastNative
//    public native double getDouble(Object obj, long offset);
//    @FastNative
//    public native void putDouble(Object obj, long offset, double newValue);
//
//    /**
//     * Parks the calling thread for the specified amount of time,
//     * unless the "permit" for the thread is already available (due to
//     * a previous call to {@link #unpark}. This method may also return
//     * spuriously (that is, without the thread being told to unpark
//     * and without the indicated amount of time elapsing).
//     *
//     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
//     * in-depth information of the behavior of this method.</p>
//     *
//     * @param absolute whether the given time value is absolute
//     * milliseconds-since-the-epoch (<code>true</code>) or relative
//     * nanoseconds-from-now (<code>false</code>)
//     * @param time the (absolute millis or relative nanos) time value
//     */
//    public native void park(boolean absolute, long time);
//    /**
//     * Unparks the given object, which must be a {@link Thread}.
//     *
//     * <p>See {@link java.util.concurrent.locks.LockSupport} for more
//     * in-depth information of the behavior of this method.</p>
//     *
//     * @param obj non-null; the object to unpark
//     */
//    @FastNative
//    public native void unpark(Object obj);
//    /**
//     * Allocates an instance of the given class without running the constructor.
//     * The class' <clinit> will be run, if necessary.
//     */
//    public native Object allocateInstance(Class<?> c);
//
//    @FastNative
//    public native int addressSize();
//
//    @FastNative
//    public native int pageSize();
//
//    @FastNative
//    public native long allocateMemory(long bytes);
//
//    @FastNative
//    public native void freeMemory(long address);
//
//    @FastNative
//    public native void setMemory(long address, long bytes, byte value);
//
//    @FastNative
//    public native byte getByte(long address);
//
//    @FastNative
//    public native void putByte(long address, byte x);
//
//    @FastNative
//    public native short getShort(long address);
//
//    @FastNative
//    public native void putShort(long address, short x);
//
//    @FastNative
//    public native char getChar(long address);
//
//    @FastNative
//    public native void putChar(long address, char x);
//
//    @FastNative
//    public native int getInt(long address);
//
//    @FastNative
//    public native void putInt(long address, int x);
//
//    @FastNative
//    public native long getLong(long address);
//
//    @FastNative
//    public native void putLong(long address, long x);
//
//    @FastNative
//    public native float getFloat(long address);
//
//    @FastNative
//    public native void putFloat(long address, float x);
//
//    @FastNative
//    public native double getDouble(long address);
//
//    @FastNative
//    public native void putDouble(long address, double x);
//
//    @FastNative
//    public native void copyMemoryToPrimitiveArray(long srcAddr,
//            Object dst, long dstOffset, long bytes);
//
//    @FastNative
//    public native void copyMemoryFromPrimitiveArray(Object src, long srcOffset,
//            long dstAddr, long bytes);
//
//    @FastNative
//    public native void copyMemory(long srcAddr, long dstAddr, long bytes);
//
//
//    // The following contain CAS-based Java implementations used on
//    // platforms not supporting native instructions
//
//    /**
//     * Atomically adds the given value to the current value of a field
//     * or array element within the given object {@code o}
//     * at the given {@code offset}.
//     *
//     * @param o object/array to update the field/element in
//     * @param offset field/element offset
//     * @param delta the value to add
//     * @return the previous value
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    public final int getAndAddInt(Object o, long offset, int delta) {
//        int v;
//        do {
//            v = getIntVolatile(o, offset);
//        } while (!compareAndSwapInt(o, offset, v, v + delta));
//        return v;
//    }
//
//    /**
//     * Atomically adds the given value to the current value of a field
//     * or array element within the given object {@code o}
//     * at the given {@code offset}.
//     *
//     * @param o object/array to update the field/element in
//     * @param offset field/element offset
//     * @param delta the value to add
//     * @return the previous value
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    public final long getAndAddLong(Object o, long offset, long delta) {
//        long v;
//        do {
//            v = getLongVolatile(o, offset);
//        } while (!compareAndSwapLong(o, offset, v, v + delta));
//        return v;
//    }
//
//    /**
//     * Atomically exchanges the given value with the current value of
//     * a field or array element within the given object {@code o}
//     * at the given {@code offset}.
//     *
//     * @param o object/array to update the field/element in
//     * @param offset field/element offset
//     * @param newValue new value
//     * @return the previous value
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    public final int getAndSetInt(Object o, long offset, int newValue) {
//        int v;
//        do {
//            v = getIntVolatile(o, offset);
//        } while (!compareAndSwapInt(o, offset, v, newValue));
//        return v;
//    }
//
//    /**
//     * Atomically exchanges the given value with the current value of
//     * a field or array element within the given object {@code o}
//     * at the given {@code offset}.
//     *
//     * @param o object/array to update the field/element in
//     * @param offset field/element offset
//     * @param newValue new value
//     * @return the previous value
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    public final long getAndSetLong(Object o, long offset, long newValue) {
//        long v;
//        do {
//            v = getLongVolatile(o, offset);
//        } while (!compareAndSwapLong(o, offset, v, newValue));
//        return v;
//    }
//
//    /**
//     * Atomically exchanges the given reference value with the current
//     * reference value of a field or array element within the given
//     * object {@code o} at the given {@code offset}.
//     *
//     * @param o object/array to update the field/element in
//     * @param offset field/element offset
//     * @param newValue new value
//     * @return the previous value
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    public final Object getAndSetObject(Object o, long offset, Object newValue) {
//        Object v;
//        do {
//            v = getObjectVolatile(o, offset);
//        } while (!compareAndSwapObject(o, offset, v, newValue));
//        return v;
//    }
//
//
//    /**
//     * Ensures that loads before the fence will not be reordered with loads and
//     * stores after the fence; a "LoadLoad plus LoadStore barrier".
//     *
//     * Corresponds to C11 atomic_thread_fence(memory_order_acquire)
//     * (an "acquire fence").
//     *
//     * A pure LoadLoad fence is not provided, since the addition of LoadStore
//     * is almost always desired, and most current hardware instructions that
//     * provide a LoadLoad barrier also provide a LoadStore barrier for free.
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    @FastNative
//    public native void loadFence();
//
//    /**
//     * Ensures that loads and stores before the fence will not be reordered with
//     * stores after the fence; a "StoreStore plus LoadStore barrier".
//     *
//     * Corresponds to C11 atomic_thread_fence(memory_order_release)
//     * (a "release fence").
//     *
//     * A pure StoreStore fence is not provided, since the addition of LoadStore
//     * is almost always desired, and most current hardware instructions that
//     * provide a StoreStore barrier also provide a LoadStore barrier for free.
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    @FastNative
//    public native void storeFence();
//
//    /**
//     * Ensures that loads and stores before the fence will not be reordered
//     * with loads and stores after the fence.  Implies the effects of both
//     * loadFence() and storeFence(), and in addition, the effect of a StoreLoad
//     * barrier.
//     *
//     * Corresponds to C11 atomic_thread_fence(memory_order_seq_cst).
//     * @since 1.8
//     */
//    // @HotSpotIntrinsicCandidate
//    @FastNative
//    public native void fullFence();
//}
