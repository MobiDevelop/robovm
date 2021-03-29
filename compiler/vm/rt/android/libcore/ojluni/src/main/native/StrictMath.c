/*
 * Copyright (c) 1994, 2003, Oracle and/or its affiliates. All rights reserved.
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

#include "jni.h"
#include "../../external/fdlibm/fdlibm.h"

#include <nativehelper/JNIHelp.h>

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }


JNIEXPORT jdouble JNICALL
StrictMath_cos(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_cos((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_sin(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_sin((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_tan(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_tan((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_asin(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_asin((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_acos(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_acos((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_atan(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_atan((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_exp(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_exp((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_log(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_log((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_log10(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_log10((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_sqrt(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_sqrt((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_cbrt(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_cbrt((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_atan2(JNIEnv *env, jclass unused, jdouble d1, jdouble d2)
{
    return (jdouble) ieee_atan2((double)d1, (double)d2);
}

JNIEXPORT jdouble JNICALL
StrictMath_pow(JNIEnv *env, jclass unused, jdouble d1, jdouble d2)
{
    return (jdouble) ieee_pow((double)d1, (double)d2);
}

JNIEXPORT jdouble JNICALL
StrictMath_IEEEremainder(JNIEnv *env, jclass unused,
                                  jdouble dividend,
                                  jdouble divisor)
{
    return (jdouble) ieee_remainder(dividend, divisor);
}

JNIEXPORT jdouble JNICALL
StrictMath_cosh(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_cosh((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_sinh(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_sinh((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_tanh(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_tanh((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_hypot(JNIEnv *env, jclass unused, jdouble x, jdouble y)
{
    return (jdouble) ieee_hypot((double)x, (double)y);
}



JNIEXPORT jdouble JNICALL
StrictMath_log1p(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_log1p((double)d);
}

JNIEXPORT jdouble JNICALL
StrictMath_expm1(JNIEnv *env, jclass unused, jdouble d)
{
    return (jdouble) ieee_expm1((double)d);
}

static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(StrictMath, cos, "(D)D"),
  NATIVE_METHOD(StrictMath, sin, "(D)D"),
  NATIVE_METHOD(StrictMath, tan, "(D)D"),
  NATIVE_METHOD(StrictMath, asin, "(D)D"),
  NATIVE_METHOD(StrictMath, acos, "(D)D"),
  NATIVE_METHOD(StrictMath, atan, "(D)D"),
  NATIVE_METHOD(StrictMath, exp, "(D)D"),
  NATIVE_METHOD(StrictMath, log, "(D)D"),
  NATIVE_METHOD(StrictMath, log10, "(D)D"),
  NATIVE_METHOD(StrictMath, sqrt, "(D)D"),
  NATIVE_METHOD(StrictMath, cbrt, "(D)D"),
  NATIVE_METHOD(StrictMath, atan2, "(DD)D"),
  NATIVE_METHOD(StrictMath, pow, "(DD)D"),
  NATIVE_METHOD(StrictMath, IEEEremainder, "(DD)D"),
  NATIVE_METHOD(StrictMath, cosh, "(D)D"),
  NATIVE_METHOD(StrictMath, sinh, "(D)D"),
  NATIVE_METHOD(StrictMath, tanh, "(D)D"),
  NATIVE_METHOD(StrictMath, hypot, "(DD)D"),
  NATIVE_METHOD(StrictMath, log1p, "(D)D"),
  NATIVE_METHOD(StrictMath, expm1, "(D)D"),
};

void register_java_lang_StrictMath(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/lang/StrictMath", gMethods, NELEM(gMethods));
}
