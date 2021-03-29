/* Copyright (C) 2014 The Android Open Source Project
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file implements interfaces from the file jvm.h. This implementation
 * is licensed under the same terms as the file jvm.h.  The
 * copyright and license information for the file jvm.h follows.
 *
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

/*
 * Services that OpenJDK expects the VM to provide.
 */
#include <dlfcn.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <unistd.h>
#include <math.h>
#include <android-base/logging.h>

#include "../../libcore/ojluni/src/main/native/jvm.h"  // TODO(narayan): fix it

#undef LOG_TAG
#define LOG_TAG "artopenjdk"

/* posix open() with extensions; used by e.g. ZipFile */
JNIEXPORT jint JVM_Open(const char* fname, jint flags, jint mode) {
    /*
     * Some code seems to want the special return value JVM_EEXIST if the
     * file open fails due to O_EXCL.
     */
    // Don't use JVM_O_DELETE, it's problematic with FUSE, see b/28901232.
    if (flags & JVM_O_DELETE) {
        LOG(FATAL) << "JVM_O_DELETE option is not supported (while opening: '"
                   << fname << "')";
    }

    flags |= O_CLOEXEC;
    int fd = TEMP_FAILURE_RETRY(open(fname, flags & ~JVM_O_DELETE, mode));
    if (fd < 0) {
        int err = errno;
        if (err == EEXIST) {
            return JVM_EEXIST;
        } else {
            return -1;
        }
    }

    return fd;
}

/* posix close() */
JNIEXPORT jint JVM_Close(jint fd) {
    // don't want TEMP_FAILURE_RETRY here -- file is closed even if EINTR
    return close(fd);
}

/* posix read() */
JNIEXPORT jint JVM_Read(jint fd, char* buf, jint nbytes) {
    return TEMP_FAILURE_RETRY(read(fd, buf, nbytes));
}

/* posix write(); is used to write messages to stderr */
JNIEXPORT jint JVM_Write(jint fd, char* buf, jint nbytes) {
    return TEMP_FAILURE_RETRY(write(fd, buf, nbytes));
}

/* posix lseek() */
JNIEXPORT jlong JVM_Lseek(jint fd, jlong offset, jint whence) {
#if !defined(__APPLE__)
    // NOTE: Using TEMP_FAILURE_RETRY here is busted for LP32 on glibc - the return
    // value will be coerced into an int32_t.
    //
    // lseek64 isn't specified to return EINTR so it shouldn't be necessary
    // anyway.
    return lseek64(fd, offset, whence);
#else
    // NOTE: This code is compiled for Mac OS but isn't ever run on that
    // platform.
    return lseek(fd, offset, whence);
#endif
}

/*
 * "raw monitors" seem to be expected to behave like non-recursive pthread
 * mutexes.  They're used by ZipFile.
 */
JNIEXPORT void* JVM_RawMonitorCreate(void) {
    pthread_mutex_t* mutex =
        reinterpret_cast<pthread_mutex_t*>(malloc(sizeof(pthread_mutex_t)));
    CHECK(mutex != nullptr);
    CHECK_PTHREAD_CALL(pthread_mutex_init, (mutex, nullptr), "JVM_RawMonitorCreate");
    return mutex;
}

JNIEXPORT void JVM_RawMonitorDestroy(void* mon) {
    CHECK_PTHREAD_CALL(pthread_mutex_destroy,
                       (reinterpret_cast<pthread_mutex_t*>(mon)),
                       "JVM_RawMonitorDestroy");
    free(mon);
}

JNIEXPORT jint JVM_RawMonitorEnter(void* mon) {
    return pthread_mutex_lock(reinterpret_cast<pthread_mutex_t*>(mon));
}

JNIEXPORT void JVM_RawMonitorExit(void* mon) {
    CHECK_PTHREAD_CALL(pthread_mutex_unlock,
                       (reinterpret_cast<pthread_mutex_t*>(mon)),
                       "JVM_RawMonitorExit");
}

JNIEXPORT char* JVM_NativePath(char* path) {
    return path;
}

JNIEXPORT jint JVM_GetLastErrorString(char* buf, int len) {
#if defined(__GLIBC__) || defined(__BIONIC__)
  if (len == 0) {
    return 0;
  }

  const int err = errno;
  char* result = strerror_r(err, buf, len);
  if (result != buf) {
    strncpy(buf, result, len);
    buf[len - 1] = '\0';
  }

  return strlen(buf);
#else
  UNUSED(buf);
  UNUSED(len);
  return -1;
#endif
}

JNIEXPORT int jio_fprintf(FILE* fp, const char* fmt, ...) {
    va_list args;

    va_start(args, fmt);
    int len = jio_vfprintf(fp, fmt, args);
    va_end(args);

    return len;
}

JNIEXPORT int jio_vfprintf(FILE* fp, const char* fmt, va_list args) {
    assert(fp != nullptr);
    return vfprintf(fp, fmt, args);
}

/* posix fsync() */
JNIEXPORT jint JVM_Sync(jint fd) {
    return TEMP_FAILURE_RETRY(fsync(fd));
}

JNIEXPORT void* JVM_FindLibraryEntry(void* handle, const char* name) {
    return dlsym(handle, name);
}

JNIEXPORT jlong JVM_CurrentTimeMillis(JNIEnv* env ATTRIBUTE_UNUSED,
                                      jclass clazz ATTRIBUTE_UNUSED) {
    struct timeval tv;
    gettimeofday(&tv, (struct timezone *) nullptr);
    jlong when = tv.tv_sec * 1000LL + tv.tv_usec / 1000;
    return when;
}

JNIEXPORT jint JVM_InitializeSocketLibrary() {
  // FIXME: TODO: is it required?
  return 0;
}

JNIEXPORT jlong JVM_FreeMemory(void) {
  // FIXME: TODO: implement
  return 0;
}

JNIEXPORT jlong JVM_TotalMemory(void) {
  // FIXME: TODO: implement
  return 0;
}

JNIEXPORT jlong JVM_MaxMemory(void) {
  // FIXME: TODO: implement
  return 0;
}

JNIEXPORT void JVM_GC(void) {
  // FIXME: TODO: is it required ?
}

JNIEXPORT __attribute__((noreturn)) void JVM_Exit(jint status) {
  LOG(INFO) << "System.exit called, status: " << status;
  exit(status);
}

JNIEXPORT jstring JVM_NativeLoad(JNIEnv* env,
                                 jstring javaFilename,
                                 jobject javaLoader,
                                 jclass caller) {
  // FIXME:  TODO: implement
  return env->NewStringUTF("not implemented: JVM_NativeLoad\0");
}

JNIEXPORT jboolean JVM_IsNaN(jdouble d) {
  return isnan(d);
}
