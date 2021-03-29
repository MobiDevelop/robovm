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

package dalvik.annotation.optimization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow the implementation to eliminate object references that are no longer needed, just as it
 * would eliminate other dead variables.
 *
 * The annotation applies to methods of the annotated class, and to methods declared in any inner
 * class declared inside the annotated class. It does not apply to methods declared in subclasses
 * or superclasses. It is ignored for interfaces.
 *
 * The annotation promises that the implementation of this class remains correct if objects are
 * finalized, and java.lang.ref References are enqueued, as soon as the JLS allows them to be,
 * i.e. after they are allowed to be marked as unreachable according to the JLS. Historically, the
 * Android runtime has garbage collected objects later than the JLS would allow, by retaining
 * objects references that are no longer needed. Some classes that implement finalize() or make
 * use of java.lang.ref APIs may have grown to depend on this historic Android runtime behavior.
 *
 * For example, consider the method
 *
 * <pre> {@code
 *   void m() {
 *     long tmpNativePtr = this.nativePtrField;
 *     foo(tmpNativePtr);
 *   }}</pre>
 *
 * where foo() accesses the memory M referenced by {@code this.nativePtrField}.  Once {@code
 * tmpNativePtr} is computed, this object itself is no longer needed, and may be unreachable by JLS
 * rules.  This may cause this object to be finalized, and M to be deleted, while {@code foo()} is
 * still running and accessing M.
 *
 * Historically, ART has avoided this by keeping "this" reachable until {@code m()} completes.
 * (Class file and dex level tools may not always do the same.) With this annotation, the Android
 * runtime may no longer retain the "this" reference, again making the erroneous early
 * finalization possible.
 *
 * The above example code could be made safe to annotate with {@code DeadReferenceSafe} by either
 * adding Reference.reachabilityFence(this) to the end of {@code m()} and similar methods, or
 * (Android-only) by declaring nativePtrField with a {@code @ReachabilitySensitive} annotation.
 *
 * The annotation will normally be safe for classes that do not use or rely on finalization-like
 * cleanup mechanisms. It is unlikely to be safe for classes that use {@code java.lang.ref} or
 * finalization but never mention {@code reachabilityFence()} or {@code @ReachabilitySensitive}.
 *
 * When it is safe, this annotation might result in better performance because more memory can be
 * reclaimed earlier by the garbage collector.
 *
 * Since this is technically safe for all classes that follow Java language rules, we expect this
 * to eventually become the default. This annotation allows us to selectively enable dead
 * reference elimination during the transition, and while the details of {@code
 * @ReachabilitySensitive} (like its name) are in flux.
 *
 * @hide
 */
@Retention(RetentionPolicy.RUNTIME)  // Let the GC or interpreter ask, if they need to.
@Target({ElementType.TYPE})
public @interface DeadReferenceSafe {}
