package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface ContentSource {

    /**
     * Opens a normalized path relative to this source's root.
     */
    InputStream open(String normalizedPath) throws IOException;

    /**
     * Returns all files belonging to this content bundle.
     */
    Collection<ContentResource> resources() throws IOException;
}
