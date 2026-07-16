package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.util.Collection;

public interface TemplateProvider {

    /**
     * Stable provider ID within the owning plugin.
     */
    String id();

    /**
     * Used for cache invalidation. Prefer a plugin or content version.
     */
    String version();

    Collection<TemplateDescriptor> getIndex();

    void instantiate(String templateId, TemplateCreationRequest request) throws IOException;
}
