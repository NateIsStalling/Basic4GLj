package com.basic4gl.lib.util;

/**
 * Interface to the Basic4GL text mechanism.
 * Note:
 *    This returns an interface to the OpenGL text mechanism.
 *    The interface is deliberately general, and in future versions of Basic4GL
 *    might be used to access other text output devices like a console window.
 *    Because of this, there are some OpenGL specific commands missing, such
 *    as setting fonts. To access these, you should use the IB4GLOpenGLText
 *    interface instead.
 */
public abstract class Text {
	/**
	 * Standard text output
	 */
	abstract void print(String text, boolean newline);

	abstract void locate(int x, int y);

	abstract void cls();

	abstract void clearRegion(int x1, int y1, int x2, int y2);

	abstract int getTextRows();

	abstract int getTextCols();

	abstract void resizeText(int cols, int rows);

	abstract void setTextScrollEnabled(boolean scroll);

	abstract boolean isTextScrollEnabled();

	abstract void drawText();

	abstract char getCharAt(int x, int y);
}
