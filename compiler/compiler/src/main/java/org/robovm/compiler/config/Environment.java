package org.robovm.compiler.config;

/**
 * Specifies environment kind build/deployment is targeted
 * @author dkimitsa
 */
public enum Environment {
    Native(""),
    Simulator("simulator");

    private final String llvmName;

    Environment(String llvmName) {
        this.llvmName = llvmName;
    }

    public String getLlvmName() {
        return llvmName;
    }

    public String asLlvmSuffix(String prefix) {
        return  (llvmName != null && !llvmName.isEmpty()) ? (prefix + llvmName) : "";
    }
}
