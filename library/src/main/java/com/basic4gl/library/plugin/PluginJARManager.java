package com.basic4gl.library.plugin;

import com.basic4gl.runtime.plugin.*;
import com.basic4gl.runtime.util.Streaming;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.DirectoryStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Manages loading and maintaining plugin JARs
 */
public class PluginJARManager extends PluginManager {
    private String directory;
    private PlatformMetadataPolicy platformMetadataPolicy = PlatformMetadataPolicy.WARN_IDE_BLOCK_EXPORT;

    /// Find itor for JAR with filename
    private PluginLibrary findItor(String filename) {
        String lcase = filename.toLowerCase();

        for (PluginLibrary lib : plugins) {
            // Filter to JARs
            if (lib instanceof PluginJAR) {
                PluginJAR jar = (PluginJAR) lib;
                if (jar.getFilename().equals(lcase)) {
                    return lib;
                }
            }
        }

        // Not found
        return null;
    }

    public PluginJARManager(boolean isStandaloneExe) {
        super(isStandaloneExe);
        this.directory = "";
    }

    /// Iterate loaded JARs
    public Vector<PluginJAR> loadedJARs(){
        Vector<PluginJAR> result = new Vector<>();

        for (PluginLibrary lib : plugins) {
            if (lib instanceof PluginJAR) {
                result.add((PluginJAR) lib);
            }
        }

        return result;
    }

    /// Find and list JAR files.
    public Vector<PluginJARFile> getJARFiles() {
        Vector<PluginJARFile> result = new Vector<>();

        Path dirPath = Path.of(directory);
        if (!Files.isDirectory(dirPath)) {
            return result;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.jar")) {
            for (Path jarPath : stream) {
                String filename = jarPath.getFileName().toString();
                PluginJAR loadedJar = find(filename);

                if (loadedJar != null) {
                    result.add(loadedJar.getFileDetails());
                    continue;
                }

                PluginJARFile details = new PluginJARFile();
                details.setFilename(filename);
                details.setLoaded(false);
                details.setDescription(filename);
                details.setVersion(new PluginVersion(0, 0));
                result.add(details);
            }
        } catch (IOException e) {
            error = "Failed to scan plugin directory '" + directory + "': " + e.getMessage();
        }

        return result;
    }

    /// Find plugin JAR by name
    public PluginJAR find(String filename) {
        return (PluginJAR) findItor(filename);
    }

    /// Return true if a JAR file is loaded
    public boolean isLoaded(String filename) { return find(filename) != null; }

    public PlatformMetadataPolicy getPlatformMetadataPolicy() {
        return platformMetadataPolicy;
    }

    public void setPlatformMetadataPolicy(PlatformMetadataPolicy platformMetadataPolicy) {
        this.platformMetadataPolicy = platformMetadataPolicy == null
                ? PlatformMetadataPolicy.WARN_IDE_BLOCK_EXPORT
                : platformMetadataPolicy;
    }

    /// Load JAR. Returns true if JAR loaded successfully, or false if an error
    /// occurred (use Error() to retrieve text).
    public boolean loadJAR(String filename) {

        // Attempt to load JAR
        // First check that it's not already loaded
        if (isLoaded(filename)) {
            error = "A plugin JAR by this name is already loaded";
            return false;
        }

        // Load JAR
        PluginJAR jar = new PluginJAR(this, directory, filename, isStandaloneExe);
        if (jar.hasFailed()) {
            error = jar.getError();
            jar.dispose();
            return false;
        }

        // Add to list
        plugins.add(jar);

        return true;
    }

    /// Unload JAR. Returns true if JAR unloaded successfully.
    public boolean unloadJAR(String filename) {

        // Find JAR
        PluginLibrary i = findItor(filename);
        if (i == null) {
            error = "This plugin JAR is not loaded";
            return false;
        }

        // Check that JAR can be unloaded.
        // If the JAR owns objects that are currently used by other JARs, then it
        // cannot be unloaded before the other JARs have been.
        PluginJAR jar = (PluginJAR)i;
        if (jar.isReferenced()) {
            error = "The following plugin JAR(s) must be unloaded first:\r\n" + jar.describeReferencingPlugins();
            return false;
        }

        // Inform other JARs that this one is being unloaded
        for (PluginLibrary plugin: plugins) {
            if (jar != plugin && plugin instanceof PluginJAR) {
                ((PluginJAR)plugin).removeReferencingPlugin(jar);
            }
        }

        // Unregister all interfaces owned by this JAR
        for (Iterator<Map.Entry<String, PluginSharedInterface>> it = sharedInterfaces.entrySet().iterator();
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
        Streaming.writeLong(stream, plugins.size());
        for (int i = 0; i < plugins.size(); i++) {
            if (plugins.get(i) instanceof PluginJAR) {
                PluginJAR jar = (PluginJAR) (plugins.get(i));
                String filename = jar.getFileDetails().getFilename();
                Streaming.writeString(stream, filename);
                Streaming.writeLong(stream, jar.getFileDetails().getVersion().getMajorVersion());
                Streaming.writeLong(stream, jar.getFileDetails().getVersion().getMinorVersion());
            }
        }
    }
    public boolean streamIn(DataInputStream stream) throws IOException{

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
            if (!loadJAR(filename))
                return false;

            // Check version number
            PluginJAR jar = find(filename);
            if (!jar.getFileDetails().getVersion().equals(version)) {
                error = "Plugin JAR " + filename + " is the wrong version.\r\n" +
                        "Version is " + jar.getFileDetails().getVersion().toString() +
                        ", expected " + version.toString();
                return false;
            }
        }
        return true;
    }

    public void setDirectory(String directory) {
        this.directory = directory == null ? "" : directory;
        // Postfix a closing slash if necessary
        if (!this.directory.isEmpty() && this.directory.charAt(this.directory.length() - 1) != File.separatorChar)
            this.directory += File.separatorChar;
    }
}