package com.basic4gl.desktop.spi.content;


/**
 * Provider interface for creating FileViewer instances
 *
 * Similar to the Basic4GLPluginProvider pattern, this allows discovery via ServiceLoader.
 * Implement this interface and register in META-INF/services to make viewers discoverable.
 */
public interface FileViewerProvider {

    /**
     * Create a new FileViewer instance
     * @return New FileViewer instance
     */
    FileViewer createViewer();

    /**
     * Get metadata about this viewer provider
     * @return Viewer metadata (name, version, supported file types)
     */
    com.basic4gl.desktop.spi.content.FileViewerMetadata getMetadata();
}

