package com.basic4gl.vm;

import java.util.Vector;

////////////////////////////////////////////////////////////////////////////////
//VMValueStack
//
//Used to stack values for reverse-Polish expression evaluation, or as
//function parameters.
public class ValueStack {
	private Vector<Value> m_data;
	private Store<String> m_strings;

	public ValueStack(Store<String> strings) {
		m_strings = strings;
		m_data = new Vector<Value>();
	}

	public boolean Empty() {
		return m_data.isEmpty();
	}

	public void Push(Value v) { // Push v as NON string
		m_data.add(new Value(v));
	}

	public void PushString(String str) {
		int index = m_strings.Alloc(); // Allocate string
		m_strings.setValue(index, str); // Copy value
		m_data.add(new Value(index)); // Create stack index
	}

	public Value TOS() {
		assert (!Empty());
		return m_data.get(m_data.size() - 1);
	}

	public Value Pop() {
		Value v = TOS();
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

	public Value get(int index) {
		return m_data.get(index);
	}

	public void Resize(int size) {
		m_data.setSize(size);
	}
}