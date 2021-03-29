/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.util;

import dalvik.system.VMRuntime;
import sun.misc.Cleaner;

import java.lang.ref.Reference;

/**
 * A NativeAllocationRegistry is used to associate native allocations with
 * Java objects and register them with the runtime.
 * There are two primary benefits of registering native allocations associated
 * with Java objects:
 * <ol>
 *  <li>The runtime will account for the native allocations when scheduling
 *  garbage collection to run.</li>
 *  <li>The runtime will arrange for the native allocation to be automatically
 *  freed by a user-supplied function when the associated Java object becomes
 *  unreachable.</li>
 * </ol>
 * A separate NativeAllocationRegistry should be instantiated for each kind
 * of native allocation, where the kind of a native allocation consists of the
 * native function used to free the allocation and the estimated size of the
 * allocation. Once a NativeAllocationRegistry is instantiated, it can be
 * used to register any number of native allocations of that kind.
 * @hide
 */
@libcore.api.CorePlatformApi
public class NativeAllocationRegistry {

    private final ClassLoader classLoader;

    // Pointer to native deallocation function of type void f(void* freeFunction).
    private final long freeFunction;

    // The size of the registered native objects. This can be, and usually is, approximate.
    // The least significant bit is one iff the object was allocated primarily with system
    // malloc().
    // This field is examined by ahat and other tools. We chose this encoding of the "is_malloced"
    // information to (a) allow existing readers to continue to work with minimal confusion,
    // and (b) to avoid adding a field to NativeAllocationRegistry objects.
    private final long size;
    // Bit mask for "is_malloced" information.
    private static final long IS_MALLOCED = 0x1;

    /**
     * Return a NativeAllocationRegistry for native memory that is mostly
     * allocated by means other than the system memory allocator. For example,
     * the memory may be allocated directly with mmap.
     * @param classLoader  ClassLoader that was used to load the native
     *                     library defining freeFunction.
     *                     This ensures that the the native library isn't unloaded
     *                     before freeFunction is called.
     * @param freeFunction address of a native function of type
     *                     <code>void f(void* nativePtr)</code> used to free this
     *                     kind of native allocation
     * @param size         estimated size in bytes of the part of the described
     *                     native memory that is not allocated with system malloc.
     *                     Approximate values are acceptable.
     * @throws IllegalArgumentException If <code>size</code> is negative
     */
    @libcore.api.CorePlatformApi
    public static NativeAllocationRegistry createNonmalloced(
            ClassLoader classLoader, long freeFunction, long size) {
        return new NativeAllocationRegistry(classLoader, freeFunction, size, false);
    }

    /**
     * Return a NativeAllocationRegistry for native memory that is mostly
     * allocated by the system memory allocator.
     * For example, the memory may be allocated directly with new or malloc.
     * <p>
     * The native function should have the type:
     * <pre>
     *    void f(void* nativePtr);
     * </pre>
     * <p>
     * @param classLoader  ClassLoader that was used to load the native
     *                     library freeFunction belongs to.
     * @param freeFunction address of a native function of type
     *                     <code>void f(void* nativePtr)</code> used to free this
     *                     kind of native allocation
     * @param size         estimated size in bytes of the part of the described
     *                     native memory allocated with system malloc.
     *                     Approximate values are acceptable. For sizes less than
     *                     a few hundered KB, use the simplified overload below.
     * @throws IllegalArgumentException If <code>size</code> is negative
     */
    @libcore.api.CorePlatformApi
    public static NativeAllocationRegistry createMalloced(
            ClassLoader classLoader, long freeFunction, long size) {
        return new NativeAllocationRegistry(classLoader, freeFunction, size, true);
    }

    /**
     * Return a NativeAllocationRegistry for native memory that is mostly
     * allocated by the system memory allocator. This version is preferred
     * for smaller objects (typically less than a few hundred KB).
     * @param classLoader  ClassLoader that was used to load the native
     *                     library freeFunction belongs to.
     * @param freeFunction address of a native function of type
     *                     <code>void f(void* nativePtr)</code> used to free this
     *                     kind of native allocation
     */
    @libcore.api.CorePlatformApi
    public static NativeAllocationRegistry createMalloced(
            ClassLoader classLoader, long freeFunction) {
        return new NativeAllocationRegistry(classLoader, freeFunction, 0, true);
    }

    /**
     * Constructs a NativeAllocationRegistry for a particular kind of native
     * allocation.
     * <p>
     * The <code>size</code> should be an estimate of the total number of
     * native bytes this kind of native allocation takes up. Different
     * NativeAllocationRegistrys must be used to register native allocations
     * with different estimated sizes, even if they use the same
     * <code>freeFunction</code>. This is used to help inform the garbage
     * collector about the possible need for collection. Memory allocated with
     * native malloc is implicitly included, and ideally should not be included in this
     * argument.
     * <p>
     * @param classLoader  ClassLoader that was used to load the native
     *                     library freeFunction belongs to.
     * @param freeFunction address of a native function used to free this
     *                     kind of native allocation
     * @param size         estimated size in bytes of this kind of native
     *                     allocation. If mallocAllocation is false, then this
     *                     should ideally exclude memory allocated by system
     *                     malloc. However including it will simply double-count it,
     *                     typically resulting in slightly increased GC frequency.
     *                     If mallocAllocation is true, then this affects only the
     *                     frequency with which we sample the malloc heap, and debugging
     *                     tools. In this case a value of zero is commonly used to
     *                     indicate an unknown non-huge size.
     * @param mallocAllocation the native object is primarily allocated via malloc.
     */
    private NativeAllocationRegistry(ClassLoader classLoader, long freeFunction, long size,
            boolean mallocAllocation) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid native allocation size: " + size);
        }
        this.classLoader = classLoader;
        this.freeFunction = freeFunction;
        this.size = mallocAllocation ? (size | IS_MALLOCED) : (size & ~IS_MALLOCED);
    }

    /**
     * Constructs a NativeAllocationRegistry for a particular kind of native
     * allocation.
     * <p>
     * New code should use the preceding factory methods rather than calling this
     * constructor directly.
     * <p>
     * The <code>size</code> should be an estimate of the total number of
     * native bytes this kind of native allocation takes up excluding bytes allocated
     * with system malloc. Different
     * NativeAllocationRegistrys must be used to register native allocations
     * with different estimated sizes, even if they use the same
     * <code>freeFunction</code>. This is used to help inform the garbage
     * collector about the possible need for collection. Memory allocated with
     * native malloc is implicitly included, and ideally should not be included in this
     * argument.
     * <p>
     * @param classLoader  ClassLoader that was used to load the native
     *                     library freeFunction belongs to.
     * @param freeFunction address of a native function used to free this
     *                     kind of native allocation
     * @param size         estimated size in bytes of this kind of native
     *                     allocation, excluding memory allocated with system malloc.
     *                     A value of 0 indicates that the memory was allocated mainly
     *                     with malloc.
     *
     * @param mallocAllocation the native object is primarily allocated via malloc.
     */
    @libcore.api.CorePlatformApi
    public NativeAllocationRegistry(ClassLoader classLoader, long freeFunction, long size) {
        this(classLoader, freeFunction, size, size == 0);
    }

    /**
     * Registers a new native allocation and associated Java object with the
     * runtime.
     * This NativeAllocationRegistry's <code>freeFunction</code> will
     * automatically be called with <code>nativePtr</code> as its sole
     * argument when <code>referent</code> becomes unreachable. If you
     * maintain copies of <code>nativePtr</code> outside
     * <code>referent</code>, you must not access these after
     * <code>referent</code> becomes unreachable, because they may be dangling
     * pointers.
     * <p>
     * The returned Runnable can be used to free the native allocation before
     * <code>referent</code> becomes unreachable. The runnable will have no
     * effect if the native allocation has already been freed by the runtime
     * or by using the runnable.
     * <p>
     * WARNING: This unconditionally takes ownership, i.e. deallocation
     * responsibility of nativePtr. nativePtr will be DEALLOCATED IMMEDIATELY
     * if the registration attempt throws an exception (other than one reporting
     * a programming error).
     *
     * @param referent      Non-null java object to associate the native allocation with
     * @param nativePtr     Non-zero address of the native allocation
     * @return runnable to explicitly free native allocation
     * @throws IllegalArgumentException if either referent or nativePtr is null.
     * @throws OutOfMemoryError  if there is not enough space on the Java heap
     *                           in which to register the allocation. In this
     *                           case, <code>freeFunction</code> will be
     *                           called with <code>nativePtr</code> as its
     *                           argument before the OutOfMemoryError is
     *                           thrown.
     */
    @libcore.api.CorePlatformApi
    public Runnable registerNativeAllocation(Object referent, long nativePtr) {
        if (referent == null) {
            throw new IllegalArgumentException("referent is null");
        }
        if (nativePtr == 0) {
            throw new IllegalArgumentException("nativePtr is null");
        }

        CleanerThunk thunk;
        CleanerRunner result;
        try {
            thunk = new CleanerThunk();
            Cleaner cleaner = Cleaner.create(referent, thunk);
            result = new CleanerRunner(cleaner);
            registerNativeAllocation(this.size);
        } catch (VirtualMachineError vme /* probably OutOfMemoryError */) {
            applyFreeFunction(freeFunction, nativePtr);
            throw vme;
        } // Other exceptions are impossible.
        // Enable the cleaner only after we can no longer throw anything, including OOME.
        thunk.setNativePtr(nativePtr);
        // Ensure that cleaner doesn't get invoked before we enable it.
        Reference.reachabilityFence(referent);
        return result;
    }

    private class CleanerThunk implements Runnable {
        private long nativePtr;

        public CleanerThunk() {
            this.nativePtr = 0;
        }

        public void run() {
            if (nativePtr != 0) {
                applyFreeFunction(freeFunction, nativePtr);
                registerNativeFree(size);
            }
        }

        public void setNativePtr(long nativePtr) {
            this.nativePtr = nativePtr;
        }
    }

    private static class CleanerRunner implements Runnable {
        private final Cleaner cleaner;

        public CleanerRunner(Cleaner cleaner) {
            this.cleaner = cleaner;
        }

        public void run() {
            cleaner.clean();
        }
    }

    // Inform the garbage collector of the allocation. We do this differently for
    // malloc-based allocations.
    private static void registerNativeAllocation(long size) {
// RoboVM Note: not used in RoboVM
//        VMRuntime runtime = VMRuntime.getRuntime();
//        if ((size & IS_MALLOCED) != 0) {
//            final long notifyImmediateThreshold = 300000;
//            if (size >= notifyImmediateThreshold) {
//                runtime.notifyNativeAllocationsInternal();
//            } else {
//                runtime.notifyNativeAllocation();
//            }
//        } else {
//            runtime.registerNativeAllocation(size);
//        }
    }

    // Inform the garbage collector of deallocation, if appropriate.
    private static void registerNativeFree(long size) {
        if ((size & IS_MALLOCED) == 0) {
            VMRuntime.getRuntime().registerNativeFree(size);
        }
    }

    /**
     * Calls <code>freeFunction</code>(<code>nativePtr</code>).
     * Provided as a convenience in the case where you wish to manually free a
     * native allocation using a <code>freeFunction</code> without using a
     * NativeAllocationRegistry.
     */
    @libcore.api.CorePlatformApi
    public static native void applyFreeFunction(long freeFunction, long nativePtr);
}

