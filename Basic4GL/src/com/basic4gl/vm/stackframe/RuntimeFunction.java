package com.basic4gl.vm.stackframe;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
//RuntimeFunction
//
public class RuntimeFunction {

	// / Index of implementing function, or -1 if none.
	public int functionIndex;

	public RuntimeFunction() {
		functionIndex = -1;
	}

	public RuntimeFunction(int _functionIndex) {
		functionIndex = _functionIndex;
	}

	public void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteLong(buffer, functionIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			functionIndex = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}