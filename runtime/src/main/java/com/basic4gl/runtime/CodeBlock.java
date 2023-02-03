package com.basic4gl.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.stackframe.RuntimeFunction;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * CodeBlock
 * Represents a block of code.
 * The program is code block 0. Any other files/strings compiled at run time
 * are also separate code blocks.
 */
public class CodeBlock implements Streamable{
	public int programOffset; // -1 if code block is invalid (e.g. because of compile error)
	public Vector<RuntimeFunction> runtimeFunctions = new Vector<RuntimeFunction>();

	public CodeBlock() {
		programOffset = -1;
	}

	public void SetLengthAtLeast(int length) {
		if (runtimeFunctions.size() < length) {
            runtimeFunctions.setSize(length);
        }
	}

	public RuntimeFunction GetRuntimeFunction(int index) {
		assertTrue(index >= 0);
		SetLengthAtLeast(index + 1);
		return runtimeFunctions.get(index);
	}

	public void streamOut(DataOutputStream stream) throws IOException{

		Streaming.WriteLong(stream, programOffset);
		Streaming.WriteLong(stream, runtimeFunctions.size());

		for (RuntimeFunction f : runtimeFunctions) {
            f.streamOut(stream);
        }
	}

	public boolean streamIn(DataInputStream stream) throws IOException {

		programOffset = (int) Streaming.ReadLong(stream);
		int count = (int) Streaming.ReadLong(stream);
		runtimeFunctions.setSize(count);

		for (int i = 0; i < count; i++){
			runtimeFunctions.set(i, new RuntimeFunction());
			runtimeFunctions.get(i).streamIn(stream);
		}

		return true;
	}
}