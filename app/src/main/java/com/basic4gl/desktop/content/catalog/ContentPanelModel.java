package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.IndexedContent;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public final class ContentPanelModel {

    private final ContentSearchIndex searchIndex = new ContentSearchIndex();

    public List<ContentScope> scopes(ContentCatalog catalog) {
        Set<String> tags = new LinkedHashSet<>();
        catalog.documents().stream()
                .flatMap(entry -> entry.descriptor().tags().stream())
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .forEach(tags::add);
        catalog.templates().stream()
                .flatMap(entry -> entry.descriptor().tags().stream())
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .forEach(tags::add);

        List<ContentScope> scopes = new ArrayList<>();
        scopes.add(ContentScope.ALL);
        tags.stream()
                .map(ContentScope::forTag)
                .sorted(Comparator.comparing(ContentScope::displayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(scopes::add);
        return List.copyOf(scopes);
    }

    public List<ContentPanelItem> items(ContentCatalog catalog, ContentScope scope, String query) {
        List<DocumentCatalogEntry> documents = filteredDocuments(catalog.documents(), scope);
        List<TemplateCatalogEntry> templates = filteredTemplates(catalog.templates(), scope);
        if (query != null && !query.isBlank()) {
            return searchIndex.search(documents, templates, query).stream()
                    .map(this::toItem)
                    .toList();
        }

        List<ContentPanelItem> items = new ArrayList<>();
        documents.stream().map(this::toItem).forEach(items::add);
        templates.stream().map(this::toItem).forEach(items::add);
        items.sort((left, right) -> {
            int sort =
                    Integer.compare(left.content().sortOrder(), right.content().sortOrder());
            if (sort != 0) {
                return sort;
            }
            return left.title().compareToIgnoreCase(right.title());
        });
        return List.copyOf(items);
    }

    public ContentBrowseNode browse(ContentCatalog catalog, ContentScope scope) {
        return browse(catalog, scope, ContentPanelItem::categoryPath);
    }

    public ContentBrowseNode browse(
            ContentCatalog catalog, ContentScope scope, Function<ContentPanelItem, List<String>> categoryPathProvider) {
        ContentBrowseNode root = new ContentBrowseNode(scope.displayName());
        for (ContentPanelItem item : items(catalog, scope, "")) {
            root.add(categoryPathProvider.apply(item), item);
        }
        return root;
    }

    public ContentSelectionSummary summary(ContentPanelItem item) {
        IndexedContent content = item.content();
        String category = String.join(" / ", content.categoryPath());
        if (content instanceof DocumentDescriptor descriptor) {
            String kind = tagLabels(descriptor.tags(), "Document");
            return new ContentSelectionSummary(
                    descriptor.title(),
                    kind,
                    descriptor.description(),
                    category,
                    descriptor.relatedTemplateIds(),
                    "Open");
        }
        if (content instanceof TemplateDescriptor descriptor) {
            String kind = tagLabels(descriptor.tags(), "Template");
            String action = descriptor.tags().isEmpty() ? "Create Program" : "Open " + kind;
            return new ContentSelectionSummary(
                    descriptor.title(),
                    kind,
                    descriptor.description(),
                    category,
                    descriptor.relatedDocumentIds(),
                    action);
        }
        return new ContentSelectionSummary(
                content.title(), "Content", content.description(), category, List.of(), "Open");
    }

    private List<DocumentCatalogEntry> filteredDocuments(List<DocumentCatalogEntry> documents, ContentScope scope) {
        return documents.stream().filter(entry -> scope.matches(entry.descriptor())).toList();
    }

    private List<TemplateCatalogEntry> filteredTemplates(List<TemplateCatalogEntry> templates, ContentScope scope) {
        return templates.stream().filter(entry -> scope.matches(entry.descriptor())).toList();
    }

    private ContentPanelItem toItem(ContentSearchResult result) {
        IndexedContent content = result.content();
        return new ContentPanelItem(
                result.globalId(),
                content.title(),
                subtitle(content, result.template()),
                content.description(),
                content.categoryPath(),
                content.tags(),
                result.template(),
                content);
    }

    private ContentPanelItem toItem(DocumentCatalogEntry entry) {
        DocumentDescriptor descriptor = entry.descriptor();
        return new ContentPanelItem(
                entry.globalId().value(),
                descriptor.title(),
                subtitle(descriptor, false),
                descriptor.description(),
                descriptor.categoryPath(),
                descriptor.tags(),
                false,
                descriptor);
    }

    private ContentPanelItem toItem(TemplateCatalogEntry entry) {
        TemplateDescriptor descriptor = entry.descriptor();
        return new ContentPanelItem(
                entry.globalId().value(),
                descriptor.title(),
                subtitle(descriptor, true),
                descriptor.description(),
                descriptor.categoryPath(),
                descriptor.tags(),
                true,
                descriptor);
    }

    private String subtitle(IndexedContent content, boolean template) {
        String kind = tagLabels(content.tags(), template ? "Template" : "Document");
        String category = String.join(" / ", content.categoryPath());
        return category.isBlank() ? kind : kind + " · " + category;
    }

    private String tagLabels(Set<String> tags, String fallback) {
        if (tags.isEmpty()) {
            return fallback;
        }
        return tags.stream()
                .map(ContentScope::labelForTag)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((left, right) -> left + ", " + right)
                .orElse(fallback);
    }
}
