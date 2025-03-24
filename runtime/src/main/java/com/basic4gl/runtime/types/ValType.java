package com.basic4gl.runtime.types;

import static com.basic4gl.runtime.types.BasicValType.*;
import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * VmValType
 * An extended value type.
 * Can be either of:
 * - A basic value
 * - A structure
 * - An array (one or more dimensions) of either of the above.
 * Note: Indexing an N dimensional results in an N-1 dimensional array.
 * Unless N = 1, in which case it results in an element.
 * - A pointer to any of the above.
 * - A "value by reference".
 * This is a pointer 'pretending' to be a value. Used to refer to
 * structures and anything else that can't fit into a register.
 */
public class ValType implements Streamable {

    public int basicType; // Basic type
    public byte arrayLevel; // 0 = value, 1 = array, 2 = 2D array
    public byte pointerLevel; // 0 = value, 1 = pointer to value, 2 =
    // pointer to pointer to value, ...
    public boolean isByRef;
    public int[] arrayDimensions = new int[TomVM.ARRAY_MAX_DIMENSIONS]; // # of

    // elements
    // in
    // each
    // array
    // dimension

    public ValType() {
        setType(VTP_UNDEFINED);
    }

    public ValType(int type) {
        setType(type);
    }

    public ValType(ValType type) {
        setType(type);
    }

    public ValType(int type, byte array) {
        this(type, array, (byte) 0, false);
    }

    public ValType(int type, byte array, byte pointer, boolean byRef) {
        setType(type, array, pointer, byRef);
    }

    /**
     * Displaying basic types and values
     * @param type
     * @return
     */
    static String getBasicValTypeName(int type) {
        if (type < 0) {
            switch (type) {
                case VTP_INT:
                    return "INT";
                case VTP_REAL:
                    return "REAL";
                case VTP_STRING:
                    return "STRING";
                case VTP_NULL:
                    return "null";
                case VTP_UNDEFINED:
                    return "UNDEFINED";
                default:
                    return "???";
            }
        } else {
            return "ADVANCED TYPE";
        }
    }

    public ValType setType(int type) {
        return setType(type, (byte) 0, (byte) 0, false);
    }

    public ValType setType(int type, byte array, byte pointer, boolean byRef) {
        assertTrue(array <= TomVM.ARRAY_MAX_DIMENSIONS);
        basicType = type;
        arrayLevel = array;
        pointerLevel = pointer;
        isByRef = byRef;

        Arrays.fill(arrayDimensions, 0);
        return this;
    }

    public ValType setType(ValType t) {
        basicType = t.basicType;
        arrayLevel = t.arrayLevel;
        pointerLevel = t.pointerLevel;
        isByRef = t.isByRef;
        arrayDimensions = Arrays.copyOf(t.arrayDimensions, TomVM.ARRAY_MAX_DIMENSIONS);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValType valType = (ValType) o;

        // TODO generated return basicType == valType.basicType && arrayLevel == valType.arrayLevel &&
        // pointerLevel == valType.pointerLevel && isByRef == valType.isByRef &&
        // Arrays.equals(arrayDimensions, valType.arrayDimensions);

        // Return true if types match
        // Compare basic types and array and pointer levels
        if (basicType != valType.basicType
                || arrayLevel != valType.arrayLevel
                || pointerLevel != valType.pointerLevel) {
            return false;
        }

        // Compare array dimensions (if not a pointer)
        if (pointerLevel == 0) {
            for (int i = 0; i < arrayLevel; i++) {
                if (arrayDimensions[i] != valType.arrayDimensions[i]) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(basicType, arrayLevel, pointerLevel, isByRef);
        result = 31 * result + Arrays.hashCode(arrayDimensions);
        return result;
    }

    public boolean matchesType(int type) {
        return equals(new ValType(type));
    }

    /**
     * Equals returns true if the types are identical in implementation.
     * This means it will return true if one is a pointer and the other
     * is a reference (as both are the same internally).
     *
     * Porting Note: The overloaded == operator uses {@link #equals(Object)} function.
     *
     * @param type
     * @return returns false if one is a pointer and the other is a reference.
     */
    public boolean exactEquals(ValType type) {

        if (!equals(type)) {
            return false;
        }

        return isByRef == type.isByRef;
    }

    public boolean exactEquals(int type) {
        return exactEquals(new ValType(type));
    }

    public boolean isNull() {
        return basicType == VTP_NULL;
    }

    public int getPhysicalPointerLevel() {
        return pointerLevel;
    }

    public int getVirtualPointerLevel() {
        return pointerLevel + (isByRef ? -1 : 0);
    }

    public void addDimension(int elements) {
        assertTrue(arrayLevel < TomVM.ARRAY_MAX_DIMENSIONS);
        assertTrue(elements > 0);

        // Bump up existing elements
        for (int i = arrayLevel; i > 0; i--) {
            arrayDimensions[i] = arrayDimensions[i - 1];
        }
        arrayLevel++;

        // Add new element count at dimension 0
        arrayDimensions[0] = elements;
    }

    /**
     * Calculate the array size based on the element size
     * @param elementSize
     * @return array size
     */
    public int getArraySize(int elementSize) {

        int result = elementSize;

        // Note: Array data format is
        // Data Data size (# 4 byte values)
        // ---- ---------
        // * Count 1
        // * Element size 1
        // * Element 0 Element size
        // * Element 1 Element size
        // ...
        // * Element Count - 1 Element size
        //
        // Thus total storage size (for 1D array) = 2 + (Count * Element
        // size)
        // An N+1 dimension array is simply an array of N dimension arrays.

        for (int i = 0; i < arrayLevel; i++) {
            result *= arrayDimensions[i];
            result += 2;
        }

        return result;
    }

    /**
     * This is logically equivalent to "ArraySize (elementSize) > size",
     * except ArraySize can fail if the calculated size doesn't fit into an integer.
     *
     * A major goal of Basic4GL is to provide a safe environment to experiment
     * in without worrying about breaking things.
     * Therefore we want to prevent people from trying to allocate
     * unrealistic amounts of memory.
     * Thus we check array sizes upon allocation/declaration.
     * @param size
     * @param elementSize
     * @return Returns true if the array size is bigger than size.
     */
    public boolean isArraySizeBiggerThan(int size, int elementSize) {

        int arraySize = elementSize;
        if (arraySize > size) {
            return true;
        }

        for (int i = 0; i < arrayLevel; i++) {
            if (size < 2 || (size - 2) / arrayDimensions[i] < arraySize) {
                return true;
            }
            arraySize *= arrayDimensions[i];
            arraySize += 2;
        }

        return false;
    }

    /**
     * Pointers fit in a register, or single basic types
     * @return
     */
    public boolean canStoreInRegister() {
        return pointerLevel > 0 || (arrayLevel == 0 && basicType < 0);
    }

    /**
     * Return the actual type that will be stored in a register when
     * referring to data of this type.
     * For values that fit into a register, the register type is the
     * same as the original type represented.
     *
     * For large values like structures and arrays, the register will
     * store an implicit reference to the data instead.
     */
    public ValType getRegisterType() {

        // Copy this value type
        ValType result = new ValType(this);

        // Check if type is an array or structure
        if (!result.canStoreInRegister()) {

            // A structure or array cannot fit into a register.
            // What is stored is an implicit by-reference pointer.
            result.pointerLevel++;
            result.isByRef = true;
        }

        return result;
    }

    /**
     * Type of actual data stored inside virtual machine register.
     * @return
     */
    public int getStoredType() {
        if (pointerLevel == 0 && arrayLevel == 0) {
            return basicType;
        } else {
            return VTP_INT;
        }
    }

    public boolean isBasicType() {
        return pointerLevel == 0 && arrayLevel == 0 && basicType < 0;
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {

        // Write VmValType to stream
        Streaming.writeLong(stream, basicType);

        Streaming.writeByte(stream, arrayLevel);
        Streaming.writeByte(stream, pointerLevel);
        Streaming.writeByte(stream, (byte) (isByRef ? 1 : 0));

        for (int i = 0; i < TomVM.ARRAY_MAX_DIMENSIONS; i++) {
            Streaming.writeLong(stream, arrayDimensions[i]);
        }
    }

    public boolean streamIn(DataInputStream stream) throws IOException {

        // Read VmValType from stream
        basicType = (int) Streaming.readLong(stream);

        arrayLevel = Streaming.readByte(stream);
        pointerLevel = Streaming.readByte(stream);
        isByRef = Streaming.readByte(stream) == 1;

        for (int i = 0; i < TomVM.ARRAY_MAX_DIMENSIONS; i++) {
            arrayDimensions[i] = (int) Streaming.readLong(stream);
        }

        return true;
    }
}
