package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.OpCode;
import com.basic4gl.vm.types.ValType.BasicValType;

// //////////////////////////////////////////////////////////////////////////////
// vmInstruction
// #pragma pack (push, 1)
public class vmInstruction {

	// Note: vmInstruction size = 12 bytes.
	// Ordering of member fields is important, as the two 4 byte members
	// are first (and hence aligned to a 4 byte boundary).
	// mSourceChar is next, and aligned to a 2 byte boundary.
	// Single byte fields are last, as their alignment is unimportant.
	public VMValue mValue; // Value
	public int mSourceLine; // For debugging
	public short mSourceChar;
	public short mOpCode; // (vmOpCode)
	public byte mType; // (vmBasicVarType)

	public vmInstruction() {
		mOpCode = OpCode.OP_NOP;
		// TODO Original source initializes with 0 instead of -1
		mType = (byte) BasicValType.VTP_UNDEFINED.getType();
		mSourceLine = 0;
		mSourceChar = 0;
		mValue = new VMValue();
	}

	public vmInstruction(vmInstruction i) {
		mOpCode = i.mOpCode;
		mType = i.mType;
		mSourceChar = i.mSourceChar;
		mSourceLine = i.mSourceLine;
		mValue = i.mValue;
	}

	public vmInstruction(short opCode, byte type, VMValue val) {
		this(opCode, type, val, 0, 0);
	}

	public vmInstruction(short opCode, byte type, VMValue val, int sourceLine,
			int sourceChar) {
		mOpCode = opCode;
		mType = type;
		mValue = val;
		mSourceLine = sourceLine;
		mSourceChar = (short) sourceChar;
	}

	// Streaming
	// #ifdef VM_STATE_STREAMING
	void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteShort(buffer, mOpCode);
			Streaming.WriteByte(buffer, mType);
			mValue.StreamOut(buffer);

			Streaming.WriteLong(buffer, mSourceLine);
			Streaming.WriteShort(buffer, mSourceChar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void StreamIn(ByteBuffer buffer) {
		try {
			mOpCode = Streaming.ReadShort(buffer);
			mType = Streaming.ReadByte(buffer);
			mValue.StreamIn(buffer);
			
			mSourceLine = (int) Streaming.ReadLong(buffer);
			mSourceChar = Streaming.ReadShort(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}