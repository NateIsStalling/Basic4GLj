package com.basic4gl.vm.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType;

////////////////////////////////////////////////////////////////////////////////
// VmStructure
//
// 1 or more VmValTypes combined into a structure type.
//
// E.g.
// Struc
// dim angle#, names$ (100), x, y
// EndStruc

public class StructureField {
	public String m_name; // Field name
	public ValType m_type; // Data type
	public int m_dataOffset; // Data offset from top of structure

	public StructureField(String name, ValType type) {
		this(name, type, 0);
	}

	public StructureField(String name, ValType type, int dataOffset) {
		m_name = name.toLowerCase();
		m_type = type;
		m_dataOffset = dataOffset;
	}

	public StructureField() {
		m_name = "";
		m_type.Set(ValType.VTP_INT);
		m_dataOffset = 0;
	}

	// #ifdef VM_STATE_STREAMING
	// Streaming
	public void StreamOut(ByteBuffer buffer) {
		try {
			Streaming.WriteString(buffer, m_name);

			m_type.StreamOut(buffer);
			Streaming.WriteLong(buffer, m_dataOffset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			m_name = Streaming.ReadString(buffer);

			m_type.StreamIn(buffer);
			m_dataOffset = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}