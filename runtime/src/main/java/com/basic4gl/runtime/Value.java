package com.basic4gl.runtime;

import com.basic4gl.runtime.util.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
    private int rawBits;

    public Value() { // Default constructor
        rawBits = 0;
    }

    Value(final Value v) { // Copy constructor
        rawBits = v.getIntVal();
    }

    public Value(Integer intVal) {
        setIntVal(intVal);
    }

    public Value(int intVal) {
        setIntVal(intVal);
    }

    public Value(Float realVal) {
        setRealVal(realVal);
    }

    public Value(float realVal) {
        setRealVal(realVal);
    }

    public int getIntVal() {
        return rawBits;
    }

    public float getRealVal() {
        return Float.intBitsToFloat(rawBits);
    }

    public void setIntVal(Integer val) {
        setIntVal(val.intValue());
    }

    public void setIntVal(int val) {
        rawBits = val;
    }

    public void setRealVal(Float val) {
        setRealVal(val.floatValue());
    }

    public void setRealVal(float val) {
        rawBits = Float.floatToRawIntBits(val);
    }

    public void setVal(Integer val) {
        setIntVal(val);
    }

    public void setVal(int val) {
        setIntVal(val);
    }

    public void setVal(Float val) {
        setRealVal(val);
    }

    public void setVal(float val) {
        setRealVal(val);
    }

    public void setVal(Value val) {
        rawBits = val.getIntVal();
    }

	//
	// Overrides for Object methods
	//

	@Override
	public String toString() {
		return this.rawBits.toString();
	}

	@Override
	public boolean equals(Object thatObject) {
		if (!(thatObject instanceof Value)) {
			return false;
		}
		Value that = (Value) thatObject;
		return this.rawBits == that.rawBits;
	}

	//
	// Streamable implementation.
	//

    public void streamOut(DataOutputStream stream) throws IOException {
        // There may be some potential cross-platform streaming issues because:
        // 1. We are unioning two data types together.
        // 2. We don't know at stream time what data type it is.
        // buffer.order( ByteOrder.LITTLE_ENDIAN);
        stream.writeInt(rawBits);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        rawBits = stream.readInt();
        return true;
    }
}
