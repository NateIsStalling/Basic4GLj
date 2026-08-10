package com.basic4gl.library.desktopgl;

import com.basic4gl.language.core.extensions.standard.IB4GLFileAccessor;
import com.basic4gl.library.desktopgl.content.FileOpener;

public class FileAccessorAdapter implements IB4GLFileAccessor {

    private final FileOpener files;

    public FileAccessorAdapter(FileOpener files) {
        this.files = files;
    }

    @Override
    public String getFilenameForRead(String filename) {
        return files.getFilenameForRead(filename, false);
    }
}
