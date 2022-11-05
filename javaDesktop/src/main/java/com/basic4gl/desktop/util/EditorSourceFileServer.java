package com.basic4gl.desktop.util;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;
import com.basic4gl.desktop.MainWindow;

import java.io.File;

/**
 * Created by Nate on 2/26/2015.
 */
public class EditorSourceFileServer implements ISourceFileServer {
    private MainEditor mEditor;
    public EditorSourceFileServer(MainEditor editor){
        mEditor = editor;
    }
    @Override
    public ISourceFile OpenSourceFile(String filename) {

        // Search for editor with matching filename.
        // Construct an EditorSourceFile object if found.
        for (int i = 0; i < mEditor.editorCount(); i++)
            if (new File(mEditor.getFilename(i)).getAbsolutePath().equals(filename))
                return new EditorSourceFile(mEditor.getEditor(i), filename);
        return null;
    }
}
