/*
 * Copyright (c) 1994, 2000, Oracle and/or its affiliates. All rights reserved.
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
 *      Link foreign methods.  This first half of this file contains the
 *      machine independent dynamic linking routines.
 *      See "BUILD_PLATFORM"/java/lang/linker_md.c to see
 *      the implementation of this shared dynamic linking
 *      interface.
 *
 *      NOTE - source in this file is POSIX.1 compliant, host
 *             specific code lives in the platform specific
 *             code tree.
 */

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"

#include <nativehelper/JNIHelp.h>

#include "nativehelper/jni_macros.h"

JNIEXPORT jlong JNICALL
Runtime_freeMemory(JNIEnv *env, jobject this)
{
    return JVM_FreeMemory();
}

JNIEXPORT jlong JNICALL
Runtime_totalMemory(JNIEnv *env, jobject this)
{
    return JVM_TotalMemory();
}

JNIEXPORT jlong JNICALL
Runtime_maxMemory(JNIEnv *env, jobject this)
{
    return JVM_MaxMemory();
}

JNIEXPORT void JNICALL
Runtime_nativeGc(JNIEnv *env, jobject this)
{
    JVM_GC();
}

JNIEXPORT void JNICALL
Runtime_nativeExit(JNIEnv *env, jclass this, jint status)
{
    JVM_Exit(status);
}

JNIEXPORT jstring JNICALL
Runtime_nativeLoad(JNIEnv* env, jclass ignored, jstring javaFilename,
                   jobject javaLoader, jclass caller)
{
    return JVM_NativeLoad(env, javaFilename, javaLoader, caller);
}

static JNINativeMethod gMethods[] = {
  FAST_NATIVE_METHOD(Runtime, freeMemory, "()J"),
  FAST_NATIVE_METHOD(Runtime, totalMemory, "()J"),
  FAST_NATIVE_METHOD(Runtime, maxMemory, "()J"),
  NATIVE_METHOD(Runtime, nativeGc, "()V"),
  NATIVE_METHOD(Runtime, nativeExit, "(I)V"),
  NATIVE_METHOD(Runtime, nativeLoad,
                "(Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/lang/Class;)"
                    "Ljava/lang/String;"),
};

void register_java_lang_Runtime(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/lang/Runtime", gMethods, NELEM(gMethods));
}
