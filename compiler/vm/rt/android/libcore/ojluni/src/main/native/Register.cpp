/* Copyright (C) 2014 The Android Open Source Project
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

#define LOG_TAG "libcore" // We'll be next to "dalvikvm" in the log; make the distinction clear.

#include <stdlib.h>

#include <log/log.h>

#include <nativehelper/ScopedLocalFrame.h>

#include "JniConstants.h"

extern "C" {

extern void register_java_io_Console(JNIEnv* env);
extern void register_java_io_FileDescriptor(JNIEnv*);
extern void register_java_io_FileInputStream(JNIEnv*);
extern void register_java_io_FileOutputStream(JNIEnv*);
extern void register_java_io_ObjectInputStream(JNIEnv*);
extern void register_java_io_ObjectOutputStream(JNIEnv*);
extern void register_java_io_ObjectStreamClass(JNIEnv*);
extern void register_java_io_UnixFileSystem(JNIEnv*);
extern void register_java_lang_Double(JNIEnv*);
extern void register_java_lang_Float(JNIEnv*);
extern void register_java_lang_ProcessEnvironment(JNIEnv*);
extern void register_java_lang_Runtime(JNIEnv*);
extern void register_java_lang_StrictMath(JNIEnv*);
extern void register_java_lang_Math(JNIEnv*);
extern void register_java_lang_System(JNIEnv*);
extern void register_java_lang_Thread(JNIEnv*);
extern void register_java_lang_UNIXProcess(JNIEnv*);
extern void register_java_net_DatagramPacket(JNIEnv*);
extern void register_java_net_Inet4Address(JNIEnv*);
extern void register_java_net_Inet6Address(JNIEnv*);
extern void register_java_net_InetAddress(JNIEnv*);
extern void register_java_net_PlainDatagramSocketImpl(JNIEnv*);
extern void register_java_net_SocketInputStream(JNIEnv*);
extern void register_java_net_SocketOutputStream(JNIEnv*);
extern void register_java_nio_MappedByteBuffer(JNIEnv* env);
extern void register_java_util_zip_Adler32(JNIEnv* env);
extern void register_java_util_zip_CRC32(JNIEnv*);
extern void register_java_util_zip_Deflater(JNIEnv*);
extern void register_java_util_zip_Inflater(JNIEnv*);
extern void register_java_util_zip_ZipFile(JNIEnv*);
extern void register_java_util_prefs_FileSystemPreferences(JNIEnv*);
extern void register_sun_nio_ch_DatagramChannelImpl(JNIEnv*);
extern void register_sun_nio_ch_DatagramDispatcher(JNIEnv*);
extern void register_sun_nio_ch_FileChannelImpl(JNIEnv*);
extern void register_sun_nio_ch_FileDispatcherImpl(JNIEnv*);
extern void register_sun_nio_ch_FileKey(JNIEnv*);
extern void register_sun_nio_ch_IOUtil(JNIEnv*);
extern void register_sun_nio_ch_NativeThread(JNIEnv*);
extern void register_sun_nio_ch_Net(JNIEnv*);
extern void register_sun_nio_ch_ServerSocketChannelImpl(JNIEnv*);
extern void register_sun_nio_ch_SocketChannelImpl(JNIEnv* env);

extern jint net_JNI_OnLoad(JavaVM*, void*);

}

extern void register_java_lang_Character(JNIEnv*);

// DalvikVM calls this on startup, so we can statically register all our native methods.
jint JNI_OnLoad(JavaVM* vm, void*) { JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        ALOGE("JavaVM::GetEnv() failed");
        abort();
    }

    ScopedLocalFrame localFrame(env);
    register_java_util_zip_ZipFile(env);
    register_java_util_zip_Inflater(env);
    register_java_util_zip_Deflater(env);
    register_java_util_zip_CRC32(env);
    register_java_util_zip_Adler32(env);
    register_sun_nio_ch_IOUtil(env);
    register_sun_nio_ch_FileChannelImpl(env);
    register_sun_nio_ch_FileDispatcherImpl(env);
    register_java_io_Console(env);
    register_java_io_FileOutputStream(env);
    register_java_io_FileInputStream(env);
    register_java_io_FileDescriptor(env);
    register_java_io_ObjectInputStream(env);
    register_java_io_ObjectOutputStream(env);
    register_java_io_UnixFileSystem(env);
    register_sun_nio_ch_NativeThread(env);
    register_sun_nio_ch_FileKey(env);
    register_java_io_ObjectStreamClass(env);
    register_java_lang_Character(env);
    register_java_lang_Float(env);
    register_java_lang_Double(env);
    register_java_lang_StrictMath(env);
    register_java_lang_Math(env);
    register_java_lang_ProcessEnvironment(env);
    register_java_lang_Runtime(env);
    register_java_lang_System(env);
    register_java_lang_UNIXProcess(env);
    // register_java_net_InetAddress depends on java_lang_Float & Math being
    // fully registered (getMethodId on InetAddress class triggers its
    // <clinit> which depends on java.lang.Float)
    register_java_net_InetAddress(env);
    register_java_net_Inet4Address(env);
    register_java_net_Inet6Address(env);
    register_java_net_SocketInputStream(env);
    register_java_net_SocketOutputStream(env);
    register_java_util_prefs_FileSystemPreferences(env);
    register_sun_nio_ch_ServerSocketChannelImpl(env);
    register_sun_nio_ch_SocketChannelImpl(env);
    register_sun_nio_ch_Net(env);
    register_sun_nio_ch_DatagramChannelImpl(env);
    register_sun_nio_ch_DatagramDispatcher(env);
    register_java_nio_MappedByteBuffer(env);
    net_JNI_OnLoad(vm, NULL);

    JniConstants::Initialize(env);
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM* vm, void*) {
    JniConstants::Invalidate();
}
