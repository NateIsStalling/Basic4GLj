package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.content.FileViewer;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for discovering and managing file viewers
 *
 * Handles ServiceLoader-based discovery of FileViewerProviders and
 * maintains a registry of available viewers.
 */
public class FileViewerRegistry {

    private final Map<String, FileViewerProvider> providers = new HashMap<>();
    private String lastError = "";

    /**
     * Initialize the registry and discover all available viewers
     */
    public void initialize() {
        providers.clear();
        lastError = "";

        try {
            ServiceLoader<FileViewerProvider> loader = ServiceLoader.load(FileViewerProvider.class);

            for (FileViewerProvider provider : loader) {
                com.basic4gl.desktop.spi.content.FileViewerMetadata metadata = provider.getMetadata();
                if (metadata != null && metadata.getName() != null) {
                    providers.put(metadata.getName(), provider);
                }
            }
        } catch (Exception e) {
            lastError = "Failed to discover file viewers: " + e.getMessage();
        }
    }

    /**
     * Find a suitable viewer for the given file
     * @param filepath Path to the file
     * @return FileViewerResult with viewer instance or error details
     */
    public FileViewerResult findViewer(Path filepath) {
        String filename = filepath.getFileName().toString();
        String extension = getFileExtension(filename);
        String mimeType = guessMimeType(filename);

        for (FileViewerProvider provider : providers.values()) {
            com.basic4gl.desktop.spi.content.FileViewerMetadata metadata = provider.getMetadata();
            if (metadata != null) {
                if ((extension != null && metadata.supportsExtension(extension))
                        || (mimeType != null && metadata.supportsMimeType(mimeType))) {
                    try {
                        FileViewer viewer = provider.createViewer();
                        return new FileViewerResult(viewer, metadata);
                    } catch (Exception e) {
                        lastError = "Failed to create viewer " + metadata.getName() + ": " + e.getMessage();
                        return new FileViewerResult(null, null, lastError);
                    }
                }
            }
        }

        lastError = "No viewer found for: " + filename;
        return new FileViewerResult(null, null, lastError);
    }

    /**
     * Get all registered viewers
     */
    public List<com.basic4gl.desktop.spi.content.FileViewerMetadata> getAvailableViewers() {
        return providers.values().stream()
                .map(FileViewerProvider::getMetadata)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get error message from last operation
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return null;
    }

    /**
     * Guess MIME type based on file extension (sane defaults)
     */
    private String guessMimeType(String filename) {
        String ext = getFileExtension(filename);
        if (ext == null) {
            return null;
        }

        return switch (ext.toLowerCase()) {
                // Images
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".bmp" -> "image/bmp";
            case ".webp" -> "image/webp";
            case ".svg" -> "image/svg+xml";
            case ".tiff", ".tif" -> "image/tiff";
            case ".ico" -> "image/x-icon";
            case ".pcx" -> "image/x-pcx";

                // Audio
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".flac" -> "audio/flac";
            case ".ogg", ".oga" -> "audio/ogg";
            case ".m4a" -> "audio/mp4";
            case ".aac" -> "audio/aac";
            case ".wma" -> "audio/x-ms-wma";
            case ".aiff", ".aif" -> "audio/aiff";

                // Video
            case ".mp4" -> "video/mp4";
            case ".webm" -> "video/webm";
            case ".mkv" -> "video/x-matroska";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            case ".flv" -> "video/x-flv";

                // Text
            case ".txt" -> "text/plain";
            case ".json" -> "application/json";
            case ".xml" -> "application/xml";
            case ".html", ".htm" -> "text/html";
            case ".css" -> "text/css";

            default -> null;
        };
    }

    /**
     * Result of finding a viewer
     */
    public static class FileViewerResult {
        private final FileViewer viewer;
        private final com.basic4gl.desktop.spi.content.FileViewerMetadata metadata;
        private final String error;

        public FileViewerResult(FileViewer viewer, com.basic4gl.desktop.spi.content.FileViewerMetadata metadata) {
            this(viewer, metadata, null);
        }

        public FileViewerResult(FileViewer viewer, com.basic4gl.desktop.spi.content.FileViewerMetadata metadata, String error) {
            this.viewer = viewer;
            this.metadata = metadata;
            this.error = error;
        }

        public FileViewer getViewer() {
            return viewer;
        }

        public com.basic4gl.desktop.spi.content.FileViewerMetadata getMetadata() {
            return metadata;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return viewer != null;
        }
    }
}
