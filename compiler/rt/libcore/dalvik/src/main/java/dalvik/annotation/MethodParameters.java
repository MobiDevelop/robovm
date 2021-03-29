// RoboVM Note: TODO: FIXME: commented out due old Reflection being used, Executable is not available there
///*
// * Copyright (C) 2016 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package dalvik.annotation;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import java.lang.reflect.Parameter;
//
///**
// * A system annotation that can optionally be used to provide parameter metadata such as
// * parameter names and modifiers.
// *
// * <p>The annotation can be omitted from a method / constructor safely when the parameter metadata
// * is not needed / desired at runtime. {@link Parameter#isNamePresent()} can be used to check
// * whether metadata is present for a parameter, and the associated reflection methods like
// * {@link java.lang.reflect.Parameter#getName()} will fall back to default behavior at runtime if
// * the information is not present.
// *
// * <p>When including parameter metadata, compilers should include parameter metadata for generated
// * classes like enums, since the parameter metadata includes whether or not a parameter is
// * synthetic or mandated.
// *
// * <p>MethodParameters currently only describes individual method parameters and there is no
// * mechanism to detect whether parameter method data is <em>generally</em> present for an
// * {@link java.lang.reflect.Executable}. Therefore, it is code-size and runtime efficient to omit
// * the annotation entirely for constructors and methods that have no parameters.
// */
//@Retention(RetentionPolicy.RUNTIME)
//@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
//@interface MethodParameters {
//
//    /*
//     * This annotation is never used in source code; it is expected to be generated in .dex
//     * files by tools like compilers. Commented definitions for the annotation members expected
//     * by the runtime / reflection code can be found below for reference.
//     *
//     * The arrays documented below must be the same size as for the method_id_item dex structure
//     * associated with the method otherwise a java.lang.reflect.MalformedParametersException will
//     * be thrown at runtime.
//     *
//     * That is: method_id_item.proto_idx -> proto_id_item.parameters_off -> type_list.size must
//     * be the same as names().length and accessFlags().length.
//     *
//     * Because MethodParameters describes all formal method parameters, even those not explicitly
//     * or implicitly declared in source code, the size of the arrays may differ from the Signature
//     * or other metadata information that can be based only on explicit parameters declared in
//     * source code. MethodParameters will also not include any information about type annotation
//     * receiver parameters that do not exist in the actual method signature.
//     */
//
//
//    /*
//     * The names of formal parameters for the associated method. The array cannot be null, but can
//     * be empty if there are no formal parameters. A value in the array can be null if the formal
//     * parameter with that index has no name.
//     *
//     * If parameter name Strings are empty or contain '.', ';', '[' or '/' then a
//     * java.lang.reflect.MalformedParametersException will be thrown at runtime.
//     */
//    // String[] names();
//
//    /*
//     * The access flags of the formal parameters for the associated method. The array cannot be
//     * null, but can be empty if there are no formal parameters.
//     *
//     * The value is a bit mask with the follow values:
//     * 0x0010 : final, the parameter was declared final
//     * 0x1000 : synthetic, the parameter was introduced by the compiler.
//     * 0x8000 : mandated, the parameter is synthetic but also implied by the language
//     *          specification.
//     *
//     * If any bits are set outside of this set then a java.lang.reflect.MalformedParametersException
//     * will be thrown at runtime.
//     */
//    // int[] accessFlags();
//}
//
