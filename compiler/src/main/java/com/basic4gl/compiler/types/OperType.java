package com.basic4gl.compiler.types;

/**
 * Internal compiler types
 */
public enum OperType {
    OT_OPERATOR,
    OT_RETURNBOOLOPERATOR,
    OT_BOOLOPERATOR,
    OT_LAZYBOOLOPERATOR,
    OT_LBRACKET,
    /**
     * Forces expression evaluation to stop
     */
    OT_STOP
}