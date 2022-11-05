package com.basic4gl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

/// A runtime function
public class RuntimeFunction implements Streamable {

	int prototypeIndex;

	public RuntimeFunction() {
		prototypeIndex = -1;
	}

	public RuntimeFunction(int _prototypeIndex) {
		prototypeIndex = _prototypeIndex;
	}
	@Override
	public void StreamOut(DataOutputStream stream) throws IOException{
		Streaming.WriteLong(stream, prototypeIndex);
	}
	@Override
	public boolean StreamIn(DataInputStream stream) throws IOException{
		prototypeIndex = (int) Streaming.ReadLong(stream);
		return true;
	}

}
