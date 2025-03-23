package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.Mutable;
import java.nio.*;
import java.util.*;

/**
 * VM Data
 *
 * Data layout ({@link #data} array)
 * -------------------------------------------------------------------------
 * 0 Reserved for null pointers
 * -------------------------------------------------------------------------
 * 1 Temporary data (allocated during expression
 * : evaluation. Grows downward)
 * m_tempData - 1
 * -------------------------------------------------------------------------
 * {@link #tempData}
 * : Unused stack/temp space
 * {@link #stackTop} - 1
 * -------------------------------------------------------------------------
 * {@link #stackTop} Local variable/parameter stack space. Used by user
 * : defined functions.
 * {@link #permanent} - 1
 * -------------------------------------------------------------------------
 * {@link #permanent}
 * : Permanent (global) variable storage
 * {@link #size()}-1
 * -------------------------------------------------------------------------
 *
 * Stack overflow occurs when m_tempData > m_stackTop.
 * Out of memory occurs when size() > m_maxDataSize.
 */
public class Data {

private Vector<Value> data;
private int tempData;
private int stackTop;
private int permanent;

/**
* Maximum # of permanent data values that can be stored.
* Note: Caller must be sure to call {@link #hasRoomFor(int)} before calling {@link #allocate(int)} to
* ensure there is room for the data.
*/
private int maxDataSize;

/**
* Temp data below this point will NOT be
* freed when {@link #freeTempData()} is called.
*/
private int tempDataLock;

int internalAllocate(int count) {

	// Allocate "count" elements and return iterator pointing to first one
	int top = size();
	int newSize = size() + count;
	if (count > 0) {
	data.setSize(newSize);
	for (int i = top; i < newSize; i++) {
		data.set(i, new Value());
	}
	}
	return top;
}

public Data(int maxDataSize, int stackSize) {
	assertTrue(stackSize > 1);
	assertTrue(maxDataSize > stackSize);

	// Ensure the maxDataSize is less than the maximum # elements supported
	// by the vector.
	// TODO Test different max sizes; previously mData.max_size()
	if (maxDataSize > Integer.MAX_VALUE) {
	maxDataSize = Integer.MAX_VALUE;
	}
	assertTrue(maxDataSize > stackSize);

	// Initialize data
	this.maxDataSize = maxDataSize;
	permanent = stackSize;
	data = new Vector<>();
	clear();
}

public Vector<Value> data() {
	return data;
}

public int getMaxDataSize() {
	return maxDataSize;
}

public int getPermanent() {
	return permanent;
}

public int getStackTop() {
	return stackTop;
}

public int getTempData() {
	return tempData;
}

public int getTempDataLock() {
	return tempDataLock;
}

public void clear() {
	// Clear existing data
	data.clear();

	// Allocate stack
	int temp = size();
	data.setSize(permanent);
	for (int i = temp; i < permanent; i++) {
	data.set(i, new Value());
	}

	// Clear temp data
	tempData = 1;
	tempDataLock = 1;

	// Clear stack
	stackTop = permanent;
}

public int size() {
	return data.size();
}

public boolean isIndexValid(int i) {
	return i >= 0 && i < size();
}

// Initialise a new block of data
public void initData(int i, ValType type, TypeLibrary typeLib) {
	assertTrue(typeLib.isTypeValid(type));

	// Bail out if doesn't contain array
	if (!typeLib.containsArray(type)) {
	return;
	}

	// Type IS array?
	if (type.arrayLevel > 0) {

	// Find element type
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;
	int elementSize = typeLib.getDataSize(elementType);

	// Set array header
	assertTrue(isIndexValid(i));
	assertTrue(isIndexValid(i + 1));

	// First value = # of elements
	data.get(i).setIntVal(type.arrayDimensions[type.arrayLevel - 1]);
	// Second value = element size
	data.get(i + 1).setIntVal(elementSize);

	// Initialise elements (if necessary)
	if (typeLib.containsArray(elementType)) {
		for (int i2 = 0; i2 < type.arrayDimensions[type.arrayLevel - 1]; i2++) {
		initData(i + 2 + i2 * elementSize, elementType, typeLib);
		}
	}
	}

	// Or type is structure containing array?
	else if (type.basicType >= 0) {

	// Initialise each field
	Structure structure = typeLib.getStructures().get(type.basicType);
	for (int i2 = 0; i2 < structure.fieldCount; i2++) {
		StructureField field = typeLib.getFields().get(structure.firstFieldIndex + i2);
		if (typeLib.containsArray(field.type)) {
		initData(i + field.dataOffset, field.type, typeLib);
		}
	}
	}
}

// Permanent data
public int allocate(int count) {
	assertTrue(count >= 0);
	assertTrue(hasRoomFor(count));

	// Allocate "count" elements and return iterator pointing to first one
	int top = data.size();
	int newSize = data.size() + count;
	if (count > 0) {
	data.setSize(newSize);
	for (int i = top; i < newSize; i++) {
		data.set(i, new Value());
	}
	}
	return top;
}

public boolean hasRoomFor(int count) {
	assertTrue(count >= 0);
	return maxDataSize - data.size() >= count;
}

// Stack data

/**
* Stack data
*/
public int allocateStack(int count) {
	assertTrue(count >= 0);
	assertTrue(hasStackRoomFor(count));

	// Allocate stack data (stack grows downward)
	stackTop -= count;

	// Initialize data
	for (int i = 0; i < count; i++) {
	data.set(stackTop + i, new Value());
	}

	// Return index of start of data
	return stackTop;
}

public boolean hasStackRoomFor(int count) {
	assertTrue(count >= 0);
	return stackTop - tempData >= count;
}

/**
* Temporary data
*/
public int allocateTemp(int count, boolean initData) {
	assertTrue(count >= 0);
	assertTrue(hasStackRoomFor(count));

	// Mark temp data position
	int top = tempData;

	// Allocate data
	tempData += count;

	// Initialize data
	if (initData) {
	for (int i = 0; i < count; i++) {
		data.set(top + i, new Value());
	}
	}

	// Return index of start of data
	return top;
}

/**
* Lock the current temporary data
* so that it will not be freed by {@link #freeTempData()}.
* @return Returns the previous lock point,
* which can be passed to {@link #unlockTempData(int)}.
*/
public int lockTempData() {
	int prev = tempDataLock;
	tempDataLock = tempData;
	return prev;
}

/**
* Unlock temporary data to a previous lock point
* (presumably returned by {@link #lockTempData()})
* @param newLockPosition
*/
public void unlockTempData(int newLockPosition) {
	assertTrue(newLockPosition >= 1);
	assertTrue(newLockPosition <= tempDataLock);
	tempDataLock = newLockPosition;
}

public void freeTempData() {
	tempData = tempDataLock;
}

public void saveState(Mutable<Integer> stackTop, Mutable<Integer> tempDataLock) {
	stackTop.set(this.stackTop);
	tempDataLock.set(lockTempData());
}

public void restoreState(int stackTop, int tempDataLock, boolean freeTempData) {

	// Restore stack
	this.stackTop = stackTop;

	// Free temp data used after state was saved
	if (freeTempData) {
	freeTempData();
	}

	// Unlock temp data from before save
	unlockTempData(tempDataLock);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, byte[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, byte[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).byteValue();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Float.valueOf(data.data().get(index + 2 + i).getRealVal()).byteValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, short[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, short[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] =
			Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).shortValue();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Float.valueOf(data.data().get(index + 2 + i).getRealVal()).shortValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, int[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, int[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = data.data().get(index + 2 + i).getIntVal();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Float.valueOf(data.data().get(index + 2 + i).getRealVal()).intValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, long[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, long[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).longValue();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Float.valueOf(data.data().get(index + 2 + i).getRealVal()).longValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, double[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, double[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] =
			Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).doubleValue();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] =
			Float.valueOf(data.data().get(index + 2 + i).getRealVal()).doubleValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, float[] array, int maxSize) {

	return readArray(data, index, type, array, maxSize, 0);
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @param offset  Starting index offset of destination array
* @return
*/
private static int readArray(
	Data data, int index, ValType type, float[] array, int maxSize, int offset) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			readArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] =
			Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).floatValue();
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		array[withOffset] = Float.valueOf(data.data().get(index + 2 + i).getRealVal()).floatValue();
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, ByteBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).byteValue());
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Float.valueOf(data.data().get(index + 2 + i).getRealVal()).byteValue());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, ShortBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).shortValue());
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Float.valueOf(data.data().get(index + 2 + i).getRealVal()).shortValue());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, IntBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Float.valueOf(data.data().get(index + 2 + i).getRealVal()).intValue());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, LongBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).longValue());
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Float.valueOf(data.data().get(index + 2 + i).getRealVal()).longValue());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(
	Data data, int index, ValType type, DoubleBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).doubleValue());
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Float.valueOf(data.data().get(index + 2 + i).getRealVal()).doubleValue());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int readArray(Data data, int index, ValType type, FloatBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return readArray(data, index, type, buffer.array(), maxSize);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			readArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(Integer.valueOf(data.data().get(index + 2 + i).getIntVal()).floatValue());
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		buffer.put(data.data().get(index + 2 + i).getRealVal());
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, byte[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

private static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	byte[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal((int) array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal((float) array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, short[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

private static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	short[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal((int) array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal((float) array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, int[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

private static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	int[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal(array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal((float) array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, long[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

private static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	long[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal((int) array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal((float) array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, double[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

private static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	double[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal((int) array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal((float) array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param array Destination array
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, float[] array, int maxSize) {

	return writeArray(data, index, type, array, maxSize, 0);
}

public static int writeArray(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	float[] array, // Destination array
	int maxSize, // Maximum # of elements
	int offset) { // starting index offset of destination array

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(array != null);
	assertTrue(maxSize > 0);

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = offset;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		arrayOffset +=
			writeArray(
				data,
				index + 2 + i * elementSize,
				elementType,
				array,
				maxSize - arrayOffset,
				arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setIntVal((int) array[withOffset]);
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		int withOffset = i + offset;
		data.data().get(index + 2 + i).setRealVal(array[withOffset]);
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, ByteBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);
	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal((int) buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal((float) buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(
	Data data, int index, ValType type, ShortBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal((int) buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal((float) buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, IntBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal(buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal((float) buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(Data data, int index, ValType type, LongBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal((int) buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal((float) buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(
	Data data, int index, ValType type, DoubleBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal((int) buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal((float) buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

/**
* Converting arrays to/from C style arrays
* @param data Data
* @param index Index of object in data
* @param type Data type
* @param buffer Destination buffer
* @param maxSize Maximum # of elements
* @return
*/
public static int writeArray(
	Data data, int index, ValType type, FloatBuffer buffer, int maxSize) {

	assertTrue(type.basicType == BasicValType.VTP_INT || type.basicType == BasicValType.VTP_REAL);
	assertTrue(type.getVirtualPointerLevel() == 0);
	assertTrue(type.arrayLevel > 0);
	assertTrue(data.isIndexValid(index));
	assertTrue(buffer != null);
	assertTrue(maxSize > 0);

	if (buffer.hasArray()) {
	return Data.writeArray(data, index, type, buffer.array(), maxSize, 0);
	}

	if (type.isByRef) {
	type.pointerLevel--;
	}
	type.isByRef = false;

	// Convert Basic4GL format array to C format array
	ValType elementType = new ValType(type);
	elementType.arrayLevel--;

	int elementCount = data.data().get(index).getIntVal();
	int elementSize = data.data().get(index + 1).getIntVal();
	if (elementType.arrayLevel > 0) {
	int arrayOffset = 0;
	for (int i = 0; i < elementCount && arrayOffset < maxSize; i++) {
		buffer.position(arrayOffset);

		arrayOffset +=
			writeArray(
				data, index + 2 + i * elementSize, elementType, buffer, maxSize - arrayOffset);
	}
	return arrayOffset;
	} else if (elementType.matchesType(BasicValType.VTP_INT)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setIntVal((int) buffer.get(i));
	}
	return elementCount;
	} else if (elementType.matchesType(BasicValType.VTP_REAL)) {
	if (elementCount > maxSize) {
		elementCount = maxSize;
	}
	for (int i = 0; i < elementCount; i++) {
		data.data().get(index + 2 + i).setRealVal(buffer.get(i));
	}
	return elementCount;
	} else {
	assertTrue(false);
	}
	return 0;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	byte[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = (byte) 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	short[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = (short) 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	int[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	long[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	double[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	float[] array, // Destination array
	int maxSize) { // Maximum # of elements

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, array, maxSize);

	// Zero remaining elements
	for (int i = size; i < maxSize; i++) {
	array[i] = 0;
	}

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	ByteBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put((byte) 0);
	}

	buffer.rewind();

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	ShortBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put((short) 0);
	}

	buffer.rewind();

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	IntBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put(0);
	}

	buffer.rewind();

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	LongBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put(0);
	}

	buffer.rewind();

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	DoubleBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put(0);
	}

	buffer.rewind();

	return size;
}

public static int readAndZero(
	Data data, // Data
	int index, // Index of object in data
	ValType type, // Data type
	FloatBuffer buffer, // Destination buffer
	int maxSize) { // Maximum # of elements

	if (buffer.hasArray()) {
	return Data.readAndZero(data, index, type, buffer.array(), maxSize);
	}

	// Note: This template function only works for numeric types
	// Read array
	int size = readArray(data, index, type, buffer, maxSize);

	// Zero remaining elements
	buffer.position(size);
	for (int i = size; i < maxSize; i++) {
	buffer.put(0);
	}

	buffer.rewind();

	return size;
}

// TODO Check if Java has built in function for this
public static void zeroArray(ByteBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put((byte) 0);
	}
	}
	buffer.rewind();
}

public static void zeroArray(ShortBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put((short) 0);
	}
	}
	buffer.rewind();
}

public static void zeroArray(IntBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put(0);
	}
	}
	buffer.rewind();
}

public static void zeroArray(LongBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put(0L);
	}
	}
	buffer.rewind();
}

public static void zeroArray(DoubleBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put(0d);
	}
	}
	buffer.rewind();
}

public static void zeroArray(FloatBuffer buffer, int size) {
	if (buffer.hasArray()) {
	zeroArray(buffer.array(), size);
	} else {
	for (int i = 0; i < size; i++) {
		buffer.put(0f);
	}
	}
	buffer.rewind();
}

public static void zeroArray(byte[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static void zeroArray(short[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static void zeroArray(int[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static void zeroArray(long[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static void zeroArray(double[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static void zeroArray(float[] array, int size) {
	for (int i = 0; i < size; i++) {
	array[i] = 0;
	}
}

public static int initTempArray(Data data, TypeLibrary typeLib, int elementType, int arraySize) {
	assertTrue(arraySize > 0);

	// Setup a basic 1D array of Integers
	ValType type = new ValType(elementType);
	type.addDimension(arraySize); // Set array size

	// Allocate temporary array
	int dataIndex = data.allocateTemp(typeLib.getDataSize(type), true);
	data.initData(dataIndex, type, typeLib);
	return dataIndex;
}

public static <T extends Number> int fillTempIntArray(
	Data data, TypeLibrary typeLib, int arraySize, List<T> array) {

	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = initTempArray(data, typeLib, BasicValType.VTP_INT, arraySize);

	// Translate C array into data
	for (int i = 0; i < arraySize; i++) {
	data.data().get(dataIndex + 2 + i).setIntVal(array.get(i).intValue());
	}

	// Return temporary index
	return dataIndex;
}

public static int fillTempIntArray(Data data, TypeLibrary typeLib, int arraySize, int[] array) {

	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = initTempArray(data, typeLib, BasicValType.VTP_INT, arraySize);

	// Translate C array into data
	for (int i = 0; i < arraySize; i++) {
	data.data().get(dataIndex + 2 + i).setIntVal(array[i]);
	}

	// Return temporary index
	return dataIndex;
}

public static <T extends Number> int fillTempRealArray(
	Data data, TypeLibrary typeLib, int arraySize, List<T> array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = initTempArray(data, typeLib, BasicValType.VTP_REAL, arraySize);

	// Translate C array into data
	for (int i = 0; i < arraySize; i++) {
	data.data().get(dataIndex + 2 + i).setRealVal(array.get(i).floatValue());
	}

	// Return temporary index
	return dataIndex;
}

public static int fillTempRealArray(Data data, TypeLibrary typeLib, int arraySize, int[] array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = initTempArray(data, typeLib, BasicValType.VTP_REAL, arraySize);

	// Translate C array into data
	for (int i = 0; i < arraySize; i++) {
	data.data().get(dataIndex + 2 + i).setRealVal((float) array[i]);
	}

	// Return temporary index
	return dataIndex;
}

public static int fillTempRealArray(
	Data data, TypeLibrary typeLib, int arraySize, float[] array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = initTempArray(data, typeLib, BasicValType.VTP_REAL, arraySize);

	// Translate C array into data
	for (int i = 0; i < arraySize; i++) {
	data.data().get(dataIndex + 2 + i).setRealVal(array[i]);
	}

	// Return temporary index
	return dataIndex;
}

public static int tempArray2D(
	Data data, TypeLibrary typeLib, int elementType, int arraySize1, int arraySize2) {
	assertTrue(arraySize1 > 0);
	assertTrue(arraySize2 > 0);

	// Setup a basic 1D array of Integers
	ValType type = new ValType(elementType);
	type.addDimension(arraySize1); // Set array size
	type.addDimension(arraySize2);

	// Allocate temporary array
	int dataIndex = data.allocateTemp(typeLib.getDataSize(type), true);
	data.initData(dataIndex, type, typeLib);
	return dataIndex;
}

public static <T> int fillTempIntArray2D(
	Data data, TypeLibrary typeLib, int arraySize1, int arraySize2, List<T> array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = tempArray2D(data, typeLib, BasicValType.VTP_INT, arraySize1, arraySize2);

	// Translate C array into data
	int i = 0;
	for (int x = 0; x < arraySize1; x++) {
	int offset = dataIndex + x * data.data().get(dataIndex + 1).getIntVal() + 2;
	for (int y = 0; y < arraySize2; y++) {
		data.data().get(offset + y + 2).setIntVal((Integer) array.get(i));
	}
	}

	// Return temporary index
	return dataIndex;
}

public static <T> int fillTempRealArray2D(
	Data data, TypeLibrary typeLib, int arraySize1, int arraySize2, List<T> array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = tempArray2D(data, typeLib, BasicValType.VTP_REAL, arraySize1, arraySize2);

	// Translate C array into data
	int i = 0;
	for (int x = 0; x < arraySize1; x++) {
	int offset = dataIndex + x * data.data().get(dataIndex + 1).getIntVal() + 2;
	for (int y = 0; y < arraySize2; y++) {
		data.data().get(offset + y + 2).setRealVal((Float) array.get(i++));
	}
	}

	// Return temporary index
	return dataIndex;
}

public static int fillTempRealArray2D(
	Data data, TypeLibrary typeLib, int arraySize1, int arraySize2, float[] array) {
	assertTrue(array != null);

	// Allocate temporary array
	int dataIndex = tempArray2D(data, typeLib, BasicValType.VTP_REAL, arraySize1, arraySize2);

	// Translate C array into data
	int i = 0;
	for (int x = 0; x < arraySize1; x++) {
	int offset = dataIndex + x * data.data().get(dataIndex + 1).getIntVal() + 2;
	for (int y = 0; y < arraySize2; y++) {
		data.data().get(offset + y + 2).setRealVal(array[i++]);
	}
	}

	// Return temporary index
	return dataIndex;
}

public static int getArrayDimensionSize(Data data, int arrayOffset, int dimension) {

	assertTrue(data.isIndexValid(arrayOffset));
	int index = arrayOffset + dimension * 2;
	assertTrue(data.isIndexValid(index));
	return data.data().get(index).getIntVal();
}
}
