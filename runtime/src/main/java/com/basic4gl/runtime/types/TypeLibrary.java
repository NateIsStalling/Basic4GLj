package com.basic4gl.runtime.types;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Used to store structure definitions, and operate on data types
 */
public class TypeLibrary implements Streamable {
    private Vector<StructureField> fields;
    private Vector<Structure> structures;

    public TypeLibrary() {
        fields = new Vector<>();
        structures = new Vector<>();
    }

    public Vector<StructureField> getFields() {
        return fields;
    }

    public Vector<Structure> getStructures() {
        return structures;
    }

    public void clear() {
        fields.clear();
        structures.clear();
    }

    // Finding structures and fields
    public boolean isEmpty() {
        return structures.isEmpty();
    }

    public boolean isStrucStored(String name) {
        return getStrucIndex(name) >= 0;
    }

    public boolean isFieldStored(Structure struc, String fieldName) {
        return getFieldIndex(struc, fieldName) >= 0;
    }

    public int getStrucIndex(String name) {
        name = name.toLowerCase();
        for (int i = 0; i < structures.size(); i++) {
            if (structures.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public int getFieldIndex(Structure struc, String fieldName) {
        fieldName = fieldName.toLowerCase();
        for (int i = struc.firstFieldIndex; i < struc.firstFieldIndex + struc.fieldCount; i++) {
            assertTrue(i >= 0);
            assertTrue(i < fields.size());
            if (fields.get(i).name.equals(fieldName)) {
                return i;
            }
        }
        return -1;
    }

    // Data type operations
    public int getDataSize(ValType type) {

        // How big would a variable of type "type" be?
        // Pointers are always one element long
        if (type.getPhysicalPointerLevel() > 0) {
            return 1;
        }

        // Calculate array size
        if (type.arrayLevel > 0) {
            return type.getArraySize(getDataSize(new ValType(type.basicType)));
        }

        if (type.basicType >= 0) {

            // Structured type. Lookup and return size of structure.
            assertTrue(type.basicType < structures.size());
            return structures.get(type.basicType).dataSize;
        }

        // Otherwise is basic type
        return 1;
    }

    public boolean isDataSizeBiggerThan(ValType type, int size) {

        // Return true if data size > size.
        // This is logically equivalent to: DataSize (type) > size,
        // except it correctly handles integer overflow for really big types.
        assertTrue(isTypeValid(type));
        if (type.getPhysicalPointerLevel() == 0 && type.arrayLevel > 0) {
            ValType element = new ValType(type);
            element.arrayLevel = 0;
            return type.isArraySizeBiggerThan(size, getDataSize(element));
        } else {
            return getDataSize(type) > size;
        }
    }

    public boolean isTypeValid(ValType type) {
        return type.basicType >= BasicValType.VTP_INT
                && type.basicType != BasicValType.VTP_UNDEFINED
                && (type.basicType < 0 || type.basicType < structures.size())
                && type.arrayLevel < TomVM.ARRAY_MAX_DIMENSIONS;
    }

    public boolean containsString(ValType type) {
        assertTrue(isTypeValid(type));

        // Pointers to objects don't *contain* anything.
        // (Pointing to something that contains a string doesn't count.)
        if (type.getVirtualPointerLevel() > 0) {
            return false;
        }

        // Examine data type
        if (type.basicType < 0) {
            return type.basicType == BasicValType.VTP_STRING;
        } else {
            return structures.get(type.basicType).containsString;
        }
    }

    public boolean containsArray(ValType type) {
        assertTrue(isTypeValid(type));

        return type.getVirtualPointerLevel() == 0 // Must not be a pointer
                && (type.arrayLevel > 0 // Can be an array
                        || (type.basicType >= 0 && structures.get(type.basicType).containsArray)); // or
        // a
        // structure
        // containing
        // an
        // array
    }

    public boolean containsPointer(ValType type) {
        assertTrue(isTypeValid(type));

        // Type is a pointer?
        if (type.pointerLevel > 0) {
            return true;
        }

        // Is a structure (or array of structures) containing a pointer?
        if (type.basicType >= 0) {
            return structures.get(type.basicType).containsPointer;
        }

        return false;
    }

    // Building structures
    public Structure getCurrentStruc() {
        assertTrue(!isEmpty()); // Must have at least one structure
        return structures.lastElement();
    }

    public StructureField getCurrentField() {
        // Current  structure must have at least 1 field
        assertTrue(fields.size() > getCurrentStruc().firstFieldIndex);

        return fields.lastElement();
    }

    /**
     * Create a new structure and make it current
     * @param name
     * @return current struc
     */
    public Structure createStruc(String name) {
        // Name must be valid and not already used
        assertTrue(!name.isEmpty());
        assertTrue(!isStrucStored(name));

        // Create new structure
        structures.add(new Structure(name, fields.size()));
        return getCurrentStruc();
    }

    /**
     * Create a new field and assign it to the current structure
     * @param name
     * @param type
     * @return current field
     */
    public StructureField createField(String name, ValType type) {

        // Name must be valid and not already used within current structure
        assertTrue(!name.isEmpty());
        assertTrue(!isFieldStored(getCurrentStruc(), name));

        // Type must be valid, and not an instance of the current structure
        // type
        // (or an array. Can be a pointer though.)
        assertTrue(isTypeValid(type));
        assertTrue(!type.isByRef);
        assertTrue(type.pointerLevel > 0 || type.basicType < 0 || type.basicType + 1 < structures.size());

        // Create new field
        fields.add(new StructureField(name, type, getCurrentStruc().dataSize));
        getCurrentStruc().fieldCount++;
        getCurrentStruc().dataSize += getDataSize(getCurrentField().type);

        // Update current structure statistics
        getCurrentStruc().containsString = getCurrentStruc().containsString || containsString(type);
        getCurrentStruc().containsArray = getCurrentStruc().containsArray || containsArray(type);
        getCurrentStruc().containsPointer = getCurrentStruc().containsPointer || containsPointer(type);
        return getCurrentField();
    }

    // Debugging/output
    public String describeVariable(String name, ValType type) {

        if (!isTypeValid(type)) {
            return "INVALID TYPE " + name;
        }

        // Return a string describing what the variable stores
        String result;

        // Var type
        if (type.basicType >= 0) {
            // Structure type
            result = structures.get(type.basicType).name + " ";
        } else {
            // Basic type
            result = ValType.getBasicValTypeName(type.basicType) + " ";
        }

        // Append pointer prefix
        int i;
        for (i = 0; i < type.getVirtualPointerLevel(); i++) {
            result += "&";
        }

        // Var name
        result += name;

        // Append array indices
        for (i = type.arrayLevel - 1; i >= 0; i--) {
            result += "(";
            if (type.getVirtualPointerLevel() == 0) {
                result += String.valueOf(type.arrayDimensions[i] - 1);
            }
            result += ")";
        }

        return result;
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {
        int i;
        // Write out fields
        Streaming.writeLong(stream, fields.size());
        for (i = 0; i < fields.size(); i++) {
            fields.get(i).streamOut(stream);
        }

        // Write out structures
        Streaming.writeLong(stream, structures.size());
        for (i = 0; i < structures.size(); i++) {
            structures.get(i).streamOut(stream);
        }
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        int i, count;
        // Clear existing data
        fields.clear();
        structures.clear();

        // Read fields
        count = (int) Streaming.readLong(stream);
        fields.setSize(count);
        for (i = 0; i < count; i++) {
            fields.set(i, new StructureField());
            fields.get(i).streamIn(stream);
        }

        // Read structures
        count = (int) Streaming.readLong(stream);
        structures.setSize(count);
        for (i = 0; i < count; i++) {
            structures.set(i, new Structure());
            structures.get(i).streamIn(stream);
        }
        return true;
    }
}
