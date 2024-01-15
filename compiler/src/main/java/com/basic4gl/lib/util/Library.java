package com.basic4gl.lib.util;

import java.util.List;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.runtime.TomVM;

public interface Library {

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
	 * Initialize miscellaneous values used in the library
	 * before the virtual machine is started.
	 * @param vm Virtual Machine instance
	 * @param args Arguments provided to the application's main function
	 */
	void init(TomVM vm, String[] args);

	/**
	 * Initialize miscellaneous values used in the library
	 * before the compiler is started.
	 * @param comp Compiler instance
	 */
	void init(TomBasicCompiler comp);

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
