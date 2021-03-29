/*
 * Copyright (C) 2018 The Android Open Source Project
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

package dalvik.annotation.codegen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates to the platform toolchain that there is an upcoming public SDK API change for a method.
 * The API change will add a more specific return type in a subclass for an overridden method.
 *
 * <p>When this annotation is present, the toolchain is required to generate on-device bytecode for
 * an overloaded implementation of the method which is marked as synthetic, returns the more
 * specific type given in {@link CovariantReturnType#returnType()}, and which delegates to the
 * method being annotated.
 *
 * <p>At some future point in time the public Android API can change so that the public API method
 * returns the new type. Android platform developers can be sure that all releases after the API
 * version specified in {@link CovariantReturnType#presentAfter()} have the new method signature on
 * device and will therefore be compatible with code compiled against the newest public Android API
 * stubs.
 *
 * <p>Once the actual method signature is updated to the new form and this annotation is removed
 * there will be a synthetic overload for the method with the more general type provided (as normal)
 * by the Java compiler ensuring compatibility with code compiled against the old stubs.
 *
 * <p>When adding this annotation to platform classes the developer should also add CTS tests to
 * ensure the expected methods are present on all Android devices.
 *
 * <p>Notes on scope and limitations:
 * <ul>
 * <li>This annotation must have no effect on generated API stubs.</li>
 * <li>This annotation does not allow the declared exceptions to be made more specific for the
 * generated synthetic method. This could be added later.</li>
 * <li>Any type parameters on the annotated method need not be copied.</li>
 * <li>This annotation is <em>not</em> expected to be treated by the toolchain as inherited. All
 * layers of platform class hierarchy must specify {@link CovariantReturnType} for all the overloads
 * that have to be generated. The annotation is marked as repeatable for this reason.</li>
 * </ul>
 *
 * @hide
 */
@Repeatable(CovariantReturnType.CovariantReturnTypes.class)
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD})
public @interface CovariantReturnType {

    /**
     * The return type of the synthetic method to generate. Must be a subclass of the return type
     * of the method being annotated.
     */
    Class<?> returnType();

    /**
     * The last Android API level not to have the generated synthetic method. The annotation can be
     * removed and the actual return type updated when support for this API level is dropped.
     */
    int presentAfter();

    /** @hide */
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD})
    @interface CovariantReturnTypes {
        CovariantReturnType[] value();
    }
}
