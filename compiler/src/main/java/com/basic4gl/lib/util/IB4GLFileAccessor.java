package com.basic4gl.lib.util;
////////////////////////////////////////////////////////////////////////////////
//IB4GLFileAccessor
//
/// Used to access files from a plugin.
/// Plugins can use this object to access files embedded in a standalone exe.
public interface IB4GLFileAccessor {
    /// Get filename to open for read.
    /// If the file is an embedded file then it will be extracted into a
    /// temporary file whose filename will be returned (in dst parameter).
    /// Otherwise simply returns the filename (in dst parameter).
    /// dst must be at least MAX_PATH characters in length.
	 void getFilenameForRead(String filename, String dst);
}
