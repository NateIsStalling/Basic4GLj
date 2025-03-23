package com.basic4gl.lib.util;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

/**
 * A single file, embedded into the executable
 */
public class EmbeddedFile {
	private String filename;
	private int length;
	private FileInputStream data;

	public EmbeddedFile() {
		filename = "";
		length = 0;
		data = null;
	}

	public EmbeddedFile(ByteBuffer rawData) {
		this(rawData, IntBuffer.allocate(1).put(0, 0));
	}

	public EmbeddedFile(ByteBuffer rawData, IntBuffer offset) {
		assertTrue(rawData != null);

		// Read filename length
		rawData.position(rawData.position() + offset.get());
		int nameLength = rawData.asIntBuffer().get(); // *((int *) (rawData + offset));
		offset.put(0, offset.get(0) + Integer.SIZE / Byte.SIZE);

		// Read filename
		byte[] name = new byte[nameLength];
		rawData.position(rawData.position() + offset.get());
		rawData.get(name);
		filename = new String(name, Charset.forName("UTF-8"));
		offset.put(0, offset.get(0) + nameLength);

		// Read length
		rawData.position(rawData.position() + offset.get());
		length = rawData.asIntBuffer().get(); // *((int *) (rawData + offset));
		offset.put(0, offset.get(0) + Integer.SIZE / Byte.SIZE);

		// Save pointer to data
		rawData.position(rawData.position() + offset.get());
		// m_data = rawData.position() + offset.get();
		offset.put(0, offset.get(0) + length);
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * Return file as a generic input stream
	 */
	public FileInputStream toStream() {
		return data;
	}

	public int length() {
		return length;
	}
}
