/*
 * Copyright (C) 2015 The Android Open Source Project
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

#include "jni.h"
#include <nativehelper/JNIHelp.h>
#include "nativehelper/jni_macros.h"

#include <stdlib.h>
#include <math.h>

JNIEXPORT jdouble JNICALL
Math_cos(jdouble d) {
    return cos(d);
}

JNIEXPORT jdouble JNICALL
Math_sin(jdouble d) {
    return sin(d);
}

JNIEXPORT jdouble JNICALL
Math_tan(jdouble d) {
    return tan(d);
}

JNIEXPORT jdouble JNICALL
Math_asin(jdouble d) {
    return asin(d);
}

JNIEXPORT jdouble JNICALL
Math_acos(jdouble d) {
    return acos(d);
}

JNIEXPORT jdouble JNICALL
Math_atan(jdouble d) {
    return atan(d);
}

JNIEXPORT jdouble JNICALL
Math_exp(jdouble d) {
    return exp(d);
}

JNIEXPORT jdouble JNICALL
Math_log(jdouble d) {
    return log(d);
}

JNIEXPORT jdouble JNICALL
Math_log10(jdouble d) {
    return log10(d);
}

JNIEXPORT jdouble JNICALL
Math_sqrt(jdouble d) {
    return sqrt(d);
}

JNIEXPORT jdouble JNICALL
Math_cbrt(jdouble d) {
    return cbrt(d);
}

JNIEXPORT jdouble JNICALL
Math_atan2(jdouble d1, jdouble d2) {
    return atan2(d1, d2);
}

JNIEXPORT jdouble JNICALL
Math_pow(jdouble d1, jdouble d2) {
    return pow(d1, d2);
}

JNIEXPORT jdouble JNICALL
Math_IEEEremainder(jdouble dividend, jdouble divisor) {
    return remainder(dividend, divisor);
}

JNIEXPORT jdouble JNICALL
Math_cosh(jdouble d) {
    return cosh(d);
}

JNIEXPORT jdouble JNICALL
Math_sinh(jdouble d) {
    return sinh(d);
}

JNIEXPORT jdouble JNICALL
Math_tanh(jdouble d) {
    return tanh(d);
}

JNIEXPORT jdouble JNICALL
Math_hypot(jdouble x, jdouble y) {
    return hypot(x, y);
}

JNIEXPORT jdouble JNICALL
Math_log1p(jdouble d) {
    return log1p(d);
}

JNIEXPORT jdouble JNICALL
Math_expm1(jdouble d) {
    return expm1(d);
}

JNIEXPORT jdouble JNICALL
Math_floor(jdouble d) {
    return floor(d);
}

JNIEXPORT jdouble JNICALL
Math_ceil(jdouble d) {
    return ceil(d);
}

JNIEXPORT jdouble JNICALL
Math_rint(jdouble d) {
    return rint(d);
}

static JNINativeMethod gMethods[] = {
  FAST_NATIVE_METHOD(Math, IEEEremainder, "(DD)D"),
  FAST_NATIVE_METHOD(Math, acos, "(D)D"),
  FAST_NATIVE_METHOD(Math, asin, "(D)D"),
  FAST_NATIVE_METHOD(Math, atan, "(D)D"),
  FAST_NATIVE_METHOD(Math, atan2, "(DD)D"),
  FAST_NATIVE_METHOD(Math, cbrt, "(D)D"),
  FAST_NATIVE_METHOD(Math, cos, "(D)D"),
  FAST_NATIVE_METHOD(Math, ceil, "(D)D"),
  FAST_NATIVE_METHOD(Math, cosh, "(D)D"),
  FAST_NATIVE_METHOD(Math, exp, "(D)D"),
  FAST_NATIVE_METHOD(Math, expm1, "(D)D"),
  FAST_NATIVE_METHOD(Math, floor, "(D)D"),
  FAST_NATIVE_METHOD(Math, hypot, "(DD)D"),
  FAST_NATIVE_METHOD(Math, log, "(D)D"),
  FAST_NATIVE_METHOD(Math, log10, "(D)D"),
  FAST_NATIVE_METHOD(Math, log1p, "(D)D"),
  FAST_NATIVE_METHOD(Math, pow, "(DD)D"),
  FAST_NATIVE_METHOD(Math, rint, "(D)D"),
  FAST_NATIVE_METHOD(Math, sin, "(D)D"),
  FAST_NATIVE_METHOD(Math, sinh, "(D)D"),
  FAST_NATIVE_METHOD(Math, sqrt, "(D)D"),
  FAST_NATIVE_METHOD(Math, tan, "(D)D"),
  FAST_NATIVE_METHOD(Math, tanh, "(D)D"),
};

void register_java_lang_Math(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/lang/Math", gMethods, NELEM(gMethods));
}
