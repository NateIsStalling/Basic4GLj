package com.basic4gl.runtime.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

public class Structure implements Streamable{
	public String m_name; // Type name
	public int m_firstField; // Index of first field
	public int m_fieldCount; // # of fields in structure
	public int m_dataSize; // Size of data
	public boolean m_containsString; // Contains one or more strings. (Requires
								// special handling when copying data.)
	public boolean m_containsArray; // Contains one or more arrays. (Requires
								// special handling when allocating data.)
	public boolean m_containsPointer; // Contains one or more pointers. (Requires
								// pointer validity checking when copying
								// data.)

	public Structure() {
		this("", 0);
	}

	public Structure(String name, int firstField) {
		m_name = name.toLowerCase();
		m_firstField = firstField;
		m_fieldCount = 0;
		m_dataSize = 0;
		m_containsString = false;
		m_containsArray = false;
		m_containsPointer = false;
	}

	// Streaming
	@Override
	public void streamOut(DataOutputStream stream) throws IOException{
		Streaming.WriteString(stream, m_name);

		Streaming.WriteLong(stream, m_firstField);
		Streaming.WriteLong(stream, m_fieldCount);
		Streaming.WriteLong(stream, m_dataSize);
		Streaming.WriteByte(stream, (byte)(m_containsString ? 1 : 0));
		Streaming.WriteByte(stream, (byte)(m_containsArray ? 1 : 0));
	}

	@Override
	public boolean streamIn(DataInputStream stream) throws IOException{
		m_name = Streaming.ReadString(stream);

		m_firstField = (int)Streaming.ReadLong(stream);
		m_fieldCount = (int)Streaming.ReadLong(stream);
		m_dataSize = (int)Streaming.ReadLong(stream);
		m_containsString = (Streaming.ReadByte(stream)==1);
		m_containsArray = (Streaming.ReadByte(stream)==1);

		return true;
	}
}
