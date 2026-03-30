package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * Extended type constants
 *
 * <p>Used when defining structures.
 * Basic4GL's data structures don't support all the types defined here.
 * <p>However, you can use these to define the data structure as it would be stored in C.
 * Basic4GL will then use this information when converting data between its own
 * format and C structure format.
 */
public class Basic4GLExtendedTypeCode {
    /**
     * Empty padding in C structure
     */
    public static final int PLUGIN_BASIC4GL_EXT_PADDING = -256;
    public static final int PLUGIN_BASIC4GL_EXT_BYTE = -255;
    public static final int PLUGIN_BASIC4GL_EXT_WORD = -254;
    public static final int PLUGIN_BASIC4GL_EXT_INT = -253;
    /**
     * 64 bit integer
     */
    public static final int PLUGIN_BASIC4GL_EXT_INT64 = -252;
    /**
     * 32 bit floating point number
     */
    public static final int PLUGIN_BASIC4GL_EXT_FLOAT = -251;
    /**
     * 64 bit floating point number
     */
    public static final int PLUGIN_BASIC4GL_EXT_DOUBLE = -250;
    /**
     * Fixed length string
     */
    public static final int PLUGIN_BASIC4GL_EXT_STRING = -249;
}
