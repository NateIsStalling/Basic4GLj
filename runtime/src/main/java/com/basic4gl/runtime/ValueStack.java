package com.basic4gl.runtime;

import java.util.Vector;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Used to stack values for reverse-Polish expression evaluation, or as
 * function parameters.
 */
public class ValueStack {
	private Vector<Value> m_data;
	private Store<String> m_strings;

	public ValueStack(Store<String> strings) {
		m_strings = strings;
		m_data = new Vector<Value>();
	}

	public boolean isEmpty() {
		return m_data.isEmpty();
	}

	public void push(Value v) { // Push v as NON string
		m_data.add(new Value(v));
	}

	public void pushString(String str) {
		int index = m_strings.Alloc(); // Allocate string
		m_strings.setValue(index, str); // Copy value
		m_data.add(new Value(index)); // Create stack index
	}

	public Value tos() {
		assertTrue(!isEmpty());
		return m_data.get(m_data.size() - 1);
	}

	public Value pop() {
		Value v = tos();
		m_data.remove(m_data.size() - 1);
		
		return v;
	}

	public String popString() {
		assertTrue(!isEmpty());
		String str;
		// Copy string value from stack
		int index = tos().getIntVal();
		assertTrue(m_strings.IndexValid(index));
		str = m_strings.valueAt(index);
		// Deallocate stacked string
		m_strings.Free(index);

		// Remove stack element
		m_data.remove(m_data.size() - 1);
		return str;
	}

	public void clear() {
		m_data.clear();
	}

	public int size() {
		return m_data.size();
	}

	public Value get(int index) {
		return m_data.get(index);
	}

	public void resize(int size) {
		m_data.setSize(size);
	}
}