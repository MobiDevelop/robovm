/*
 * Copyright (C) 2011 The Android Open Source Project
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

#define LOG_TAG "AsynchronousCloseMonitor"

#include <nativehelper/JNIHelp.h>
#include <nativehelper/jni_macros.h>

#include "AsynchronousCloseMonitor.h"

static void AsynchronousCloseMonitor_signalBlockedThreads(JNIEnv* env, jclass, jobject javaFd) {
    int fd = jniGetFDFromFileDescriptor(env, javaFd);
    AsynchronousCloseMonitor::signalBlockedThreads(fd);
}

static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(AsynchronousCloseMonitor, signalBlockedThreads, "(Ljava/io/FileDescriptor;)V"),
};
extern "C" void register_libcore_io_AsynchronousCloseMonitor(JNIEnv* env) {
    AsynchronousCloseMonitor::init();
    jniRegisterNativeMethods(env, "libcore/io/AsynchronousCloseMonitor", gMethods, NELEM(gMethods));
}
