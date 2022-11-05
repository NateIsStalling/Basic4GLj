package com.basic4gl.compiler.util;


////////////////////////////////////////////////////////////////////////////////
//ExtFuncSpec
//
//Extended function specification including information about where the
//function is stored (whether it's built in, or stored in a DLL and which one.)
//Used by the DLL manager to pass info to the compiler.
public class ExtFuncSpec {

	// Pointer to main specification
	public FuncSpec m_spec;

	// Function details
	public boolean m_builtin; // True = builtin, false = plugin DLL function
	public int m_pluginIndex; // Index of DLL (if applicable). Note m_spec.m_index
						// holds index of function WITHIN the DLL.
}