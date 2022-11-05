package com.basic4gl.lib.util;
////////////////////////////////////////////////////////////////////////////////
//IB4GLText
//
/// Interface to the Basic4GL text mechanism.
/// Note:
///     This returns an interface to the OpenGL text mechanism.
///     The interface is deliberately general, and in future versions of Basic4GL
///     might be used to access other text output devices like a console window.
///     Because of this, there are some OpenGL specific commands missing, such
///     as setting fonts. To access these, you should use the IB4GLOpenGLText
///     interface defined in Basic4GLOpenGLObjects.h instead.
public abstract class Text {
	// Standard text output
	abstract void Print(String text, boolean newline);

	abstract void Locate(int x, int y);

	abstract void Cls();

	abstract void ClearRegion(int x1, int y1, int x2, int y2);

	abstract int TextRows();

	abstract int TextCols();

	abstract void ResizeText(int cols, int rows);

	abstract void SetTextScroll(boolean scroll);

	abstract boolean TextScroll();

	abstract void DrawText();

	abstract char CharAt(int x, int y);
}
