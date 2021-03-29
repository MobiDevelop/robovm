/*
 * Copyright (C) 2017 The Android Open Source Project
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

#include <jni.h>

#include <conscrypt/compatibility_close_monitor.h>
#include <conscrypt/jniutil.h>
#include <conscrypt/logging.h>
#include <conscrypt/native_crypto.h>

#ifndef CONSCRYPT_JNI_VERSION
#define CONSCRYPT_JNI_VERSION JNI_VERSION_1_6
#endif  // !CONSCRYPT_JNI_VERSION

using conscrypt::CompatibilityCloseMonitor;
using conscrypt::NativeCrypto;

// RoboVM note: JNI_OnLoad() is not supported with static JNI. The NativeCrypto Java class has been changed to call a native method named onload() instead.
extern "C" void Java_com_android_org_conscrypt_NativeCrypto_onload(JNIEnv* env, jclass) {
    JavaVM* vm;
    env->GetJavaVM(&vm);

    // Initialize the JNI constants.
    conscrypt::jniutil::init(vm, env);

    // Register all of the native JNI methods.
    NativeCrypto::registerNativeMethods(env);

    // Perform static initialization of the close monitor (if required on this platform).
    CompatibilityCloseMonitor::init();
}