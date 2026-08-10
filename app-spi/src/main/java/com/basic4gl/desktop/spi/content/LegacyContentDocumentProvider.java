package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Deprecated
public final class LegacyContentDocumentProvider implements DocumentProvider {

    private final Content content;
    private final DocumentDescriptor descriptor;

    public LegacyContentDocumentProvider(Content content) {
        this.content = content;
        this.descriptor = new DocumentDescriptor(
                content.getFile().getName(),
                content.getName(),
                content.getMetadata().getDescription(),
                List.of(
                        content.getMetadata().getCategory() == null
                                ? "Legacy"
                                : content.getMetadata().getCategory()),
                Set.of(
                        content.getMetadata().getTags() == null
                                ? new String[0]
                                : content.getMetadata().getTags()),
                0,
                List.of());
    }

    @Override
    public String id() {
        return "legacy-document-" + descriptor.id();
    }

    @Override
    public String version() {
        return content.getMetadata().getVersion() == null
                ? "legacy"
                : content.getMetadata().getVersion();
    }

    @Override
    public Collection<DocumentDescriptor> getIndex() {
        return List.of(descriptor);
    }

    @Override
    public ContentDocument openDocument(String documentId) throws IOException {
        if (!descriptor.id().equals(documentId)) {
            throw new IOException("Unknown legacy document: " + documentId);
        }
        return new ContentDocument(
                "text/plain",
                new DirectoryContentSource(content.getFile().getParentFile().toPath()),
                content.getFile().getName());
    }
}
