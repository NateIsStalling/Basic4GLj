package com.basic4gl.language.adapter.content;

import com.basic4gl.library.fileviewer.FileViewer;
import com.basic4gl.library.fileviewer.FileViewerMetadata;
import com.basic4gl.library.fileviewer.FileViewerProvider;

/**
 * Provider for DefaultAudioViewer
 */
public class DefaultAudioViewerProvider implements FileViewerProvider {

    private static final FileViewerMetadata METADATA = new FileViewerMetadata(
            "Audio Viewer",
            "1.0.0",
            "Default viewer for audio files (WAV, AU)",
            new String[] {".wav", ".au"},
            new String[] {"audio/wav", "audio/x-wav", "audio/basic"});

    @Override
    public FileViewer createViewer() {
        return new DefaultAudioViewer();
    }

    @Override
    public FileViewerMetadata getMetadata() {
        return METADATA;
    }
}
