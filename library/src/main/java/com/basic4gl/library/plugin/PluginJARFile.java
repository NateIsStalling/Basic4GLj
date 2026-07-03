package com.basic4gl.library.plugin;

import com.basic4gl.language.spi.PluginVersion;

/**
 * File details of a plugin JAR.
 */
public class PluginJARFile {
    private String filename;
    private String description;
    private boolean loaded;
    private boolean compatible = true;
    private String sourceDirectory;
    private PluginVersion version;

    public PluginJARFile() {}

    public PluginJARFile(
            String filename,
            String description,
            boolean loaded,
            boolean compatible,
            String sourceDirectory,
            PluginVersion version) {
        this.filename = filename;
        this.description = description;
        this.loaded = loaded;
        this.compatible = compatible;
        this.sourceDirectory = sourceDirectory;
        this.version = version;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public PluginVersion getVersion() {
        return version;
    }

    public void setVersion(PluginVersion version) {
        this.version = version;
    }
}
