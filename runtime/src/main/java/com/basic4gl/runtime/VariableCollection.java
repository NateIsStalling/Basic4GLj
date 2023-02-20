package com.basic4gl.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.types.TypeLibrary;
import com.basic4gl.runtime.types.ValType;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * An array of variables.
 */
public class VariableCollection implements Streamable{
	public static class Variable implements Streamable {
		/**
		 * Var name
		 */
		public String name;

		/**
		 * Index of data in com.basic4GL.vm.Data array.
		 * 0 = not allocated.
		 */
		public int dataIndex;

		/**
		 * Data type
		 */
		public ValType type;

		public Variable() {
			name = "";
			dataIndex = 0;
			type = new ValType (BasicValType.VTP_INT);
		}

		public Variable(String name, ValType type) {
			this.name = name.toLowerCase();
			this.type = type;
			deallocate();
		}

		protected void finalize() {
			deallocate();
			try {
				super.finalize();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public boolean allocated() {
			return dataIndex > 0;
		}

		// Allocating
		public void deallocate() {
			dataIndex = 0;
		}

		public void allocate(Data data, TypeLibrary typeLib) {

			// Allocate new data
			assertTrue(!allocated());
			dataIndex = data.allocate(typeLib.getDataSize(type));

			// Initialise it
			data.initData(dataIndex, type, typeLib);
		}

		// Streaming
		@Override
		public void streamOut(DataOutputStream stream) throws IOException{

			Streaming.writeString(stream, name);
			Streaming.writeLong(stream, dataIndex);

			type.streamOut(stream);
		}

		public boolean streamIn(DataInputStream stream) throws IOException{

			name = Streaming.readString(stream);
			dataIndex = (int) Streaming.readLong(stream);

			type.streamIn(stream);

			return true;
		}
	}

	private Vector<Variable> variables;
	private Data data;
	private TypeLibrary types;

	public VariableCollection(Data data, TypeLibrary types) {
		this.data = data;
		this.types = types;

		variables = new Vector<Variable>();
	}

	/**
	 * Variables
	 */
	public Vector<Variable> getVariables() {
		return variables;
	}

	/**
	 * Data
	 */
	public Data getData() {
		return data;
	}

	/**
	 * Type information
	 */
	public TypeLibrary getTypes() {
		return types;
	}

	public void deallocate() {

		// Deallocate all variable data.
		// Variables and data type info remain.
		for (Variable var : variables) {
			var.deallocate();
		}
		data.clear();
	}

	public void clear() {

		// Deallocate everything.
		// No variables, data or type information remains
		variables.clear();
		data.clear();
		types.clear();
	}

	/**
	 * Finding variables
 	 */
	public int getVariableIndex(String name) {
		name = name.toLowerCase();
		for (int i = 0; i < variables.size(); i++) {
			if (variables.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public boolean containsVariable(String name) {
		return getVariableIndex(name) >= 0;
	}

	public int size() {
		return variables.size();
	}

	public boolean isIndexValid(int index) {
		return index >= 0 && index < size();
	}

	/**
	 * Var creation and allocation
 	 */
	public int createVar(String name, ValType type) {
		assertTrue(!containsVariable(name));

		// Allocate new variable and return index
		int top = variables.size();
		variables.add(new Variable(name, type));
		return top;
	}

	/**
	 * Var creation and allocation
	 */
	public void allocateVar(Variable var) {
		var.allocate(data, types);
	}

	// Streaming
	public void streamOut(DataOutputStream stream) throws IOException{

		// Stream out type data
		types.streamOut(stream);

		// Stream out variables
		Streaming.writeLong(stream, variables.size());
		for (int i = 0; i < variables.size(); i++) {
			variables.get(i).streamOut(stream);
		}
	}

	public boolean streamIn(DataInputStream stream) throws IOException{

		// Stream in type data
		types.streamIn(stream);

		// Stream in variables
		long count = Streaming.readLong(stream);
		variables.setSize((int) count);
		for (int i = 0; i < count; i++) {
			variables.set(i, new Variable());
			variables.get(i).streamIn(stream);
		}
		return true;
	}
}
