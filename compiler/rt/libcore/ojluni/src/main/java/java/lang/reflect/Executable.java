// RoboVM Note: TODO: FIXME: commented out due old Reflection being used, Executable is not available there
///*
// * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// *
// * This code is free software; you can redistribute it and/or modify it
// * under the terms of the GNU General Public License version 2 only, as
// * published by the Free Software Foundation.  Oracle designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Oracle in the LICENSE file that accompanied this code.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// * version 2 for more details (a copy is included in the LICENSE file that
// * accompanied this code).
// *
// * You should have received a copy of the GNU General Public License version
// * 2 along with this work; if not, write to the Free Software Foundation,
// * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// *
// * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
// * or visit www.oracle.com if you need additional information or have any
// * questions.
// */
//
//package java.lang.reflect;
//
//import dalvik.annotation.optimization.FastNative;
//
//import java.lang.annotation.Annotation;
//import java.util.Objects;
//import libcore.reflect.AnnotatedElements;
//import libcore.reflect.GenericSignatureParser;
//import libcore.reflect.ListOfTypes;
//import libcore.reflect.Types;
//import libcore.util.EmptyArray;
//
///**
// * A shared superclass for the common functionality of {@link Method}
// * and {@link Constructor}.
// *
// * @since 1.8
// */
//public abstract class Executable extends AccessibleObject
//    implements Member, GenericDeclaration {
//
//    // Android-changed: Extensive modifications made throughout the class for ART.
//    // Android-removed: Declared vs actual parameter annotation indexes handling.
//    // Android-removed: Type annotations runtime code. Not supported on Android.
//
//    /*
//     * Only grant package-visibility to the constructor.
//     */
//    Executable() {}
//
//    /**
//     * Does the Executable have generic information.
//     */
//    abstract boolean hasGenericInformation();
//
//    boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
//        /* Avoid unnecessary cloning */
//        if (params1.length == params2.length) {
//            for (int i = 0; i < params1.length; i++) {
//                if (params1[i] != params2[i])
//                    return false;
//            }
//            return true;
//        }
//        return false;
//    }
//
//    void separateWithCommas(Class<?>[] types, StringBuilder sb) {
//        for (int j = 0; j < types.length; j++) {
//            sb.append(types[j].getTypeName());
//            if (j < (types.length - 1))
//                sb.append(",");
//        }
//
//    }
//
//    void printModifiersIfNonzero(StringBuilder sb, int mask, boolean isDefault) {
//        int mod = getModifiers() & mask;
//
//        if (mod != 0 && !isDefault) {
//            sb.append(Modifier.toString(mod)).append(' ');
//        } else {
//            int access_mod = mod & Modifier.ACCESS_MODIFIERS;
//            if (access_mod != 0)
//                sb.append(Modifier.toString(access_mod)).append(' ');
//            if (isDefault)
//                sb.append("default ");
//            mod = (mod & ~Modifier.ACCESS_MODIFIERS);
//            if (mod != 0)
//                sb.append(Modifier.toString(mod)).append(' ');
//        }
//    }
//
//    String sharedToString(int modifierMask,
//                          boolean isDefault,
//                          Class<?>[] parameterTypes,
//                          Class<?>[] exceptionTypes) {
//        try {
//            StringBuilder sb = new StringBuilder();
//
//            printModifiersIfNonzero(sb, modifierMask, isDefault);
//            specificToStringHeader(sb);
//
//            sb.append('(');
//            separateWithCommas(parameterTypes, sb);
//            sb.append(')');
//            if (exceptionTypes.length > 0) {
//                sb.append(" throws ");
//                separateWithCommas(exceptionTypes, sb);
//            }
//            return sb.toString();
//        } catch (Exception e) {
//            return "<" + e + ">";
//        }
//    }
//
//    /**
//     * Generate toString header information specific to a method or
//     * constructor.
//     */
//    abstract void specificToStringHeader(StringBuilder sb);
//
//    String sharedToGenericString(int modifierMask, boolean isDefault) {
//        try {
//            StringBuilder sb = new StringBuilder();
//
//            printModifiersIfNonzero(sb, modifierMask, isDefault);
//
//            TypeVariable<?>[] typeparms = getTypeParameters();
//            if (typeparms.length > 0) {
//                boolean first = true;
//                sb.append('<');
//                for(TypeVariable<?> typeparm: typeparms) {
//                    if (!first)
//                        sb.append(',');
//                    // Class objects can't occur here; no need to test
//                    // and call Class.getName().
//                    sb.append(typeparm.toString());
//                    first = false;
//                }
//                sb.append("> ");
//            }
//
//            specificToGenericStringHeader(sb);
//
//            sb.append('(');
//            Type[] params = getGenericParameterTypes();
//            for (int j = 0; j < params.length; j++) {
//                String param = params[j].getTypeName();
//                if (isVarArgs() && (j == params.length - 1)) // replace T[] with T...
//                    param = param.replaceFirst("\\[\\]$", "...");
//                sb.append(param);
//                if (j < (params.length - 1))
//                    sb.append(',');
//            }
//            sb.append(')');
//            Type[] exceptions = getGenericExceptionTypes();
//            if (exceptions.length > 0) {
//                sb.append(" throws ");
//                for (int k = 0; k < exceptions.length; k++) {
//                    sb.append((exceptions[k] instanceof Class)?
//                              ((Class)exceptions[k]).getName():
//                              exceptions[k].toString());
//                    if (k < (exceptions.length - 1))
//                        sb.append(',');
//                }
//            }
//            return sb.toString();
//        } catch (Exception e) {
//            return "<" + e + ">";
//        }
//    }
//
//    /**
//     * Generate toGenericString header information specific to a
//     * method or constructor.
//     */
//    abstract void specificToGenericStringHeader(StringBuilder sb);
//
//    /**
//     * Returns the {@code Class} object representing the class or interface
//     * that declares the executable represented by this object.
//     */
//    public abstract Class<?> getDeclaringClass();
//
//    /**
//     * Returns the name of the executable represented by this object.
//     */
//    public abstract String getName();
//
//    /**
//     * Returns the Java language {@linkplain Modifier modifiers} for
//     * the executable represented by this object.
//     */
//    public abstract int getModifiers();
//
//    /**
//     * Returns an array of {@code TypeVariable} objects that represent the
//     * type variables declared by the generic declaration represented by this
//     * {@code GenericDeclaration} object, in declaration order.  Returns an
//     * array of length 0 if the underlying generic declaration declares no type
//     * variables.
//     *
//     * @return an array of {@code TypeVariable} objects that represent
//     *     the type variables declared by this generic declaration
//     * @throws GenericSignatureFormatError if the generic
//     *     signature of this generic declaration does not conform to
//     *     the format specified in
//     *     <cite>The Java&trade; Virtual Machine Specification</cite>
//     */
//    public abstract TypeVariable<?>[] getTypeParameters();
//
//    /**
//     * Returns an array of {@code Class} objects that represent the formal
//     * parameter types, in declaration order, of the executable
//     * represented by this object.  Returns an array of length
//     * 0 if the underlying executable takes no parameters.
//     *
//     * @return the parameter types for the executable this object
//     * represents
//     */
//    public abstract Class<?>[] getParameterTypes();
//
//    /**
//     * Returns the number of formal parameters (whether explicitly
//     * declared or implicitly declared or neither) for the executable
//     * represented by this object.
//     *
//     * @return The number of formal parameters for the executable this
//     * object represents
//     */
//    public int getParameterCount() {
//        throw new AbstractMethodError();
//    }
//
//    /**
//     * Returns an array of {@code Type} objects that represent the formal
//     * parameter types, in declaration order, of the executable represented by
//     * this object. Returns an array of length 0 if the
//     * underlying executable takes no parameters.
//     *
//     * <p>If a formal parameter type is a parameterized type,
//     * the {@code Type} object returned for it must accurately reflect
//     * the actual type parameters used in the source code.
//     *
//     * <p>If a formal parameter type is a type variable or a parameterized
//     * type, it is created. Otherwise, it is resolved.
//     *
//     * @return an array of {@code Type}s that represent the formal
//     *     parameter types of the underlying executable, in declaration order
//     * @throws GenericSignatureFormatError
//     *     if the generic method signature does not conform to the format
//     *     specified in
//     *     <cite>The Java&trade; Virtual Machine Specification</cite>
//     * @throws TypeNotPresentException if any of the parameter
//     *     types of the underlying executable refers to a non-existent type
//     *     declaration
//     * @throws MalformedParameterizedTypeException if any of
//     *     the underlying executable's parameter types refer to a parameterized
//     *     type that cannot be instantiated for any reason
//     */
//    public Type[] getGenericParameterTypes() {
//        // Android-changed: getGenericParameterTypes() implementation for use with ART.
//        return Types.getTypeArray(
//                getMethodOrConstructorGenericInfoInternal().genericParameterTypes, false);
//    }
//
//    /**
//     * Behaves like {@code getGenericParameterTypes}, but returns type
//     * information for all parameters, including synthetic parameters.
//     */
//    Type[] getAllGenericParameterTypes() {
//        final boolean genericInfo = hasGenericInformation();
//
//        // Easy case: we don't have generic parameter information.  In
//        // this case, we just return the result of
//        // getParameterTypes().
//        if (!genericInfo) {
//            return getParameterTypes();
//        } else {
//            final boolean realParamData = hasRealParameterData();
//            final Type[] genericParamTypes = getGenericParameterTypes();
//            final Type[] nonGenericParamTypes = getParameterTypes();
//            final Type[] out = new Type[nonGenericParamTypes.length];
//            final Parameter[] params = getParameters();
//            int fromidx = 0;
//            // If we have real parameter data, then we use the
//            // synthetic and mandate flags to our advantage.
//            if (realParamData) {
//                for (int i = 0; i < out.length; i++) {
//                    final Parameter param = params[i];
//                    if (param.isSynthetic() || param.isImplicit()) {
//                        // If we hit a synthetic or mandated parameter,
//                        // use the non generic parameter info.
//                        out[i] = nonGenericParamTypes[i];
//                    } else {
//                        // Otherwise, use the generic parameter info.
//                        out[i] = genericParamTypes[fromidx];
//                        fromidx++;
//                    }
//                }
//            } else {
//                // Otherwise, use the non-generic parameter data.
//                // Without method parameter reflection data, we have
//                // no way to figure out which parameters are
//                // synthetic/mandated, thus, no way to match up the
//                // indexes.
//                return genericParamTypes.length == nonGenericParamTypes.length ?
//                    genericParamTypes : nonGenericParamTypes;
//            }
//            return out;
//        }
//    }
//
//    /**
//     * Returns an array of {@code Parameter} objects that represent
//     * all the parameters to the underlying executable represented by
//     * this object.  Returns an array of length 0 if the executable
//     * has no parameters.
//     *
//     * <p>The parameters of the underlying executable do not necessarily
//     * have unique names, or names that are legal identifiers in the
//     * Java programming language (JLS 3.8).
//     *
//     * @throws MalformedParametersException if the class file contains
//     * a MethodParameters attribute that is improperly formatted.
//     * @return an array of {@code Parameter} objects representing all
//     * the parameters to the executable this object represents.
//     */
//    public Parameter[] getParameters() {
//        // TODO: This may eventually need to be guarded by security
//        // mechanisms similar to those in Field, Method, etc.
//        //
//        // Need to copy the cached array to prevent users from messing
//        // with it.  Since parameters are immutable, we can
//        // shallow-copy.
//        return privateGetParameters().clone();
//    }
//
//    private Parameter[] synthesizeAllParams() {
//        final int realparams = getParameterCount();
//        final Parameter[] out = new Parameter[realparams];
//        for (int i = 0; i < realparams; i++)
//            // TODO: is there a way to synthetically derive the
//            // modifiers?  Probably not in the general case, since
//            // we'd have no way of knowing about them, but there
//            // may be specific cases.
//            out[i] = new Parameter("arg" + i, 0, this, i);
//        return out;
//    }
//
//    private void verifyParameters(final Parameter[] parameters) {
//        final int mask = Modifier.FINAL | Modifier.SYNTHETIC | Modifier.MANDATED;
//
//        if (getParameterTypes().length != parameters.length)
//            throw new MalformedParametersException("Wrong number of parameters in MethodParameters attribute");
//
//        for (Parameter parameter : parameters) {
//            final String name = parameter.getRealName();
//            final int mods = parameter.getModifiers();
//
//            if (name != null) {
//                if (name.isEmpty() || name.indexOf('.') != -1 ||
//                    name.indexOf(';') != -1 || name.indexOf('[') != -1 ||
//                    name.indexOf('/') != -1) {
//                    throw new MalformedParametersException("Invalid parameter name \"" + name + "\"");
//                }
//            }
//
//            if (mods != (mods & mask)) {
//                throw new MalformedParametersException("Invalid parameter modifiers");
//            }
//        }
//    }
//
//    private Parameter[] privateGetParameters() {
//        // Use tmp to avoid multiple writes to a volatile.
//        Parameter[] tmp = parameters;
//
//        if (tmp == null) {
//
//            // Otherwise, go to the JVM to get them
//            try {
//                tmp = getParameters0();
//            } catch(IllegalArgumentException e) {
//                // Rethrow ClassFormatErrors
//                // Android-changed: Exception changed to be more descriptive.
//                MalformedParametersException e2 =
//                        new MalformedParametersException(
//                                "Invalid parameter metadata in class file");
//                e2.initCause(e);
//                throw e2;
//            }
//
//            // If we get back nothing, then synthesize parameters
//            if (tmp == null) {
//                hasRealParameterData = false;
//                tmp = synthesizeAllParams();
//            } else {
//                hasRealParameterData = true;
//                verifyParameters(tmp);
//            }
//
//            parameters = tmp;
//        }
//
//        return tmp;
//    }
//
//    boolean hasRealParameterData() {
//        // If this somehow gets called before parameters gets
//        // initialized, force it into existence.
//        if (parameters == null) {
//            privateGetParameters();
//        }
//        return hasRealParameterData;
//    }
//
//    private transient volatile boolean hasRealParameterData;
//    private transient volatile Parameter[] parameters;
//
//    // Android-changed: Added @FastNative to getParameters0()
//    @FastNative
//    private native Parameter[] getParameters0();
//
//    /**
//     * Returns an array of {@code Class} objects that represent the
//     * types of exceptions declared to be thrown by the underlying
//     * executable represented by this object.  Returns an array of
//     * length 0 if the executable declares no exceptions in its {@code
//     * throws} clause.
//     *
//     * @return the exception types declared as being thrown by the
//     * executable this object represents
//     */
//    public abstract Class<?>[] getExceptionTypes();
//
//    /**
//     * Returns an array of {@code Type} objects that represent the
//     * exceptions declared to be thrown by this executable object.
//     * Returns an array of length 0 if the underlying executable declares
//     * no exceptions in its {@code throws} clause.
//     *
//     * <p>If an exception type is a type variable or a parameterized
//     * type, it is created. Otherwise, it is resolved.
//     *
//     * @return an array of Types that represent the exception types
//     *     thrown by the underlying executable
//     * @throws GenericSignatureFormatError
//     *     if the generic method signature does not conform to the format
//     *     specified in
//     *     <cite>The Java&trade; Virtual Machine Specification</cite>
//     * @throws TypeNotPresentException if the underlying executable's
//     *     {@code throws} clause refers to a non-existent type declaration
//     * @throws MalformedParameterizedTypeException if
//     *     the underlying executable's {@code throws} clause refers to a
//     *     parameterized type that cannot be instantiated for any reason
//     */
//    public Type[] getGenericExceptionTypes() {
//        // Android-changed: getGenericExceptionTypes() implementation for use with ART.
//        return Types.getTypeArray(
//                getMethodOrConstructorGenericInfoInternal().genericExceptionTypes, false);
//    }
//
//    /**
//     * Returns a string describing this {@code Executable}, including
//     * any type parameters.
//     * @return a string describing this {@code Executable}, including
//     * any type parameters
//     */
//    public abstract String toGenericString();
//
//    /**
//     * Returns {@code true} if this executable was declared to take a
//     * variable number of arguments; returns {@code false} otherwise.
//     *
//     * @return {@code true} if an only if this executable was declared
//     * to take a variable number of arguments.
//     */
//    public boolean isVarArgs()  {
//        // Android-changed: isVarArgs() made slightly more efficient.
//        return (accessFlags & Modifier.VARARGS) != 0;
//    }
//
//    /**
//     * Returns {@code true} if this executable is a synthetic
//     * construct; returns {@code false} otherwise.
//     *
//     * @return true if and only if this executable is a synthetic
//     * construct as defined by
//     * <cite>The Java&trade; Language Specification</cite>.
//     * @jls 13.1 The Form of a Binary
//     */
//    public boolean isSynthetic() {
//        // Android-changed: isSynthetic() made slightly more efficient.
//        return (accessFlags & Modifier.SYNTHETIC) != 0;
//    }
//
//    /**
//     * Returns an array of arrays of {@code Annotation}s that
//     * represent the annotations on the formal parameters, in
//     * declaration order, of the {@code Executable} represented by
//     * this object.  Synthetic and mandated parameters (see
//     * explanation below), such as the outer "this" parameter to an
//     * inner class constructor will be represented in the returned
//     * array.  If the executable has no parameters (meaning no formal,
//     * no synthetic, and no mandated parameters), a zero-length array
//     * will be returned.  If the {@code Executable} has one or more
//     * parameters, a nested array of length zero is returned for each
//     * parameter with no annotations. The annotation objects contained
//     * in the returned arrays are serializable.  The caller of this
//     * method is free to modify the returned arrays; it will have no
//     * effect on the arrays returned to other callers.
//     *
//     * A compiler may add extra parameters that are implicitly
//     * declared in source ("mandated"), as well as parameters that
//     * are neither implicitly nor explicitly declared in source
//     * ("synthetic") to the parameter list for a method.  See {@link
//     * java.lang.reflect.Parameter} for more information.
//     *
//     * @see java.lang.reflect.Parameter
//     * @see java.lang.reflect.Parameter#getAnnotations
//     * @return an array of arrays that represent the annotations on
//     *    the formal and implicit parameters, in declaration order, of
//     *    the executable represented by this object
//     */
//    public abstract Annotation[][] getParameterAnnotations();
//
//    /**
//     * {@inheritDoc}
//     * @throws NullPointerException  {@inheritDoc}
//     */
//    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
//        Objects.requireNonNull(annotationClass);
//        // Android-changed: Implemented getAnnotation(Class) natively.
//        return getAnnotationNative(annotationClass);
//    }
//
//    // Android-changed: Implemented getAnnotation(Class) natively.
//    @FastNative
//    private native <T extends Annotation> T getAnnotationNative(Class<T> annotationClass);
//
//    /**
//     * {@inheritDoc}
//     * @throws NullPointerException {@inheritDoc}
//     */
//    @Override
//    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
//        // Android-changed: getAnnotationsByType(Class), Android uses AnnotatedElements instead.
//        return AnnotatedElements.getDirectOrIndirectAnnotationsByType(this, annotationClass);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public Annotation[] getDeclaredAnnotations()  {
//        // Android-changed: Implemented getDeclaredAnnotations() natively.
//        return getDeclaredAnnotationsNative();
//    }
//
//    // Android-added: Implemented getDeclaredAnnotations() natively.
//    @FastNative
//    private native Annotation[] getDeclaredAnnotationsNative();
//
//    // BEGIN Android-added: Additional ART-related fields and logic.
//    // This code is shared for Method and Constructor.
//
//    /** Bits encoding access (e.g. public, private) as well as other runtime specific flags */
//    @SuppressWarnings("unused") // set by runtime
//    private int accessFlags;
//
//    /**
//     * The ArtMethod associated with this Executable, required for dispatching due to entrypoints
//     * Classloader is held live by the declaring class.
//     */
//    @SuppressWarnings("unused") // set by runtime
//    private long artMethod;
//
//    /** Executable's declaring class */
//    @SuppressWarnings("unused") // set by runtime
//    private Class<?> declaringClass;
//
//    /**
//     * Overriden method's declaring class (same as declaringClass unless declaringClass is a proxy
//     * class).
//     */
//    @SuppressWarnings("unused") // set by runtime
//    private Class<?> declaringClassOfOverriddenMethod;
//
//    /** The method index of this method within its defining dex file */
//    @SuppressWarnings("unused") // set by runtime
//    private int dexMethodIndex;
//
//    /**
//     * We insert native method stubs for abstract methods so we don't have to
//     * check the access flags at the time of the method call.  This results in
//     * "native abstract" methods, which can't exist.  If we see the "abstract"
//     * flag set, clear the "native" flag.
//     *
//     * We also move the DECLARED_SYNCHRONIZED flag into the SYNCHRONIZED
//     * position, because the callers of this function are trying to convey
//     * the "traditional" meaning of the flags to their callers.
//     */
//    private static int fixMethodFlags(int flags) {
//        if ((flags & Modifier.ABSTRACT) != 0) {
//            flags &= ~Modifier.NATIVE;
//        }
//        flags &= ~Modifier.SYNCHRONIZED;
//        int ACC_DECLARED_SYNCHRONIZED = 0x00020000;
//        if ((flags & ACC_DECLARED_SYNCHRONIZED) != 0) {
//            flags |= Modifier.SYNCHRONIZED;
//        }
//        return flags & 0xffff;  // mask out bits not used by Java
//    }
//
//    final int getModifiersInternal() {
//        return fixMethodFlags(accessFlags);
//    }
//
//    final Class<?> getDeclaringClassInternal() {
//        return declaringClass;
//    }
//
//    /**
//     * Returns an array of {@code Class} objects associated with the parameter types of this
//     * Executable. If the Executable was declared with no parameters, {@code null} will be
//     * returned.
//     *
//     * @return the parameter types, or {@code null} if no parameters were declared.
//     */
//    @FastNative
//    final native Class<?>[] getParameterTypesInternal();
//
//    @FastNative
//    final native int getParameterCountInternal();
//
//    // Android provides a more efficient implementation of this method for Executable than the one
//    // implemented in AnnotatedElement.
//    @Override
//    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
//        Objects.requireNonNull(annotationType);
//        return isAnnotationPresentNative(annotationType);
//    }
//    @FastNative
//    private native boolean isAnnotationPresentNative(Class<? extends Annotation> annotationType);
//
//    final Annotation[][] getParameterAnnotationsInternal() {
//        Annotation[][] parameterAnnotations = getParameterAnnotationsNative();
//        if (parameterAnnotations == null) {
//            parameterAnnotations = new Annotation[getParameterTypes().length][0];
//        }
//        return parameterAnnotations;
//    }
//    @FastNative
//    private native Annotation[][] getParameterAnnotationsNative();
//
//    /**
//     * @hide - exposed for use by {@link Class}.
//     */
//    public final int getAccessFlags() {
//        return accessFlags;
//    }
//
//    /**
//     * @hide - exposed for use by {@code java.lang.invoke.*}.
//     */
//    public final long getArtMethod() {
//        return artMethod;
//    }
//
//    static final class GenericInfo {
//        final ListOfTypes genericExceptionTypes;
//        final ListOfTypes genericParameterTypes;
//        final Type genericReturnType;
//        final TypeVariable<?>[] formalTypeParameters;
//
//        GenericInfo(ListOfTypes exceptions, ListOfTypes parameters, Type ret,
//                TypeVariable<?>[] formal) {
//            genericExceptionTypes = exceptions;
//            genericParameterTypes = parameters;
//            genericReturnType = ret;
//            formalTypeParameters = formal;
//        }
//    }
//
//    final boolean hasGenericInformationInternal() {
//        return getSignatureAnnotation() != null;
//    }
//
//    /**
//     * Returns generic information associated with this method/constructor member.
//     */
//    final GenericInfo getMethodOrConstructorGenericInfoInternal() {
//        String signatureAttribute = getSignatureAttribute();
//        Class<?>[] exceptionTypes = this.getExceptionTypes();
//        GenericSignatureParser parser =
//                new GenericSignatureParser(this.getDeclaringClass().getClassLoader());
//        if (this instanceof Method) {
//            parser.parseForMethod(this, signatureAttribute, exceptionTypes);
//        } else {
//            parser.parseForConstructor(this, signatureAttribute, exceptionTypes);
//        }
//        return new GenericInfo(parser.exceptionTypes, parser.parameterTypes,
//                parser.returnType, parser.formalTypeParameters);
//    }
//
//    public String getSignatureAttribute() {
//        String[] annotation = getSignatureAnnotation();
//        if (annotation == null) {
//            return null;
//        }
//        StringBuilder result = new StringBuilder();
//        for (String s : annotation) {
//            result.append(s);
//        }
//        return result.toString();
//    }
//    @FastNative
//    private native String[] getSignatureAnnotation();
//
//    final boolean equalNameAndParametersInternal(Method m) {
//        return getName().equals(m.getName()) && (compareMethodParametersInternal(m) == 0);
//    }
//
//    @FastNative
//    native int compareMethodParametersInternal(Method meth);
//
//    @FastNative
//    final native String getMethodNameInternal();
//
//    @FastNative
//    final native Class<?> getMethodReturnTypeInternal();
//
//    /** A cheap implementation for {@link Method#isDefault()}. */
//    final boolean isDefaultMethodInternal() {
//        return (accessFlags & Modifier.DEFAULT) != 0;
//    }
//
//    /** A cheap implementation for {@link Method#isBridge()}. */
//    final boolean isBridgeMethodInternal() {
//        return (accessFlags & Modifier.BRIDGE) != 0;
//    }
//    // END Android-added: Additional ART-related fields and logic.
//
//}
