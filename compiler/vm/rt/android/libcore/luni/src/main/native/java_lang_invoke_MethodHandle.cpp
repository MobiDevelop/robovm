/*
 * Copyright (C) 2016 The Android Open Source Project
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

#include <nativehelper/JNIHelp.h>
#include <nativehelper/jni_macros.h>

static jobject MethodHandle_invokeExact(JNIEnv* env, jobject, jobjectArray) {
    jniThrowException(env, "java/lang/UnsupportedOperationException",
            "MethodHandle.invokeExact cannot be invoked reflectively.");
    return nullptr;
}

static jobject MethodHandle_invoke(JNIEnv* env, jobject, jobjectArray) {
    jniThrowException(env, "java/lang/UnsupportedOperationException",
            "MethodHandle.invoke cannot be invoked reflectively.");
    return nullptr;
}

static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(MethodHandle, invokeExact, "([Ljava/lang/Object;)Ljava/lang/Object;"),
    NATIVE_METHOD(MethodHandle, invoke, "([Ljava/lang/Object;)Ljava/lang/Object;"),
};

extern "C" void register_java_lang_invoke_MethodHandle(JNIEnv* env) {
    jniRegisterNativeMethods(env, "java/lang/invoke/MethodHandle", gMethods, NELEM(gMethods));
}
