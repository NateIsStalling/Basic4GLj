package com.basic4gl.library.desktopgl;

import com.basic4gl.language.core.extensions.opengl.IB4GLOpenGLText;
import com.basic4gl.language.core.extensions.standard.IB4GLText;
import com.basic4gl.library.desktopgl.content.Content2DManager;
import com.basic4gl.library.desktopgl.content.GLTextGrid;

/**
 * Interface for plugins
 */
public class TextAdapter implements IB4GLText, IB4GLOpenGLText {

    private final GLTextGrid appText;
    private final Content2DManager contentManager;

    public TextAdapter(GLTextGrid appText, Content2DManager contentManager) {
        this.appText = appText;
        this.contentManager = contentManager;
    }

    // IB4GLText
    public void print(String text, boolean newline) {
        appText.write(text);
        if (newline) {
            appText.newLine();
        }
        contentManager.changeMade();
    }

    public void locate(int x, int y) {
        appText.setCursorPosition(x, y);
    }

    public void cls() {
        appText.clear();
        contentManager.changeMade();
    }

    public void clearRegion(int x1, int y1, int x2, int y2) {
        appText.clearRegion(x1, y1, x2, y2);
        contentManager.changeMade();
    }

    public int getTextRows() {
        return appText.getRows();
    }

    public int getTextCols() {
        return appText.getColumns();
    }

    public void resizeText(int cols, int rows) {
        if (rows < 1) {
            rows = 1;
        }
        if (rows > 500) {
            rows = 500;
        }
        if (cols < 1) {
            cols = 1;
        }
        if (cols > 500) {
            cols = 500;
        }
        appText.resize(rows, cols);
        contentManager.changeMade();
    }

    public void setTextScrollEnabled(boolean scroll) {
        appText.setScroll(scroll);
    }

    public boolean getTextScrollEnabled() {
        return appText.getScroll();
    }

    public void drawText() {
        contentManager.draw();
    }

    public char getCharAt(int x, int y) {
        return appText.getTextAt(x, y);
    }

    // IB4GLOpenGLText
    public void setFont(int fontTexture) {
        appText.setTexture(fontTexture);
    }

    public int getDefaultFont() {
        return appText.getDefaultTexture();
    }

    public void setTextMode(TextMode mode) {
        contentManager.setDrawMode(mode);
    }

    public void setColor(byte red, byte green, byte blue) {
        appText.setColour(GLTextGrid.makeColour(red, green, blue));
    }
}
