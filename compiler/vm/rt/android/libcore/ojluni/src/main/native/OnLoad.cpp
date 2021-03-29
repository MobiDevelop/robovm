/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  The Android Open Source
 * Project designates this particular file as subject to the "Classpath"
 * exception as provided by The Android Open Source Project in the LICENSE
 * file that accompanied this code.
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
 */

#include <jni.h>
#include <android-base/logging.h>
#include <nativehelper/ScopedLocalFrame.h>

extern "C" void register_java_util_zip_ZipFile(JNIEnv* env);
extern "C" void register_java_util_zip_Inflater(JNIEnv* env);
extern "C" void register_java_util_zip_Deflater(JNIEnv* env);
extern "C" void register_java_util_zip_CRC32(JNIEnv* env);
extern "C" void register_java_util_zip_Adler32(JNIEnv* env);
extern "C" void register_java_io_FileDescriptor(JNIEnv* env);
extern "C" void register_sun_nio_ch_DatagramChannelImpl(JNIEnv* env);
extern "C" void register_sun_nio_ch_DatagramDispatcher(JNIEnv* env);
extern "C" void register_java_io_Console(JNIEnv* env);
extern "C" void register_sun_nio_ch_IOUtil(JNIEnv* env);
extern "C" void register_sun_nio_ch_SocketChannelImpl(JNIEnv* env);
extern "C" void register_sun_nio_ch_FileChannelImpl(JNIEnv* env);
extern "C" void register_sun_nio_ch_FileDispatcherImpl(JNIEnv* env);
extern "C" void register_java_io_FileOutputStream(JNIEnv* env);
extern "C" void register_java_io_FileInputStream(JNIEnv* env);
extern "C" void register_java_util_prefs_FileSystemPreferences(JNIEnv* env);
extern "C" void register_sun_nio_ch_NativeThread(JNIEnv* env);
extern "C" void register_sun_nio_ch_FileKey(JNIEnv* env);
extern "C" void register_java_io_UnixFileSystem(JNIEnv* env);
extern "C" void register_java_io_ObjectStreamClass(JNIEnv* env);
extern "C" void register_java_io_ObjectOutputStream(JNIEnv* env);
extern "C" void register_java_io_ObjectInputStream(JNIEnv* env);
extern "C" void register_java_net_InetAddress(JNIEnv* env);
extern "C" jint net_JNI_OnLoad(JavaVM *vm, void* ignored);
extern "C" void register_sun_nio_ch_Net(JNIEnv* env);
extern "C" void register_java_nio_MappedByteBuffer(JNIEnv* env);
extern "C" void register_java_net_Inet6Address(JNIEnv* env);
extern "C" void register_java_net_Inet4Address(JNIEnv* env);
extern "C" void register_sun_nio_ch_ServerSocketChannelImpl(JNIEnv* env);
extern "C" void register_java_net_SocketInputStream(JNIEnv* env);
extern "C" void register_java_net_SocketOutputStream(JNIEnv* env);
extern "C" void register_java_lang_Float(JNIEnv* env);
extern "C" void register_java_lang_Double(JNIEnv* env);
extern "C" void register_java_lang_StrictMath(JNIEnv* env);
extern "C" void register_java_lang_Math(JNIEnv* env);
extern "C" void register_java_lang_ProcessEnvironment(JNIEnv* env);
extern "C" void register_java_lang_System(JNIEnv* env);
extern "C" void register_java_lang_Runtime(JNIEnv* env);
extern "C" void register_java_lang_UNIXProcess(JNIEnv* env);
void register_java_lang_Character(JNIEnv* env);

// RoboVM Note: renamed from JNI_OnLoad
extern "C" void ojluni_OnLoad(JavaVM* vm, void*) {
    void* raw_env;
    jint version = JNI_VERSION_1_6;
    jint result = vm->GetEnv(&raw_env, version);
    CHECK_EQ(result, JNI_OK);
    CHECK(raw_env != nullptr);
    JNIEnv* env = static_cast<JNIEnv*>(raw_env);
    ScopedLocalFrame localFrame(env);

    jint net_jni_version = net_JNI_OnLoad(vm, /* ignored */ nullptr);
    CHECK(net_jni_version == JNI_VERSION_1_2 ||
          net_jni_version == JNI_VERSION_1_4 ||
          net_jni_version == JNI_VERSION_1_6);
}

extern "C" void ojluniRegister(JNIEnv* env) {
  // Some registration functions also do some extra local initialization,
  // creating local references in the process. ART does not expect JNI_OnLoad()
  // to leave any local references in the current frame, so create a new one.
  // Request space for 256 local references (increase if necessary).
  ScopedLocalFrame localFrame(env);

  // Some registration functions also record field ids retrieved using
  // GetFieldID(), forcing the initialization of the searched class. As some
  // class initializers (notably Inet*Address) directly or indirectly use
  // the StringBuilder, we need to start by registering native methods needed
  // for resizing its internal buffer. That's done through Arrays.copyOf()
  // which uses System.arraycopy() and Math.min(), forcing the initialization
  // of System and Main. The former uses System's own native methods while the
  // latter uses native methods of Float and Double but not Math's own.
  register_java_lang_Float(env);
  register_java_lang_Double(env);
  register_java_lang_System(env);

  // Initialize the rest in the order in which they appear in Android.bp .
  register_java_util_zip_ZipFile(env);
  register_java_util_zip_Inflater(env);
  register_java_util_zip_Deflater(env);
  register_java_util_zip_CRC32(env);
  register_java_util_zip_Adler32(env);
  register_java_io_FileDescriptor(env);
  register_sun_nio_ch_DatagramChannelImpl(env);
  register_sun_nio_ch_DatagramDispatcher(env);
  register_java_io_Console(env);
  register_sun_nio_ch_IOUtil(env);
  register_sun_nio_ch_SocketChannelImpl(env);
  register_sun_nio_ch_FileChannelImpl(env);
  register_sun_nio_ch_FileDispatcherImpl(env);
  register_java_io_FileOutputStream(env);
  register_java_io_FileInputStream(env);
  register_java_util_prefs_FileSystemPreferences(env);
  register_sun_nio_ch_NativeThread(env);
  register_sun_nio_ch_FileKey(env);
  register_java_io_UnixFileSystem(env);
  register_java_io_ObjectStreamClass(env);
  register_java_io_ObjectOutputStream(env);
  register_java_io_ObjectInputStream(env);
  register_java_net_InetAddress(env);

  register_sun_nio_ch_Net(env);
  register_java_nio_MappedByteBuffer(env);
  register_java_net_Inet6Address(env);
  register_java_net_Inet4Address(env);
  register_sun_nio_ch_ServerSocketChannelImpl(env);
  register_java_net_SocketInputStream(env);
  register_java_net_SocketOutputStream(env);
  register_java_lang_StrictMath(env);
  register_java_lang_Math(env);
  register_java_lang_ProcessEnvironment(env);
  register_java_lang_Runtime(env);
  register_java_lang_UNIXProcess(env);
  register_java_lang_Character(env);
}
