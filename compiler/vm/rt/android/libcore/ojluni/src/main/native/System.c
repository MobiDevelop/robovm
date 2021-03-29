/*
 * Copyright (c) 1994, 2010, Oracle and/or its affiliates. All rights reserved.
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
#define LOG_TAG "libcore"

#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <log/log.h>

#include "io_util.h"
#include "jni.h"
#include "jni_util.h"
#include "jvm.h"

#include "openssl/opensslv.h"
#include "zlib.h"
#include <nativehelper/JNIHelp.h>
#if defined(__ANDROID__)
void android_get_LD_LIBRARY_PATH(char*, size_t);
#endif

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

#define PUTPROP(props, key, val) \
    if (1) { \
        jstring jkey = (*env)->NewStringUTF(env, key); \
        jstring jval = (*env)->NewStringUTF(env, val); \
        jobject r = (*env)->CallObjectMethod(env, props, putID, jkey, jval); \
        if ((*env)->ExceptionOccurred(env)) return NULL; \
        (*env)->DeleteLocalRef(env, jkey); \
        (*env)->DeleteLocalRef(env, jval); \
        (*env)->DeleteLocalRef(env, r); \
    } else ((void) 0)

/*  "key" is a char type string with only ASCII character in it.
    "val" is a nchar (typedefed in java_props.h) type string  */

#define PUTPROP_ForPlatformNString(props, key, val) \
    if (1) { \
        jstring jkey = (*env)->NewStringUTF(env, key);  \
        jstring jval = GetStringPlatform(env, val); \
        jobject r = (*env)->CallObjectMethod(env, props, putID, jkey, jval); \
        if ((*env)->ExceptionOccurred(env)) return NULL; \
        (*env)->DeleteLocalRef(env, jkey); \
        (*env)->DeleteLocalRef(env, jval); \
        (*env)->DeleteLocalRef(env, r); \
    } else ((void) 0)
#define REMOVEPROP(props, key) \
    if (1) { \
        jstring jkey = JNU_NewStringPlatform(env, key); \
        jobject r = (*env)->CallObjectMethod(env, props, removeID, jkey); \
        if ((*env)->ExceptionOccurred(env)) return NULL; \
        (*env)->DeleteLocalRef(env, jkey); \
        (*env)->DeleteLocalRef(env, r); \
    } else ((void) 0)
#define GETPROP(props, key, jret) \
    if (1) { \
        jstring jkey = JNU_NewStringPlatform(env, key); \
        (jret) = (*env)->CallObjectMethod(env, props, getPropID, jkey); \
        if ((*env)->ExceptionOccurred(env)) return NULL; \
        (*env)->DeleteLocalRef(env, jkey); \
    } else ((void) 0)

#ifndef VENDOR /* Third party may overwrite this. */
#define VENDOR "Oracle Corporation"
#define VENDOR_URL "http://java.oracle.com/"
#define VENDOR_URL_BUG "http://bugreport.sun.com/bugreport/"
#endif

#define JAVA_MAX_SUPPORTED_VERSION 51
#define JAVA_MAX_SUPPORTED_MINOR_VERSION 0

#ifdef JAVA_SPECIFICATION_VENDOR /* Third party may NOT overwrite this. */
  #error "ERROR: No override of JAVA_SPECIFICATION_VENDOR is allowed"
#else
  #define JAVA_SPECIFICATION_VENDOR "Oracle Corporation"
#endif

/*
 * The following three functions implement setter methods for
 * java.lang.System.{in, out, err}. They are natively implemented
 * because they violate the semantics of the language (i.e. set final
 * variable).
 */
JNIEXPORT void JNICALL
System_setIn0(JNIEnv *env, jclass cla, jobject stream)
{
    jfieldID fid =
        (*env)->GetStaticFieldID(env,cla,"in","Ljava/io/InputStream;");
    if (fid == 0)
        return;
    (*env)->SetStaticObjectField(env,cla,fid,stream);
}

JNIEXPORT void JNICALL
System_setOut0(JNIEnv *env, jclass cla, jobject stream)
{
    jfieldID fid =
        (*env)->GetStaticFieldID(env,cla,"out","Ljava/io/PrintStream;");
    if (fid == 0)
        return;
    (*env)->SetStaticObjectField(env,cla,fid,stream);
}

JNIEXPORT void JNICALL
System_setErr0(JNIEnv *env, jclass cla, jobject stream)
{
    jfieldID fid =
        (*env)->GetStaticFieldID(env,cla,"err","Ljava/io/PrintStream;");
    if (fid == 0)
        return;
    (*env)->SetStaticObjectField(env,cla,fid,stream);
}

static void cpchars(jchar *dst, char *src, int n)
{
    int i;
    for (i = 0; i < n; i++) {
        dst[i] = src[i];
    }
}

JNIEXPORT jstring JNICALL
System_mapLibraryName(JNIEnv *env, jclass ign, jstring libname)
{
    int len;
    int prefix_len = (int) strlen(JNI_LIB_PREFIX);
    int suffix_len = (int) strlen(JNI_LIB_SUFFIX);

    jchar chars[256];
    if (libname == NULL) {
        JNU_ThrowNullPointerException(env, 0);
        return NULL;
    }
    len = (*env)->GetStringLength(env, libname);
    if (len > 240) {
        JNU_ThrowIllegalArgumentException(env, "name too long");
        return NULL;
    }
    cpchars(chars, JNI_LIB_PREFIX, prefix_len);
    (*env)->GetStringRegion(env, libname, 0, len, chars + prefix_len);
    len += prefix_len;
    cpchars(chars + len, JNI_LIB_SUFFIX, suffix_len);
    len += suffix_len;

    return (*env)->NewString(env, chars, len);
}

static jobjectArray System_specialProperties(JNIEnv* env, jclass ignored) {
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray result = (*env)->NewObjectArray(env, 4, stringClass, NULL);

    char path[PATH_MAX];
    char* process_path = getcwd(path, sizeof(path));
    char user_dir[PATH_MAX + 10] = "user.dir=";
    strncat(user_dir, process_path, PATH_MAX);
    jstring user_dir_str = (*env)->NewStringUTF(env, user_dir);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    (*env)->SetObjectArrayElement(env, result, 0, user_dir_str);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    jstring zlib_str = (*env)->NewStringUTF(env, "android.zlib.version=" ZLIB_VERSION);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    (*env)->SetObjectArrayElement(env, result, 1, zlib_str);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    jstring ssl_str = (*env)->NewStringUTF(env, "android.openssl.version=" OPENSSL_VERSION_TEXT);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    (*env)->SetObjectArrayElement(env, result, 2, ssl_str);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }


    const char* library_path = getenv("LD_LIBRARY_PATH");
#if defined(__ANDROID__)
    if (library_path == NULL) {
        android_get_LD_LIBRARY_PATH(path, sizeof(path));
        library_path = path;
    }
#endif
    if (library_path == NULL) {
        library_path = "";
    }
    char* java_path = malloc(strlen("java.library.path=") + strlen(library_path) + 1);
    strcpy(java_path, "java.library.path=");
    strcat(java_path, library_path);
    jstring java_path_str = (*env)->NewStringUTF(env, java_path);
    free((void*)java_path);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }
    (*env)->SetObjectArrayElement(env, result, 3, java_path_str);
    if ((*env)->ExceptionCheck(env)) {
        return NULL;
    }

    return result;
}

static void System_log(JNIEnv* env, jclass ignored, jchar type, jstring javaMessage, jthrowable exception) {
    int priority;
    switch (type) {
    case 'D': case 'd': priority = ANDROID_LOG_DEBUG;   break;
    case 'E': case 'e': priority = ANDROID_LOG_ERROR;   break;
    case 'F': case 'f': priority = ANDROID_LOG_FATAL;   break;
    case 'I': case 'i': priority = ANDROID_LOG_INFO;    break;
    case 'S': case 's': priority = ANDROID_LOG_SILENT;  break;
    case 'V': case 'v': priority = ANDROID_LOG_VERBOSE; break;
    case 'W': case 'w': priority = ANDROID_LOG_WARN;    break;
    default:            priority = ANDROID_LOG_DEFAULT; break;
    }

    WITH_PLATFORM_STRING(env, javaMessage, message) {
      LOG_PRI(priority, "System", "%s", message);
    } END_PLATFORM_STRING(env, message);

    if (exception != NULL) {
        jniLogException(env, priority, "System", exception);
    }
}

static jlong System_nanoTime(JNIEnv* env, jclass unused) {
  struct timespec now;
  clock_gettime(CLOCK_MONOTONIC, &now);
  return now.tv_sec * 1000000000LL + now.tv_nsec;
}

static jlong System_currentTimeMillis(JNIEnv* env, jclass unused) {
  return JVM_CurrentTimeMillis(NULL, NULL);
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(System, mapLibraryName, "(Ljava/lang/String;)Ljava/lang/String;"),
  NATIVE_METHOD(System, setErr0, "(Ljava/io/PrintStream;)V"),
  NATIVE_METHOD(System, setOut0, "(Ljava/io/PrintStream;)V"),
  NATIVE_METHOD(System, setIn0, "(Ljava/io/InputStream;)V"),
  NATIVE_METHOD(System, specialProperties, "()[Ljava/lang/String;"),
  NATIVE_METHOD(System, log, "(CLjava/lang/String;Ljava/lang/Throwable;)V"),
  NATIVE_METHOD(System, currentTimeMillis, "()J"),
  NATIVE_METHOD(System, nanoTime, "()J"),
};

void register_java_lang_System(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/lang/System", gMethods, NELEM(gMethods));
}
