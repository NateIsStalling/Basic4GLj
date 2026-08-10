package com.basic4gl.language.core.runtime;

/**
 * Allows the compiler to rollback cleanly if an error occurs during
 * compilation. Used during runtime compilation to ensure the compiler
 * does not leave the VM in an unstable state.
 * Note: Currently not everything is rolled back, just enough to keep the
 * VM stable. There may still be resources used (such as code instructions
 * allocated), but they should be benign and unreachable.
 */
public class RuntimeFunctionRollbackPoint {

    private com.basic4gl.language.core.runtime.RollbackPoint vmRollback;

    private int runtimeFunctionCount;

    /**
     * Virtual machine rollback
     */
    public com.basic4gl.language.core.runtime.RollbackPoint getVmRollback() {
        return vmRollback;
    }

    public void setVmRollback(com.basic4gl.language.core.runtime.RollbackPoint vmRollback) {
        this.vmRollback = vmRollback;
    }

    /**
     * Runtime functions
     */
    public int getRuntimeFunctionCount() {
        return runtimeFunctionCount;
    }

    public void setRuntimeFunctionCount(int runtimeFunctionCount) {
        this.runtimeFunctionCount = runtimeFunctionCount;
    }
}
