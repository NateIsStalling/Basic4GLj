package com.basic4gl.desktop.spi.content;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ClasspathContentSource implements ContentSource {

    private final ClassLoader classLoader;
    private final String rootPrefix;
    private final List<ContentResource> resources;

    public ClasspathContentSource(ClassLoader classLoader, String rootPrefix, String indexResourcePath)
            throws IOException {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.rootPrefix = normalizePrefix(rootPrefix);
        String indexPath = ContentPaths.normalize(indexResourcePath);
        try (InputStream input = classLoader.getResourceAsStream(indexPath)) {
            if (input == null) {
                throw new FileNotFoundException("Classpath content index not found: " + indexPath);
            }
            this.resources = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .map(ContentPaths::normalize)
                    .map(ContentResource::new)
                    .sorted((left, right) -> left.path().compareToIgnoreCase(right.path()))
                    .toList();
        }
    }

    @Override
    public InputStream open(String normalizedPath) throws IOException {
        String safePath = ContentPaths.normalize(normalizedPath);
        InputStream input = classLoader.getResourceAsStream(rootPrefix + safePath);
        if (input == null) {
            throw new FileNotFoundException("Classpath content resource not found: " + safePath);
        }
        return input;
    }

    @Override
    public Collection<ContentResource> resources() {
        return resources;
    }

    private static String normalizePrefix(String rootPrefix) {
        if (rootPrefix == null || rootPrefix.isBlank()) {
            return "";
        }
        return ContentPaths.normalize(rootPrefix) + "/";
    }
}
