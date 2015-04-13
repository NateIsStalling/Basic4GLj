package com.basic4gl.compiler.util;

import com.basic4gl.util.Mutable;

////////////////////////////////////////////////////////////////////////////////
// ILineNumberMapping
//
// / Used to map line numbers in source files to line numbers of the main
// file
// / that is built after the pre-processor expands includes.
// / Mainly used for debugging, so that breakpoint and IP positions can be
// / displayed in the correct positions in the correct source files.
public abstract class ILineNumberMapping {

	// / Return the filename and line number corresponding to a main line
	// number.
	public abstract void SourceFromMain(Mutable<String> filename, Mutable<Integer> fileLineNo,
			int lineNo);

	// / Return line number within a specific file that corresponds to a
	// main
	// line number.
	// / Returns -1 if line number does not correspond to the file
	// specified.
	public abstract int SourceFromMain(String filename, int lineNo);

	// / Returns main line number from file and line number
	public abstract int MainFromSource(String filename, int fileLineNo);
}