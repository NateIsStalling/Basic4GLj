package com.basic4gl.runtime.stackframe;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Describes a user function call signature and return value
 */
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
		localVarTypes = new Vector<>();
		localVarIndex = new HashMap<>();
		reset();
	}

	public void reset() {
		paramCount = 0;
		localVarTypes.clear();
		localVarIndex.clear();
		hasReturnVal = false;
		returnValType = new ValType(BasicValType.VTP_UNDEFINED);
	}

	// Return index of local variable, or if not found
	public int getLocalVar(String name) {
		Integer val = localVarIndex.get(name);
		return val == null ? -1 : val;
	}

	// Find local variable name.
	// This is inefficient. Used for debugging views etc where performance is
	// not a big priority.
	public String getLocalVarName(int index) {

		// Return user variable name, given its index.
		// Not particularly efficient. Used for debugging functions (VM view
		// etc)
		for (String key : localVarIndex.keySet()) {
			if (localVarIndex.get(key) == index) {
				return key;
			}
		}
		return "???";
	}

	// Add a new local variable and return its index
	public int newLocalVar(String name, ValType type) {
		int index = localVarTypes.size();

		// Create new variable definition
		localVarTypes.add(new ValType(type));

		// Link name to index
		localVarIndex.put(name, index);
		return index;
	}

	public int newParam(String name, ValType type) {

		// Parameters must be added before local variables
		assertTrue(paramCount == localVarTypes.size());

		// Add parameter like a local variable
		int index = newLocalVar(name, type);

		// Track that it is a parameter
		paramCount++;

		return index;
	}

	// Return true if this function protype matches the other one.
	public boolean matches(UserFuncPrototype func) {

		// Match return value
		if (hasReturnVal != func.hasReturnVal) {
			return false;
		}

		if (hasReturnVal && !returnValType.exactEquals(func.returnValType)) {
			return false;
		}

		// Match parameters
		if (paramCount != func.paramCount) {
			return false;
		}

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
				if (funcI == null) {
					return false;
				}

				// Must have same index
				if (!localVarIndex.get(key).equals(funcI)) {
					return false;
				}

				// Types must match
				if (!localVarTypes.get(localVarIndex.get(key)).exactEquals(func.localVarTypes.get(funcI))) {
					return false;
				}
			}
		}

		// No differences found
		return true;
	}

	public void streamOut(DataOutputStream stream) throws IOException {
		// Return value
		Streaming.writeByte(stream, hasReturnVal ? (byte) 1 : 0);
		if (hasReturnVal) {
			returnValType.streamOut(stream);
		}

		// Parameters/local variables
		Streaming.writeLong(stream, localVarTypes.size());
		for (int i = 0; i < localVarTypes.size(); i++) {
			// #ifdef STREAM_NAMES
			String name = getLocalVarName(i);
			Streaming.writeString(stream, name);
			// #endif
			localVarTypes.get(i).streamOut(stream);
		}
		Streaming.writeLong(stream, paramCount);
	}

	public boolean streamIn(DataInputStream stream) throws IOException {
		reset();

		// Return value
		hasReturnVal = Streaming.readByte(stream) != 0;

		if (hasReturnVal) {
			returnValType.streamIn(stream);
		}

		// Parameters/local variables
		int count = (int) Streaming.readLong(stream);
		localVarTypes.setSize(count);
		for (int i = 0; i < localVarTypes.size(); i++) {
			// #ifdef STREAM_NAMES
			String name = Streaming.readString(stream);
			localVarIndex.put(name, i);
			// #endif
			localVarTypes.set(i, new ValType());
			localVarTypes.get(i).streamIn(stream);
		}
		paramCount = (int) Streaming.readLong(stream);
		return true;
	}
}
