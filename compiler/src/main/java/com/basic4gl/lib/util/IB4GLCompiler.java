package com.basic4gl.lib.util;

/**
 * Used to compile and execute Basic4GL code at RUNTIME.
 * IMPORTANT:
 *  1. Plugins should ONLY compile code while a program is running (i.e. from
 *     within a runtime function.
 *  2. When the program ends the plugin should consider all handles from
 *     compiled blocks of code become INVALID. The plugin should not try to
 *     call them.
 */
public interface IB4GLCompiler {

/**
* Compile Basic4GL code and return its handle.
* @param sourceText
* @return Returns a non-zero handle if compilation succeeded. Returns 0 if an error occurs.
*/
int compile(String sourceText);

// Error retrieving methods. Call only if Compile() returns 0.

/**
* Get error description.
*/
String getErrorText();

/**
* Line number where error occurred
*/
int getErrorLine();

/**
* Column number where error occurred
*/
int getErrorColumn();

// Execute code block

/**
* Execute code.
* Returns true if code executed without a runtime error.
* Returns false if an error occurred. If an error occurs, the plugin
* should return immediately. Basic4GL will then stop and display the error.
*/
boolean execute(int codeHandle);
}
