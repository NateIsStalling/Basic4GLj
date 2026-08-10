package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.content.FileViewerException;
import java.nio.file.Path;

/**
 * Convenience facade for using the file viewer system
 *
 * This class provides a simple API for the most common use cases:
 * - Load and display a file
 * - Check if a file type is supported
 * - List available viewers
 */
public class FileViewerManager {

    private static final FileViewerRegistry registry = new FileViewerRegistry();
    private static boolean initialized = false;

    private FileViewerManager() {
        // Static utility class
    }

    /**
     * Initialize the file viewer system (must be called once before use)
     */
    public static void initialize() {
        if (!initialized) {
            registry.initialize();
            initialized = true;
        }
    }

    /**
     * Load a file with an appropriate viewer
     *
     * Example usage:
     *   try {
     *       FileViewerManager.initialize();
     *       FileViewerResult result = FileViewerManager.loadFile(Paths.get("image.png"));
     *       if (result.isSuccess()) {
     *           JFrame frame = new JFrame("File Viewer");
     *           frame.add(result.getViewer().getComponent());
     *           frame.setSize(800, 600);
     *           frame.setVisible(true);
     *       } else {
     *           System.err.println("Error: " + result.getError());
     *       }
     *   } catch (Exception e) {
     *       e.printStackTrace();
     *   }
     *
     * @param filepath Path to file to view
     * @return FileViewerResult containing viewer or error details
     */
    public static FileViewerRegistry.FileViewerResult loadFile(PluginContext context, Path filepath) {
        if (!initialized) {
            initialize();
        }

        FileViewerRegistry.FileViewerResult result = registry.findViewer(filepath);
        if (result.isSuccess()) {
            try {
                result.getViewer().loadFile(context, filepath);
            } catch (FileViewerException e) {
                return new FileViewerRegistry.FileViewerResult(null, null, e.getMessage());
            }
        }
        return result;
    }

    /**
     * Check if a file type is supported by any registered viewer
     */
    public static boolean isSupported(String filename) {
        if (!initialized) {
            initialize();
        }
        for (com.basic4gl.desktop.spi.content.FileViewerMetadata viewer : registry.getAvailableViewers()) {
            String ext = getFileExtension(filename);
            if (ext != null && viewer.supportsExtension(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all registered viewers
     */
    public static java.util.List<com.basic4gl.desktop.spi.content.FileViewerMetadata> getAvailableViewers() {
        if (!initialized) {
            initialize();
        }
        return registry.getAvailableViewers();
    }

    /**
     * Get last error message
     */
    public static String getLastError() {
        return registry.getLastError();
    }

    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return null;
    }
}
