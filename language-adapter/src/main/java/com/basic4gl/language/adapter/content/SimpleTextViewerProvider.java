package com.basic4gl.language.adapter.content;

import com.basic4gl.library.fileviewer.FileViewer;
import com.basic4gl.library.fileviewer.FileViewerMetadata;
import com.basic4gl.library.fileviewer.FileViewerProvider;

/**
 * Provider for SimpleTextViewer
 */
public class SimpleTextViewerProvider implements FileViewerProvider {

    private static final FileViewerMetadata METADATA = new FileViewerMetadata(
            "Text Viewer",
            "1.0.0",
            "Simple viewer for text-based files",
            new String[] {
                ".txt", ".json", ".xml", ".html", ".htm", ".css", ".java", ".cpp", ".c", ".h", ".py", ".js", ".md",
                ".log"
            },
            new String[] {
                "text/plain", "text/html", "text/css", "application/json", "application/xml", "text/x-java-source"
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
