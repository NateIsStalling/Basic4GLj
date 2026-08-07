package com.basic4gl.desktop.spi.content;

/**
 * Exception thrown by file viewer operations
 */
public class FileViewerException extends Exception {
    public FileViewerException(String message) {
        super(message);
    }

    public FileViewerException(String message, Throwable cause) {
        super(message, cause);
    }
}
