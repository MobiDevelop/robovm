/*
 * Copyright (C) 2013 The Android Open Source Project
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

#include "nativehelper/JniInvocation.h"

#ifdef _WIN32
#include <windows.h>
#else
#include <dlfcn.h>
#endif
#include <stdlib.h>
#include <string.h>

#include <cstddef>

#define LOG_TAG "JniInvocation"
#include <log/log.h>

#ifdef __ANDROID__
#include <sys/system_properties.h>
#endif

#include "android-base/errors.h"
#include "JniConstants.h"

namespace {

template <typename T>
void UNUSED(const T&) {}

bool IsDebuggable() {
#ifdef __ANDROID__
  char debuggable[PROP_VALUE_MAX] = {0};
  __system_property_get("ro.debuggable", debuggable);
  return strcmp(debuggable, "1") == 0;
#else
  return false;
#endif
}

int GetLibrarySystemProperty(char* buffer) {
#ifdef __ANDROID__
  return __system_property_get("persist.sys.dalvik.vm.lib.2", buffer);
#else
  UNUSED(buffer);
  return 0;
#endif
}

#ifdef _WIN32
#define FUNC_POINTER FARPROC
#else
#define FUNC_POINTER void*
#endif

void* OpenLibrary(const char* filename) {
#ifdef _WIN32
  return LoadLibrary(filename);
#else
  // Load with RTLD_NODELETE in order to ensure that libart.so is not unmapped when it is closed.
  // This is due to the fact that it is possible that some threads might have yet to finish
  // exiting even after JNI_DeleteJavaVM returns, which can lead to segfaults if the library is
  // unloaded.
  const int kDlopenFlags = RTLD_NOW | RTLD_NODELETE;
  return dlopen(filename, kDlopenFlags);
#endif
}

int CloseLibrary(void* handle) {
#ifdef _WIN32
  return FreeLibrary(static_cast<HMODULE>(handle));
#else
  return dlclose(handle);
#endif
}

FUNC_POINTER GetSymbol(void* handle, const char* symbol) {
#ifdef _WIN32
  return GetProcAddress(static_cast<HMODULE>(handle), symbol);
#else
  return dlsym(handle, symbol);
#endif
}

std::string GetError() {
#ifdef _WIN32
  return android::base::SystemErrorCodeToString(GetLastError());
#else
  return std::string(dlerror());
#endif
}

}  // namespace

struct JniInvocationImpl final {
 public:
  JniInvocationImpl();
  ~JniInvocationImpl();

  bool Init(const char* library);

  //  static const char* GetLibrary(const char* library, char* buffer);

  static const char* GetLibrary(const char* library,
                                char* buffer,
                                bool (*is_debuggable)() = IsDebuggable,
                                int (*get_library_system_property)(char* buffer) = GetLibrarySystemProperty);

  static JniInvocationImpl& GetJniInvocation();

  jint JNI_GetDefaultJavaVMInitArgs(void* vmargs);
  jint JNI_CreateJavaVM(JavaVM** p_vm, JNIEnv** p_env, void* vm_args);
  jint JNI_GetCreatedJavaVMs(JavaVM** vms, jsize size, jsize* vm_count);

 private:
  JniInvocationImpl(const JniInvocationImpl&) = delete;
  JniInvocationImpl& operator=(const JniInvocationImpl&) = delete;

  bool FindSymbol(FUNC_POINTER* pointer, const char* symbol);

  static JniInvocationImpl* jni_invocation_;

  // Handle to library opened with dlopen(). Library exports
  // JNI_GetDefaultJavaVMInitArgs, JNI_CreateJavaVM, JNI_GetCreatedJavaVMs.
  void* handle_;
  jint (*JNI_GetDefaultJavaVMInitArgs_)(void*);
  jint (*JNI_CreateJavaVM_)(JavaVM**, JNIEnv**, void*);
  jint (*JNI_GetCreatedJavaVMs_)(JavaVM**, jsize, jsize*);

  friend class JNIInvocation_Debuggable_Test;
  friend class JNIInvocation_NonDebuggable_Test;
};

// Check JniInvocationImpl size is same as fields, e.g. no vtable present.
static_assert(sizeof(JniInvocationImpl) == 4 * sizeof(uintptr_t));

JniInvocationImpl* JniInvocationImpl::jni_invocation_ = NULL;

JniInvocationImpl::JniInvocationImpl() :
    handle_(NULL),
    JNI_GetDefaultJavaVMInitArgs_(NULL),
    JNI_CreateJavaVM_(NULL),
    JNI_GetCreatedJavaVMs_(NULL) {
  LOG_ALWAYS_FATAL_IF(jni_invocation_ != NULL, "JniInvocation instance already initialized");
  jni_invocation_ = this;
}

JniInvocationImpl::~JniInvocationImpl() {
  jni_invocation_ = NULL;
  if (handle_ != NULL) {
    CloseLibrary(handle_);
  }
}

static const char* kLibraryFallback = "libart.so";

const char* JniInvocationImpl::GetLibrary(const char* library,
                                          char* buffer,
                                          bool (*is_debuggable)(),
                                          int (*get_library_system_property)(char* buffer)) {
#ifdef __ANDROID__
  const char* default_library;

  if (!is_debuggable()) {
    // Not a debuggable build.
    // Do not allow arbitrary library. Ignore the library parameter. This
    // will also ignore the default library, but initialize to fallback
    // for cleanliness.
    library = kLibraryFallback;
    default_library = kLibraryFallback;
  } else {
    // Debuggable build.
    // Accept the library parameter. For the case it is NULL, load the default
    // library from the system property.
    if (buffer != NULL) {
      if (get_library_system_property(buffer) > 0) {
        default_library = buffer;
      } else {
        default_library = kLibraryFallback;
      }
    } else {
      // No buffer given, just use default fallback.
      default_library = kLibraryFallback;
    }
  }
#else
  UNUSED(buffer);
  UNUSED(is_debuggable);
  UNUSED(get_library_system_property);
  const char* default_library = kLibraryFallback;
#endif
  if (library == NULL) {
    library = default_library;
  }

  return library;
}

bool JniInvocationImpl::Init(const char* library) {
#ifdef __ANDROID__
  char buffer[PROP_VALUE_MAX];
#else
  char* buffer = NULL;
#endif
  library = GetLibrary(library, buffer);
  handle_ = OpenLibrary(library);
  if (handle_ == NULL) {
    if (strcmp(library, kLibraryFallback) == 0) {
      // Nothing else to try.
      ALOGE("Failed to dlopen %s: %s", library, GetError().c_str());
      return false;
    }
    // Note that this is enough to get something like the zygote
    // running, we can't property_set here to fix this for the future
    // because we are root and not the system user. See
    // RuntimeInit.commonInit for where we fix up the property to
    // avoid future fallbacks. http://b/11463182
    ALOGW("Falling back from %s to %s after dlopen error: %s",
          library, kLibraryFallback, GetError().c_str());
    library = kLibraryFallback;
    handle_ = OpenLibrary(library);
    if (handle_ == NULL) {
      ALOGE("Failed to dlopen %s: %s", library, GetError().c_str());
      return false;
    }
  }
  if (!FindSymbol(reinterpret_cast<FUNC_POINTER*>(&JNI_GetDefaultJavaVMInitArgs_),
                  "JNI_GetDefaultJavaVMInitArgs")) {
    return false;
  }
  if (!FindSymbol(reinterpret_cast<FUNC_POINTER*>(&JNI_CreateJavaVM_),
                  "JNI_CreateJavaVM")) {
    return false;
  }
  if (!FindSymbol(reinterpret_cast<FUNC_POINTER*>(&JNI_GetCreatedJavaVMs_),
                  "JNI_GetCreatedJavaVMs")) {
    return false;
  }
  return true;
}

jint JniInvocationImpl::JNI_GetDefaultJavaVMInitArgs(void* vmargs) {
  return JNI_GetDefaultJavaVMInitArgs_(vmargs);
}

jint JniInvocationImpl::JNI_CreateJavaVM(JavaVM** p_vm, JNIEnv** p_env, void* vm_args) {
  return JNI_CreateJavaVM_(p_vm, p_env, vm_args);
}

jint JniInvocationImpl::JNI_GetCreatedJavaVMs(JavaVM** vms, jsize size, jsize* vm_count) {
  return JNI_GetCreatedJavaVMs_(vms, size, vm_count);
}

bool JniInvocationImpl::FindSymbol(FUNC_POINTER* pointer, const char* symbol) {
  *pointer = GetSymbol(handle_, symbol);
  if (*pointer == NULL) {
    ALOGE("Failed to find symbol %s: %s\n", symbol, GetError().c_str());
    CloseLibrary(handle_);
    handle_ = NULL;
    return false;
  }
  return true;
}

JniInvocationImpl& JniInvocationImpl::GetJniInvocation() {
  LOG_ALWAYS_FATAL_IF(jni_invocation_ == NULL,
                      "Failed to create JniInvocation instance before using JNI invocation API");
  return *jni_invocation_;
}

MODULE_API jint JNI_GetDefaultJavaVMInitArgs(void* vm_args) {
  return JniInvocationImpl::GetJniInvocation().JNI_GetDefaultJavaVMInitArgs(vm_args);
}

MODULE_API jint JNI_CreateJavaVM(JavaVM** p_vm, JNIEnv** p_env, void* vm_args) {
  // Ensure any cached heap objects from previous VM instances are
  // invalidated. There is no notification here that a VM is destroyed. These
  // cached objects limit us to one VM instance per process.
  JniConstants::Invalidate();
  return JniInvocationImpl::GetJniInvocation().JNI_CreateJavaVM(p_vm, p_env, vm_args);
}

MODULE_API jint JNI_GetCreatedJavaVMs(JavaVM** vms, jsize size, jsize* vm_count) {
  return JniInvocationImpl::GetJniInvocation().JNI_GetCreatedJavaVMs(vms, size, vm_count);
}

MODULE_API JniInvocationImpl* JniInvocationCreate() {
  return new JniInvocationImpl();
}

MODULE_API void JniInvocationDestroy(JniInvocationImpl* instance) {
  delete instance;
}

MODULE_API int JniInvocationInit(JniInvocationImpl* instance, const char* library) {
  return instance->Init(library) ? 1 : 0;
}

MODULE_API const char* JniInvocationGetLibrary(const char* library, char* buffer) {
  return JniInvocationImpl::GetLibrary(library, buffer);
}

MODULE_API const char* JniInvocation::GetLibrary(const char* library,
                                                 char* buffer,
                                                 bool (*is_debuggable)(),
                                                 int (*get_library_system_property)(char* buffer)) {
  return JniInvocationImpl::GetLibrary(library, buffer, is_debuggable, get_library_system_property);
}
