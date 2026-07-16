package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.IndexedContent;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ContentPanelModel {

    private final ContentSearchIndex searchIndex = new ContentSearchIndex();

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
        ContentBrowseNode root = new ContentBrowseNode(scopeLabel(scope));
        for (ContentPanelItem item : items(catalog, scope, "")) {
            root.add(item.categoryPath(), item);
        }
        return root;
    }

    public ContentSelectionSummary summary(ContentPanelItem item) {
        IndexedContent content = item.content();
        String category = String.join(" / ", content.categoryPath());
        if (content instanceof DocumentDescriptor descriptor) {
            String kind = documentKindLabel(descriptor.tags());
            return new ContentSelectionSummary(
                    descriptor.title(),
                    kind,
                    descriptor.description(),
                    category,
                    descriptor.relatedTemplateIds(),
                    "Open");
        }
        if (content instanceof TemplateDescriptor descriptor) {
            String kind = templateKindLabel(descriptor.tags());
            String action = hasTag(descriptor.tags(), "sample") ? "Open Sample" : "Create Program";
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
        return documents.stream()
                .filter(entry -> switch (scope) {
                    case ALL -> true;
                    case LEARN -> isLearning(entry.descriptor().tags());
                    case REFERENCE -> isReference(entry.descriptor().tags());
                    case SAMPLES -> false;
                })
                .toList();
    }

    private List<TemplateCatalogEntry> filteredTemplates(List<TemplateCatalogEntry> templates, ContentScope scope) {
        return templates.stream()
                .filter(entry -> switch (scope) {
                    case ALL -> true;
                    case LEARN, SAMPLES -> hasTag(entry.descriptor().tags(), "sample");
                    case REFERENCE -> false;
                })
                .toList();
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
        String kind = template ? templateKindLabel(content.tags()) : documentKindLabel(content.tags());
        String category = String.join(" / ", content.categoryPath());
        return category.isBlank() ? kind : kind + " · " + category;
    }

    private String documentKindLabel(Set<String> tags) {
        if (hasTag(tags, "tutorial")) {
            return "Tutorial";
        }
        if (hasTag(tags, "guide")) {
            return "Guide";
        }
        if (hasTag(tags, "reference")) {
            return "Reference";
        }
        return "Document";
    }

    private String templateKindLabel(Set<String> tags) {
        if (hasTag(tags, "sample")) {
            return "Sample";
        }
        if (hasTag(tags, "starter")) {
            return "Starter";
        }
        if (hasTag(tags, "project-template")) {
            return "Project Template";
        }
        return "Template";
    }

    private boolean isLearning(Set<String> tags) {
        return hasAnyTag(tags, Set.of("learn", "tutorial", "getting-started"));
    }

    private boolean isReference(Set<String> tags) {
        return hasAnyTag(tags, Set.of("reference", "guide"));
    }

    private boolean hasAnyTag(Set<String> tags, Set<String> needles) {
        return needles.stream().anyMatch(needle -> hasTag(tags, needle));
    }

    private boolean hasTag(Set<String> tags, String needle) {
        String normalizedNeedle = needle.toLowerCase(Locale.ROOT);
        return tags.stream().map(tag -> tag.toLowerCase(Locale.ROOT)).anyMatch(normalizedNeedle::equals);
    }

    private String scopeLabel(ContentScope scope) {
        return switch (scope) {
            case ALL -> "All";
            case LEARN -> "Learn";
            case REFERENCE -> "Reference";
            case SAMPLES -> "Samples";
        };
    }
}
