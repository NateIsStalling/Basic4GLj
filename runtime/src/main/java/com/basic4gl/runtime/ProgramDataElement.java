package com.basic4gl.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

/**
 * General purpose program data (as allocated with "DATA" statement in BASIC).
 */
class ProgramDataElement implements Streamable{
	int mBasicType;
	Value mValue;

	public int getType() {
		return mBasicType;
	}

	public Value getValue() {
		return mValue;
	}

	public void setType(int type) {
		mBasicType = type;
	}

	public void setValue(Value value) {
		mValue = value;
	}

	// Streaming
	public void streamOut(DataOutputStream stream) throws IOException{
		Streaming.WriteLong(stream, mBasicType);
		mValue.streamOut(stream);
	}

	public boolean streamIn(DataInputStream stream) throws IOException{
		mBasicType = (int)Streaming.ReadLong(stream);
		mValue.streamIn(stream);
		return true;
	}
}