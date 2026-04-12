package com.basic4gl.compiler.plugin.sdk.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Declares supported platforms (OS + CPU architecture combinations) for a plugin.
 * Allows plugin authors to declare compatibility and helps the host loader enforce
 * cross-platform constraints before loading.
 */
public class PlatformSupport {
    private final Set<Platform> supportedPlatforms;
    private final boolean supportsNativeLibraries;
    private final String sandboxNotes;

    public static class Platform {
        public final PlatformId os;
        public final CpuArch arch;

        public Platform(PlatformId os, CpuArch arch) {
            this.os = os;
            this.arch = arch;
        }

        public boolean matches(PlatformId osId, CpuArch archId) {
            return os == osId && arch == archId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Platform)) return false;
            Platform p = (Platform) o;
            return os == p.os && arch == p.arch;
        }

        @Override
        public int hashCode() {
            return os.hashCode() * 31 + arch.hashCode();
        }

        @Override
        public String toString() {
            return os.id() + "-" + arch.primary();
        }
    }

    private PlatformSupport(Builder builder) {
        this.supportedPlatforms = Collections.unmodifiableSet(builder.platforms);
        this.supportsNativeLibraries = builder.supportsNativeLibraries;
        this.sandboxNotes = builder.sandboxNotes;
    }

    public Set<Platform> supportedPlatforms() {
        return supportedPlatforms;
    }

    public boolean supportsNativeLibraries() {
        return supportsNativeLibraries;
    }

    public String sandboxNotes() {
        return sandboxNotes;
    }

    /**
     * Check if this plugin supports the current runtime platform.
     */
    public boolean supportsCurrent() {
        return supports(PlatformId.current(), CpuArch.current());
    }

    /**
     * Check if this plugin supports the given platform.
     */
    public boolean supports(PlatformId os, CpuArch arch) {
        return supportedPlatforms.stream().anyMatch(p -> p.matches(os, arch));
    }

    /**
     * Builder for fluent PlatformSupport construction.
     */
    public static class Builder {
        private final Set<Platform> platforms = new HashSet<>();
        private boolean supportsNativeLibraries = false;
        private String sandboxNotes = "";

        public Builder addPlatform(PlatformId os, CpuArch arch) {
            platforms.add(new Platform(os, arch));
            return this;
        }

        public Builder addAllPlatforms() {
            platforms.add(new Platform(PlatformId.WINDOWS, CpuArch.X86_64));
            platforms.add(new Platform(PlatformId.WINDOWS, CpuArch.X86_32));
            platforms.add(new Platform(PlatformId.MACOS, CpuArch.X86_64));
            platforms.add(new Platform(PlatformId.MACOS, CpuArch.ARM64));
            platforms.add(new Platform(PlatformId.LINUX, CpuArch.X86_64));
            platforms.add(new Platform(PlatformId.LINUX, CpuArch.ARM64));
            return this;
        }

        public Builder supportsNativeLibraries(boolean value) {
            this.supportsNativeLibraries = value;
            return this;
        }

        public Builder sandboxNotes(String notes) {
            this.sandboxNotes = notes;
            return this;
        }

        public PlatformSupport build() {
            if (platforms.isEmpty()) {
                // Default to all platforms if none specified
                return addAllPlatforms().build();
            }
            return new PlatformSupport(this);
        }
    }
}

