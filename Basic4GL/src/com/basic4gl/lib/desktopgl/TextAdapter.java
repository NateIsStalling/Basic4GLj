package com.basic4gl.lib.desktopgl;

import com.basic4gl.lib.util.IB4GLOpenGLText;

/**
 * Created by Nate on 11/2/2015.
 */
////////////////////////////////////////////////////////////////////////////////
// Interface for DLLs

public interface TextAdapter extends IB4GLOpenGLText {

    //DLLFUNC
    // IB4GLText
    void Print(String text, boolean newline);
    void Locate(int x, int y);
    void Cls();
    void ClearRegion(int x1, int y1, int x2, int y2);
    int TextRows();
    int TextCols();
    void ResizeText(int cols, int rows);
    void SetTextScroll(boolean scroll);
    boolean TextScroll();
    void DrawText();
    char CharAt(int x, int y);

    // IB4GLOpenGLText
    void Font(int fontTexture);
    int DefaultFont();
    void SetTextMode(TextMode mode);
    void Color(byte red, byte green, byte blue);
}
