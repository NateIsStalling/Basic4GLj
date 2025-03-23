package com.basic4gl.desktop.util;

import javax.swing.*;

public interface IFileManager {

// Child editor access
int editorCount();

JTextArea getEditor(int index);

String getFilename(int index);

String getCurrentDirectory();

String getAppDirectory();
}
