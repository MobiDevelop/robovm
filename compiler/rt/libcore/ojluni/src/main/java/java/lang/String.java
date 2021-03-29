// RoboVM Note: FIXME: using existing (old) implementation in rt/robovm folder
// RoboVM Note:        to reuse native code for quicker start-up
///*
// * Copyright (C) 2014 The Android Open Source Project
// * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
//package java.lang;
//
//import dalvik.annotation.optimization.FastNative;
//import java.io.ObjectStreamField;
//import java.io.UnsupportedEncodingException;
//import java.nio.charset.Charset;
//import java.nio.ByteBuffer;
//import java.util.Comparator;
//import java.util.Formatter;
//import java.util.Locale;
//import java.util.Objects;
//import java.util.StringJoiner;
//import java.util.regex.Pattern;
//import java.util.regex.PatternSyntaxException;
//
//import libcore.util.CharsetUtils;
//
///**
// * The {@code String} class represents character strings. All
// * string literals in Java programs, such as {@code "abc"}, are
// * implemented as instances of this class.
// * <p>
// * Strings are constant; their values cannot be changed after they
// * are created. String buffers support mutable strings.
// * Because String objects are immutable they can be shared. For example:
// * <blockquote><pre>
// *     String str = "abc";
// * </pre></blockquote><p>
// * is equivalent to:
// * <blockquote><pre>
// *     char data[] = {'a', 'b', 'c'};
// *     String str = new String(data);
// * </pre></blockquote><p>
// * Here are some more examples of how strings can be used:
// * <blockquote><pre>
// *     System.out.println("abc");
// *     String cde = "cde";
// *     System.out.println("abc" + cde);
// *     String c = "abc".substring(2,3);
// *     String d = cde.substring(1, 2);
// * </pre></blockquote>
// * <p>
// * The class {@code String} includes methods for examining
// * individual characters of the sequence, for comparing strings, for
// * searching strings, for extracting substrings, and for creating a
// * copy of a string with all characters translated to uppercase or to
// * lowercase. Case mapping is based on the Unicode Standard version
// * specified by the {@link java.lang.Character Character} class.
// * <p>
// * The Java language provides special support for the string
// * concatenation operator (&nbsp;+&nbsp;), and for conversion of
// * other objects to strings. String concatenation is implemented
// * through the {@code StringBuilder}(or {@code StringBuffer})
// * class and its {@code append} method.
// * String conversions are implemented through the method
// * {@code toString}, defined by {@code Object} and
// * inherited by all classes in Java. For additional information on
// * string concatenation and conversion, see Gosling, Joy, and Steele,
// * <i>The Java Language Specification</i>.
// *
// * <p> Unless otherwise noted, passing a <tt>null</tt> argument to a constructor
// * or method in this class will cause a {@link NullPointerException} to be
// * thrown.
// *
// * <p>A {@code String} represents a string in the UTF-16 format
// * in which <em>supplementary characters</em> are represented by <em>surrogate
// * pairs</em> (see the section <a href="Character.html#unicode">Unicode
// * Character Representations</a> in the {@code Character} class for
// * more information).
// * Index values refer to {@code char} code units, so a supplementary
// * character uses two positions in a {@code String}.
// * <p>The {@code String} class provides methods for dealing with
// * Unicode code points (i.e., characters), in addition to those for
// * dealing with Unicode code units (i.e., {@code char} values).
// *
// * @author  Lee Boynton
// * @author  Arthur van Hoff
// * @author  Martin Buchholz
// * @author  Ulf Zibis
// * @see     java.lang.Object#toString()
// * @see     java.lang.StringBuffer
// * @see     java.lang.StringBuilder
// * @see     java.nio.charset.Charset
// * @since   JDK1.0
// */
//
//public final class String
//    implements java.io.Serializable, Comparable<String>, CharSequence {
//
//    // BEGIN Android-changed: The character data is managed by the runtime.
//    // We only keep track of the length here and compression here. This has several consequences
//    // throughout this class:
//    //  - References to value[i] are replaced by charAt(i).
//    //  - References to value.length are replaced by calls to length().
//    //  - Sometimes the result of length() is assigned to a local variable to avoid repeated calls.
//    //  - We skip several attempts at optimization where the values field was assigned to a local
//    //    variable to avoid the getfield opcode.
//    // These changes are not all marked individually.
//    //
//    // private final char value[];
//    //
//    // If STRING_COMPRESSION_ENABLED, count stores the length shifted one bit to the left with the
//    // lowest bit used to indicate whether or not the bytes are compressed (see GetFlaggedCount in
//    // the native code).
//    private final int count;
//    // END Android-changed: The character data is managed by the runtime.
//
//    // Android-changed: We make use of new StringIndexOutOfBoundsException constructor signatures.
//    // These improve some error messages. These changes are not all marked individually.
//
//    /** Cache the hash code for the string */
//    private int hash; // Default to 0
//
//    /** use serialVersionUID from JDK 1.0.2 for interoperability */
//    private static final long serialVersionUID = -6849794470754667710L;
//
//    /**
//     * Class String is special cased within the Serialization Stream Protocol.
//     *
//     * A String instance is written into an ObjectOutputStream according to
//     * <a href="https://docs.oracle.com/javase/8/docs/platform/serialization/spec/output.html">
//     * Object Serialization Specification, Section 6.2, "Stream Elements"</a>
//     */
//    private static final ObjectStreamField[] serialPersistentFields =
//        new ObjectStreamField[0];
//
//    /**
//     * Initializes a newly created {@code String} object so that it represents
//     * an empty character sequence.  Note that use of this constructor is
//     * unnecessary since Strings are immutable.
//     */
//    public String() {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Initializes a newly created {@code String} object so that it represents
//     * the same sequence of characters as the argument; in other words, the
//     * newly created string is a copy of the argument string. Unless an
//     * explicit copy of {@code original} is needed, use of this constructor is
//     * unnecessary since Strings are immutable.
//     *
//     * @param  original
//     *         A {@code String}
//     */
//    public String(String original) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new {@code String} so that it represents the sequence of
//     * characters currently contained in the character array argument. The
//     * contents of the character array are copied; subsequent modification of
//     * the character array does not affect the newly created string.
//     *
//     * @param  value
//     *         The initial value of the string
//     */
//    public String(char value[]) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new {@code String} that contains characters from a subarray
//     * of the character array argument. The {@code offset} argument is the
//     * index of the first character of the subarray and the {@code count}
//     * argument specifies the length of the subarray. The contents of the
//     * subarray are copied; subsequent modification of the character array does
//     * not affect the newly created string.
//     *
//     * @param  value
//     *         Array that is the source of characters
//     *
//     * @param  offset
//     *         The initial offset
//     *
//     * @param  count
//     *         The length
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} and {@code count} arguments index
//     *          characters outside the bounds of the {@code value} array
//     */
//    public String(char value[], int offset, int count) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new {@code String} that contains characters from a subarray
//     * of the <a href="Character.html#unicode">Unicode code point</a> array
//     * argument.  The {@code offset} argument is the index of the first code
//     * point of the subarray and the {@code count} argument specifies the
//     * length of the subarray.  The contents of the subarray are converted to
//     * {@code char}s; subsequent modification of the {@code int} array does not
//     * affect the newly created string.
//     *
//     * @param  codePoints
//     *         Array that is the source of Unicode code points
//     *
//     * @param  offset
//     *         The initial offset
//     *
//     * @param  count
//     *         The length
//     *
//     * @throws  IllegalArgumentException
//     *          If any invalid Unicode code point is found in {@code
//     *          codePoints}
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} and {@code count} arguments index
//     *          characters outside the bounds of the {@code codePoints} array
//     *
//     * @since  1.5
//     */
//    public String(int[] codePoints, int offset, int count) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new {@code String} constructed from a subarray of an array
//     * of 8-bit integer values.
//     *
//     * <p> The {@code offset} argument is the index of the first byte of the
//     * subarray, and the {@code count} argument specifies the length of the
//     * subarray.
//     *
//     * <p> Each {@code byte} in the subarray is converted to a {@code char} as
//     * specified in the method above.
//     *
//     * @deprecated This method does not properly convert bytes into characters.
//     * As of JDK&nbsp;1.1, the preferred way to do this is via the
//     * {@code String} constructors that take a {@link
//     * java.nio.charset.Charset}, charset name, or that use the platform's
//     * default charset.
//     *
//     * @param  ascii
//     *         The bytes to be converted to characters
//     *
//     * @param  hibyte
//     *         The top 8 bits of each 16-bit Unicode code unit
//     *
//     * @param  offset
//     *         The initial offset
//     * @param  count
//     *         The length
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} or {@code count} argument is invalid
//     *
//     * @see  #String(byte[], int)
//     * @see  #String(byte[], int, int, java.lang.String)
//     * @see  #String(byte[], int, int, java.nio.charset.Charset)
//     * @see  #String(byte[], int, int)
//     * @see  #String(byte[], java.lang.String)
//     * @see  #String(byte[], java.nio.charset.Charset)
//     * @see  #String(byte[])
//     */
//    @Deprecated
//    public String(byte ascii[], int hibyte, int offset, int count) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new {@code String} containing characters constructed from
//     * an array of 8-bit integer values. Each character <i>c</i>in the
//     * resulting string is constructed from the corresponding component
//     * <i>b</i> in the byte array such that:
//     *
//     * <blockquote><pre>
//     *     <b><i>c</i></b> == (char)(((hibyte &amp; 0xff) &lt;&lt; 8)
//     *                         | (<b><i>b</i></b> &amp; 0xff))
//     * </pre></blockquote>
//     *
//     * @deprecated  This method does not properly convert bytes into
//     * characters.  As of JDK&nbsp;1.1, the preferred way to do this is via the
//     * {@code String} constructors that take a {@link
//     * java.nio.charset.Charset}, charset name, or that use the platform's
//     * default charset.
//     *
//     * @param  ascii
//     *         The bytes to be converted to characters
//     *
//     * @param  hibyte
//     *         The top 8 bits of each 16-bit Unicode code unit
//     *
//     * @see  #String(byte[], int, int, java.lang.String)
//     * @see  #String(byte[], int, int, java.nio.charset.Charset)
//     * @see  #String(byte[], int, int)
//     * @see  #String(byte[], java.lang.String)
//     * @see  #String(byte[], java.nio.charset.Charset)
//     * @see  #String(byte[])
//     */
//    @Deprecated
//    public String(byte ascii[], int hibyte) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified subarray of
//     * bytes using the specified charset.  The length of the new {@code String}
//     * is a function of the charset, and hence may not be equal to the length
//     * of the subarray.
//     *
//     * <p> The behavior of this constructor when the given bytes are not valid
//     * in the given charset is unspecified.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @param  offset
//     *         The index of the first byte to decode
//     *
//     * @param  length
//     *         The number of bytes to decode
//
//     * @param  charsetName
//     *         The name of a supported {@linkplain java.nio.charset.Charset
//     *         charset}
//     *
//     * @throws  UnsupportedEncodingException
//     *          If the named charset is not supported
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} and {@code length} arguments index
//     *          characters outside the bounds of the {@code bytes} array
//     *
//     * @since  JDK1.1
//     */
//    public String(byte bytes[], int offset, int length, String charsetName)
//            throws UnsupportedEncodingException {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified subarray of
//     * bytes using the specified {@linkplain java.nio.charset.Charset charset}.
//     * The length of the new {@code String} is a function of the charset, and
//     * hence may not be equal to the length of the subarray.
//     *
//     * <p> This method always replaces malformed-input and unmappable-character
//     * sequences with this charset's default replacement string.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @param  offset
//     *         The index of the first byte to decode
//     *
//     * @param  length
//     *         The number of bytes to decode
//     *
//     * @param  charset
//     *         The {@linkplain java.nio.charset.Charset charset} to be used to
//     *         decode the {@code bytes}
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} and {@code length} arguments index
//     *          characters outside the bounds of the {@code bytes} array
//     *
//     * @since  1.6
//     */
//    public String(byte bytes[], int offset, int length, Charset charset) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified array of bytes
//     * using the specified {@linkplain java.nio.charset.Charset charset}.  The
//     * length of the new {@code String} is a function of the charset, and hence
//     * may not be equal to the length of the byte array.
//     *
//     * <p> The behavior of this constructor when the given bytes are not valid
//     * in the given charset is unspecified.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @param  charsetName
//     *         The name of a supported {@linkplain java.nio.charset.Charset
//     *         charset}
//     *
//     * @throws  UnsupportedEncodingException
//     *          If the named charset is not supported
//     *
//     * @since  JDK1.1
//     */
//    public String(byte bytes[], String charsetName)
//            throws UnsupportedEncodingException {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified array of
//     * bytes using the specified {@linkplain java.nio.charset.Charset charset}.
//     * The length of the new {@code String} is a function of the charset, and
//     * hence may not be equal to the length of the byte array.
//     *
//     * <p> This method always replaces malformed-input and unmappable-character
//     * sequences with this charset's default replacement string.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @param  charset
//     *         The {@linkplain java.nio.charset.Charset charset} to be used to
//     *         decode the {@code bytes}
//     *
//     * @since  1.6
//     */
//    public String(byte bytes[], Charset charset) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified subarray of
//     * bytes using the platform's default charset.  The length of the new
//     * {@code String} is a function of the charset, and hence may not be equal
//     * to the length of the subarray.
//     *
//     * <p> The behavior of this constructor when the given bytes are not valid
//     * in the default charset is unspecified.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @param  offset
//     *         The index of the first byte to decode
//     *
//     * @param  length
//     *         The number of bytes to decode
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If the {@code offset} and the {@code length} arguments index
//     *          characters outside the bounds of the {@code bytes} array
//     *
//     * @since  JDK1.1
//     */
//    public String(byte bytes[], int offset, int length) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Constructs a new {@code String} by decoding the specified array of bytes
//     * using the platform's default charset.  The length of the new {@code
//     * String} is a function of the charset, and hence may not be equal to the
//     * length of the byte array.
//     *
//     * <p> The behavior of this constructor when the given bytes are not valid
//     * in the default charset is unspecified.  The {@link
//     * java.nio.charset.CharsetDecoder} class should be used when more control
//     * over the decoding process is required.
//     *
//     * @param  bytes
//     *         The bytes to be decoded into characters
//     *
//     * @since  JDK1.1
//     */
//    public String(byte bytes[]) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new string that contains the sequence of characters
//     * currently contained in the string buffer argument. The contents of the
//     * string buffer are copied; subsequent modification of the string buffer
//     * does not affect the newly created string.
//     *
//     * @param  buffer
//     *         A {@code StringBuffer}
//     */
//    public String(StringBuffer buffer) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    /**
//     * Allocates a new string that contains the sequence of characters
//     * currently contained in the string builder argument. The contents of the
//     * string builder are copied; subsequent modification of the string builder
//     * does not affect the newly created string.
//     *
//     * <p> This constructor is provided to ease migration to {@code
//     * StringBuilder}. Obtaining a string from a string builder via the {@code
//     * toString} method is likely to run faster and is generally preferred.
//     *
//     * @param   builder
//     *          A {@code StringBuilder}
//     *
//     * @since  1.5
//     */
//    public String(StringBuilder builder) {
//        // Android-changed: Constructor unsupported as all calls are intercepted by the runtime.
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//
//    // Android-removed: Unused package-private constructor String(char[] value, boolean share).
//
//    // BEGIN Android-added: Constructor for internal use.
//    // Not implemented in java as all calls are intercepted by the runtime.
//    /**
//     * Package private constructor
//     *
//     * @deprecated Use {@link #String(char[],int,int)} instead.
//     */
//    @Deprecated
//    String(int offset, int count, char[] value) {
//        throw new UnsupportedOperationException("Use StringFactory instead.");
//    }
//    // END Android-added: Constructor for internal use.
//
//    /**
//     * Returns the length of this string.
//     * The length is equal to the number of <a href="Character.html#unicode">Unicode
//     * code units</a> in the string.
//     *
//     * @return  the length of the sequence of characters represented by this
//     *          object.
//     */
//    public int length() {
//        // BEGIN Android-changed: Get length from count field rather than value array (see above).
//        // return value.length;
//        final boolean STRING_COMPRESSION_ENABLED = true;
//        if (STRING_COMPRESSION_ENABLED) {
//            // For the compression purposes (save the characters as 8-bit if all characters
//            // are ASCII), the least significant bit of "count" is used as the compression flag.
//            return (count >>> 1);
//        } else {
//            return count;
//        }
//        // END Android-changed: Get length from count field rather than value array (see above).
//    }
//
//    /**
//     * Returns {@code true} if, and only if, {@link #length()} is {@code 0}.
//     *
//     * @return {@code true} if {@link #length()} is {@code 0}, otherwise
//     * {@code false}
//     *
//     * @since 1.6
//     */
//    public boolean isEmpty() {
//        // Android-changed: Get length from count field rather than value array (see above).
//        // Empty string has {@code count == 0} with or without string compression enabled.
//        // return value.length == 0;
//        return count == 0;
//    }
//
//    /**
//     * Returns the {@code char} value at the
//     * specified index. An index ranges from {@code 0} to
//     * {@code length() - 1}. The first {@code char} value of the sequence
//     * is at index {@code 0}, the next at index {@code 1},
//     * and so on, as for array indexing.
//     *
//     * <p>If the {@code char} value specified by the index is a
//     * <a href="Character.html#unicode">surrogate</a>, the surrogate
//     * value is returned.
//     *
//     * @param      index   the index of the {@code char} value.
//     * @return     the {@code char} value at the specified index of this string.
//     *             The first {@code char} value is at index {@code 0}.
//     * @exception  IndexOutOfBoundsException  if the {@code index}
//     *             argument is negative or not less than the length of this
//     *             string.
//     */
//    // BEGIN Android-changed: Replace with implementation in runtime to access chars (see above).
//    @FastNative
//    public native char charAt(int index);
//    // END Android-changed: Replace with implementation in runtime to access chars (see above).
//
//    /**
//     * Returns the character (Unicode code point) at the specified
//     * index. The index refers to {@code char} values
//     * (Unicode code units) and ranges from {@code 0} to
//     * {@link #length()}{@code  - 1}.
//     *
//     * <p> If the {@code char} value specified at the given index
//     * is in the high-surrogate range, the following index is less
//     * than the length of this {@code String}, and the
//     * {@code char} value at the following index is in the
//     * low-surrogate range, then the supplementary code point
//     * corresponding to this surrogate pair is returned. Otherwise,
//     * the {@code char} value at the given index is returned.
//     *
//     * @param      index the index to the {@code char} values
//     * @return     the code point value of the character at the
//     *             {@code index}
//     * @exception  IndexOutOfBoundsException  if the {@code index}
//     *             argument is negative or not less than the length of this
//     *             string.
//     * @since      1.5
//     */
//    public int codePointAt(int index) {
//        if ((index < 0) || (index >= length())) {
//            throw new StringIndexOutOfBoundsException(index);
//        }
//        // Android-changed: Skip codePointAtImpl optimization that needs access to java chars.
//        return Character.codePointAt(this, index);
//    }
//
//    /**
//     * Returns the character (Unicode code point) before the specified
//     * index. The index refers to {@code char} values
//     * (Unicode code units) and ranges from {@code 1} to {@link
//     * CharSequence#length() length}.
//     *
//     * <p> If the {@code char} value at {@code (index - 1)}
//     * is in the low-surrogate range, {@code (index - 2)} is not
//     * negative, and the {@code char} value at {@code (index -
//     * 2)} is in the high-surrogate range, then the
//     * supplementary code point value of the surrogate pair is
//     * returned. If the {@code char} value at {@code index -
//     * 1} is an unpaired low-surrogate or a high-surrogate, the
//     * surrogate value is returned.
//     *
//     * @param     index the index following the code point that should be returned
//     * @return    the Unicode code point value before the given index.
//     * @exception IndexOutOfBoundsException if the {@code index}
//     *            argument is less than 1 or greater than the length
//     *            of this string.
//     * @since     1.5
//     */
//    public int codePointBefore(int index) {
//        int i = index - 1;
//        if ((i < 0) || (i >= length())) {
//            throw new StringIndexOutOfBoundsException(index);
//        }
//        // Android-changed: Skip codePointBeforeImpl optimization that needs access to java chars.
//        return Character.codePointBefore(this, index);
//    }
//
//    /**
//     * Returns the number of Unicode code points in the specified text
//     * range of this {@code String}. The text range begins at the
//     * specified {@code beginIndex} and extends to the
//     * {@code char} at index {@code endIndex - 1}. Thus the
//     * length (in {@code char}s) of the text range is
//     * {@code endIndex-beginIndex}. Unpaired surrogates within
//     * the text range count as one code point each.
//     *
//     * @param beginIndex the index to the first {@code char} of
//     * the text range.
//     * @param endIndex the index after the last {@code char} of
//     * the text range.
//     * @return the number of Unicode code points in the specified text
//     * range
//     * @exception IndexOutOfBoundsException if the
//     * {@code beginIndex} is negative, or {@code endIndex}
//     * is larger than the length of this {@code String}, or
//     * {@code beginIndex} is larger than {@code endIndex}.
//     * @since  1.5
//     */
//    public int codePointCount(int beginIndex, int endIndex) {
//        if (beginIndex < 0 || endIndex > length() || beginIndex > endIndex) {
//            throw new IndexOutOfBoundsException();
//        }
//        // Android-changed: Skip codePointCountImpl optimization that needs access to java chars.
//        return Character.codePointCount(this, beginIndex, endIndex);
//    }
//
//    /**
//     * Returns the index within this {@code String} that is
//     * offset from the given {@code index} by
//     * {@code codePointOffset} code points. Unpaired surrogates
//     * within the text range given by {@code index} and
//     * {@code codePointOffset} count as one code point each.
//     *
//     * @param index the index to be offset
//     * @param codePointOffset the offset in code points
//     * @return the index within this {@code String}
//     * @exception IndexOutOfBoundsException if {@code index}
//     *   is negative or larger then the length of this
//     *   {@code String}, or if {@code codePointOffset} is positive
//     *   and the substring starting with {@code index} has fewer
//     *   than {@code codePointOffset} code points,
//     *   or if {@code codePointOffset} is negative and the substring
//     *   before {@code index} has fewer than the absolute value
//     *   of {@code codePointOffset} code points.
//     * @since 1.5
//     */
//    public int offsetByCodePoints(int index, int codePointOffset) {
//        if (index < 0 || index > length()) {
//            throw new IndexOutOfBoundsException();
//        }
//        // Android-changed: Skip offsetByCodePointsImpl optimization that needs access to java chars
//        return Character.offsetByCodePoints(this, index, codePointOffset);
//    }
//
//    /**
//     * Copy characters from this string into dst starting at dstBegin.
//     * This method doesn't perform any range checking.
//     */
//    void getChars(char dst[], int dstBegin) {
//        // Android-changed: Replace arraycopy with native call since chars are managed by runtime.
//        getCharsNoCheck(0, length(), dst, dstBegin);
//    }
//
//    /**
//     * Copies characters from this string into the destination character
//     * array.
//     * <p>
//     * The first character to be copied is at index {@code srcBegin};
//     * the last character to be copied is at index {@code srcEnd-1}
//     * (thus the total number of characters to be copied is
//     * {@code srcEnd-srcBegin}). The characters are copied into the
//     * subarray of {@code dst} starting at index {@code dstBegin}
//     * and ending at index:
//     * <blockquote><pre>
//     *     dstBegin + (srcEnd-srcBegin) - 1
//     * </pre></blockquote>
//     *
//     * @param      srcBegin   index of the first character in the string
//     *                        to copy.
//     * @param      srcEnd     index after the last character in the string
//     *                        to copy.
//     * @param      dst        the destination array.
//     * @param      dstBegin   the start offset in the destination array.
//     * @exception IndexOutOfBoundsException If any of the following
//     *            is true:
//     *            <ul><li>{@code srcBegin} is negative.
//     *            <li>{@code srcBegin} is greater than {@code srcEnd}
//     *            <li>{@code srcEnd} is greater than the length of this
//     *                string
//     *            <li>{@code dstBegin} is negative
//     *            <li>{@code dstBegin+(srcEnd-srcBegin)} is larger than
//     *                {@code dst.length}</ul>
//     */
//    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
//        // BEGIN Android-changed: Implement in terms of length() and native getCharsNoCheck method.
//        if (dst == null) {
//            throw new NullPointerException("dst == null");
//        }
//
//        if (srcBegin < 0) {
//            throw new StringIndexOutOfBoundsException(this, srcBegin);
//        }
//        if (srcEnd > length()) {
//            throw new StringIndexOutOfBoundsException(this, srcEnd);
//        }
//
//        int n = srcEnd - srcBegin;
//        if (srcEnd < srcBegin) {
//            throw new StringIndexOutOfBoundsException(this, srcBegin, n);
//        }
//
//        if (dstBegin < 0) {
//            throw new ArrayIndexOutOfBoundsException("dstBegin < 0. dstBegin=" + dstBegin);
//        }
//        // dstBegin can be equal to dst.length, but only in the case where zero chars are to be
//        // copied.
//        if (dstBegin > dst.length) {
//            throw new ArrayIndexOutOfBoundsException(
//                    "dstBegin > dst.length. dstBegin=" + dstBegin + ", dst.length=" + dst.length);
//        }
//        if (n > dst.length - dstBegin) {
//            throw new ArrayIndexOutOfBoundsException(
//                    "n > dst.length - dstBegin. n=" + n + ", dst.length=" + dst.length
//                            + "dstBegin=" + dstBegin);
//        }
//
//        getCharsNoCheck(srcBegin, srcEnd, dst, dstBegin);
//        // END Android-changed: Implement in terms of length() and native getCharsNoCheck method.
//    }
//
//    // BEGIN Android-added: Native method to access char storage managed by runtime.
//    /**
//     * getChars without bounds checks, for use by other classes
//     * within the java.lang package only.  The caller is responsible for
//     * ensuring that start >= 0 && start <= end && end <= count.
//     */
//    @FastNative
//    native void getCharsNoCheck(int start, int end, char[] buffer, int index);
//    // END Android-added: Native method to access char storage managed by runtime.
//
//    /**
//     * Copies characters from this string into the destination byte array. Each
//     * byte receives the 8 low-order bits of the corresponding character. The
//     * eight high-order bits of each character are not copied and do not
//     * participate in the transfer in any way.
//     *
//     * <p> The first character to be copied is at index {@code srcBegin}; the
//     * last character to be copied is at index {@code srcEnd-1}.  The total
//     * number of characters to be copied is {@code srcEnd-srcBegin}. The
//     * characters, converted to bytes, are copied into the subarray of {@code
//     * dst} starting at index {@code dstBegin} and ending at index:
//     *
//     * <blockquote><pre>
//     *     dstBegin + (srcEnd-srcBegin) - 1
//     * </pre></blockquote>
//     *
//     * @deprecated  This method does not properly convert characters into
//     * bytes.  As of JDK&nbsp;1.1, the preferred way to do this is via the
//     * {@link #getBytes()} method, which uses the platform's default charset.
//     *
//     * @param  srcBegin
//     *         Index of the first character in the string to copy
//     *
//     * @param  srcEnd
//     *         Index after the last character in the string to copy
//     *
//     * @param  dst
//     *         The destination array
//     *
//     * @param  dstBegin
//     *         The start offset in the destination array
//     *
//     * @throws  IndexOutOfBoundsException
//     *          If any of the following is true:
//     *          <ul>
//     *            <li> {@code srcBegin} is negative
//     *            <li> {@code srcBegin} is greater than {@code srcEnd}
//     *            <li> {@code srcEnd} is greater than the length of this String
//     *            <li> {@code dstBegin} is negative
//     *            <li> {@code dstBegin+(srcEnd-srcBegin)} is larger than {@code
//     *                 dst.length}
//     *          </ul>
//     */
//    @Deprecated
//    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
//        if (srcBegin < 0) {
//            throw new StringIndexOutOfBoundsException(this, srcBegin);
//        }
//        if (srcEnd > length()) {
//            throw new StringIndexOutOfBoundsException(this, srcEnd);
//        }
//        if (srcBegin > srcEnd) {
//            throw new StringIndexOutOfBoundsException(this, srcEnd - srcBegin);
//        }
//
//        int j = dstBegin;
//        int n = srcEnd;
//        int i = srcBegin;
//
//        while (i < n) {
//            dst[j++] = (byte)charAt(i++);
//        }
//    }
//
//    /**
//     * Encodes this {@code String} into a sequence of bytes using the named
//     * charset, storing the result into a new byte array.
//     *
//     * <p> The behavior of this method when this string cannot be encoded in
//     * the given charset is unspecified.  The {@link
//     * java.nio.charset.CharsetEncoder} class should be used when more control
//     * over the encoding process is required.
//     *
//     * @param  charsetName
//     *         The name of a supported {@linkplain java.nio.charset.Charset
//     *         charset}
//     *
//     * @return  The resultant byte array
//     *
//     * @throws  UnsupportedEncodingException
//     *          If the named charset is not supported
//     *
//     * @since  JDK1.1
//     */
//    public byte[] getBytes(String charsetName)
//            throws UnsupportedEncodingException {
//        if (charsetName == null) throw new NullPointerException();
//        // Android-changed: Skip StringCoding optimization that needs access to java chars.
//        // return StringCoding.encode(charsetName, value, 0, value.length);
//        return getBytes(Charset.forNameUEE(charsetName));
//    }
//
//    /**
//     * Encodes this {@code String} into a sequence of bytes using the given
//     * {@linkplain java.nio.charset.Charset charset}, storing the result into a
//     * new byte array.
//     *
//     * <p> This method always replaces malformed-input and unmappable-character
//     * sequences with this charset's default replacement byte array.  The
//     * {@link java.nio.charset.CharsetEncoder} class should be used when more
//     * control over the encoding process is required.
//     *
//     * @param  charset
//     *         The {@linkplain java.nio.charset.Charset} to be used to encode
//     *         the {@code String}
//     *
//     * @return  The resultant byte array
//     *
//     * @since  1.6
//     */
//    public byte[] getBytes(Charset charset) {
//        // BEGIN Android-changed: Skip StringCoding optimization that needs access to java chars.
//        // if (charset == null) throw new NullPointerException();
//        // return StringCoding.encode(charset, value, 0, value.length);
//        if (charset == null) {
//            throw new NullPointerException("charset == null");
//        }
//
//        final int len = length();
//        final String name = charset.name();
//        if ("UTF-8".equals(name)) {
//            return CharsetUtils.toUtf8Bytes(this, 0, len);
//        } else if ("ISO-8859-1".equals(name)) {
//            return CharsetUtils.toIsoLatin1Bytes(this, 0, len);
//        } else if ("US-ASCII".equals(name)) {
//            return CharsetUtils.toAsciiBytes(this, 0, len);
//        } else if ("UTF-16BE".equals(name)) {
//            return CharsetUtils.toBigEndianUtf16Bytes(this, 0, len);
//        }
//
//        ByteBuffer buffer = charset.encode(this);
//        byte[] bytes = new byte[buffer.limit()];
//        buffer.get(bytes);
//        return bytes;
//        // END Android-changed: Skip StringCoding optimization that needs access to java chars.
//    }
//
//    /**
//     * Encodes this {@code String} into a sequence of bytes using the
//     * platform's default charset, storing the result into a new byte array.
//     *
//     * <p> The behavior of this method when this string cannot be encoded in
//     * the default charset is unspecified.  The {@link
//     * java.nio.charset.CharsetEncoder} class should be used when more control
//     * over the encoding process is required.
//     *
//     * @return  The resultant byte array
//     *
//     * @since      JDK1.1
//     */
//    public byte[] getBytes() {
//        // Android-changed: Skip StringCoding optimization that needs access to java chars.
//        // return StringCoding.encode(value, 0, value.length);
//        return getBytes(Charset.defaultCharset());
//    }
//
//    /**
//     * Compares this string to the specified object.  The result is {@code
//     * true} if and only if the argument is not {@code null} and is a {@code
//     * String} object that represents the same sequence of characters as this
//     * object.
//     *
//     * @param  anObject
//     *         The object to compare this {@code String} against
//     *
//     * @return  {@code true} if the given object represents a {@code String}
//     *          equivalent to this string, {@code false} otherwise
//     *
//     * @see  #compareTo(String)
//     * @see  #equalsIgnoreCase(String)
//     */
//    public boolean equals(Object anObject) {
//        if (this == anObject) {
//            return true;
//        }
//        if (anObject instanceof String) {
//            String anotherString = (String)anObject;
//            int n = length();
//            if (n == anotherString.length()) {
//                int i = 0;
//                while (n-- != 0) {
//                    if (charAt(i) != anotherString.charAt(i))
//                            return false;
//                    i++;
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Compares this string to the specified {@code StringBuffer}.  The result
//     * is {@code true} if and only if this {@code String} represents the same
//     * sequence of characters as the specified {@code StringBuffer}. This method
//     * synchronizes on the {@code StringBuffer}.
//     *
//     * @param  sb
//     *         The {@code StringBuffer} to compare this {@code String} against
//     *
//     * @return  {@code true} if this {@code String} represents the same
//     *          sequence of characters as the specified {@code StringBuffer},
//     *          {@code false} otherwise
//     *
//     * @since  1.4
//     */
//    public boolean contentEquals(StringBuffer sb) {
//        return contentEquals((CharSequence)sb);
//    }
//
//    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
//        char v2[] = sb.getValue();
//        int n = length();
//        if (n != sb.length()) {
//            return false;
//        }
//        for (int i = 0; i < n; i++) {
//            if (charAt(i) != v2[i]) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Compares this string to the specified {@code CharSequence}.  The
//     * result is {@code true} if and only if this {@code String} represents the
//     * same sequence of char values as the specified sequence. Note that if the
//     * {@code CharSequence} is a {@code StringBuffer} then the method
//     * synchronizes on it.
//     *
//     * @param  cs
//     *         The sequence to compare this {@code String} against
//     *
//     * @return  {@code true} if this {@code String} represents the same
//     *          sequence of char values as the specified sequence, {@code
//     *          false} otherwise
//     *
//     * @since  1.5
//     */
//    public boolean contentEquals(CharSequence cs) {
//        // Argument is a StringBuffer, StringBuilder
//        if (cs instanceof AbstractStringBuilder) {
//            if (cs instanceof StringBuffer) {
//                synchronized(cs) {
//                   return nonSyncContentEquals((AbstractStringBuilder)cs);
//                }
//            } else {
//                return nonSyncContentEquals((AbstractStringBuilder)cs);
//            }
//        }
//        // Argument is a String
//        if (cs instanceof String) {
//            return equals(cs);
//        }
//        // Argument is a generic CharSequence
//        int n = length();
//        if (n != cs.length()) {
//            return false;
//        }
//        for (int i = 0; i < n; i++) {
//            if (charAt(i) != cs.charAt(i)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Compares this {@code String} to another {@code String}, ignoring case
//     * considerations.  Two strings are considered equal ignoring case if they
//     * are of the same length and corresponding characters in the two strings
//     * are equal ignoring case.
//     *
//     * <p> Two characters {@code c1} and {@code c2} are considered the same
//     * ignoring case if at least one of the following is true:
//     * <ul>
//     *   <li> The two characters are the same (as compared by the
//     *        {@code ==} operator)
//     *   <li> Applying the method {@link
//     *        java.lang.Character#toUpperCase(char)} to each character
//     *        produces the same result
//     *   <li> Applying the method {@link
//     *        java.lang.Character#toLowerCase(char)} to each character
//     *        produces the same result
//     * </ul>
//     *
//     * @param  anotherString
//     *         The {@code String} to compare this {@code String} against
//     *
//     * @return  {@code true} if the argument is not {@code null} and it
//     *          represents an equivalent {@code String} ignoring case; {@code
//     *          false} otherwise
//     *
//     * @see  #equals(Object)
//     */
//    public boolean equalsIgnoreCase(String anotherString) {
//        final int len = length();
//        return (this == anotherString) ? true
//                : (anotherString != null)
//                && (anotherString.length() == len)
//                && regionMatches(true, 0, anotherString, 0, len);
//    }
//
//    /**
//     * Compares two strings lexicographically.
//     * The comparison is based on the Unicode value of each character in
//     * the strings. The character sequence represented by this
//     * {@code String} object is compared lexicographically to the
//     * character sequence represented by the argument string. The result is
//     * a negative integer if this {@code String} object
//     * lexicographically precedes the argument string. The result is a
//     * positive integer if this {@code String} object lexicographically
//     * follows the argument string. The result is zero if the strings
//     * are equal; {@code compareTo} returns {@code 0} exactly when
//     * the {@link #equals(Object)} method would return {@code true}.
//     * <p>
//     * This is the definition of lexicographic ordering. If two strings are
//     * different, then either they have different characters at some index
//     * that is a valid index for both strings, or their lengths are different,
//     * or both. If they have different characters at one or more index
//     * positions, let <i>k</i> be the smallest such index; then the string
//     * whose character at position <i>k</i> has the smaller value, as
//     * determined by using the &lt; operator, lexicographically precedes the
//     * other string. In this case, {@code compareTo} returns the
//     * difference of the two character values at position {@code k} in
//     * the two string -- that is, the value:
//     * <blockquote><pre>
//     * this.charAt(k)-anotherString.charAt(k)
//     * </pre></blockquote>
//     * If there is no index position at which they differ, then the shorter
//     * string lexicographically precedes the longer string. In this case,
//     * {@code compareTo} returns the difference of the lengths of the
//     * strings -- that is, the value:
//     * <blockquote><pre>
//     * this.length()-anotherString.length()
//     * </pre></blockquote>
//     *
//     * @param   anotherString   the {@code String} to be compared.
//     * @return  the value {@code 0} if the argument string is equal to
//     *          this string; a value less than {@code 0} if this string
//     *          is lexicographically less than the string argument; and a
//     *          value greater than {@code 0} if this string is
//     *          lexicographically greater than the string argument.
//     */
//    // BEGIN Android-changed: Replace with implementation in runtime to access chars (see above).
//    @FastNative
//    public native int compareTo(String anotherString);
//    // END Android-changed: Replace with implementation in runtime to access chars (see above).
//
//    /**
//     * A Comparator that orders {@code String} objects as by
//     * {@code compareToIgnoreCase}. This comparator is serializable.
//     * <p>
//     * Note that this Comparator does <em>not</em> take locale into account,
//     * and will result in an unsatisfactory ordering for certain locales.
//     * The java.text package provides <em>Collators</em> to allow
//     * locale-sensitive ordering.
//     *
//     * @see     java.text.Collator#compare(String, String)
//     * @since   1.2
//     */
//    public static final Comparator<String> CASE_INSENSITIVE_ORDER
//                                         = new CaseInsensitiveComparator();
//    private static class CaseInsensitiveComparator
//            implements Comparator<String>, java.io.Serializable {
//        // use serialVersionUID from JDK 1.2.2 for interoperability
//        private static final long serialVersionUID = 8575799808933029326L;
//
//        public int compare(String s1, String s2) {
//            int n1 = s1.length();
//            int n2 = s2.length();
//            int min = Math.min(n1, n2);
//            for (int i = 0; i < min; i++) {
//                char c1 = s1.charAt(i);
//                char c2 = s2.charAt(i);
//                if (c1 != c2) {
//                    c1 = Character.toUpperCase(c1);
//                    c2 = Character.toUpperCase(c2);
//                    if (c1 != c2) {
//                        c1 = Character.toLowerCase(c1);
//                        c2 = Character.toLowerCase(c2);
//                        if (c1 != c2) {
//                            // No overflow because of numeric promotion
//                            return c1 - c2;
//                        }
//                    }
//                }
//            }
//            return n1 - n2;
//        }
//
//        /** Replaces the de-serialized object. */
//        private Object readResolve() { return CASE_INSENSITIVE_ORDER; }
//    }
//
//    /**
//     * Compares two strings lexicographically, ignoring case
//     * differences. This method returns an integer whose sign is that of
//     * calling {@code compareTo} with normalized versions of the strings
//     * where case differences have been eliminated by calling
//     * {@code Character.toLowerCase(Character.toUpperCase(character))} on
//     * each character.
//     * <p>
//     * Note that this method does <em>not</em> take locale into account,
//     * and will result in an unsatisfactory ordering for certain locales.
//     * The java.text package provides <em>collators</em> to allow
//     * locale-sensitive ordering.
//     *
//     * @param   str   the {@code String} to be compared.
//     * @return  a negative integer, zero, or a positive integer as the
//     *          specified String is greater than, equal to, or less
//     *          than this String, ignoring case considerations.
//     * @see     java.text.Collator#compare(String, String)
//     * @since   1.2
//     */
//    public int compareToIgnoreCase(String str) {
//        return CASE_INSENSITIVE_ORDER.compare(this, str);
//    }
//
//    /**
//     * Tests if two string regions are equal.
//     * <p>
//     * A substring of this {@code String} object is compared to a substring
//     * of the argument other. The result is true if these substrings
//     * represent identical character sequences. The substring of this
//     * {@code String} object to be compared begins at index {@code toffset}
//     * and has length {@code len}. The substring of other to be compared
//     * begins at index {@code ooffset} and has length {@code len}. The
//     * result is {@code false} if and only if at least one of the following
//     * is true:
//     * <ul><li>{@code toffset} is negative.
//     * <li>{@code ooffset} is negative.
//     * <li>{@code toffset+len} is greater than the length of this
//     * {@code String} object.
//     * <li>{@code ooffset+len} is greater than the length of the other
//     * argument.
//     * <li>There is some nonnegative integer <i>k</i> less than {@code len}
//     * such that:
//     * {@code this.charAt(toffset + }<i>k</i>{@code ) != other.charAt(ooffset + }
//     * <i>k</i>{@code )}
//     * </ul>
//     *
//     * @param   toffset   the starting offset of the subregion in this string.
//     * @param   other     the string argument.
//     * @param   ooffset   the starting offset of the subregion in the string
//     *                    argument.
//     * @param   len       the number of characters to compare.
//     * @return  {@code true} if the specified subregion of this string
//     *          exactly matches the specified subregion of the string argument;
//     *          {@code false} otherwise.
//     */
//    public boolean regionMatches(int toffset, String other, int ooffset,
//            int len) {
//        int to = toffset;
//        int po = ooffset;
//        // Note: toffset, ooffset, or len might be near -1>>>1.
//        if ((ooffset < 0) || (toffset < 0)
//                || (toffset > (long)length() - len)
//                || (ooffset > (long)other.length() - len)) {
//            return false;
//        }
//        while (len-- > 0) {
//            if (charAt(to++) != other.charAt(po++)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Tests if two string regions are equal.
//     * <p>
//     * A substring of this {@code String} object is compared to a substring
//     * of the argument {@code other}. The result is {@code true} if these
//     * substrings represent character sequences that are the same, ignoring
//     * case if and only if {@code ignoreCase} is true. The substring of
//     * this {@code String} object to be compared begins at index
//     * {@code toffset} and has length {@code len}. The substring of
//     * {@code other} to be compared begins at index {@code ooffset} and
//     * has length {@code len}. The result is {@code false} if and only if
//     * at least one of the following is true:
//     * <ul><li>{@code toffset} is negative.
//     * <li>{@code ooffset} is negative.
//     * <li>{@code toffset+len} is greater than the length of this
//     * {@code String} object.
//     * <li>{@code ooffset+len} is greater than the length of the other
//     * argument.
//     * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
//     * integer <i>k</i> less than {@code len} such that:
//     * <blockquote><pre>
//     * this.charAt(toffset+k) != other.charAt(ooffset+k)
//     * </pre></blockquote>
//     * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
//     * integer <i>k</i> less than {@code len} such that:
//     * <blockquote><pre>
//     * Character.toLowerCase(this.charAt(toffset+k)) !=
//     Character.toLowerCase(other.charAt(ooffset+k))
//     * </pre></blockquote>
//     * and:
//     * <blockquote><pre>
//     * Character.toUpperCase(this.charAt(toffset+k)) !=
//     *         Character.toUpperCase(other.charAt(ooffset+k))
//     * </pre></blockquote>
//     * </ul>
//     *
//     * @param   ignoreCase   if {@code true}, ignore case when comparing
//     *                       characters.
//     * @param   toffset      the starting offset of the subregion in this
//     *                       string.
//     * @param   other        the string argument.
//     * @param   ooffset      the starting offset of the subregion in the string
//     *                       argument.
//     * @param   len          the number of characters to compare.
//     * @return  {@code true} if the specified subregion of this string
//     *          matches the specified subregion of the string argument;
//     *          {@code false} otherwise. Whether the matching is exact
//     *          or case insensitive depends on the {@code ignoreCase}
//     *          argument.
//     */
//    public boolean regionMatches(boolean ignoreCase, int toffset,
//            String other, int ooffset, int len) {
//        int to = toffset;
//        int po = ooffset;
//        // Note: toffset, ooffset, or len might be near -1>>>1.
//        if ((ooffset < 0) || (toffset < 0)
//                || (toffset > (long)length() - len)
//                || (ooffset > (long)other.length() - len)) {
//            return false;
//        }
//        while (len-- > 0) {
//            char c1 = charAt(to++);
//            char c2 = other.charAt(po++);
//            if (c1 == c2) {
//                continue;
//            }
//            if (ignoreCase) {
//                // If characters don't match but case may be ignored,
//                // try converting both characters to uppercase.
//                // If the results match, then the comparison scan should
//                // continue.
//                char u1 = Character.toUpperCase(c1);
//                char u2 = Character.toUpperCase(c2);
//                if (u1 == u2) {
//                    continue;
//                }
//                // Unfortunately, conversion to uppercase does not work properly
//                // for the Georgian alphabet, which has strange rules about case
//                // conversion.  So we need to make one last check before
//                // exiting.
//                if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
//                    continue;
//                }
//            }
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Tests if the substring of this string beginning at the
//     * specified index starts with the specified prefix.
//     *
//     * @param   prefix    the prefix.
//     * @param   toffset   where to begin looking in this string.
//     * @return  {@code true} if the character sequence represented by the
//     *          argument is a prefix of the substring of this object starting
//     *          at index {@code toffset}; {@code false} otherwise.
//     *          The result is {@code false} if {@code toffset} is
//     *          negative or greater than the length of this
//     *          {@code String} object; otherwise the result is the same
//     *          as the result of the expression
//     *          <pre>
//     *          this.substring(toffset).startsWith(prefix)
//     *          </pre>
//     */
//    public boolean startsWith(String prefix, int toffset) {
//        int to = toffset;
//        int po = 0;
//        int pc = prefix.length();
//        // Note: toffset might be near -1>>>1.
//        if ((toffset < 0) || (toffset > length() - pc)) {
//            return false;
//        }
//        while (--pc >= 0) {
//            if (charAt(to++) != prefix.charAt(po++)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Tests if this string starts with the specified prefix.
//     *
//     * @param   prefix   the prefix.
//     * @return  {@code true} if the character sequence represented by the
//     *          argument is a prefix of the character sequence represented by
//     *          this string; {@code false} otherwise.
//     *          Note also that {@code true} will be returned if the
//     *          argument is an empty string or is equal to this
//     *          {@code String} object as determined by the
//     *          {@link #equals(Object)} method.
//     * @since   1. 0
//     */
//    public boolean startsWith(String prefix) {
//        return startsWith(prefix, 0);
//    }
//
//    /**
//     * Tests if this string ends with the specified suffix.
//     *
//     * @param   suffix   the suffix.
//     * @return  {@code true} if the character sequence represented by the
//     *          argument is a suffix of the character sequence represented by
//     *          this object; {@code false} otherwise. Note that the
//     *          result will be {@code true} if the argument is the
//     *          empty string or is equal to this {@code String} object
//     *          as determined by the {@link #equals(Object)} method.
//     */
//    public boolean endsWith(String suffix) {
//        return startsWith(suffix, length() - suffix.length());
//    }
//
//    /**
//     * Returns a hash code for this string. The hash code for a
//     * {@code String} object is computed as
//     * <blockquote><pre>
//     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
//     * </pre></blockquote>
//     * using {@code int} arithmetic, where {@code s[i]} is the
//     * <i>i</i>th character of the string, {@code n} is the length of
//     * the string, and {@code ^} indicates exponentiation.
//     * (The hash value of the empty string is zero.)
//     *
//     * @return  a hash code value for this object.
//     */
//    public int hashCode() {
//        int h = hash;
//        final int len = length();
//        if (h == 0 && len > 0) {
//            for (int i = 0; i < len; i++) {
//                h = 31 * h + charAt(i);
//            }
//            hash = h;
//        }
//        return h;
//    }
//
//    /**
//     * Returns the index within this string of the first occurrence of
//     * the specified character. If a character with value
//     * {@code ch} occurs in the character sequence represented by
//     * this {@code String} object, then the index (in Unicode
//     * code units) of the first such occurrence is returned. For
//     * values of {@code ch} in the range from 0 to 0xFFFF
//     * (inclusive), this is the smallest value <i>k</i> such that:
//     * <blockquote><pre>
//     * this.charAt(<i>k</i>) == ch
//     * </pre></blockquote>
//     * is true. For other values of {@code ch}, it is the
//     * smallest value <i>k</i> such that:
//     * <blockquote><pre>
//     * this.codePointAt(<i>k</i>) == ch
//     * </pre></blockquote>
//     * is true. In either case, if no such character occurs in this
//     * string, then {@code -1} is returned.
//     *
//     * @param   ch   a character (Unicode code point).
//     * @return  the index of the first occurrence of the character in the
//     *          character sequence represented by this object, or
//     *          {@code -1} if the character does not occur.
//     */
//    public int indexOf(int ch) {
//        return indexOf(ch, 0);
//    }
//
//    /**
//     * Returns the index within this string of the first occurrence of the
//     * specified character, starting the search at the specified index.
//     * <p>
//     * If a character with value {@code ch} occurs in the
//     * character sequence represented by this {@code String}
//     * object at an index no smaller than {@code fromIndex}, then
//     * the index of the first such occurrence is returned. For values
//     * of {@code ch} in the range from 0 to 0xFFFF (inclusive),
//     * this is the smallest value <i>k</i> such that:
//     * <blockquote><pre>
//     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
//     * </pre></blockquote>
//     * is true. For other values of {@code ch}, it is the
//     * smallest value <i>k</i> such that:
//     * <blockquote><pre>
//     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
//     * </pre></blockquote>
//     * is true. In either case, if no such character occurs in this
//     * string at or after position {@code fromIndex}, then
//     * {@code -1} is returned.
//     *
//     * <p>
//     * There is no restriction on the value of {@code fromIndex}. If it
//     * is negative, it has the same effect as if it were zero: this entire
//     * string may be searched. If it is greater than the length of this
//     * string, it has the same effect as if it were equal to the length of
//     * this string: {@code -1} is returned.
//     *
//     * <p>All indices are specified in {@code char} values
//     * (Unicode code units).
//     *
//     * @param   ch          a character (Unicode code point).
//     * @param   fromIndex   the index to start the search from.
//     * @return  the index of the first occurrence of the character in the
//     *          character sequence represented by this object that is greater
//     *          than or equal to {@code fromIndex}, or {@code -1}
//     *          if the character does not occur.
//     */
//    public int indexOf(int ch, int fromIndex) {
//        final int max = length();
//        if (fromIndex < 0) {
//            fromIndex = 0;
//        } else if (fromIndex >= max) {
//            // Note: fromIndex might be near -1>>>1.
//            return -1;
//        }
//
//        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
//            // handle most cases here (ch is a BMP code point or a
//            // negative value (invalid code point))
//            for (int i = fromIndex; i < max; i++) {
//                if (charAt(i) == ch) {
//                    return i;
//                }
//            }
//            return -1;
//        } else {
//            return indexOfSupplementary(ch, fromIndex);
//        }
//    }
//
//    /**
//     * Handles (rare) calls of indexOf with a supplementary character.
//     */
//    private int indexOfSupplementary(int ch, int fromIndex) {
//        if (Character.isValidCodePoint(ch)) {
//            final char hi = Character.highSurrogate(ch);
//            final char lo = Character.lowSurrogate(ch);
//            final int max = length() - 1;
//            for (int i = fromIndex; i < max; i++) {
//                if (charAt(i) == hi && charAt(i + 1) == lo) {
//                    return i;
//                }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * Returns the index within this string of the last occurrence of
//     * the specified character. For values of {@code ch} in the
//     * range from 0 to 0xFFFF (inclusive), the index (in Unicode code
//     * units) returned is the largest value <i>k</i> such that:
//     * <blockquote><pre>
//     * this.charAt(<i>k</i>) == ch
//     * </pre></blockquote>
//     * is true. For other values of {@code ch}, it is the
//     * largest value <i>k</i> such that:
//     * <blockquote><pre>
//     * this.codePointAt(<i>k</i>) == ch
//     * </pre></blockquote>
//     * is true.  In either case, if no such character occurs in this
//     * string, then {@code -1} is returned.  The
//     * {@code String} is searched backwards starting at the last
//     * character.
//     *
//     * @param   ch   a character (Unicode code point).
//     * @return  the index of the last occurrence of the character in the
//     *          character sequence represented by this object, or
//     *          {@code -1} if the character does not occur.
//     */
//    public int lastIndexOf(int ch) {
//        return lastIndexOf(ch, length() - 1);
//    }
//
//    /**
//     * Returns the index within this string of the last occurrence of
//     * the specified character, searching backward starting at the
//     * specified index. For values of {@code ch} in the range
//     * from 0 to 0xFFFF (inclusive), the index returned is the largest
//     * value <i>k</i> such that:
//     * <blockquote><pre>
//     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
//     * </pre></blockquote>
//     * is true. For other values of {@code ch}, it is the
//     * largest value <i>k</i> such that:
//     * <blockquote><pre>
//     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
//     * </pre></blockquote>
//     * is true. In either case, if no such character occurs in this
//     * string at or before position {@code fromIndex}, then
//     * {@code -1} is returned.
//     *
//     * <p>All indices are specified in {@code char} values
//     * (Unicode code units).
//     *
//     * @param   ch          a character (Unicode code point).
//     * @param   fromIndex   the index to start the search from. There is no
//     *          restriction on the value of {@code fromIndex}. If it is
//     *          greater than or equal to the length of this string, it has
//     *          the same effect as if it were equal to one less than the
//     *          length of this string: this entire string may be searched.
//     *          If it is negative, it has the same effect as if it were -1:
//     *          -1 is returned.
//     * @return  the index of the last occurrence of the character in the
//     *          character sequence represented by this object that is less
//     *          than or equal to {@code fromIndex}, or {@code -1}
//     *          if the character does not occur before that point.
//     */
//    public int lastIndexOf(int ch, int fromIndex) {
//        if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
//            // handle most cases here (ch is a BMP code point or a
//            // negative value (invalid code point))
//            int i = Math.min(fromIndex, length() - 1);
//            for (; i >= 0; i--) {
//                if (charAt(i) == ch) {
//                    return i;
//                }
//            }
//            return -1;
//        } else {
//            return lastIndexOfSupplementary(ch, fromIndex);
//        }
//    }
//
//    /**
//     * Handles (rare) calls of lastIndexOf with a supplementary character.
//     */
//    private int lastIndexOfSupplementary(int ch, int fromIndex) {
//        if (Character.isValidCodePoint(ch)) {
//            char hi = Character.highSurrogate(ch);
//            char lo = Character.lowSurrogate(ch);
//            int i = Math.min(fromIndex, length() - 2);
//            for (; i >= 0; i--) {
//                if (charAt(i) == hi && charAt(i + 1) == lo) {
//                    return i;
//                }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * Returns the index within this string of the first occurrence of the
//     * specified substring.
//     *
//     * <p>The returned index is the smallest value <i>k</i> for which:
//     * <blockquote><pre>
//     * this.startsWith(str, <i>k</i>)
//     * </pre></blockquote>
//     * If no such value of <i>k</i> exists, then {@code -1} is returned.
//     *
//     * @param   str   the substring to search for.
//     * @return  the index of the first occurrence of the specified substring,
//     *          or {@code -1} if there is no such occurrence.
//     */
//    public int indexOf(String str) {
//        return indexOf(str, 0);
//    }
//
//    /**
//     * Returns the index within this string of the first occurrence of the
//     * specified substring, starting at the specified index.
//     *
//     * <p>The returned index is the smallest value <i>k</i> for which:
//     * <blockquote><pre>
//     * <i>k</i> &gt;= fromIndex {@code &&} this.startsWith(str, <i>k</i>)
//     * </pre></blockquote>
//     * If no such value of <i>k</i> exists, then {@code -1} is returned.
//     *
//     * @param   str         the substring to search for.
//     * @param   fromIndex   the index from which to start the search.
//     * @return  the index of the first occurrence of the specified substring,
//     *          starting at the specified index,
//     *          or {@code -1} if there is no such occurrence.
//     */
//    public int indexOf(String str, int fromIndex) {
//        // Android-changed: Delegate to the static indexOf method below.
//        return indexOf(this, str, fromIndex);
//    }
//
//    // BEGIN Android-added: Private static indexOf method that takes String parameters.
//    // The use of length(), charAt(), etc. makes it more efficient for compressed strings.
//    /**
//     * The source is the string being searched, and the target is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   target       the characters being searched for.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    private static int indexOf(String source, String target, int fromIndex) {
//        final int sourceLength = source.length();
//        final int targetLength = target.length();
//        if (fromIndex >= sourceLength) {
//            return (targetLength == 0 ? sourceLength : -1);
//        }
//        if (fromIndex < 0) {
//            fromIndex = 0;
//        }
//        if (targetLength == 0) {
//            return fromIndex;
//        }
//
//        char first = target.charAt(0);
//        int max = (sourceLength - targetLength);
//
//        for (int i = fromIndex; i <= max; i++) {
//            /* Look for first character. */
//            if (source.charAt(i)!= first) {
//                while (++i <= max && source.charAt(i) != first);
//            }
//
//            /* Found first character, now look at the rest of v2 */
//            if (i <= max) {
//                int j = i + 1;
//                int end = j + targetLength - 1;
//                for (int k = 1; j < end && source.charAt(j)
//                         == target.charAt(k); j++, k++);
//
//                if (j == end) {
//                    /* Found whole string. */
//                    return i;
//                }
//            }
//        }
//        return -1;
//    }
//    // END Android-added: Private static indexOf method that takes String parameters.
//
//    /**
//     * Code shared by String and AbstractStringBuilder to do searches. The
//     * source is the character array being searched, and the target
//     * is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   sourceOffset offset of the source string.
//     * @param   sourceCount  count of the source string.
//     * @param   target       the characters being searched for.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    static int indexOf(char[] source, int sourceOffset, int sourceCount,
//            String target, int fromIndex) {
//        return indexOf(source, sourceOffset, sourceCount,
//                       target.toCharArray(), 0, target.length(),
//                       fromIndex);
//    }
//
//    /**
//     * Code shared by String and StringBuffer to do searches. The
//     * source is the character array being searched, and the target
//     * is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   sourceOffset offset of the source string.
//     * @param   sourceCount  count of the source string.
//     * @param   target       the characters being searched for.
//     * @param   targetOffset offset of the target string.
//     * @param   targetCount  count of the target string.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    static int indexOf(char[] source, int sourceOffset, int sourceCount,
//            char[] target, int targetOffset, int targetCount,
//            int fromIndex) {
//        if (fromIndex >= sourceCount) {
//            return (targetCount == 0 ? sourceCount : -1);
//        }
//        if (fromIndex < 0) {
//            fromIndex = 0;
//        }
//        if (targetCount == 0) {
//            return fromIndex;
//        }
//
//        char first = target[targetOffset];
//        int max = sourceOffset + (sourceCount - targetCount);
//
//        for (int i = sourceOffset + fromIndex; i <= max; i++) {
//            /* Look for first character. */
//            if (source[i] != first) {
//                while (++i <= max && source[i] != first);
//            }
//
//            /* Found first character, now look at the rest of v2 */
//            if (i <= max) {
//                int j = i + 1;
//                int end = j + targetCount - 1;
//                for (int k = targetOffset + 1; j < end && source[j]
//                        == target[k]; j++, k++);
//
//                if (j == end) {
//                    /* Found whole string. */
//                    return i - sourceOffset;
//                }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * Returns the index within this string of the last occurrence of the
//     * specified substring.  The last occurrence of the empty string ""
//     * is considered to occur at the index value {@code this.length()}.
//     *
//     * <p>The returned index is the largest value <i>k</i> for which:
//     * <blockquote><pre>
//     * this.startsWith(str, <i>k</i>)
//     * </pre></blockquote>
//     * If no such value of <i>k</i> exists, then {@code -1} is returned.
//     *
//     * @param   str   the substring to search for.
//     * @return  the index of the last occurrence of the specified substring,
//     *          or {@code -1} if there is no such occurrence.
//     */
//    public int lastIndexOf(String str) {
//        return lastIndexOf(str, length());
//    }
//
//    /**
//     * Returns the index within this string of the last occurrence of the
//     * specified substring, searching backward starting at the specified index.
//     *
//     * <p>The returned index is the largest value <i>k</i> for which:
//     * <blockquote><pre>
//     * <i>k</i> {@code <=} fromIndex {@code &&} this.startsWith(str, <i>k</i>)
//     * </pre></blockquote>
//     * If no such value of <i>k</i> exists, then {@code -1} is returned.
//     *
//     * @param   str         the substring to search for.
//     * @param   fromIndex   the index to start the search from.
//     * @return  the index of the last occurrence of the specified substring,
//     *          searching backward from the specified index,
//     *          or {@code -1} if there is no such occurrence.
//     */
//    public int lastIndexOf(String str, int fromIndex) {
//        // Android-changed: Change parameters to static lastIndexOf to match new signature below.
//        return lastIndexOf(this, str, fromIndex);
//    }
//
//    // BEGIN Android-added: Private static lastIndexOf method that takes String parameters.
//    // The use of length(), charAt(), etc. makes it more efficient for compressed strings.
//    /**
//     * The source is the string being searched, and the target is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   target       the characters being searched for.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    private static int lastIndexOf(String source, String target, int fromIndex) {
//        /*
//         * Check arguments; return immediately where possible. For
//         * consistency, don't check for null str.
//         */
//        final int sourceLength = source.length();
//        final int targetLength = target.length();
//        int rightIndex = sourceLength - targetLength;
//        if (fromIndex < 0) {
//            return -1;
//        }
//        if (fromIndex > rightIndex) {
//            fromIndex = rightIndex;
//        }
//        /* Empty string always matches. */
//        if (targetLength == 0) {
//            return fromIndex;
//        }
//
//        int strLastIndex = targetLength - 1;
//        char strLastChar = target.charAt(strLastIndex);
//        int min = targetLength - 1;
//        int i = min + fromIndex;
//
//        startSearchForLastChar:
//        while (true) {
//            while (i >= min && source.charAt(i) != strLastChar) {
//                i--;
//            }
//            if (i < min) {
//                return -1;
//            }
//            int j = i - 1;
//            int start = j - (targetLength - 1);
//            int k = strLastIndex - 1;
//
//            while (j > start) {
//                if (source.charAt(j--) != target.charAt(k--)) {
//                    i--;
//                    continue startSearchForLastChar;
//                }
//            }
//            return start + 1;
//        }
//    }
//    // END Android-added: Private static lastIndexOf method that takes String parameters.
//
//    /**
//     * Code shared by String and AbstractStringBuilder to do searches. The
//     * source is the character array being searched, and the target
//     * is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   sourceOffset offset of the source string.
//     * @param   sourceCount  count of the source string.
//     * @param   target       the characters being searched for.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
//            String target, int fromIndex) {
//        return lastIndexOf(source, sourceOffset, sourceCount,
//                       target.toCharArray(), 0, target.length(),
//                       fromIndex);
//    }
//
//    /**
//     * Code shared by String and StringBuffer to do searches. The
//     * source is the character array being searched, and the target
//     * is the string being searched for.
//     *
//     * @param   source       the characters being searched.
//     * @param   sourceOffset offset of the source string.
//     * @param   sourceCount  count of the source string.
//     * @param   target       the characters being searched for.
//     * @param   targetOffset offset of the target string.
//     * @param   targetCount  count of the target string.
//     * @param   fromIndex    the index to begin searching from.
//     */
//    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
//            char[] target, int targetOffset, int targetCount,
//            int fromIndex) {
//        /*
//         * Check arguments; return immediately where possible. For
//         * consistency, don't check for null str.
//         */
//        int rightIndex = sourceCount - targetCount;
//        if (fromIndex < 0) {
//            return -1;
//        }
//        if (fromIndex > rightIndex) {
//            fromIndex = rightIndex;
//        }
//        /* Empty string always matches. */
//        if (targetCount == 0) {
//            return fromIndex;
//        }
//
//        int strLastIndex = targetOffset + targetCount - 1;
//        char strLastChar = target[strLastIndex];
//        int min = sourceOffset + targetCount - 1;
//        int i = min + fromIndex;
//
//    startSearchForLastChar:
//        while (true) {
//            while (i >= min && source[i] != strLastChar) {
//                i--;
//            }
//            if (i < min) {
//                return -1;
//            }
//            int j = i - 1;
//            int start = j - (targetCount - 1);
//            int k = strLastIndex - 1;
//
//            while (j > start) {
//                if (source[j--] != target[k--]) {
//                    i--;
//                    continue startSearchForLastChar;
//                }
//            }
//            return start - sourceOffset + 1;
//        }
//    }
//
//    /**
//     * Returns a string that is a substring of this string. The
//     * substring begins with the character at the specified index and
//     * extends to the end of this string. <p>
//     * Examples:
//     * <blockquote><pre>
//     * "unhappy".substring(2) returns "happy"
//     * "Harbison".substring(3) returns "bison"
//     * "emptiness".substring(9) returns "" (an empty string)
//     * </pre></blockquote>
//     *
//     * @param      beginIndex   the beginning index, inclusive.
//     * @return     the specified substring.
//     * @exception  IndexOutOfBoundsException  if
//     *             {@code beginIndex} is negative or larger than the
//     *             length of this {@code String} object.
//     */
//    public String substring(int beginIndex) {
//        if (beginIndex < 0) {
//            throw new StringIndexOutOfBoundsException(this, beginIndex);
//        }
//        int subLen = length() - beginIndex;
//        if (subLen < 0) {
//            throw new StringIndexOutOfBoundsException(this, beginIndex);
//        }
//        // Android-changed: Use native fastSubstring instead of String constructor.
//        return (beginIndex == 0) ? this : fastSubstring(beginIndex, subLen);
//    }
//
//    /**
//     * Returns a string that is a substring of this string. The
//     * substring begins at the specified {@code beginIndex} and
//     * extends to the character at index {@code endIndex - 1}.
//     * Thus the length of the substring is {@code endIndex-beginIndex}.
//     * <p>
//     * Examples:
//     * <blockquote><pre>
//     * "hamburger".substring(4, 8) returns "urge"
//     * "smiles".substring(1, 5) returns "mile"
//     * </pre></blockquote>
//     *
//     * @param      beginIndex   the beginning index, inclusive.
//     * @param      endIndex     the ending index, exclusive.
//     * @return     the specified substring.
//     * @exception  IndexOutOfBoundsException  if the
//     *             {@code beginIndex} is negative, or
//     *             {@code endIndex} is larger than the length of
//     *             this {@code String} object, or
//     *             {@code beginIndex} is larger than
//     *             {@code endIndex}.
//     */
//    public String substring(int beginIndex, int endIndex) {
//        if (beginIndex < 0) {
//            throw new StringIndexOutOfBoundsException(this, beginIndex);
//        }
//        if (endIndex > length()) {
//            throw new StringIndexOutOfBoundsException(this, endIndex);
//        }
//        int subLen = endIndex - beginIndex;
//        if (subLen < 0) {
//            throw new StringIndexOutOfBoundsException(subLen);
//        }
//
//        // Android-changed: Use native fastSubstring instead of String constructor.
//        return ((beginIndex == 0) && (endIndex == length())) ? this
//                : fastSubstring(beginIndex, subLen);
//    }
//
//    // BEGIN Android-added: Native method to access char storage managed by runtime.
//    @FastNative
//    private native String fastSubstring(int start, int length);
//    // END Android-added: Native method to access char storage managed by runtime.
//
//    /**
//     * Returns a character sequence that is a subsequence of this sequence.
//     *
//     * <p> An invocation of this method of the form
//     *
//     * <blockquote><pre>
//     * str.subSequence(begin,&nbsp;end)</pre></blockquote>
//     *
//     * behaves in exactly the same way as the invocation
//     *
//     * <blockquote><pre>
//     * str.substring(begin,&nbsp;end)</pre></blockquote>
//     *
//     * @apiNote
//     * This method is defined so that the {@code String} class can implement
//     * the {@link CharSequence} interface.
//     *
//     * @param   beginIndex   the begin index, inclusive.
//     * @param   endIndex     the end index, exclusive.
//     * @return  the specified subsequence.
//     *
//     * @throws  IndexOutOfBoundsException
//     *          if {@code beginIndex} or {@code endIndex} is negative,
//     *          if {@code endIndex} is greater than {@code length()},
//     *          or if {@code beginIndex} is greater than {@code endIndex}
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public CharSequence subSequence(int beginIndex, int endIndex) {
//        return this.substring(beginIndex, endIndex);
//    }
//
//    /**
//     * Concatenates the specified string to the end of this string.
//     * <p>
//     * If the length of the argument string is {@code 0}, then this
//     * {@code String} object is returned. Otherwise, a
//     * {@code String} object is returned that represents a character
//     * sequence that is the concatenation of the character sequence
//     * represented by this {@code String} object and the character
//     * sequence represented by the argument string.<p>
//     * Examples:
//     * <blockquote><pre>
//     * "cares".concat("s") returns "caress"
//     * "to".concat("get").concat("her") returns "together"
//     * </pre></blockquote>
//     *
//     * @param   str   the {@code String} that is concatenated to the end
//     *                of this {@code String}.
//     * @return  a string that represents the concatenation of this object's
//     *          characters followed by the string argument's characters.
//     */
//    // BEGIN Android-changed: Replace with implementation in runtime to access chars (see above).
//    @FastNative
//    public native String concat(String str);
//    // END Android-changed: Replace with implementation in runtime to access chars (see above).
//
//    /**
//     * Returns a string resulting from replacing all occurrences of
//     * {@code oldChar} in this string with {@code newChar}.
//     * <p>
//     * If the character {@code oldChar} does not occur in the
//     * character sequence represented by this {@code String} object,
//     * then a reference to this {@code String} object is returned.
//     * Otherwise, a {@code String} object is returned that
//     * represents a character sequence identical to the character sequence
//     * represented by this {@code String} object, except that every
//     * occurrence of {@code oldChar} is replaced by an occurrence
//     * of {@code newChar}.
//     * <p>
//     * Examples:
//     * <blockquote><pre>
//     * "mesquite in your cellar".replace('e', 'o')
//     *         returns "mosquito in your collar"
//     * "the war of baronets".replace('r', 'y')
//     *         returns "the way of bayonets"
//     * "sparring with a purple porpoise".replace('p', 't')
//     *         returns "starring with a turtle tortoise"
//     * "JonL".replace('q', 'x') returns "JonL" (no change)
//     * </pre></blockquote>
//     *
//     * @param   oldChar   the old character.
//     * @param   newChar   the new character.
//     * @return  a string derived from this string by replacing every
//     *          occurrence of {@code oldChar} with {@code newChar}.
//     */
//    public String replace(char oldChar, char newChar) {
//        // BEGIN Android-changed: Replace with implementation using native doReplace method.
//        if (oldChar != newChar) {
//            final int len = length();
//            for (int i = 0; i < len; ++i) {
//                if (charAt(i) == oldChar) {
//                    return doReplace(oldChar, newChar);
//                }
//            }
//        }
//        // END Android-changed: Replace with implementation using native doReplace method.
//        return this;
//    }
//
//    // BEGIN Android-added: Native method to access char storage managed by runtime.
//    // Implementation of replace(char oldChar, char newChar) called when we found a match.
//    @FastNative
//    private native String doReplace(char oldChar, char newChar);
//    // END Android-added: Native method to access char storage managed by runtime.
//
//    /**
//     * Tells whether or not this string matches the given <a
//     * href="../util/regex/Pattern.html#sum">regular expression</a>.
//     *
//     * <p> An invocation of this method of the form
//     * <i>str</i>{@code .matches(}<i>regex</i>{@code )} yields exactly the
//     * same result as the expression
//     *
//     * <blockquote>
//     * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#matches(String,CharSequence)
//     * matches(<i>regex</i>, <i>str</i>)}
//     * </blockquote>
//     *
//     * @param   regex
//     *          the regular expression to which this string is to be matched
//     *
//     * @return  {@code true} if, and only if, this string matches the
//     *          given regular expression
//     *
//     * @throws  PatternSyntaxException
//     *          if the regular expression's syntax is invalid
//     *
//     * @see java.util.regex.Pattern
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public boolean matches(String regex) {
//        return Pattern.matches(regex, this);
//    }
//
//    /**
//     * Returns true if and only if this string contains the specified
//     * sequence of char values.
//     *
//     * @param s the sequence to search for
//     * @return true if this string contains {@code s}, false otherwise
//     * @since 1.5
//     */
//    public boolean contains(CharSequence s) {
//        return indexOf(s.toString()) > -1;
//    }
//
//    /**
//     * Replaces the first substring of this string that matches the given <a
//     * href="../util/regex/Pattern.html#sum">regular expression</a> with the
//     * given replacement.
//     *
//     * <p> An invocation of this method of the form
//     * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
//     * yields exactly the same result as the expression
//     *
//     * <blockquote>
//     * <code>
//     * {@link java.util.regex.Pattern}.{@link
//     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
//     * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
//     * java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
//     * </code>
//     * </blockquote>
//     *
//     *<p>
//     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
//     * replacement string may cause the results to be different than if it were
//     * being treated as a literal replacement string; see
//     * {@link java.util.regex.Matcher#replaceFirst}.
//     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
//     * meaning of these characters, if desired.
//     *
//     * @param   regex
//     *          the regular expression to which this string is to be matched
//     * @param   replacement
//     *          the string to be substituted for the first match
//     *
//     * @return  The resulting {@code String}
//     *
//     * @throws  PatternSyntaxException
//     *          if the regular expression's syntax is invalid
//     *
//     * @see java.util.regex.Pattern
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public String replaceFirst(String regex, String replacement) {
//        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
//    }
//
//    /**
//     * Replaces each substring of this string that matches the given <a
//     * href="../util/regex/Pattern.html#sum">regular expression</a> with the
//     * given replacement.
//     *
//     * <p> An invocation of this method of the form
//     * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
//     * yields exactly the same result as the expression
//     *
//     * <blockquote>
//     * <code>
//     * {@link java.util.regex.Pattern}.{@link
//     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
//     * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
//     * java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
//     * </code>
//     * </blockquote>
//     *
//     *<p>
//     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
//     * replacement string may cause the results to be different than if it were
//     * being treated as a literal replacement string; see
//     * {@link java.util.regex.Matcher#replaceAll Matcher.replaceAll}.
//     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
//     * meaning of these characters, if desired.
//     *
//     * @param   regex
//     *          the regular expression to which this string is to be matched
//     * @param   replacement
//     *          the string to be substituted for each match
//     *
//     * @return  The resulting {@code String}
//     *
//     * @throws  PatternSyntaxException
//     *          if the regular expression's syntax is invalid
//     *
//     * @see java.util.regex.Pattern
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public String replaceAll(String regex, String replacement) {
//        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
//    }
//
//    /**
//     * Replaces each substring of this string that matches the literal target
//     * sequence with the specified literal replacement sequence. The
//     * replacement proceeds from the beginning of the string to the end, for
//     * example, replacing "aa" with "b" in the string "aaa" will result in
//     * "ba" rather than "ab".
//     *
//     * @param  target The sequence of char values to be replaced
//     * @param  replacement The replacement sequence of char values
//     * @return  The resulting string
//     * @since 1.5
//     */
//    public String replace(CharSequence target, CharSequence replacement) {
//        // BEGIN Android-changed: Replace regex-based implementation with a bespoke one.
//        if (target == null) {
//            throw new NullPointerException("target == null");
//        }
//
//        if (replacement == null) {
//            throw new NullPointerException("replacement == null");
//        }
//
//        String replacementStr = replacement.toString();
//        String targetStr = target.toString();
//
//        // Special case when target == "". This is a pretty nonsensical transformation and nobody
//        // should be hitting this.
//        //
//        // See commit 870b23b3febc85 and http://code.google.com/p/android/issues/detail?id=8807
//        // An empty target is inserted at the start of the string, the end of the string and
//        // between all characters.
//        final int len = length();
//        if (targetStr.isEmpty()) {
//            // Note that overallocates by |replacement.size()| if |this| is the empty string, but
//            // that should be a rare case within an already nonsensical case.
//            StringBuilder sb = new StringBuilder(replacementStr.length() * (len + 2) + len);
//            sb.append(replacementStr);
//            for (int i = 0; i < len; ++i) {
//                sb.append(charAt(i));
//                sb.append(replacementStr);
//            }
//
//            return sb.toString();
//        }
//
//        // This is the "regular" case.
//        int lastMatch = 0;
//        StringBuilder sb = null;
//        for (;;) {
//            int currentMatch = indexOf(this, targetStr, lastMatch);
//            if (currentMatch == -1) {
//                break;
//            }
//
//            if (sb == null) {
//                sb = new StringBuilder(len);
//            }
//
//            sb.append(this, lastMatch, currentMatch);
//            sb.append(replacementStr);
//            lastMatch = currentMatch + targetStr.length();
//        }
//
//        if (sb != null) {
//            sb.append(this, lastMatch, len);
//            return sb.toString();
//        } else {
//            return this;
//        }
//        // END Android-changed: Replace regex-based implementation with a bespoke one.
//    }
//
//    /**
//     * Splits this string around matches of the given
//     * <a href="../util/regex/Pattern.html#sum">regular expression</a>.
//     *
//     * <p> The array returned by this method contains each substring of this
//     * string that is terminated by another substring that matches the given
//     * expression or is terminated by the end of the string.  The substrings in
//     * the array are in the order in which they occur in this string.  If the
//     * expression does not match any part of the input then the resulting array
//     * has just one element, namely this string.
//     *
//     * <p> When there is a positive-width match at the beginning of this
//     * string then an empty leading substring is included at the beginning
//     * of the resulting array. A zero-width match at the beginning however
//     * never produces such empty leading substring.
//     *
//     * <p> The {@code limit} parameter controls the number of times the
//     * pattern is applied and therefore affects the length of the resulting
//     * array.  If the limit <i>n</i> is greater than zero then the pattern
//     * will be applied at most <i>n</i>&nbsp;-&nbsp;1 times, the array's
//     * length will be no greater than <i>n</i>, and the array's last entry
//     * will contain all input beyond the last matched delimiter.  If <i>n</i>
//     * is non-positive then the pattern will be applied as many times as
//     * possible and the array can have any length.  If <i>n</i> is zero then
//     * the pattern will be applied as many times as possible, the array can
//     * have any length, and trailing empty strings will be discarded.
//     *
//     * <p> The string {@code "boo:and:foo"}, for example, yields the
//     * following results with these parameters:
//     *
//     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split example showing regex, limit, and result">
//     * <tr>
//     *     <th>Regex</th>
//     *     <th>Limit</th>
//     *     <th>Result</th>
//     * </tr>
//     * <tr><td align=center>:</td>
//     *     <td align=center>2</td>
//     *     <td>{@code { "boo", "and:foo" }}</td></tr>
//     * <tr><td align=center>:</td>
//     *     <td align=center>5</td>
//     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
//     * <tr><td align=center>:</td>
//     *     <td align=center>-2</td>
//     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
//     * <tr><td align=center>o</td>
//     *     <td align=center>5</td>
//     *     <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
//     * <tr><td align=center>o</td>
//     *     <td align=center>-2</td>
//     *     <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
//     * <tr><td align=center>o</td>
//     *     <td align=center>0</td>
//     *     <td>{@code { "b", "", ":and:f" }}</td></tr>
//     * </table></blockquote>
//     *
//     * <p> An invocation of this method of the form
//     * <i>str.</i>{@code split(}<i>regex</i>{@code ,}&nbsp;<i>n</i>{@code )}
//     * yields the same result as the expression
//     *
//     * <blockquote>
//     * <code>
//     * {@link java.util.regex.Pattern}.{@link
//     * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
//     * java.util.regex.Pattern#split(java.lang.CharSequence,int) split}(<i>str</i>,&nbsp;<i>n</i>)
//     * </code>
//     * </blockquote>
//     *
//     *
//     * @param  regex
//     *         the delimiting regular expression
//     *
//     * @param  limit
//     *         the result threshold, as described above
//     *
//     * @return  the array of strings computed by splitting this string
//     *          around matches of the given regular expression
//     *
//     * @throws  PatternSyntaxException
//     *          if the regular expression's syntax is invalid
//     *
//     * @see java.util.regex.Pattern
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public String[] split(String regex, int limit) {
//        // BEGIN Android-changed: Replace custom fast-path with use of new Pattern.fastSplit method.
//        // Try fast splitting without allocating Pattern object
//        String[] fast = Pattern.fastSplit(regex, this, limit);
//        if (fast != null) {
//            return fast;
//        }
//        // END Android-changed: Replace custom fast-path with use of new Pattern.fastSplit method.
//        return Pattern.compile(regex).split(this, limit);
//    }
//
//    /**
//     * Splits this string around matches of the given <a
//     * href="../util/regex/Pattern.html#sum">regular expression</a>.
//     *
//     * <p> This method works as if by invoking the two-argument {@link
//     * #split(String, int) split} method with the given expression and a limit
//     * argument of zero.  Trailing empty strings are therefore not included in
//     * the resulting array.
//     *
//     * <p> The string {@code "boo:and:foo"}, for example, yields the following
//     * results with these expressions:
//     *
//     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
//     * <tr>
//     *  <th>Regex</th>
//     *  <th>Result</th>
//     * </tr>
//     * <tr><td align=center>:</td>
//     *     <td>{@code { "boo", "and", "foo" }}</td></tr>
//     * <tr><td align=center>o</td>
//     *     <td>{@code { "b", "", ":and:f" }}</td></tr>
//     * </table></blockquote>
//     *
//     *
//     * @param  regex
//     *         the delimiting regular expression
//     *
//     * @return  the array of strings computed by splitting this string
//     *          around matches of the given regular expression
//     *
//     * @throws  PatternSyntaxException
//     *          if the regular expression's syntax is invalid
//     *
//     * @see java.util.regex.Pattern
//     *
//     * @since 1.4
//     * @spec JSR-51
//     */
//    public String[] split(String regex) {
//        return split(regex, 0);
//    }
//
//    /**
//     * Returns a new String composed of copies of the
//     * {@code CharSequence elements} joined together with a copy of
//     * the specified {@code delimiter}.
//     *
//     * <blockquote>For example,
//     * <pre>{@code
//     *     String message = String.join("-", "Java", "is", "cool");
//     *     // message returned is: "Java-is-cool"
//     * }</pre></blockquote>
//     *
//     * Note that if an element is null, then {@code "null"} is added.
//     *
//     * @param  delimiter the delimiter that separates each element
//     * @param  elements the elements to join together.
//     *
//     * @return a new {@code String} that is composed of the {@code elements}
//     *         separated by the {@code delimiter}
//     *
//     * @throws NullPointerException If {@code delimiter} or {@code elements}
//     *         is {@code null}
//     *
//     * @see java.util.StringJoiner
//     * @since 1.8
//     */
//    public static String join(CharSequence delimiter, CharSequence... elements) {
//        Objects.requireNonNull(delimiter);
//        Objects.requireNonNull(elements);
//        // Number of elements not likely worth Arrays.stream overhead.
//        StringJoiner joiner = new StringJoiner(delimiter);
//        for (CharSequence cs: elements) {
//            joiner.add(cs);
//        }
//        return joiner.toString();
//    }
//
//    /**
//     * Returns a new {@code String} composed of copies of the
//     * {@code CharSequence elements} joined together with a copy of the
//     * specified {@code delimiter}.
//     *
//     * <blockquote>For example,
//     * <pre>{@code
//     *     List<String> strings = new LinkedList<>();
//     *     strings.add("Java");strings.add("is");
//     *     strings.add("cool");
//     *     String message = String.join(" ", strings);
//     *     //message returned is: "Java is cool"
//     *
//     *     Set<String> strings = new LinkedHashSet<>();
//     *     strings.add("Java"); strings.add("is");
//     *     strings.add("very"); strings.add("cool");
//     *     String message = String.join("-", strings);
//     *     //message returned is: "Java-is-very-cool"
//     * }</pre></blockquote>
//     *
//     * Note that if an individual element is {@code null}, then {@code "null"} is added.
//     *
//     * @param  delimiter a sequence of characters that is used to separate each
//     *         of the {@code elements} in the resulting {@code String}
//     * @param  elements an {@code Iterable} that will have its {@code elements}
//     *         joined together.
//     *
//     * @return a new {@code String} that is composed from the {@code elements}
//     *         argument
//     *
//     * @throws NullPointerException If {@code delimiter} or {@code elements}
//     *         is {@code null}
//     *
//     * @see    #join(CharSequence,CharSequence...)
//     * @see    java.util.StringJoiner
//     * @since 1.8
//     */
//    public static String join(CharSequence delimiter,
//            Iterable<? extends CharSequence> elements) {
//        Objects.requireNonNull(delimiter);
//        Objects.requireNonNull(elements);
//        StringJoiner joiner = new StringJoiner(delimiter);
//        for (CharSequence cs: elements) {
//            joiner.add(cs);
//        }
//        return joiner.toString();
//    }
//
//    /**
//     * Converts all of the characters in this {@code String} to lower
//     * case using the rules of the given {@code Locale}.  Case mapping is based
//     * on the Unicode Standard version specified by the {@link java.lang.Character Character}
//     * class. Since case mappings are not always 1:1 char mappings, the resulting
//     * {@code String} may be a different length than the original {@code String}.
//     * <p>
//     * Examples of lowercase  mappings are in the following table:
//     * <table border="1" summary="Lowercase mapping examples showing language code of locale, upper case, lower case, and description">
//     * <tr>
//     *   <th>Language Code of Locale</th>
//     *   <th>Upper Case</th>
//     *   <th>Lower Case</th>
//     *   <th>Description</th>
//     * </tr>
//     * <tr>
//     *   <td>tr (Turkish)</td>
//     *   <td>&#92;u0130</td>
//     *   <td>&#92;u0069</td>
//     *   <td>capital letter I with dot above -&gt; small letter i</td>
//     * </tr>
//     * <tr>
//     *   <td>tr (Turkish)</td>
//     *   <td>&#92;u0049</td>
//     *   <td>&#92;u0131</td>
//     *   <td>capital letter I -&gt; small letter dotless i </td>
//     * </tr>
//     * <tr>
//     *   <td>(all)</td>
//     *   <td>French Fries</td>
//     *   <td>french fries</td>
//     *   <td>lowercased all chars in String</td>
//     * </tr>
//     * <tr>
//     *   <td>(all)</td>
//     *   <td><img src="doc-files/capiota.gif" alt="capiota"><img src="doc-files/capchi.gif" alt="capchi">
//     *       <img src="doc-files/captheta.gif" alt="captheta"><img src="doc-files/capupsil.gif" alt="capupsil">
//     *       <img src="doc-files/capsigma.gif" alt="capsigma"></td>
//     *   <td><img src="doc-files/iota.gif" alt="iota"><img src="doc-files/chi.gif" alt="chi">
//     *       <img src="doc-files/theta.gif" alt="theta"><img src="doc-files/upsilon.gif" alt="upsilon">
//     *       <img src="doc-files/sigma1.gif" alt="sigma"></td>
//     *   <td>lowercased all chars in String</td>
//     * </tr>
//     * </table>
//     *
//     * @param locale use the case transformation rules for this locale
//     * @return the {@code String}, converted to lowercase.
//     * @see     java.lang.String#toLowerCase()
//     * @see     java.lang.String#toUpperCase()
//     * @see     java.lang.String#toUpperCase(Locale)
//     * @since   1.1
//     */
//    public String toLowerCase(Locale locale) {
//        // Android-changed: Replace custom code with call to new CaseMapper class.
//        return CaseMapper.toLowerCase(locale, this);
//    }
//
//    /**
//     * Converts all of the characters in this {@code String} to lower
//     * case using the rules of the default locale. This is equivalent to calling
//     * {@code toLowerCase(Locale.getDefault())}.
//     * <p>
//     * <b>Note:</b> This method is locale sensitive, and may produce unexpected
//     * results if used for strings that are intended to be interpreted locale
//     * independently.
//     * Examples are programming language identifiers, protocol keys, and HTML
//     * tags.
//     * For instance, {@code "TITLE".toLowerCase()} in a Turkish locale
//     * returns {@code "t\u005Cu0131tle"}, where '\u005Cu0131' is the
//     * LATIN SMALL LETTER DOTLESS I character.
//     * To obtain correct results for locale insensitive strings, use
//     * {@code toLowerCase(Locale.ROOT)}.
//     * <p>
//     * @return  the {@code String}, converted to lowercase.
//     * @see     java.lang.String#toLowerCase(Locale)
//     */
//    public String toLowerCase() {
//        return toLowerCase(Locale.getDefault());
//    }
//
//    /**
//     * Converts all of the characters in this {@code String} to upper
//     * case using the rules of the given {@code Locale}. Case mapping is based
//     * on the Unicode Standard version specified by the {@link java.lang.Character Character}
//     * class. Since case mappings are not always 1:1 char mappings, the resulting
//     * {@code String} may be a different length than the original {@code String}.
//     * <p>
//     * Examples of locale-sensitive and 1:M case mappings are in the following table.
//     *
//     * <table border="1" summary="Examples of locale-sensitive and 1:M case mappings. Shows Language code of locale, lower case, upper case, and description.">
//     * <tr>
//     *   <th>Language Code of Locale</th>
//     *   <th>Lower Case</th>
//     *   <th>Upper Case</th>
//     *   <th>Description</th>
//     * </tr>
//     * <tr>
//     *   <td>tr (Turkish)</td>
//     *   <td>&#92;u0069</td>
//     *   <td>&#92;u0130</td>
//     *   <td>small letter i -&gt; capital letter I with dot above</td>
//     * </tr>
//     * <tr>
//     *   <td>tr (Turkish)</td>
//     *   <td>&#92;u0131</td>
//     *   <td>&#92;u0049</td>
//     *   <td>small letter dotless i -&gt; capital letter I</td>
//     * </tr>
//     * <tr>
//     *   <td>(all)</td>
//     *   <td>&#92;u00df</td>
//     *   <td>&#92;u0053 &#92;u0053</td>
//     *   <td>small letter sharp s -&gt; two letters: SS</td>
//     * </tr>
//     * <tr>
//     *   <td>(all)</td>
//     *   <td>Fahrvergn&uuml;gen</td>
//     *   <td>FAHRVERGN&Uuml;GEN</td>
//     *   <td></td>
//     * </tr>
//     * </table>
//     * @param locale use the case transformation rules for this locale
//     * @return the {@code String}, converted to uppercase.
//     * @see     java.lang.String#toUpperCase()
//     * @see     java.lang.String#toLowerCase()
//     * @see     java.lang.String#toLowerCase(Locale)
//     * @since   1.1
//     */
//    public String toUpperCase(Locale locale) {
//        // Android-changed: Replace custom code with call to new CaseMapper class.
//        return CaseMapper.toUpperCase(locale, this, length());
//    }
//
//    /**
//     * Converts all of the characters in this {@code String} to upper
//     * case using the rules of the default locale. This method is equivalent to
//     * {@code toUpperCase(Locale.getDefault())}.
//     * <p>
//     * <b>Note:</b> This method is locale sensitive, and may produce unexpected
//     * results if used for strings that are intended to be interpreted locale
//     * independently.
//     * Examples are programming language identifiers, protocol keys, and HTML
//     * tags.
//     * For instance, {@code "title".toUpperCase()} in a Turkish locale
//     * returns {@code "T\u005Cu0130TLE"}, where '\u005Cu0130' is the
//     * LATIN CAPITAL LETTER I WITH DOT ABOVE character.
//     * To obtain correct results for locale insensitive strings, use
//     * {@code toUpperCase(Locale.ROOT)}.
//     * <p>
//     * @return  the {@code String}, converted to uppercase.
//     * @see     java.lang.String#toUpperCase(Locale)
//     */
//    public String toUpperCase() {
//        return toUpperCase(Locale.getDefault());
//    }
//
//    /**
//     * Returns a string whose value is this string, with any leading and trailing
//     * whitespace removed.
//     * <p>
//     * If this {@code String} object represents an empty character
//     * sequence, or the first and last characters of character sequence
//     * represented by this {@code String} object both have codes
//     * greater than {@code '\u005Cu0020'} (the space character), then a
//     * reference to this {@code String} object is returned.
//     * <p>
//     * Otherwise, if there is no character with a code greater than
//     * {@code '\u005Cu0020'} in the string, then a
//     * {@code String} object representing an empty string is
//     * returned.
//     * <p>
//     * Otherwise, let <i>k</i> be the index of the first character in the
//     * string whose code is greater than {@code '\u005Cu0020'}, and let
//     * <i>m</i> be the index of the last character in the string whose code
//     * is greater than {@code '\u005Cu0020'}. A {@code String}
//     * object is returned, representing the substring of this string that
//     * begins with the character at index <i>k</i> and ends with the
//     * character at index <i>m</i>-that is, the result of
//     * {@code this.substring(k, m + 1)}.
//     * <p>
//     * This method may be used to trim whitespace (as defined above) from
//     * the beginning and end of a string.
//     *
//     * @return  A string whose value is this string, with any leading and trailing white
//     *          space removed, or this string if it has no leading or
//     *          trailing white space.
//     */
//    public String trim() {
//        int len = length();
//        int st = 0;
//
//        while ((st < len) && (charAt(st) <= ' ')) {
//            st++;
//        }
//        while ((st < len) && (charAt(len - 1) <= ' ')) {
//            len--;
//        }
//        return ((st > 0) || (len < length())) ? substring(st, len) : this;
//    }
//
//    /**
//     * This object (which is already a string!) is itself returned.
//     *
//     * @return  the string itself.
//     */
//    public String toString() {
//        return this;
//    }
//
//    /**
//     * Converts this string to a new character array.
//     *
//     * @return  a newly allocated character array whose length is the length
//     *          of this string and whose contents are initialized to contain
//     *          the character sequence represented by this string.
//     */
//    // BEGIN Android-changed: Replace with implementation in runtime to access chars (see above).
//    @FastNative
//    public native char[] toCharArray();
//    // END Android-changed: Replace with implementation in runtime to access chars (see above).
//
//
//    /**
//     * Returns a formatted string using the specified format string and
//     * arguments.
//     *
//     * <p> The locale always used is the one returned by {@link
//     * java.util.Locale#getDefault() Locale.getDefault()}.
//     *
//     * @param  format
//     *         A <a href="../util/Formatter.html#syntax">format string</a>
//     *
//     * @param  args
//     *         Arguments referenced by the format specifiers in the format
//     *         string.  If there are more arguments than format specifiers, the
//     *         extra arguments are ignored.  The number of arguments is
//     *         variable and may be zero.  The maximum number of arguments is
//     *         limited by the maximum dimension of a Java array as defined by
//     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
//     *         The behaviour on a
//     *         {@code null} argument depends on the <a
//     *         href="../util/Formatter.html#syntax">conversion</a>.
//     *
//     * @throws  java.util.IllegalFormatException
//     *          If a format string contains an illegal syntax, a format
//     *          specifier that is incompatible with the given arguments,
//     *          insufficient arguments given the format string, or other
//     *          illegal conditions.  For specification of all possible
//     *          formatting errors, see the <a
//     *          href="../util/Formatter.html#detail">Details</a> section of the
//     *          formatter class specification.
//     *
//     * @return  A formatted string
//     *
//     * @see  java.util.Formatter
//     * @since  1.5
//     */
//    public static String format(String format, Object... args) {
//        return new Formatter().format(format, args).toString();
//    }
//
//    /**
//     * Returns a formatted string using the specified locale, format string,
//     * and arguments.
//     *
//     * @param  l
//     *         The {@linkplain java.util.Locale locale} to apply during
//     *         formatting.  If {@code l} is {@code null} then no localization
//     *         is applied.
//     *
//     * @param  format
//     *         A <a href="../util/Formatter.html#syntax">format string</a>
//     *
//     * @param  args
//     *         Arguments referenced by the format specifiers in the format
//     *         string.  If there are more arguments than format specifiers, the
//     *         extra arguments are ignored.  The number of arguments is
//     *         variable and may be zero.  The maximum number of arguments is
//     *         limited by the maximum dimension of a Java array as defined by
//     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
//     *         The behaviour on a
//     *         {@code null} argument depends on the
//     *         <a href="../util/Formatter.html#syntax">conversion</a>.
//     *
//     * @throws  java.util.IllegalFormatException
//     *          If a format string contains an illegal syntax, a format
//     *          specifier that is incompatible with the given arguments,
//     *          insufficient arguments given the format string, or other
//     *          illegal conditions.  For specification of all possible
//     *          formatting errors, see the <a
//     *          href="../util/Formatter.html#detail">Details</a> section of the
//     *          formatter class specification
//     *
//     * @return  A formatted string
//     *
//     * @see  java.util.Formatter
//     * @since  1.5
//     */
//    public static String format(Locale l, String format, Object... args) {
//        return new Formatter(l).format(format, args).toString();
//    }
//
//    /**
//     * Returns the string representation of the {@code Object} argument.
//     *
//     * @param   obj   an {@code Object}.
//     * @return  if the argument is {@code null}, then a string equal to
//     *          {@code "null"}; otherwise, the value of
//     *          {@code obj.toString()} is returned.
//     * @see     java.lang.Object#toString()
//     */
//    public static String valueOf(Object obj) {
//        return (obj == null) ? "null" : obj.toString();
//    }
//
//    /**
//     * Returns the string representation of the {@code char} array
//     * argument. The contents of the character array are copied; subsequent
//     * modification of the character array does not affect the returned
//     * string.
//     *
//     * @param   data     the character array.
//     * @return  a {@code String} that contains the characters of the
//     *          character array.
//     */
//    public static String valueOf(char data[]) {
//        return new String(data);
//    }
//
//    /**
//     * Returns the string representation of a specific subarray of the
//     * {@code char} array argument.
//     * <p>
//     * The {@code offset} argument is the index of the first
//     * character of the subarray. The {@code count} argument
//     * specifies the length of the subarray. The contents of the subarray
//     * are copied; subsequent modification of the character array does not
//     * affect the returned string.
//     *
//     * @param   data     the character array.
//     * @param   offset   initial offset of the subarray.
//     * @param   count    length of the subarray.
//     * @return  a {@code String} that contains the characters of the
//     *          specified subarray of the character array.
//     * @exception IndexOutOfBoundsException if {@code offset} is
//     *          negative, or {@code count} is negative, or
//     *          {@code offset+count} is larger than
//     *          {@code data.length}.
//     */
//    public static String valueOf(char data[], int offset, int count) {
//        return new String(data, offset, count);
//    }
//
//    /**
//     * Equivalent to {@link #valueOf(char[], int, int)}.
//     *
//     * @param   data     the character array.
//     * @param   offset   initial offset of the subarray.
//     * @param   count    length of the subarray.
//     * @return  a {@code String} that contains the characters of the
//     *          specified subarray of the character array.
//     * @exception IndexOutOfBoundsException if {@code offset} is
//     *          negative, or {@code count} is negative, or
//     *          {@code offset+count} is larger than
//     *          {@code data.length}.
//     */
//    public static String copyValueOf(char data[], int offset, int count) {
//        return new String(data, offset, count);
//    }
//
//    /**
//     * Equivalent to {@link #valueOf(char[])}.
//     *
//     * @param   data   the character array.
//     * @return  a {@code String} that contains the characters of the
//     *          character array.
//     */
//    public static String copyValueOf(char data[]) {
//        return new String(data);
//    }
//
//    /**
//     * Returns the string representation of the {@code boolean} argument.
//     *
//     * @param   b   a {@code boolean}.
//     * @return  if the argument is {@code true}, a string equal to
//     *          {@code "true"} is returned; otherwise, a string equal to
//     *          {@code "false"} is returned.
//     */
//    public static String valueOf(boolean b) {
//        return b ? "true" : "false";
//    }
//
//    /**
//     * Returns the string representation of the {@code char}
//     * argument.
//     *
//     * @param   c   a {@code char}.
//     * @return  a string of length {@code 1} containing
//     *          as its single character the argument {@code c}.
//     */
//    public static String valueOf(char c) {
//        // Android-changed: Replace constructor call with call to StringFactory class.
//        // There is currently no String(char[], boolean) on Android to call. http://b/79902155
//        // char data[] = {c};
//        // return new String(data, true);
//        return StringFactory.newStringFromChars(0, 1, new char[] { c });
//    }
//
//    /**
//     * Returns the string representation of the {@code int} argument.
//     * <p>
//     * The representation is exactly the one returned by the
//     * {@code Integer.toString} method of one argument.
//     *
//     * @param   i   an {@code int}.
//     * @return  a string representation of the {@code int} argument.
//     * @see     java.lang.Integer#toString(int, int)
//     */
//    public static String valueOf(int i) {
//        return Integer.toString(i);
//    }
//
//    /**
//     * Returns the string representation of the {@code long} argument.
//     * <p>
//     * The representation is exactly the one returned by the
//     * {@code Long.toString} method of one argument.
//     *
//     * @param   l   a {@code long}.
//     * @return  a string representation of the {@code long} argument.
//     * @see     java.lang.Long#toString(long)
//     */
//    public static String valueOf(long l) {
//        return Long.toString(l);
//    }
//
//    /**
//     * Returns the string representation of the {@code float} argument.
//     * <p>
//     * The representation is exactly the one returned by the
//     * {@code Float.toString} method of one argument.
//     *
//     * @param   f   a {@code float}.
//     * @return  a string representation of the {@code float} argument.
//     * @see     java.lang.Float#toString(float)
//     */
//    public static String valueOf(float f) {
//        return Float.toString(f);
//    }
//
//    /**
//     * Returns the string representation of the {@code double} argument.
//     * <p>
//     * The representation is exactly the one returned by the
//     * {@code Double.toString} method of one argument.
//     *
//     * @param   d   a {@code double}.
//     * @return  a  string representation of the {@code double} argument.
//     * @see     java.lang.Double#toString(double)
//     */
//    public static String valueOf(double d) {
//        return Double.toString(d);
//    }
//
//    /**
//     * Returns a canonical representation for the string object.
//     * <p>
//     * A pool of strings, initially empty, is maintained privately by the
//     * class {@code String}.
//     * <p>
//     * When the intern method is invoked, if the pool already contains a
//     * string equal to this {@code String} object as determined by
//     * the {@link #equals(Object)} method, then the string from the pool is
//     * returned. Otherwise, this {@code String} object is added to the
//     * pool and a reference to this {@code String} object is returned.
//     * <p>
//     * It follows that for any two strings {@code s} and {@code t},
//     * {@code s.intern() == t.intern()} is {@code true}
//     * if and only if {@code s.equals(t)} is {@code true}.
//     * <p>
//     * All literal strings and string-valued constant expressions are
//     * interned. String literals are defined in section 3.10.5 of the
//     * <cite>The Java&trade; Language Specification</cite>.
//     *
//     * @return  a string that has the same contents as this string, but is
//     *          guaranteed to be from a pool of unique strings.
//     */
//    // BEGIN Android-changed: Annotate native method as @FastNative.
//    @FastNative
//    // END Android-changed: Annotate native method as @FastNative.
//    public native String intern();
//}
