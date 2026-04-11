package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * Metadata describing a plugin's capabilities, version, and platform support.
 * Replaces the C++ DLL_Name_QueryFunction pattern.
 * 
 * Plugin providers return this from their metadata() method to allow the host loader
 * to validate compatibility before instantiating the actual plugin.
 */
public class PluginMetadata {
    private final String name;
    private final int majorVersion;
    private final int minorVersion;
    private final String description;
    private final PlatformSupport platformSupport;
    private final int minApiMajor;
    private final int minApiMinor;

    private PluginMetadata(Builder builder) {
        this.name = builder.name;
        this.majorVersion = builder.majorVersion;
        this.minorVersion = builder.minorVersion;
        this.description = builder.description;
        this.platformSupport = builder.platformSupport;
        this.minApiMajor = builder.minApiMajor;
        this.minApiMinor = builder.minApiMinor;
    }

    public String name() {
        return name;
    }

    public int majorVersion() {
        return majorVersion;
    }

    public int minorVersion() {
        return minorVersion;
    }

    public String description() {
        return description;
    }

    public PlatformSupport platformSupport() {
        return platformSupport;
    }

    public int minApiMajor() {
        return minApiMajor;
    }

    public int minApiMinor() {
        return minApiMinor;
    }

    /**
     * Check if this plugin is compatible with the host's API version.
     */
    public boolean isApiCompatible(int hostMajor, int hostMinor) {
        if (hostMajor > minApiMajor) return true;
        if (hostMajor == minApiMajor && hostMinor >= minApiMinor) return true;
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s v%d.%d", name, majorVersion, minorVersion);
    }

    /**
     * Builder for fluent PluginMetadata construction.
     */
    public static class Builder {
        private String name;
        private int majorVersion = 1;
        private int minorVersion = 0;
        private String description = "";
        private PlatformSupport platformSupport;
        private int minApiMajor = 1;
        private int minApiMinor = 0;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(int major, int minor) {
            this.majorVersion = major;
            this.minorVersion = minor;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder platformSupport(PlatformSupport support) {
            this.platformSupport = support;
            return this;
        }

        public Builder minApiVersion(int major, int minor) {
            this.minApiMajor = major;
            this.minApiMinor = minor;
            return this;
        }

        public PluginMetadata build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Plugin name is required");
            }
            if (platformSupport == null) {
                platformSupport = new PlatformSupport.Builder().addAllPlatforms().build();
            }
            return new PluginMetadata(this);
        }
    }
}

