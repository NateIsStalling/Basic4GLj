package com.basic4gl.vm.stackframe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType;

//---------------------------------------------------------------------------
//Created 8-Dec-07: Thomas Mulgrew
//
//Virtual Machine run-time stack-frame entry, for user defined functions.

////////////////////////////////////////////////////////////////////////////////
//vmUserFuncPrototype
//
/// Describes a user function call signiture and return value

public class vmUserFuncPrototype {

	// Paramter and local variable types
	// 0..paramCount-1 -> Parameters
	// paramCount..localVarTypes.size()-1 -> Local variables
	public Vector<ValType> localVarTypes; // Local variable data types
	public Map<String, Integer> localVarIndex; // Name->index lookup (parameters and
										// local vars)
	public int paramCount;
	public boolean hasReturnVal;
	public ValType returnValType;

	public vmUserFuncPrototype() {
		localVarTypes = new Vector<ValType>();
		localVarIndex = new HashMap<String, Integer>();
		Reset();
	}

	public void Reset() {
		paramCount = 0;
		localVarTypes.clear();
		localVarIndex.clear();
		hasReturnVal = false;
		returnValType = new ValType(ValType.VTP_UNDEFINED);
	}

	// Return index of local variable, or if not found
	public int GetLocalVar(String name) {
		Integer val = localVarIndex.get(name);
		return val == null ? -1 : val;
	}

	// Find local variable name.
	// This is inefficient. Used for debugging views etc where performance is
	// not a big priority.
	public String GetLocalVarName(int index) {

		// Return user variable name, given its index.
		// Not particularly efficient. Used for debugging functions (VM view
		// etc)
		for (String key : localVarIndex.keySet())
			if (localVarIndex.get(key) == index)
				return key;
		return "???";
	}

	// Add a new local variable and return its index
	public int NewLocalVar(String name, ValType type) {
		int index = localVarTypes.size();

		// Create new variable definition
		localVarTypes.add(new ValType(type));

		// Link name to index
		localVarIndex.put(name, index);
		return index;
	}

	public int NewParam(String name, ValType type) {

		// Parameters must be added before local variables
		assert (paramCount == localVarTypes.size());

		// Add parameter like a local variable
		int index = NewLocalVar(name, type);

		// Track that it is a parameter
		paramCount++;

		return index;
	}

	// Return true if this function protype matches the other one.
	public boolean Matches(vmUserFuncPrototype func) {

		// Match return value
		if (hasReturnVal != func.hasReturnVal)
			return false;

		if (hasReturnVal && !returnValType.ExactEquals(func.returnValType))
			return false;

		// Match parameters
		if (paramCount != func.paramCount)
			return false;

		// Loop through local vars/params
		// for ( map<string,int>::iterator i = localVarIndex.begin();
		// i != localVarIndex.end();
		// i++) {
		for (String key : localVarIndex.keySet()) {
			// Only interested in params
			if (localVarIndex.get(key) < paramCount) {

				// Other function must have param of same name and parameter
				// index
				Integer funcI = func.localVarIndex.get(key);

				// Param must exist
				if (funcI == null)
					return false;

				// Must have same index
				if (!localVarIndex.get(key).equals(funcI))
					return false;

				// Types must match
				if (!localVarTypes.get(localVarIndex.get(key)).ExactEquals(
						func.localVarTypes.get(funcI)))
					return false;
			}
		}

		// No differences found
		return true;
	}

	public void StreamOut(ByteBuffer buffer) {
		try {
			// Return value
			Streaming.WriteByte(buffer, hasReturnVal ? (byte) 1 : 0);
			if (hasReturnVal)
				returnValType.StreamOut(buffer);

			// Parameters/local variables
			Streaming.WriteLong(buffer, localVarTypes.size());
			for (int i = 0; i < localVarTypes.size(); i++) {
				// #ifdef STREAM_NAMES
				String name = GetLocalVarName(i);
				Streaming.WriteString(buffer, name);
				// #endif
				localVarTypes.get(i).StreamOut(buffer);
			}
			Streaming.WriteLong(buffer, paramCount);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {
		Reset();

		try {
			// Return value
			hasReturnVal = Streaming.ReadByte(buffer) != 0;

			if (hasReturnVal)
				returnValType.StreamIn(buffer);

			// Parameters/local variables
			int count = (int) Streaming.ReadLong(buffer);
			localVarTypes.setSize(count);
			for (int i = 0; i < localVarTypes.size(); i++) {
				// #ifdef STREAM_NAMES
				String name = Streaming.ReadString(buffer);
				localVarIndex.put(name, i);
				// #endif
				localVarTypes.get(i).StreamIn(buffer);
			}
			paramCount = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
