package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.IndexedContent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ContentSearchIndex {

    public List<ContentSearchResult> search(
            List<DocumentCatalogEntry> documents, List<TemplateCatalogEntry> templates, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ContentSearchResult> results = new ArrayList<>();
        for (DocumentCatalogEntry entry : documents) {
            int score = score(entry.descriptor(), entry.pluginDisplayName(), normalizedQuery);
            if (score > 0) {
                results.add(new ContentSearchResult(entry.globalId().value(), entry.descriptor(), false, score));
            }
        }
        for (TemplateCatalogEntry entry : templates) {
            int score = score(entry.descriptor(), entry.pluginDisplayName(), normalizedQuery);
            if (score > 0) {
                results.add(new ContentSearchResult(entry.globalId().value(), entry.descriptor(), true, score));
            }
        }
        results.sort(Comparator.comparingInt(ContentSearchResult::score)
                .reversed()
                .thenComparing(result -> result.content().sortOrder())
                .thenComparing(result -> result.content().title(), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(results);
    }

    private int score(IndexedContent content, String pluginDisplayName, String query) {
        if (query.isBlank()) {
            return 1;
        }
        String title = content.title().toLowerCase(Locale.ROOT);
        if (title.equals(query)) {
            return 100;
        }
        if (title.startsWith(query)) {
            return 80;
        }
        if (title.contains(query)) {
            return 60;
        }
        if (content.tags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).equals(query))) {
            return 50;
        }
        if (content.description().toLowerCase(Locale.ROOT).contains(query)) {
            return 30;
        }
        if (content.categoryPath().stream()
                .anyMatch(category -> category.toLowerCase(Locale.ROOT).contains(query))) {
            return 25;
        }
        if (pluginDisplayName.toLowerCase(Locale.ROOT).contains(query)) {
            return 10;
        }
        return 0;
    }
}
