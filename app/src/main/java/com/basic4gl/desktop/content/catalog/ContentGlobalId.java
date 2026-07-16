package com.basic4gl.desktop.content.catalog;

import java.util.Objects;

public record ContentGlobalId(String pluginId, String providerId, String itemId) {

    public ContentGlobalId {
        pluginId = requireNonBlank(pluginId, "pluginId");
        providerId = requireNonBlank(providerId, "providerId");
        itemId = requireNonBlank(itemId, "itemId");
    }

    public String value() {
        return pluginId + ":" + providerId + ":" + itemId;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
