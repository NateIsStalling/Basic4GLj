package com.basic4gl.lib.util;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of embedded files, keyed by relative filename
 */
public class EmbeddedFiles {
private String parentDirectory; // Parent directory
//    private Map<String,EmbeddedFile> m_files = new HashMap<>();

private Map<String, File> tempFiles = new HashMap<>();

public URL getResource(String filename) {
	return this.getClass().getClassLoader().getResource(filename);
}

public boolean isStored(String filename) {
	return getResource(filename) != null;
}

// Find stream.
// Caller must free
public FileInputStream open(String filename) // Opens file. Returns NULL if not present.
	{
	if (isStored(filename)) {
	try {
		File file = new File(getResource(filename).toURI());
		return new FileInputStream(file);
	} catch (URISyntaxException | FileNotFoundException e) {
		e.printStackTrace();
	}
	}
	return null;
}

public FileInputStream openOrLoad(
	String
		filename) // Opens file. Falls back to disk if not present. Returns NULL if not present OR
		// on disk
	{

	// Try embedded files first
	FileInputStream result = open(filename);
	if (result == null) {

	// Otherwise try to load from file
	FileInputStream diskFile = null;
	try {
		diskFile = new FileInputStream(new File(parentDirectory, filename));
		result = diskFile;
	} catch (FileNotFoundException e) {
		System.out.println("missing parent: " + parentDirectory);
		e.printStackTrace();
		result = null;
	}
	}
	return result;
}

// Routines

// Copy a InputStream into a OutputStream
public static void copyStream(InputStream src, OutputStream dst) {
	copyStream(src, dst, -1);
}

public static void copyStream(InputStream src, OutputStream dst, long len) {

	// Copy stream to stream
	ByteBuffer buffer = ByteBuffer.allocate(0x4000);
	while (len > 0x4000 || len < 0) {
	buffer.mark();
	try {
		src.read(buffer.array(), buffer.arrayOffset(), 0x4000);
		buffer.reset();
		dst.write(buffer.array(), buffer.arrayOffset(), 0x4000);
		len -= 0x4000;
	} catch (IOException e) {
		len = 0;
		e.printStackTrace();
		break;
	}
	}
	if (len > 0) {
	try {
		src.read(buffer.array(), buffer.arrayOffset(), (int) len);
		dst.write(buffer.array(), buffer.arrayOffset(), (int) len);
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
}

// Create embedded representation of stream
static boolean embedFile(String parent, String filename, OutputStream stream) {

	// Open file
	File file;
	FileInputStream fileStream = null;
	try {
	file = new File(parent, filename);
	fileStream = new FileInputStream(file);
	} catch (FileNotFoundException e) {
	return false;
	}

	// Convert filename to relative
	String relName = new File(parent, filename).getAbsolutePath();

	// Calculate lengths
	int nameLen = relName.length() + 1; // +1 for 0 terminator
	LongBuffer fileLen = LongBuffer.allocate(1).put(0, file.length());

	// Write data to stream
	try {
	stream.write(ByteBuffer.allocate(4).putInt(nameLen).array());
	stream.write(relName.getBytes(StandardCharsets.UTF_8));
	stream.write(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(fileLen.get()).array());
	copyStream(new FileInputStream(file), stream, fileLen.get());
	return true;
	} catch (Exception e) {
	e.printStackTrace();
	return false;
	}
}

String extractStoredFile(String filename, String mParent) {
	filename = FileUtil.separatorsToSystem(filename);
	Exception exception = null;
	File file = null;

	InputStream in = null;
	OutputStream out = null;
	try {
	// TODO keep cache of temp filenames
	URL resource = getResource(filename);
	File tempDirectory = FileUtil.getTempDirectory(mParent);

	file = File.createTempFile("temp", new File(filename).getName(), tempDirectory);
	System.out.println("file: " + filename);
	System.out.println("Created temp file: " + file.getAbsolutePath());
	file.deleteOnExit();

	in = resource.openStream();
	out = new FileOutputStream(file);

	byte[] buffer = new byte[1024];
	int length;
	while ((length = in.read(buffer)) > 0) {
		out.write(buffer, 0, length);
	}

	} catch (IOException e) {
	e.printStackTrace();
	exception = e;
	} finally {
	if (in != null) {
		try {
		in.close();
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	if (out != null) {
		try {
		out.close();
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	}
	if (file == null || exception != null) {
	return "";
	}
	// Return new filename
	return new File(mParent).toURI().relativize(file.toURI()).getPath();
}

public void setParentDirectory(String parent) {
	parentDirectory = parent;
}

public String getParentDirectory() {
	return parentDirectory;
}
}
