/*
 * Copyright 2018 The Android Open Source Project
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

#pragma once

#include "jni.h"

struct JniConstants {
    // Global reference to java.io.FileDescriptor.
    static jclass GetFileDescriptorClass(JNIEnv* env);

    // java.io.FileDescriptor.descriptor.
    static jfieldID GetFileDescriptorDescriptorField(JNIEnv* env);

    // java.io.FileDescriptor.ownerId.
    static jfieldID GetFileDescriptorOwnerIdField(JNIEnv* env);

    // void java.io.FileDescriptor.<init>().
    static jmethodID GetFileDescriptorInitMethod(JNIEnv* env);

    // Global reference to java.nio.NIOAccess.
    static jclass GetNioAccessClass(JNIEnv* env);

    // Object java.nio.NIOAccess.getBaseArray(Buffer);
    static jmethodID GetNioAccessGetBaseArrayMethod(JNIEnv* env);

    // int java.nio.NIOAccess.getBaseArrayOffset(Buffer);
    static jmethodID GetNioAccessGetBaseArrayOffsetMethod(JNIEnv* env);

    // Global reference to java.nio.Buffer.
    static jclass GetNioBufferClass(JNIEnv* env);

    // long java.nio.Buffer.address
    static jfieldID GetNioBufferAddressField(JNIEnv* env);

    // int java.nio.Buffer._elementSizeShift
    static jfieldID GetNioBufferElementSizeShiftField(JNIEnv* env);

    // int java.nio.Buffer.limit;
    static jfieldID GetNioBufferLimitField(JNIEnv* env);

    // int java.nio.Buffer.position;
    static jfieldID GetNioBufferPositionField(JNIEnv* env);

    // Object java.nio.Buffer.array()
    static jmethodID GetNioBufferArrayMethod(JNIEnv* env);

    // int java.nio.Buffer.arrayOffset()
    static jmethodID GetNioBufferArrayOffsetMethod(JNIEnv* env);

    // Global reference to java.lang.ref.Reference.
    static jclass GetReferenceClass(JNIEnv* env);

    // Object java.lang.ref.Reference.get()
    static jmethodID GetReferenceGetMethod(JNIEnv* env);

    // Global reference to java.lang.String.
    static jclass GetStringClass(JNIEnv* env);

    // RoboVM note: Start change
    // merging luni/JniConstants and ojluni/JniConstants into single place as
    // RoboVM will combine VM/RT code into static library and will not able
    // to call stand-along JNIInit

    // RoboVM note: entries from luni/JniConstants.h
    // Initialized cached heap objects. This should be called in JNI_OnLoad.
    static void Initialize(JNIEnv* env);

    // Invalidate cached heap objects. This should be called in JNI_OnUnload.
    static void Invalidate();

    static jclass GetBooleanClass(JNIEnv* env);
    static jclass GetCharsetICUClass(JNIEnv* env);
    static jclass GetDoubleClass(JNIEnv* env);
    static jclass GetErrnoExceptionClass(JNIEnv* env);
    static jclass GetGaiExceptionClass(JNIEnv* env);
    static jclass GetInet6AddressClass(JNIEnv* env);
    static jclass GetInet6AddressHolderClass(JNIEnv* env);
    static jclass GetInetAddressClass(JNIEnv* env);
    static jclass GetInetAddressHolderClass(JNIEnv* env);
    static jclass GetInetSocketAddressClass(JNIEnv* env);
    static jclass GetInetSocketAddressHolderClass(JNIEnv* env);
    static jclass GetIntegerClass(JNIEnv* env);
    static jclass GetLocaleDataClass(JNIEnv* env);
    static jclass GetLongClass(JNIEnv* env);
    static jclass GetNetlinkSocketAddressClass(JNIEnv* env);
    static jclass GetPacketSocketAddressClass(JNIEnv* env);
    static jclass GetPatternSyntaxExceptionClass(JNIEnv* env);
    static jclass GetStructAddrinfoClass(JNIEnv* env);
    static jclass GetStructFlockClass(JNIEnv* env);
    static jclass GetStructGroupReqClass(JNIEnv* env);
    static jclass GetStructIfaddrsClass(JNIEnv* env);
    static jclass GetStructLingerClass(JNIEnv* env);
    static jclass GetStructPasswdClass(JNIEnv* env);
    static jclass GetStructPollfdClass(JNIEnv* env);
    static jclass GetStructStatClass(JNIEnv* env);
    static jclass GetStructStatVfsClass(JNIEnv* env);
    static jclass GetStructTimespecClass(JNIEnv* env);
    static jclass GetStructTimevalClass(JNIEnv* env);
    static jclass GetStructUcredClass(JNIEnv* env);
    static jclass GetStructUtsnameClass(JNIEnv* env);
    static jclass GetUnixSocketAddressClass(JNIEnv* env);

    // RoboVM note: entries from ojluni/JniConstants.h
    // Gets class representing SocketTagger from cache.
    static jclass GetSocketTaggerClass(JNIEnv* env);
    // RoboVM note: End of changes
};
