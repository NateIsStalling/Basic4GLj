package com.basic4gl.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.types.ValType;

// //////////////////////////////////////////////////////////////////////////////
// Instruction
// #pragma pack (push, 1)
public class Instruction implements Streamable{

	// Note: Instruction size = 12 bytes.
	// Ordering of member fields is important, as the two 4 byte members
	// are first (and hence aligned to a 4 byte boundary).
	// mSourceChar is next, and aligned to a 2 byte boundary.
	// Single byte fields are last, as their alignment is unimportant.
	public Value mValue; // Value
	public int mSourceLine; // For debugging
	public int mSourceChar;
	public short mOpCode; // (vmOpCode)
	public int mType; // (vmBasicVarType)

	public Instruction() {
		mOpCode = OpCode.OP_NOP;
		// TODO Original source initializes with 0 instead of -1
		mType = ValType.VTP_UNDEFINED;
		mSourceLine = 0;
		mSourceChar = 0;
		mValue = new Value();
	}

	public Instruction(Instruction i) {
		mOpCode = i.mOpCode;
		mType = i.mType;
		mSourceChar = i.mSourceChar;
		mSourceLine = i.mSourceLine;
		mValue = i.mValue;
	}

	public Instruction(short opCode, int type, Value val) {
		this(opCode, type, val, 0, 0);
	}

	public Instruction(short opCode, int type, Value val, int sourceLine, int sourceChar) {
		mOpCode = opCode;
		mType = type;
		mValue = val;
		mSourceLine = sourceLine;
		mSourceChar = sourceChar;
	}

	// Streaming
	// #ifdef VM_STATE_STREAMING
	public void StreamOut(DataOutputStream stream) throws IOException {
		Streaming.WriteShort(stream, mOpCode);
		Streaming.WriteLong(stream, mType);
		mValue.StreamOut(stream);

		Streaming.WriteLong(stream, mSourceLine);
		Streaming.WriteLong(stream, mSourceChar);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException {
		mOpCode = Streaming.ReadShort(stream);
		mType = (int)Streaming.ReadLong(stream);
		mValue.StreamIn(stream);

		mSourceLine = (int) Streaming.ReadLong(stream);
		mSourceChar = (int) Streaming.ReadLong(stream);
		return true;
	}
}