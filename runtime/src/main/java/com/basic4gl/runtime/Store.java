package com.basic4gl.runtime;

import java.util.ArrayList;
import java.util.Vector;

import static com.basic4gl.runtime.util.Assert.assertTrue;

////////////////////////////////////////////////////////////////////////////////
//Store
//
//Abstract template class for allocating and referencing a specific type of
//object.
//Used for strings, pointers, handles e.t.c.
//The virtual machine stores only the array index, and thus VM programs avoid
//having to see and manipulate pointers, handles e.t.c.
public class Store<T> {
	Vector<T> m_array;
	Vector<Boolean> m_valAllocated;
	ArrayList<Integer> m_freeList; // List of free indices
	T m_blankElement; // New elements are initialised to this

	public Store(T blankElement) {
		m_blankElement = blankElement;
		
		m_array = new Vector<T>();
		m_valAllocated = new Vector<Boolean>();
		
		m_freeList = new ArrayList<Integer>();
	}

	public boolean IndexValid(int index) { // Return true if index is a valid
		// allocated index
		return index >= 0 && index < m_array.size()
				&& m_valAllocated.get(index);
	}

	public boolean IndexStored(int index) {
		return index != 0 && IndexValid(index);
	}

	public T Value(int index) {
		assertTrue(IndexValid(index));
		return m_array.get(index);
	}

	public void setValue(int index, T val) {
		assertTrue(IndexValid(index));
		m_array.set(index, (T) val);
	}

	public int Alloc (){
		int index;
		if (m_freeList.isEmpty ()) {

			// Extend array by a single item, and return index of that item
			index = m_array.size ();
			m_array.add(m_blankElement);
			m_valAllocated.add (true);        // Mark element as in use
		}
		else {

			// Reuse previously freed index
			index = m_freeList.get(0);
			m_freeList.remove(0);
			//index = 0; possible porting mistake, my bad.. the index = m_freeList.get(0) should be correct
			
			// Initialise element
			m_array .set(index, m_blankElement);
			m_valAllocated .set(index, true);
		}
		return index;
	}

	public void Free(int index) {

		// Deallocate index and return to array
		assertTrue(IndexValid(index));
		m_valAllocated.set(index, false);
		m_freeList.add(0, index);
	}

	public void Clear() {

		// Clear allocated values
		m_freeList.clear();
		m_array.clear();
		m_valAllocated.clear();

		// Allocate a "blank" value for the 0th element.
		// Basic4GL uses 0 to indicate that data hasn't been allocated yet.
		Alloc();
	}

	public int StoredElements() {
		return m_array.size() - m_freeList.size();
	}

	public Vector<T> Array() {
		return m_array;
	}

	public Vector<Boolean> ValAllocated() {
		return m_valAllocated;
	}

	public T BlankElement() {
		return m_blankElement;
	}
}