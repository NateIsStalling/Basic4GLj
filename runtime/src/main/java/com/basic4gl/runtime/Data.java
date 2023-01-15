package com.basic4gl.runtime;

import java.nio.*;
import java.util.*;

import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.types.Structure;
import com.basic4gl.runtime.types.StructureField;
import com.basic4gl.runtime.types.TypeLibrary;
import com.basic4gl.runtime.types.ValType;

import static com.basic4gl.runtime.util.Assert.assertTrue;

public class Data {

	// //////////////////////////////////////////////////////////////////////////////
	// vmData
	//

	// Data layout (mData array)
	// -------------------------------------------------------------------------
	// 0 Reserved for null pointers
	// -------------------------------------------------------------------------
	// 1 Temporary data (allocated during expression
	// : evaluation. Grows downward)
	// m_tempData - 1
	// -------------------------------------------------------------------------
	// m_tempData
	// : Unused stack/temp space
	// m_stackTop - 1
	// -------------------------------------------------------------------------
	// m_stackTop Local variable/parameter stack space. Used by user
	// : defined functions.
	// m_permanent - 1
	// -------------------------------------------------------------------------
	// m_permanent
	// : Permanent (global) variable storage
	// size()-1
	// -------------------------------------------------------------------------
	//
	// Stack overflow occurs when m_tempData > m_stackTop.
	// Out of memory occurs when size() > m_maxDataSize.
	private Vector<Value> m_data;
	private int m_tempData;
	private int m_stackTop;
	private int m_permanent;
	private int m_maxDataSize;
	// Maximum # of permanent data values that can be stored.
	// Note: Caller must be sure to call RoomFor before calling Allocate to
	// ensure there is room for the data.

	int m_tempDataLock; // Temp data below this point will NOT be

	// freed when FreeTempData is called.

	int InternalAllocate(int count) {

		// Allocate "count" elements and return iterator pointing to first one
		int top = Size();
		int newSize = Size() + count;
		if (count > 0) {
			m_data.setSize(newSize);
			for (int i = top; i < newSize; i++)
				m_data.set(i, new Value());
		}
		return top;
	}

	public Data(int maxDataSize, int stackSize) {
		assertTrue(stackSize > 1);
		assertTrue(maxDataSize > stackSize);

		// Ensure the maxDataSize is less than the maximum # elements supported
		// by the vector.
		// TODO Test different max sizes; previously mData.max_size()
		if (maxDataSize > Integer.MAX_VALUE)
			maxDataSize = Integer.MAX_VALUE;
		assertTrue(maxDataSize > stackSize);

		// Initialize data
		m_maxDataSize = maxDataSize;
		m_permanent = stackSize;
		m_data = new Vector<Value>();
		Clear();
	}

	public Vector<Value> Data() {
		return m_data;
	}

	public int MaxDataSize() {
		return m_maxDataSize;
	}

	public int Permanent() {
		return m_permanent;
	}

	public int StackTop() {
		return m_stackTop;
	}

	public int TempData() {
		return m_tempData;
	}

	public int TempDataLock() {
		return m_tempDataLock;
	}

	public void Clear() {
		// Clear existing data
		m_data.clear();

		// Allocate stack
		int temp = Size();
		m_data.setSize(m_permanent);
		for (int i = temp; i < m_permanent; i++)
			m_data.set(i, new Value());

		// Clear temp data
		m_tempData = 1;
		m_tempDataLock = 1;

		// Clear stack
		m_stackTop = m_permanent;
	}

	public int Size() {
		return m_data.size();
	}

	public boolean IndexValid(int i) {
		return i >= 0 && i < Size();
	}
	
	// Initialise a new block of data
	public void InitData(int i, ValType type, TypeLibrary typeLib) 
	{
		assertTrue(typeLib.TypeValid(type));

		// Bail out if doesn't contain array
		if (!typeLib.ContainsArray(type))
			return;

		// Type IS array?
		if (type.m_arrayLevel > 0) {

			// Find element type
			ValType elementType = new ValType(type);
			elementType.m_arrayLevel--;
			int elementSize = typeLib.DataSize(elementType);

			// Set array header
			assertTrue(IndexValid(i));
			assertTrue(IndexValid(i + 1));
			
			// First value = # of elements
			m_data.get(i).setIntVal(type.m_arrayDims[type.m_arrayLevel - 1]);
			// Second value = element size
			m_data.get(i + 1).setIntVal(elementSize);

			// Initialise elements (if necessary)
			if (typeLib.ContainsArray(elementType)) {
				for (int i2 = 0; i2 < type.m_arrayDims[type.m_arrayLevel - 1]; i2++)
					InitData(i + 2 + i2 * elementSize, elementType, typeLib);
			}
		}

		// Or type is structure containing array?
		else if (type.m_basicType >= 0) {

			// Initialise each field
			Structure structure = typeLib.Structures().get(
					type.m_basicType);
			for (int i2 = 0; i2 < structure.m_fieldCount; i2++) {
				StructureField field = typeLib.Fields().get(
						structure.m_firstField + i2);
				if (typeLib.ContainsArray(field.m_type))
					InitData(i + field.m_dataOffset, field.m_type, typeLib);
			}
		}
	}

	// Permanent data
	public int Allocate(int count) {
		assertTrue(count >= 0);
		assertTrue(RoomFor(count));

		// Allocate "count" elements and return iterator pointing to first one
		int top = m_data.size();
		int newSize = m_data.size() + count;
		if (count > 0) {
			m_data.setSize(newSize);
			for (int i = top; i < newSize; i++)
				m_data.set(i, new Value());
		}
		return top;
	}

	public boolean RoomFor(int count) {
		assertTrue(count >= 0);
		return m_maxDataSize - m_data.size() >= count;
	}

	// Stack data
	public int AllocateStack(int count) {
		assertTrue(count >= 0);
		assertTrue(StackRoomFor(count));

		// Allocate stack data (stack grows downward)
		m_stackTop -= count;

		// Initialize data
		for (int i = 0; i < count; i++)
			m_data.set(m_stackTop + i, new Value());

		// Return index of start of data
		return m_stackTop;
	}

	public boolean StackRoomFor(int count) {
		assertTrue(count >= 0);
		return m_stackTop - m_tempData >= count;
	}

	// Temporary data
	public int AllocateTemp(int count, boolean initData) {
		assertTrue(count >= 0);
		assertTrue(StackRoomFor(count));

		// Mark temp data position
		int top = m_tempData;

		// Allocate data
		m_tempData += count;

		// Initialize data
		if (initData)
			for (int i = 0; i < count; i++)
				m_data.set(top + i, new Value());

		// Return index of start of data
		return top;
	}

	// Lock the current temporary data so that it will not be freed by
	// FreeTempData(). Returns the previous lock point, which can be passed to
	// UnlockTempData().
	public int LockTempData() {
		int prev = m_tempDataLock;
		m_tempDataLock = m_tempData;
		return prev;
	}

	// Unlock temporary data to a previous lock point (presumably returned by
	// LockTempData()
	public void UnlockTempData(int newLockPosition) {
		assertTrue(newLockPosition >= 1);
		assertTrue(newLockPosition <= m_tempDataLock);
		m_tempDataLock = newLockPosition;
	}

	public void FreeTemp() {
		m_tempData = m_tempDataLock;
	}

	public void SaveState(Mutable<Integer> stackTop, Mutable<Integer> tempDataLock) {
		stackTop.set(m_stackTop);
		tempDataLock.set(LockTempData());
	}

	public void RestoreState(int stackTop, int tempDataLock,
			boolean freeTempData) {

		// Restore stack
		m_stackTop = stackTop;

		// Free temp data used after state was saved
		if (freeTempData)
			FreeTemp();

		// Unlock temp data from before save
		UnlockTempData(tempDataLock);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Misc functions

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
									int index, // Index of object in data
									ValType type, // Data type
									byte[] array, // Destination array
									int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).byteValue();
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).byteValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								short[] array, // Destination array
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).shortValue();
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).shortValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								int[] array, // Destination array
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).intValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								long[] array, // Destination array
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).longValue();
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).longValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								double[] array, // Destination array
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).doubleValue();
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).doubleValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								float[] array, // Destination array
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).floatValue();
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				array[i] = Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).floatValue();
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								ByteBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).byteValue());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).byteValue());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								ShortBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).shortValue());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).shortValue());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								IntBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).intValue());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								LongBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).longValue());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).longValue());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								DoubleBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).doubleValue());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Float.valueOf(data.Data().get(index + 2 + i).getRealVal()).doubleValue());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	// Converting arrays to/from C style arrays
	public static int ReadArray(Data data, // Data
								int index, // Index of object in data
								ValType type, // Data type
								FloatBuffer buffer, // Destination buffer
								int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return ReadArray(
					data,
					index,
					type,
					buffer.array(),
					maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += ReadArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(Integer.valueOf(data.Data().get(index + 2 + i).getIntVal()).floatValue());
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				buffer.put(data.Data().get(index + 2 + i).getRealVal());
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	public static int WriteArray(Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			byte[] array, // Destination array
			int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int)array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	public static int WriteArray(Data data, // Data
									 int index, // Index of object in data
									 ValType type, // Data type
									 short[] array, // Destination array
									 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}



	public static int WriteArray(Data data, // Data
									 int index, // Index of object in data
									 ValType type, // Data type
									 int[] array, // Destination array
									 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal(array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
									 int index, // Index of object in data
									 ValType type, // Data type
									 long[] array, // Destination array
									 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 double[] array, // Destination array
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 float[] array, // Destination array
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(array != null);
		assertTrue(maxSize > 0);

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++)
				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, Arrays.copyOfRange(array, arrayOffset, array.length), maxSize
								- arrayOffset);
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) array[i]);
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal(array[i]);
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 ByteBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int)buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 ShortBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}



	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 IntBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal(buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 LongBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 DoubleBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal((float) buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}
	public static int WriteArray(Data data, // Data
								 int index, // Index of object in data
								 ValType type, // Data type
								 FloatBuffer buffer, // Destination buffer
								 int maxSize) { // Maximum # of elements
		assertTrue(type.m_basicType == ValType.VTP_INT || type.m_basicType == ValType.VTP_REAL);
		assertTrue(type.VirtualPointerLevel() == 0);
		assertTrue(type.m_arrayLevel > 0);
		assertTrue(data.IndexValid(index));
		assertTrue(buffer != null);
		assertTrue(maxSize > 0);

		if (buffer.hasArray()) {
			return Data.WriteArray(data, index, type, buffer.array(), maxSize);
		}

		if (type.m_byRef)
			type.m_pointerLevel--;
		type.m_byRef = false;

		// Convert Basic4GL format array to C format array
		ValType elementType = new ValType(type);
		elementType.m_arrayLevel--;

		int elementCount = data.Data().get(index).getIntVal();
		int elementSize = data.Data().get(index + 1).getIntVal();
		if (elementType.m_arrayLevel > 0) {
			int arrayOffset = 0;
			for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
				buffer.position(arrayOffset);

				arrayOffset += WriteArray(data, index + 2 + i * elementSize,
						elementType, buffer, maxSize
								- arrayOffset);
			}
			return arrayOffset;
		} else if (elementType.Equals(ValType.VTP_INT)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i)
						.setIntVal((int) buffer.get(i));
			return elementCount;
		} else if (elementType.Equals(ValType.VTP_REAL)) {
			if (elementCount > maxSize)
				elementCount = maxSize;
			for (int i = 0; i < elementCount; i++)
				data.Data().get(index + 2 + i).setRealVal(buffer.get(i));
			return elementCount;
		} else
			assertTrue(false);
		return 0;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		byte[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = (byte) 0;

		return size;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		short[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = (short) 0;

		return size;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		int[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = 0;

		return size;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		long[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = 0;

		return size;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		double[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = 0;

		return size;
	}

	public static int ReadAndZero(
		Data data, // Data
		int index, // Index of object in data
		ValType type, // Data type
		float[] array, // Destination array
		int maxSize) { // Maximum # of elements

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, array, maxSize);

		// Zero remaining elements
		for (int i = size; i < maxSize; i++)
			array[i] = 0;

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			ByteBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put((byte) 0);

		buffer.rewind();

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			ShortBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put((short) 0);

		buffer.rewind();

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			IntBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put(0);

		buffer.rewind();

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			LongBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put(0);

		buffer.rewind();

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			DoubleBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put(0);

		buffer.rewind();

		return size;
	}

	public static int ReadAndZero(
			Data data, // Data
			int index, // Index of object in data
			ValType type, // Data type
			FloatBuffer buffer, // Destination buffer
			int maxSize) { // Maximum # of elements

		if (buffer.hasArray()) {
			return Data.ReadAndZero(data, index, type, buffer.array(), maxSize);
		}

		// Note: This template function only works for numeric types
		// Read array
		int size = ReadArray(data, index, type, buffer, maxSize);

		// Zero remaining elements
		buffer.position(size);
		for (int i = size; i < maxSize; i++)
			buffer.put(0);

		buffer.rewind();

		return size;
	}

	//TODO Check if Java has built in function for this
	public static void ZeroArray(ByteBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put((byte) 0);
		}
		buffer.rewind();
	}
	public static void ZeroArray(ShortBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put((short) 0);
		}
		buffer.rewind();
	}
	public static void ZeroArray(IntBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put(0);
		}
		buffer.rewind();
	}
	public static void ZeroArray(LongBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put(0L);
		}
		buffer.rewind();
	}
	public static void ZeroArray(DoubleBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put(0d);
		}
		buffer.rewind();
	}
	public static void ZeroArray(FloatBuffer buffer, int size) {
		if (buffer.hasArray()) {
			ZeroArray(buffer.array(), size);
		} else {
			for (int i = 0; i < size; i++)
				buffer.put(0f);
		}
		buffer.rewind();
	}

	public static void ZeroArray(byte[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}
	public static void ZeroArray(short[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}
	public static void ZeroArray(int[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}
	public static void ZeroArray(long[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}
	public static void ZeroArray(double[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}
	public static void ZeroArray(float[] array, int size) {
		for (int i = 0; i < size; i++)
			array[i] = 0;
	}

	public static int TempArray(Data data, TypeLibrary typeLib,
			int elementType, int arraySize) {
		assertTrue(arraySize > 0);

		// Setup a basic 1D array of Integers
		ValType type = new ValType(elementType);
		type.AddDimension(arraySize); // Set array size

		// Allocate temporary array
		int dataIndex = data.AllocateTemp(typeLib.DataSize(type), true);
		data.InitData(dataIndex, type, typeLib);
		return dataIndex;
	}

	public static <T extends  Number> int FillTempIntArray(Data data, TypeLibrary typeLib,
			int arraySize, List<T> array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray(data, typeLib, ValType.VTP_INT,
				arraySize);

		// Translate C array into data
		for (int i = 0; i < arraySize; i++)
			data.Data().get(dataIndex + 2 + i)
					.setIntVal(array.get(i).intValue());

		// Return temporary index
		return dataIndex;
	}
	public static int FillTempIntArray(Data data, TypeLibrary typeLib,
														   int arraySize, int[] array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray(data, typeLib, ValType.VTP_INT,
				arraySize);

		// Translate C array into data
		for (int i = 0; i < arraySize; i++)
			data.Data().get(dataIndex + 2 + i)
					.setIntVal(array[i]);

		// Return temporary index
		return dataIndex;
	}

	public static <T extends  Number> int FillTempRealArray(Data data, TypeLibrary typeLib,
			int arraySize, List<T>  array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray(data, typeLib, ValType.VTP_REAL,
				arraySize);

		// Translate C array into data
		for (int i = 0; i < arraySize; i++)
			data.Data().get(dataIndex + 2 + i).setRealVal(array.get(i).floatValue());

		// Return temporary index
		return dataIndex;
	}

	public static int FillTempRealArray(Data data, TypeLibrary typeLib,
															int arraySize, int[] array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray(data, typeLib, ValType.VTP_REAL,
				arraySize);

		// Translate C array into data
		for (int i = 0; i < arraySize; i++)
			data.Data().get(dataIndex + 2 + i).setRealVal((float) array[i]);

		// Return temporary index
		return dataIndex;
	}
	public static int FillTempRealArray(Data data, TypeLibrary typeLib,
										int arraySize, float[] array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray(data, typeLib, ValType.VTP_REAL,
				arraySize);

		// Translate C array into data
		for (int i = 0; i < arraySize; i++)
			data.Data().get(dataIndex + 2 + i).setRealVal(array[i]);

		// Return temporary index
		return dataIndex;
	}
	public static int TempArray2D(Data data, TypeLibrary typeLib,
			int elementType, int arraySize1, int arraySize2) {
		assertTrue(arraySize1 > 0);
		assertTrue(arraySize2 > 0);

		// Setup a basic 1D array of Integers
		ValType type = new ValType(elementType);
		type.AddDimension(arraySize1); // Set array size
		type.AddDimension(arraySize2);

		// Allocate temporary array
		int dataIndex = data.AllocateTemp(typeLib.DataSize(type), true);
		data.InitData(dataIndex, type, typeLib);
		return dataIndex;
	}

	public static <T> int FillTempIntArray2D(Data data, TypeLibrary typeLib,
			int arraySize1, int arraySize2, List<T> array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray2D(data, typeLib, ValType.VTP_INT,
				arraySize1, arraySize2);

		// Translate C array into data
		int i = 0;
		for (int x = 0; x < arraySize1; x++) {
			int offset = dataIndex + x
					* data.Data().get(dataIndex + 1).getIntVal() + 2;
			for (int y = 0; y < arraySize2; y++)
				data.Data().get(offset + y + 2)
						.setIntVal((Integer) array.get(i));
		}

		// Return temporary index
		return dataIndex;
	}

	public static <T> int FillTempRealArray2D(Data data, TypeLibrary typeLib,
			int arraySize1, int arraySize2, List<T> array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray2D(data, typeLib, ValType.VTP_REAL,
				arraySize1, arraySize2);

		// Translate C array into data
		int i = 0;
		for (int x = 0; x < arraySize1; x++) {
			int offset = dataIndex + x
					* data.Data().get(dataIndex + 1).getIntVal() + 2;
			for (int y = 0; y < arraySize2; y++)
				data.Data().get(offset + y + 2)
						.setRealVal((Float) array.get(i++));
		}

		// Return temporary index
		return dataIndex;
	}
	public static int FillTempRealArray2D(Data data, TypeLibrary typeLib,
											  int arraySize1, int arraySize2, float[] array) {
		assertTrue(array != null);

		// Allocate temporary array
		int dataIndex = TempArray2D(data, typeLib, ValType.VTP_REAL,
				arraySize1, arraySize2);

		// Translate C array into data
		int i = 0;
		for (int x = 0; x < arraySize1; x++) {
			int offset = dataIndex + x
					* data.Data().get(dataIndex + 1).getIntVal() + 2;
			for (int y = 0; y < arraySize2; y++)
				data.Data().get(offset + y + 2)
						.setRealVal( array[i++]);
		}

		// Return temporary index
		return dataIndex;
	}

	public static int ArrayDimensionSize(Data data, int arrayOffset,
			int dimension) {
		assertTrue(data.IndexValid(arrayOffset));
		int index = arrayOffset + dimension * 2;
		assertTrue(data.IndexValid(index));
		return data.Data().get(index).getIntVal();

	}
}
