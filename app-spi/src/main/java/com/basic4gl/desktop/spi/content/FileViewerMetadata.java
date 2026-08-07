package com.basic4gl.desktop.spi.content;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Metadata about a file viewer
 *
 * Contains information for discovery and compatibility checking
 */
public class FileViewerMetadata {

    private final String name;
    private final String version;
    private final Set<String> supportedExtensions;
    private final Set<String> supportedMimeTypes;
    private final String description;

    public FileViewerMetadata(
            String name, String version, String description, String[] extensions, String[] mimeTypes) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.supportedExtensions = Collections.unmodifiableSet(
                extensions != null ? new HashSet<>(Arrays.asList(extensions)) : new HashSet<>());
        this.supportedMimeTypes = Collections.unmodifiableSet(
                mimeTypes != null ? new HashSet<>(Arrays.asList(mimeTypes)) : new HashSet<>());
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    public Set<String> getSupportedMimeTypes() {
        return supportedMimeTypes;
    }

    /**
     * Check if this viewer supports the given extension
     */
    public boolean supportsExtension(String extension) {
        if (extension == null) {
            return false;
        }
        String normalized = extension.toLowerCase();
        if (!normalized.startsWith(".")) {
            normalized = "." + normalized;
        }
        return supportedExtensions.contains(normalized);
    }

    /**
     * Check if this viewer supports the given MIME type
     */
    public boolean supportsMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return supportedMimeTypes.contains(mimeType.toLowerCase());
    }

    @Override
    public String toString() {
        return "FileViewerMetadata{" + "name='"
                + name + '\'' + ", version='"
                + version + '\'' + ", extensions="
                + supportedExtensions + '}';
    }
}
