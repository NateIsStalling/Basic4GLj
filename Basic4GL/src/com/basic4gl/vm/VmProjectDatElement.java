package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType.BasicValType;

////////////////////////////////////////////////////////////////////////////////
//vmProgramDataElement
//
//General purpose program data (as allocated with "DATA" statement in BASIC).
class VmProgramDataElement {
	BasicValType m_type;
	VMValue m_value;

	public BasicValType getType() {
		return m_type;
	}

	public VMValue getValue() {
		return m_value;
	}

	public void setType(BasicValType type) {
		m_type = type;
	}

	public void setValue(VMValue value) {
		m_value = value;
	}

	// Streaming
	public void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteLong(buffer, m_type.getType());

			m_value.StreamOut(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			m_type = BasicValType.getType((int) Streaming.ReadLong(buffer));

			m_value.StreamIn(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}