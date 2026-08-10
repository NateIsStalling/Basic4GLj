package com.basic4gl.compiler.util;

/**
 * Serves source files by filename
 * TODO: remove me. this sucks.
 */
public interface ISourceFileServer {
    /**
     * Open source file and return interface.
     */
    ISourceFile openSourceFile(String filename);
}
