package com.basic4gl.runtime.stackframe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
//RuntimeFunction
//
public class RuntimeFunction implements Streamable{

	// / Index of implementing function, or -1 if none.
	public int functionIndex;

	public RuntimeFunction() {
		functionIndex = -1;
	}

	public RuntimeFunction(int _functionIndex) {
		functionIndex = _functionIndex;
	}

	@Override
	public void StreamOut(DataOutputStream stream) throws IOException{
		Streaming.WriteLong(stream, functionIndex);
	}

	@Override
	public boolean StreamIn(DataInputStream stream) throws IOException{
		functionIndex = (int) Streaming.ReadLong(stream);

		return true;
	}
}