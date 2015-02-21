package com.basic4gl.lib.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Launch target for a Basic4GL application; should include the main function 
 * or other entry point - depending on target platform.  
 * @author Nate
 *
 */
public interface Target extends Library {
	public abstract String getFileDescription();
	public abstract String getFileExtension();

	public abstract boolean isRunnable();
	public abstract void reset();
	public abstract void activate();
	
	public abstract void show(TaskCallback callbacks);
	public abstract void hide();
	public abstract void stop();
	
	public abstract boolean isFullscreen();
	public abstract boolean isVisible();
	public abstract boolean isClosing();

	/**
	 * 
	 * @return Window for callbacks
	 */
	public abstract Object getContext();
	/**
	 * 
	 * @return GL Context for callbacks
	 */
	public abstract Object getContextGL();

	/**
	 * Get list of properties the compiler can configure for
	 * building or running the application.
	 * @example Window size or target OS
	 * @return Read-only list of properties for application
	 */
	public abstract Configuration getSettings();

	/**
	 *
	 * @return Instance of target's property collection
	 */
	public abstract Configuration getConfiguration();

	/**
	 *
	 * @param config
	 */
	public abstract void setConfiguration(Configuration config);

	/**
	 * Load target's configuration from a file system
	 * @throws Exception
	 */
	public abstract void loadConfiguration(InputStream stream) throws Exception;

	/**
	 * Save target's configuration to a file system
	 * @throws Exception
	 */
	public abstract void saveConfiguration(OutputStream stream) throws Exception;

	/**
	 * Call when pausing application to store the VM's state.
	 * @return VM state as stream
	 */
	public abstract void saveState(OutputStream stream) throws Exception;

	/**
	 * Call when starting application to resume application from a stored VM state
	 * @param stream Stored VM state
	 */
	public abstract void loadState(InputStream stream) throws Exception;

	/**
	 * Bundle the target in a stream to store a standalone copy on the filesystem.
	 * Used by the compiler.
	 * @return
	 */
	public abstract boolean export(OutputStream stream, TaskCallback callback) throws Exception;


}
