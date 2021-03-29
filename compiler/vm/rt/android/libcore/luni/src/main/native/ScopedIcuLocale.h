/*
 * Copyright (C) 2014 The Android Open Source Project
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

#ifndef SCOPED_ICU_LOCALE_H_included
#define SCOPED_ICU_LOCALE_H_included

#include <nativehelper/JNIHelp.h>
#include <nativehelper/ScopedUtfChars.h>
#include "unicode/locid.h"

class ScopedIcuLocale {
 public:
  ScopedIcuLocale(JNIEnv* env, jstring javaLocaleName) : mEnv(env) {
    mLocale.setToBogus();

    if (javaLocaleName == NULL) {
      jniThrowNullPointerException(mEnv, "javaLocaleName == null");
      return;
    }

    const ScopedUtfChars localeName(env, javaLocaleName);
    if (localeName.c_str() == NULL) {
      return;
    }

    mLocale = icu::Locale::createFromName(localeName.c_str());
  }

  ~ScopedIcuLocale() {
  }

  bool valid() const {
    return !mLocale.isBogus();
  }

  icu::Locale& locale() {
    return mLocale;
  }

 private:
  JNIEnv* const mEnv;
  icu::Locale mLocale;

  // Disallow copy and assignment.
  ScopedIcuLocale(const ScopedIcuLocale&);
  void operator=(const ScopedIcuLocale&);
};

#endif  // SCOPED_ICU_LOCALE_H_included
