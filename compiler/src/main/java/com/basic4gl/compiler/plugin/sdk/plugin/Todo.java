package com.basic4gl.library.plugin.sdk;

public class Todo {
}

////////////////////////////////////////////////////////////////////////////////
//	DLL functions

/// Called to query the DLL.
/// Basic4GL will pass pointers to:
/// A 256 character array, into which the DLL should write a 0 terminated description string
/// A major and minor version integer, into which the DLL should return version information
/// Function must be named Basic4GL_Query (and declared in an extern "C" block)
/// Function MUST return BASIC4GL_DLL_VERSION.
typedef int (DLLFUNC *DLL_Name_QueryFunction)(char *details, int *major, int *minor);

/// Called when the DLL is loaded. Function should return an object supporting
/// the IDLL_Basic4GL_Plugin interface.
/// Function must be named Basic4GL_Init (and declared in an extern "C" block)
typedef IDLL_Basic4GL_Plugin *(DLLFUNC *DLL_Basic4GL_InitFunction)();

