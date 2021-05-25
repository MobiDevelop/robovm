package org.robovm.compiler.plugin.desugar;

import org.robovm.compiler.ModuleBuilder;
import org.robovm.compiler.clazz.Clazz;
import org.robovm.compiler.config.Config;
import org.robovm.compiler.plugin.AbstractCompilerPlugin;
import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.DynamicInvokeExpr;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This plugin adds support for Java 9+ String concatenation by replacing dynamicInvoke instructions
 * to {@code java.lang.invoke.StringConcatFactory} with StringBuilder appends.
 * @author CoderBaron
 */
public class StringConcatRewriterPlugin extends AbstractCompilerPlugin {
    private StringConcatRewriter rewriter;

    private void init() {
        if (rewriter == null) {
            rewriter = new StringConcatRewriter();
        }
    }

    private static boolean isMakeConcatBootstrapMethod(SootMethodRef methodRef) {
        return methodRef.declaringClass().getName().equals("java.lang.invoke.StringConcatFactory")
                && methodRef.name().equals("makeConcat");
    }

    private static boolean isMakeConcatWithConstantsBootstrapMethod(SootMethodRef methodRef) {
        return methodRef.declaringClass().getName().equals("java.lang.invoke.StringConcatFactory")
                && methodRef.name().equals("makeConcatWithConstants");
    }

    @Override
    public void beforeClass(Config config, Clazz clazz, ModuleBuilder moduleBuilder) throws IOException {
        init();

        SootClass sootClass = clazz.getSootClass();

        for (SootMethod method : sootClass.getMethods()) {
            transformStringConcats(method);
        }
    }

    private void transformStringConcats(SootMethod method) {
        if (!method.isConcrete()) {
            return;
        }

        Body body = method.retrieveActiveBody();
        PatchingChain<Unit> units = body.getUnits();
        for (Unit unit = units.getFirst(); unit != null; unit = body.getUnits().getSuccOf(unit)) {
            if (unit instanceof DefinitionStmt) {
                if (((DefinitionStmt) unit).getRightOp() instanceof DynamicInvokeExpr) {
                    DynamicInvokeExpr expr = (DynamicInvokeExpr) ((DefinitionStmt) unit).getRightOp();

                    Value outValue = ((DefinitionStmt) unit).getLeftOp();
                    SootMethodRef bootstrapMethodRef = expr.getBootstrapMethodRef();
                    List<Value> args = expr.getArgs();
                    List<Value> bootstrapArgs = expr.getBootstrapArgs();

                    LinkedList<Unit> newUnits = null;
                    if (isMakeConcatBootstrapMethod(bootstrapMethodRef)) {
                        newUnits = rewriter.rewriteMakeConcat(body, outValue, args);
                    } else if (isMakeConcatWithConstantsBootstrapMethod(bootstrapMethodRef)) {
                        newUnits = rewriter.rewriteMakeConcatWithConstants(body, outValue, args, bootstrapArgs);
                    }

                    if (newUnits != null) {
                        // Replace string concat instruction.
                        units.insertAfter(newUnits, unit);
                        units.remove(unit);
                        unit = newUnits.getLast();
                    }
                }
            }
        }
    }
}
