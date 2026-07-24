package com.basic4gl.language.adapter;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;
import com.basic4gl.desktop.spi.SourceFileService;

public class FileServiceAdapter implements ISourceFileServer {
    private final SourceFileService service;

    public FileServiceAdapter(SourceFileService service) {
        this.service = service;
    }

    @Override
    public ISourceFile openSourceFile(String s) {
        com.basic4gl.desktop.spi.ISourceFile sourceFile = service.openSourceFile(s);
        if (sourceFile == null) {
            return null;
        }
        return new SourceFile(sourceFile);
    }

    static class SourceFile implements ISourceFile {
        private final com.basic4gl.desktop.spi.ISourceFile sourceFile;

        public SourceFile(com.basic4gl.desktop.spi.ISourceFile sourceFile) {
            this.sourceFile = sourceFile;
        }

        @Override
        public String getNextLine() {
            return sourceFile.getNextLine();
        }

        @Override
        public String getFilename() {
            return sourceFile.getFilename();
        }

        @Override
        public int getLineNumber() {
            return sourceFile.getLineNumber();
        }

        @Override
        public boolean isEof() {
            return sourceFile.isEof();
        }

        @Override
        public void release() {
            sourceFile.release();
        }
    }
}
