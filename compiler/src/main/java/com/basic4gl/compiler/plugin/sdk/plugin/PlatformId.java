package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * Operating system identifier for platform-specific plugin declarations.
 */
public enum PlatformId {
    WINDOWS("windows"),
    MACOS("macos"),
    LINUX("linux"),
    UNKNOWN("unknown");

    private final String id;

    PlatformId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static PlatformId fromString(String id) {
        return switch (id.toLowerCase()) {
            case "windows" -> WINDOWS;
            case "macos" -> MACOS;
            case "linux" -> LINUX;
            default -> UNKNOWN;
        };
    }

    /**
     * Detect current platform at runtime.
     */
    public static PlatformId current() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) return WINDOWS;
        if (os.contains("mac")) return MACOS;
        if (os.contains("nux") || os.contains("linux")) return LINUX;
        return UNKNOWN;
    }
}

