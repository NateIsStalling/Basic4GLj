package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.util.Objects;

public record TemplateCatalogEntry(
        ContentGlobalId globalId,
        String pluginDisplayName,
        String providerVersion,
        TemplateDescriptor descriptor,
        TemplateProvider provider) {

    public TemplateCatalogEntry {
        globalId = Objects.requireNonNull(globalId, "globalId");
        pluginDisplayName = pluginDisplayName == null ? "" : pluginDisplayName;
        providerVersion = providerVersion == null ? "" : providerVersion;
        descriptor = Objects.requireNonNull(descriptor, "descriptor");
        provider = Objects.requireNonNull(provider, "provider");
    }
}
