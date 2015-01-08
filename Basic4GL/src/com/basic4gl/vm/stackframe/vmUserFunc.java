package com.basic4gl.vm.stackframe;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;

public class vmUserFunc {
	public int prototypeIndex;
	public boolean implemented;
	public int programOffset;

	public vmUserFunc() {
	};

	public vmUserFunc(int _prototypeIndex, boolean _implemented) {
		this(_prototypeIndex, _implemented, -1);
	}

	public vmUserFunc(int _prototypeIndex, boolean _implemented,
			int _programOffset) {
		prototypeIndex = _prototypeIndex;
		implemented = _implemented;
		programOffset = _programOffset;
	}

	public void StreamOut(ByteBuffer buffer) {

		// Assume program is complete, i.e all functions are implemented, before
		// streaming occurs.
		assert (implemented);
		try {
			Streaming.WriteLong(buffer, prototypeIndex);
			Streaming.WriteLong(buffer, programOffset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			prototypeIndex = (int) Streaming.ReadLong(buffer);
			programOffset = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		implemented = true;
	}

}
