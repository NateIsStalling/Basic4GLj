package com.basic4gl.language.core.runtime;

/**
 * Interface for libraries to access a VM driver when initialized
 */
public interface IVMDriverAccess {
    void init(com.basic4gl.language.core.runtime.IVMDriver driver);
}
