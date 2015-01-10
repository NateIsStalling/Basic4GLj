package com.basic4gl.vm.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.util.Constants;

////////////////////////////////////////////////////////////////////////////////
// VmValType
//
// An extended value type.
// Can be either of:
// * A basic value
// * A structure
// * An array (one or more dimensions) of either of the above.
// Note: Indexing an N dimensional results in an N-1 dimensional array.
// Unless N = 1, in which case it results in an element.
// * A pointer to any of the above.
// * A "value by reference".
// This is a pointer 'pretending' to be a value. Used to refer to
// structures and anything else that can't fit into a register.

public class ValType {
	// //////////////////////////////////////////////////////////////////////////////
	// BasicValType
	//
	// Basic4GL data types are indicated by an integer.
	// -1 is special, and indicates an undefined type.
	// Other negative numbers are the basic supported types of language. That
	// is,
	// string, integer and floating point ("real").
	//
	// Positive numbers represent structure types, and the value is the index of
	// the structure in the structures array.
	//
	// Note: Basic value types are loaded into registers directly.
	// Other types (arrays and structures) are loaded as pointers.

	public static final int	VTP_INT = -5;
	public static final int	VTP_REAL = -4;
	public static final int	VTP_STRING = -3;
	public static final int	VTP_NULL = -2;
	public static final int	VTP_UNDEFINED = -1;

	public int m_basicType; // Basic type
	public byte m_arrayLevel; // 0 = value, 1 = array, 2 = 2D array
	public byte m_pointerLevel; // 0 = value, 1 = pointer to value, 2 =
								// pointer to pointer to value, ...
	public boolean m_byRef;
	public int[] m_arrayDims = new int[Constants.VM_MAXDIMENSIONS]; // # of
																	// elements
																	// in
																	// each
																	// array
																	// dimension

	public ValType() {
		Set(VTP_UNDEFINED);
	}
	public ValType(int type) {
		Set(type);
	}

	public ValType(ValType type) {
		Set(type);
	}

	public ValType(int type, byte array) {
		this(type, array, (byte) 0, false);
	}

	public ValType(int type, byte array, byte pointer, boolean byRef) {
		Set(type, array, pointer, byRef);
	}

	// Displaying basic types and values
	static String BasicValTypeName(int type) {
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
		} else
			return "ADVANCED TYPE";
	}

	public ValType Set(int type) {
		return Set(type, (byte) 0, (byte) 0, false);
	}
	public ValType Set(int type, byte array, byte pointer, boolean byRef) {
		assert (array <= Constants.VM_MAXDIMENSIONS);
		m_basicType = type;
		m_arrayLevel = array;
		m_pointerLevel = pointer;
		m_byRef = byRef;

		Arrays.fill(m_arrayDims, 0);
		return this;
	}

	public ValType Set(ValType t) {
		m_basicType = t.m_basicType;
		m_arrayLevel = t.m_arrayLevel;
		m_pointerLevel = t.m_pointerLevel;
		m_byRef = t.m_byRef;
		m_arrayDims = Arrays.copyOf(t.m_arrayDims, Constants.VM_MAXDIMENSIONS);
		return this;
	}

	public boolean Equals(ValType t) {

		// Return true if types match
		// Compare basic types and array and pointer levels
		if (m_basicType != t.m_basicType || m_arrayLevel != t.m_arrayLevel
				|| m_pointerLevel != t.m_pointerLevel)
			return false;

		// Compare array dimensions (if not a pointer)
		if (m_pointerLevel == 0)
			for (int i = 0; i < m_arrayLevel; i++)
				if (m_arrayDims[i] != t.m_arrayDims[i])
					return false;

		return true;
	}

	public boolean Equals(int type) {
		return Equals(new ValType(type));
	}

	public boolean ExactEquals(ValType type) {

		// Equals returns true if the types are identical in implementation.
		// This means it will return true if one is a pointer and the other
		// is a
		// reference (as both are the same internally).

		// ExactEquals returns false if one is a pointer and the other is a
		// reference.

		// Note: The overloaded == operator uses Equals function.

		if (!Equals(type))
			return false;

		if (m_byRef != type.m_byRef)
			return false;

		return true;
	}

	public boolean ExactEquals(int type) {
		return ExactEquals(new ValType(type));
	}

	public boolean IsNull() {
		return m_basicType == VTP_NULL;
	}

	public int PhysicalPointerLevel() {
		return m_pointerLevel;
	}

	public int VirtualPointerLevel() {
		return m_pointerLevel + (m_byRef ? -1 : 0);
	}

	public void AddDimension(int elements) {
		assert (m_arrayLevel < Constants.VM_MAXDIMENSIONS);
		assert (elements > 0);

		// Bump up existing elements
		for (int i = m_arrayLevel; i > 0; i--)
			m_arrayDims[i] = m_arrayDims[i - 1];
		m_arrayLevel++;

		// Add new element count at dimension 0
		m_arrayDims[0] = elements;
	}

	public int ArraySize(int elementSize) {

		// Calculate the array size based on the element size
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

		for (int i = 0; i < m_arrayLevel; i++) {
			result *= m_arrayDims[i];
			result += 2;
		}

		return result;
	}

	public boolean ArraySizeBiggerThan(int size, int elementSize) {

		// Returns true if the array size is bigger than size.
		// This is logically equivalent to "ArraySize (elementSize) > size",
		// except
		// ArraySize can fail if the calculated size doesn't fit into an
		// integer.

		// A major goal of Basic4GL is to provide a safe environment to
		// experiment
		// in without worrying about breaking things.
		// Therefore we want to prevent people from trying to allocate
		// unrealistic
		// amounts of memory. Thus we check array sizes upon
		// allocation/declaration.

		int arraySize = elementSize;
		if (arraySize > size)
			return true;

		for (int i = 0; i < m_arrayLevel; i++) {
			if (size < 2 || (size - 2) / m_arrayDims[i] < arraySize)
				return true;
			arraySize *= m_arrayDims[i];
			arraySize += 2;
		}

		return false;
	}

	public boolean CanStoreInRegister() {
		return m_pointerLevel > 0 || // Pointers fit in a register
				(m_arrayLevel == 0 && m_basicType < 0); // Or
																	// single
																	// basic
																	// types
	}

	public ValType RegisterType() {

		// Return the actual type that will be stored in a register when
		// referring
		// to data of this type.
		// For values that fit into a register, the register type is the
		// same as
		// the original type represented.
		// For large values like structures and arrays, the register will
		// store an
		// implicit reference to the data instead.

		// Copy this value type
		ValType result = this;

		// Check if type is an array or structure
		if (!result.CanStoreInRegister()) {

			// A structure or array cannot fit into a register.
			// What is stored is an implicit by-reference pointer.
			result.m_pointerLevel++;
			result.m_byRef = true;
		}

		return result;
	}

	public int StoredType() {

		// Type of actual data stored inside virtual machine register.
		if (m_pointerLevel == 0 && m_arrayLevel == 0)
			return m_basicType;
		else
			return VTP_INT;
	}

	public boolean IsBasic() {
		return m_pointerLevel == 0 && m_arrayLevel == 0
				&& m_basicType < 0;
	}

	// Streaming
	public void StreamOut(ByteBuffer buffer) {

		// Write VmValType to stream
		try {
			Streaming.WriteLong(buffer, m_basicType);

			Streaming.WriteByte(buffer, m_arrayLevel);
			Streaming.WriteByte(buffer, m_pointerLevel);
			Streaming.WriteByte(buffer, (byte) (m_byRef ? 1 : 0));
			for (int i = 0; i < Constants.VM_MAXDIMENSIONS; i++)
				Streaming.WriteLong(buffer, m_arrayDims[i]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void StreamIn(ByteBuffer buffer) {

		// Read VmValType from stream
		try {
			m_basicType = (int)Streaming.ReadLong(buffer);
			m_arrayLevel = Streaming.ReadByte(buffer);
			m_pointerLevel = Streaming.ReadByte(buffer);
			m_byRef = Streaming.ReadByte(buffer) == 1 ? true : false;
			for (int i = 0; i < Constants.VM_MAXDIMENSIONS; i++)
				m_arrayDims[i] = (int) Streaming.ReadLong(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
