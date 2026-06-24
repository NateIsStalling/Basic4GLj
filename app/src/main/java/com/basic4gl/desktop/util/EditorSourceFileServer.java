package com.basic4gl.desktop.util;

import com.basic4gl.desktop.spi.ISourceFile;
import com.basic4gl.desktop.spi.SourceFileService;

import java.io.File;

/**
 * Created by Nate on 2/26/2015.
 */
public class EditorSourceFileServer implements SourceFileService {
    private final IFileManager fileManager;

    public EditorSourceFileServer(IFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public ISourceFile openSourceFile(String filename) {

        // Search for editor with matching filename.
        // Construct an EditorSourceFile object if found.
        for (int i = 0; i < fileManager.editorCount(); i++) {
            if (new File(fileManager.getFilename(i)).getAbsolutePath().equals(filename)) {
                return new EditorSourceFile(fileManager.getEditor(i), filename);
            }
        }
        return null;
    }
}
