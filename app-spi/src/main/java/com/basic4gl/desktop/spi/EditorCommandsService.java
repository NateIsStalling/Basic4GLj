package com.basic4gl.desktop.spi;

import java.io.File;

public interface EditorCommandsService {
    void openFileWithPreferredViewer(File file);
    // TODO this should be cleaned up before 1.0; refactoring
    void openMarkdownInDocsTab(File file);
    public String collectAllSourceText();
    void actionOpenFolder();
    void selectNextBookmark();
    void selectPreviousBookmark();
    void toggleBookmark();

    void setWorkspaceDirectory(File selectedFile);

    void insertText(String text, int caretOffset);
}
