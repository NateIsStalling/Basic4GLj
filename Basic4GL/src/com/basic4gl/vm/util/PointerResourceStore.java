package com.basic4gl.vm.util;


////////////////////////////////////////////////////////////////////////////////
//vmPointerResourceStore
//
//A vmResourceStore of pointers.
//null = blank.
//Pointer is deleted when removed.

public abstract class PointerResourceStore<T> extends ResourceStore<T> {
/* Java handles garbage collection
protected void DeleteElement (int index) {
delete vmResourceStore<T>.Value (index);                   // delete pointer
}
*/
public PointerResourceStore (){ super(null); }
}