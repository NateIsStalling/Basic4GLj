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
	/**
	 * Not an actual type. Marks the start of valid types (VTP_MARKER + 1 and up)
	 */
	public static final int VTP_MARKER = -9;
	/**
	 * Like VTP_FUNC_PTR, but prototype not known at compile time.
	 * Runtime check required before assigning to a VTP_FUNC_PTR
	 */
	public static final int VTP_UNTYPED_FUNC_PTR = -8;
	/**
	 * Used to indicate an expression evaluated to a sub call (i.e. has no value)
	 */
	public static final int VTP_VOID = -7;
	/**
	 * Function pointer. (Internally stored as an integer)
	 */
	public static final int VTP_FUNC_PTR = -6;

	public static final int VTP_INT = -5;
	public static final int VTP_REAL = -4;
	public static final int VTP_STRING = -3;
	public static final int VTP_NULL = -2;
	public static final int VTP_UNDEFINED = -1;

	public static String getName(int bvt) {
		switch (bvt) {
			case VTP_UNTYPED_FUNC_PTR:
				return "VTP_UNTYPED_FUNC_PTR";
			case VTP_VOID:
				return "VTP_VOID";
			case VTP_FUNC_PTR:
				return "VTP_FUNC_PTR";
			case VTP_INT:
				return "VTP_INT";
			case VTP_REAL:
				return "VTP_REAL";
			case VTP_STRING:
				return "VTP_STRING";
			case VTP_NULL:
				return "VTP_NULL";
			case VTP_UNDEFINED:
				return "VTP_UNDEFINED";
			default:
				return "???";
		}
	}
}
