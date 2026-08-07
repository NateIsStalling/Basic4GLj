package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.util.Collection;

public interface DocumentProvider {

    /**
     * Stable provider ID within the owning plugin.
     */
    String id();

    /**
     * Used for cache invalidation. Prefer a plugin or content version.
     */
    String version();

    Collection<DocumentDescriptor> getIndex();

    ContentDocument openDocument(String documentId) throws IOException;
}
