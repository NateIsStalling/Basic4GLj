package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.IndexedContent;
import java.util.Locale;
import java.util.Set;

public record ContentScope(String tag, String displayName) {

    public static final ContentScope ALL = new ContentScope(null, "All Documentation");

    public ContentScope {
        tag = tag == null || tag.isBlank() ? null : tag.trim().toLowerCase(Locale.ROOT);
        displayName = displayName == null || displayName.isBlank() ? labelForTag(tag) : displayName.trim();
    }

    public static ContentScope forTag(String tag) {
        return new ContentScope(tag, labelForTag(tag));
    }

    public boolean all() {
        return tag == null;
    }

    public boolean matches(IndexedContent content) {
        return all() || hasTag(content.tags());
    }

    public boolean hasTag(Set<String> tags) {
        return tag != null && tags.stream().anyMatch(value -> tag.equals(value.toLowerCase(Locale.ROOT)));
    }

    public static String labelForTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return "";
        }
        String[] words = tag.trim().replace('_', '-').split("-+");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                label.append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return label.toString();
    }
}
