package com.basic4gl.library.plugin;

import com.basic4gl.runtime.plugin.PluginVersion;

/**
 * File details of a plugin JAR.
 */
public class PluginJARFile {
    private String filename;
    private String description;
    private boolean loaded;
    private PluginVersion version;

    public PluginJARFile() {

    }

    public PluginJARFile(String filename, String description, boolean loaded, PluginVersion version) {
        this.filename = filename;
        this.description = description;
        this.loaded = loaded;
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

    public PluginVersion getVersion() {
        return version;
    }

    public void setVersion(PluginVersion version) {
        this.version = version;
    }
}
