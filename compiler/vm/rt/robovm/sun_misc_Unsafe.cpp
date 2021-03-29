/*
 * Copyright (C) 2012 RoboVM AB
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
#include <stddef.h>
#include <robovm.h>
extern "C" {
#include "reflection_helpers.h"
}
#include <unistd.h>
#include <cstdlib>
#include <cstring>
#include <atomic>

extern "C" {
static inline jboolean checkNull(Env* env, Object* o) {
  if (!o) {
    rvmThrowNullPointerException(env);
    return FALSE;
  }
  return TRUE;
}

static inline void* getFieldAddress(Object* o, jlong offset) {
  return (void*) (((jbyte*) o) + offset);
}

jlong Java_sun_misc_Unsafe_objectFieldOffset0(Env* env, Object* unsafe, Object* fieldObject) {
  if (!checkNull(env, fieldObject)) return 0;
  InstanceField* field = (InstanceField*) getFieldFromFieldObject(env, fieldObject);
  if (!field) return 0;
  return field->offset;
}

jint Java_sun_misc_Unsafe_arrayBaseOffset0(Env* env, Object* unsafe, Class* clazz) {
  if (clazz == array_Z) {
    return offsetof(BooleanArray, values);
  }
  if (clazz == array_B) {
    return offsetof(ByteArray, values);
  }
  if (clazz == array_C) {
    return offsetof(CharArray, values);
  }
  if (clazz == array_S) {
    return offsetof(ShortArray, values);
  }
  if (clazz == array_I) {
    return offsetof(IntArray, values);
  }
  if (clazz == array_J) {
    return offsetof(LongArray, values);
  }
  if (clazz == array_F) {
    return offsetof(FloatArray, values);
  }
  if (clazz == array_D) {
    return offsetof(DoubleArray, values);
  }
  return offsetof(ObjectArray, values);
}

jint Java_sun_misc_Unsafe_arrayIndexScale0(Env* env, Object* unsafe, Class* clazz) {
  return rvmGetArrayElementSize(env, clazz);
}

jboolean Java_sun_misc_Unsafe_compareAndSwapInt(Env* env, Object* unsafe, Object* obj, jlong offset, jint expectedValue, jint newValue) {
  if (!checkNull(env, obj)) return FALSE;
  jint* address = (jint*) getFieldAddress(obj, offset);
  return rvmAtomicCompareAndSwapInt(address, expectedValue, newValue);
}

jboolean Java_sun_misc_Unsafe_compareAndSwapLong(Env* env, Object* unsafe, Object* obj, jlong offset, jlong expectedValue, jlong newValue) {
  if (!checkNull(env, obj)) return FALSE;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  return rvmAtomicCompareAndSwapLong(address, expectedValue, newValue);
}

jboolean Java_sun_misc_Unsafe_compareAndSwapObject(Env* env, Object* unsafe, Object* obj, jlong offset, Object* expectedValue, Object* newValue) {
  if (!checkNull(env, obj)) return FALSE;
  void** address = (void**) getFieldAddress(obj, offset);
  return rvmAtomicCompareAndSwapPtr(address, expectedValue, newValue);
}

jint Java_sun_misc_Unsafe_getInt__J(Env* env, Object* unsafe, jlong address) {
  jint * ptr = (jint*) address;
  return *ptr;
}

jint Java_sun_misc_Unsafe_getInt__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jint* address = (jint*) getFieldAddress(obj, offset);
  return *address;
}

jfloat Java_sun_misc_Unsafe_getFloat__J(Env* env, Object* unsafe, jlong address) {
  jfloat * ptr = (jfloat*) address;
  return *ptr;
}

jfloat Java_sun_misc_Unsafe_getFloat__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jfloat* address = (jfloat*) getFieldAddress(obj, offset);
  return *address;
}

jdouble Java_sun_misc_Unsafe_getDouble__J(Env* env, Object* unsafe, jlong address) {
  jdouble * ptr = (jdouble*) address;
  return *ptr;
}

jdouble Java_sun_misc_Unsafe_getDouble__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jdouble* address = (jdouble*) getFieldAddress(obj, offset);
  return *address;
}

jboolean Java_sun_misc_Unsafe_getBoolean(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jboolean* address = (jboolean*) getFieldAddress(obj, offset);
  return *address;
}

jbyte Java_sun_misc_Unsafe_getByte__J(Env* env, Object* unsafe, jlong address) {
  jbyte* ptr = (jbyte*) address;
  return *ptr;
}

jbyte Java_sun_misc_Unsafe_getByte__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jbyte* address = (jbyte*) getFieldAddress(obj, offset);
  return *address;
}

jshort Java_sun_misc_Unsafe_getShort__J(Env* env, Object* unsafe, jlong address) {
  jshort* ptr = (jshort*) address;
  return *ptr;
}

jshort Java_sun_misc_Unsafe_getShort__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jshort* address = (jshort*) getFieldAddress(obj, offset);
  return *address;
}

jchar Java_sun_misc_Unsafe_getChar__J(Env* env, Object* unsafe, jlong address) {
  jchar* ptr = (jchar*) address;
  return *ptr;
}

jchar Java_sun_misc_Unsafe_getChar__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jchar* address = (jchar*) getFieldAddress(obj, offset);
  return *address;
}

jint Java_sun_misc_Unsafe_getIntVolatile(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jint* address = (jint*) getFieldAddress(obj, offset);
  return rvmAtomicLoadInt(address);
}

jlong Java_sun_misc_Unsafe_getLong__J(Env* env, Object* unsafe, jlong address) {
  jlong* ptr = (jlong*) address;
  return *ptr;
}

jlong Java_sun_misc_Unsafe_getLong__Ljava_lang_Object_2J(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  return *address;
}

jlong Java_sun_misc_Unsafe_getLongVolatile(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return 0;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  return rvmAtomicLoadLong(address);
}

Object* Java_sun_misc_Unsafe_getObject(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return NULL;
  Object** address = (Object**) getFieldAddress(obj, offset);
  return *address;
}

Object* Java_sun_misc_Unsafe_getObjectVolatile(Env* env, Object* unsafe, Object* obj, jlong offset) {
  if (!checkNull(env, obj)) return NULL;
  Object** address = (Object**) getFieldAddress(obj, offset);
  return (Object*) rvmAtomicLoadPtr((void**) address);
}

void Java_sun_misc_Unsafe_putInt__JI(Env* env, Object* unsafe, jlong addr, jint newValue) {
  jint* address = (jint*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putInt__Ljava_lang_Object_2JI(Env* env, Object* unsafe, Object* obj, jlong offset, jint newValue) {
  if (!checkNull(env, obj)) return;
  jint* address = (jint*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putFloat__JF(Env* env, Object* unsafe, jlong addr, jfloat newValue) {
  jfloat* address = (jfloat*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putFloat__Ljava_lang_Object_2JF(Env* env, Object* unsafe, Object* obj, jlong offset, jfloat newValue) {
  if (!checkNull(env, obj)) return;
  jfloat* address = (jfloat*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putDouble__JD(Env* env, Object* unsafe, jlong addr, jdouble newValue) {
  jdouble* address = (jdouble*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putDouble__Ljava_lang_Object_2JD(Env* env, Object* unsafe, Object* obj, jlong offset, jdouble newValue) {
  if (!checkNull(env, obj)) return;
  jdouble* address = (jdouble*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putBoolean(Env* env, Object* unsafe, Object* obj, jlong offset, jboolean newValue) {
  if (!checkNull(env, obj)) return;
  jboolean* address = (jboolean*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putByte__JB(Env* env, Object* unsafe, jlong addr, jbyte newValue) {
  jbyte* address = (jbyte*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putByte__Ljava_lang_Object_2JB(Env* env, Object* unsafe, Object* obj, jlong offset, jbyte newValue) {
  if (!checkNull(env, obj)) return;
  jbyte* address = (jbyte*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putChar__JC(Env* env, Object* unsafe, jlong addr, jchar newValue) {
  jchar* address = (jchar*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putChar__Ljava_lang_Object_2JC(Env* env, Object* unsafe, Object* obj, jlong offset, jchar newValue) {
  if (!checkNull(env, obj)) return;
  jchar* address = (jchar*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putShort__JS(Env* env, Object* unsafe, jlong addr, jshort newValue) {
  jshort* address = (jshort*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putShort__Ljava_lang_Object_2JS(Env* env, Object* unsafe, Object* obj, jlong offset, jshort newValue) {
  if (!checkNull(env, obj)) return;
  jshort* address = (jshort*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putIntVolatile(Env* env, Object* unsafe, Object* obj, jlong offset, jint newValue) {
  if (!checkNull(env, obj)) return;
  jint* address = (jint*) getFieldAddress(obj, offset);
  rvmAtomicStoreInt(address, newValue);
}

void Java_sun_misc_Unsafe_putLong__JJ(Env* env, Object* unsafe, jlong addr, jlong newValue) {
  jlong* address = (jlong*) addr;
  *address = newValue;
}

void Java_sun_misc_Unsafe_putLong__Ljava_lang_Object_2JJ(Env* env, Object* unsafe, Object* obj, jlong offset, jlong newValue) {
  if (!checkNull(env, obj)) return;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putLongVolatile(Env* env, Object* unsafe, Object* obj, jlong offset, jlong newValue) {
  if (!checkNull(env, obj)) return;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  rvmAtomicStoreLong(address, newValue);
}

void Java_sun_misc_Unsafe_putObject(Env* env, Object* unsafe, Object* obj, jlong offset, Object* newValue) {
  if (!checkNull(env, obj)) return;
  Object** address = (Object**) getFieldAddress(obj, offset);
  *address = newValue;
}

void Java_sun_misc_Unsafe_putObjectVolatile(Env* env, Object* unsafe, Object* obj, jlong offset, Object* newValue) {
  if (!checkNull(env, obj)) return;
  Object** address = (Object**) getFieldAddress(obj, offset);
  rvmAtomicStorePtr((void**) address, newValue);
}

void Java_sun_misc_Unsafe_putOrderedInt(Env* env, Object* unsafe, Object* obj, jlong offset, jint newValue) {
  if (!checkNull(env, obj)) return;
  jint* address = (jint*) getFieldAddress(obj, offset);
  rvmAtomicSynchronize();
  *address = newValue;
}

void Java_sun_misc_Unsafe_putOrderedLong(Env* env, Object* unsafe, Object* obj, jlong offset, jlong newValue) {
  if (!checkNull(env, obj)) return;
  jlong* address = (jlong*) getFieldAddress(obj, offset);
  rvmAtomicSynchronize();
  *address = newValue;
}

void Java_sun_misc_Unsafe_putOrderedObject(Env* env, Object* unsafe, Object* obj, jlong offset, Object* newValue) {
  if (!checkNull(env, obj)) return;
  Object** address = (Object**) getFieldAddress(obj, offset);
  rvmAtomicSynchronize();
  *address = newValue;
}

Object* Java_sun_misc_Unsafe_allocateInstance(Env* env, Object* unsafe, Class* c) {
  return rvmAllocateObject(env, c);
}

jint Java_sun_misc_Unsafe_addressSize(Env* env, jobject ob) {
  return sizeof(void*);
}

jint Java_sun_misc_Unsafe_pageSize(Env* env, jobject ob) {
  return sysconf(_SC_PAGESIZE);
}

jlong Java_sun_misc_Unsafe_allocateMemory(Env* env, jobject ob, jlong bytes) {
  return PTR_TO_LONG(rvmAllocateMemoryUncollectable(env, bytes));
}

void Java_sun_misc_Unsafe_freeMemory(Env* env, jobject ob, jlong address) {
  rvmFreeMemoryUncollectable(env, LONG_TO_PTR(address));
}

void Java_sun_misc_Unsafe_setMemory(Env* env, jobject ob, jlong address, jlong bytes, jbyte value) {
  memset(reinterpret_cast<void*>(static_cast<uintptr_t>(address)), value, bytes);
}

void Java_sun_misc_Unsafe_copyMemory(Env *env, jobject unsafe, jlong src,
                                     jlong dst, jlong size) {
  if (size == 0) {
    return;
  }
  // size is nonnegative and fits into size_t
  if (size < 0 || size != (jlong)(size_t) size) {
    rvmThrowNewf(env, java_lang_IllegalArgumentException,"Invalid size: %ld", size);
  }
  size_t sz = (size_t)size;
  memcpy(reinterpret_cast<void *>(dst), reinterpret_cast<void *>(src), sz);
}

void Java_sun_misc_Unsafe_loadFence(Env*, jobject) {
  std::atomic_thread_fence(std::memory_order_acquire);
}

void Java_sun_misc_Unsafe_storeFence(Env*, jobject) {
  std::atomic_thread_fence(std::memory_order_release);
}

void Java_sun_misc_Unsafe_fullFence(Env*, jobject) {
  std::atomic_thread_fence(std::memory_order_seq_cst);
}

} // extern "C"