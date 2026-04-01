package com.basic4gl.compiler.plugin;

import com.basic4gl.runtime.util.Assert;

public class PluginJAR extends PluginLibrary {

    /// Filename (excluding path)
    private String filename;

    /// JAR handle.
    /// Note: This is really a HINSTANCE, but I'm damned if I'll include the entire
    /// <windows.h> in this header just for that defn.
    private int handle;

    /// JAR file details
    private PluginJARFile fileDetails;


    public PluginJAR(PluginJARManager manager, String path, String filename, boolean isStandaloneExe){
        super(manager);

        // Save filename
        filename = filename.toLowerCase();

        // Load JAR
        handle = (int) LoadLibrary((path + filename));
        if (handle == null)
            return;

        // Query file details
        fileDetails.setFilename(filename);
        fileDetails.setLoaded(true);
        if (!LoadFileDetails((HINSTANCE) handle, fileDetails))
            return;

        // Find Init function
        Basic4GLInitFunction init = (Basic4GLInitFunction) GetProcAddress((HINSTANCE) handle, "Basic4GL_Init");
        if (init == null)
            return;

        // Call init function to get plugin interface
        plugin = init();
        if (plugin == null) {
            return;
        }

        // Inform plugin it has been loaded. Let it register its functions.
        if (!plugin.load(this, isStandaloneExe)) {
            return;
        }
        completeFunction();

        // JAR successfully initialised
        failed = false;
    }
    public void dispose(){

        // Unload plugin first
        unload();

        // Unload JAR
        if (handle != null)
            FreeLibrary((HINSTANCE) handle);
    }


    public String getFilename() { return filename; }
    public PluginJARFile getFileDetails()    { return fileDetails; }

    /// Plugin description for error reporting etc
    public String getPluginDescription(){
        return fileDetails.getFilename();
    }

    /// Error text if failed
    public String getError() {

        if (handle == null)
            return "Could not load plugin: " + filename;

        else if (plugin == null)
            return "Could not initialise plugin: " + filename;

        else
            return super.getError();
    }

    private boolean LoadFileDetails(HINSTANCE jar, PluginJARFile details) {
        Assert.assertTrue(jar != null);

        // Find query function
        JAR_Basic4GL_QueryFunction query = (JAR_Basic4GL_QueryFunction) GetProcAddress(jar, "Basic4GL_Query");
        if (query == null)
            return false;

        // Query the JAR for details
        char[] detailStr = new char[256];
        memset(detailStr, 0, 256);
        PluginVersion version = details.getVersion();
        if (version == null) {
            version = new PluginVersion();
            details.setVersion(version);
        }
        version.setMajorVersion(0);
        version.setMinorVersion(0);

        int version = query(detailStr, &details.version.major, &details.version.minor);
        details.description = detailStr;

        // Note: Calling code must calculate details.filename and details.loaded itself
        return version >= BASIC4GL_JAR_MIN_VERSION && version <= BASIC4GL_JAR_VERSION;
    }
}
