package com.basic4gl.vm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import com.basic4gl.util.Streamable;
import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.TypeLibrary;
import com.basic4gl.vm.types.ValType;

//An array of variables.
public class VariableCollection implements Streamable{
	public class Variable implements Streamable {
		public String m_name; // Var name
		public int m_dataIndex; // Index of data in com.basic4GL.vm.Data array. 0 = not
		// allocated.
		public ValType m_type; // Data type

		public Variable() {
			m_name = "";
			m_dataIndex = 0;
			m_type = new ValType(ValType.VTP_INT);
		}

		public Variable(String name, ValType type) {
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
		@Override
		public void StreamOut(DataOutputStream stream) throws IOException{

			Streaming.WriteString(stream, m_name);
			Streaming.WriteLong(stream, m_dataIndex);

			m_type.StreamOut(stream);
		}

		public boolean StreamIn(DataInputStream stream) throws IOException{

			m_name = Streaming.ReadString(stream);
			m_dataIndex = (int) Streaming.ReadLong(stream);

			m_type.StreamIn(stream);

			return true;
		}
	}

	Vector<Variable> mVariables; // Variables
	Data mData; // Data
	TypeLibrary mTypes; // Type information

	public VariableCollection(Data data, TypeLibrary types) {
		mData = data;
		mTypes = types;

		mVariables = new Vector<Variable>();
	}

	public Vector<Variable> getVariables() {
		return mVariables;
	}

	public Data getData() {
		return mData;
	}

	public TypeLibrary getTypes() {
		return mTypes;
	}

	public void deallocate() {

		// Deallocate all variable data.
		// Variables and data type info remain.
		for (Variable var : mVariables)
			var.Deallocate();
		mData.Clear();
	}

	public void clear() {

		// Deallocate everything.
		// No variables, data or type information remains
		mVariables.clear();
		mData.Clear();
		mTypes.Clear();
	}

	// Finding variables
	public int getVariableIndex(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < mVariables.size(); i++)
			if (mVariables.get(i).m_name.equals(name))
				return i;
		return -1;
	}

	public boolean containsVariable(String name) {
		return getVariableIndex(name) >= 0;
	}

	public int Size() {
		return mVariables.size();
	}

	public boolean IndexValid(int index) {
		return index >= 0 && index < Size();
	}

	// Var creation and allocation
	public int NewVar(String name, ValType type) {
		assert (!containsVariable(name));

		// Allocate new variable and return index
		int top = mVariables.size();
		mVariables.add(new Variable(name, type));
		return top;
	}

	public void AllocateVar(Variable var) {
		var.Allocate(mData, mTypes);
	}

	// Streaming
	public void StreamOut(DataOutputStream stream) throws IOException{

		// Stream out type data
		mTypes.StreamOut(stream);

		// Stream out variables
		Streaming.WriteLong(stream, mVariables.size());
		for (int i = 0; i < mVariables.size(); i++)
			mVariables.get(i).StreamOut(stream);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{

		// Stream in type data
		mTypes.StreamIn(stream);

		// Stream in variables
		long count = Streaming.ReadLong(stream);
		mVariables.setSize((int) count);
		for (int i = 0; i < count; i++) {
			mVariables.set(i, new Variable());
			mVariables.get(i).StreamIn(stream);
		}
		return true;
	}
}