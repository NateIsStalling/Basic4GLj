package com.basic4gl.library.plugin;

import com.basic4gl.language.core.streaming.Streaming;
import com.basic4gl.language.spi.PlatformMetadataPolicy;
import com.basic4gl.language.spi.PluginLibrary;
import com.basic4gl.language.spi.PluginManager;
import com.basic4gl.language.spi.PluginSharedInterface;
import com.basic4gl.language.spi.PluginVersion;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Manages loading and maintaining plugin JARs
 */
public class PluginJARManager extends PluginManager {
    private List<String> directories;
    private PlatformMetadataPolicy platformMetadataPolicy = PlatformMetadataPolicy.WARN_IDE_BLOCK_EXPORT;

    /**
     * Finds the PluginLibrary for a loaded JAR with the given filename.
     * @param filename
     * @return
     */
    private PluginLibrary findItor(String filename) {
        String normalized = filename == null ? "" : filename.trim();

        for (PluginLibrary lib : plugins) {
            // Filter to JARs
            if (lib instanceof PluginJAR) {
                PluginJAR jar = (PluginJAR) lib;
                if (jar.getFilename().equalsIgnoreCase(normalized)) {
                    return lib;
                }
            }
        }

        // Not found
        return null;
    }

    public PluginJARManager(boolean isStandaloneExe) {
        super(isStandaloneExe);
        this.directories = new ArrayList<>();
    }

    /**
     * @return Returns a list of currently loaded plugin JARs.
     */
    public Vector<PluginJAR> loadedJARs() {
        Vector<PluginJAR> result = new Vector<>();

        for (PluginLibrary lib : plugins) {
            if (lib instanceof PluginJAR) {
                result.add((PluginJAR) lib);
            }
        }

        return result;
    }

    /**
     * Find and list JAR files.
     * @return
     */
    public Vector<PluginJARFile> getJARFiles() {
        Vector<PluginJARFile> result = new Vector<>();
        error = null;

        if (directories.isEmpty()) {
            return result;
        }

        for (String directory : directories) {
            Path dirPath = Path.of(directory);
            if (!Files.isDirectory(dirPath)) {
                continue;
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.jar")) {
                for (Path jarPath : stream) {
                    String filename = jarPath.getFileName().toString();
                    PluginJAR loadedJar = findByFilenameAndSource(filename, directory);

                    if (loadedJar != null) {
                        result.add(loadedJar.getFileDetails());
                        continue;
                    }

                    result.add(PluginJAR.inspectFileDetails(directory, filename, platformMetadataPolicy));
                }
            } catch (IOException e) {
                error = "Failed to scan plugin directory '" + directory + "': " + e.getMessage();
            }
        }

        return result;
    }

    /**
     * Finds the PluginJAR with the given filename.
     * @param filename
     * @return
     */
    public PluginJAR find(String filename) {
        return (PluginJAR) findItor(filename);
    }

    /**
     * Return true if a JAR file is loaded
     * @param filename
     * @return
     */
    public boolean isLoaded(String filename) {
        return find(filename) != null;
    }

    public boolean isLoaded(String filename, String sourceDirectory) {
        return findByFilenameAndSource(filename, sourceDirectory) != null;
    }

    public PluginJAR findByFilenameAndSource(String filename, String sourceDirectory) {
        if (filename == null || sourceDirectory == null) {
            return null;
        }
        String normalizedSource = normalizeDirectory(sourceDirectory);
        for (PluginLibrary lib : plugins) {
            if (!(lib instanceof PluginJAR jar)) {
                continue;
            }
            PluginJARFile details = jar.getFileDetails();
            if (details == null || details.getFilename() == null || details.getSourceDirectory() == null) {
                continue;
            }
            if (!details.getFilename().equalsIgnoreCase(filename)) {
                continue;
            }
            String loadedSource = normalizeDirectory(details.getSourceDirectory());
            if (loadedSource != null && loadedSource.equalsIgnoreCase(normalizedSource)) {
                return jar;
            }
        }
        return null;
    }

    public PluginJARDetails getPluginDetails(String filename) {
        return getPluginDetails(filename, null);
    }

    public PluginJARDetails getPluginDetails(String filename, String sourceDirectory) {
        if (filename == null || filename.isBlank()) {
            return new PluginJARDetails(
                    "",
                    "Invalid plugin name",
                    "No plugin filename was provided.",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "");
        }

        PluginJAR loadedJar = sourceDirectory == null
                ? find(filename)
                : findByFilenameAndSource(filename, sourceDirectory);
        if (loadedJar != null) {
            return loadedJar.toDetails();
        }

        String resolvedSourceDirectory = sourceDirectory == null
                ? findContainingDirectory(filename)
                : normalizeDirectory(sourceDirectory);
        if (resolvedSourceDirectory == null) {
            return new PluginJARDetails(
                    filename,
                    "Plugin not found",
                    "No configured plugin source contains this JAR.",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "");
        }

        PluginJARFile fileDetails = PluginJAR.inspectFileDetails(resolvedSourceDirectory, filename, platformMetadataPolicy);
        if (fileDetails == null || !fileDetails.isCompatible()) {
            String summary = fileDetails == null || fileDetails.getDescription() == null
                    ? filename
                    : fileDetails.getDescription();
            return new PluginJARDetails(
                    filename,
                    summary,
                    "Load this plugin to inspect callable members.",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "");
        }

        PluginJARManager inspector = new PluginJARManager(isStandaloneExe);
        inspector.setPlatformMetadataPolicy(platformMetadataPolicy);
        inspector.setDirectories(List.of(resolvedSourceDirectory));

        if (!inspector.loadPlugin(filename, resolvedSourceDirectory)) {
            String loadError = inspector.getError();
            return new PluginJARDetails(
                    filename,
                    loadError == null || loadError.isBlank() ? "Failed to inspect plugin" : loadError,
                    "The plugin could not be loaded for inspection.",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "");
        }

        PluginJAR inspected = inspector.find(filename);
        PluginJARDetails details;
        if (inspected == null) {
            details = new PluginJARDetails(
                    filename,
                    "Failed to inspect plugin",
                    "Plugin loaded but no details were available.",
                    false,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "");
        } else {
            details = inspected.toDetails();
        }

        inspector.clear();
        return details;
    }

    public PlatformMetadataPolicy getPlatformMetadataPolicy() {
        return platformMetadataPolicy;
    }

    public void setPlatformMetadataPolicy(PlatformMetadataPolicy platformMetadataPolicy) {
        this.platformMetadataPolicy =
                platformMetadataPolicy == null ? PlatformMetadataPolicy.WARN_IDE_BLOCK_EXPORT : platformMetadataPolicy;
    }

    public boolean loadPlugin(String filename) {
        String sourceDirectory = findContainingDirectory(filename);
        if (sourceDirectory == null) {
            error = "Could not find plugin JAR '" + filename + "' in configured plugin directories";
            return false;
        }
        return loadPlugin(filename, sourceDirectory);
    }

    public boolean loadPlugin(String filename, String sourceDirectory) {
        // Attempt to load JAR
        // First check that it's not already loaded
        if (isLoaded(filename, sourceDirectory)) {
            error = "A plugin JAR by this name is already loaded";
            return false;
        }
        String normalizedSource = normalizeDirectory(sourceDirectory);
        if (normalizedSource == null || !Files.exists(Path.of(normalizedSource, filename))) {
            error = "Could not find plugin JAR '" + filename + "' in '" + sourceDirectory + "'";
            return false;
        }

        // Load JAR
        PluginJAR jar = new PluginJAR(this, normalizedSource, filename, isStandaloneExe);
        if (jar.hasFailed()) {
            error = jar.getError();
            jar.dispose();
            return false;
        }

        // Add to list
        plugins.add(jar);

        return true;
    }

    public boolean unloadPlugin(String filename) {
        return unloadPlugin(filename, null);
    }

    public boolean unloadPlugin(String filename, String sourceDirectory) {

        // Find JAR
        PluginLibrary i = sourceDirectory == null ? findItor(filename) : findByFilenameAndSource(filename, sourceDirectory);
        if (i == null) {
            error = "This plugin JAR is not loaded";
            return false;
        }

        // Check that JAR can be unloaded.
        // If the JAR owns objects that are currently used by other JARs, then it
        // cannot be unloaded before the other JARs have been.
        PluginJAR jar = (PluginJAR) i;
        if (jar.isReferenced()) {
            error = "The following plugin JAR(s) must be unloaded first:\r\n" + jar.describeReferencingPlugins();
            return false;
        }

        // Inform other JARs that this one is being unloaded
        for (PluginLibrary plugin : plugins) {
            if (jar != plugin && plugin instanceof PluginJAR) {
                ((PluginJAR) plugin).removeReferencingPlugin(jar);
            }
        }

        // Unregister all interfaces owned by this JAR
        for (Iterator<Map.Entry<String, PluginSharedInterface>> it =
                        sharedInterfaces.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<String, PluginSharedInterface> entry = it.next();
            if (entry.getValue().getOwner() == jar) {
                it.remove();
            }
        }

        // Unload it
        i.dispose();

        // Remove it from our list
        plugins.remove(i);

        return true;
    }

    public void streamOut(DataOutputStream stream) throws IOException {
        // Write JAR filenames, and versions
        List<PluginLibrary> pluginJars =
                plugins.stream().filter(x -> x instanceof PluginJAR).toList();
        Streaming.writeLong(stream, pluginJars.size());
        for (PluginLibrary plugin : pluginJars) {
            PluginJAR jar = (PluginJAR) plugin;
            String filename = jar.getFileDetails().getFilename();
            Streaming.writeString(stream, filename);
            Streaming.writeLong(stream, jar.getFileDetails().getVersion().getMajorVersion());
            Streaming.writeLong(stream, jar.getFileDetails().getVersion().getMinorVersion());
        }
    }

    public boolean streamIn(DataInputStream stream) throws IOException {

        // Clear out any existing plugins
        clear();

        long count = Streaming.readLong(stream);
        for (int i = 0; i < count; i++) {

            // Read file details
            String filename = Streaming.readString(stream);
            PluginVersion version = new PluginVersion();
            version.setMajorVersion(Streaming.readLong(stream));
            version.setMinorVersion(Streaming.readLong(stream));

            // Attempt to load JAR
            if (!loadPlugin(filename)) return false;

            // Check version number
            PluginJAR jar = find(filename);
            if (!jar.getFileDetails().getVersion().equals(version)) {
                error = "Plugin JAR " + filename + " is the wrong version.\r\n" + "Version is "
                        + jar.getFileDetails().getVersion().toString() + ", expected "
                        + version.toString();
                return false;
            }
        }
        return true;
    }

    public void setDirectory(String directory) {
        String normalized = normalizeDirectory(directory);
        if (normalized == null) {
            this.directories = new ArrayList<>();
            return;
        }
        this.directories = new ArrayList<>(List.of(normalized));
    }

    public String getDirectory() {
        return directories.isEmpty() ? "" : directories.get(0);
    }

    public void setDirectories(List<String> directories) {
        ArrayList<String> normalizedDirectories = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (directories != null) {
            for (String directory : directories) {
                String normalized = normalizeDirectory(directory);
                if (normalized == null) {
                    continue;
                }
                String key = normalized.toLowerCase();
                if (seen.contains(key)) {
                    continue;
                }
                seen.add(key);
                normalizedDirectories.add(normalized);
            }
        }
        this.directories = normalizedDirectories;
    }

    public List<String> getDirectories() {
        return List.copyOf(directories);
    }

    private String findContainingDirectory(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        for (String directory : directories) {
            Path path = Path.of(directory, filename);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return directory;
            }
        }
        return null;
    }

    private String normalizeDirectory(String directory) {
        if (directory == null) {
            return null;
        }
        String trimmed = directory.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
