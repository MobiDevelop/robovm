/*
 * Copyright (C) 2016 The Android Open Source Project
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

package dalvik.system;

/**
 * Holder class for extraneous Class data.
 *
 * This class holds data for Class objects that is either rarely useful, only necessary for
 * debugging purposes or both. This allows us to extend the Class class without impacting memory
 * use.
 *
 * @hide For internal runtime use only.
 */
public final class ClassExt {
    /**
     * An array of all obsolete DexCache objects that are needed for obsolete methods.
     *
     * These entries are associated with the obsolete ArtMethod pointers at the same indexes in the
     * obsoleteMethods array.
     *
     * This field has native components and is a logical part of the 'Class' type.
     */
    private Object[] obsoleteDexCaches;

    /**
     * An array of all native obsolete ArtMethod pointers.
     *
     * These are associated with their DexCaches at the same index in the obsoleteDexCaches array.
     *
     * This field is actually either an int[] or a long[] depending on size of a pointer.
     *
     * This field contains native pointers and is a logical part of the 'Class' type.
     */
    private Object obsoleteMethods;

    /**
     * If set, the bytes, native pointer (as a java.lang.Long), or DexCache of the original dex-file
     * associated with the related class.
     *
     * In this instance 'original' means either (1) the dex-file loaded for this class when it was
     * first loaded after all non-retransformation capable transformations had been performed but
     * before any retransformation capable ones had been done or (2) the most recent dex-file bytes
     * given for a class redefinition.
     *
     * Needed in order to implement retransformation of classes.
     *
     * This field is a logical part of the 'Class' type.
     */
    private Object originalDexFile;

    /**
     * If class verify fails, we must return same error on subsequent tries. We may store either
     * the class of the error, or an actual instance of Throwable here.
     *
     * This field is a logical part of the 'Class' type.
     */
    private Object verifyError;

    /**
     * If set, native pointer to the initial, pre-redefine, dex file associated with the related
     * class. This is different from the {@code originalDexFile} which is the pre-retransform dex
     * file, i.e. could contain the bytes of the dex file provided during redefine.
     *
     * It is enough to store the native pointer because the pre-redefine dex file is either part
     * of boot classpath or it is being kept alive by its class loader. Class loaders always keep
     * dex files alive even if all their classes have been redefined.
     *
     * Needed in order to preserve access to dex-level hiddenapi flags after JVMTI redefine.
     *
     * This field is a logical part of the 'Class' type.
     */
    private long preRedefineDexFilePtr;

    /**
     * ClassDef index of the related class in the pre-redefine dex file. Set together with
     * {@code preRedefineDexFilePtr}.
     *
     * Needed in order to preserve access to dex-level hiddenapi flags after JVMTI redefine.
     *
     * This field is a logical part of the 'Class' type.
     */
    private int preRedefineClassDefIndex;

    /**
    * Private constructor.
    *
    * Only created by the runtime.
    */
    private ClassExt() {}
}
