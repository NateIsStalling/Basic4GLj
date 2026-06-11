package com.basic4gl.compiler.types;

public enum UserFunctionType {
    /**
     * Function implementation
     */
    UFT_IMPLEMENTATION,
    /**
     * Forward declaration
     */
    UFT_FWDDECLARATION,
    /**
     * Declaring a function pointer type
     */
    UFT_RUNTIMEDECLARATION,
    /**
     * Declaring a function pointer type
     */
    UFT_POINTER
}
