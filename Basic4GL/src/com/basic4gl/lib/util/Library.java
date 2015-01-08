package com.basic4gl.lib.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.compConstant;
import com.basic4gl.util.compFuncSpec;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.util.Function;

public interface Library {
	/**
	 * @return True if library is a build target
	 */
	public abstract boolean isTarget();
	
	/**
	 * @return Library name
	 */
	public abstract String name();
	/**
	 * @return Version name
	 */
	public abstract String version();
	/**
	 * Description of library; do not include list of functions 
	 * or constants - they will be displayed separately.
	 * @return Description
	 */
	public abstract String description();
	
	/**
	 * @return Author name
	 */
	public abstract String author();
	
	/**
	 * @return Author contact info
	 */
	public abstract String contact();
		
	/**
	 * Unique identifier for library for compatibility or qualifying conflicts
	 * If your library includes a target please include it's id in a README file
	 * so other developers can add it to the compatibility lists of their libraries.
	 * @return Library compatibility identifier
	 */
	public abstract String id();
	
	/**
	 * List of id's of compatible targets
	 * @return List of known compatible targets
	 */
	public abstract String[] compat();
	
	/**
	 * Initialize miscellaneous values used in the library
	 * before the virtual machine is started.
	 * @param vm
	 */
	public abstract void init(TomVM vm);
	
	/**
	 * Returns a list of constants for the compiler to use
	 * @param comp
	 */
	public abstract Map<String, compConstant> constants();
	
	/**
	 * Initialize and register functions and constants with the compiler.
	 * comp.AddFunction() comp.AddConstant()
	 * @param comp
	 */
	public abstract Map<String, List<Function>> functions();
	
	/**
	 * Initialize and register functions and constants with the compiler.
	 * comp.AddFunction() comp.AddConstant()
	 * @param comp
	 */
	public abstract Map<String, List<compFuncSpec>> specs();
	
	/**
	 * Documentation for functions and constants included in library; 
	 * @return HashMap<name, description>
	 */
	public abstract HashMap<String, String> getTokenTips();
	
	/**
	 * 
	 * @return List of files required by library
	 */
	public abstract List<String> getDependencies();
	/**
	 * 
	 * @return List of files required by library to add to the class path
	 */
	public abstract List<String> getDependenciesForClassPath();
}
