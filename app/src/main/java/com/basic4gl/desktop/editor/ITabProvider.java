package com.basic4gl.desktop.editor;

public interface ITabProvider {
  int getFileTabIndex(String filename);

  int getTabIndex(String filePath);

  void setSelectedTabIndex(int index);

  void openTab(String filename);
}
