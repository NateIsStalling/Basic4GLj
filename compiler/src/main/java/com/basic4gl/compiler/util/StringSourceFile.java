package com.basic4gl.compiler.util;

/**
 * A null object implementation of ISourceFile, used when source file reading is
 * disabled or unavailable.
 * It simulates reading a single content string as a line.
 */
public final class StringSourceFile implements ISourceFile {

	private final String[] lines;
	private int currentLine;

	/**
	 * Initialize the NullSourceFile with a single fixed string as the content.
	 * 
	 * @param content The dummy content for the "file".
	 */
	public StringSourceFile(String content) {
		this.lines = content.split("\\r?\\n");
	}

	/**
	 * Simulate reading the single stored line of content.
	 * 
	 * @return The stored content, or null if already consumed.
	 */
	@Override
	public String getNextLine() {
		String line = lines[currentLine];
		currentLine++;
		return line;
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
		return currentLine;
	}

	/**
	 * @return True immediately after the first line is returned.
	 */
	@Override
	public boolean isEof() {
		return currentLine >= lines.length;
	}

	/**
	 * No-operation for cleanup.
	 */
	@Override
	public void release() {
	}
}
