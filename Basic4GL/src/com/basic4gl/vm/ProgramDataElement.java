package com.basic4gl.vm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.util.Streamable;
import com.basic4gl.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
//vmProgramDataElement
//
//General purpose program data (as allocated with "DATA" statement in BASIC).
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
	public void StreamOut(DataOutputStream stream) throws IOException{
		Streaming.WriteLong(stream, mBasicType);
		mValue.StreamOut(stream);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{
		mBasicType = (int)Streaming.ReadLong(stream);
		mValue.StreamIn(stream);
		return true;
	}
}