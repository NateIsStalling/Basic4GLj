package com.basic4gl.util;

import com.basic4gl.lib.util.CallbackMessage;
import com.basic4gl.lib.util.DebuggerCallbacks;
import com.basic4gl.lib.util.Library;

/**
 * Interface to help manage the lifecycle of a thread that drives the VM
 */
public interface IVMDriver {

	//Initialize settings and variables
	void activate();
	//Set defaults
	void reset();

	//Begin thread and show window
	void start(DebuggerCallbacks debugger);
	//Close or minimize window
	void hide();
	//Stop VM; window may enter an idle state or close
	void stop();

	//Check window state
	boolean isFullscreen();
	boolean isVisible();
	boolean isClosing();

	//Respond to window events
	void handleEvents();

	//Run the VM for a number of steps and report progress
    CallbackMessage driveVM(int steps);

	//Called before VM loop is started
    void onPreExecute();
	//Called after VM loop completes; keep window in an idle state until closed
    void onPostExecute();
	//Occurs before thread ends
    void onFinally();

	//Initialize library; used to check if a library implements interfaces compatible with the driver
	void initLibrary(Library lib);
}
