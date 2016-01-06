package com.basic4gl.lib.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.util.Function;

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
	 * @param vm
	 */
	void init(TomVM vm);

	/**
	 * Initialize miscellaneous values used in the library
	 * before the compiler is started.
	 * @param comp
	 */
	void init(TomBasicCompiler comp);

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
