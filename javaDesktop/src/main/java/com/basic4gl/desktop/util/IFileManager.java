package com.basic4gl.desktop.util;

import javax.swing.*;

public interface IFileManager {

    // Child editor access
    public abstract int editorCount();
    public abstract JTextArea getEditor(int index);
    public abstract String getFilename(int index);
    public abstract String getCurrentDirectory();

    String getAppDirectory();
}
