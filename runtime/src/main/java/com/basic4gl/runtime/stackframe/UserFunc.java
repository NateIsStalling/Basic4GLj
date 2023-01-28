package com.basic4gl.runtime.stackframe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

import static com.basic4gl.runtime.util.Assert.assertTrue;

public class UserFunc implements Streamable{
	public int mPrototypeIndex;
	public boolean mImplemented;
	public int mProgramOffset;

	public UserFunc() {
	}

    public UserFunc(int prototypeIndex, boolean implemented) {
		this(prototypeIndex, implemented, -1);
	}

	public UserFunc(int prototypeIndex, boolean implemented, int programOffset) {
		mPrototypeIndex = prototypeIndex;
		mImplemented = implemented;
		mProgramOffset = programOffset;
	}

	public void StreamOut(DataOutputStream stream) throws IOException{
		// Assume program is complete, i.e all functions are implemented, before
		// streaming occurs.
		assertTrue(mImplemented);

		Streaming.WriteLong(stream, mPrototypeIndex);
		Streaming.WriteLong(stream, mProgramOffset);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{
		mPrototypeIndex = (int) Streaming.ReadLong(stream);
		mProgramOffset = (int) Streaming.ReadLong(stream);

		mImplemented = true;

		return true;
	}

}
