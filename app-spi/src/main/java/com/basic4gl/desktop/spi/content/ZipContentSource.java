package com.basic4gl.desktop.spi.content;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipContentSource implements ContentSource {

    private final Path zipPath;
    private final String rootPrefix;

    public ZipContentSource(Path zipPath) {
        this(zipPath, "");
    }

    public ZipContentSource(Path zipPath, String rootPrefix) {
        this.zipPath =
                Objects.requireNonNull(zipPath, "zipPath").toAbsolutePath().normalize();
        if (rootPrefix == null || rootPrefix.isBlank()) {
            this.rootPrefix = "";
        } else {
            this.rootPrefix = ContentPaths.normalize(rootPrefix) + "/";
        }
    }

    @Override
    public InputStream open(String normalizedPath) throws IOException {
        String safePath = ContentPaths.normalize(normalizedPath);
        ZipFile zipFile = openZipFile();
        ZipEntry entry = zipFile.getEntry(rootPrefix + safePath);
        if (entry == null || entry.isDirectory()) {
            zipFile.close();
            throw new FileNotFoundException("Content resource not found: " + normalizedPath);
        }
        InputStream input = zipFile.getInputStream(entry);
        return new FilterInputStream(input) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    zipFile.close();
                }
            }
        };
    }

    @Override
    public Collection<ContentResource> resources() throws IOException {
        try (ZipFile zipFile = openZipFile()) {
            return zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(ZipEntry::getName)
                    .filter(this::isUnderRoot)
                    .map(this::removeRoot)
                    .map(ContentPaths::normalize)
                    .map(ContentResource::new)
                    .sorted((left, right) -> left.path().compareToIgnoreCase(right.path()))
                    .toList();
        } catch (IllegalArgumentException ex) {
            throw new IOException("ZIP content contains an unsafe resource path", ex);
        }
    }

    protected ZipFile openZipFile() throws IOException {
        return new ZipFile(zipPath.toFile());
    }

    private boolean isUnderRoot(String entryName) {
        return rootPrefix.isEmpty() || entryName.startsWith(rootPrefix);
    }

    private String removeRoot(String entryName) {
        if (rootPrefix.isEmpty()) {
            return entryName;
        }
        return entryName.substring(rootPrefix.length());
    }
}
