package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.content.FileViewer;
import com.basic4gl.desktop.spi.content.FileViewerMetadata;
import com.basic4gl.desktop.spi.content.FileViewerProvider;

public class MarkdownViewerProvider implements FileViewerProvider {

    private static final FileViewerMetadata METADATA = new FileViewerMetadata(
            "Markdown Viewer", "1.0.0", "Simple viewer for Markdown files", new String[] {".md"}, new String[] {
                "text/markdown"
            });

    @Override
    public FileViewer createViewer() {
        return new SimpleTextViewer();
    }

    @Override
    public FileViewerMetadata getMetadata() {
        return METADATA;
    }
}
