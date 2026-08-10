package com.basic4gl.desktop.spi.content;

import com.basic4gl.desktop.spi.PluginContext;
import java.nio.file.Path;
import javax.swing.JComponent;

/**
 * Main interface for file viewers
 *
 * Implementations should provide a UI component that displays a specific file type.
 * Viewers must handle their own resource cleanup.
 */
public interface FileViewer {

    /**
     * Load and display a file
     * @param path Path to the file to view
     * @throws FileViewerException if file cannot be loaded or displayed
     */
    void loadFile(PluginContext context, Path path) throws FileViewerException;

    /**
     * Get the Swing component that displays the file
     * @return JComponent to display in UI
     */
    JComponent getComponent();

    /**
     * Check if this viewer can handle the given file
     * @param filename Filename to check
     * @param mimeType Optional MIME type (may be null)
     * @return true if this viewer can display this file
     */
    boolean canHandle(String filename, String mimeType);

    /**
     * Get user-friendly name for this viewer
     * @return Viewer name (e.g. "Image Viewer", "Audio Player")
     */
    String getName();

    /**
     * Get version of this viewer (for compatibility checking)
     * @return Version string
     */
    String getVersion();

    /**
     * Clean up resources used by this viewer
     */
    void dispose();
}
