package com.basic4gl.compiler.plugin.sdk.plugin;

import java.nio.ByteBuffer;

/**
 * The main interface to Basic4GL for runtime functions in DLLs. This interface
 * is passed to runtime functions when they are called, and is used to perform
 * necessary services such as fetching function parameters, setting return
 * values and converting between Basic4GL and standard C/C++ formats.
 */
public interface Basic4GLRuntime {

    /// Fetch function parameter.
    /// Parameters are numbered from right to left, the right most parameter
    /// being index 1.
    /// The request must be correct and correspond to the function definition.
    /// I.e. if you declare paramter 3 as an integer, you MUST request it as
    /// an integer. If you declare a 2 parameter function, you MUST not request
    /// parameter 4.
    /// If you do so, Basic4GL's behaviour will be undefined, and it will likely
    /// crash.
    ///
    /// Basic4GL has just 3 basic types:
    /// int, float (called "real" in Basic4gl documentation) and string (char*)
    int getIntParam(int index);
    float getFloatParam(int index);
    String getStringParam(int index);

    /// Set function return value.
    /// Be sure to call the appropriate function for your return value type,
    /// as declared in your function declaration.
    /// (If your function doesn't return a value, don't call anything...)
    /// Otherwise behaviour is undefined and Basic4GL could crash.
    void setIntResult(int result);
    void setFloatResult(float result);
    void setStringResult(String result);

    /// Get array parameter, and convert to C/C++ style array
    /// Note: Will convert to a fixed size array.
    /// If the array from the Basic4GL program size does not match, it will
    /// simply ignore any overhang from the Basic4GL array, and leave any overhang
    /// from the target array set to 0.
    void getIntArrayParam(
            int index,
            int[] array,						// Data is returned here. Must be as big as the array params.
            int dimensions,                 // 10 is the maximum # of dimesions supported by Basic4GL
            int dimension0Size,int... args);
    void getFloatArrayParam(
            int index,
            float[] array,					// Data is returned here. Must be as big as the array params.
            int dimensions,                 // 10 is the maximum # of dimesions supported by Basic4GL
            int dimension0Size,
            int... args);

    /// Return the size of an array dimension.
    /// "index" is the parameter index.
    /// "dimension" is the dimension index, 0 origin.
    /// The return value is the actual number of elements in the array (for that dimension).
    /// Need to keep in mind that the BASIC dim command actually allocates one more element
    /// than the number specified (0..N inclusive).
    /// So if an array is DIMmed as: dim a(10)
    /// this method will return 11 as the number of elements.
    int getArrayParamDimension(
            int index,
            int dimension);

    /// Set array parameter result
    void setIntArrayResult(
            int[] array,
            int dimensions,
            int dimension0Size,int... args);
    void setFloatArrayResult(
            float[] array,
            int dimensions,
            int dimension0Size,int... args);

    //---------------------------------------------------------------------------------------------
    // Extended data access methods
    //
    // These methods handle converting data between C variables/structures and Basic4GL parameters
    // and return values.
    //
    // Note: Unlike the above methods, when specifying a data type you must specify the C data type
    //	using the DLL_Basic4GL_Ext_TypeCode codes. Basic4GL will automatically translate between the
    //	specified type and its own data types appropriately.
    //
    // Reading/writing data is a 2 step format:
    //
    //	1. Define the data type using one of the following methods:
    //		* SetType
    //		* SetStringType
    //	   You may also need to use one of the following modifiers:
    //		* ModTypeArray
    //		* ModTypeReference
    //
    //	2. Call the appropriate method to copy and convert the data. One of:
    //		* GetParam
    //		* SetParam
    //		* SetReturnValue

    // Data type definition for data conversion
    void setType(int baseType);					// Can either be a DLL_Basic4GL_Ext_TypeCode, or a structure type handle (returned from RegisterStructure or FetchStructure)
    void setStringType(int size);				// Number of characters (including terminating 0)

    /// Convert a type into an array
    void modTypeArray(
            int dimensions,
            int dimension0Size,
		int ...otherDimensions);

    /// Convert a type into a reference.
    /// Useful for accessing reference parameters.
    /// Only required if the parameter is a reference to a basic type (int, float or string).
    /// Basic4GL automatically treats arrays and structures as by-reference types, and does
    /// not need to be told!
    void modTypeReference();

    // Reading/writing parameters and return values

    /**
     *
     * @param index Parameter index
     * @param dest Data is copied here
     * @return
     */
    Object getParam(int index, ByteBuffer dest);

    /**
     *
     * @param index Parameter index
     * @param src Data is copied from here
     */
    void setParam(int index, ByteBuffer src);

    /**
     *
     * @param src Data is copied from here
     */
    void setReturnValue(ByteBuffer src);

    //---------------------------------------------------------------------------------------------
    // Direct data access.
    // These methods allow you to access Basic4GL variable memory directly.
    // To use these methods you must have a good understanding of the underlying
    // Basic4GL data structures, as reading/writing data incorrectly can cause
    // crashes and lockups.
    // These methods should be used as a last resort, if none of the other methods
    // can will achieve the result you need.

    /// Get int value at address
    int directGetInt(int memAddr);

    /// Get float value at address
    float directGetFloat(int memAddr);

    /// Get string value at address.
    /// 'str' and 'maxlen' describe the buffer into which the string will be
    /// copied (if it is longer than maxLen-1, it will be truncated).
    /// The method always returns 'buffer'
    char[] directGetString(int memAddr, char[] str, int maxLen);

    /// Write int value to address
    void directSetInt(int memAddr, int value);

    /// Write float value to address
    void directSetFloat(int memAddr, float value);

    /// Write real value to address
    void directSetString(int memAddr, String str);
    //---------------------------------------------------------------------------------------------

    /// Begin a long running function.
    /// This should be called from a regular BASIC function which should then immediately return.
    /// *** THE FUNCTION MUST ALSO BE REGISTERED WITH A TIMESHARING BREAK (using ModTimeShare()). ***
    /// Basic4GL will not execute any more VM op-codes until the long running function handler
    /// calls EndLongRunningFn().
    /// The main application event loop will continue to run however, so that operating system events
    /// can be processed. Basic4GL can poll the long running function handler if required
    /// (see the IDLL_Basic4GL_LongRunningFunction interface for more info).
    ///
    /// An example of a long running function would be stopping and waiting for a user to key in a
    /// text string.
    /// * The regular BASIC function would create the handler object, hook it up to receive
    ///   keypress events and call BeginLongRunningFn().
    /// * Basic4GL will perform a timesharing break after the regular function returns (because it is
    ///   marked timesharing with ModTimeShare()).
    /// * Basic4GL will then process operating system events, but not execute any VM op-codes.
    /// * The handler will process keypress events and update the display as necessary.
    /// * Once the user presses ENTER, the handler will set the result (using SetStringResult())
    ///   and call EndLongRunningFn().
    /// * Basic4GL will then resume excuting VM op-codes.
    void beginLongRunningFunction(Basic4GLLongRunningFunction handler);

    /// End a long running function and resume executing BASIC program op-codes.
    void endLongRunningFunction();

    /// Indicate an error from a function
    void  functionError(String text);

    /// Indicates to the virtual machine that a timesharing break is required.
    /// If the BASIC function has been modified with:
    /// IDLL_Basic4GL_FunctionRegistry.ModTimeshareBreak()
    /// this will trigger a time sharing break after the function returns.
    /// This is useful for creating conditional timesharing breaks, when it
    /// is not known until runtime whether the break is required.
    void  isTimeshareBreakRequired();
}
