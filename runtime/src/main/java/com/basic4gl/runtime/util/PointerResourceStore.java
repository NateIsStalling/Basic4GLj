package com.basic4gl.runtime.util;

/**
 * A vm ResourceStore of pointers.
 * null = blank.
 * Pointer is deleted when removed.
 */
public class PointerResourceStore<T> extends ResourceStore<T> {
/* Java handles garbage collection
protected void DeleteElement (int index) {
delete vmResourceStore<T>.Value (index);                   // delete pointer
}
*/
public PointerResourceStore() {
	super(null);
}

public void deleteElement(int index) {
	store.setValue(index, null); // delete pointer
}
}
