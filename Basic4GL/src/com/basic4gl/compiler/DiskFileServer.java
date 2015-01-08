package com.basic4gl.compiler;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;

////////////////////////////////////////////////////////////////////////////////
//DiskFileServer
//
/// Disk file implementation of ISourceFileServer
public class DiskFileServer extends ISourceFileServer {
	// ISourceFileServer methods
	@Override
	public ISourceFile OpenSourceFile(String filename) {
		DiskFile file = new DiskFile(filename);
		if (file.Fail()) {
			file = null;
			return null;
		}
		return file;
	}
}