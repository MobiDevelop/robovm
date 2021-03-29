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

#define LOG_TAG "Matcher"

#include <memory>
#include <stdlib.h>

#include <android-base/logging.h>
#include <nativehelper/JNIHelp.h>
#include <nativehelper/ScopedPrimitiveArray.h>
#include <nativehelper/ScopedStringChars.h>
#include <nativehelper/jni_macros.h>

#include "IcuUtilities.h"
#include "JniException.h"
#include "ScopedJavaUnicodeString.h"
#include "unicode/parseerr.h"
#include "unicode/regex.h"

// ICU documentation: http://icu-project.org/apiref/icu4c/classRegexMatcher.html

/**
 * Encapsulates an instance of ICU4C's RegexMatcher class along with a copy of
 * the input it's currently operating on in the native heap.
 *
 * Rationale: We choose to make a copy here because it turns out to be a lot
 * cheaper when a moving GC and/or string compression is enabled. This is
 * because env->GetStringChars() always copies in this scenario. This becomes
 * especially bad when the String in question is long and/or contains a large
 * number of matches.
 *
 * Drawbacks: The native allocation associated with this class is no longer
 * fixed size, so we're effectively lying to the NativeAllocationRegistry about
 * the size of the object(s) we're allocating on the native heap. The peak
 * memory usage doesn't change though, given that GetStringChars would have
 * made an allocation of precisely the same size.
 */
class MatcherState {
public:
    MatcherState(icu::RegexMatcher* matcher) :
        mMatcher(matcher),
        mUChars(nullptr),
        mUText(nullptr),
        mStatus(U_ZERO_ERROR) {
    }

    bool updateInput(JNIEnv* env, jstring input) {
        // First, close the UText struct, since we're about to allocate a new one.
        if (mUText != nullptr) {
            utext_close(mUText);
            mUText = nullptr;
        }

        // Then delete the UChar* associated with the UText struct..
        mUChars.reset(nullptr);

        // TODO: We should investigate whether we can avoid an additional copy
        // in the native heap when is_copy == JNI_TRUE. The problem with doing
        // that is that we might call ReleaseStringChars with a different
        // JNIEnv* on a different downcall. This is currently safe as
        // implemented in ART, but is unlikely to be portable and the spec stays
        // silent on the matter.
        ScopedStringChars inputChars(env, input);
        if (inputChars.get() == nullptr) {
            // There will be an exception pending if we get here.
            return false;
        }

        // Make a copy of |input| on the native heap. This copy will be live
        // until the next call to updateInput or close.
        mUChars.reset(new (std::nothrow) UChar[inputChars.size()]);
        if (mUChars.get() == nullptr) {
            env->ThrowNew(env->FindClass("Ljava/lang/OutOfMemoryError;"), "Out of memory");
            return false;
        }

        static_assert(sizeof(UChar) == sizeof(jchar), "sizeof(Uchar) != sizeof(jchar)");
        memcpy(mUChars.get(), inputChars.get(), inputChars.size() * sizeof(jchar));

        // Reset any errors that might have occurred on previous patches.
        mStatus = U_ZERO_ERROR;
        mUText = utext_openUChars(nullptr, mUChars.get(), inputChars.size(), &mStatus);
        if (mUText == nullptr) {
            CHECK(maybeThrowIcuException(env, "utext_openUChars", mStatus));
            return false;
        }

        // It is an error for ICU to have returned a non-null mUText but to
        // still have indicated an error.
        CHECK(U_SUCCESS(mStatus));

        mMatcher->reset(mUText);
        return true;
    }

    ~MatcherState() {
        if (mUText != nullptr) {
            utext_close(mUText);
        }
    }

    icu::RegexMatcher* matcher() {
        return mMatcher.get();
    }

    UErrorCode& status() {
        return mStatus;
    }

    void updateOffsets(JNIEnv* env, jintArray javaOffsets) {
        ScopedIntArrayRW offsets(env, javaOffsets);
        if (offsets.get() == NULL) {
            return;
        }

        for (size_t i = 0, groupCount = mMatcher->groupCount(); i <= groupCount; ++i) {
            offsets[2*i + 0] = mMatcher->start(i, mStatus);
            offsets[2*i + 1] = mMatcher->end(i, mStatus);
        }
    }

private:
    std::unique_ptr<icu::RegexMatcher> mMatcher;
    std::unique_ptr<UChar[]> mUChars;
    UText* mUText;
    UErrorCode mStatus;

    // Disallow copy and assignment.
    MatcherState(const MatcherState&);
    void operator=(const MatcherState&);
};

static inline MatcherState* toMatcherState(jlong address) {
    return reinterpret_cast<MatcherState*>(static_cast<uintptr_t>(address));
}

static void Matcher_free(void* address) {
    MatcherState* state = reinterpret_cast<MatcherState*>(address);
    delete state;
}

static jlong Matcher_getNativeFinalizer(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(&Matcher_free);
}

static jboolean Matcher_findImpl(JNIEnv* env, jclass, jlong addr, jint startIndex, jintArray offsets) {
    MatcherState* state = toMatcherState(addr);
    UBool result = state->matcher()->find(startIndex, state->status());
    if (result) {
        state->updateOffsets(env, offsets);
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jboolean Matcher_findNextImpl(JNIEnv* env, jclass, jlong addr, jintArray offsets) {
    MatcherState* state = toMatcherState(addr);
    UBool result = state->matcher()->find();
    if (result) {
        state->updateOffsets(env, offsets);
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jint Matcher_groupCountImpl(JNIEnv*, jclass, jlong addr) {
    MatcherState* state = toMatcherState(addr);
    return state->matcher()->groupCount();
}

static jboolean Matcher_hitEndImpl(JNIEnv*, jclass, jlong addr) {
    MatcherState* state = toMatcherState(addr);
    if (state->matcher()->hitEnd() != 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jboolean Matcher_lookingAtImpl(JNIEnv* env, jclass, jlong addr, jintArray offsets) {
    MatcherState* state = toMatcherState(addr);
    UBool result = state->matcher()->lookingAt(state->status());
    if (result) {
        state->updateOffsets(env, offsets);
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jboolean Matcher_matchesImpl(JNIEnv* env, jclass, jlong addr, jintArray offsets) {
    MatcherState* state = toMatcherState(addr);
    UBool result = state->matcher()->matches(state->status());
    if (result) {
        state->updateOffsets(env, offsets);
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static jlong Matcher_openImpl(JNIEnv* env, jclass, jlong patternAddr) {
    icu::RegexPattern* pattern = reinterpret_cast<icu::RegexPattern*>(static_cast<uintptr_t>(patternAddr));
    UErrorCode status = U_ZERO_ERROR;
    icu::RegexMatcher* result = pattern->matcher(status);
    if (maybeThrowIcuException(env, "RegexPattern::matcher", status)) {
        return 0;
    }

    return reinterpret_cast<uintptr_t>(new MatcherState(result));
}

static jboolean Matcher_requireEndImpl(JNIEnv*, jclass, jlong addr) {
    MatcherState* state = toMatcherState(addr);
    if (state->matcher()->requireEnd() != 0) {
        return JNI_TRUE;
    } else {
        return JNI_FALSE;
    }
}

static void Matcher_setInputImpl(JNIEnv* env, jclass, jlong addr, jstring javaText, jint start, jint end) {
    MatcherState* state = toMatcherState(addr);
    if (state->updateInput(env, javaText)) {
        state->matcher()->region(start, end, state->status());
    }
}

static void Matcher_useAnchoringBoundsImpl(JNIEnv*, jclass, jlong addr, jboolean value) {
    MatcherState* state = toMatcherState(addr);
    state->matcher()->useAnchoringBounds(value);
}

static void Matcher_useTransparentBoundsImpl(JNIEnv*, jclass, jlong addr, jboolean value) {
    MatcherState* state = toMatcherState(addr);
    state->matcher()->useTransparentBounds(value);
}

static jint Matcher_getMatchedGroupIndex0(JNIEnv* env, jclass, jlong patternAddr, jstring javaGroupName) {
  icu::RegexPattern* pattern = reinterpret_cast<icu::RegexPattern*>(static_cast<uintptr_t>(patternAddr));
  ScopedJavaUnicodeString groupName(env, javaGroupName);
  UErrorCode status = U_ZERO_ERROR;

  jint result = pattern->groupNumberFromName(groupName.unicodeString(), status);
  if (U_SUCCESS(status)) {
    return result;
  }
  if (status == U_REGEX_INVALID_CAPTURE_GROUP_NAME) {
    return -1;
  }
  maybeThrowIcuException(env, "RegexPattern::groupNumberFromName", status);
  return -1;
}


static JNINativeMethod gMethods[] = {
    NATIVE_METHOD(Matcher, getMatchedGroupIndex0, "(JLjava/lang/String;)I"),
    NATIVE_METHOD(Matcher, findImpl, "(JI[I)Z"),
    NATIVE_METHOD(Matcher, findNextImpl, "(J[I)Z"),
    NATIVE_METHOD(Matcher, getNativeFinalizer, "()J"),
    NATIVE_METHOD(Matcher, groupCountImpl, "(J)I"),
    NATIVE_METHOD(Matcher, hitEndImpl, "(J)Z"),
    NATIVE_METHOD(Matcher, lookingAtImpl, "(J[I)Z"),
    NATIVE_METHOD(Matcher, matchesImpl, "(J[I)Z"),
    NATIVE_METHOD(Matcher, openImpl, "(J)J"),
    NATIVE_METHOD(Matcher, requireEndImpl, "(J)Z"),
    NATIVE_METHOD(Matcher, setInputImpl, "(JLjava/lang/String;II)V"),
    NATIVE_METHOD(Matcher, useAnchoringBoundsImpl, "(JZ)V"),
    NATIVE_METHOD(Matcher, useTransparentBoundsImpl, "(JZ)V"),
};
extern "C" void register_java_util_regex_Matcher(JNIEnv* env) {
    jniRegisterNativeMethods(env, "java/util/regex/Matcher", gMethods, NELEM(gMethods));
}
