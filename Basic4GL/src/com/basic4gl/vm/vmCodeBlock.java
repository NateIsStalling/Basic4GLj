package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.stackframe.vmRuntimeFunction;

////////////////////////////////////////////////////////////////////////////////
//  vmCodeBlock
//
/// Represents a block of code.
/// The program is code block 0. Any other files/strings compiled at run time
/// are also separate code blocks.
public class vmCodeBlock {
	public int programOffset; // -1 if code block is invalid (e.g. because of compile
						// error)
	public Vector<vmRuntimeFunction> runtimeFunctions;

	public vmCodeBlock() {
		programOffset = -1;
	}

	public void SetLengthAtLeast(int length) {
		if (runtimeFunctions.size() < length)
			runtimeFunctions.setSize(length);
	}

	public vmRuntimeFunction GetRuntimeFunction(int index) {
		assert (index >= 0);
		SetLengthAtLeast(index + 1);
		return runtimeFunctions.get(index);
	}

	public void StreamOut(ByteBuffer buffer) {

		try {
			Streaming.WriteLong(buffer, programOffset);
			Streaming.WriteLong(buffer, runtimeFunctions.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (vmRuntimeFunction f : runtimeFunctions)
			f.StreamOut(buffer);
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			programOffset = (int) Streaming.ReadLong(buffer);
			int count = (int) Streaming.ReadLong(buffer);
			runtimeFunctions.setSize(count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (vmRuntimeFunction f : runtimeFunctions)
			f.StreamIn(buffer);
	}
}