package com.basic4gl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

/**
 * Recognised constants (e.g. "true", "false")
 */
public class Constant implements Streamable {
	/**
	 * Value type
	 */
    int basicType;

	/**
	 * Value of Constant if {@link #basicType} is {@value BasicValType#VTP_INT}
	 */
    int intValue;

	/**
	 * Value of Constant if {@link #basicType} is {@value BasicValType#VTP_REAL}
	 */
    float realValue;

	/**
	 * Value of Constant if {@link #basicType} is {@value BasicValType#VTP_STRING}
	 */
    String stringValue;

    public Constant() {
        basicType = BasicValType.VTP_STRING;
        stringValue = "";
        intValue = 0;
        realValue = 0f;
    }

    public Constant(String s) {
        basicType = BasicValType.VTP_STRING;
        stringValue = s;
        intValue = 0;
        realValue = 0f;
    }

    public Constant(int i) {
        basicType = BasicValType.VTP_INT;
        intValue = i;
        stringValue = "";
        realValue = 0f;
    }

    public Constant(float r) {
        basicType = BasicValType.VTP_REAL;
        realValue = r;
        stringValue = "";
        intValue = 0;
    }

    public Constant(double r) {
        basicType = BasicValType.VTP_REAL;
        realValue = (float) r;
        stringValue = "";
        intValue = 0;
    }

    public Constant(Constant c) {
        basicType = c.basicType;
        realValue = c.realValue;
        intValue = c.intValue;
        stringValue = c.stringValue;
    }

    public int getType() {
        return basicType;
    }

    public String ToString() {
        switch (basicType) {
            case BasicValType.VTP_INT:
                return String.valueOf(intValue);
            case BasicValType.VTP_REAL:
                return String.valueOf(realValue);
            case BasicValType.VTP_STRING:
                return stringValue;
            default:
                return "???";
        }
    }

    @Override
    public void streamOut(DataOutputStream stream) throws IOException {
        Streaming.writeLong(stream, basicType);

        switch (basicType) {
            case BasicValType.VTP_INT:
                Streaming.writeLong(stream, intValue);
                break;
            case BasicValType.VTP_REAL:
                Streaming.writeFloat(stream, realValue);
                break;
            case BasicValType.VTP_STRING:
                Streaming.writeString(stream, stringValue);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean streamIn(DataInputStream stream) throws IOException {
        basicType = (int) Streaming.readLong(stream);
        switch (basicType) {
            case BasicValType.VTP_INT:
                intValue = (int) Streaming.readLong(stream);
                break;
            case BasicValType.VTP_REAL:
                realValue = Streaming.readFloat(stream);
                break;
            case BasicValType.VTP_STRING:
                stringValue = Streaming.readString(stream);
                break;
            default:
                break;
        }
        return true;
    }
}
