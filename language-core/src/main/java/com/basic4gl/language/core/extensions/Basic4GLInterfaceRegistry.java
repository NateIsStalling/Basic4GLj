package com.basic4gl.language.core.extensions;

public interface Basic4GLInterfaceRegistry {

    /**
     * Register a shared interface to Basic4GL, which can be fetched and used
     * by other libraries.
     * <p>Libraries can register objects to Basic4GL (by text ID), so that other libraries
     * can fetch and use them. Basic4GL records that the library using the object
     * is dependent on the library that constructed it, and ensures that libraries are
     * unloaded in the correct order.
     * <p>The library that constructed the object should destroy it in
     * Basic4GLPlugin.unload() and NOT before.
     * <p>"name", "major" and "minor" are used to identify the object (major.minor
     * is its version number), so that other libraries can request it.
     *
     * @param serviceType   used to identify the object
     * @param service object to share
     * @param major  used to identify the object
     * @param minor  used to identify the object
     */
    <T> void registerInterface(Class<T> serviceType, T service, int major, int minor);

    /**
     * Register a shared interface to Basic4GL, which can be fetched and used
     * by other libraries.
     * <p>Libraries can register objects to Basic4GL (by text ID), so that other libraries
     * can fetch and use them. Basic4GL records that the library using the object
     * is dependent on the library that constructed it, and ensures that libraries are
     * unloaded in the correct order.
     * <p>The library that constructed the object should destroy it in
     * Basic4GLPlugin.unload() and NOT before.
     * <p>"name", "major" and "minor" are used to identify the object (major.minor
     * is its version number), so that other libraries can request it.
     *
     * @param serviceType   used to identify the object
     * @param service object to share
     * @param major  used to identify the object
     * @param minor  used to identify the object
     */
    <T> void registerInterfaceInternal(Class<T> serviceType, T service, int major, int minor);

    /**
     * Fetch an interface from another library.
     *
     * @param serviceType
     * @param major
     * @param minor
     * @return Returns the interface if a matching one is found. Returns null otherwise.
     */
    <T> T fetchInterface(Class<T> serviceType, int major, int minor);
}
