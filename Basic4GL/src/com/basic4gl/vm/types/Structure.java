package com.basic4gl.vm.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;

public class Structure {
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

	// #ifdef VM_STATE_STREAMING
	// Streaming
	public void StreamOut(ByteBuffer buffer) {
		
		try {
		Streaming.WriteString(buffer, m_name);

		Streaming.WriteLong(buffer, m_firstField);
		Streaming.WriteLong(buffer, m_fieldCount);
		Streaming.WriteLong(buffer, m_dataSize);
		Streaming.WriteByte(buffer, (byte)(m_containsString ? 1 : 0));
		Streaming.WriteByte(buffer, (byte)(m_containsArray ? 1 : 0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
		m_name = Streaming.ReadString(buffer);

		m_firstField = (int)Streaming.ReadLong(buffer);
		m_fieldCount = (int)Streaming.ReadLong(buffer);
		m_dataSize = (int)Streaming.ReadLong(buffer);
		m_containsString = (Streaming.ReadByte(buffer)==1);
		m_containsArray = (Streaming.ReadByte(buffer)==1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
