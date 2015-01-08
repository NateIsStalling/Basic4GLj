package com.basic4gl.lib.util;
////////////////////////////////////////////////////////////////////////////////
//IB4GLCompiler
//
/// Used to compile and execute Basic4GL code at RUNTIME.
/// IMPORTANT:
///  1. Plugins should ONLY compile code while a program is running (i.e. from
///     within a runtime function.
///  2. When the program ends the plugin should consider all handles from
///     compiled blocks of code become INVALID. The plugin should not try to
///     call them.
public interface Compiler {
	/// Compile Basic4GL code and return its handle.
    /// Returns a non-zero handle if compilation succeeded.
    /// Returns 0 if an error occurs.
    public abstract int Compile(String sourceText);

    // Error retrieving methods. Call only if Compile() returns 0.

    /// Get error description.
    /// Error is placed in buffer.
    /// If error is longer than bufferLen, it will be truncated to fit.
    public abstract void GetErrorText(String buffer, int bufferLen);

    /// Line number where error occurred
    public abstract int GetErrorLine();

    /// Column number where error occurred
    public abstract int GetErrorCol();

    // Execute code block

    /// Execute code.
    /// Returns true if code executed without a runtime error.
    /// Returns false if an error occurred. If an error occurs, the plugin
    /// should return immediately. Basic4GL will then stop and display the error.
    public abstract boolean Execute(int codeHandle);
}
