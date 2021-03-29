/*
 * Copyright (C) 2017 The Android Open Source Project
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

package dalvik.annotation.optimization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to non-static fields (instance variables) that act as handles to data that is
 * explicitly cleaned up in response to the containing object becoming unreachable. Such cleanup
 * is triggered by the garbage collector, typically by enqueuing a java.lang.ref.Reference, or by
 * invoking an overridden finalize() method. The annotation is needed only when such explicit
 * GC-triggered cleanup mechanisms are used.
 *
 * Most commonly, the fields f annotated this way will have primitive long type, but actually hold
 * native pointers, as in: <pre>   {@code
 *
 * {@literal @}ReachabilitySensitive
 * private long nativePtr; // C++ pointer to NativeFoo.
 * }</pre>
 *
 * Less frequently, such fields may also be e.g. Java references to Java objects that in turn
 * contain such native pointers. Or they may be e.g. Java ints that are used to access Java data
 * external to the object containing f.
 *
 * Specifically, an access inside a (static or instance) method of class C to a non-static
 * field f of C declared ReachabilitySensitive behaves as though it results in the introduction of
 * java.lang.ref.Reference.reachabilityFence()s according to the following rules:
 *
 * 1) For every local reference variable v declared immediately inside lexical scope s, if s
 * contains such an access a, such that the field f accessed by a is reachable from v, then
 * Reference.reachabilityFence(v) will be executed just before either (1) the exit of the scope s,
 * or (2) just before any assignment to v. For our purposes, “this” is treated as a variable
 * declared at method scope, as if it were an explicit parameter.
 *
 * 2) Define the full-expression containing e to be the largest enclosing expression f containing
 * e, such that there is no statement both containing e and properly contained in f.  If the
 * full-expression containing the allocation of the object containing the field f is the same
 * full-expression as the full-expression containing the access a, then
 * Reference.reachabilityFence(p), where p is a reference to the object containing f, is executed
 * at the end of the full expression.
 *
 * Some tools may implement these semantics by simply refusing to eliminate any dead references
 * in a method accessing an @ReachabilitySensitive field of the same class.
 *
 * If the annotation is applied to an instance method, calls to that method are treated
 * as accesses to a ReachabilitySensitive field of that object. Classes will normally
 * not provide getter methods for ReachabilitySensitive fields, since that introduces a
 * subtle dependency between the useful lifetime of the return value and the reachability
 * of the original object. However if this cannot be avoided, such a getter method should
 * be annotated as @ReachabilitySensitive.
 *
 * The annotation directly affects only methods of the containing class. There are situations in
 * which accesses from another class (or calls from another class to an annotated method) are
 * unavoidable. Normally all such accesses should be accompanied by corresponding
 * reachabilityFence() calls. The @ReachabilitySensitive annotation allows tools to check that
 * this is done.
 *
 * Note that the annotation also does not affect subclass methods. That is commonly OK. For
 * example, native pointers should normally be declared private, and thus will only be accessed
 * by methods of the same class. If an access from a subclass is unavoidable, again the
 * annotation may allow tools to check for the required reachabilityFences.
 *
 * @hide
 */
@Retention(RetentionPolicy.RUNTIME)  // Let the GC or interpreter ask, if they need to.
                                     // TODO(b/72332040): Reconsider retention later.
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ReachabilitySensitive {}
