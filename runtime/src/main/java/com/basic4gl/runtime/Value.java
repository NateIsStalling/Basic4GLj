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
 */
public class Value implements Streamable {
	/**
	 * The value, stored as a 32-bit integer.
	 *
	 * These are interpreted as 32-bit floats, indexes into a separate string store,
	 * references to structure types, etc.
	 */
    private int rawBits;

	//
	// Constructors and conversion from Java values.
	//

    public Value() {
        rawBits = 0;
    }

    public Value(final Value v) {
        rawBits = v.getIntVal();
    }

	// Note: we use the unboxed versions here, 

    public Value(int intVal) {
        setIntVal(intVal);
    }

    public Value(float realVal) {
        setRealVal(realVal);
    }

	//
	// Accessors that interpret the bits stored.
	//

	// A regular old `int` is stored directly.

    public int getIntVal() {
        return rawBits;
    }

    public void setIntVal(Integer val) {
        setIntVal(val.intValue());
    }

    public void setIntVal(int val) {
        rawBits = val;
    }

	// `float` must be cast to and from the bit representation.

    public float getRealVal() {
        return Float.intBitsToFloat(rawBits);
    }

    public void setRealVal(Float val) {
        setRealVal(val.floatValue());
    }

    public void setRealVal(float val) {
        rawBits = Float.floatToRawIntBits(val);
    }

	// General `set` operation.

    public void setVal(Value val) {
        rawBits = val.getIntVal();
    }

    public void setVal(int val) {
        setIntVal(val);
    }

    public void setVal(float val) {
        setRealVal(val);
    }

	//
	// Overrides for Object methods
	//

	/** Convert the value to a string, for easy displaying to users. */
	@Override
	public String toString() {
		return "Value(int: " + this.getIntVal() + ", real:" + this.getRealVal() + ")";
	}

	/** Return true if this Value is equal to that Value. */
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

	/**
	 * Write this value to the stream given.
	 */
    public void streamOut(DataOutputStream stream) throws IOException {
        // There may be some potential cross-platform streaming issues because:
        // 1. We are unioning two data types together.
        // 2. We don't know at stream time what data type it is.
        // buffer.order( ByteOrder.LITTLE_ENDIAN);
        stream.writeInt(rawBits);
    }

	/**
	 * Read a value from the stream.
	 */
    public boolean streamIn(DataInputStream stream) throws IOException {
        rawBits = stream.readInt();
        return true;
    }
}
