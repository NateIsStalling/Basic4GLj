package com.basic4gl.desktop.util;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;

import java.io.File;

/**
 * Created by Nate on 2/26/2015.
 */
public class EditorSourceFileServer implements ISourceFileServer {
    private IFileManager mFileManager;
    public EditorSourceFileServer(IFileManager fileManager){
        mFileManager = fileManager;
    }
    @Override
    public ISourceFile OpenSourceFile(String filename) {

        // Search for editor with matching filename.
        // Construct an EditorSourceFile object if found.
        for (int i = 0; i < mFileManager.editorCount(); i++) {
            if (new File(mFileManager.getFilename(i)).getAbsolutePath().equals(filename)) {
                return new EditorSourceFile(mFileManager.getEditor(i), filename);
            }
        }
        return null;
    }
}
