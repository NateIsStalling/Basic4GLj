package com.basic4gl.runtime.util;

import com.basic4gl.runtime.Store;

////////////////////////////////////////////////////////////////////////////////
//vmResourceStore
//
//Template implementation of vmResources, using an internal Store

public abstract class ResourceStore<T> extends Resources {

	protected Store<T> m_store;

	protected abstract void DeleteElement(int index);

	@SuppressWarnings("unchecked")
	public ResourceStore(T blankElement) {
		m_store = new Store<T>(blankElement);
	}

	public void Clear() {

		// Delete each individual element
		for (int i = 0; i < m_store.Array().size(); i++) {
            if (m_store.ValAllocated().get(i)) {
                DeleteElement(i);
            }
        }

		// Clear store
		m_store.Clear();
	}

	// Pass calls through to internal store
	public boolean IndexValid(int index) {
		return m_store.IndexValid(index);
	}

	public boolean IndexStored(int index) {
		return m_store.IndexStored(index);
	}

	public T Value(int index) {
		return m_store.Value(index);
	}

	public void setValue(int index, T value) {
		m_store.setValue(index, value);
	}

	public int Alloc() {
		return m_store.Alloc();
	}

	public int Alloc(T element) {
		if (element.equals(m_store.BlankElement())) {
            return -1;
        } else {
			int index = Alloc();
			setValue(index, element);
			return index;
		}
	}

	public void Remove(int index) { // Remove an element without freeing it.
									// Calling code is responsible for freeing
									// the resource.
		m_store.Free(index);
	}

	public void Free(int index) { // Remove and free element.
		if (IndexValid(index)) {
			DeleteElement(index);
			Remove(index);
		}
	}

	public Store<T> Store() {
		return m_store;
	}
}
