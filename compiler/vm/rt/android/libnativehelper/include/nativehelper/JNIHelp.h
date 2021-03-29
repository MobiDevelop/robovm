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

/*
 * JNI helper functions.
 *
 * This file may be included by C or C++ code, which is trouble because jni.h
 * uses different typedefs for JNIEnv in each language.
 */
#ifndef NATIVEHELPER_JNIHELP_H_
#define NATIVEHELPER_JNIHELP_H_

#include <errno.h>
#include <unistd.h>

#include <jni.h>
#include "module_api.h"

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

/*
 * Register one or more native methods with a particular class.
 * "className" looks like "java/lang/String". Aborts on failure.
 * TODO: fix all callers and change the return type to void.
 */
MODULE_API int jniRegisterNativeMethods(C_JNIEnv* env,
                                        const char* className,
                                        const JNINativeMethod* gMethods,
                                        int numMethods);

/*
 * Throw an exception with the specified class and an optional message.
 *
 * The "className" argument will be passed directly to FindClass, which
 * takes strings with slashes (e.g. "java/lang/Object").
 *
 * If an exception is currently pending, we log a warning message and
 * clear it.
 *
 * Returns 0 on success, nonzero if something failed (e.g. the exception
 * class couldn't be found, so *an* exception will still be pending).
 *
 * Currently aborts the VM if it can't throw the exception.
 */
MODULE_API int jniThrowException(C_JNIEnv* env, const char* className, const char* msg);

/*
 * Throw an exception with the specified class and formatted error message.
 *
 * The "className" argument will be passed directly to FindClass, which
 * takes strings with slashes (e.g. "java/lang/Object").
 *
 * If an exception is currently pending, we log a warning message and
 * clear it.
 *
 * Returns 0 on success, nonzero if something failed (e.g. the exception
 * class couldn't be found, so *an* exception will still be pending).
 *
 * Currently aborts the VM if it can't throw the exception.
 */
MODULE_API int jniThrowExceptionFmt(C_JNIEnv* env, const char* className, const char* fmt, va_list args);

/*
 * Throw a java.lang.NullPointerException, with an optional message.
 */
MODULE_API int jniThrowNullPointerException(C_JNIEnv* env, const char* msg);

/*
 * Throw a java.lang.RuntimeException, with an optional message.
 */
MODULE_API int jniThrowRuntimeException(C_JNIEnv* env, const char* msg);

/*
 * Throw a java.io.IOException, generating the message from errno.
 */
MODULE_API int jniThrowIOException(C_JNIEnv* env, int errnum);

/*
 * Return a pointer to a locale-dependent error string explaining errno
 * value 'errnum'. The returned pointer may or may not be equal to 'buf'.
 * This function is thread-safe (unlike strerror) and portable (unlike
 * strerror_r).
 */
MODULE_API const char* jniStrError(int errnum, char* buf, size_t buflen);

/*
 * Returns a new java.io.FileDescriptor for the given int fd.
 */
MODULE_API jobject jniCreateFileDescriptor(C_JNIEnv* env, int fd);

/*
 * Returns the int fd from a java.io.FileDescriptor.
 */
MODULE_API int jniGetFDFromFileDescriptor(C_JNIEnv* env, jobject fileDescriptor);

/*
 * Sets the int fd in a java.io.FileDescriptor.  Throws java.lang.NullPointerException
 * if fileDescriptor is null.
 */
MODULE_API void jniSetFileDescriptorOfFD(C_JNIEnv* env,
                                         jobject fileDescriptor,
                                         int value);

/*
 * Returns the long ownerId from a java.io.FileDescriptor.
 */
MODULE_API jlong jniGetOwnerIdFromFileDescriptor(C_JNIEnv* env, jobject fileDescriptor);

/*
 * Gets the managed heap array backing a java.nio.Buffer instance.
 *
 * Returns nullptr if there is no array backing.
 *
 * This method performs a JNI call to java.nio.NIOAccess.getBaseArray().
 */
MODULE_API jarray jniGetNioBufferBaseArray(C_JNIEnv* env, jobject nioBuffer);

/*
 * Gets the offset in bytes from the start of the managed heap array backing the buffer.
 *
 * Returns 0 if there is no array backing.
 *
 * This method performs a JNI call to java.nio.NIOAccess.getBaseArrayOffset().
 */
MODULE_API jint jniGetNioBufferBaseArrayOffset(C_JNIEnv* env, jobject nioBuffer);

/*
 * Gets field information from a java.nio.Buffer instance.
 *
 * Reads the |position|, |limit|, and |elementSizeShift| fields from the buffer instance.
 *
 * Returns the |address| field of the java.nio.Buffer instance which is only valid (non-zero) when
 * the buffer is backed by a direct buffer.
 */
MODULE_API jlong jniGetNioBufferFields(C_JNIEnv* env,
                                       jobject nioBuffer,
                                       /*out*/jint* position,
                                       /*out*/jint* limit,
                                       /*out*/jint* elementSizeShift);

/*
 * Gets the current position from a java.nio.Buffer as a pointer to memory in a fixed buffer.
 *
 * Returns 0 if |nioBuffer| is not backed by a direct buffer.
 *
 * This method reads the |address|, |position|, and |elementSizeShift| fields from the
 * java.nio.Buffer instance to calculate the pointer address for the current position.
 */
MODULE_API jlong jniGetNioBufferPointer(C_JNIEnv* env, jobject nioBuffer);

/*
 * Returns the reference from a java.lang.ref.Reference.
 */
MODULE_API jobject jniGetReferent(C_JNIEnv* env, jobject ref);

/*
 * Returns a Java String object created from UTF-16 data either from jchar or,
 * if called from C++11, char16_t (a bitwise identical distinct type).
 */
MODULE_API jstring jniCreateString(C_JNIEnv* env, const jchar* unicodeChars, jsize len);

/*
 * Log a message and an exception.
 * If exception is NULL, logs the current exception in the JNI environment.
 */
MODULE_API void jniLogException(C_JNIEnv* env, int priority, const char* tag, jthrowable exception);

/*
 * Clear the cache of constants libnativehelper is using.
 */
MODULE_API void jniUninitializeConstants();

/*
 * For C++ code, we provide inlines that map to the C functions.  g++ always
 * inlines these, even on non-optimized builds.
 */
#if defined(__cplusplus)

inline int jniRegisterNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods) {
    return jniRegisterNativeMethods(&env->functions, className, gMethods, numMethods);
}

inline int jniThrowException(JNIEnv* env, const char* className, const char* msg) {
    return jniThrowException(&env->functions, className, msg);
}

/*
 * Equivalent to jniThrowException but with a printf-like format string and
 * variable-length argument list. This is only available in C++.
 */
inline int jniThrowExceptionFmt(JNIEnv* env, const char* className, const char* fmt, ...) {
    va_list args;
    va_start(args, fmt);
    return jniThrowExceptionFmt(&env->functions, className, fmt, args);
    va_end(args);
}

inline int jniThrowNullPointerException(JNIEnv* env, const char* msg) {
    return jniThrowNullPointerException(&env->functions, msg);
}

inline int jniThrowRuntimeException(JNIEnv* env, const char* msg) {
    return jniThrowRuntimeException(&env->functions, msg);
}

inline int jniThrowIOException(JNIEnv* env, int errnum) {
    return jniThrowIOException(&env->functions, errnum);
}

inline jobject jniCreateFileDescriptor(JNIEnv* env, int fd) {
    return jniCreateFileDescriptor(&env->functions, fd);
}

inline int jniGetFDFromFileDescriptor(JNIEnv* env, jobject fileDescriptor) {
    return jniGetFDFromFileDescriptor(&env->functions, fileDescriptor);
}

inline void jniSetFileDescriptorOfFD(JNIEnv* env, jobject fileDescriptor, int value) {
    jniSetFileDescriptorOfFD(&env->functions, fileDescriptor, value);
}

inline jlong jniGetOwnerIdFromFileDescriptor(JNIEnv* env, jobject fileDescriptor) {
    return jniGetOwnerIdFromFileDescriptor(&env->functions, fileDescriptor);
}

inline jarray jniGetNioBufferBaseArray(JNIEnv* env, jobject nioBuffer) {
    return jniGetNioBufferBaseArray(&env->functions, nioBuffer);
}

inline jint jniGetNioBufferBaseArrayOffset(JNIEnv* env, jobject nioBuffer) {
    return jniGetNioBufferBaseArrayOffset(&env->functions, nioBuffer);
}

inline jlong jniGetNioBufferFields(JNIEnv* env, jobject nioBuffer,
                                   jint* position, jint* limit, jint* elementSizeShift) {
    return jniGetNioBufferFields(&env->functions, nioBuffer,
                                 position, limit, elementSizeShift);
}

inline jlong jniGetNioBufferPointer(JNIEnv* env, jobject nioBuffer) {
    return jniGetNioBufferPointer(&env->functions, nioBuffer);
}

inline jobject jniGetReferent(JNIEnv* env, jobject ref) {
    return jniGetReferent(&env->functions, ref);
}

inline jstring jniCreateString(JNIEnv* env, const jchar* unicodeChars, jsize len) {
    return jniCreateString(&env->functions, unicodeChars, len);
}

inline jstring jniCreateString(JNIEnv* env, const char16_t* unicodeChars, jsize len) {
    return jniCreateString(&env->functions, reinterpret_cast<const jchar*>(unicodeChars), len);
}

inline void jniLogException(JNIEnv* env, int priority, const char* tag, jthrowable exception = NULL) {
    jniLogException(&env->functions, priority, tag, exception);
}

#if !defined(DISALLOW_COPY_AND_ASSIGN)
// DISALLOW_COPY_AND_ASSIGN disallows the copy and operator= functions. It goes in the private:
// declarations in a class.
#define DISALLOW_COPY_AND_ASSIGN(TypeName) \
  TypeName(const TypeName&) = delete;  \
  void operator=(const TypeName&) = delete
#endif  // !defined(DISALLOW_COPY_AND_ASSIGN)

#endif  // defined(__cplusplus)

/*
 * TEMP_FAILURE_RETRY is defined by some, but not all, versions of
 * <unistd.h>. (Alas, it is not as standard as we'd hoped!) So, if it's
 * not already defined, then define it here.
 */
#ifndef TEMP_FAILURE_RETRY
/* Used to retry syscalls that can return EINTR. */
#define TEMP_FAILURE_RETRY(exp) ({         \
    typeof (exp) _rc;                      \
    do {                                   \
        _rc = (exp);                       \
    } while (_rc == -1 && errno == EINTR); \
    _rc; })
#endif

#endif  /* NATIVEHELPER_JNIHELP_H_ */
