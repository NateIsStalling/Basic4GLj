package com.basic4gl.desktop.spi.content;

import java.util.Objects;

public record ContentDocument(String mediaType, ContentSource source, String entryPath) {

    public ContentDocument {
        mediaType = ContentValidation.requireNonBlank(mediaType, "mediaType");
        source = Objects.requireNonNull(source, "source");
        entryPath = ContentValidation.requireNonBlank(entryPath, "entryPath");
    }
}
