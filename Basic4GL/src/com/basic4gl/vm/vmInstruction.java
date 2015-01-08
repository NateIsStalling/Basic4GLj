package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType.BasicValType;
import com.basic4gl.vm.vmCode.vmOpCode;

// //////////////////////////////////////////////////////////////////////////////
// vmInstruction
// #pragma pack (push, 1)
public class vmInstruction {

	// Note: vmInstruction size = 12 bytes.
	// Ordering of member fields is important, as the two 4 byte members
	// are first (and hence aligned to a 4 byte boundary).
	// m_sourceChar is next, and aligned to a 2 byte boundary.
	// Single byte fields are last, as their alignment is unimportant.
	public VMValue m_value; // Value
	public int m_sourceLine; // For debugging
	public short m_sourceChar;
	public vmOpCode m_opCode; // (vmOpCode)
	public byte m_type; // (vmBasicVarType)

	public vmInstruction() {
		m_opCode = vmOpCode.OP_NOP;
		// TODO Original source initializes with 0 instead of -1
		m_type = (byte) BasicValType.VTP_UNDEFINED.getType();
		m_sourceLine = 0;
		m_sourceChar = 0;
		m_value = new VMValue();
	}

	public vmInstruction(vmInstruction i) {
		m_opCode = i.m_opCode;
		m_type = i.m_type;
		m_sourceChar = i.m_sourceChar;
		m_sourceLine = i.m_sourceLine;
		m_value = i.m_value;
	}

	public vmInstruction(byte opCode, byte type, VMValue val) {
		this(opCode, type, val, 0, 0);
	}

	public vmInstruction(byte opCode, byte type, VMValue val, int sourceLine,
			int sourceChar) {
		m_opCode = vmOpCode.getCode(opCode);
		m_type = type;
		m_value = val;
		m_sourceLine = sourceLine;
		m_sourceChar = (short) sourceChar;
	}

	// Streaming
	// #ifdef VM_STATE_STREAMING
	void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteByte(buffer, m_opCode.getCode());
			Streaming.WriteByte(buffer, m_type);
			m_value.StreamOut(buffer);

			Streaming.WriteLong(buffer, m_sourceLine);
			Streaming.WriteShort(buffer, m_sourceChar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void StreamIn(ByteBuffer buffer) {
		try {
			m_opCode = vmOpCode.getCode(Streaming.ReadByte(buffer));
			m_type = Streaming.ReadByte(buffer);
			m_value.StreamIn(buffer);
			
			m_sourceLine = (int) Streaming.ReadLong(buffer);
			m_sourceChar = Streaming.ReadShort(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}