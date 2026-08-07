package com.basic4gl.desktop.spi.content;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public final class DirectoryContentSource implements ContentSource {

    private final Path root;

    public DirectoryContentSource(Path root) {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (!Files.isDirectory(this.root)) {
            throw new IllegalArgumentException("Content root is not a directory: " + root);
        }
    }

    @Override
    public InputStream open(String normalizedPath) throws IOException {
        String safePath = ContentPaths.normalize(normalizedPath);
        Path target = root.resolve(safePath.replace("/", root.getFileSystem().getSeparator()))
                .normalize();
        Path realRoot = root.toRealPath();
        Path realTarget = target.toRealPath();
        if (!realTarget.startsWith(realRoot) || !Files.isRegularFile(realTarget)) {
            throw new FileNotFoundException("Content resource not found: " + normalizedPath);
        }
        return Files.newInputStream(realTarget);
    }

    @Override
    public Collection<ContentResource> resources() throws IOException {
        Path realRoot = root.toRealPath();
        try (var paths = Files.walk(realRoot)) {
            return paths.filter(Files::isRegularFile)
                    .map(realRoot::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .map(ContentPaths::normalize)
                    .map(ContentResource::new)
                    .sorted((left, right) -> left.path().compareToIgnoreCase(right.path()))
                    .toList();
        }
    }
}
