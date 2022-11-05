package com.basic4gl.compiler.util;


// //////////////////////////////////////////////////////////////////////////////
// ISourceFileServer
//
// / Serves source files by filename
public interface ISourceFileServer {
	// / Open source file and return interface.
	public abstract ISourceFile OpenSourceFile(String filename);
}