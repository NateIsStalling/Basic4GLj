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

	protected Set<Integer> handles = new HashSet<>();

	protected abstract void deleteHandle(int handle);

	public void clear() {

		// Clear set
		handles.clear();
	}

	public boolean isHandleValid(int handle) {
		return handles.contains(handle);
	}

	public void addHandle(int handle) {
		handles.add(handle);
	}

	public void removeHandle(int handle) {
		handles.remove(handle);
	}

	public void freeHandle(int handle) {
		deleteHandle(handle);
		removeHandle(handle);
	}
}
