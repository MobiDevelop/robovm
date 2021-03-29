/*
 * Copyright (c) 2001, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * Solaris/Linux platform specific code to support the Prefs API.
 */

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <utime.h>
#include "jni_util.h"

#include <nativehelper/JNIHelp.h>

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(Java_java_util_prefs_ ## className ## _ ## functionName) }

JNIEXPORT jint JNICALL
Java_java_util_prefs_FileSystemPreferences_chmod(JNIEnv *env,
                       jclass thisclass, jstring java_fname, jint permission) {
    const char *fname = JNU_GetStringPlatformChars(env, java_fname, JNI_FALSE);
    int result;
    result =  chmod(fname, permission);
    if (result != 0)
       result = errno;
    JNU_ReleaseStringPlatformChars(env, java_fname, fname);
    return (jint) result;
}

// Android-changed: Fuchsia: Alias *64 on Fuchsia builds. http://b/119496969
// #if defined(_ALLBSD_SOURCE)
#if defined(_ALLBSD_SOURCE) || defined(__Fuchsia__)
typedef struct flock FLOCK;
#else
typedef struct flock64 FLOCK;
#endif

/**
 * Try to open a named lock file.
 * The result is a cookie that can be used later to unlock the file.
 * On failure the result is zero.
 */
JNIEXPORT jintArray JNICALL
Java_java_util_prefs_FileSystemPreferences_lockFile0(JNIEnv *env,
    jclass thisclass, jstring java_fname, jint permission, jboolean shared) {
    const char *fname = JNU_GetStringPlatformChars(env, java_fname, JNI_FALSE);
    int fd, rc;
    int result[2];
    jintArray javaResult;
    int old_umask;
    FLOCK fl;

    fl.l_whence = SEEK_SET;
    fl.l_len = 0;
    fl.l_start = 0;
    if (shared == JNI_TRUE) {
        fl.l_type = F_RDLCK;
    } else {
        fl.l_type = F_WRLCK;
    }

    if (shared == JNI_TRUE) {
        fd = open(fname, O_RDONLY, 0);
    } else {
        old_umask = umask(0);
        fd = open(fname, O_WRONLY|O_CREAT, permission);
        result[1] = errno;
        umask(old_umask);
    }

    if (fd < 0) {
        result[0] = 0;
    } else {
// Android-changed: Fuchsia: Alias *64 on Fuchsia builds. http://b/119496969
// #if defined(_ALLBSD_SOURCE)
#if defined(_ALLBSD_SOURCE) || defined(__Fuchsia__)
        rc = fcntl(fd, F_SETLK, &fl);
#else
        rc = fcntl(fd, F_SETLK64, &fl);
#endif
        result[1] = errno;
        if (rc < 0) {
            result[0]= 0;
            close(fd);
        } else {
          result[0] = fd;
        }
    }
    JNU_ReleaseStringPlatformChars(env, java_fname, fname);
    javaResult = (*env)->NewIntArray(env,2);
    (*env)->SetIntArrayRegion(env, javaResult, 0, 2, result);
    return javaResult;
}


/**
 * Try to unlock a lock file, using a cookie returned by lockFile.
 */
JNIEXPORT jint JNICALL
Java_java_util_prefs_FileSystemPreferences_unlockFile0(JNIEnv *env,
                                      jclass thisclass, jint fd) {

    int rc;
    FLOCK fl;
    fl.l_whence = SEEK_SET;
    fl.l_len = 0;
    fl.l_start = 0;
    fl.l_type = F_UNLCK;
// Android-changed: Fuchsia: Alias *64 on Fuchsia builds. http://b/119496969
// #if defined(_ALLBSD_SOURCE)
#if defined(_ALLBSD_SOURCE) || defined(__Fuchsia__)
    rc = fcntl(fd, F_SETLK, &fl);
#else
    rc = fcntl(fd, F_SETLK64, &fl);
#endif

    if (rc < 0) {
        close(fd);
        return (jint)errno;
    }
    rc = close(fd);
    if (rc < 0) {
        return (jint) errno;
    }
    return 0;
}

static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(FileSystemPreferences, lockFile0, "(Ljava/lang/String;IZ)[I"),
    NATIVE_METHOD(FileSystemPreferences, unlockFile0, "(I)I"),
    NATIVE_METHOD(FileSystemPreferences, chmod, "(Ljava/lang/String;I)I"),
};

void register_java_util_prefs_FileSystemPreferences(JNIEnv* env) {
    jniRegisterNativeMethods(env, "java/util/prefs/FileSystemPreferences", gMethods, NELEM(gMethods));
}
