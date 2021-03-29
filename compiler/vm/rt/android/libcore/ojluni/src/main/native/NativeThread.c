/*
 * Copyright (c) 2002, 2006, Oracle and/or its affiliates. All rights reserved.
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

#include <sys/types.h>
#include <string.h>
#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include "nio_util.h"
#include <nativehelper/JNIHelp.h>

#ifdef __linux__
  #include <pthread.h>
  #include <sys/signal.h>
  // Android-changed: Bionic (and AsynchronousCloseMonitor) expects libcore to use
  // __SIGRTMIN + 2, not __SIGRTMAX - 2
  /* Also defined in net/linux_close.c */
  #define INTERRUPT_SIGNAL (__SIGRTMIN + 2)
#elif __solaris__
  #include <thread.h>
  #include <signal.h>
  #define INTERRUPT_SIGNAL (SIGRTMAX - 2)
#elif _ALLBSD_SOURCE
  #include <pthread.h>
  #include <signal.h>
  /* Also defined in net/bsd_close.c */
  #define INTERRUPT_SIGNAL SIGIO
// BEGIN Android-added: Fuchsia: Support for Fuchsia platform.
#elif __Fuchsia__
  #include <pthread.h>
  #include <signal.h>
  /* Also defined in net/bsd_close.c */
  #define INTERRUPT_SIGNAL SIGIO
// END Android-added: Fuchsia: Support for Fuchsia platform.
#else
  #error "missing platform-specific definition here"
#endif

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

static void
nullHandler(int sig)
{
}

static void  NativeThread_init(JNIEnv *env)
{
    /* Install the null handler for INTERRUPT_SIGNAL.  This might overwrite the
     * handler previously installed by java/net/linux_close.c, but that's okay
     * since neither handler actually does anything.  We install our own
     * handler here simply out of paranoia; ultimately the two mechanisms
     * should somehow be unified, perhaps within the VM.
     */

    sigset_t ss;
    struct sigaction sa, osa;
    sa.sa_handler = nullHandler;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    if (sigaction(INTERRUPT_SIGNAL, &sa, &osa) < 0)
        JNU_ThrowIOExceptionWithLastError(env, "sigaction");
}

JNIEXPORT jlong JNICALL
NativeThread_current(JNIEnv *env, jclass cl)
{
#ifdef __solaris__
    return (jlong)thr_self();
#else
    return (jlong)pthread_self();
#endif
}

JNIEXPORT void JNICALL
NativeThread_signal(JNIEnv *env, jclass cl, jlong thread)
{
    int ret;
#ifdef __solaris__
    ret = thr_kill((thread_t)thread, INTERRUPT_SIGNAL);
#else
    ret = pthread_kill((pthread_t)thread, INTERRUPT_SIGNAL);
#endif
    if (ret != 0)
        JNU_ThrowIOExceptionWithLastError(env, "Thread signal failed");
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(NativeThread, current, "()J"),
  NATIVE_METHOD(NativeThread, signal, "(J)V"),
};

void register_sun_nio_ch_NativeThread(JNIEnv* env) {
  jniRegisterNativeMethods(env, "sun/nio/ch/NativeThread", gMethods, NELEM(gMethods));
  NativeThread_init(env);
}
