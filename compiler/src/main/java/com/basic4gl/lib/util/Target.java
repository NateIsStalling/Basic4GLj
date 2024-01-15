package com.basic4gl.lib.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Launch target for a Basic4GL application; should include the main function 
 * or other entry point - depending on target platform.  
 * @author Nate
 *
 */
public interface Target extends Library, ITargetCommandLineOptions {

	/**
	 * Get list of properties the compiler can configure for
	 * building or running the application.
	 *
	 * @return Read-only list of properties for application
	 * @example Window size or target OS
	 */
	Configuration getSettings();

	/**
	 * @return Instance of target's property collection
	 */
	Configuration getConfiguration();

	/**
	 * @param config
	 */
	void setConfiguration(Configuration config);

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
}
