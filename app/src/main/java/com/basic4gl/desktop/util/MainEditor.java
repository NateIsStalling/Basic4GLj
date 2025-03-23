package com.basic4gl.desktop.util;

import com.basic4gl.runtime.InstructionPosition;

/**
 * Created by Nate on 3/23/2015.
 */
public interface MainEditor {

  // Debugger and abstract machine state
  boolean isVMRunning();

  int getVMRow(String filename, InstructionPosition instructionPosition);

  int isBreakpt(String filename, int line);

  boolean toggleBreakpt(String filename, int line);

  String getVariableAt(String line, int x);

  String evaluateVariable(String variable);

  void insertDeleteLines(String filename, int fileLineNo, int delta);

  // Insert or delete one or more lines. delta is positive for inserts,
  // negative for deletes. Calling this function allows the editor to
  // update breakpoints etc.

  // UI objects
  // public abstract TImageList* GetMainImages();
  // public abstract TImageList* GetGutterImages();

  // Syntax highlighting
  // public abstract SyntaxHighlightParams& Highlighting();
  // public abstract TLineAttr GetTextTokenStyle(String textToken);
  // public abstract TLineAttr GetSymbolStyle(String textToken);

  // Misc
  void jumpToFile(String filename);

  void refreshUI();
}
