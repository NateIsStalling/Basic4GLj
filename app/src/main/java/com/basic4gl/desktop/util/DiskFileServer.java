package com.basic4gl.desktop.util;

import com.basic4gl.desktop.spi.ISourceFile;
import com.basic4gl.desktop.spi.SourceFileService;

/**
 * Disk file implementation of ISourceFileServer
 */
public class DiskFileServer implements SourceFileService {
    // ISourceFileServer methods
    @Override
    public ISourceFile openSourceFile(String filename) {
        DiskFile file = new DiskFile(filename);
        if (file.hasError()) {
            file = null;
            return null;
        }
        return file;
    }
}
