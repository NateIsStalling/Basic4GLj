package com.basic4gl.compiler.util;

import com.basic4gl.lib.util.CallbackMessage;
import com.basic4gl.lib.util.Library;

/**
 * Interface to help manage the lifecycle of a thread that drives the VM
 */
public interface IVMDriver {

	/**
	 * Initialize settings and variables
	 */
	void activate();

	/**
	 * Set defaults
	 */
	void reset();

	/**
	 * Begin program and show window
	 */
	void start();

	/**
	 * Close or minimize window
	 */
	void hide();

	/**
	 * Stop VM; window may enter an idle state or close
	 */
	void stop();

	/**
	 * Stop VM and close window
	 */
	void terminate();

	/**
	 * Check window state
	 * @return true if fullscreen
	 */
	boolean isFullscreen();

	/**
	 * Check window state
	 * @return true if visible
	 */
	boolean isVisible();

	/**
	 * Check window state
	 * @return true if application is closing
	 */
	boolean isClosing();

	/**
	 * Respond to window events
	 * @return returns true if no error
	 */
	boolean handleEvents();

	/**
	 * Run the VM for a number of steps and report progress
	 * @return CallbackMessage with progress status
	 */
	CallbackMessage driveVM(int steps);

	/**
	 * Called before VM loop is started
	 */
	void onPreExecute();

	/**
	 * Called after VM loop completes; keep window in an idle state until closed
	 */
	void onPostExecute();

	/**
	 * Occurs before thread ends
	 */
	void onFinally();

	/**
	 * Initialize library;
	 * used to check if a library implements interfaces compatible with the driver
	 */
	void initLibrary(Library lib);
}
