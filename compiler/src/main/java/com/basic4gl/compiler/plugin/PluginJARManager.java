package com.basic4gl.compiler.plugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import static com.basic4gl.runtime.util.Streaming.*;

/**
 * Manages loading and maintaining plugin JARs
 */
public class PluginJARManager extends PluginManager {
    private String directory;

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

    public PluginJARManager(String directory, boolean isStandaloneExe) {
        super(isStandaloneExe);
        this.directory = directory;
        // Postfix a closing slash if necessary
        if (directory != "" && directory[directory.length() - 1] != File.separatorChar)
            directory += File.separatorChar;
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

        // Scan directory for files
        std::string searchPath = directory + "*.jar";
        WIN32_FIND_DATA seekData;
        HANDLE seekHandle = FindFirstFile(searchPath.c_str(), &seekData);
        if (seekHandle == INVALID_HANDLE_VALUE)
            return result;

        // Scan JAR files in directory and add to list
        boolean foundFile = true;
        while (foundFile) {

            // Open the JAR and query it for details
            HINSTANCE jar = LoadLibrary((directory + seekData.cFileName).c_str());
            if (jar != null) {

                // Query details from JAR
                PluginJARFile details = new PluginJARFile();
                details.setFilename(seekData.cFileName);
                details.setLoaded(isLoaded(details.filename));
                if (LoadFileDetails(jar, details)) {

                    // Add JAR file entries
                    result.add(details);
                }

                // Unload the JAR
                FreeLibrary(jar);
            }

            // Find next JAR in directory
            foundFile = FindNextFile(seekHandle, &seekData);
        }

        // File search finished
        FindClose(seekHandle);

        return result;
    }

    /// Find plugin JAR by name
    public PluginJAR find(String filename) {
        return (PluginJAR) findItor(filename);
    }

    /// Return true if a JAR file is loaded
    public boolean isLoaded(String filename) { return find(filename) != null; }

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
        for (Iterator<Map.Entry<String, PluginJARSharedInterface>> it = sharedInterfaces.entrySet().iterator();
             it.hasNext(); ) {
            Map.Entry<String, PluginJARSharedInterface> entry = it.next();
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
        writeLong(stream, plugins.size());
        for (int i = 0; i < plugins.size(); i++) {
            if (plugins.get(i) instanceof PluginJAR) {
                PluginJAR jar = (PluginJAR) (plugins.get(i));
                String filename = jar.getFileDetails().getFilename();
                writeString(stream, filename);
                writeLong(stream, jar.getFileDetails().getVersion().getMajorVersion());
                writeLong(stream, jar.getFileDetails().getVersion().getMinorVersion());
            }
        }
    }
    public boolean streamIn(DataInputStream stream) throws IOException{

        // Clear out any existing plugins
        clear();

        long count = readLong(stream);
        for (int i = 0; i < count; i++) {

            // Read file details
            String filename = readString(stream);
            PluginVersion version = new PluginVersion();
            version.setMajorVersion(readLong(stream));
            version.setMinorVersion(readLong(stream));

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
}