/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dalvik.system;

import dalvik.annotation.compat.UnsupportedAppUsage;
import org.robovm.rt.VM;

import java.util.Arrays;

/**
 * Provides a limited interface to the Dalvik VM stack. This class is mostly
 * used for implementing security checks.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class VMStack {

    private VMStack() {
    }

    /**
     * Returns the defining class loader of the caller's caller.
     *
     * @return the requested class loader, or {@code null} if this is the
     *         bootstrap class loader.
     * @deprecated Use {@code ClassLoader.getClassLoader(sun.reflect.Reflection.getCallerClass())}.
     *         Note that that can return {@link BootClassLoader} on Android where the RI
     *         would have returned null.
     */
    @UnsupportedAppUsage
    @Deprecated
    public static ClassLoader getCallingClassLoader() {
        // RoboVM note: This is native in Android
        return VM.getStackClasses(1, 1)[0].getClassLoader();
    }

    /**
     * Returns the class of the caller's caller.
     *
     * @return the requested class, or {@code null}.
     * @deprecated Use {@link sun.reflect.Reflection#getCallerClass()}.
     */
    @Deprecated
    public static Class<?> getStackClass1() {
        return getStackClass2();
    }

    /**
     * Returns the class of the caller's caller's caller.
     *
     * @return the requested class, or {@code null}.
     */
    @UnsupportedAppUsage
    public static Class<?> getStackClass2() {
        // RoboVM note: This is native in Android
        Class<?>[] classes = VM.getStackClasses(2, 1);
        return classes != null && classes.length > 0 ? classes[0] : null;
    }

    /**
     * Creates an array of classes from the methods at the top of the stack.
     * We continue until we reach the bottom of the stack or exceed the
     * specified maximum depth.
     * <p>
     * The topmost stack frame (this method) and the one above that (the
     * caller) are excluded from the array.  Frames with java.lang.reflect
     * classes are skipped over.
     * <p>
     * The classes in the array are the defining classes of the methods.
     * <p>
     * This is similar to Harmony's VMStack.getClasses, except that this
     * implementation doesn't have a concept of "privileged" frames.
     *
     * @param maxDepth
     *      maximum number of classes to return, or -1 for all
     * @return an array with classes for the most-recent methods on the stack
     */
    public static Class<?>[] getClasses(int maxDepth) {
        // RoboVM note: This is native in Android
        // TODO: Skip over java.lang.reflect classes.
        return VM.getStackClasses(1, maxDepth);
    }

    private static final ClassLoader bootstrapLoader = Object.class.getClassLoader();
    private static final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

    public static ClassLoader getClosestUserClassLoader() {
        return getClosestUserClassLoader(bootstrapLoader, systemLoader);
    }

    /**
     * Returns the first ClassLoader on the call stack that isn't the
     * bootstrap class loader.
     */
    public static ClassLoader getClosestUserClassLoader(ClassLoader bootstrap,
                                                        ClassLoader system) {
        // RoboVM note: This is native in Android.
        Class<?>[] stackClasses = VMStack.getClasses(-1);
        for (Class<?> stackClass : stackClasses) {
            ClassLoader loader = stackClass.getClassLoader();
            if (loader != null && loader != bootstrap && loader != system) {
                return loader;
            }
        }
        return null;
    }

    /**
     * Retrieves the stack trace from the specified thread.
     *
     * @param t
     *      thread of interest
     * @return an array of stack trace elements, or null if the thread
     *      doesn't have a stack trace (e.g. because it exited)
     */
    @UnsupportedAppUsage
    public static StackTraceElement[] getThreadStackTrace(Thread t) {
        // RoboVM note: This is native in Android.
        return t.getStackTrace();
    }

// RoboVM Note: not used
//    /**
//     * Retrieves an annotated stack trace from the specified thread.
//     *
//     * @param t
//     *      thread of interest
//     * @return an array of annotated stack frames, or null if the thread
//     *      doesn't have a stack trace (e.g. because it exited)
//     */
//    @libcore.api.CorePlatformApi
//    @FastNative
//    native public static AnnotatedStackTraceElement[]
//            getAnnotatedThreadStackTrace(Thread t);

    /**
     * Retrieves a partial stack trace from the specified thread into
     * the provided array.
     *
     * @param t
     *      thread of interest
     * @param stackTraceElements
     *      preallocated array for use when only the top of stack is
     *      desired. Unused elements will be filled with null values.
     * @return the number of elements filled
     */
    @UnsupportedAppUsage
    public static int fillStackTraceElements(Thread t,
                                             StackTraceElement[] stackTraceElements) {
        // RoboVM note: This is native in Android.
        Arrays.fill(stackTraceElements, null);
        StackTraceElement[] st = t.getStackTrace();
        int n = Math.min(st.length, stackTraceElements.length);
        System.arraycopy(st, 0, stackTraceElements, 0, n);
        return n;
    }
}
