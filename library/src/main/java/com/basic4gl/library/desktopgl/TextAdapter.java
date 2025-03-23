package com.basic4gl.library.desktopgl;

import com.basic4gl.lib.util.IB4GLOpenGLText;

/**
 * Interface for DLLs
 */
public interface TextAdapter extends IB4GLOpenGLText {

	// DLLFUNC
	// IB4GLText
	void print(String text, boolean newline);

	void locate(int x, int y);

	void cls();

	void clearRegion(int x1, int y1, int x2, int y2);

	int getTextRows();

	int getTextCols();

	void resizeText(int cols, int rows);

	void setTextScrollEnabled(boolean scroll);

	boolean getTextScrollEnabled();

	void drawText();

	char getCharAt(int x, int y);

	// IB4GLOpenGLText
	void setFont(int fontTexture);

	int getDefaultFont();

	void setTextMode(TextMode mode);

	void setColor(byte red, byte green, byte blue);
}
