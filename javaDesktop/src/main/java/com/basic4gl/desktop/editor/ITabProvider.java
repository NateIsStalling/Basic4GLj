package com.basic4gl.desktop.editor;

import java.io.File;

public interface ITabProvider {
    int getFileTabIndex(String filename);
    int getTabIndex(String filePath);
    void setSelectedTabIndex(int index);
    void openTab(String filename);
}
