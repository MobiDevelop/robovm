/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.lang;

import android.system.ErrnoException;
import android.system.StructPasswd;
import android.system.StructUtsname;
import dalvik.system.VMRuntime;
import java.io.*;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyPermission;
import libcore.icu.ICU;
import libcore.io.Libcore;
import libcore.timezone.TimeZoneDataFiles;

import org.robovm.rt.VM;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;
/**
 * The <code>System</code> class contains several useful class fields
 * and methods. It cannot be instantiated.
 *
 * <p>Among the facilities provided by the <code>System</code> class
 * are standard input, standard output, and error output streams;
 * access to externally defined properties and environment
 * variables; a means of loading files and libraries; and a utility
 * method for quickly copying a portion of an array.
 *
 * @author  unascribed
 * @since   JDK1.0
 */
public final class System {
    /** Don't let anyone instantiate this class */
    private System() {
    }

    /**
     * The "standard" input stream. This stream is already
     * open and ready to supply input data. Typically this stream
     * corresponds to keyboard input or another input source specified by
     * the host environment or user.
     */
    public final static InputStream in;

    /**
     * The "standard" output stream. This stream is already
     * open and ready to accept output data. Typically this stream
     * corresponds to display output or another output destination
     * specified by the host environment or user.
     * <p>
     * For simple stand-alone Java applications, a typical way to write
     * a line of output data is:
     * <blockquote><pre>
     *     System.out.println(data)
     * </pre></blockquote>
     * <p>
     * See the <code>println</code> methods in class <code>PrintStream</code>.
     *
     * @see     java.io.PrintStream#println()
     * @see     java.io.PrintStream#println(boolean)
     * @see     java.io.PrintStream#println(char)
     * @see     java.io.PrintStream#println(char[])
     * @see     java.io.PrintStream#println(double)
     * @see     java.io.PrintStream#println(float)
     * @see     java.io.PrintStream#println(int)
     * @see     java.io.PrintStream#println(long)
     * @see     java.io.PrintStream#println(java.lang.Object)
     * @see     java.io.PrintStream#println(java.lang.String)
     */
    public final static PrintStream out;

    /**
     * The "standard" error output stream. This stream is already
     * open and ready to accept output data.
     * <p>
     * Typically this stream corresponds to display output or another
     * output destination specified by the host environment or user. By
     * convention, this output stream is used to display error messages
     * or other information that should come to the immediate attention
     * of a user even if the principal output stream, the value of the
     * variable <code>out</code>, has been redirected to a file or other
     * destination that is typically not continuously monitored.
     */
    public final static PrintStream err;

    /**
     * Dedicated lock for GC / Finalization logic.
     */
    private static final Object LOCK = new Object();

    /**
     * Whether or not we need to do a GC before running the finalizers.
     */
    private static boolean runGC;

    /**
     * If we just ran finalization, we might want to do a GC to free the finalized objects.
     * This lets us do gc/runFinlization/gc sequences but prevents back to back System.gc().
     */
    private static boolean justRanFinalization;

    /**
     * Reassigns the "standard" input stream.
     *
     * <p>First, if there is a security manager, its <code>checkPermission</code>
     * method is called with a <code>RuntimePermission("setIO")</code> permission
     *  to see if it's ok to reassign the "standard" input stream.
     * <p>
     *
     * @param in the new standard input stream.
     *
     * @throws SecurityException
     *        if a security manager exists and its
     *        <code>checkPermission</code> method doesn't allow
     *        reassigning of the standard input stream.
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setIn(InputStream in) {
        setIn0(in);
    }

    /**
     * Reassigns the "standard" output stream.
     *
     * <p>First, if there is a security manager, its <code>checkPermission</code>
     * method is called with a <code>RuntimePermission("setIO")</code> permission
     *  to see if it's ok to reassign the "standard" output stream.
     *
     * @param out the new standard output stream
     *
     * @throws SecurityException
     *        if a security manager exists and its
     *        <code>checkPermission</code> method doesn't allow
     *        reassigning of the standard output stream.
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setOut(PrintStream out) {
        setOut0(out);
    }

    /**
     * Reassigns the "standard" error output stream.
     *
     * <p>First, if there is a security manager, its <code>checkPermission</code>
     * method is called with a <code>RuntimePermission("setIO")</code> permission
     *  to see if it's ok to reassign the "standard" error output stream.
     *
     * @param err the new standard error output stream.
     *
     * @throws SecurityException
     *        if a security manager exists and its
     *        <code>checkPermission</code> method doesn't allow
     *        reassigning of the standard error output stream.
     *
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     *
     * @since   JDK1.1
     */
    public static void setErr(PrintStream err) {
        setErr0(err);
    }

    private static volatile Console cons = null;
    /**
     * Returns the unique {@link java.io.Console Console} object associated
     * with the current Java virtual machine, if any.
     *
     * @return  The system console, if any, otherwise <tt>null</tt>.
     *
     * @since   1.6
     */
     public static Console console() {
         // Android-changed: Added proper double checked locking for cons access
         if (cons == null) {
             synchronized (System.class) {
                 if (cons == null) {
                     cons = Console.console();
                 }
             }
         }
         return cons;
     }

    /**
     * Returns the channel inherited from the entity that created this
     * Java virtual machine.
     *
     * <p> This method returns the channel obtained by invoking the
     * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
     * inheritedChannel} method of the system-wide default
     * {@link java.nio.channels.spi.SelectorProvider} object. </p>
     *
     * <p> In addition to the network-oriented channels described in
     * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
     * inheritedChannel}, this method may return other kinds of
     * channels in the future.
     *
     * @return  The inherited channel, if any, otherwise <tt>null</tt>.
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  SecurityException
     *          If a security manager is present and it does not
     *          permit access to the channel.
     *
     * @since 1.5
     */
    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }

    private static native void setIn0(InputStream in);
    private static native void setOut0(PrintStream out);
    private static native void setErr0(PrintStream err);

    /**
     * Throws {@code SecurityException} (except in case {@code sm == null}).
     *
     * <p>Security managers do <i>not</i> provide a secure environment for
     * executing untrusted code and are unsupported on Android. Untrusted code
     * cannot be safely isolated within a single VM on Android, so this method
     * <i>always</i> throws a {@code SecurityException} when passed a non-null SecurityManager
     *
     * @param s a security manager
     * @throws SecurityException always, unless {@code sm == null}
     */
    public static
    void setSecurityManager(final SecurityManager s) {
        if (s != null) {
            throw new SecurityException();
        }
    }

    /**
     * Always returns {@code null} in Android
     *
     * @return  {@code null} in Android
     */
    public static SecurityManager getSecurityManager() {
        // No-op on android.
        return null;
    }

    /**
     * Returns the current time in milliseconds.  Note that
     * while the unit of time of the return value is a millisecond,
     * the granularity of the value depends on the underlying
     * operating system and may be larger.  For example, many
     * operating systems measure time in units of tens of
     * milliseconds.
     *
     * <p> See the description of the class <code>Date</code> for
     * a discussion of slight discrepancies that may arise between
     * "computer time" and coordinated universal time (UTC).
     *
     * @return  the difference, measured in milliseconds, between
     *          the current time and midnight, January 1, 1970 UTC.
     * @see     java.util.Date
     */
    public static native long currentTimeMillis();

    /**
     * Returns the current value of the running Java Virtual Machine's
     * high-resolution time source, in nanoseconds.
     *
     * <p>This method can only be used to measure elapsed time and is
     * not related to any other notion of system or wall-clock time.
     * The value returned represents nanoseconds since some fixed but
     * arbitrary <i>origin</i> time (perhaps in the future, so values
     * may be negative).  The same origin is used by all invocations of
     * this method in an instance of a Java virtual machine; other
     * virtual machine instances are likely to use a different origin.
     *
     * <p>This method provides nanosecond precision, but not necessarily
     * nanosecond resolution (that is, how frequently the value changes)
     * - no guarantees are made except that the resolution is at least as
     * good as that of {@link #currentTimeMillis()}.
     *
     * <p>Differences in successive calls that span greater than
     * approximately 292 years (2<sup>63</sup> nanoseconds) will not
     * correctly compute elapsed time due to numerical overflow.
     *
     * <p>The values returned by this method become meaningful only when
     * the difference between two such values, obtained within the same
     * instance of a Java virtual machine, is computed.
     *
     * <p> For example, to measure how long some code takes to execute:
     *  <pre> {@code
     * long startTime = System.nanoTime();
     * // ... the code being measured ...
     * long estimatedTime = System.nanoTime() - startTime;}</pre>
     *
     * <p>To compare two nanoTime values
     *  <pre> {@code
     * long t0 = System.nanoTime();
     * ...
     * long t1 = System.nanoTime();}</pre>
     *
     * one should use {@code t1 - t0 < 0}, not {@code t1 < t0},
     * because of the possibility of numerical overflow.
     *
     * @return the current value of the running Java Virtual Machine's
     *         high-resolution time source, in nanoseconds
     * @since 1.5
     */
    public static native long nanoTime();

    /**
     * Copies {@code length} elements from the array {@code src},
     * starting at offset {@code srcPos}, into the array {@code dst},
     * starting at offset {@code dstPos}.
     *
     * <p>The source and destination arrays can be the same array,
     * in which case copying is performed as if the source elements
     * are first copied into a temporary array and then into the
     * destination array.
     *
     * @param src
     *            the source array to copy the content.
     * @param srcPos
     *            the starting index of the content in {@code src}.
     * @param dst
     *            the destination array to copy the data into.
     * @param dstPos
     *            the starting index for the copied content in {@code dst}.
     * @param length
     *            the number of elements to be copied.
     */
    public static void arraycopy(Object src, int srcPos, Object dst, int dstPos,
                                 int length) {
        // RoboVM note: This is native in Android. We're using code from Apache Harmony instead.
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        if (dst == null) {
            throw new NullPointerException("dst == null");
        }
        Class<?> type1 = src.getClass();
        Class<?> type2 = dst.getClass();
        if (!type1.isArray()) {
            throw new ArrayStoreException("source of type " + type1.getName() + " is not an array");
        }
        if (!type2.isArray()) {
            throw new ArrayStoreException("destination of type " + type2.getName() + " is not an array");
        }
        Class<?> componentType1 = type1.getComponentType();
        Class<?> componentType2 = type2.getComponentType();
        if (!componentType1.isPrimitive()) {
            if (componentType2.isPrimitive()) {
                throw new ArrayStoreException(type1.getCanonicalName() + " and " + type2.getCanonicalName()
                        + " are incompatible array types");
            }
            arraycopy((Object[]) src, srcPos, (Object[]) dst, dstPos, length);
        } else {
            if (componentType2 != componentType1) {
                throw new ArrayStoreException(type1.getCanonicalName() + " and " + type2.getCanonicalName()
                        + " are incompatible array types");
            }
            if (componentType1 == int.class) {
                arraycopy((int[]) src, srcPos, (int[]) dst, dstPos, length);
            } else if (componentType1 == byte.class) {
                arraycopy((byte[]) src, srcPos, (byte[]) dst, dstPos, length);
            } else if (componentType1 == long.class) {
                arraycopy((long[]) src, srcPos, (long[]) dst, dstPos, length);
            } else if (componentType1 == short.class) {
                arraycopy((short[]) src, srcPos, (short[]) dst, dstPos, length);
            } else if (componentType1 == char.class) {
                arraycopy((char[]) src, srcPos, (char[]) dst, dstPos, length);
            } else if (componentType1 == boolean.class) {
                arraycopy((boolean[]) src, srcPos, (boolean[]) dst, dstPos, length);
            } else if (componentType1 == double.class) {
                arraycopy((double[]) src, srcPos, (double[]) dst, dstPos, length);
            } else if (componentType1 == float.class) {
                arraycopy((float[]) src, srcPos, (float[]) dst, dstPos, length);
            }
        }
    }

    private static void arraycopyCheckBounds(int srcLength, int srcPos, int dstLength, int dstPos, int length) {
        if (srcPos < 0 || dstPos < 0 || length < 0 ||
                srcPos > srcLength - length ||
                dstPos > dstLength - length) {
            throw new ArrayIndexOutOfBoundsException("src.length=" + srcLength + " srcPos=" + srcPos
                    + " dst.length=" + dstLength + " dstPos=" + dstPos + " length=" + length);
        }
    }

    private static void arraycopyFast(Object src, int srcPos, Object dst, int dstPos, int length, int logElemSize) {
        if (length > 0) {
            long srcAddr = VM.getArrayValuesAddress(src) + (srcPos << logElemSize);
            long dstAddr = VM.getArrayValuesAddress(dst) + (dstPos << logElemSize);
            if (logElemSize == 0) {
                VM.memmove8(dstAddr, srcAddr, length);
            } else if (logElemSize == 1) {
                VM.memmove16(dstAddr, srcAddr, length);
            } else if (logElemSize == 2) {
                VM.memmove32(dstAddr, srcAddr, length);
            } else if (logElemSize == 3) {
                VM.memmove64(dstAddr, srcAddr, length);
            } else {
                throw new AssertionError();
            }
        }
    }

    private static void arraycopy(Object[] src, int srcPos, Object[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        if (length > 0) {
            // TODO: Use arraycopyFast() if src.class and dst.class have same dimensionality and (src instanceof dst)
            int i = 0;
            try {
                // Check if this is a forward or backwards arraycopy
                if (src != dst || srcPos > dstPos || srcPos + length <= dstPos) {
                    for (i = 0; i < length; ++i) {
                        dst[dstPos + i] = src[srcPos + i];
                    }
                } else {
                    for (i = length - 1; i >= 0; --i) {
                        dst[dstPos + i] = src[srcPos + i];
                    }
                }
            } catch (ArrayStoreException e) {
                // Throw a new one with a more descriptive message.
                Class<?> srcElemClass = src[i + srcPos].getClass();
                String srcElemTypeName = srcElemClass.isArray()
                        ? srcElemClass.getCanonicalName() : srcElemClass.getName();
                throw new ArrayStoreException(String.format(
                        "source[%d] of type %s cannot be stored in destination array of type %s",
                        i + srcPos, srcElemTypeName, dst.getClass().getCanonicalName()));
            }
        }
    }

    private static void arraycopy(int[] src, int srcPos, int[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 2);
    }

    private static void arraycopy(byte[] src, int srcPos, byte[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 0);
    }

    private static void arraycopy(short[] src, int srcPos, short[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 1);
    }

    private static void arraycopy(long[] src, int srcPos, long[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 3);
    }

    private static void arraycopy(char[] src, int srcPos, char[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 1);
    }

    private static void arraycopy(boolean[] src, int srcPos, boolean[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 0);
    }

    private static void arraycopy(double[] src, int srcPos, double[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 3);
    }

    private static void arraycopy(float[] src, int srcPos, float[] dst, int dstPos, int length) {
        arraycopyCheckBounds(src.length, srcPos, dst.length, dstPos, length);
        arraycopyFast(src, srcPos, dst, dstPos, length, 2);
    }

    /**
     * Returns the same hash code for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode().
     * The hash code for the null reference is zero.
     *
     * @param x object for which the hashCode is to be calculated
     * @return  the hashCode
     * @since   JDK1.1
     */
    // RoboVM Note: using native implementation
    public static native int identityHashCode(Object x);

    /**
     * System properties. The following properties are guaranteed to be defined:
     * <dl>
     * <dt>java.version         <dd>Java version number
     * <dt>java.vendor          <dd>Java vendor specific string
     * <dt>java.vendor.url      <dd>Java vendor URL
     * <dt>java.home            <dd>Java installation directory
     * <dt>java.class.version   <dd>Java class version number
     * <dt>java.class.path      <dd>Java classpath
     * <dt>os.name              <dd>Operating System Name
     * <dt>os.arch              <dd>Operating System Architecture
     * <dt>os.version           <dd>Operating System Version
     * <dt>file.separator       <dd>File separator ("/" on Unix)
     * <dt>path.separator       <dd>Path separator (":" on Unix)
     * <dt>line.separator       <dd>Line separator ("\n" on Unix)
     * <dt>user.name            <dd>User account name
     * <dt>user.home            <dd>User home directory
     * <dt>user.dir             <dd>User's current working directory
     * </dl>
     */

    private static Properties props;
    private static Properties unchangeableProps;

    private static native String[] specialProperties();

    private static native String[] robovmSpecialProperties();

    static final class PropertiesWithNonOverrideableDefaults extends Properties {
        PropertiesWithNonOverrideableDefaults(Properties defaults) {
            super(defaults);
        }

        @Override
        public Object put(Object key, Object value) {
            if (defaults.containsKey(key)) {
                logE("Ignoring attempt to set property \"" + key +
                        "\" to value \"" + value + "\".");
                return defaults.get(key);
            }

            return super.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            if (defaults.containsKey(key)) {
                logE("Ignoring attempt to remove property \"" + key + "\".");
                return null;
            }

            return super.remove(key);
        }
    }

    private static void parsePropertyAssignments(Properties p, String[] assignments) {
        for (String assignment : assignments) {
            int split = assignment.indexOf('=');
            String key = assignment.substring(0, split);
            String value = assignment.substring(split + 1);
            p.put(key, value);
        }
    }

    private static Properties initUnchangeableSystemProperties() {
        VMRuntime runtime = VMRuntime.getRuntime();
        Properties p = new Properties();

        // Set non-static properties.
        p.put("java.boot.class.path", runtime.bootClassPath());
        p.put("java.class.path", runtime.classPath());

        // TODO: does this make any sense? Should we just leave java.home unset?
        String javaHome = getenv("JAVA_HOME");
        if (javaHome == null) {
            javaHome = "/system";
        }
        p.put("java.home", javaHome);

        p.put("java.vm.version", runtime.vmVersion());

        try {
            StructPasswd passwd = Libcore.os.getpwuid(Libcore.os.getuid());
            p.put("user.home", passwd.pw_dir);
            p.put("user.name", passwd.pw_name);
        } catch (ErrnoException exception) {
            // RoboVM note: Start change. Fall back to environment variables. getpwuid() fails on the iOS simulator.
            String home = getenv("HOME");
            String user = getenv("USER");
            p.put("user.home", home != null ? home : "");
            p.put("user.name", user != null ? user : "");
            // RoboVM note: End change.
        }

        StructUtsname info = Libcore.os.uname();
        p.put("os.arch", info.machine);
        // os.name was previously hardcoded to "Linux", but was reverted due to support
        // for Fuchsia. b/121268567 shows initialization regressions.
        p.put("os.name", info.sysname);
        p.put("os.version", info.release);

        // RoboVM note: Added in RoboVM. Make sure we get sane and consistent
        // user.home, user.dir and user.name values on iOS.
        if (info.machine.contains("iOS")) {
            // On iOS we want user.home and user.dir to point to the app's data
            // container root dir. This is the dir $HOME points to. We also set
            // user.name to $USER or hardcode 'mobile' if $USER isn't set (iOS
            // simulator).
            String home = getenv("HOME");
            p.put("user.dir", home != null ? home : "/");
        }
        // RoboVM note: End change.

        // Android-added: Undocumented properties that exist only on Android.
        p.put("android.icu.library.version", ICU.getIcuVersion());
        p.put("android.icu.unicode.version", ICU.getUnicodeVersion());
        p.put("android.icu.cldr.version", ICU.getCldrVersion());

        // Property override for ICU4J : this is the location of the ICU4C data. This
        // is prioritized over the properties in ICUConfig.properties. The issue with using
        // that is that it doesn't play well with jarjar and it needs complicated build rules
        // to change its default value.
        String icuDataPath = TimeZoneDataFiles.generateIcuDataPath();
        p.put("android.icu.impl.ICUBinary.dataPath", icuDataPath);

        parsePropertyAssignments(p, specialProperties());

        parsePropertyAssignments(p, robovmSpecialProperties());

        // Override built-in properties with settings from the command line.
        // Note: it is not possible to override hardcoded values.
        parsePropertyAssignments(p, runtime.properties());


        // Set static hardcoded properties.
        // These come last, as they must be guaranteed to agree with what a backend compiler
        // may assume when compiling the boot image on Android.
        for (String[] pair : AndroidHardcodedSystemProperties.STATIC_PROPERTIES) {
            if (p.containsKey(pair[0])) {
                logE("Ignoring command line argument: -D" + pair[0]);
            }
            if (pair[1] == null) {
                p.remove(pair[0]);
            } else {
                p.put(pair[0], pair[1]);
            }
        }

        return p;
    }

    private static Properties initProperties() {
        Properties p = new PropertiesWithNonOverrideableDefaults(unchangeableProps);
        setDefaultChangeableProperties(p);
        return p;
    }

    private static Properties setDefaultChangeableProperties(Properties p) {
        // On Android, each app gets its own temporary directory.
        // (See android.app.ActivityThread.) This is just a fallback default,
        // useful only on the host.
        // We check first if the property has not been set already: note that it
        // can only be set from the command line through the '-Djava.io.tmpdir=' option.
        if (!unchangeableProps.containsKey("java.io.tmpdir")) {
            // RoboVM note: Use value of $TMPDIR if set. Otherwise use /tmp as Android does.
            String tmpdir = getenv("TMPDIR");
            p.put("java.io.tmpdir", tmpdir != null ? tmpdir : "/tmp");
        }

        // Android has always had an empty "user.home" (see docs for getProperty).
        // This is not useful for normal android apps which need to use android specific
        // APIs such as {@code Context.getFilesDir} and {@code Context.getCacheDir} but
        // we make it changeable for backward compatibility, so that they can change it
        // to a writeable location if required.
        // We check first if the property has not been set already: note that it
        // can only be set from the command line through the '-Duser.home=' option.
        if (!unchangeableProps.containsKey("user.home")) {
            p.put("user.home", "");
        }

        return p;
    }

    /**
     * Inits an unchangeable system property with the given value.
     *
     * This is called from native code when the environment needs to change under native
     * bridge emulation.
     *
     * @hide also visible for tests.
     */
    public static void setUnchangeableSystemProperty(String key, String value) {
        checkKey(key);
        unchangeableProps.put(key, value);
    }

    private static void addLegacyLocaleSystemProperties() {
        final String locale = getProperty("user.locale", "");
        if (!locale.isEmpty()) {
            Locale l = Locale.forLanguageTag(locale);
            setUnchangeableSystemProperty("user.language", l.getLanguage());
            setUnchangeableSystemProperty("user.region", l.getCountry());
            setUnchangeableSystemProperty("user.variant", l.getVariant());
        } else {
            // If "user.locale" isn't set we fall back to our old defaults of
            // language="en" and region="US" (if unset) and don't attempt to set it.
            // The Locale class will fall back to using user.language and
            // user.region if unset.
            final String language = getProperty("user.language", "");
            final String region = getProperty("user.region", "");

            if (language.isEmpty()) {
                setUnchangeableSystemProperty("user.language", "en");
            }

            if (region.isEmpty()) {
                setUnchangeableSystemProperty("user.region", "US");
            }
        }
    }

    /**
     * Determines the current system properties.
     *
     *
     * <p>The following properties are always provided by the Dalvik VM:</p>
     * <p><table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
     * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
     *     <td><b>Name</b></td>        <td><b>Meaning</b></td>                    <td><b>Example</b></td></tr>
     * <tr><td>file.separator</td>     <td>{@link java.io.File#separator}</td>    <td>{@code /}</td></tr>
     *
     * <tr><td>java.class.path</td>    <td>System class path</td>                 <td>{@code .}</td></tr>
     * <tr><td>java.class.version</td> <td>(Not useful on Android)</td>           <td>{@code 50.0}</td></tr>
     * <tr><td>java.compiler</td>      <td>(Not useful on Android)</td>           <td>Empty</td></tr>
     * <tr><td>java.ext.dirs</td>      <td>(Not useful on Android)</td>           <td>Empty</td></tr>
     * <tr><td>java.home</td>          <td>Location of the VM on the file system</td> <td>{@code /system}</td></tr>
     * <tr><td>java.io.tmpdir</td>     <td>See {@link java.io.File#createTempFile}</td> <td>{@code /sdcard}</td></tr>
     * <tr><td>java.library.path</td>  <td>Search path for JNI libraries</td>     <td>{@code /vendor/lib:/system/lib}</td></tr>
     * <tr><td>java.vendor</td>        <td>Human-readable VM vendor</td>          <td>{@code The Android Project}</td></tr>
     * <tr><td>java.vendor.url</td>    <td>URL for VM vendor's web site</td>      <td>{@code http://www.android.com/}</td></tr>
     * <tr><td>java.version</td>       <td>(Not useful on Android)</td>           <td>{@code 0}</td></tr>
     *
     * <tr><td>java.specification.version</td>    <td>VM libraries version</td>        <td>{@code 0.9}</td></tr>
     * <tr><td>java.specification.vendor</td>     <td>VM libraries vendor</td>         <td>{@code The Android Project}</td></tr>
     * <tr><td>java.specification.name</td>       <td>VM libraries name</td>           <td>{@code Dalvik Core Library}</td></tr>
     * <tr><td>java.vm.version</td>               <td>VM implementation version</td>   <td>{@code 1.2.0}</td></tr>
     * <tr><td>java.vm.vendor</td>                <td>VM implementation vendor</td>    <td>{@code The Android Project}</td></tr>
     * <tr><td>java.vm.name</td>                  <td>VM implementation name</td>      <td>{@code Dalvik}</td></tr>
     * <tr><td>java.vm.specification.version</td> <td>VM specification version</td>    <td>{@code 0.9}</td></tr>
     * <tr><td>java.vm.specification.vendor</td>  <td>VM specification vendor</td>     <td>{@code The Android Project}</td></tr>
     * <tr><td>java.vm.specification.name</td>    <td>VM specification name</td>       <td>{@code Dalvik Virtual Machine Specification}</td></tr>
     *
     * <tr><td>line.separator</td>     <td>The system line separator</td>         <td>{@code \n}</td></tr>
     *
     * <tr><td>os.arch</td>            <td>OS architecture</td>                   <td>{@code armv7l}</td></tr>
     * <tr><td>os.name</td>            <td>OS (kernel) name</td>                  <td>{@code Linux}</td></tr>
     * <tr><td>os.version</td>         <td>OS (kernel) version</td>               <td>{@code 2.6.32.9-g103d848}</td></tr>
     *
     * <tr><td>path.separator</td>     <td>See {@link java.io.File#pathSeparator}</td> <td>{@code :}</td></tr>
     *
     * <tr><td>user.dir</td>           <td>Base of non-absolute paths</td>        <td>{@code /}</td></tr>
     * <tr><td>user.home</td>          <td>(Not useful on Android)</td>           <td>Empty</td></tr>
     * <tr><td>user.name</td>          <td>(Not useful on Android)</td>           <td>Empty</td></tr>
     *
     * </table>
     * <p>
     * Multiple paths in a system property value are separated by the path
     * separator character of the platform.
     * <p>
     * Note that even if the security manager does not permit the
     * <code>getProperties</code> operation, it may choose to permit the
     * {@link #getProperty(String)} operation.
     *
     * @return     the system properties
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkPropertiesAccess</code> method doesn't allow access
     *              to the system properties.
     * @see        #setProperties
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertiesAccess()
     * @see        java.util.Properties
     */
    public static Properties getProperties() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }

        return props;
    }

    /**
     * Returns the system-dependent line separator string.  It always
     * returns the same value - the initial value of the {@linkplain
     * #getProperty(String) system property} {@code line.separator}.
     *
     * <p>On UNIX systems, it returns {@code "\n"}; on Microsoft
     * Windows systems it returns {@code "\r\n"}.
     *
     * @return the system-dependent line separator string
     * @since 1.7
     */
    public static String lineSeparator() {
        return lineSeparator;
    }

    private static String lineSeparator;


    // Comment replaced with android one.
    /**
     * Attempts to set all system properties. Copies all properties from
     * {@code p} and discards system properties that are read only and cannot
     * be modified. See {@link #getProperty} for a list of such properties.
     */
    public static void setProperties(Properties props) {
        Properties baseProperties = new PropertiesWithNonOverrideableDefaults(unchangeableProps);
        if (props != null) {
            baseProperties.putAll(props);
        } else {
            setDefaultChangeableProperties(baseProperties);
        }

        System.props = baseProperties;
    }

    /**
     * Gets the system property indicated by the specified key.
     * <p>
     * First, if there is a security manager, its
     * <code>checkPropertyAccess</code> method is called with the key as
     * its argument. This may result in a SecurityException.
     * <p>
     * If there is no current set of system properties, a set of system
     * properties is first created and initialized in the same manner as
     * for the <code>getProperties</code> method.
     *
     * @param      key   the name of the system property.
     * @return     the string value of the system property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     * @see        #setProperty
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see        java.lang.System#getProperties()
     */
    public static String getProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key);
    }

    /**
     * Gets the system property indicated by the specified key.
     * <p>
     * First, if there is a security manager, its
     * <code>checkPropertyAccess</code> method is called with the
     * <code>key</code> as its argument.
     * <p>
     * If there is no current set of system properties, a set of system
     * properties is first created and initialized in the same manner as
     * for the <code>getProperties</code> method.
     *
     * @param      key   the name of the system property.
     * @param      def   a default value.
     * @return     the string value of the system property,
     *             or the default value if there is no property with that key.
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *             access to the specified system property.
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     * @see        #setProperty
     * @see        java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     * @see        java.lang.System#getProperties()
     */
    public static String getProperty(String key, String def) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }

        return props.getProperty(key, def);
    }

    /**
     * Sets the system property indicated by the specified key.
     * <p>
     * First, if a security manager exists, its
     * <code>SecurityManager.checkPermission</code> method
     * is called with a <code>PropertyPermission(key, "write")</code>
     * permission. This may result in a SecurityException being thrown.
     * If no exception is thrown, the specified property is set to the given
     * value.
     * <p>
     *
     * @param      key   the name of the system property.
     * @param      value the value of the system property.
     * @return     the previous value of the system property,
     *             or <code>null</code> if it did not have one.
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkPermission</code> method doesn't allow
     *             setting of the specified property.
     * @exception  NullPointerException if <code>key</code> or
     *             <code>value</code> is <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     * @see        #getProperty
     * @see        java.lang.System#getProperty(java.lang.String)
     * @see        java.lang.System#getProperty(java.lang.String, java.lang.String)
     * @see        java.util.PropertyPermission
     * @see        SecurityManager#checkPermission
     * @since      1.2
     */
    public static String setProperty(String key, String value) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key,
                SecurityConstants.PROPERTY_WRITE_ACTION));
        }

        return (String) props.setProperty(key, value);
    }

    /**
     * Removes the system property indicated by the specified key.
     * <p>
     * First, if a security manager exists, its
     * <code>SecurityManager.checkPermission</code> method
     * is called with a <code>PropertyPermission(key, "write")</code>
     * permission. This may result in a SecurityException being thrown.
     * If no exception is thrown, the specified property is removed.
     * <p>
     *
     * @param      key   the name of the system property to be removed.
     * @return     the previous string value of the system property,
     *             or <code>null</code> if there was no property with that key.
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkPropertyAccess</code> method doesn't allow
     *              access to the specified system property.
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     * @see        #getProperty
     * @see        #setProperty
     * @see        java.util.Properties
     * @see        java.lang.SecurityException
     * @see        java.lang.SecurityManager#checkPropertiesAccess()
     * @since 1.5
     */
    public static String clearProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key, "write"));
        }

        return (String) props.remove(key);
    }

    private static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }

    /**
     * Gets the value of the specified environment variable. An
     * environment variable is a system-dependent external named
     * value.
     *
     * <p>If a security manager exists, its
     * {@link SecurityManager#checkPermission checkPermission}
     * method is called with a
     * <code>{@link RuntimePermission}("getenv."+name)</code>
     * permission.  This may result in a {@link SecurityException}
     * being thrown.  If no exception is thrown the value of the
     * variable <code>name</code> is returned.
     *
     * <p><a name="EnvironmentVSSystemProperties"><i>System
     * properties</i> and <i>environment variables</i></a> are both
     * conceptually mappings between names and values.  Both
     * mechanisms can be used to pass user-defined information to a
     * Java process.  Environment variables have a more global effect,
     * because they are visible to all descendants of the process
     * which defines them, not just the immediate Java subprocess.
     * They can have subtly different semantics, such as case
     * insensitivity, on different operating systems.  For these
     * reasons, environment variables are more likely to have
     * unintended side effects.  It is best to use system properties
     * where possible.  Environment variables should be used when a
     * global effect is desired, or when an external system interface
     * requires an environment variable (such as <code>PATH</code>).
     *
     * <p>On UNIX systems the alphabetic case of <code>name</code> is
     * typically significant, while on Microsoft Windows systems it is
     * typically not.  For example, the expression
     * <code>System.getenv("FOO").equals(System.getenv("foo"))</code>
     * is likely to be true on Microsoft Windows.
     *
     * @param  name the name of the environment variable
     * @return the string value of the variable, or <code>null</code>
     *         if the variable is not defined in the system environment
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @throws SecurityException
     *         if a security manager exists and its
     *         {@link SecurityManager#checkPermission checkPermission}
     *         method doesn't allow access to the environment variable
     *         <code>name</code>
     * @see    #getenv()
     * @see    ProcessBuilder#environment()
     */
    public static String getenv(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        return Libcore.os.getenv(name);
    }


    /**
     * Returns an unmodifiable string map view of the current system environment.
     * The environment is a system-dependent mapping from names to
     * values which is passed from parent to child processes.
     *
     * <p>If the system does not support environment variables, an
     * empty map is returned.
     *
     * <p>The returned map will never contain null keys or values.
     * Attempting to query the presence of a null key or value will
     * throw a {@link NullPointerException}.  Attempting to query
     * the presence of a key or value which is not of type
     * {@link String} will throw a {@link ClassCastException}.
     *
     * <p>The returned map and its collection views may not obey the
     * general contract of the {@link Object#equals} and
     * {@link Object#hashCode} methods.
     *
     * <p>The returned map is typically case-sensitive on all platforms.
     *
     * <p>If a security manager exists, its
     * {@link SecurityManager#checkPermission checkPermission}
     * method is called with a
     * <code>{@link RuntimePermission}("getenv.*")</code>
     * permission.  This may result in a {@link SecurityException} being
     * thrown.
     *
     * <p>When passing information to a Java subprocess,
     * <a href=#EnvironmentVSSystemProperties>system properties</a>
     * are generally preferred over environment variables.
     *
     * @return the environment as a map of variable names to values
     * @throws SecurityException
     *         if a security manager exists and its
     *         {@link SecurityManager#checkPermission checkPermission}
     *         method doesn't allow access to the process environment
     * @see    #getenv(String)
     * @see    ProcessBuilder#environment()
     * @since  1.5
     */
    public static java.util.Map<String,String> getenv() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv.*"));
        }

        return ProcessEnvironment.getenv();
    }

    /**
     * Terminates the currently running Java Virtual Machine. The
     * argument serves as a status code; by convention, a nonzero status
     * code indicates abnormal termination.
     * <p>
     * This method calls the <code>exit</code> method in class
     * <code>Runtime</code>. This method never returns normally.
     * <p>
     * The call <code>System.exit(n)</code> is effectively equivalent to
     * the call:
     * <blockquote><pre>
     * Runtime.getRuntime().exit(n)
     * </pre></blockquote>
     *
     * @param      status   exit status.
     * @throws  SecurityException
     *        if a security manager exists and its <code>checkExit</code>
     *        method doesn't allow exit with the specified status.
     * @see        java.lang.Runtime#exit(int)
     */
    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    /**
     * Runs the garbage collector.
     * <p>
     * Calling the <code>gc</code> method suggests that the Java Virtual
     * Machine expend effort toward recycling unused objects in order to
     * make the memory they currently occupy available for quick reuse.
     * When control returns from the method call, the Java Virtual
     * Machine has made a best effort to reclaim space from all discarded
     * objects.
     * <p>
     * The call <code>System.gc()</code> is effectively equivalent to the
     * call:
     * <blockquote><pre>
     * Runtime.getRuntime().gc()
     * </pre></blockquote>
     *
     * @see     java.lang.Runtime#gc()
     */
    public static void gc() {
        boolean shouldRunGC;
        synchronized (LOCK) {
            shouldRunGC = justRanFinalization;
            if (shouldRunGC) {
                justRanFinalization = false;
            } else {
                runGC = true;
            }
        }
        if (shouldRunGC) {
            Runtime.getRuntime().gc();
        }
    }

    /**
     * Runs the finalization methods of any objects pending finalization.
     * <p>
     * Calling this method suggests that the Java Virtual Machine expend
     * effort toward running the <code>finalize</code> methods of objects
     * that have been found to be discarded but whose <code>finalize</code>
     * methods have not yet been run. When control returns from the
     * method call, the Java Virtual Machine has made a best effort to
     * complete all outstanding finalizations.
     * <p>
     * The call <code>System.runFinalization()</code> is effectively
     * equivalent to the call:
     * <blockquote><pre>
     * Runtime.getRuntime().runFinalization()
     * </pre></blockquote>
     *
     * @see     java.lang.Runtime#runFinalization()
     */
    public static void runFinalization() {
        boolean shouldRunGC;
        synchronized (LOCK) {
            shouldRunGC = runGC;
            runGC = false;
        }
        if (shouldRunGC) {
            Runtime.getRuntime().gc();
        }
        Runtime.getRuntime().runFinalization();
        synchronized (LOCK) {
            justRanFinalization = true;
        }
    }

    /**
     * Enable or disable finalization on exit; doing so specifies that the
     * finalizers of all objects that have finalizers that have not yet been
     * automatically invoked are to be run before the Java runtime exits.
     * By default, finalization on exit is disabled.
     *
     * <p>If there is a security manager,
     * its <code>checkExit</code> method is first called
     * with 0 as its argument to ensure the exit is allowed.
     * This could result in a SecurityException.
     *
     * @deprecated  This method is inherently unsafe.  It may result in
     *      finalizers being called on live objects while other threads are
     *      concurrently manipulating those objects, resulting in erratic
     *      behavior or deadlock.
     * @param value indicating enabling or disabling of finalization
     * @throws  SecurityException
     *        if a security manager exists and its <code>checkExit</code>
     *        method doesn't allow the exit.
     *
     * @see     java.lang.Runtime#exit(int)
     * @see     java.lang.Runtime#gc()
     * @see     java.lang.SecurityManager#checkExit(int)
     * @since   JDK1.1
     */
    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        Runtime.runFinalizersOnExit(value);
    }

    /**
     * Loads the native library specified by the filename argument.  The filename
     * argument must be an absolute path name.
     *
     * If the filename argument, when stripped of any platform-specific library
     * prefix, path, and file extension, indicates a library whose name is,
     * for example, L, and a native library called L is statically linked
     * with the VM, then the JNI_OnLoad_L function exported by the library
     * is invoked rather than attempting to load a dynamic library.
     * A filename matching the argument does not have to exist in the
     * file system.
     * See the JNI Specification for more details.
     *
     * Otherwise, the filename argument is mapped to a native library image in
     * an implementation-dependent manner.
     *
     * <p>
     * The call <code>System.load(name)</code> is effectively equivalent
     * to the call:
     * <blockquote><pre>
     * Runtime.getRuntime().load(name)
     * </pre></blockquote>
     *
     * @param      filename   the file to load.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkLink</code> method doesn't allow
     *             loading of the specified dynamic library
     * @exception  UnsatisfiedLinkError  if either the filename is not an
     *             absolute path name, the native library is not statically
     *             linked with the VM, or the library cannot be mapped to
     *             a native library image by the host system.
     * @exception  NullPointerException if <code>filename</code> is
     *             <code>null</code>
     * @see        java.lang.Runtime#load(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void load(String filename) {
        Runtime.getRuntime().load0(Reflection.getCallerClass(), filename);
    }

    /**
     * Loads the native library specified by the <code>libname</code>
     * argument.  The <code>libname</code> argument must not contain any platform
     * specific prefix, file extension or path. If a native library
     * called <code>libname</code> is statically linked with the VM, then the
     * JNI_OnLoad_<code>libname</code> function exported by the library is invoked.
     * See the JNI Specification for more details.
     *
     * Otherwise, the libname argument is loaded from a system library
     * location and mapped to a native library image in an implementation-
     * dependent manner.
     * <p>
     * The call <code>System.loadLibrary(name)</code> is effectively
     * equivalent to the call
     * <blockquote><pre>
     * Runtime.getRuntime().loadLibrary(name)
     * </pre></blockquote>
     *
     * @param      libname   the name of the library.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkLink</code> method doesn't allow
     *             loading of the specified dynamic library
     * @exception  UnsatisfiedLinkError if either the libname argument
     *             contains a file path, the native library is not statically
     *             linked with the VM,  or the library cannot be mapped to a
     *             native library image by the host system.
     * @exception  NullPointerException if <code>libname</code> is
     *             <code>null</code>
     * @see        java.lang.Runtime#loadLibrary(java.lang.String)
     * @see        java.lang.SecurityManager#checkLink(java.lang.String)
     */
    @CallerSensitive
    public static void loadLibrary(String libname) {
        Runtime.getRuntime().loadLibrary0(Reflection.getCallerClass(), libname);
    }

    /**
     * Maps a library name into a platform-specific string representing
     * a native library.
     *
     * @param      libname the name of the library.
     * @return     a platform-dependent native library name.
     * @exception  NullPointerException if <code>libname</code> is
     *             <code>null</code>
     * @see        java.lang.System#loadLibrary(java.lang.String)
     * @see        java.lang.ClassLoader#findLibrary(java.lang.String)
     * @since      1.2
     */
    public static native String mapLibraryName(String libname);

    /**
     * Create PrintStream for stdout/err based on encoding.
     */
    private static PrintStream newPrintStream(FileOutputStream fos, String enc) {
       if (enc != null) {
            try {
                return new PrintStream(new BufferedOutputStream(fos, 128), true, enc);
            } catch (UnsupportedEncodingException uee) {}
        }
        return new PrintStream(new BufferedOutputStream(fos, 128), true);
    }


    /**
     * Initialize the system class.  Called after thread initialization.
     */
    static {
        unchangeableProps = initUnchangeableSystemProperties();
        props = initProperties();
        addLegacyLocaleSystemProperties();

        // TODO: Confirm that this isn't something super important.
        // sun.misc.VM.saveAndRemoveProperties(props);

        lineSeparator = props.getProperty("line.separator");

        FileInputStream fdIn = new FileInputStream(FileDescriptor.in);
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);
        // BEGIN Android-changed: lower buffer size.
        // in = new BufferedInputStream(fdIn);
        in = new BufferedInputStream(fdIn, 128);
        // END Android-changed: lower buffer size.
        out = newPrintStream(fdOut, props.getProperty("sun.stdout.encoding"));
        err = newPrintStream(fdErr, props.getProperty("sun.stderr.encoding"));

        // Initialize any miscellenous operating system settings that need to be
        // set for the class libraries. Currently this is no-op everywhere except
        // for Windows where the process-wide error mode is set before the java.io
        // classes are used.
        sun.misc.VM.initializeOSEnvironment();

        // Subsystems that are invoked during initialization can invoke
        // sun.misc.VM.isBooted() in order to avoid doing things that should
        // wait until the application class loader has been set up.
        // IMPORTANT: Ensure that this remains the last initialization action!
        sun.misc.VM.booted();
    }

    /**
     * @hide internal use only
     */
    public static void logE(String message) {
        log('E', message, null);
    }

    /**
     * @hide internal use only
     */
    public static void logE(String message, Throwable th) {
        log('E', message, th);
    }

    /**
     * @hide internal use only
     */
    public static void logI(String message) {
        log('I', message, null);
    }

    /**
     * @hide internal use only
     */
    public static void logI(String message, Throwable th) {
        log('I', message, th);
    }

    /**
     * @hide internal use only
     */
    public static void logW(String message) {
        log('W', message, null);
    }

    /**
     * @hide internal use only
     */
    public static void logW(String message, Throwable th) {
        log('W', message, th);
    }

    private static native void log(char type, String message, Throwable th);
}
