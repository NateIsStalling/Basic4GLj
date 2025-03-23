package com.basic4gl.lib.util;

import com.basic4gl.runtime.util.PointerResourceStore;

public class FileStreamResourceStore extends PointerResourceStore<FileStream> {
  @Override
  public void free(int index) {
    closeAtIndex(index);
    super.free(index);
  }

  @Override
  public void deleteElement(int index) {
    closeAtIndex(index);
    super.deleteElement(index);
  }

  @Override
  public void setValue(int index, FileStream value) {
    closeAtIndex(index);
    super.setValue(index, value);
  }

  @Override
  public void remove(int index) {
    closeAtIndex(index);
    super.remove(index);
  }

  private void closeAtIndex(int index) {
    FileStream fileStream = getValueAt(index);
    if (fileStream != null) {
      fileStream.close();
    }
  }
}
