package com.basic4gl.desktop.spi.content;

import java.util.List;
import java.util.Set;

final class ContentValidation {

    private ContentValidation() {}

    static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    static String optionalString(String value) {
        return value == null ? "" : value;
    }

    static List<String> copyList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values);
    }

    static Set<String> copySet(Set<String> values) {
        if (values == null) {
            return Set.of();
        }
        return Set.copyOf(values);
    }
}
