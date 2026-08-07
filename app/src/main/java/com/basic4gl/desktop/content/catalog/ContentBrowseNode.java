package com.basic4gl.desktop.content.catalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ContentBrowseNode {

    private final String name;
    private final List<ContentBrowseNode> children = new ArrayList<>();
    private final List<ContentPanelItem> items = new ArrayList<>();

    public ContentBrowseNode(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public List<ContentBrowseNode> children() {
        return List.copyOf(children);
    }

    public List<ContentPanelItem> items() {
        return List.copyOf(items);
    }

    void add(List<String> categoryPath, ContentPanelItem item) {
        if (categoryPath.isEmpty()) {
            items.add(item);
            sort();
            return;
        }
        String childName = categoryPath.get(0);
        ContentBrowseNode child = children.stream()
                .filter(node -> node.name.equals(childName))
                .findFirst()
                .orElseGet(() -> {
                    ContentBrowseNode node = new ContentBrowseNode(childName);
                    children.add(node);
                    return node;
                });
        child.add(categoryPath.subList(1, categoryPath.size()), item);
        sort();
    }

    private void sort() {
        children.sort(Comparator.comparing(ContentBrowseNode::name, String.CASE_INSENSITIVE_ORDER));
        items.sort(
                Comparator.comparing((ContentPanelItem item) -> item.content().sortOrder())
                        .thenComparing(ContentPanelItem::title, String.CASE_INSENSITIVE_ORDER));
    }
}
