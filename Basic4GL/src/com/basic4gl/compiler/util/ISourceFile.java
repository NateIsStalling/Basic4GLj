package com.basic4gl.compiler.util;

////////////////////////////////////////////////////////////////////////////////
	// ISourceFile
	//
	// / Interface to a source file.
	public abstract class ISourceFile {

		// / Return the next line of source code
		public abstract String GetNextLine();

		// / The filename
		public abstract String Filename();

		// / Return the line number. 0 = Top line of file.
		public abstract int LineNumber();

		// / True if reached End of File
		public abstract boolean Eof();

		// / Called when preprocessor is finished with the source file
		public abstract void Release();
	}