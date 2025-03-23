package com.basic4gl.lib.util;

/**
 * Created by Nate on 11/2/2015.
 */
public interface IB4GLOpenGLText {
	// Text mode constants/variables
	enum TextMode {
		TEXT_SIMPLE(0), // (Default). After any print function, text is automatically rendered as follows:
		//      * Back buffer is cleared
		//      * Text is rendered
		//      * Buffers are swapped

		TEXT_BUFFERED(1), // Must explicitly call DrawText(), as before:
		//      * Back buffer is cleared
		//      * Text is rendered
		//      * Buffers are swapped

		TEXT_OVERLAID(2); // Same as TEXT_BUFFERED, except DrawText() only renders the text.
		// Must explictly clear and swap buffers as necessary.
		private final int mMode;

		TextMode(int mode) {
			mMode = mode;
		}

		public int getMode() {
			return mMode;
		}
	}

	void setFont(int fontTexture);

	int getDefaultFont();

	void setTextMode(TextMode mode);

	void setColor(byte red, byte green, byte blue);
}
