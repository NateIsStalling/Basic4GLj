package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.stackframe.RuntimeFunction;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Represents a block of code.
 * The program is code block 0. Any other files/strings compiled at run time
 * are also separate code blocks.
 */
public class CodeBlock implements Streamable {
	/**
	 * -1 if code block is invalid (e.g. because of compile error)
	 */
	public int programOffset;

	public Vector<RuntimeFunction> runtimeFunctions = new Vector<>();

	public CodeBlock() {
		programOffset = -1;
	}

	public void setLengthAtLeast(int length) {
		int size = runtimeFunctions.size();
		if (size < length) {
			runtimeFunctions.setSize(length);
			for (int i = size; i < length; i++) {
				runtimeFunctions.set(i, new RuntimeFunction());
			}
		}
	}

	public RuntimeFunction getRuntimeFunction(int index) {
		assertTrue(index >= 0);
		setLengthAtLeast(index + 1);
		return runtimeFunctions.get(index);
	}

	public void streamOut(DataOutputStream stream) throws IOException {

		Streaming.writeLong(stream, programOffset);
		Streaming.writeLong(stream, runtimeFunctions.size());

		for (RuntimeFunction f : runtimeFunctions) {
			f.streamOut(stream);
		}
	}

	public boolean streamIn(DataInputStream stream) throws IOException {

		programOffset = (int) Streaming.readLong(stream);
		int count = (int) Streaming.readLong(stream);
		runtimeFunctions.setSize(count);

		for (int i = 0; i < count; i++) {
			runtimeFunctions.set(i, new RuntimeFunction());
			runtimeFunctions.get(i).streamIn(stream);
		}

		return true;
	}
}
