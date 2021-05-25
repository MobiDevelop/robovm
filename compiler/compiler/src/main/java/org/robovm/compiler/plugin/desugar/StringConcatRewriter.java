package org.robovm.compiler.plugin.desugar;

import soot.*;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;

import java.util.*;

public class StringConcatRewriter {
    private static final String JAVA_OBJECT = "java.lang.Object";
    private static final String JAVA_STRING = "java.lang.String";
    private static final String JAVA_STRINGBUILDER = "java.lang.StringBuilder";
    private static final String JAVA_STRINGBUILDER_INIT = "void <init>()";
    private static final String JAVA_STRINGBUILDER_TOSTRING = "java.lang.String toString()";
    private static final String APPEND = "append";

    private SootMethod StringBuilder_init;
    private SootMethod StringBuilder_toString;
    private SootMethod defaultStringBuilder_append;
    private Map<Type, SootMethod> StringBuilder_append;

    public StringConcatRewriter() {
        init();
    }

    private void init() {
        SootClass java_lang_StringBuilder = SootResolver.v().makeClassRef(JAVA_STRINGBUILDER);
        StringBuilder_init = java_lang_StringBuilder.getMethod(JAVA_STRINGBUILDER_INIT);
        StringBuilder_toString = java_lang_StringBuilder.getMethod(JAVA_STRINGBUILDER_TOSTRING);

        StringBuilder_append = new HashMap<>();
        defaultStringBuilder_append = java_lang_StringBuilder
                .getMethod(APPEND, Collections.singletonList(RefType.v(JAVA_OBJECT)),
                        RefType.v(JAVA_STRINGBUILDER));

        StringBuilder_append.put(BooleanType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(BooleanType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(CharType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(CharType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(ByteType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(IntType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(ShortType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(IntType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(IntType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(IntType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(LongType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(LongType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(FloatType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(FloatType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(DoubleType.v(),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(DoubleType.v()),
                        RefType.v(JAVA_STRINGBUILDER)));
        StringBuilder_append.put(RefType.v(JAVA_STRING),
                java_lang_StringBuilder.getMethod(APPEND, Collections.singletonList(RefType.v(JAVA_STRING)),
                        RefType.v(JAVA_STRINGBUILDER)));
    }

    public LinkedList<Unit> rewriteMakeConcat(Body body, Value outValue, List<Value> concatArgs) {
        return rewrite(body, outValue, concatArgs);
    }

    public LinkedList<Unit> rewriteMakeConcatWithConstants(Body body, Value outValue,
            List<Value> concatArgs, List<Value> bootstrapArgs) {
        Value recipeValue = bootstrapArgs.get(0);
        if (!(recipeValue instanceof StringConstant)) {
            throw new IllegalArgumentException("makeConcatWithConstants argument 'recipe' must be a String!");
        }

        String recipe = ((StringConstant) recipeValue).value;
        List<Value> args = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int constIndex = 1; // First constant is recipe
        int length = recipe.length();

        for (int i = 0; i < length; i++) {
            char c = recipe.charAt(i);
            if (c == '\u0001') { // Reference to argument
                if (sb.length() > 0) {
                    args.add(StringConstant.v(sb.toString()));
                    sb.setLength(0);
                }
                args.add(concatArgs.get(argIndex++));
            } else if (c == '\u0002') { // Reference to constant
                StringConstant constant = (StringConstant) bootstrapArgs.get(constIndex++);
                sb.append(constant.value);
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            args.add(StringConstant.v(sb.toString()));
        }

        return rewrite(body, outValue, args);
    }

    private LinkedList<Unit> rewrite(Body body, Value outValue, List<Value> args) {
        LinkedList<Unit> newUnits = new LinkedList<>();

        // Create new local StringBuilder
        Local stringBuilderLocal = createNewLocal(body, RefType.v(JAVA_STRINGBUILDER));
        newUnits.add(
                Jimple.v().newAssignStmt(stringBuilderLocal, Jimple.v().newNewExpr(RefType.v(JAVA_STRINGBUILDER))));

        // Initialize local StringBuilder
        newUnits.add(Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(stringBuilderLocal, StringBuilder_init.makeRef())));

        for (Value value : args) {
            if (value instanceof StringConstant) {
                // Create new constant string local
                Local constString = createNewLocal(body, RefType.v(JAVA_STRING));
                newUnits.add(Jimple.v().newAssignStmt(constString, value));
                value = constString;
            }

            // Append argument to StringBuilder
            newUnits.add(Jimple.v().newInvokeStmt(
                    Jimple.v().newVirtualInvokeExpr(stringBuilderLocal,
                            StringBuilder_append.getOrDefault(value.getType(),
                                    defaultStringBuilder_append).makeRef(),
                            value)));
        }

        // Assign StringBuilder.toString()
        newUnits.add(Jimple.v().newAssignStmt(outValue,
                Jimple.v().newVirtualInvokeExpr(stringBuilderLocal, StringBuilder_toString.makeRef())));

        return newUnits;
    }

    private Local createNewLocal(Body body, Type t) {
        Local local = Jimple.v().newLocal("$v" + body.getLocals().size(), t);
        body.getLocals().add(local);

        return local;
    }
}
