package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.content.FileViewer;
import com.basic4gl.desktop.spi.content.FileViewerMetadata;
import com.basic4gl.desktop.spi.content.FileViewerProvider;

/**
 * Provider for DefaultImageViewer
 */
public class DefaultImageViewerProvider implements FileViewerProvider {

    private static final FileViewerMetadata METADATA = new FileViewerMetadata(
            "Image Viewer",
            "1.0.0",
            "Default viewer for common image formats",
            new String[] {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp", ".tiff", ".tif", ".ico"},
            new String[] {
                "image/png", "image/jpeg", "image/gif", "image/bmp", "image/webp", "image/tiff", "image/x-icon"
            });

    @Override
    public FileViewer createViewer() {
        return new DefaultImageViewer();
    }

    @Override
    public FileViewerMetadata getMetadata() {
        return METADATA;
    }
}
