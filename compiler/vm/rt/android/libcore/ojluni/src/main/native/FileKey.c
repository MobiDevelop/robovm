/*
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
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

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "nio.h"
#include "nio_util.h"
#include <nativehelper/JNIHelp.h>

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

// Android-changed: Alias *64 on Fuchsia builds. http://b/119496969
// #ifdef _ALLBSD_SOURCE
#if defined(_ALLBSD_SOURCE) || defined(__Fuchsia__)
#define stat64 stat

#define fstat64 fstat
#endif

static jfieldID key_st_dev;    /* id for FileKey.st_dev */
static jfieldID key_st_ino;    /* id for FileKey.st_ino */


static void FileKey_initIDs(JNIEnv *env)
{
    jclass clazz = (*env)->FindClass(env, "sun/nio/ch/FileKey");
    key_st_dev = (*env)->GetFieldID(env, clazz, "st_dev", "J");
    key_st_ino = (*env)->GetFieldID(env, clazz, "st_ino", "J");
}


JNIEXPORT void JNICALL
FileKey_init(JNIEnv *env, jobject this, jobject fdo)
{
    struct stat64 fbuf;
    int res;

    RESTARTABLE(fstat64(fdval(env, fdo), &fbuf), res);
    if (res < 0) {
        JNU_ThrowIOExceptionWithLastError(env, "fstat64 failed");
    } else {
        (*env)->SetLongField(env, this, key_st_dev, (jlong)fbuf.st_dev);
        (*env)->SetLongField(env, this, key_st_ino, (jlong)fbuf.st_ino);
    }
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(FileKey, init, "(Ljava/io/FileDescriptor;)V"),
};

void register_sun_nio_ch_FileKey(JNIEnv* env) {
    jniRegisterNativeMethods(env, "sun/nio/ch/FileKey", gMethods, NELEM(gMethods));
    FileKey_initIDs(env);
}
