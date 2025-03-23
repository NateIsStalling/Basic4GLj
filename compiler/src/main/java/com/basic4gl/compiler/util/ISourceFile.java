package com.basic4gl.compiler.util;

/**
 * Interface to a source file.
 */
public interface ISourceFile {

  /**
   * @return The next line of source code
   */
  String getNextLine();

  /**
   * @return The filename
   */
  String getFilename();

  /**
   * @return Return the line number. 0 = Top line of file.
   */
  int getLineNumber();

  /**
   * @return True if reached End of File
   */
  boolean isEof();

  /**
   * Called when preprocessor is finished with the source file
   */
  void release();
}
