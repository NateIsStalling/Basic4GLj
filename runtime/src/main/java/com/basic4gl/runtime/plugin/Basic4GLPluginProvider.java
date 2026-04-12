package com.basic4gl.runtime.plugin;

/**
 * Service provider entry point for a Basic4GL plugin JAR.
 *
 * <p>Plugin JARs expose one or more providers via Java's {@link java.util.ServiceLoader}
 * mechanism. The host queries {@link #metadata()} first, validates compatibility, then
 * requests an actual plugin instance via {@link #createPlugin()}.
 */
public interface Basic4GLPluginProvider {

	/**
	 * Returns descriptive metadata used for compatibility and platform checks.
	 */
	PluginMetadata metadata();

	/**
	 * Creates a new plugin instance after metadata validation succeeds.
	 */
	Basic4GLPlugin createPlugin();
}

