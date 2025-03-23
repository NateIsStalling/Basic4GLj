package com.basic4gl.runtime;

import com.basic4gl.runtime.util.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Used to store a single value.
 * Used internally by registers, stack entries and variables.
 *
 * Note: When storing a string, the actual value is stored in a separate
 * string array. {@link #getIntVal()} then stores the index of the string in
 * this array.
 * porting note: had directive `#pragma pack (push, 1)`
 */
public class Value implements Streamable {
private boolean isInt;
private Integer intVal;
private Float realVal;

public Value() { // Default constructor
	isInt = true;
	intVal = 0;
	realVal = 0f;
}

Value(final Value v) { // Copy constructor
	isInt = v.isInt;
	intVal = v.intVal;
	realVal = v.realVal;
}

public Value(Integer intVal) {
	setIntVal(intVal);
}

public Value(Float realVal) {
	setRealVal(realVal);
}

public int getIntVal() {
	return intVal.intValue();
}

public float getRealVal() {
	return realVal.floatValue();
}

public void setIntVal(Integer val) {
	isInt = true;
	intVal = val;
	realVal = val.floatValue();
}

public void setRealVal(Float val) {
	isInt = false;
	intVal = val.intValue();
	realVal = val;
}

public void setVal(Integer val) {
	isInt = true;
	setIntVal(val);
}

public void setVal(Float val) {
	isInt = false;
	setRealVal(val);
}

public void setVal(Value val) {
	isInt = val.isInt;
	intVal = val.intVal;
	realVal = val.realVal;
}

// Streaming
public void streamOut(DataOutputStream stream) throws IOException {

	// There may be some potential cross-platform streaming issues because:
	// 1. We are unioning two data types together.
	// 2. We don't know at stream time what data type it is.
	// buffer.order( ByteOrder.LITTLE_ENDIAN);
	if (isInt) {
	stream.writeInt(intVal);
	} else
	// stream.write(ByteBuffer.allocate(4).putFloat(m_realVal).array());
	{
	stream.writeFloat(realVal);
	}
}

public boolean streamIn(DataInputStream stream) throws IOException {
	byte[] b = new byte[Float.SIZE / Byte.SIZE];
	stream.read(b);
	intVal = ByteBuffer.wrap(b).getInt();
	realVal = ByteBuffer.wrap(b).getFloat();
	return true;
}
}
