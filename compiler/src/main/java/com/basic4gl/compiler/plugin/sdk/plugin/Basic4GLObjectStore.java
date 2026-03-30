package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * These are optional utility objects that your DLL can request from Basic4GL.
 * <p>They are used to associate an object with an integer handle. (Basic4GL
 * variables cannot store object references, but can store integer handles).
 * Basic4GL also tracks all objects allocated with an object store, and will
 * call your plugin to dispose of any that are still active when the Basic4GL
 * program completes.
 */
public interface Basic4GLObjectStore {

    /**
     * Add an object and return a new unique integer handle
     * @param object
     * @return unique integer handle
     */
    int add(Object object);

    /**
     * Remove an object, and free up its handle.
     * This also calls the dispose-of-object callback.
     * @param handle
     */
    void remove(int handle);

    /// Return true if handle is valid.
    boolean isHandleValid(int handle);

    /// Return object for given handle, or NULL if none.
    Object getObject(int handle);

    /// Remove ALL objects and free up handles.
    /// The dispose-of-object callback is called for each object.
    void clear();
};