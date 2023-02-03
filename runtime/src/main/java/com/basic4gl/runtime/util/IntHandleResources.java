package com.basic4gl.runtime.util;

import java.util.HashSet;
import java.util.Set;

/**
 * For resource types that are represented as an integer handle.
 * There is no real advantage of creating a further integer.resource mapping
 * as the resource is already an integer. Therefore we simply record which
 * handles have been allocated so that we can deallocate them.
 */
public abstract class IntHandleResources extends Resources {

	protected Set<Integer> m_handles = new HashSet<Integer>();

	protected abstract void DeleteHandle(int handle);

	public void clear() {

		// Clear set
		m_handles.clear();
	}

	public boolean Valid(int handle) {
		return m_handles.contains(handle);
	}

	public void Store(int handle) {
		m_handles.add(handle);
	}

	public void Remove(int handle) {
		m_handles.remove(handle);
	}

	public void Free(int handle) {
		DeleteHandle(handle);
		Remove(handle);
	}
}
