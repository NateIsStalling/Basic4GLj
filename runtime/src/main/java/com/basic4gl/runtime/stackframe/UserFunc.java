package com.basic4gl.runtime.stackframe;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserFunc implements Streamable {
	public int mPrototypeIndex;
	public boolean mImplemented;
	public int mProgramOffset;

	public UserFunc() {}

	public UserFunc(int prototypeIndex, boolean implemented) {
		this(prototypeIndex, implemented, -1);
	}

	public UserFunc(int prototypeIndex, boolean implemented, int programOffset) {
		mPrototypeIndex = prototypeIndex;
		mImplemented = implemented;
		mProgramOffset = programOffset;
	}

	public void streamOut(DataOutputStream stream) throws IOException {
		// Assume program is complete, i.e all functions are implemented, before
		// streaming occurs.
		assertTrue(mImplemented);

		Streaming.writeLong(stream, mPrototypeIndex);
		Streaming.writeLong(stream, mProgramOffset);
	}

	public boolean streamIn(DataInputStream stream) throws IOException {
		mPrototypeIndex = (int) Streaming.readLong(stream);
		mProgramOffset = (int) Streaming.readLong(stream);

		mImplemented = true;

		return true;
	}
}
