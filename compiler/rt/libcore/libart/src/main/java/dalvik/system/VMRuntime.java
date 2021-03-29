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
import dalvik.annotation.optimization.FastNative;
import libcore.util.EmptyArray;
import org.robovm.rt.VM;

import java.lang.ref.FinalizerReference;
import java.lang.reflect.Array;

/**
 * Provides an interface to VM-global, Dalvik-specific features.
 * An application cannot create its own Runtime instance, and must obtain
 * one from the getRuntime method.
 *
 * RoboVM Note: all Android specific and not required API was removed
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public final class VMRuntime {

    /**
     * Holds the VMRuntime singleton.
     */
    private static final VMRuntime THE_ONE = new VMRuntime();

    /**
     * Magic version number for a current development build, which has not
     * yet turned into an official release. This number must be larger than
     * any released version in {@code android.os.Build.VERSION_CODES}.
     * @hide
     */
    @libcore.api.CorePlatformApi
    public static final int SDK_VERSION_CUR_DEVELOPMENT = 10000;

    // RoboVM Note: final static on RoboVM
    private static final int targetSdkVersion = SDK_VERSION_CUR_DEVELOPMENT;


    /**
     * Prevents this class from being instantiated.
     */
    private VMRuntime() {
    }

    /**
     * Returns the object that represents the VM instance's Dalvik-specific
     * runtime environment.
     *
     * @return the runtime object
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static VMRuntime getRuntime() {
        return THE_ONE;
    }

    /**
     * Returns a copy of the VM's command-line property settings.
     * These are in the form "name=value" rather than "-Dname=value".
     */
    public String[] properties() {
        // RoboVM note: This is native Android.
        // TODO: Support command-line supplied properties?
        return EmptyArray.STRING;
    }

    public String bootClassPath() {
        // RoboVM note: This is native Android.
        return VM.bootClassPath();
    }

    /**
     * Returns the VM's class path.
     */
    public String classPath() {
        // RoboVM note: This is native Android.
        return VM.classPath();
    }

    /**
     * Returns the VM's version.
     */
    public String vmVersion() {
        // RoboVM note: This is native Android.
        return VM.vmVersion();
    }

    /**
     * Returns whether the VM is running in 64-bit mode.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @FastNative
    public static native boolean is64Bit();

    /**
     * Gets the target SDK version. See {@link #setTargetSdkVersion} for
     * special values.
     */
    @libcore.api.CorePlatformApi
    public synchronized int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    /**
     * Returns an array allocated in an area of the Java heap where it will never be moved.
     * This is used to implement native allocations on the Java heap, such as DirectByteBuffers
     * and Bitmaps.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @FastNative
    public Object newNonMovableArray(Class<?> componentType, int length) {
        // RoboVM note: This is native in Android. In RoboVM the GC never moves around objects on
        // the heap so we can use Array.newInstance().
        return Array.newInstance(componentType, length);
    }

    /**
     * Returns the address of array[0]. This differs from using JNI in that JNI might lie and
     * give you the address of a copy of the array when in forcecopy mode.
     */
    public long addressOf(Object array) {
        // RoboVM note: This is native in Android.
        return VM.getArrayValuesAddress(array);
    }

    /**
     * Returns true if either a Java debugger or native debugger is active.
     */
    public boolean isDebuggerActive() {
        // RoboVM note: This is native in Android. Called by the FinalizerWatchdogDaemon thread to check whether a
        // debugger is attached. If there is a debugger the  FinalizerWatchdogDaemon thread will not exit the VM if a
        // finalizer takes more time than the allowed MAX_FINALIZE_MILLIS.
        return false;
    }

    /**
     * Registers a native allocation so that the heap knows about it and performs GC as required.
     * If the number of native allocated bytes exceeds the native allocation watermark, the
     * function requests a concurrent GC. If the native bytes allocated exceeds a second higher
     * watermark, it is determined that the application is registering native allocations at an
     * unusually high rate and a GC is performed inside of the function to prevent memory usage
     * from excessively increasing. Memory allocated via system malloc() should not be included
     * in this count. The argument must be the same as that later passed to registerNativeFree(),
     * but may otherwise be approximate.
     */
    @UnsupportedAppUsage
    public void registerNativeAllocation(long bytes) {
        // RoboVM note: This is native in Android. In RoboVM this is a nop.
    }

    /**
     * Backward compatibility version of registerNativeAllocation. We used to pass an int instead
     * of a long. The RenderScript support library looks it up via reflection.
     * @deprecated Use long argument instead.
     */
    @UnsupportedAppUsage
    @Deprecated
    @libcore.api.CorePlatformApi
    public void registerNativeAllocation(int bytes) {
        registerNativeAllocation((long) bytes);
    }

    /**
     * Registers a native free by reducing the number of native bytes accounted for.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public void registerNativeFree(long bytes) {
        // RoboVM note: This is native in Android. In RoboVM this is a nop.
    }

    /**
     * Backward compatibility version of registerNativeFree.
     * @deprecated Use long argument instead.
     */
    @UnsupportedAppUsage
    @Deprecated
    @libcore.api.CorePlatformApi
    public void registerNativeFree(int bytes) {
        registerNativeFree((long) bytes);
    }

    /**
     * Wait for objects to be finalized.
     *
     * If finalization takes longer than timeout, then the function returns before all objects are
     * finalized.
     *
     * @param timeout
     *            timeout in nanoseconds of the maximum time to wait until all pending finalizers
     *            are run. If timeout is 0, then there is no timeout. Note that the timeout does
     *            not stop the finalization process, it merely stops the wait.
     *
     * @see #Runtime.runFinalization()
     * @see #wait(long,int)
     */
    @UnsupportedAppUsage
    public static void runFinalization(long timeout) {
        try {
            FinalizerReference.finalizeAllEnqueued(timeout);
        } catch (InterruptedException e) {
            // Interrupt the current thread without actually throwing the InterruptionException
            // for the caller.
            Thread.currentThread().interrupt();
        }
    }
}
