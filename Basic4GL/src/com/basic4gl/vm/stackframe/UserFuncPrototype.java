package com.basic4gl.vm.stackframe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import com.basic4gl.util.Streamable;
import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType;

//---------------------------------------------------------------------------
//Created 8-Dec-07: Thomas Mulgrew
//
//Virtual Machine run-time stack-frame entry, for user defined functions.

////////////////////////////////////////////////////////////////////////////////
//UserFuncPrototype
//
/// Describes a user function call signiture and return value

public class UserFuncPrototype implements Streamable {

	// Paramter and local variable types
	// 0..paramCount-1 -> Parameters
	// paramCount..localVarTypes.size()-1 -> Local variables
	public Vector<ValType> localVarTypes; // Local variable data types
	public Map<String, Integer> localVarIndex; // Name->index lookup (parameters and
	// local vars)
	public int paramCount;
	public boolean hasReturnVal;
	public ValType returnValType;

	public UserFuncPrototype() {
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
	public boolean Matches(UserFuncPrototype func) {

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

	public void StreamOut(DataOutputStream stream) throws IOException{
		// Return value
		Streaming.WriteByte(stream, hasReturnVal ? (byte) 1 : 0);
		if (hasReturnVal)
			returnValType.StreamOut(stream);

		// Parameters/local variables
		Streaming.WriteLong(stream, localVarTypes.size());
		for (int i = 0; i < localVarTypes.size(); i++) {
			// #ifdef STREAM_NAMES
			String name = GetLocalVarName(i);
			Streaming.WriteString(stream, name);
			// #endif
			localVarTypes.get(i).StreamOut(stream);
		}
		Streaming.WriteLong(stream, paramCount);
	}

	public boolean StreamIn(DataInputStream stream) throws IOException{
		Reset();

		// Return value
		hasReturnVal = Streaming.ReadByte(stream) != 0;

		if (hasReturnVal)
			returnValType.StreamIn(stream);

		// Parameters/local variables
		int count = (int) Streaming.ReadLong(stream);
		localVarTypes.setSize(count);
		for (int i = 0; i < localVarTypes.size(); i++) {
			// #ifdef STREAM_NAMES
			String name = Streaming.ReadString(stream);
			localVarIndex.put(name, i);
			// #endif
			localVarTypes.set(i, new ValType());
			localVarTypes.get(i).StreamIn(stream);
		}
		paramCount = (int) Streaming.ReadLong(stream);
		return true;
	}

}
