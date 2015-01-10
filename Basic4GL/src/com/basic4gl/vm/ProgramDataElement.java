package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
//vmProgramDataElement
//
//General purpose program data (as allocated with "DATA" statement in BASIC).
class ProgramDataElement {
	int mBasicType;
	Value m_value;

	public int getType() {
		return mBasicType;
	}

	public Value getValue() {
		return m_value;
	}

	public void setType(int type) {
		mBasicType = type;
	}

	public void setValue(Value value) {
		m_value = value;
	}

	// Streaming
	public void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteLong(buffer, mBasicType);

			m_value.StreamOut(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			mBasicType = (int)Streaming.ReadLong(buffer);

			m_value.StreamIn(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}