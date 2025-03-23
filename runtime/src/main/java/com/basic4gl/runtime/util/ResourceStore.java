package com.basic4gl.runtime.util;

import com.basic4gl.runtime.Store;

/**
 * Template implementation of vm Resources, using an internal Store
 */
public abstract class ResourceStore<T> extends Resources {

  protected Store<T> store;

  protected abstract void deleteElement(int index);

  @SuppressWarnings("unchecked")
  public ResourceStore(T blankElement) {
    store = new Store<T>(blankElement);
  }

  public void clear() {

    // Delete each individual element
    for (int i = 0; i < store.getArray().size(); i++) {
      if (store.getValAllocated().get(i)) {
        deleteElement(i);
      }
    }

    // Clear store
    store.clear();
  }

  // Pass calls through to internal store
  public boolean isIndexValid(int index) {
    return store.isIndexValid(index);
  }

  public boolean isIndexStored(int index) {
    return store.isIndexStored(index);
  }

  public T getValueAt(int index) {
    return store.getValueAt(index);
  }

  public void setValue(int index, T value) {
    store.setValue(index, value);
  }

  public int alloc() {
    return store.alloc();
  }

  public int alloc(T element) {
    if (element.equals(store.getBlankElement())) {
      return -1;
    } else {
      int index = alloc();
      setValue(index, element);
      return index;
    }
  }

  /**
   * Remove an element without freeing it.
   * Calling code is responsible for freeing the resource.
   * @param index
   */
  public void remove(int index) {
    store.freeAtIndex(index);
  }

  /**
   * Remove and free element.
   * @param index
   */
  public void free(int index) {
    if (isIndexValid(index)) {
      deleteElement(index);
      remove(index);
    }
  }

  public Store<T> getStore() {
    return store;
  }
}
