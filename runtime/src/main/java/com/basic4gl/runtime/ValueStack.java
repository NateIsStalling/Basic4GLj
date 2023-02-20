package com.basic4gl.runtime;

import java.util.Vector;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Used to stack values for reverse-Polish expression evaluation, or as
 * function parameters.
 */
public class ValueStack {
	private Vector<Value> data;
	private Store<String> strings;

	public ValueStack(Store<String> strings) {
		this.strings = strings;
		data = new Vector<Value>();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public void push(Value v) { // Push v as NON string
		data.add(new Value(v));
	}

	public void pushString(String str) {
		int index = strings.alloc(); // Allocate string
		strings.setValue(index, str); // Copy value
		data.add(new Value(index)); // Create stack index
	}

	public Value tos() {
		assertTrue(!isEmpty());
		return data.get(data.size() - 1);
	}

	public Value pop() {
		Value v = tos();
		data.remove(data.size() - 1);
		
		return v;
	}

	public String popString() {
		assertTrue(!isEmpty());
		String str;
		// Copy string value from stack
		int index = tos().getIntVal();
		assertTrue(strings.isIndexValid(index));
		str = strings.getValueAt(index);
		// Deallocate stacked string
		strings.freeAtIndex(index);

		// Remove stack element
		data.remove(data.size() - 1);
		return str;
	}

	public void clear() {
		data.clear();
	}

	public int size() {
		return data.size();
	}

	public Value get(int index) {
		return data.get(index);
	}

	public void resize(int size) {
		data.setSize(size);
	}
}