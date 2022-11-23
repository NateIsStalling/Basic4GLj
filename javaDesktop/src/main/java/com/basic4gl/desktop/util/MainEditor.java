package com.basic4gl.desktop.util;

import javax.swing.*;

/**
 * Created by Nate on 3/23/2015.
 */
public interface MainEditor {
    
    // Child editor access
    public abstract int editorCount();
    public abstract JTextArea getEditor(int index);
    public abstract String getFilename(int index);
    public abstract String getCurrentDirectory();

    // Debugger and abstract machine state
    public abstract boolean isVMRunning();
    public abstract int getVMRow(String filename);
    public abstract int isBreakpt(String filename, int line);
    public abstract boolean toggleBreakpt(String filename, int line);
    public abstract String getVariableAt(String line, int x);
    public abstract String evaluateVariable(String variable);
    public abstract void insertDeleteLines(String filename, int fileLineNo, int delta);
    // Insert or delete one or more lines. delta is positive for inserts,
    // negative for deletes. Calling this function allows the editor to
    // update breakpoints etc.

    // UI objects
    //public abstract TImageList* GetMainImages();
    //public abstract TImageList* GetGutterImages();

    // Syntax highlighting
    //public abstract SyntaxHighlightParams& Highlighting();
    //public abstract TLineAttr GetTextTokenStyle(String textToken);
    //public abstract TLineAttr GetSymbolStyle(String textToken);

    // Misc
    public abstract void jumpToFile(String filename);
    public abstract void refreshUI();

}
