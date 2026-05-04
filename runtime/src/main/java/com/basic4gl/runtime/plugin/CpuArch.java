package com.basic4gl.runtime.plugin;

/**
 * CPU architecture identifier for platform-specific plugin declarations.
 */
public enum CpuArch {
    X86_64("x86_64", "amd64"),
    ARM64("arm64", "aarch64"),
    X86_32("x86", "i386"),
    UNKNOWN("unknown");

    private final String[] aliases;

    CpuArch(String... aliases) {
        this.aliases = aliases;
    }

    public String primary() {
        return aliases.length > 0 ? aliases[0] : "unknown";
    }

    public static CpuArch fromString(String arch) {
        String normalized = arch.toLowerCase();
        for (CpuArch a : CpuArch.values()) {
            for (String alias : a.aliases) {
                if (alias.equalsIgnoreCase(normalized)) {
                    return a;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * Detect current CPU architecture at runtime.
     */
    public static CpuArch current() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return fromString(arch);
    }
}

