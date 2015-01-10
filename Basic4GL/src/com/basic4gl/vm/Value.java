package com.basic4gl.vm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

////////////////////////////////////////////////////////////////////////////////
//Value
//
//Used to store a single value.
//Used internally by registers, stack entries and variables.

//#pragma pack (push, 1)
public class Value {
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
	void StreamOut(ByteBuffer buffer) {

		// There may be some potential cross-platform streaming issues because:
		// 1. We are unioning two data types together.
		// 2. We don't know at stream time what data type it is.
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		if (mIsInt)
			buffer.putInt(m_intVal);
		else
			buffer.putFloat(m_realVal);
	}

	void StreamIn(ByteBuffer buffer) {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		setIntVal(buffer.getInt());
	}
}