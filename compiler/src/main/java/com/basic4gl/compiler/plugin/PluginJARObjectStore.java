package com.basic4gl.compiler.plugin;

import com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLObjectStore;
import com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLObjectStoreListener;
import com.basic4gl.runtime.Store;

/**
 * A resource store for plugin libraries
 */
public class PluginJARObjectStore implements Basic4GLObjectStore {
    private final Store<Object> store;
    private final Basic4GLObjectStoreListener callback;

    private void deleteObject(int handle) {
        // Free object
        Object object = getObject(handle);
        if (callback != null)
            callback.onObjectRemoved(this, object);
    }
    public PluginJARObjectStore(Basic4GLObjectStoreListener callback) {
        store = new Store<>(null);
        this.callback = callback;
        store.clear();			// Required to allocate single blank object
    }

    public int add(Object object){
        int handle = store.alloc();
        store.setValue(handle, object);
        return handle;
    }
    public void remove(int handle){
        if (isHandleValid(handle)) {

            // Free object
            deleteObject(handle);

            // Deallocate handle
            store.freeAtIndex(handle);
        }
    }
    public boolean isHandleValid(int handle) {
        return store.isIndexStored(handle);
    }
    public Object getObject(int handle) {
        return isHandleValid(handle) ? store.getValueAt(handle) : null;
    }
    public void clear(){
        // Delete each individual element
        // Note: Skip index 0, as this is the dummy no-object index.
        for (int i = 1; i < store.getArray().size(); i++) {
            if (store.getValAllocated().get(i)) {
                deleteObject(i);
            }
        }

        // Clear store
        store.clear();
    }
};
