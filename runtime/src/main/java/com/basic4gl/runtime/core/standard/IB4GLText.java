package com.basic4gl.runtime.core.standard;


/**
 * Interface to the Basic4GL text mechanism.
 * <p>Note:
 * This returns an interface to the OpenGL text mechanism.
 * The interface is deliberately general, and in future versions of Basic4GL
 * might be used to access other text output devices like a console window.
 * <p>Because of this, there are some OpenGL specific commands missing, such
 * as setting fonts. To access these, you should use the IB4GLOpenGLText
 * interface instead.
 */
public interface IB4GLText {
    // Standard text output
    void print(String text, boolean newline);
    void locate(int x, int y);
    void cls();
    void clearRegion(int x1, int y1, int x2, int y2);
    int textRows();
    int textCols();
    void resizeText(int cols, int rows);
    void setTextScroll(boolean scroll);
    boolean textScroll();
    void drawText();
    char charAt(int x, int y);
}
