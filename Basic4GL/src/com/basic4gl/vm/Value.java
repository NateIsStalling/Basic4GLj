package com.basic4gl.vm;

import com.basic4gl.util.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

////////////////////////////////////////////////////////////////////////////////
//Value
//
//Used to store a single value.
//Used internally by registers, stack entries and variables.

//#pragma pack (push, 1)
public class Value implements Streamable{
	private boolean mIsInt;
	private Integer m_intVal;
	private Float m_realVal;

	// Note: When storing a string, the actual value is stored in a separate
	// string array. mIntVal then stores the index of the string in
	// this array.

	public int getIntVal() {
		return m_intVal.intValue();
	}

	public float getRealVal() {
		return m_realVal.floatValue();
	}

	public void setIntVal(Integer val) {
		mIsInt = true;
		m_intVal = val;
		m_realVal = val.floatValue();

	}

	public void setRealVal(Float val) {
		mIsInt = false;
		m_intVal = val.intValue();
		m_realVal = val;

	}

	public void setVal(Integer val) {
		mIsInt = true;
		setIntVal(val);
	}

	public void setVal(Float val) {
		mIsInt = false;
		setRealVal(val);
	}
	public void setVal(Value val) {
		mIsInt = val.mIsInt;
		m_intVal = val.m_intVal;
		m_realVal = val.m_realVal;
	}
	public Value() { // Default constructor
		mIsInt = true;
		m_intVal = 0;
		m_realVal = 0f;
	}

	Value(final Value v) { // Copy constructor
		mIsInt = v.mIsInt;
		m_intVal = v.m_intVal;
		m_realVal = v.m_realVal;
	}

	public Value(Integer intVal) {
		setIntVal(intVal);
	}

	public Value(Float realVal) {
		setRealVal(realVal);
	}

	// Streaming
	public void StreamOut(DataOutputStream stream) throws IOException{

		// There may be some potential cross-platform streaming issues because:
		// 1. We are unioning two data types together.
		// 2. We don't know at stream time what data type it is.
		//buffer.order( ByteOrder.LITTLE_ENDIAN);
		if (mIsInt)
			stream.writeInt(m_intVal);
		else
			//stream.write(ByteBuffer.allocate(4).putFloat(m_realVal).array());
			stream.writeFloat(m_realVal);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{
		byte[] b = new byte[Float.SIZE / Byte.SIZE];
		stream.read(b);
		m_intVal = ByteBuffer.wrap(b).getInt();
		m_realVal = ByteBuffer.wrap(b).getFloat();
		return true;
	}
}