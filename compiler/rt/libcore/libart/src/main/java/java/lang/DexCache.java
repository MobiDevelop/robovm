/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package java.lang;

import dalvik.annotation.compat.UnsupportedAppUsage;
import dalvik.annotation.optimization.FastNative;

/**
 * A dex cache holds resolved copies of strings, fields, methods, and classes from the dexfile.
 */
final class DexCache {
    /** The location of the associated dex file. */
    private String location;

    /** Holds C pointer to dexFile. */
    @UnsupportedAppUsage
    private long dexFile;

    /**
     * References to pre resolved strings.
     */
    private long preResolvedStrings;

    /**
     * References to CallSite (C array pointer) as they become resolved following
     * interpreter semantics.
     */
    private long resolvedCallSites;

    /**
     * References to fields (C array pointer) as they become resolved following
     * interpreter semantics. May refer to fields defined in other dex files.
     */
    private long resolvedFields;

    /**
     * References to MethodType (C array pointer) as they become resolved following
     * interpreter semantics.
     */
    private long resolvedMethodTypes;

    /**
     * References to methods (C array pointer) as they become resolved following
     * interpreter semantics. May refer to methods defined in other dex files.
     */
    private long resolvedMethods;

    /**
     * References to types (C array pointer) as they become resolved following
     * interpreter semantics. May refer to types defined in other dex files.
     */
    private long resolvedTypes;

    /**
     * References to strings (C array pointer) as they become resolved following
     * interpreter semantics. All strings are interned.
     */
    private long strings;

    /**
     * The number of elements in the native pre resolved strings array.
     */
    private int numPreResolvedStrings;

    /**
     * The number of elements in the native call sites array.
     */
    private int numResolvedCallSites;

    /**
     * The number of elements in the native resolvedFields array.
     */
    private int numResolvedFields;

    /**
     * The number of elements in the native method types array.
     */
    private int numResolvedMethodTypes;

    /**
     * The number of elements in the native resolvedMethods array.
     */
    private int numResolvedMethods;

    /**
     * The number of elements in the native resolvedTypes array.
     */
    private int numResolvedTypes;

    /**
     * The number of elements in the native strings array.
     */
    private int numStrings;

    // Only created by the VM.
    private DexCache() {}
}

