package com.basic4gl.runtime.plugin;

/**
 * Policy for handling plugins with missing or incomplete platform metadata.
 * 
 * Allows host/loader to enforce different strictness levels:
 * - Development: lenient defaults for rapid iteration
 * - IDE: warn but allow with caveats
 * - Export/Distribution: strict enforcement to prevent incompatibility issues
 */
public enum PlatformMetadataPolicy {
    /**
     * ASSUME_ALL: If no platform support metadata is found, assume the plugin works on all platforms.
     * Use for: Development, legacy plugins, pure-Java-only plugins.
     * Risk: Plugin may fail at runtime on unsupported platforms.
     */
    ASSUME_ALL,

    /**
     * WARN_IDE_BLOCK_EXPORT: Allow in IDE with a warning, but block from export/bundling.
     * Use for: IDE development workflows where runtime testing is available.
     * Risk: User may accidentally export a plugin that fails on target platform.
     * Mitigation: Clear warnings + export preview validation.
     */
    WARN_IDE_BLOCK_EXPORT,

    /**
     * STRICT_BLOCK: Block plugin load entirely if metadata is missing/incomplete.
     * Use for: Production, App Store, distribution scenarios.
     * Ensures: Only well-declared plugins are used.
     * Cost: Breaks legacy plugins or requires metadata updates.
     */
    STRICT_BLOCK
}

