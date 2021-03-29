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

#include <nativehelper/JNIHelp.h>
#include <nativehelper/jni_macros.h>

static void ThrowUnsupportedOperationForAccessMode(JNIEnv* env, const char* accessMode) {
  // VarHandle access mode methods should be dispatched by the
  // interpreter or inlined into compiled code. The JNI methods below
  // are discoverable via reflection, but are not intended to be
  // invoked this way.
  jniThrowExceptionFmt(env,
                       "java/lang/UnsupportedOperationException",
                       "VarHandle.%s cannot be invoked reflectively.",
                       accessMode);
}

static jobject VarHandle_compareAndExchange(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "compareAndExchange");
  return nullptr;
}

static jobject VarHandle_compareAndExchangeAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "compareAndExchangeAcquire");
  return nullptr;
}

static jobject VarHandle_compareAndExchangeRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "compareAndExchangeRelease");
  return nullptr;
}

static jboolean VarHandle_compareAndSet(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "compareAndSet");
  return JNI_FALSE;
}

static jobject VarHandle_get(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "get");
  return nullptr;
}

static jobject VarHandle_getAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAcquire");
  return nullptr;
}

static jobject VarHandle_getAndAdd(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndAdd");
  return nullptr;
}

static jobject VarHandle_getAndAddAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndAddAcquire");
  return nullptr;
}

static jobject VarHandle_getAndAddRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndAddRelease");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseAnd(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseAnd");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseAndAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseAndAcquire");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseAndRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseAndRelease");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseOr(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseOr");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseOrAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseOrAcquire");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseOrRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseOrRelease");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseXor(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseXor");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseXorAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseXorAcquire");
  return nullptr;
}

static jobject VarHandle_getAndBitwiseXorRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndBitwiseXorRelease");
  return nullptr;
}

static jobject VarHandle_getAndSet(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndSet");
  return nullptr;
}

static jobject VarHandle_getAndSetAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndSetAcquire");
  return nullptr;
}

static jobject VarHandle_getAndSetRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getAndSetRelease");
  return nullptr;
}

static jobject VarHandle_getOpaque(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getOpaque");
  return nullptr;
}

static jobject VarHandle_getVolatile(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "getVolatile");
  return nullptr;
}

static void VarHandle_set(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "set");
}

static void VarHandle_setOpaque(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "setOpaque");
}

static void VarHandle_setRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "setRelease");
}

static void VarHandle_setVolatile(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "setVolatile");
}

static jboolean VarHandle_weakCompareAndSet(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "weakCompareAndSet");
  return JNI_FALSE;
}

static jboolean VarHandle_weakCompareAndSetAcquire(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "weakCompareAndSetAcquire");
  return JNI_FALSE;
}

static jboolean VarHandle_weakCompareAndSetPlain(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "weakCompareAndSetPlain");
  return JNI_FALSE;
}

static jboolean VarHandle_weakCompareAndSetRelease(JNIEnv* env, jobject, jobjectArray) {
  // Only reachable with reflection (see comment in ThrowUnsupportedOperationForAccessMode).
  ThrowUnsupportedOperationForAccessMode(env, "weakCompareAndSetRelease");
  return JNI_FALSE;
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(VarHandle, compareAndExchange, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, compareAndExchangeAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, compareAndExchangeRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, compareAndSet, "([Ljava/lang/Object;)Z"),
  NATIVE_METHOD(VarHandle, get, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndAdd, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndAddAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndAddRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseAnd, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseAndAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseAndRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseOr, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseOrAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseOrRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseXor, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseXorAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndBitwiseXorRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndSet, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndSetAcquire, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getAndSetRelease, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getOpaque, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, getVolatile, "([Ljava/lang/Object;)Ljava/lang/Object;"),
  NATIVE_METHOD(VarHandle, set, "([Ljava/lang/Object;)V"),
  NATIVE_METHOD(VarHandle, setOpaque, "([Ljava/lang/Object;)V"),
  NATIVE_METHOD(VarHandle, setRelease, "([Ljava/lang/Object;)V"),
  NATIVE_METHOD(VarHandle, setVolatile, "([Ljava/lang/Object;)V"),
  NATIVE_METHOD(VarHandle, weakCompareAndSet, "([Ljava/lang/Object;)Z"),
  NATIVE_METHOD(VarHandle, weakCompareAndSetAcquire, "([Ljava/lang/Object;)Z"),
  NATIVE_METHOD(VarHandle, weakCompareAndSetPlain, "([Ljava/lang/Object;)Z"),
  NATIVE_METHOD(VarHandle, weakCompareAndSetRelease, "([Ljava/lang/Object;)Z"),
};

extern "C" void register_java_lang_invoke_VarHandle(JNIEnv* env) {
    jniRegisterNativeMethods(env, "java/lang/invoke/VarHandle", gMethods, NELEM(gMethods));
}
