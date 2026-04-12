package com.basic4gl.runtime.plugin;


/**
 * Implement this interface to be notified when objects have been removed
 * from an Basic4GLObjectStore.
 */
public interface Basic4GLObjectStoreListener {
    /// Called when an object is removed, either by:
    /// * An explicit call to Basic4GLObjectStore.remove()
    /// * An explicit call to Basic4GLObjectStore.clear()
    /// * An implicit clear, such as when a Basic4GL program completes.
    void onObjectRemoved(Basic4GLObjectStore store, Object object);
}