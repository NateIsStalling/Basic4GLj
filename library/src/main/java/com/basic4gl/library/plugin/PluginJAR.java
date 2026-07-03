package com.basic4gl.library.plugin;

import com.basic4gl.language.core.types.BasicValType;
import com.basic4gl.language.core.types.Constant;
import com.basic4gl.language.core.types.FunctionSpecification;
import com.basic4gl.language.core.types.ValType;
import com.basic4gl.language.spi.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    public static PluginJARFile inspectFileDetails(
            String path, String filename, PlatformMetadataPolicy platformMetadataPolicy) {
        PluginJARFile details = new PluginJARFile();
        details.setFilename(filename);
        details.setSourceDirectory(path);
        details.setLoaded(false);
        details.setCompatible(true);
        details.setDescription(filename);
        details.setVersion(new PluginVersion(0, 0));

        try {
            Path jarPath = Path.of(path, filename);
            if (!Files.exists(jarPath)) {
                details.setCompatible(false);
                details.setDescription("JAR file not found: " + jarPath);
                return details;
            }

            URL jarUrl = jarPath.toUri().toURL();
            try (URLClassLoader loader = URLClassLoader.newInstance(
                    new URL[] {jarUrl}, Thread.currentThread().getContextClassLoader())) {
                ServiceLoader<Basic4GLPluginProvider> providerLoader =
                        ServiceLoader.load(Basic4GLPluginProvider.class, loader);

                for (Basic4GLPluginProvider provider : providerLoader) {
                    PluginMetadata metadata;
                    try {
                        metadata = provider.metadata();
                    } catch (Exception e) {
                        details.setCompatible(false);
                        details.setDescription("Plugin metadata failed to load: " + e.getMessage());
                        return details;
                    }

                    if (metadata == null) {
                        details.setCompatible(false);
                        details.setDescription("Plugin metadata is missing: "
                                + provider.getClass().getName());
                        return details;
                    }

                    String pluginName = resolvePluginName(metadata, filename);
                    applyMetadataToDetails(details, metadata, pluginName);

                    String compatibilityError =
                            validateMetadataCompatibility(metadata, pluginName, platformMetadataPolicy);
                    if (compatibilityError != null) {
                        details.setCompatible(false);
                        details.setDescription(compatibilityError);
                    }

                    return details;
                }
            }

            details.setCompatible(false);
            details.setDescription("Invalid plugin '" + filename + "'");
            return details;
        } catch (UnsupportedClassVersionError e) {
            details.setCompatible(false);
            details.setDescription("Plugin failed to load: " + e.getMessage());
            return details;
        } catch (Exception e) {
            details.setCompatible(false);
            details.setDescription("ServiceLoader discovery failed: " + e.getMessage());
            return details;
        }
    }

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
        this.filename = filename;
        this.fileDetails = new PluginJARFile();
        this.fileDetails.setFilename(filename);
        this.fileDetails.setSourceDirectory(path);
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
                    new URL[] {jarUrl}, Thread.currentThread().getContextClassLoader());

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
                    errorMessage = "Plugin metadata failed to load: " + e.getMessage();
                    return false;
                }

                if (metadata == null) {
                    errorMessage =
                            "Plugin metadata is missing: " + provider.getClass().getName();
                    return false;
                }

                String pluginName = resolvePluginName(metadata, filename);

                // Store metadata in fileDetails
                this.metadata = metadata;
                applyMetadataToDetails(fileDetails, metadata, pluginName);

                // Validate compatibility
                String compatibilityError =
                        validateMetadataCompatibility(metadata, pluginName, manager.getPlatformMetadataPolicy());
                if (compatibilityError != null) {
                    fileDetails.setCompatible(false);
                    errorMessage = compatibilityError;
                    return false;
                }

                // Create plugin instance
                try {
                    plugin = provider.createPlugin();
                } catch (Exception e) {
                    errorMessage = "Plugin failed to load: " + e.getMessage();
                    return false;
                }

                if (plugin == null) {
                    errorMessage = "Plugin failed to load";
                    return false;
                }

                // Found and initialized one provider; success
                return true;
            }

            // No providers found in JAR
            errorMessage = "Invalid plugin '" + filename + "'";
            return false;
        } catch (UnsupportedClassVersionError e) {
            // Plugin was likely compiled with an unsupported JVM version (ie: newer version)
            errorMessage = "Plugin failed to load: " + e.getMessage();
            return false;
        } catch (Exception e) {
            errorMessage = "ServiceLoader discovery failed: " + e.getMessage();
            return false;
        }
    }

    private static String resolvePluginName(PluginMetadata metadata, String fallbackFilename) {
        return metadata.name() == null || metadata.name().isBlank() ? fallbackFilename : metadata.name();
    }

    private static void applyMetadataToDetails(PluginJARFile details, PluginMetadata metadata, String pluginName) {
        details.setCompatible(true);
        // Store plugin name separately for IDE display
        details.setPluginName(pluginName);
        details.setDescription(
                metadata.description() == null || metadata.description().isBlank()
                        ? pluginName
                        : metadata.description());
        details.setVersion(new PluginVersion(metadata.majorVersion(), metadata.minorVersion()));
    }

    private static String validateMetadataCompatibility(
            PluginMetadata metadata, String pluginName, PlatformMetadataPolicy platformMetadataPolicy) {
        if (!metadata.isApiCompatible(
                Basic4GLPluginProvider.PLUGIN_API_VERSION_MAJOR, Basic4GLPluginProvider.PLUGIN_API_VERSION_MINOR)) {
            return "Plugin '" + pluginName + "' requires plugin API >= " + metadata.minApiMajor() + "."
                    + metadata.minApiMinor() + ", but current plugin API version is "
                    + Basic4GLPluginProvider.PLUGIN_API_VERSION_MAJOR + "."
                    + Basic4GLPluginProvider.PLUGIN_API_VERSION_MINOR;
        }

        if (metadata.platformSupport() == null) {
            if (platformMetadataPolicy == PlatformMetadataPolicy.STRICT_BLOCK) {
                return "Plugin '" + pluginName + "' did not provide platform support metadata";
            }
            return null;
        }

        if (!metadata.platformSupport().supportsCurrent()) {
            return "Plugin '" + pluginName + "' does not support "
                    + PlatformId.current().id() + "-" + CpuArch.current().primary();
        }

        return null;
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
                System.err.println(
                        "Warning: Failed to close plugin classloader for '" + filename + "': " + e.getMessage());
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
        return fileDetails != null && fileDetails.getDescription() != null ? fileDetails.getDescription() : filename;
    }

    public PluginMetadata getMetadata() {
        return metadata;
    }

    public PluginJARDetails toDetails() {
        List<String> functions = new ArrayList<>();
        for (int i = 0; i < count(); i++) {
            String functionName = getFunctionName(i);
            if (functionName == null || functionName.isBlank()) {
                continue;
            }

            FunctionSpecification spec = null;
            if (i >= 0 && i < getFunctionSpecs().size()) {
                spec = getFunctionSpecs().get(i);
            }
            functions.add(formatFunctionSignature(functionName, spec));
        }
        functions.sort(String.CASE_INSENSITIVE_ORDER);

        List<String> constants = new ArrayList<>();
        for (Map.Entry<String, Constant> entry : getConstants().entrySet()) {
            Constant constant = entry.getValue();
            String line = entry.getKey()
                    + " = ("
                    + formatType(constant == null ? BasicValType.VTP_UNDEFINED : constant.getType())
                    + ") "
                    + (constant == null ? "" : constant.toString());
            constants.add(line);
        }
        constants.sort(String.CASE_INSENSITIVE_ORDER);

        String structures = manager.getStructureManager().describeOwnedStructures(this);
        String metadataDetails = buildMetadataDetails();
        String metadataSummary =
                fileDetails == null || fileDetails.getDescription() == null ? filename : fileDetails.getDescription();

        return new PluginJARDetails(
                filename,
                metadataSummary,
                metadataDetails,
                fileDetails != null && fileDetails.isCompatible(),
                functions,
                constants,
                structures);
    }

    private String buildMetadataDetails() {
        if (metadata == null) {
            return "No metadata available.";
        }

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(resolvePluginName(metadata, filename)).append(System.lineSeparator());
        details.append("Version: ")
                .append(metadata.majorVersion())
                .append(".")
                .append(metadata.minorVersion())
                .append(System.lineSeparator());
        details.append("Description: ")
                .append(
                        metadata.description() == null || metadata.description().isBlank()
                                ? "-"
                                : metadata.description())
                .append(System.lineSeparator());
        details.append("Min API: ")
                .append(metadata.minApiMajor())
                .append(".")
                .append(metadata.minApiMinor())
                .append(System.lineSeparator());
        if (metadata.platformSupport() == null) {
            details.append("Platforms: (unspecified)");
        } else {
            List<String> platforms = metadata.platformSupport().supportedPlatforms().stream()
                    .map(PlatformSupport.Platform::toString)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            details.append("Platforms: ").append(platforms.isEmpty() ? "(none)" : String.join(", ", platforms));
        }
        return details.toString();
    }

    private static String formatFunctionSignature(String name, FunctionSpecification spec) {
        if (spec == null) {
            return name;
        }

        StringBuilder line = new StringBuilder();
        if (spec.isFunction()) {
            line.append(formatType(spec.getReturnType())).append(" ");
        }
        line.append(name);
        line.append(spec.hasBrackets() ? "(" : " ");

        boolean needComma = false;
        if (spec.getParamTypes() != null && spec.getParamTypes().getParams() != null) {
            for (ValType type : spec.getParamTypes().getParams()) {
                if (needComma) {
                    line.append(", ");
                }
                line.append(formatType(type));
                needComma = true;
            }
        }
        if (spec.hasBrackets()) {
            line.append(")");
        }
        return line.toString();
    }

    private static String formatType(ValType type) {
        if (type == null) {
            return "???";
        }

        StringBuilder result = new StringBuilder();
        if (type.pointerLevel > 0) {
            result.append("BYREF ");
        }
        switch (type.basicType) {
            case BasicValType.VTP_INT:
                result.append("INT");
                break;
            case BasicValType.VTP_REAL:
                result.append("REAL");
                break;
            case BasicValType.VTP_STRING:
                result.append("STRING");
                break;
            case BasicValType.VTP_FUNC_PTR:
            case BasicValType.VTP_UNTYPED_FUNC_PTR:
                result.append("FUNCTION");
                break;
            case BasicValType.VTP_VOID:
                break;
            default:
                result.append("???");
                break;
        }
        return result.toString();
    }

    private static String formatType(int basicType) {
        return formatType(new ValType(basicType));
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
