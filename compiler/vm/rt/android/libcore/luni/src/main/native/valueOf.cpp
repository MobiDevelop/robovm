/*
 * Copyright (C) 2010 The Android Open Source Project
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

#define LOG_TAG "valueOf"

#include "valueOf.h"

#include <nativehelper/JNIHelp.h>

#include "JniConstants.h"

template <typename T>
static jobject valueOf(JNIEnv* env, jclass c, const char* signature, const T& value) {
    static jmethodID valueOfMethod = env->GetStaticMethodID(c, "valueOf", signature);
    if (env->ExceptionCheck()) {
        return NULL;
    }
    jobject result = env->CallStaticObjectMethod(c, valueOfMethod, value);
    if (env->ExceptionCheck()) {
        return NULL;
    }
    return result;
}

jobject booleanValueOf(JNIEnv* env, jboolean value) {
    return valueOf(env, JniConstants::GetBooleanClass(env), "(Z)Ljava/lang/Boolean;", value);
}

jobject doubleValueOf(JNIEnv* env, jdouble value) {
    return valueOf(env, JniConstants::GetDoubleClass(env), "(D)Ljava/lang/Double;", value);
}

jobject integerValueOf(JNIEnv* env, jint value) {
    return valueOf(env, JniConstants::GetIntegerClass(env), "(I)Ljava/lang/Integer;", value);
}

jobject longValueOf(JNIEnv* env, jlong value) {
    return valueOf(env, JniConstants::GetLongClass(env), "(J)Ljava/lang/Long;", value);
}

jboolean booleanValue(JNIEnv* env, jobject javaLangBoolean) {
    static jfieldID fid = env->GetFieldID(JniConstants::GetBooleanClass(env), "value", "Z");
    return env->GetBooleanField(javaLangBoolean, fid);
}

jint intValue(JNIEnv* env, jobject javaLangInteger) {
    static jfieldID fid = env->GetFieldID(JniConstants::GetIntegerClass(env), "value", "I");
    return env->GetIntField(javaLangInteger, fid);
}
