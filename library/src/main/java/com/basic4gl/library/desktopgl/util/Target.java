package com.basic4gl.library.desktopgl.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Launch target for a Basic4GL application; should include the main function
 * or other entry point - depending on target platform.
 * @author Nate
 *
 */
public interface Target {

    /**
     * @return Library module name
     */
    String name();

    /**
     * Brief description of library module
     * @return Description
     */
    String description();

    /**
     * Get list of properties the compiler can configure for
     * building or running the application.
     *
     * @return Read-only list of properties for application
     * @example Window size or target OS
     */
    com.basic4gl.language.core.runtime.Configuration getSettings();

    /**
     * @return Instance of target's property collection
     */
    com.basic4gl.language.core.runtime.Configuration getConfiguration();

    /**
     * @param config
     */
    void setConfiguration(com.basic4gl.language.core.runtime.Configuration config);

    /**
     * Load target's configuration from a file system
     *
     * @throws Exception
     */
    void loadConfiguration(InputStream stream) throws Exception;

    /**
     * Save target's configuration to a file system
     *
     * @throws Exception
     */
    void saveConfiguration(OutputStream stream) throws Exception;

    /**
     * Call when pausing application to store the VM's state.
     *
     * @return VM state as stream
     */
    void saveState(OutputStream stream) throws Exception;

    /**
     * Call when starting application to resume application from a stored VM state
     *
     * @param stream Stored VM state
     */
    void loadState(InputStream stream) throws Exception;

    /**
     * Do any necessary cleanup post-execution
     */
    void cleanup();

    /**
     *
     * @return List of files required by library to include when exported
     */
    List<String> getDependencies();

    /**
     *
     * @return List of files or directories required by library to add to the class path
     */
    List<String> getClassPathObjects();
}
