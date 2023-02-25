package com.basic4gl.compiler.util;

/**
 * ExtFuncSpec
 *
 * Extended function specification including information about where the
 * function is stored (whether it's built in, or stored in a DLL and which one.)
 * Used by the DLL manager to pass info to the compiler.
 */
public class ExtendedFunctionSpecification {

	/**
	 * Pointer to main specification
 	 */
	public FunctionSpecification spec;

	// Function details

	/**
	 * True = builtin, false = plugin DLL function
	 */
	public boolean builtin;

	/**
	 * Index of DLL (if applicable).
	 * Note spec.index holds index of function WITHIN the DLL.
	 */
	public int pluginIndex;
}