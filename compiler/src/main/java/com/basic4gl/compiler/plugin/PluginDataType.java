package com.basic4gl.compiler.plugin;

import com.basic4gl.runtime.TomVM;

import static com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLExtendedTypeCode.PLUGIN_BASIC4GL_EXT_PADDING;
import static com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLExtendedTypeCode.PLUGIN_BASIC4GL_EXT_STRING;
import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Represents a data type in a plugin structure.
 * Note: We store the C data type, which includes types not directly supported
 *     by Basic4GL, such as bytes, words, double (64 bit floating point
 *     numbers).
 *     The Basic4GL representation is derived from the C type.
 *     By knowing both the C and Basic4GL storage formats, we can convert
 *     between Basic4GL structures and C structures upon request.
 */
public class PluginDataType {

    // Basic data type.
    // Can be a DLL_Basic4GL_Ext_TypeCode constant, or a structure handle.
    private int baseType;

    // Level of pointer indirection.
    // 0 = Value (not a pointer at all)
    // 1 = Pointer
    // 2 = Pointer to pointer
    // etc
    private byte pointerLevel;

    // True if pointer is by reference.
    // (Applies when pointerLevel > 0 only).
    private boolean byReference;

    // # of array dimensions
    // 0 = Not an array
    // 1 = 1D array
    // 2 = 2D array etc
    private byte arrayLevel;

    // Array dimension sizes
    private final int[] arrayDims = new int[TomVM.ARRAY_MAX_DIMENSIONS];

    // String sizes (in characters, including 0 terminator. Applies when base
    // type = DLL_BASIC4GL_EXT_STRING only).
    // Also doubles as padding size in byte (type = DLL_BASIC4GL_EXT_PADDING)
    private int stringSize;


    /// Convert array type into array element type
    void makeIntoElementType() {
        assertTrue(arrayLevel > 0);
        arrayLevel--;
    }

    void deref() {
        assertTrue(pointerLevel > 0);
        pointerLevel--;
        byReference = false;
    }

    // Constructors
    public static PluginDataType padding(int bytes) {
        PluginDataType result = new PluginDataType();
        result.baseType = PLUGIN_BASIC4GL_EXT_PADDING;
        result.stringSize = bytes;
        return result;
    }

    public static PluginDataType simpleType(int baseType) {
        PluginDataType result = new PluginDataType();
        result.baseType = baseType;
        return result;
    }

    public static PluginDataType string(int size) {
        PluginDataType result = new PluginDataType();
        result.baseType = PLUGIN_BASIC4GL_EXT_STRING;
        result.stringSize = size;
        return result;
    }

    public byte getPointerLevel() {
        return pointerLevel;
    }

    public void setPointerLevel(byte pointerLevel) {
        this.pointerLevel = pointerLevel;
    }

    public int getBaseType() {
        return baseType;
    }

    public void setBaseType(int baseType) {
        this.baseType = baseType;
    }

    public boolean isByReference() {
        return byReference;
    }

    public void setByReference(boolean byReference) {
        this.byReference = byReference;
    }

    public byte getArrayLevel() {
        return arrayLevel;
    }

    public void setArrayLevel(byte arrayLevel) {
        this.arrayLevel = arrayLevel;
    }

    public int[] getArrayDims() {
        return arrayDims;
    }

    public int getStringSize() {
        return stringSize;
    }

    public void setStringSize(int stringSize) {
        this.stringSize = stringSize;
    }
}
