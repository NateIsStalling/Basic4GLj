package com.basic4gl.lib.util;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.util.FunctionSpecification;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for libraries that register functions and constants with the compiler
 */
public interface FunctionLibrary extends Library {
/**
* Returns a list of constants for the compiler to use
*/
Map<String, Constant> constants();

/**
* Initialize and register functions and constants with the compiler.
*/
Map<String, FunctionSpecification[]> specs();

/**
* Documentation for functions and constants included in library.
* @return HashMap<token, description>
*/
HashMap<String, String> getTokenTips();
}
