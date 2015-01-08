package com.basic4gl.vm;

import java.util.Vector;

import com.basic4gl.util.Mutable;

////////////////////////////////////////////////////////////////////////////////
//VMValueStack
//
//Used to stack values for reverse-Polish expression evaluation, or as
//function parameters.
public class VmValueStack {
	Vector<VMValue> m_data;
	VmStore<String> m_strings;

	public VmValueStack(VmStore<String> strings) {
		m_strings = strings;
		m_data = new Vector<VMValue>();
	}

	public boolean Empty() {
		return m_data.isEmpty();
	}

	public void Push(VMValue v) { // Push v as NON string
		m_data.add(new VMValue(v));
	}

	public void PushString(String str) {
		int index = m_strings.Alloc(); // Allocate string
		m_strings.setValue(index, str); // Copy value
		m_data.add(new VMValue(index)); // Create stack index
	}

	public VMValue TOS() {
		assert (!Empty());
		return m_data.get(m_data.size() - 1);
	}

	public VMValue Pop() {
		VMValue v = TOS();
		m_data.remove(m_data.size() - 1);
		
		return v;
	}

	public String PopString() {
		assert (!Empty());
		String str;
		// Copy string value from stack
		int index = TOS().getIntVal();
		assert (m_strings.IndexValid(index));
		str = m_strings.Value(index);
		// Deallocate stacked string
		m_strings.Free(index);

		// Remove stack element
		m_data.remove(m_data.size() - 1);
		return str;
	}

	public void Clear() {
		m_data.clear();
	}

	public int Size() {
		return m_data.size();
	}

	public VMValue get(int index) {
		return m_data.get(index);
	}

	public void Resize(int size) {
		m_data.setSize(size);
	}
}