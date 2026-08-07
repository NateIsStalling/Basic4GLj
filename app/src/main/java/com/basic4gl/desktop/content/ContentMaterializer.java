package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.content.ContentPaths;
import com.basic4gl.desktop.spi.content.ContentResource;
import com.basic4gl.desktop.spi.content.ContentSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ContentMaterializer {

    private final Path cacheRoot;

    public ContentMaterializer(Path cacheRoot) {
        this.cacheRoot =
                Objects.requireNonNull(cacheRoot, "cacheRoot").toAbsolutePath().normalize();
    }

    public Path materialize(String pluginId, String providerId, String providerVersion, ContentSource source)
            throws IOException {
        Objects.requireNonNull(source, "source");
        Path finalRoot = versionRoot(pluginId, providerId, providerVersion);
        if (Files.isDirectory(finalRoot)) {
            return finalRoot;
        }

        Path providerRoot = finalRoot.getParent();
        Files.createDirectories(providerRoot);
        Path tempRoot = Files.createTempDirectory(providerRoot, ".tmp-");
        boolean moved = false;
        try {
            copyResources(source, tempRoot);
            if (Files.isDirectory(finalRoot)) {
                deleteRecursively(tempRoot);
                return finalRoot;
            }
            moveIntoPlace(tempRoot, finalRoot);
            moved = true;
            return finalRoot;
        } catch (FileAlreadyExistsException ex) {
            deleteRecursively(tempRoot);
            return finalRoot;
        } finally {
            if (!moved && Files.exists(tempRoot)) {
                deleteRecursively(tempRoot);
            }
        }
    }

    public void deleteObsoleteVersions(String pluginId, String providerId, String activeProviderVersion)
            throws IOException {
        Path providerRoot = cacheRoot
                .resolve("content")
                .resolve(safeKey(pluginId))
                .resolve(safeKey(providerId))
                .normalize();
        if (!Files.isDirectory(providerRoot)) {
            return;
        }
        String activeKey = safeKey(activeProviderVersion);
        try (var paths = Files.list(providerRoot)) {
            for (Path path : paths.toList()) {
                if (Files.isDirectory(path) && !path.getFileName().toString().equals(activeKey)) {
                    deleteRecursively(path);
                }
            }
        }
    }

    private void copyResources(ContentSource source, Path tempRoot) throws IOException {
        Set<String> copiedPaths = new HashSet<>();
        for (ContentResource resource : source.resources()) {
            String resourcePath;
            try {
                resourcePath = ContentPaths.normalize(resource.path());
            } catch (IllegalArgumentException ex) {
                throw new IOException("Unsafe content resource path: " + resource.path(), ex);
            }
            if (!copiedPaths.add(resourcePath)) {
                throw new IOException("Duplicate content resource path: " + resourcePath);
            }
            Path target = tempRoot.resolve(
                            resourcePath.replace("/", tempRoot.getFileSystem().getSeparator()))
                    .normalize();
            if (!target.startsWith(tempRoot)) {
                throw new IOException("Content resource escapes materialized root: " + resourcePath);
            }
            Files.createDirectories(target.getParent());
            try (InputStream input = source.open(resourcePath)) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private Path versionRoot(String pluginId, String providerId, String providerVersion) {
        return cacheRoot
                .resolve("content")
                .resolve(safeKey(pluginId))
                .resolve(safeKey(providerId))
                .resolve(safeKey(providerVersion))
                .normalize();
    }

    private static String safeKey(String value) {
        if (value == null || value.isBlank()) {
            return "_";
        }
        String safe = value.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.isBlank() ? "_" : safe;
    }

    private static void moveIntoPlace(Path tempRoot, Path finalRoot) throws IOException {
        try {
            Files.move(tempRoot, finalRoot, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempRoot, finalRoot);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path :
                    paths.sorted((left, right) -> right.compareTo(left)).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
