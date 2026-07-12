package com.basic4gl.desktop.editor;

import java.io.File;
import javax.swing.*;

/**
 * Base interface for file viewers. All viewers (text editors, image viewers, audio players, hex viewers, etc.)
 * implement this interface.
 *
 * This allows the IDE to support different types of files with different viewing/editing mechanisms while maintaining
 * a consistent interface for tab management.
 */
public interface IFileViewer {
    /**
     * @return the title to display in the tab
     */
    String getTitle();

    /**
     * @return the full file path or empty string if unsaved
     */
    String getFilePath();

    /**
     * @return the JComponent to display in the tab
     */
    JComponent getContentPane();

    /**
     * @return the File object associated with this viewer, or null if unsaved
     */
    File getFile();

    /**
     * @return the short filename (just the name, not the full path)
     */
    String getShortFilename();

    /**
     * @return true if the file has been modified since opening/saving
     */
    boolean isModified();

    /**
     * Mark this viewer's content as modified
     */
    void setModified();

    /**
     * @return the viewer type (for identifying which viewer is being used)
     */
    ViewerType getViewerType();

    /**
     * Enumeration of supported viewer types
     */
    enum ViewerType {
        TEXT_EDITOR("Text Editor"),
        IMAGE_VIEWER("Image Viewer"),
        AUDIO_VIEWER("Audio Viewer"),
        HEX_VIEWER("Hex Editor"),
        MARKDOWN_VIEWER("Markdown Viewer");

        public final String display;

        ViewerType(String display) {
            this.display = display;
        }
    }
}
