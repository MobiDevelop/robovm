/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.security.jca;

import dalvik.system.VMRuntime;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Collection of methods to get and set provider list. Also includes
 * special code for the provider list during JAR verification.
 *
 * @author  Andreas Sterbenz
 * @since   1.5
 */
public class Providers {

    private static final ThreadLocal<ProviderList> threadLists =
        new InheritableThreadLocal<>();

    // number of threads currently using thread-local provider lists
    // tracked to allow an optimization if == 0
    private static volatile int threadListsUsed;

    // current system-wide provider list
    // Note volatile immutable object, so no synchronization needed.
    private static volatile ProviderList providerList;

    // Android-added: Keep reference to system-created Bouncy Castle provider
    // See comments near deprecation methods at the bottom of this file.
    private static volatile Provider SYSTEM_BOUNCY_CASTLE_PROVIDER;

    static {
        // set providerList to empty list first in case initialization somehow
        // triggers a getInstance() call (although that should not happen)
        providerList = ProviderList.EMPTY;
        providerList = ProviderList.fromSecurityProperties();

        // BEGIN Android-added: Initialize all providers and assert that this succeeds.
        // removeInvalid is specified to try initializing all configured providers
        // and removing those that aren't instantiable. This has the side effect
        // of eagerly initializing all providers.
        final int numConfiguredProviders = providerList.size();
        providerList = providerList.removeInvalid();
        if (numConfiguredProviders != providerList.size()) {
            throw new AssertionError("Unable to configure default providers");
        }
        // END Android-added: Initialize all providers and assert that this succeeds.
        // Android-added: Set BC provider instance
        SYSTEM_BOUNCY_CASTLE_PROVIDER = providerList.getProvider("BC");
    }

    private Providers() {
        // empty
    }

    // we need special handling to resolve circularities when loading
    // signed JAR files during startup. The code below is part of that.

    // Basically, before we load data from a signed JAR file, we parse
    // the PKCS#7 file and verify the signature. We need a
    // CertificateFactory, Signatures, etc. to do that. We have to make
    // sure that we do not try to load the implementation from the JAR
    // file we are just verifying.
    //
    // To avoid that, we use different provider settings during JAR
    // verification.  However, we do not want those provider settings to
    // interfere with other parts of the system. Therefore, we make them local
    // to the Thread executing the JAR verification code.
    //
    // The code here is used by sun.security.util.SignatureFileVerifier.
    // See there for details.

    private static final String BACKUP_PROVIDER_CLASSNAME =
        "sun.security.provider.VerificationProvider";

    // Hardcoded classnames of providers to use for JAR verification.
    // MUST NOT be on the bootclasspath and not in signed JAR files.
    private static final String[] jarVerificationProviders = {
        // BEGIN Android-changed: Use Conscrypt and BC, not the sun.security providers.
        /*
        "sun.security.provider.Sun",
        "sun.security.rsa.SunRsaSign",
        // Note: SunEC *is* in a signed JAR file, but it's not signed
        // by EC itself. So it's still safe to be listed here.
        "sun.security.ec.SunEC",
        */
        "com.android.org.conscrypt.OpenSSLProvider",
        "com.android.org.bouncycastle.jce.provider.BouncyCastleProvider",
        "com.android.org.conscrypt.JSSEProvider",
        // END Android-changed: Use Conscrypt and BC, not the sun.security providers.
        BACKUP_PROVIDER_CLASSNAME,
    };

    // Return to Sun provider or its backup.
    // This method should only be called by
    // sun.security.util.ManifestEntryVerifier and java.security.SecureRandom.
    public static Provider getSunProvider() {
        try {
            Class<?> clazz = Class.forName(jarVerificationProviders[0]);
            return (Provider)clazz.newInstance();
        } catch (Exception e) {
            try {
                Class<?> clazz = Class.forName(BACKUP_PROVIDER_CLASSNAME);
                return (Provider)clazz.newInstance();
            } catch (Exception ee) {
                throw new RuntimeException("Sun provider not found", e);
            }
        }
    }

    /**
     * Start JAR verification. This sets a special provider list for
     * the current thread. You MUST save the return value from this
     * method and you MUST call stopJarVerification() with that object
     * once you are done.
     */
    public static Object startJarVerification() {
        ProviderList currentList = getProviderList();
        ProviderList jarList = currentList.getJarList(jarVerificationProviders);
        // return the old thread-local provider list, usually null
        return beginThreadProviderList(jarList);
    }

    /**
     * Stop JAR verification. Call once you have completed JAR verification.
     */
    public static void stopJarVerification(Object obj) {
        // restore old thread-local provider list
        endThreadProviderList((ProviderList)obj);
    }

    /**
     * Return the current ProviderList. If the thread-local list is set,
     * it is returned. Otherwise, the system wide list is returned.
     */
    public static ProviderList getProviderList() {
        ProviderList list = getThreadProviderList();
        if (list == null) {
            list = getSystemProviderList();
        }
        return list;
    }

    /**
     * Set the current ProviderList. Affects the thread-local list if set,
     * otherwise the system wide list.
     */
    public static void setProviderList(ProviderList newList) {
        if (getThreadProviderList() == null) {
            setSystemProviderList(newList);
        } else {
            changeThreadProviderList(newList);
        }
    }

    /**
     * Get the full provider list with invalid providers (those that
     * could not be loaded) removed. This is the list we need to
     * present to applications.
     */
    public static ProviderList getFullProviderList() {
        ProviderList list;
        synchronized (Providers.class) {
            list = getThreadProviderList();
            if (list != null) {
                ProviderList newList = list.removeInvalid();
                if (newList != list) {
                    changeThreadProviderList(newList);
                    list = newList;
                }
                return list;
            }
        }
        list = getSystemProviderList();
        ProviderList newList = list.removeInvalid();
        if (newList != list) {
            setSystemProviderList(newList);
            list = newList;
        }
        return list;
    }

    private static ProviderList getSystemProviderList() {
        return providerList;
    }

    private static void setSystemProviderList(ProviderList list) {
        providerList = list;
    }

    public static ProviderList getThreadProviderList() {
        // avoid accessing the threadlocal if none are currently in use
        // (first use of ThreadLocal.get() for a Thread allocates a Map)
        if (threadListsUsed == 0) {
            return null;
        }
        return threadLists.get();
    }

    // Change the thread local provider list. Use only if the current thread
    // is already using a thread local list and you want to change it in place.
    // In other cases, use the begin/endThreadProviderList() methods.
    private static void changeThreadProviderList(ProviderList list) {
        threadLists.set(list);
    }

    /**
     * Methods to manipulate the thread local provider list. It is for use by
     * JAR verification (see above) and the SunJSSE FIPS mode only.
     *
     * It should be used as follows:
     *
     *   ProviderList list = ...;
     *   ProviderList oldList = Providers.beginThreadProviderList(list);
     *   try {
     *     // code that needs thread local provider list
     *   } finally {
     *     Providers.endThreadProviderList(oldList);
     *   }
     *
     */

    public static synchronized ProviderList beginThreadProviderList(ProviderList list) {
        if (ProviderList.debug != null) {
            ProviderList.debug.println("ThreadLocal providers: " + list);
        }
        ProviderList oldList = threadLists.get();
        threadListsUsed++;
        threadLists.set(list);
        return oldList;
    }

    public static synchronized void endThreadProviderList(ProviderList list) {
        if (list == null) {
            if (ProviderList.debug != null) {
                ProviderList.debug.println("Disabling ThreadLocal providers");
            }
            threadLists.remove();
        } else {
            if (ProviderList.debug != null) {
                ProviderList.debug.println
                    ("Restoring previous ThreadLocal providers: " + list);
            }
            threadLists.set(list);
        }
        threadListsUsed--;
    }

    // BEGIN Android-added: Check for requests of deprecated Bouncy Castle algorithms.
    // Beginning in Android P, Bouncy Castle versions of algorithms available through
    // Conscrypt are deprecated.  We will no longer supply them to applications
    // with a target API level of P or later, and will print a warning for applications
    // with a target API level before P.
    //
    // We only care about the system-provided Bouncy Castle provider; applications are allowed to
    // install their own copy of Bouncy Castle if they want to continue using those implementations.

    /**
     * Maximum target API level for which we will provide the deprecated Bouncy Castle algorithms.
     *
     * Only exists for testing and shouldn't be changed.
     *
     * @hide
     */
    public static final int DEFAULT_MAXIMUM_ALLOWABLE_TARGET_API_LEVEL_FOR_BC_DEPRECATION = 27;

    private static int maximumAllowableApiLevelForBcDeprecation =
            DEFAULT_MAXIMUM_ALLOWABLE_TARGET_API_LEVEL_FOR_BC_DEPRECATION;

    /**
     * Sets the target API level for BC deprecation, only for use in tests.
     *
     * @hide
     */
    public static void setMaximumAllowableApiLevelForBcDeprecation(int targetApiLevel) {
        maximumAllowableApiLevelForBcDeprecation = targetApiLevel;
    }

    /**
     * Returns the target API level for BC deprecation, only for use in tests.
     *
     * @hide
     */
    public static int getMaximumAllowableApiLevelForBcDeprecation() {
        return maximumAllowableApiLevelForBcDeprecation;
    }

    /**
     * Checks if the installed provider with the given name is the system-installed Bouncy
     * Castle provider.  If so, throws {@code NoSuchAlgorithmException} if the algorithm
     * being requested is deprecated and the application targets a late-enough API level.
     *
     * @hide
     */
    public static synchronized void checkBouncyCastleDeprecation(String provider,
            String service, String algorithm) throws NoSuchAlgorithmException {
        // Applications may install their own BC provider, only the algorithms from the system
        // provider are deprecated.
        if ("BC".equals(provider)
                && providerList.getProvider(provider) == SYSTEM_BOUNCY_CASTLE_PROVIDER) {
            checkBouncyCastleDeprecation(service, algorithm);
        }
    }

    /**
     * Checks if the given provider is the system-installed Bouncy Castle provider.  If so,
     * throws {@code NoSuchAlgorithmException} if the algorithm being requested is deprecated
     * and the application targets a late-enough API level.
     *
     * @hide
     */
    public static synchronized void checkBouncyCastleDeprecation(Provider provider,
            String service, String algorithm) throws NoSuchAlgorithmException {
        // Applications may install their own BC provider, only the algorithms from the system
        // provider are deprecated.
        if (provider == SYSTEM_BOUNCY_CASTLE_PROVIDER) {
            checkBouncyCastleDeprecation(service, algorithm);
        }
    }

    // The set of algorithms that are deprecated.  This list is created using
    // libcore/tools/crypto/src/java/libcore/java/security/ProviderOverlap.java, with
    // additional Ciphers added manually (see comment below).
    private static final Set<String> DEPRECATED_ALGORITHMS = new HashSet<String>();
    static {
        DEPRECATED_ALGORITHMS.addAll(Arrays.asList(
                "ALGORITHMPARAMETERS.1.2.840.113549.3.7",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.2",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.22",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.26",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.42",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.46",
                "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.6",
                "ALGORITHMPARAMETERS.AES",
                "ALGORITHMPARAMETERS.DESEDE",
                "ALGORITHMPARAMETERS.EC",
                "ALGORITHMPARAMETERS.GCM",
                "ALGORITHMPARAMETERS.OAEP",
                "ALGORITHMPARAMETERS.TDEA",
                "CERTIFICATEFACTORY.X.509",
                "CERTIFICATEFACTORY.X509",
                // List of Ciphers produced by ProviderOverlap:
                "CIPHER.1.2.840.113549.3.4",
                "CIPHER.2.16.840.1.101.3.4.1.26",
                "CIPHER.2.16.840.1.101.3.4.1.46",
                "CIPHER.2.16.840.1.101.3.4.1.6",
                "CIPHER.AES/GCM/NOPADDING",
                "CIPHER.ARC4",
                "CIPHER.ARCFOUR",
                "CIPHER.OID.1.2.840.113549.3.4",
                "CIPHER.RC4",
                // End of Ciphers produced by ProviderOverlap
                // Additional ciphers transformations that will resolve to the same things as
                // the automatically-produced overlap due to the Cipher transformation rules.
                // These have been added manually.
                "CIPHER.ARC4/ECB/NOPADDING",
                "CIPHER.ARC4/NONE/NOPADDING",
                "CIPHER.ARCFOUR/ECB/NOPADDING",
                "CIPHER.ARCFOUR/NONE/NOPADDING",
                "CIPHER.RC4/ECB/NOPADDING",
                "CIPHER.RC4/NONE/NOPADDING",
                // End of additional Ciphers
                "KEYAGREEMENT.ECDH",
                "KEYFACTORY.1.2.840.10045.2.1",
                "KEYFACTORY.1.2.840.113549.1.1.1",
                "KEYFACTORY.1.2.840.113549.1.1.7",
                "KEYFACTORY.1.3.133.16.840.63.0.2",
                "KEYFACTORY.2.5.8.1.1",
                "KEYFACTORY.EC",
                "KEYFACTORY.RSA",
                "KEYGENERATOR.1.2.840.113549.2.10",
                "KEYGENERATOR.1.2.840.113549.2.11",
                "KEYGENERATOR.1.2.840.113549.2.7",
                "KEYGENERATOR.1.2.840.113549.2.8",
                "KEYGENERATOR.1.2.840.113549.2.9",
                "KEYGENERATOR.1.3.6.1.5.5.8.1.1",
                "KEYGENERATOR.1.3.6.1.5.5.8.1.2",
                "KEYGENERATOR.2.16.840.1.101.3.4.2.1",
                "KEYGENERATOR.AES",
                "KEYGENERATOR.DESEDE",
                "KEYGENERATOR.HMAC-MD5",
                "KEYGENERATOR.HMAC-SHA1",
                "KEYGENERATOR.HMAC-SHA224",
                "KEYGENERATOR.HMAC-SHA256",
                "KEYGENERATOR.HMAC-SHA384",
                "KEYGENERATOR.HMAC-SHA512",
                "KEYGENERATOR.HMAC/MD5",
                "KEYGENERATOR.HMAC/SHA1",
                "KEYGENERATOR.HMAC/SHA224",
                "KEYGENERATOR.HMAC/SHA256",
                "KEYGENERATOR.HMAC/SHA384",
                "KEYGENERATOR.HMAC/SHA512",
                "KEYGENERATOR.HMACMD5",
                "KEYGENERATOR.HMACSHA1",
                "KEYGENERATOR.HMACSHA224",
                "KEYGENERATOR.HMACSHA256",
                "KEYGENERATOR.HMACSHA384",
                "KEYGENERATOR.HMACSHA512",
                "KEYGENERATOR.TDEA",
                "KEYPAIRGENERATOR.1.2.840.10045.2.1",
                "KEYPAIRGENERATOR.1.2.840.113549.1.1.1",
                "KEYPAIRGENERATOR.1.2.840.113549.1.1.7",
                "KEYPAIRGENERATOR.1.3.133.16.840.63.0.2",
                "KEYPAIRGENERATOR.2.5.8.1.1",
                "KEYPAIRGENERATOR.EC",
                "KEYPAIRGENERATOR.RSA",
                "MAC.1.2.840.113549.2.10",
                "MAC.1.2.840.113549.2.11",
                "MAC.1.2.840.113549.2.7",
                "MAC.1.2.840.113549.2.8",
                "MAC.1.2.840.113549.2.9",
                "MAC.1.3.6.1.5.5.8.1.1",
                "MAC.1.3.6.1.5.5.8.1.2",
                "MAC.2.16.840.1.101.3.4.2.1",
                "MAC.HMAC-MD5",
                "MAC.HMAC-SHA1",
                "MAC.HMAC-SHA224",
                "MAC.HMAC-SHA256",
                "MAC.HMAC-SHA384",
                "MAC.HMAC-SHA512",
                "MAC.HMAC/MD5",
                "MAC.HMAC/SHA1",
                "MAC.HMAC/SHA224",
                "MAC.HMAC/SHA256",
                "MAC.HMAC/SHA384",
                "MAC.HMAC/SHA512",
                "MAC.HMACMD5",
                "MAC.HMACSHA1",
                "MAC.HMACSHA224",
                "MAC.HMACSHA256",
                "MAC.HMACSHA384",
                "MAC.HMACSHA512",
                "MAC.PBEWITHHMACSHA224",
                "MAC.PBEWITHHMACSHA256",
                "MAC.PBEWITHHMACSHA384",
                "MAC.PBEWITHHMACSHA512",
                "MESSAGEDIGEST.1.2.840.113549.2.5",
                "MESSAGEDIGEST.1.3.14.3.2.26",
                "MESSAGEDIGEST.2.16.840.1.101.3.4.2.1",
                "MESSAGEDIGEST.2.16.840.1.101.3.4.2.2",
                "MESSAGEDIGEST.2.16.840.1.101.3.4.2.3",
                "MESSAGEDIGEST.2.16.840.1.101.3.4.2.4",
                "MESSAGEDIGEST.MD5",
                "MESSAGEDIGEST.SHA",
                "MESSAGEDIGEST.SHA-1",
                "MESSAGEDIGEST.SHA-224",
                "MESSAGEDIGEST.SHA-256",
                "MESSAGEDIGEST.SHA-384",
                "MESSAGEDIGEST.SHA-512",
                "MESSAGEDIGEST.SHA1",
                "MESSAGEDIGEST.SHA224",
                "MESSAGEDIGEST.SHA256",
                "MESSAGEDIGEST.SHA384",
                "MESSAGEDIGEST.SHA512",
                "SECRETKEYFACTORY.DESEDE",
                "SECRETKEYFACTORY.TDEA",
                "SIGNATURE.1.2.840.10045.4.1",
                "SIGNATURE.1.2.840.10045.4.3.1",
                "SIGNATURE.1.2.840.10045.4.3.2",
                "SIGNATURE.1.2.840.10045.4.3.3",
                "SIGNATURE.1.2.840.10045.4.3.4",
                "SIGNATURE.1.2.840.113549.1.1.11",
                "SIGNATURE.1.2.840.113549.1.1.12",
                "SIGNATURE.1.2.840.113549.1.1.13",
                "SIGNATURE.1.2.840.113549.1.1.14",
                "SIGNATURE.1.2.840.113549.1.1.4",
                "SIGNATURE.1.2.840.113549.1.1.5",
                "SIGNATURE.1.3.14.3.2.29",
                "SIGNATURE.ECDSA",
                "SIGNATURE.ECDSAWITHSHA1",
                "SIGNATURE.MD5/RSA",
                "SIGNATURE.MD5WITHRSA",
                "SIGNATURE.MD5WITHRSAENCRYPTION",
                "SIGNATURE.NONEWITHECDSA",
                "SIGNATURE.OID.1.2.840.10045.4.3.1",
                "SIGNATURE.OID.1.2.840.10045.4.3.2",
                "SIGNATURE.OID.1.2.840.10045.4.3.3",
                "SIGNATURE.OID.1.2.840.10045.4.3.4",
                "SIGNATURE.OID.1.2.840.113549.1.1.11",
                "SIGNATURE.OID.1.2.840.113549.1.1.12",
                "SIGNATURE.OID.1.2.840.113549.1.1.13",
                "SIGNATURE.OID.1.2.840.113549.1.1.14",
                "SIGNATURE.OID.1.2.840.113549.1.1.4",
                "SIGNATURE.OID.1.2.840.113549.1.1.5",
                "SIGNATURE.OID.1.3.14.3.2.29",
                "SIGNATURE.SHA1/RSA",
                "SIGNATURE.SHA1WITHECDSA",
                "SIGNATURE.SHA1WITHRSA",
                "SIGNATURE.SHA1WITHRSAENCRYPTION",
                "SIGNATURE.SHA224/ECDSA",
                "SIGNATURE.SHA224/RSA",
                "SIGNATURE.SHA224WITHECDSA",
                "SIGNATURE.SHA224WITHRSA",
                "SIGNATURE.SHA224WITHRSAENCRYPTION",
                "SIGNATURE.SHA256/ECDSA",
                "SIGNATURE.SHA256/RSA",
                "SIGNATURE.SHA256WITHECDSA",
                "SIGNATURE.SHA256WITHRSA",
                "SIGNATURE.SHA256WITHRSAENCRYPTION",
                "SIGNATURE.SHA384/ECDSA",
                "SIGNATURE.SHA384/RSA",
                "SIGNATURE.SHA384WITHECDSA",
                "SIGNATURE.SHA384WITHRSA",
                "SIGNATURE.SHA384WITHRSAENCRYPTION",
                "SIGNATURE.SHA512/ECDSA",
                "SIGNATURE.SHA512/RSA",
                "SIGNATURE.SHA512WITHECDSA",
                "SIGNATURE.SHA512WITHRSA",
                "SIGNATURE.SHA512WITHRSAENCRYPTION"
        ));
    }

    /**
     * Throws an exception or logs a warning if the supplied service and algorithm identify
     * a deprecated algorithm from Bouncy Castle, depending on the application's target API level.
     * Only called if we have already determined that the request is for the system Bouncy Castle
     * provider.
     */
    private static void checkBouncyCastleDeprecation(String service, String algorithm)
            throws NoSuchAlgorithmException {
        String key = service + "." + algorithm;
        if (DEPRECATED_ALGORITHMS.contains(key.toUpperCase(Locale.US))) {
            if (VMRuntime.getRuntime().getTargetSdkVersion()
                    <= maximumAllowableApiLevelForBcDeprecation) {
                // This application is allowed to access these functions, only print a warning
                System.logE(" ******** DEPRECATED FUNCTIONALITY ********");
                System.logE(" * The implementation of the " + key + " algorithm from");
                System.logE(" * the BC provider is deprecated in this version of Android.");
                System.logE(" * It will be removed in a future version of Android and your");
                System.logE(" * application will no longer be able to request it.  Please see");
                System.logE(" * https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html");
                System.logE(" * for more details.");
            } else {
                throw new NoSuchAlgorithmException("The BC provider no longer provides an"
                        + " implementation for " + key + ".  Please see"
                        + " https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html"
                        + " for more details.");
            }
        }
    }
    // END Android-added: Check for requests of deprecated Bouncy Castle algorithms.

}
