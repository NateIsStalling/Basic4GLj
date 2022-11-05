package com.basic4gl.runtime.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;

////////////////////////////////////////////////////////////////////////////////
// VmValTypeSet
//
// A small number of VM op-codes operate on VmValType advanced data types
// (e.g. OP_COPY). Op-codes don't have storage space to specify an advanced
// data
// type, so instead they specify an index into this set array.
public class ValTypeSet implements Streamable{

	private Vector<ValType> mTypes;

	public ValTypeSet(){
		mTypes = new Vector<ValType>();
	}

	public void clear() {
		mTypes.clear();
	}

	public int getIndex(ValType type) {

		// Get index of type "type" in our set.
		// If type is not present, create a new one and return an index to
		// that.

		// Look for type
		int i;
		for (i = 0; i < mTypes.size(); i++)
			if (mTypes.get(i).Equals(type))
				return i;

		// Otherwise create new one
		i = mTypes.size();
		mTypes.add(new ValType(type));
		return i;
	}

	public ValType getValType(int index) {
		if (index >= 0 && index < mTypes.size())
			return mTypes.get(index);
		return null;
	}

	// Streaming
	public void StreamOut(DataOutputStream stream) throws IOException{

		Streaming.WriteLong(stream, mTypes.size());
		for (int i = 0; i < mTypes.size(); i++)
			mTypes.get(i).StreamOut(stream);

	}

	public boolean StreamIn(DataInputStream stream) throws IOException{
		int count;
		count = (int) Streaming.ReadLong(stream);

		mTypes.setSize(count);
		for (int i = 0; i < count; i++) {
			mTypes.set(i, new ValType());
			mTypes.get(i).StreamIn(stream);
		}
		return true;
	}
}
