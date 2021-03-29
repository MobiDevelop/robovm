/*
 * Copyright (C) 2016 The Android Open Source Project
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

#include <nativehelper/JNIHelp.h>

#include "JniConstants.h"

extern "C" int tagSocket(JNIEnv* env, int fd) {
    if (env->ExceptionOccurred()) {
      return fd;
    }

    jclass socketTaggerClass = JniConstants::GetSocketTaggerClass(env);
    jmethodID get = env->GetStaticMethodID(socketTaggerClass,
                                           "get",
                                           "()Ldalvik/system/SocketTagger;");
    jobject socketTagger = env->CallStaticObjectMethod(socketTaggerClass, get);
    jmethodID tag = env->GetMethodID(socketTaggerClass, "tag", "(Ljava/io/FileDescriptor;)V");

    jobject fileDescriptor = jniCreateFileDescriptor(env, fd);
    env->CallVoidMethod(socketTagger, tag, fileDescriptor);
    return fd;
}
