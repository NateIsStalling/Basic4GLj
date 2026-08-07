package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public final class ManifestDocumentProvider implements DocumentProvider {

    private final String id;
    private final String version;
    private final ContentSource source;
    private final List<DocumentDescriptor> descriptors;

    public ManifestDocumentProvider(
            String id, String version, ContentSource source, Collection<DocumentDescriptor> descriptors) {
        this.id = ContentValidation.requireNonBlank(id, "id");
        this.version = ContentValidation.requireNonBlank(version, "version");
        this.source = source;
        this.descriptors = List.copyOf(descriptors == null ? List.of() : descriptors);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public Collection<DocumentDescriptor> getIndex() {
        return descriptors;
    }

    @Override
    public ContentDocument openDocument(String documentId) throws IOException {
        for (DocumentDescriptor descriptor : descriptors) {
            if (descriptor.id().equals(documentId)) {
                return new ContentDocument(mediaType(descriptor.id()), source, descriptor.id());
            }
        }
        throw new IOException("Unknown document: " + documentId);
    }

    private static String mediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return "text/html";
        }
        if (lower.endsWith(".txt")) {
            return "text/plain";
        }
        return "text/markdown";
    }
}
