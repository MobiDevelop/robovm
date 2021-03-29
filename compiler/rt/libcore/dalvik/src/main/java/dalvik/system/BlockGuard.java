/*
 * Copyright (C) 2010 The Android Open Source Project
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

import libcore.util.NonNull;

import dalvik.annotation.compat.UnsupportedAppUsage;
import java.util.Objects;

/**
 * Interface that enables {@code StrictMode} to install callbacks to implement
 * its policy detection and penalty behavior in {@code libcore} code.
 * <p>
 * The framework separately defines {@code StrictMode.ThreadPolicy} and
 * {@code StrictMode.VmPolicy}, so we mirror that separation here; the former is
 * designed for per-thread policies, and the latter for process-wide policies.
 * <p>
 * Note that this is all best-effort to catch most accidental mistakes and isn't
 * intended to be a perfect mechanism, nor provide any sort of security.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
@libcore.api.IntraCoreApi
public final class BlockGuard {

    // TODO: refactor class name to something more generic, since its scope is
    // growing beyond just blocking/logging.

    /**
     * Per-thread interface used to implement {@code StrictMode.ThreadPolicy}.
     *
     * @hide
     */
    @libcore.api.CorePlatformApi
    @libcore.api.IntraCoreApi
    public interface Policy {
        /**
         * Called on disk writes.
         */
        @libcore.api.CorePlatformApi
        void onWriteToDisk();

        /**
         * Called on disk reads.
         */
        @UnsupportedAppUsage
        @libcore.api.CorePlatformApi
        void onReadFromDisk();

        /**
         * Called on network operations.
         */
        @UnsupportedAppUsage
        @libcore.api.IntraCoreApi
        void onNetwork();

        /**
         * Called on unbuffered input/ouput operations.
         */
        @libcore.api.CorePlatformApi
        void onUnbufferedIO();

        /**
         * Called on explicit GC request, i.e. Runtime.gc().
         */
        void onExplicitGc();

        /**
         * Returns the policy bitmask, for shipping over Binder calls
         * to remote threads/processes and reinstantiating the policy
         * there.  The bits in the mask are from the DISALLOW_* and
         * PENALTY_* constants.
         */
        @libcore.api.CorePlatformApi
        int getPolicyMask();
    }

    /**
     * Per-process interface used to implement {@code StrictMode.VmPolicy}.
     * @hide
     */
    @libcore.api.CorePlatformApi
    public interface VmPolicy {
        /**
         * Called by core libraries code when the given path is accessed. This
         * allows an implementation to alert developers to unexpected path
         * access, such as trying to access encrypted files before the
         * encryption key has been installed.
         * <p>
         * This only needs to be called once when a path is first accessed by
         * the process; it doesn't need to be invoked for each subsequent
         * read/write. (In contrast, we always need to call the per-thread
         * policy for every read/write, since ownership of an open file can move
         * between threads.)
         * <p>
         * Note that this is all best-effort to catch most accidental mistakes
         * and isn't intended to be a perfect mechanism, nor provide any sort of
         * security.
         *
         * @param path The path in the local file system that is being accessed
         *            for reading or writing.
         */
        @libcore.api.CorePlatformApi
        void onPathAccess(String path);
    }

    /**
     * @deprecated no longer actively used, but kept intact for greylist.
     */
    @Deprecated
    public static class BlockGuardPolicyException extends RuntimeException {
        // bitmask of DISALLOW_*, PENALTY_*, etc flags
        @UnsupportedAppUsage
        private final int mPolicyState;
        @UnsupportedAppUsage
        private final int mPolicyViolated;
        @UnsupportedAppUsage
        private final String mMessage;   // may be null

        public BlockGuardPolicyException(int policyState, int policyViolated) {
            this(policyState, policyViolated, null);
        }

        @UnsupportedAppUsage
        public BlockGuardPolicyException(int policyState, int policyViolated, String message) {
            mPolicyState = policyState;
            mPolicyViolated = policyViolated;
            mMessage = message;
            fillInStackTrace();
        }

        public int getPolicy() {
            return mPolicyState;
        }

        public int getPolicyViolation() {
            return mPolicyViolated;
        }

        public String getMessage() {
            // Note: do not change this format casually.  It's
            // somewhat unfortunately Parceled and passed around
            // Binder calls and parsed back into an Exception by
            // Android's StrictMode.  This was the least invasive
            // option and avoided a gross mix of Java Serialization
            // combined with Parcels.
            return "policy=" + mPolicyState + " violation=" + mPolicyViolated +
                    (mMessage == null ? "" : (" msg=" + mMessage));
        }
    }

    /**
     * The default, permissive per-thread policy.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static final Policy LAX_POLICY = new Policy() {
        @Override public String toString() { return "LAX_POLICY"; }
        @Override public void onWriteToDisk() {}
        @Override public void onReadFromDisk() {}
        @Override public void onNetwork() {}
        @Override public void onUnbufferedIO() {}
        @Override public void onExplicitGc() {}

        @Override
        public int getPolicyMask() {
            return 0;
        }
    };

    /**
     * The default, permissive per-process policy.
     */
    @libcore.api.CorePlatformApi
    public static final VmPolicy LAX_VM_POLICY = new VmPolicy() {
        @Override public String toString() { return "LAX_VM_POLICY"; }
        @Override public void onPathAccess(String path) {}
    };

    @UnsupportedAppUsage
    private static ThreadLocal<Policy> threadPolicy = new ThreadLocal<Policy>() {
        @Override protected Policy initialValue() {
            return LAX_POLICY;
        }
    };

    private static volatile VmPolicy vmPolicy = LAX_VM_POLICY;

    /**
     * Get the per-thread policy for the current thread.
     *
     * @return the current thread's policy. Will return the {@link #LAX_POLICY}
     *         instance if nothing else is set.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    @libcore.api.IntraCoreApi
    public static @NonNull Policy getThreadPolicy() {
        return threadPolicy.get();
    }

    /**
     * Sets the per-thread policy for the current thread.
     * <p>
     * This should only be called by {@code StrictMode}, since there can only be
     * one policy active at any given time.
     *
     * @param policy policy to set. Use the public {@link #LAX_POLICY} if you
     *            want to unset the active policy.
     */
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public static void setThreadPolicy(@NonNull Policy policy) {
        threadPolicy.set(Objects.requireNonNull(policy));
    }

    /**
     * Get the per-process policy for the current process.
     *
     * @return the current process's policy. Will return the
     *         {@link #LAX_VM_POLICY} instance if nothing else is set.
     */
    @libcore.api.CorePlatformApi
    public static @NonNull VmPolicy getVmPolicy() {
        return vmPolicy;
    }

    /**
     * Set the per-process policy for the current process.
     * <p>
     * This should only be called by {@code StrictMode}, since there can only be
     * one policy active at any given time.
     *
     * @param policy policy to set. Use the public {@link #LAX_VM_POLICY} if you
     *            want to unset the active policy.
     */
    @libcore.api.CorePlatformApi
    public static void setVmPolicy(@NonNull VmPolicy policy) {
        vmPolicy = Objects.requireNonNull(policy);
    }

    private BlockGuard() {}
}
