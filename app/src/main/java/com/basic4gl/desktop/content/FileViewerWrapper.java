package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileViewer;
import java.io.File;

/**
 * Wrapper class that bridges IFileViewer with FileEditor for backward compatibility.
 * This allows the IDE to store and manage different viewer types while maintaining
 * compatibility with existing FileEditor-based code.
 */
public class FileViewerWrapper {
    private final IFileViewer viewer;
    private final FileEditor textEditor; // Only non-null if viewer is TextFileViewer

    public FileViewerWrapper(IFileViewer viewer) {
        this.viewer = viewer;
        if (viewer instanceof TextFileViewer textFileViewer) {
            this.textEditor = textFileViewer.getFileEditor();
        } else if (viewer instanceof HtmlViewer htmlViewer) {
            this.textEditor = htmlViewer.getFileEditor();
        } else {
            this.textEditor = null;
        }
    }

    public FileViewerWrapper(FileEditor editor) {
        this(new TextFileViewer(editor));
    }

    /**
     * Gets the viewer interface for general file operations
     */
    public IFileViewer getViewer() {
        return viewer;
    }

    /**
     * Gets the FileEditor if this is a text viewer, otherwise null
     * For backward compatibility with code expecting FileEditor
     */
    public FileEditor getFileEditor() {
        return textEditor;
    }

    /**
     * Returns true if this wrapper contains a text editor
     */
    public boolean isTextEditor() {
        return textEditor != null;
    }

    /**
     * Gets the viewer type
     */
    public IFileViewer.ViewerType getViewerType() {
        return viewer.getViewerType();
    }

    /**
     * Gets the title
     */
    public String getTitle() {
        return viewer.getTitle();
    }

    /**
     * Gets the file path
     */
    public String getFilePath() {
        return viewer.getFilePath();
    }

    /**
     * Gets the File object
     */
    public File getFile() {
        return viewer.getFile();
    }

    /**
     * Gets the short filename
     */
    public String getShortFilename() {
        return viewer.getShortFilename();
    }

    /**
     * Gets the content pane
     */
    public javax.swing.JComponent getContentPane() {
        return viewer.getContentPane();
    }

    /**
     * Checks if modified
     */
    public boolean isModified() {
        return viewer.isModified();
    }

    /**
     * Sets modified state
     */
    public void setModified() {
        viewer.setModified();
    }
}
