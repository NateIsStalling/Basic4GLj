package com.basic4gl.compiler.util;

/**
 * Serves source files by filename
 */
public interface ISourceFileServer {
	/**
	 * Open source file and return interface.
	 */
	public abstract ISourceFile openSourceFile(String filename);
}