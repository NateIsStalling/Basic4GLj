package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.TypeLibrary;
import com.basic4gl.vm.types.ValType;

//An array of variables.
public class Variables {
	public class Var {
		public String m_name; // Var name
		public int m_dataIndex; // Index of data in com.basic4GL.vm.Data array. 0 = not
								// allocated.
		public ValType m_type; // Data type

		public Var() {
			m_name = "";
			m_dataIndex = 0;
			m_type = new ValType(ValType.VTP_INT);
		}

		public Var(String name, ValType type) {
			m_name = name.toLowerCase();
			m_type = type;
			Deallocate();
		}

		protected void finalize() {
			Deallocate();
			try {
				super.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public boolean Allocated() {
			return m_dataIndex > 0;
		}

		// Allocating
		public void Deallocate() {
			m_dataIndex = 0;
		}

		public void Allocate(Data data, TypeLibrary typeLib) {

			// Allocate new data
			assert (!Allocated());
			m_dataIndex = data.Allocate(typeLib.DataSize(m_type));

			// Initialise it
			data.InitData(m_dataIndex, m_type, typeLib);
		}

		// Streaming
		public void StreamOut(ByteBuffer buffer) {
			try {
				Streaming.WriteString(buffer, m_name);

				Streaming.WriteLong(buffer, m_dataIndex);
				m_type.StreamOut(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void StreamIn(ByteBuffer buffer) {
			try {
				m_name = Streaming.ReadString(buffer);

				m_dataIndex = (int) Streaming.ReadLong(buffer);
				m_type.StreamIn(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	Vector<Var> m_variables; // Variables
	Data m_data; // Data
	TypeLibrary m_types; // Type information

	public Variables(Data data, TypeLibrary types) {
		m_data = data;
		m_types = types;
		
		m_variables = new Vector<Var>();
	}

	public Vector<Var> Variables() {
		return m_variables;
	}

	public Data Data() {
		return m_data;
	}

	public TypeLibrary Types() {
		return m_types;
	}

	public void Deallocate() {

		// Deallocate all variable data.
		// Variables and data type info remain.
		for (Var var : m_variables)
			var.Deallocate();
		m_data.Clear();
	}

	public void Clear() {

		// Deallocate everything.
		// No variables, data or type information remains
		m_variables.clear();
		m_data.Clear();
		m_types.Clear();
	}

	// Finding variables
	public int GetVar(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < m_variables.size(); i++)
			if (m_variables.get(i).m_name.equals(name))
				return i;
		return -1;
	}

	public boolean VarStored(String name) {
		return GetVar(name) >= 0;
	}

	public int Size() {
		return m_variables.size();
	}

	public boolean IndexValid(int index) {
		return index >= 0 && index < Size();
	}

	// Var creation and allocation
	public int NewVar(String name, ValType type) {
		assert (!VarStored(name));

		// Allocate new variable and return index
		int top = m_variables.size();
		m_variables.add(new Var(name, type));
		return top;
	}

	public void AllocateVar(Var var) {
		var.Allocate(m_data, m_types);
	}

	// Streaming
	public void StreamOut(ByteBuffer buffer) {
		try {
			// Stream out type data
			m_types.StreamOut(buffer);

			// Stream out variables
			Streaming.WriteLong(buffer, m_variables.size());
			for (int i = 0; i < m_variables.size(); i++)
				m_variables.get(i).StreamOut(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		try {
			// Stream in type data
			m_types.StreamIn(buffer);

			// Stream in variables
			long count = Streaming.ReadLong(buffer);
			m_variables.setSize((int) count);
			for (int i = 0; i < count; i++)
				m_variables.get(i).StreamIn(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
