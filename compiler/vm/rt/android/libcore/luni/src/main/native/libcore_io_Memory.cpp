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

#define LOG_TAG "Memory"

#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>

#include <nativehelper/JNIHelp.h>
#include <nativehelper/ScopedBytes.h>
#include <nativehelper/ScopedPrimitiveArray.h>
#include <nativehelper/jni_macros.h>

#include "JniConstants.h"
#include "Portability.h"

// Use packed structures for access to unaligned data on targets with alignment restrictions.
// The compiler will generate appropriate code to access these structures without
// generating alignment exceptions.
template <typename T> static inline T get_unaligned(const T* address) {
    struct unaligned { T v; } __attribute__ ((packed));
    const unaligned* p = reinterpret_cast<const unaligned*>(address);
    return p->v;
}

template <typename T> static inline void put_unaligned(T* address, T v) {
    struct unaligned { T v; } __attribute__ ((packed));
    unaligned* p = reinterpret_cast<unaligned*>(address);
    p->v = v;
}

template <typename T> static T cast(jlong address) {
    return reinterpret_cast<T>(static_cast<uintptr_t>(address));
}

// Byte-swap 2 jshort values packed in a jint.
static inline jint bswap_2x16(jint v) {
    // v is initially ABCD
#if defined(__mips__) && defined(__mips_isa_rev) && (__mips_isa_rev >= 2)
    __asm__ volatile ("wsbh %0, %0" : "+r" (v));  // v=BADC
#else
    v = bswap_32(v);                              // v=DCBA
    v = (v << 16) | ((v >> 16) & 0xffff);         // v=BADC
#endif
    return v;
}

static inline void swapShorts(jshort* dstShorts, const jshort* srcShorts, size_t count) {
    // Do 32-bit swaps as long as possible...
    jint* dst = reinterpret_cast<jint*>(dstShorts);
    const jint* src = reinterpret_cast<const jint*>(srcShorts);
    for (size_t i = 0; i < count / 2; ++i) {
        jint v = get_unaligned<jint>(src++);
        put_unaligned<jint>(dst++, bswap_2x16(v));
    }
    if ((count % 2) != 0) {
      jshort v = get_unaligned<jshort>(reinterpret_cast<const jshort*>(src));
      put_unaligned<jshort>(reinterpret_cast<jshort*>(dst), bswap_16(v));
    }
}

static inline void swapInts(jint* dstInts, const jint* srcInts, size_t count) {
    for (size_t i = 0; i < count; ++i) {
        jint v = get_unaligned<int>(srcInts++);
        put_unaligned<jint>(dstInts++, bswap_32(v));
    }
}

static inline void swapLongs(jlong* dstLongs, const jlong* srcLongs, size_t count) {
    jint* dst = reinterpret_cast<jint*>(dstLongs);
    const jint* src = reinterpret_cast<const jint*>(srcLongs);
    for (size_t i = 0; i < count; ++i) {
        jint v1 = get_unaligned<jint>(src++);
        jint v2 = get_unaligned<jint>(src++);
        put_unaligned<jint>(dst++, bswap_32(v2));
        put_unaligned<jint>(dst++, bswap_32(v1));
    }
}

static void Memory_memmove(JNIEnv* env, jclass, jobject dstObject, jint dstOffset, jobject srcObject, jint srcOffset, jlong length) {
    ScopedBytesRW dstBytes(env, dstObject);
    if (dstBytes.get() == NULL) {
        return;
    }
    ScopedBytesRO srcBytes(env, srcObject);
    if (srcBytes.get() == NULL) {
        return;
    }
    memmove(dstBytes.get() + dstOffset, srcBytes.get() + srcOffset, length);
}

static jbyte Memory_peekByte(JNIEnv*, jclass, jlong srcAddress) {
    return *cast<const jbyte*>(srcAddress);
}

static void Memory_peekByteArray(JNIEnv* env, jclass, jlong srcAddress, jbyteArray dst, jint dstOffset, jint byteCount) {
    env->SetByteArrayRegion(dst, dstOffset, byteCount, cast<const jbyte*>(srcAddress));
}

// Implements the peekXArray methods:
// - For unswapped access, we just use the JNI SetXArrayRegion functions.
// - For swapped access, we use GetXArrayElements and our own copy-and-swap routines.
//   GetXArrayElements is disproportionately cheap on Dalvik because it doesn't copy (as opposed
//   to Hotspot, which always copies). The SWAP_FN copies and swaps in one pass, which is cheaper
//   than copying and then swapping in a second pass. Depending on future VM/GC changes, the
//   swapped case might need to be revisited.
#define PEEKER(SCALAR_TYPE, JNI_NAME, SWAP_TYPE, SWAP_FN) { \
    if (swap) { \
        Scoped ## JNI_NAME ## ArrayRW elements(env, dst); \
        if (elements.get() == NULL) { \
            return; \
        } \
        const SWAP_TYPE* src = cast<const SWAP_TYPE*>(srcAddress); \
        SWAP_FN(reinterpret_cast<SWAP_TYPE*>(elements.get()) + dstOffset, src, count); /*NOLINT*/ \
    } else { \
        const SCALAR_TYPE* src = cast<const SCALAR_TYPE*>(srcAddress); \
        env->Set ## JNI_NAME ## ArrayRegion(dst, dstOffset, count, src); \
    } \
}

static void Memory_peekCharArray(JNIEnv* env, jclass, jlong srcAddress, jcharArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jchar, Char, jshort, swapShorts);
}

static void Memory_peekDoubleArray(JNIEnv* env, jclass, jlong srcAddress, jdoubleArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jdouble, Double, jlong, swapLongs);
}

static void Memory_peekFloatArray(JNIEnv* env, jclass, jlong srcAddress, jfloatArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jfloat, Float, jint, swapInts);
}

static void Memory_peekIntArray(JNIEnv* env, jclass, jlong srcAddress, jintArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jint, Int, jint, swapInts);
}

static void Memory_peekLongArray(JNIEnv* env, jclass, jlong srcAddress, jlongArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jlong, Long, jlong, swapLongs);
}

static void Memory_peekShortArray(JNIEnv* env, jclass, jlong srcAddress, jshortArray dst, jint dstOffset, jint count, jboolean swap) {
    PEEKER(jshort, Short, jshort, swapShorts);
}

static void Memory_pokeByte(JNIEnv*, jclass, jlong dstAddress, jbyte value) {
    *cast<jbyte*>(dstAddress) = value;
}

static void Memory_pokeByteArray(JNIEnv* env, jclass, jlong dstAddress, jbyteArray src, jint offset, jint length) {
    env->GetByteArrayRegion(src, offset, length, cast<jbyte*>(dstAddress));
}

// Implements the pokeXArray methods:
// - For unswapped access, we just use the JNI GetXArrayRegion functions.
// - For swapped access, we use GetXArrayElements and our own copy-and-swap routines.
//   GetXArrayElements is disproportionately cheap on Dalvik because it doesn't copy (as opposed
//   to Hotspot, which always copies). The SWAP_FN copies and swaps in one pass, which is cheaper
//   than copying and then swapping in a second pass. Depending on future VM/GC changes, the
//   swapped case might need to be revisited.
#define POKER(SCALAR_TYPE, JNI_NAME, SWAP_TYPE, SWAP_FN) { \
    if (swap) { \
        Scoped ## JNI_NAME ## ArrayRO elements(env, src); \
        if (elements.get() == NULL) { \
            return; \
        } \
        const SWAP_TYPE* src = reinterpret_cast<const SWAP_TYPE*>(elements.get()) + srcOffset; \
        SWAP_FN(cast<SWAP_TYPE*>(dstAddress), src, count); /*NOLINT*/ \
    } else { \
        env->Get ## JNI_NAME ## ArrayRegion(src, srcOffset, count, cast<SCALAR_TYPE*>(dstAddress)); /*NOLINT*/ \
    } \
}

static void Memory_pokeCharArray(JNIEnv* env, jclass, jlong dstAddress, jcharArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jchar, Char, jshort, swapShorts);
}

static void Memory_pokeDoubleArray(JNIEnv* env, jclass, jlong dstAddress, jdoubleArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jdouble, Double, jlong, swapLongs);
}

static void Memory_pokeFloatArray(JNIEnv* env, jclass, jlong dstAddress, jfloatArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jfloat, Float, jint, swapInts);
}

static void Memory_pokeIntArray(JNIEnv* env, jclass, jlong dstAddress, jintArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jint, Int, jint, swapInts);
}

static void Memory_pokeLongArray(JNIEnv* env, jclass, jlong dstAddress, jlongArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jlong, Long, jlong, swapLongs);
}

static void Memory_pokeShortArray(JNIEnv* env, jclass, jlong dstAddress, jshortArray src, jint srcOffset, jint count, jboolean swap) {
    POKER(jshort, Short, jshort, swapShorts);
}

static jshort Memory_peekShortNative(JNIEnv*, jclass, jlong srcAddress) {
    return get_unaligned<jshort>(cast<const jshort*>(srcAddress));
}

static void Memory_pokeShortNative(JNIEnv*, jclass, jlong dstAddress, jshort value) {
    put_unaligned<jshort>(cast<jshort*>(dstAddress), value);
}

static jint Memory_peekIntNative(JNIEnv*, jclass, jlong srcAddress) {
    return get_unaligned<jint>(cast<const jint*>(srcAddress));
}

static void Memory_pokeIntNative(JNIEnv*, jclass, jlong dstAddress, jint value) {
    put_unaligned<jint>(cast<jint*>(dstAddress), value);
}

static jlong Memory_peekLongNative(JNIEnv*, jclass, jlong srcAddress) {
    return get_unaligned<jlong>(cast<const jlong*>(srcAddress));
}

static void Memory_pokeLongNative(JNIEnv*, jclass, jlong dstAddress, jlong value) {
    put_unaligned<jlong>(cast<jlong*>(dstAddress), value);
}

static void unsafeBulkCopy(jbyte* dst, const jbyte* src, jint byteCount,
        jint sizeofElement, jboolean swap) {
    if (!swap) {
        memcpy(dst, src, byteCount);
        return;
    }

    if (sizeofElement == 2) {
        jshort* dstShorts = reinterpret_cast<jshort*>(dst);
        const jshort* srcShorts = reinterpret_cast<const jshort*>(src);
        swapShorts(dstShorts, srcShorts, byteCount / 2);
    } else if (sizeofElement == 4) {
        jint* dstInts = reinterpret_cast<jint*>(dst);
        const jint* srcInts = reinterpret_cast<const jint*>(src);
        swapInts(dstInts, srcInts, byteCount / 4);
    } else if (sizeofElement == 8) {
        jlong* dstLongs = reinterpret_cast<jlong*>(dst);
        const jlong* srcLongs = reinterpret_cast<const jlong*>(src);
        swapLongs(dstLongs, srcLongs, byteCount / 8);
    }
}

static void Memory_unsafeBulkGet(JNIEnv* env, jclass, jobject dstObject, jint dstOffset,
        jint byteCount, jbyteArray srcArray, jint srcOffset, jint sizeofElement, jboolean swap) {
    ScopedByteArrayRO srcBytes(env, srcArray);
    if (srcBytes.get() == NULL) {
        return;
    }
    jarray dstArray = reinterpret_cast<jarray>(dstObject);
    jbyte* dstBytes = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(dstArray, NULL));
    if (dstBytes == NULL) {
        return;
    }
    jbyte* dst = dstBytes + dstOffset*sizeofElement;
    const jbyte* src = srcBytes.get() + srcOffset;
    unsafeBulkCopy(dst, src, byteCount, sizeofElement, swap);
    env->ReleasePrimitiveArrayCritical(dstArray, dstBytes, 0);
}

static void Memory_unsafeBulkPut(JNIEnv* env, jclass, jbyteArray dstArray, jint dstOffset,
        jint byteCount, jobject srcObject, jint srcOffset, jint sizeofElement, jboolean swap) {
    ScopedByteArrayRW dstBytes(env, dstArray);
    if (dstBytes.get() == NULL) {
        return;
    }
    jarray srcArray = reinterpret_cast<jarray>(srcObject);
    jbyte* srcBytes = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(srcArray, NULL));
    if (srcBytes == NULL) {
        return;
    }
    jbyte* dst = dstBytes.get() + dstOffset;
    const jbyte* src = srcBytes + srcOffset*sizeofElement;
    unsafeBulkCopy(dst, src, byteCount, sizeofElement, swap);
    env->ReleasePrimitiveArrayCritical(srcArray, srcBytes, 0);
}

static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(Memory, memmove, "(Ljava/lang/Object;ILjava/lang/Object;IJ)V"),
    FAST_NATIVE_METHOD(Memory, peekByte, "(J)B"),
    NATIVE_METHOD(Memory, peekByteArray, "(J[BII)V"),
    NATIVE_METHOD(Memory, peekCharArray, "(J[CIIZ)V"),
    NATIVE_METHOD(Memory, peekDoubleArray, "(J[DIIZ)V"),
    NATIVE_METHOD(Memory, peekFloatArray, "(J[FIIZ)V"),
    FAST_NATIVE_METHOD(Memory, peekIntNative, "(J)I"),
    NATIVE_METHOD(Memory, peekIntArray, "(J[IIIZ)V"),
    FAST_NATIVE_METHOD(Memory, peekLongNative, "(J)J"),
    NATIVE_METHOD(Memory, peekLongArray, "(J[JIIZ)V"),
    FAST_NATIVE_METHOD(Memory, peekShortNative, "(J)S"),
    NATIVE_METHOD(Memory, peekShortArray, "(J[SIIZ)V"),
    FAST_NATIVE_METHOD(Memory, pokeByte, "(JB)V"),
    NATIVE_METHOD(Memory, pokeByteArray, "(J[BII)V"),
    NATIVE_METHOD(Memory, pokeCharArray, "(J[CIIZ)V"),
    NATIVE_METHOD(Memory, pokeDoubleArray, "(J[DIIZ)V"),
    NATIVE_METHOD(Memory, pokeFloatArray, "(J[FIIZ)V"),
    FAST_NATIVE_METHOD(Memory, pokeIntNative, "(JI)V"),
    NATIVE_METHOD(Memory, pokeIntArray, "(J[IIIZ)V"),
    FAST_NATIVE_METHOD(Memory, pokeLongNative, "(JJ)V"),
    NATIVE_METHOD(Memory, pokeLongArray, "(J[JIIZ)V"),
    FAST_NATIVE_METHOD(Memory, pokeShortNative, "(JS)V"),
    NATIVE_METHOD(Memory, pokeShortArray, "(J[SIIZ)V"),
    NATIVE_METHOD(Memory, unsafeBulkGet, "(Ljava/lang/Object;II[BIIZ)V"),
    NATIVE_METHOD(Memory, unsafeBulkPut, "([BIILjava/lang/Object;IIZ)V"),
};
extern "C" void register_libcore_io_Memory(JNIEnv* env) {
    jniRegisterNativeMethods(env, "libcore/io/Memory", gMethods, NELEM(gMethods));
}
