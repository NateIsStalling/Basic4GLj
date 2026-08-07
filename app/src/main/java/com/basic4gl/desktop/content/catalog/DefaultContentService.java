package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.ContentRegistration;
import com.basic4gl.desktop.spi.content.ContentService;
import com.basic4gl.desktop.spi.content.DocumentProvider;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.util.Objects;

public final class DefaultContentService implements ContentService {

    private final ContentCatalog catalog;
    private final String pluginId;
    private final String pluginDisplayName;

    public DefaultContentService(ContentCatalog catalog, String pluginId, String pluginDisplayName) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.pluginId = requireNonBlank(pluginId, "pluginId");
        this.pluginDisplayName = pluginDisplayName == null ? "" : pluginDisplayName;
    }

    @Override
    public ContentRegistration registerDocumentProvider(DocumentProvider provider) {
        return catalog.registerDocumentProvider(pluginId, pluginDisplayName, provider);
    }

    @Override
    public ContentRegistration registerTemplateProvider(TemplateProvider provider) {
        return catalog.registerTemplateProvider(pluginId, pluginDisplayName, provider);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }
}
