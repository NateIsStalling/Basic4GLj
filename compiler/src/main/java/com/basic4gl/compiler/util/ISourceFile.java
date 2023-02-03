package com.basic4gl.compiler.util;

/**
 * ISourceFile
 *
 * Interface to a source file.
 */
public interface ISourceFile {

	/**
	 * @return The next line of source code
	 */
	public abstract String getNextLine();

	/**
	 * @return The filename
	 */
	public abstract String getFilename();

	/**
	 * @return Return the line number. 0 = Top line of file.
	 */
	public abstract int getLineNumber();

	/**
	 * @return True if reached End of File
	 */
	public abstract boolean isEof();

	/**
	 * Called when preprocessor is finished with the source file
	 */
	public abstract void release();
}