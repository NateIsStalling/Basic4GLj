package com.basic4gl.runtime.types;

/**
 * Basic4GL data types are indicated by an integer.
 * -1 is special, and indicates an undefined type.
 * Other negative numbers are the basic supported types of language. That
 * is,
 * string, integer and floating point ("real").
 *
 * Positive numbers represent structure types, and the value is the index of
 * the structure in the structures array.
 *
 * Note: Basic value types are loaded into registers directly.
 * Other types (arrays and structures) are loaded as pointers.
 */
public class BasicValType {
    public static final int VTP_INT = -5;
    public static final int VTP_REAL = -4;
    public static final int VTP_STRING = -3;
    public static final int VTP_NULL = -2;
    public static final int VTP_UNDEFINED = -1;
}
