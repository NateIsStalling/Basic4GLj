package com.basic4gl.desktop.spi;

import java.io.File;
import java.util.List;

public interface EditorCommandsService {
    void openFileWithPreferredViewer(File file);

    public String collectAllSourceText();

    void actionOpenFolder();

    void selectNextBookmark();

    void selectPreviousBookmark();

    void toggleBookmark();

    List<BookmarkInfo> listBookmarks();

    void goToBookmark(String filePath, int lineNumber);

    void setWorkspaceDirectory(File selectedFile);

    void insertText(String text, int caretOffset);
}
