package com.basic4gl.library.plugin;

import com.basic4gl.runtime.plugin.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Represents a dynamically-loaded plugin JAR.
 * 
 * Replaces C++ DLL-based loading (LoadLibrary, GetProcAddress) with Java ServiceLoader.
 * Maintains the same plugin lifecycle: metadata validation -> provider discovery -> plugin.load().
 * 
 * Workflow:
 * 1. Load JAR via URLClassLoader with parent classloader inheritance
 * 2. Discover providers via ServiceLoader (replaces GetProcAddress)
 * 3. Call provider.metadata() for platform/version validation (replaces Query function)
 * 4. Call provider.createPlugin() if metadata passes (replaces Init function)
 * 5. Call plugin.load(registry) to register functions (existing lifecycle)
 * 6. Finalize via completeFunction()
 */
public class PluginJAR extends PluginLibrary {

    private final PluginJARManager manager;
    private String filename;
    private URLClassLoader classLoader;
    private PluginJARFile fileDetails;
    private String errorMessage;
    private PluginMetadata metadata;

    /**
     * Load and initialize a plugin JAR from the given path.
     * 
     * @param manager Parent PluginManager/PluginJARManager
     * @param path Directory path containing the JAR
     * @param filename JAR filename
     * @param isStandaloneExe Whether running as standalone executable
     */
    public PluginJAR(PluginJARManager manager, String path, String filename, boolean isStandaloneExe) {
        super(manager);
        this.manager = manager;
        this.filename = filename.toLowerCase();
        this.fileDetails = new PluginJARFile();
        this.fileDetails.setFilename(this.filename);
        this.errorMessage = "";
        this.metadata = null;

        try {
            // Step 1: Load JAR via URLClassLoader
            Path jarPath = Path.of(path, this.filename);
            if (!Files.exists(jarPath)) {
                errorMessage = "JAR file not found: " + jarPath;
                return;
            }

            URL jarUrl = jarPath.toUri().toURL();
            this.classLoader = URLClassLoader.newInstance(
                new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader()
            );

            // Step 2-4: Discover provider, validate metadata, create plugin
            if (!discoverAndLoadProvider()) {
                return;
            }

            // Step 5: Call plugin.load() to register functions
            if (!plugin.load(this, isStandaloneExe)) {
                return;
            }

            // Step 6: Finalize function registration
            completeFunction();

            fileDetails.setLoaded(true);
            failed = false;

        } catch (Exception e) {
            errorMessage = "Exception loading plugin JAR '" + filename + "': " + e.getMessage();
        }
    }

    /**
     * Discover provider via ServiceLoader, validate metadata, and create plugin.
     * Returns false if any step fails.
     */
    private boolean discoverAndLoadProvider() {
        try {
            ServiceLoader<Basic4GLPluginProvider> loader =
                ServiceLoader.load(Basic4GLPluginProvider.class, classLoader);

            for (Basic4GLPluginProvider provider : loader) {
                // Get metadata for preflight validation
                PluginMetadata metadata;
                try {
                    metadata = provider.metadata();
                } catch (Exception e) {
                    errorMessage = "Provider.metadata() failed: " + e.getMessage();
                    return false;
                }

                if (metadata == null) {
                    errorMessage = "Provider.metadata() returned null";
                    return false;
                }

                // Store metadata in fileDetails
                this.metadata = metadata;
                fileDetails.setDescription(
                        metadata.description() == null || metadata.description().isBlank()
                                ? metadata.name()
                                : metadata.description());
                fileDetails.setVersion(new PluginVersion(metadata.majorVersion(), metadata.minorVersion()));

                // Validate platform support
                if (metadata.platformSupport() == null) {
                    PlatformMetadataPolicy policy = manager.getPlatformMetadataPolicy();
                    if (policy == PlatformMetadataPolicy.STRICT_BLOCK) {
                        errorMessage = "Plugin '" + metadata.name() + "' did not provide platform support metadata";
                        return false;
                    }
                } else {
                    if (!metadata.platformSupport().supportsCurrent()) {
                        errorMessage = "Plugin '" + metadata.name() + "' does not support " +
                                PlatformId.current().id() + "-" + CpuArch.current().primary();
                        return false;
                    }
                }

                // Create plugin instance
                try {
                    plugin = provider.createPlugin();
                } catch (Exception e) {
                    errorMessage = "Provider.createPlugin() failed: " + e.getMessage();
                    return false;
                }

                if (plugin == null) {
                    errorMessage = "Provider.createPlugin() returned null";
                    return false;
                }

                // Found and initialized one provider; success
                return true;
            }

            // No providers found in JAR
            errorMessage = "No providers found in " + filename;
            return false;

        } catch (Exception e) {
            errorMessage = "ServiceLoader discovery failed: " + e.getMessage();
            return false;
        }
    }

    @Override
    public void dispose() {
        // Unload plugin first
        unload();

        // Close classloader to release JAR resources (allows reloading)
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (IOException e) {
                // Log but don't fail on close error
                System.err.println("Warning: Failed to close plugin classloader for '" + filename + "': " + e.getMessage());
            }
            classLoader = null;
        }
    }

    public String getFilename() {
        return filename;
    }

    public PluginJARFile getFileDetails() {
        return fileDetails;
    }

    @Override
    public String getDescription() {
        return fileDetails != null && fileDetails.getDescription() != null ?
                fileDetails.getDescription() : filename;
    }

    public PluginMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String getError() {
        if (!errorMessage.isEmpty()) {
            return errorMessage;
        }

        if (plugin != null) {
            String parentError = super.getError();
            if (parentError != null && !parentError.isEmpty()) {
                return parentError;
            }
        }

        if (classLoader == null) {
            return "Could not load plugin JAR: " + filename;
        }

        if (plugin == null) {
            return "Could not instantiate plugin from JAR: " + filename;
        }

        return "Plugin error: " + filename;
    }
}
