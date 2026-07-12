package com.basic4gl.desktop.spi.content;

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
