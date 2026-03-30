package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * Used to register functions to Basic4GL.
 */
public interface Basic4GLFunctionRegistry {


    //////////////////////////////////////////////////////////////////////////////
    //	Constants

    /**
     * Register a constant
     *
     * @param name
     * @param value
     */
    void registerStringConstant(
            String name,
            String value);

    /**
     * Register a constant
     *
     * @param name
     * @param value
     */
    void registerIntConstant(
            String name,
            int value);

    /**
     * Register a constant
     *
     * @param name
     * @param value
     */
    void registerFloatConstant(
            String name,
            float value);

    ///////////////////////////////////////////////////////////////////////////
    //	Standard functions

    /**
     * Register function that returns no value
     *
     * @param name
     * @param function
     */
    void registerVoidFunction(
            String name,
            Basic4GLFunction function);

    /**
     * Register function that returns single value
     *
     * @param name
     * @param function
     * @param typeCode
     */
    void registerFunction(
            String name,
            Basic4GLFunction function,
            Basic4GLTypeCode typeCode);

    /**
     * Register function that returns an array.
     * Note: This method has been superceeded by modReturnArray(), and is retained
     * only for backwards compatibility with older plugins.
     *
     * @param name
     * @param function
     * @param typeCode
     * @param dimensions
     */
    void registerArrayFunction(
            String name,
            Basic4GLFunction function,
            Basic4GLTypeCode typeCode,
            int dimensions);

    /**
     * Register function that returns a structure.
     * structureTypeHandle is the handle returned from registerStructure(), or fetchStructure().
     *
     * @param name
     * @param function
     * @param structureTypeHandle
     */
    void registerStructureFunction(
            String name,
            Basic4GLFunction function,
            int structureTypeHandle);

    /**
     * Modifies the last registered function to return an array.
     * E.g. if the function was registered as returning an integer, this will
     * alter it to return an array of integers.
     *
     * @param dimensions
     */
    void modReturnArray(int dimensions);


    /**
     * Modifies the last registered function to return a pointer.
     * E.g. if the function was registered as returning a string, this will
     * alter it to return a pointer to a string.
     * <p>
     * Note:
     * Returning pointers is an advanced operation, and you need to be
     * careful that you understand Basic4GL's data formats and know
     * exactly what you're doing.
     * Unless you know exactly why you need to return a pointer from
     * a DLL function, you should avoid this method.
     * <p>
     * Level is the level of indirection:
     * 1 = Pointer (most common)
     * 2 = Pointer to pointer
     * ...
     *
     * @param level
     */
    void modReturnPointer(int level);

    /**
     * Modifies the last registered function to have no brackets.
     * Note:
     * By convention ALL Basic4GL functions have brackets except for a special
     * subset of historical commands such as PRINT and LOCATE.
     * Please try to stick to this convention, unless the function really is an
     * historical BASIC command.
     */
    void modNoBrackets();

    /**
     * Modifies the last registered function to trigger a timesharing break
     * after it is called.
     * <p>If your function will (or may) take a reasonable amount of time to
     * complete (say 0.01 seconds or more), you should use this mod to ensure
     * Basic4GL forces a timeshare break after it, so that it stays responsive.
     * Some examples are:
     * - File I/O functions
     * - Pausing timer based functions
     */
    void modTimeshare();

    ///////////////////////////////////////////////////////////////////////////
    //	Standard function parameters

    /**
     * Add standard parameter to the last registered function.
     * Parameters should be added from left to right.
     * Please be aware that the RIGHTMOST parameter will have index 1 when the
     * function is called at runtime, i.e. the LAST parameter added.
     *
     * @param typeCode
     */
    void addParam(Basic4GLTypeCode typeCode);

    /**
     * Add an array parameter to the last registered function.
     * Note: This method has been superceeded by ModParamArray(), and is retained
     * only for backwards compatibility with older plugins.
     *
     * @param typeCode
     * @param dimensions
     */
    void addArrayParam(
            Basic4GLTypeCode typeCode,
            int dimensions);

    /**
     * Add a structure parameter to the last registered function.
     * 'handle' is the structure type handle returned from "RegisterStructure" or "FetchStructure"
     *
     * @param handle
     */
    void addStrucParam(int handle);

    /**
     * Convert the last parameter added into an array parameter.
     *
     * @param dimensions
     */
    void modParamArray(int dimensions);

    /**
     * Convert the last parameter added into a pointer.
     * 'level' is the level of indirection:
     * Using pointers is an advanced function, and you need to be sure you know
     * what you're doing. If in doubt, avoid...
     * 1 = Pointer (most common)
     * 2 = Pointer to pointer etc
     *
     * @param level
     */
    void modParamPointer(int level);

    /**
     * Convert the last parameter added into a reference parameter.
     * This is like Pascal's "var" parameters, or C++'s & parameters.
     * They can be used to return values.
     * Note: The parameter will appear as a pointer to the plugin function,
     * and should be accessed like one.
     * Note 2: Structure and array parameters are automatically passed by
     * reference, so you don't need to call this method for them.
     */
    void modParamReference();

    ///////////////////////////////////////////////////////////////////////////
    //	Structures

    /**
     * Register a new structure.
     * Returns a handle allowing the structure to be referenced in function
     * or parameter declarations.
     * Will return 0 if fails (can fail if the structure has been registered
     * already). Plugin should set an appropriate error and return false.
     *
     * @param name
     * @param versionMajor
     * @param versionMinor
     * @return
     */
    int registerStructure(
            String name,
            int versionMajor,
            int versionMinor);

    ///////////////////////////////////////////////////////////////////////////
    // Structure fields

    /**
     * Add empty padding
     *
     * @param numBytes
     */
    void addStrucPadding(int numBytes);

    /**
     * Add regular field.
     *
     * @param name
     * @param type can be a Basic4GLExtendedTypeCode constant, or structure handle.
     */
    void addStrucField(String name, int type);

    /**
     * Add a string field.
     * Must supply string size in bytes (including 0 terminator)
     *
     * @param name
     * @param size
     */
    void addStrucStringField(String name, int size);

    /**
     * Convert the last defined structure field into an array
     *
     * @param dimensions
     * @param dimension1Size
     */
    void modStrucFieldArray(int dimensions, int dimension1Size, int ...otherSizes);

    /**
     * Convert the last defined structure field into a pointer
     * Note: 'level' = the level of indirection.
     * 1 = Pointer (most common)
     * 2 = Pointer to pointer
     * ...
     *
     * @param level
     */
    void modStrucFieldPointer(int level);

    /**
     * Fetch structure (presumably registered by another plugin).
     *
     * @param name
     * @param versionMajor
     * @param versionMinor
     * @return Returns handle, or 0 if not registered.
     */
    int fetchStructure(
            String name,
            int versionMajor,
            int versionMinor);

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
     * @param object object to share
     * @param name   used to identify the object
     * @param major  used to identify the object
     * @param minor  used to identify the object
     */
    void registerInterface(
            Object object,
            String name,
            int major,
            int minor);

    /**
     * Fetch an interface from another library.
     *
     * @param name
     * @param major
     * @param minor
     * @return Returns the interface if a matching one is found. Returns null otherwise.
     */
    Object fetchInterface(
            String name,
            int major,
            int minor);

    /**
     * Create an object store.
     * These are objects that store miscellaneous objects, and assign them
     * integer IDs (that can be assigned to Basic4GL variables).
     *
     * @param listener (can be null if not used) is called to notify the library whenever an object is removed.
     *                 Note: The library should not destroy this object. It will be destroyed automatically
     *                 by Basic4GL when the library is unloaded.
     * @return
     */
    Basic4GLObjectStore createObjectStore(Basic4GLObjectStoreListener listener);

    /**
     * Modifies the last registered function to trigger a timesharing break
     * after it is called, IF the "timeshare" flag has been set in the virtual machine.
     * See Basic4GLRuntime.isTimeshareBreakRequired().
     */
    void modConditionalTimeshare();
}
