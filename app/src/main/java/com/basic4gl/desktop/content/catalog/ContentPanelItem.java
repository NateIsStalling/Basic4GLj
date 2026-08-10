package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.IndexedContent;
import java.util.List;
import java.util.Set;

public record ContentPanelItem(
        String globalId,
        String title,
        String subtitle,
        String description,
        List<String> categoryPath,
        Set<String> tags,
        boolean template,
        IndexedContent content) {

    public String displayName() {
        return title == null ? "" : title;
    }

    public String displaySubtitle() {
        return subtitle == null ? "" : subtitle;
    }

    @Override
    public String toString() {
        return displayName();
    }
}
