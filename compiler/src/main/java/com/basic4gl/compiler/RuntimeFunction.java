package com.basic4gl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

/**
 * A runtime function
 */
public class RuntimeFunction implements Streamable {

	int prototypeIndex;

	public RuntimeFunction() {
		prototypeIndex = -1;
	}

	public RuntimeFunction(int prototypeIndex) {
		this.prototypeIndex = prototypeIndex;
	}

	@Override
	public void streamOut(DataOutputStream stream) throws IOException{
		Streaming.writeLong(stream, prototypeIndex);
	}
	@Override
	public boolean streamIn(DataInputStream stream) throws IOException{
		prototypeIndex = (int) Streaming.readLong(stream);
		return true;
	}

}
