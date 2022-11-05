package com.basic4gl.runtime.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
// VmStructure
//
// 1 or more VmValTypes combined into a structure type.
//
// E.g.
// Struc
// dim angle#, names$ (100), x, y
// EndStruc

public class StructureField implements Streamable{
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
		m_type = new ValType(ValType.VTP_INT);
		m_dataOffset = 0;
	}

	// Streaming
	public void StreamOut(DataOutputStream stream) throws IOException{

		Streaming.WriteString(stream, m_name);

		m_type.StreamOut(stream);
		Streaming.WriteLong(stream, m_dataOffset);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{

		m_name = Streaming.ReadString(stream);

		m_type.StreamIn(stream);
		m_dataOffset = (int) Streaming.ReadLong(stream);

		return true;
	}
}