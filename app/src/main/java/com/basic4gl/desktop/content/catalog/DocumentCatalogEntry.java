package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.DocumentProvider;
import java.util.Objects;

public record DocumentCatalogEntry(
        ContentGlobalId globalId,
        String pluginDisplayName,
        String providerVersion,
        DocumentDescriptor descriptor,
        DocumentProvider provider) {

    public DocumentCatalogEntry {
        globalId = Objects.requireNonNull(globalId, "globalId");
        pluginDisplayName = pluginDisplayName == null ? "" : pluginDisplayName;
        providerVersion = providerVersion == null ? "" : providerVersion;
        descriptor = Objects.requireNonNull(descriptor, "descriptor");
        provider = Objects.requireNonNull(provider, "provider");
    }
}
