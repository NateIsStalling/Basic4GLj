package com.basic4gl.compiler.util;

/**
 * A null object implementation of ISourceFile, used when source file reading is
 * disabled or unavailable.
 * It simulates reading a single content string as a line.
 */
public final class NullSourceFile implements ISourceFile {

	private final String content;
	private boolean isConsumed = false;

	/**
	 * Initialize the NullSourceFile with a single fixed string as the content.
	 * 
	 * @param content The dummy content for the "file".
	 */
	public NullSourceFile(String content) {
		this.content = content;
	}

	/**
	 * Simulate reading the single stored line of content.
	 * 
	 * @return The stored content, or null if already consumed.
	 */
	@Override
	public String getNextLine() {
		if (!isConsumed) {
			isConsumed = true;
			return content;
		}
		return null;
	}

	/**
	 * @return A dummy filename.
	 */
	@Override
	public String getFilename() {
		return null;
	}

	/**
	 * @return Line number, hardcoded for simplicity.
	 */
	@Override
	public int getLineNumber() {
		return 0;
	}

	/**
	 * @return True immediately after the first line is returned.
	 */
	@Override
	public boolean isEof() {
		return isConsumed;
	}

	/**
	 * No-operation for cleanup.
	 */
	@Override
	public void release() {
	}
}
