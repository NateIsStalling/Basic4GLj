package com.basic4gl.desktop.spi.content;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class MapContentSource implements ContentSource {

    private final Map<String, Supplier<InputStream>> resources;

    public MapContentSource(Map<String, ? extends Supplier<InputStream>> resources) {
        Objects.requireNonNull(resources, "resources");
        Map<String, Supplier<InputStream>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, ? extends Supplier<InputStream>> entry : resources.entrySet()) {
            String path = ContentPaths.normalize(entry.getKey());
            Supplier<InputStream> supplier = Objects.requireNonNull(entry.getValue(), "resource supplier");
            if (copied.put(path, supplier::get) != null) {
                throw new IllegalArgumentException("Duplicate content resource path: " + path);
            }
        }
        this.resources = Map.copyOf(copied);
    }

    public static MapContentSource fromBytes(Map<String, byte[]> resources) {
        Objects.requireNonNull(resources, "resources");
        Map<String, Supplier<InputStream>> suppliers = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            byte[] bytes =
                    Objects.requireNonNull(entry.getValue(), "resource bytes").clone();
            suppliers.put(entry.getKey(), () -> new ByteArrayInputStream(bytes));
        }
        return new MapContentSource(suppliers);
    }

    @Override
    public InputStream open(String normalizedPath) throws IOException {
        String safePath = ContentPaths.normalize(normalizedPath);
        Supplier<InputStream> supplier = resources.get(safePath);
        if (supplier == null) {
            throw new FileNotFoundException("Content resource not found: " + normalizedPath);
        }
        InputStream input = supplier.get();
        if (input == null) {
            throw new IOException("Content resource supplier returned null: " + safePath);
        }
        return input;
    }

    @Override
    public Collection<ContentResource> resources() {
        return resources.keySet().stream()
                .sorted(String::compareToIgnoreCase)
                .map(ContentResource::new)
                .toList();
    }
}
