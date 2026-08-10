package com.basic4gl.desktop.spi;

/**
 * Serves source files by filename
 */
public interface SourceFileService {
    /**
     * Open source file and return interface.
     */
    ISourceFile openSourceFile(String filename);
}
