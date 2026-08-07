package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DirectoryDocumentProvider implements DocumentProvider {

    private final String id;
    private final String version;
    private final ContentSource source;
    private final List<String> rootCategory;
    private final List<DocumentDescriptor> index;

    public DirectoryDocumentProvider(String id, String version, Path root, List<String> rootCategory)
            throws IOException {
        this(id, version, new DirectoryContentSource(root), rootCategory);
    }

    public DirectoryDocumentProvider(String id, String version, ContentSource source, List<String> rootCategory)
            throws IOException {
        this.id = ContentValidation.requireNonBlank(id, "id");
        this.version = ContentValidation.requireNonBlank(version, "version");
        this.source = source;
        this.rootCategory = ContentValidation.copyList(rootCategory);
        this.index = buildIndex();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public List<DocumentDescriptor> getIndex() {
        return index;
    }

    @Override
    public ContentDocument openDocument(String documentId) throws IOException {
        for (DocumentDescriptor descriptor : index) {
            if (descriptor.id().equals(documentId)) {
                return new ContentDocument(mediaType(descriptor.id()), source, descriptor.id());
            }
        }
        throw new IOException("Unknown document: " + documentId);
    }

    protected List<DocumentDescriptor> buildIndex() throws IOException {
        List<DocumentDescriptor> descriptors = new ArrayList<>();
        int sortOrder = 0;
        for (ContentResource resource : source.resources()) {
            String path = resource.path();
            if (!isMarkdown(path) || hidden(path)) {
                continue;
            }
            descriptors.add(new DocumentDescriptor(
                    path,
                    title(path),
                    "",
                    category(path),
                    tags(path),
                    "index.md".equalsIgnoreCase(path) ? -1000 : sortOrder++,
                    List.of()));
        }
        return List.copyOf(descriptors);
    }

    private String title(String path) throws IOException {
        try (var input = source.open(path)) {
            String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : text.split("\\R")) {
                if (line.startsWith("# ")) {
                    return line.substring(2).trim();
                }
            }
        }
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return splitTitle(filename.replaceFirst("\\.[^.]+$", ""));
    }

    private List<String> category(String path) {
        List<String> category = new ArrayList<>(rootCategory);
        if (!"index.md".equalsIgnoreCase(path)) {
            category.add("Reference");
        }
        return List.copyOf(category);
    }

    private Set<String> tags(String path) {
        if ("index.md".equalsIgnoreCase(path)) {
            return Set.of("learn", "getting-started");
        }
        return Set.of("reference", "guide");
    }

    private static boolean isMarkdown(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".md") || lower.endsWith(".markdown");
    }

    private static String mediaType(String path) {
        return isMarkdown(path) ? "text/markdown" : "text/plain";
    }

    static boolean hidden(String path) {
        for (String segment : path.split("/")) {
            if (segment.startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    static String splitTitle(String text) {
        String spaced = text.replace('-', ' ').replace('_', ' ').replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        StringBuilder result = new StringBuilder();
        for (String word : spaced.split("\\s+")) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
