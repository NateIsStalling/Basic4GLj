package com.basic4gl.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;

/// A runtime function
public class compRuntimeFunction {

	int prototypeIndex;

	public compRuntimeFunction() {
		prototypeIndex = -1;
	}

	public compRuntimeFunction(int _prototypeIndex) {
		prototypeIndex = _prototypeIndex;
	}

	void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteLong(buffer, prototypeIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void StreamIn(ByteBuffer buffer) {
		try {
			prototypeIndex = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
