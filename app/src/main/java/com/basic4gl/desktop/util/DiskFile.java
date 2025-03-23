package com.basic4gl.desktop.util;

import com.basic4gl.compiler.util.ISourceFile;
import java.io.File;
import java.io.FileInputStream;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Disk file implementation of ISourceFile
 */
public class DiskFile implements ISourceFile {
	private FileInputStream fileInputStream;
	private FileChannel channel;

	private ByteBuffer buffer;
	private final String filename;

	private long size;
	private int lineNo;

	public DiskFile(String filename) {
		this.filename = filename;
		if (new File(filename).exists()) {
			try {

				fileInputStream = new FileInputStream(this.filename);
				channel = fileInputStream.getChannel();
				size = channel.size();

				buffer = ByteBuffer.allocate((int) size);
				channel.read(buffer);

				channel.close();
				fileInputStream.close();

				buffer.rewind();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				channel = null;
				fileInputStream = null;
			}
		} else {
			channel = null;
			fileInputStream = null;
		}
		lineNo = 0;
	}

	public boolean hasError() {
		return (fileInputStream == null);
	}

	// ISourceFile methods
	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public int getLineNumber() {
		return lineNo;
	}

	@Override
	public String getNextLine() {
		if (!isEof()) {
			lineNo++;
			byte[] lineBuffer = new byte[65536];
			boolean crFlag = false; // Carriage return flag
			byte b = 0;
			int i = 0;

			// Parse mBuffer until it reaches a new line character
			do {
				b = buffer.get();
				crFlag = (b == '\r'); // Set return character flag

				if (b != '\n' && !crFlag) {
					lineBuffer[i] = b;
				}

				// Check if return character is followed by a new line character
				if (crFlag) {
					b = buffer.get();
					// If return character was not followed by a new line char or null char
					// then reset the buffer's position to before the last byte read
					if (b != '\n' && b != 0) {
						buffer.position(buffer.position() - 1);
					}
				}
				i++;
			} while (buffer.hasRemaining() && b != '\n' && b != 0 && !crFlag);

			return new String(Arrays.copyOfRange(lineBuffer, 0, i));
		} else {
			return "";
		}
	}

	@Override
	public boolean isEof() {
		return hasError() || !buffer.hasRemaining();
	}

	@Override
	public void release() {
		try {

			this.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
