package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileEditorActionListener;
import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.editor.IToggleBreakpointListener;
import com.basic4gl.desktop.util.IFileManager;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rtextarea.SearchContext;

/**
 * Text file viewer that wraps the existing FileEditor functionality.
 * This is the primary viewer for code files, text files, markdown, and any other text-based content.
 */
public class TextFileViewer implements IFileViewer {
    private final FileEditor editor;

    public TextFileViewer(FileEditor editor) {
        this.editor = editor;
    }

    /**
     * Creates a new text file viewer for an unsaved file.
     */
    public TextFileViewer(
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {
        this.editor =
                new FileEditor(actionListener, fileManager, toggleBreakpointListener, linkGenerator, searchContext);
    }

    /**
     * Creates a new text file viewer and loads the specified file.
     */
    public TextFileViewer(
            File file,
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {
        this.editor =
                new FileEditor(actionListener, fileManager, toggleBreakpointListener, linkGenerator, searchContext);

        if (file != null && file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                editor.filePath = file.getAbsolutePath();
                editor.fileName = file.getName();
                editor.editorPane.read(fr, null);
                fr.close();
                editor.isSaved = true;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null, "Could not read file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
            editor.editorPane.discardAllEdits(); // Otherwise 'undo' will clear the text area after loading
        } else if (file != null) {
            editor.filePath = file.getAbsolutePath();
            editor.fileName = file.getName();
            editor.isSaved = false;
        }
    }

    /**
     * Gets the underlying FileEditor instance for compatibility with existing code
     */
    public FileEditor getFileEditor() {
        return editor;
    }

    @Override
    public String getTitle() {
        return editor.getTitle();
    }

    @Override
    public String getFilePath() {
        return editor.getFilePath();
    }

    @Override
    public JComponent getContentPane() {
        return editor.getContentPane();
    }

    @Override
    public File getFile() {
        return editor.getFile();
    }

    @Override
    public String getShortFilename() {
        return editor.getShortFilename();
    }

    @Override
    public boolean isModified() {
        return editor.isModified();
    }

    @Override
    public void setModified() {
        editor.setModified();
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.TEXT_EDITOR;
    }

    @Override
    public boolean hasPreview() {
        return false;
    }

    @Override
    public void setViewMode(ViewMode viewMode) {}

    @Override
    public ViewMode getViewMode() {
        return ViewMode.DEFAULT;
    }
}
