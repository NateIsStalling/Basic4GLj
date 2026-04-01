package com.basic4gl.compiler.plugin;

import com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLLongRunningFunction;
import com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLRuntime;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.Value;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Mutable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLExtendedTypeCode.*;
import static com.basic4gl.runtime.TomVM.ARRAY_MAX_DIMENSIONS;
import static com.basic4gl.runtime.types.BasicValType.VTP_INT;
import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Adapts the Basic4GLRuntime interface to a TomVM virtual machine.
 */
public class TomVMJARAdapter implements Basic4GLRuntime {
    private TomVM  vm;
    private PluginStructureManager structureManager;

    // Working
    private PluginDataType currentType;

    private void ReadIntArray(
            int dataIndex,
            int[] dest,
            int dimensionCount,
            int[] dimensions) {

        // First two entries are the element count and size
        int elementCount = vm.getData().data().get(dataIndex).getIntVal();
        int elementSize = vm.getData().data().get(dataIndex + 1).getIntVal();

        // Move to start of array data
        dataIndex += 2;

        // Recursively extract array data
        if (dimensionCount > 1) {

            // Calculate # of elements in one entry in this dimension
            int destElements = 1;
            for (int d = 1; d < dimensionCount; d++)
                destElements *= dimensions[d];

            // Recursively copy sub dimensions
            for (int i = 0; i < dimensions[0]; i++) {
                if (i < elementCount) {
                    ReadIntArray(
                            dataIndex + i * elementSize,
                            &dest[i * destElements],
                            dimensionCount - 1,
                    &dimensions[1]);
                } else {
                    memset( & dest[i * destElements], 0, destElements * sizeof( int));
                }
            }
        }
        else {
            // Down to lowest dimension.
            // Copy elements
            for (int i = 0; i < dimensions[0]; i++)
                if (i < elementCount) {
                    dest[i] = vm.getData().data().get(dataIndex + i).getIntVal();
                } else {
                    dest[i] = 0;
                    }
        }
    }

    private void ReadFloatArray(
            int dataIndex,
            float[] dest,
            int dimensionCount,
            int [] dimensions) {

            // First two entries are the element count and size
            int elementCount = vm.getData().data().get(dataIndex).getIntVal();
            int elementSize = vm.getData().data().get(dataIndex + 1).getIntVal();

            // Move to start of array data
            dataIndex += 2;

            // Recursively extract array data
            if (dimensionCount > 1) {

                // Calculate # of elements in one entry in this dimension
                int destElements = 1;
                for (int d = 1; d < dimensionCount; d++)
                    destElements *= dimensions[d];

                // Recursively copy sub dimensions
                for (int i = 0; i < dimensions[0]; i++) {
                    if (i < elementCount) {
                        ReadFloatArray(
                                dataIndex + i * elementSize,
                                & dest[i * destElements],
                                dimensionCount - 1,
				&dimensions[1]);
                    }
			else {
                        memset( & dest[i * destElements], 0, destElements * sizeof( int));
                    }
                }
            }
            else {
                // Down to lowest dimension.
                // Copy elements
                for (int i = 0; i < dimensions[0]; i++)
                    if (i < elementCount)
                        dest[i] = vm.getData().data().get(dataIndex + i).getRealVal();
                    else
                        dest[i] = 0;
            }
    }

    private void WriteIntArray(
            int dataIndex,
            IntBuffer src,
            int dimensionCount,
            int[] dimensions) {

            // First two entries are the element count and size
            int elementCount = vm.getData().data().get(dataIndex).getIntVal();
            int elementSize = vm.getData().data().get(dataIndex + 1).getIntVal();
            int min = elementCount < dimensions[0] ? elementCount : dimensions[0];

            // Move to start of array data
            dataIndex += 2;

            // Recursively copy data to array
            if (dimensionCount > 1) {

                // Calculate # of elements in one entry in this dimension
                int destElements = 1;
                for (int d = 1; d < dimensionCount; d++)
                    destElements *= dimensions[d];

                for (int i = 0; i < min; i++) {
                    src.position(i * destElements);
                    WriteIntArray(
                            dataIndex + i * elementSize,
                            src,
                            dimensionCount - 1,
                &dimensions[1]);
                }
            }
            else {
                // Down to lowest dimension.
                // Can copy elements
                for (int i = 0; i < min; i++) {
                    vm.getData().data().get(dataIndex + i).setIntVal(src[i]);
                }
            }
        }

    private void WriteFloatArray(
            int dataIndex,
            float[] src,
            int dimensionCount,
            int[] dimensions) {


            // First two entries are the element count and size
            int elementCount = vm.getData().data().get(dataIndex).getIntVal();
            int elementSize = vm.getData().data().get(dataIndex + 1).getIntVal();
            int min = elementCount < dimensions[0] ? elementCount : dimensions[0];

            // Move to start of array data
            dataIndex += 2;

            // Recursively copy data to array
            if (dimensionCount > 1) {

                // Calculate # of elements in one entry in this dimension
                int destElements = 1;
                for (int d = 1; d < dimensionCount; d++)
                    destElements *= dimensions[d];

                for (int i = 0; i < min; i++) {
                    WriteFloatArray(
                            dataIndex + i * elementSize,
                            & src[i * destElements],
                            dimensionCount - 1,
			&dimensions[1]);
                }
            }
            else {
                // Down to lowest dimension.
                // Can copy elements
                for (int i = 0; i < min; i++)
                    vm.getData().data().get(dataIndex + i).setRealVal(src[i]);
            }
        }

    private int CreateTempArray(
            ValType type,
            int dimensionCount,
            int[] dimensions) {


            // Create Basic4GL type to define array
            for (int d = 0; d < dimensionCount; d++)
                type.addDimension(dimensions[d]);

            // Allocate temporary array, and initialise it
            return CreateTempData(type);
        }

    private int CreateTempData(ValType type) {

            // Allocate temporary data
            int dataIndex = vm.getData().allocateTemp(vm.getDataTypes().getDataSize(type), true);

            // Initialize it
            vm.getData().initData(dataIndex, type, vm.getDataTypes());

            // Return data index
            return dataIndex;
        }
    private void FixCurrentType() {

            // Convert structures and arrays to by-reference
            if (currentType.getPointerLevel() == 0 &&
                    (currentType.getBaseType() > 0 || currentType.getArrayLevel() > 0)) {
                currentType.setPointerLevel((byte)1);
                currentType.setByReference(true);
            }
        }

    // Convert Basic data to C data
    private void CArrayFromBasicArray(PluginDataType type, ByteBuffer cData, Mutable<Integer> basicDataIndex) {


            assertTrue(type.getPointerLevel() == 0);
            assertTrue(type.getArrayLevel() > 0);

            // Extract element info
            int basicCount = vm.getData().data().get(basicDataIndex.get()).getIntVal();
            int size = vm.getData().data().get(basicDataIndex.get() + 1).getIntVal();
            int arrayStart = basicDataIndex.get() + 2;

            // If greater than # requested, use the lesser (so as not to overflow
            // target C array).
            int count = basicCount;
            if (count > type.getArrayDims()[type.getArrayLevel() - 1])
                count = type.getArrayDims()[type.getArrayLevel() - 1];

            // Get element type
            PluginDataType elementType = new PluginDataType(type);
            elementType.makeIntoElementType();

            // Convert each element
            for (int i = 0; i < count; i++) {
                int elementDataIndex = arrayStart + i * size;
                CDataFromBasicData(elementType, cData, new Mutable<Integer>(elementDataIndex));
            }

            // Advance data index
            basicDataIndex.set(arrayStart + basicCount * size);
        }
    private void CStructureFromBasicStructure(PluginDataType type, ByteBuffer cData, Mutable<Integer> basicDataIndex) {

            // Find plugin structure
            PluginStructure structure = structureManager.getStructure(type.getBaseType());

            // Convert each field
            for (int i = 0; i < structure.getFieldCount(); i++)
                CDataFromBasicData(structure.getField(i).getDataType(), cData, basicDataIndex);
        }
    private void CValueFromBasicValue(PluginDataType type, ByteBuffer cData, Value value) {

        assertTrue(type.getPointerLevel() == 0);
        assertTrue(type.getArrayLevel() == 0);
        assertTrue(type.getBaseType() < 0);

            int strIndex, len;
            String str;
            switch (type.getBaseType()) {
                case PLUGIN_BASIC4GL_EXT_BYTE:
                    // Copy Basic4GL int to C byte
                    cData.put((byte)value.getIntVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_WORD:
                    // Copy Basic4GL int to C word (int16_t)
                    cData.putChar((char)value.getIntVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_INT:
                    // Copy Basic4GL int to C int
                    cData.putInt(value.getIntVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_INT64:
                    // Copy Basic4GL int to C 64bit integer (int64_t)
                    cData.putLong(value.getIntVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_FLOAT:
                    // Copy Basic4GL real to C float
                    cData.putFloat(value.getRealVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_DOUBLE:
                    // Copy Basic4GL real to C double
                    cData.putDouble(value.getRealVal());
                    break;

                case PLUGIN_BASIC4GL_EXT_STRING:

                    // Fetch string
                    strIndex = value.getIntVal();
                    str = vm.getString(strIndex);

                    // Copy characters
                    len = str.length();
                    if (len > type.getStringSize() - 1)
                        len = type.getStringSize() - 1;
                    memcpy(cData, str.c_str(), len);
                    cData[len] = 0;                 // (Terminate string)
                    cData += type.getStringSize();
                    break;

                default:
                    assertTrue(false);
            }
        }
    private void CValueFromBasicValue(PluginDataType type, ByteBuffer cData, Mutable<Integer> basicDataIndex) {


            assertTrue(type.getPointerLevel() == 0);
            assertTrue(type.getArrayLevel() == 0);
            assertTrue(type.getBaseType() < 0);

            if (type.getBaseType() == PLUGIN_BASIC4GL_EXT_PADDING)
                cData += type.getStringSize();
            else {
                CValueFromBasicValue(type, cData, vm.getData().data().get(basicDataIndex.get()));
                basicDataIndex.set(basicDataIndex.get() + 1);
            }
        }
    private void CDataFromBasicData(PluginDataType type, ByteBuffer cData, Mutable<Integer> basicDataIndex){

            if (type.getPointerLevel() > 0) {

                // Note: We cannot convert pointers, so we just store NULL.
        *((void**)cData) = NULL;
                cData += sizeof(void*);
                basicDataIndex.set(basicDataIndex.get() + 1);
            }
            else if (type.getArrayLevel() > 0) {

                // Convert array
                CArrayFromBasicArray(type, cData, basicDataIndex);

            }
            else if (type.getBaseType() > 0) {

                // Convert structure
                CStructureFromBasicStructure(type, cData, basicDataIndex);

            }
            else {

                // Convert value
                CValueFromBasicValue(type, cData, basicDataIndex);
            }
        }

    // Convert C data to basic data
    private void BasicArrayFromCArray(PluginDataType type, Mutable<Integer> basicDataIndex, ByteBuffer cData) {

            assertTrue(type.getPointerLevel() == 0);
            assertTrue(type.getArrayLevel() > 0);

            // Get array size
            int basicCount = vm.getData().data().get(basicDataIndex.get()).getIntVal();
            int size = vm.getData().data().get(basicDataIndex.get() + 1).getIntVal();
            int arrayStart = basicDataIndex.get() + 2;

            // If greater than # requested, use the lesser (so as not to overrun
            // source C array).
            int count = basicCount;
            if (count > type.getArrayDims()[type.getArrayLevel() - 1])
                count = type.getArrayDims()[type.getArrayLevel() - 1];

            // Get element type
            PluginDataType elementType = new PluginDataType(type);
            elementType.makeIntoElementType();

            // Convert each element
            for (int i = 0; i < count; i++) {
                int elementDataIndex = arrayStart + i * size;
                BasicDataFromCData(elementType, new Mutable<Integer>(elementDataIndex), cData);
            }

            // Advance data index
            basicDataIndex.set(arrayStart + basicCount * size);
        }
    private void BasicStructureFromCStructure(PluginDataType type, Mutable<Integer> basicDataIndex, ByteBuffer cData) {

            // Find plugin structure
            PluginStructure structure = structureManager.getStructure(type.getBaseType());

            // Convert each field
            for (int i = 0; i < structure.getFieldCount(); i++)
                BasicDataFromCData(structure.getField(i).getDataType(), basicDataIndex, cData);
        }
    private void BasicValueFromCValue(PluginDataType type, Value value, ByteBuffer cData) {

            assertTrue(type.getPointerLevel() == 0);
            assertTrue(type.getArrayLevel() == 0);
            assertTrue(type.getBaseType() < 0);

            int strIndex;
            switch (type.getBaseType()) {

                case PLUGIN_BASIC4GL_EXT_BYTE:
                    // Copy C byte to Basic4GL int
                    value.setIntVal((int) cData.get());
                    break;

                case PLUGIN_BASIC4GL_EXT_WORD:
                    // Copy Basic4GL int to C word (int16_t)
                    value.setIntVal((int) cData.getChar());
                    break;

                case PLUGIN_BASIC4GL_EXT_INT:
                    // Copy Basic4GL int to C int
                    value.setIntVal(cData.getInt());
                    break;

                case PLUGIN_BASIC4GL_EXT_INT64:
                    // Copy Basic4GL int to C 64bit integer (int64_t)
                    // TODO review for casting/truncation errors;
                    //  original source looks like it has a potential bug with value.IntVal() = *((int64_t*)cData), where IntValue() is an int&
                    value.setIntVal((int)cData.getLong());
                    break;

                case PLUGIN_BASIC4GL_EXT_FLOAT:
                    // Copy Basic4GL real to C float
                    value.setRealVal(cData.getFloat());
                    break;

                case PLUGIN_BASIC4GL_EXT_DOUBLE:
                    // Copy Basic4GL real to C double
                    value.setRealVal((float)cData.getDouble());
                    break;

                case PLUGIN_BASIC4GL_EXT_STRING:

                    // Allocate string handle (if necessary)
                    strIndex = value.getIntVal();
                    if (strIndex == 0) {
                        strIndex = vm.allocString();
                        value.setIntVal(strIndex);
                    }

                    // Copy string to Basic4GL
                    vm.setString(strIndex, cData);
                    cData += type.getStringSize();
                    break;

                default:
                    assertTrue(false);
            }
        }
    private void BasicValueFromCValue(PluginDataType type, Mutable<Integer> basicDataIndex, ByteBuffer cData) {

            assertTrue(type.getPointerLevel() == 0);
            assertTrue(type.getArrayLevel() == 0);
            assertTrue(type.getBaseType() < 0);

            if (type.getBaseType() == PLUGIN_BASIC4GL_EXT_PADDING)
                cData += type.getStringSize();
            else {
                BasicValueFromCValue(type, vm.getData().data().get(basicDataIndex.get()), cData);
                basicDataIndex.set(basicDataIndex.get() + 1);
            }
        }
    private void BasicDataFromCData(PluginDataType type, Mutable<Integer> basicDataIndex, ByteBuffer cData) {

            if (type.getPointerLevel() > 0) {

                // Note: We cannot convert pointers, so we just store NULL.
                vm.getData().data().get(basicDataIndex.get()).setIntVal(0);
                cData += sizeof(void*);
                basicDataIndex.set(basicDataIndex.get() + 1);
            }
            else if (type.getArrayLevel() > 0) {

                // Convert array
                BasicArrayFromCArray(type, basicDataIndex, cData);

            }
            else if (type.getBaseType() > 0) {

                // Convert structure
                BasicStructureFromCStructure(type, basicDataIndex, cData);

            }
            else {

                // Convert value
                BasicValueFromCValue(type, basicDataIndex, cData);
            }
        }


    public TomVMJARAdapter(TomVM vm, PluginStructureManager structureManager) {
        this.vm = vm;
        this.structureManager = structureManager;
    }

    // IDLL_Basic4GL_Runtime interface

    // Regular parameters
    public  int getIntParam(int index) {
            return vm.getIntParam(index);
        }
    public  float getFloatParam(int index) {
            return vm.getRealParam(index);
        }
    public  String getStringParam(int index) {
            return vm.getStringParam(index);
        }

    // Regular return values
    public  void setIntResult(int result) {
            vm.getReg().setIntVal(result);
        }
    public  void setFloatResult(float result){
            vm.getReg().setRealVal(result);
        }
    public  void setStringResult(String result) {
            vm.setRegString(result);
        }

    // Array parameters
    public  void getIntArrayParam(
            int index,
            int[] array,
            int dimensions,
            int dimension0Size,
        int... otherDimensions) {

            assertTrue(dimensions < ARRAY_MAX_DIMENSIONS);
            int[] dimensionArray = new int[ARRAY_MAX_DIMENSIONS];
            if (dimensions > 0)
            {
                dimensionArray[0] = dimension0Size;
                for (int i = 1; i < dimensions; i++) {
                    dimensionArray[i] = otherDimensions[i - 1];
                }
            }

            ReadIntArray(
                    vm.getIntParam(index),
                    array,
                    dimensions,
                    dimensionArray);
        }
    public  void getFloatArrayParam(
            int index,
            float[] array,
            int dimensions,
            int dimension0Size,
            int... otherDimensions) {
            assertTrue(dimensions < ARRAY_MAX_DIMENSIONS);
            int[] dimensionArray = new int[ARRAY_MAX_DIMENSIONS];
            if (dimensions > 0)
            {
                dimensionArray[0] = dimension0Size;
                for (int i = 1; i < dimensions; i++) {
                    dimensionArray[i] = otherDimensions[i - 1];
                }
            }

            ReadFloatArray(
                    vm.getIntParam(index),
                    array,
                    dimensions,
                    dimensionArray);
        }
    public  int getArrayParamDimension(
            int index,
            int dimension) {

            return ArrayDimensionSize(vm.getData(), vm.getIntParam(index), dimension);
        }

    // Array results
    public  void setIntArrayResult(
            int [] array,
            int dimensions,
            int dimension0Size,
            int... otherDimensions) {

            assertTrue(dimensions < ARRAY_MAX_DIMENSIONS);
            int[] dimensionArray = new int[ARRAY_MAX_DIMENSIONS];
            if (dimensions > 0)
            {
                dimensionArray[0] = dimension0Size;

                for (int i = 1; i < dimensions; i++) {
                    dimensionArray[i] = otherDimensions[i - 1];
                }
            }

            // Allocate temporary array
            int dataIndex = CreateTempArray(new ValType(VTP_INT), dimensions, dimensionArray);

            // Fill it with data
            WriteIntArray(dataIndex, IntBuffer.wrap(array), dimensions, dimensionArray);

            // Assign array to virtual machine register
            vm.getReg().setIntVal(dataIndex);
        }
    public  void setFloatArrayResult(
            float []array,
            int dimensions,
            int dimension0Size,
            int... otherDimensions) {

            assertTrue(dimensions < ARRAY_MAX_DIMENSIONS);
            int[] dimensionArray = new int[ARRAY_MAX_DIMENSIONS];
            if (dimensions > 0)
            {
                dimensionArray[0] = dimension0Size;

                for (int i = 1; i < dimensions; i++) {
                    dimensionArray[i] = otherDimensions[i - 1];
                }
            }

            // Allocate temporary array
            int dataIndex = CreateTempArray(new ValType(VTP_INT), dimensions, dimensionArray);

            // Fill it with data
            WriteFloatArray(dataIndex, array, dimensions, dimensionArray);

            // Assign array to virtual machine register
            vm.getReg().setIntVal(dataIndex);
        }

    // General purpose data access routines
    public  void setType(
            int baseType) {

            currentType = PluginDataType.simpleType(baseType);
        }
    public  void setStringType(
            int size) {

            currentType = PluginDataType.string(size);
        }
    public  void modTypeArray(
            int dimensions,
            int dimension0Size,
            int... otherDimensions) {

            assertTrue(dimensions < ARRAY_MAX_DIMENSIONS);
            int[] dimensionArray = new int[ARRAY_MAX_DIMENSIONS];
            if (dimensions > 0)
            {
                dimensionArray[0] = dimension0Size;

                for (int i = 1; i < dimensions; i++) {
                    dimensionArray[i] = otherDimensions[i - 1];
                }
            }

            currentType.setArrayLevel(dimensions);
            int *dimensionSize = dimensionArray;
            for (int i = dimensions - 1; i >= 0; i--) {
                currentType.getArrayDims()[i] = *dimensionSize;
                dimensionSize++;
            }
        }
    public  void modTypeReference() {

            if (!currentType.isByReference()) {
                currentType.setPointerLevel(currentType.getPointerLevel() + 1);
                currentType.setByReference(true);
            }
        }
    public  void getParam(
            int index,
            ByteBuffer dst) {

            // Ensure data type is complete
            FixCurrentType();

            // Simple type?
            if (currentType.getArrayLevel() == 0 &&
                    currentType.getPointerLevel() == 0 &&
                    currentType.getBaseType() < 0) {
                CValueFromBasicValue(currentType, dst, vm.getParam(index));
            }
            else {

                // Dereference
                int dataIndex = vm.getParam(index).getIntVal();

                // Get dereferenced type
                PluginDataType derefType = currentType;
                derefType.deref();

                // Convert data
                CDataFromBasicData(derefType, dst, dataIndex);
            }
        }
    public  void setParam(
            int index,
            ByteBuffer src) {

            // Ensure data type is complete
            FixCurrentType();

            // Simple type
            if (currentType.getArrayLevel() == 0 &&
                    currentType.getPointerLevel() == 0 &&
                    currentType.getBaseType() < 0) {
                BasicValueFromCValue(currentType, vm.getParam(index), src);
            }
            else {

                // Dereference
                int dataIndex = vm.getParam(index).getIntVal();

                // Get dereferenced type
                PluginDataType derefType = currentType;
                derefType.deref();

                // Convert data
                BasicDataFromCData(derefType, dataIndex, src);
            }
        }
    public  void setReturnValue(
            ByteBuffer src) {

            // Ensure data type is complete
            FixCurrentType();

            // Simple type
            if (currentType.getArrayLevel() == 0 &&
                    currentType.getPointerLevel() == 0 &&
                    currentType.getBaseType() < 0) {

                // Special case! String values are written to RegString
                if (currentType.getBaseType() == PLUGIN_BASIC4GL_EXT_STRING) {
                    vm.setRegString( ( char*)src);
                } else {
                    BasicValueFromCValue(currentType, vm.getReg(), src);
                }
            }
            else {

                // Non simple.

                // Get dereferenced type
                PluginDataType derefType = currentType;
                derefType.deref();

                // Allocate temporary storage space for result.
                ValType vmType = structureManager.vmTypeFromPluginType(derefType);
                int returnDataIndex = CreateTempData(vmType);

                // Convert data and write to temp space
                int dataIndex = returnDataIndex;
                BasicDataFromCData(derefType, dataIndex, src);

                // Return reference to result data in register
                vm.getReg().setIntVal(returnDataIndex);
            }
        }

    // Direct data access
    public  int directGetInt(int memAddr) {
        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            return vm.getData().data().get(memAddr).getIntVal();

        }
    public  float directGetFloat(int memAddr) {

        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            return vm.getData().data().get(memAddr).getRealVal();
        }
    public  char * DirectGetString(int memAddr, char* str, int maxLen) {

        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            int index = vm.getData().data().get(memAddr).getIntVal();
            String s = vm.getString(index);

            // Copy string to buffer
            int len = s.length();
            if (len > maxLen - 1)
                len = maxLen - 1;
            for (int i = 0; i < len; i++) {
                str[i] = s.c_str()[i];
            }
            str[len] = 0;

            return str;
        }
    public  void directSetInt(int memAddr, int value) {

        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            vm.getData().data().get(memAddr).setIntVal(value);
        }
    public  void directSetFloat(int memAddr, float value) {

        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            vm.getData().data().get(memAddr).setRealVal(value);
        }
    public  void directSetString(int memAddr, String str) {
        assertTrue(memAddr > 0);
        assertTrue(memAddr < vm.getData().data().size());
            Value dest = vm.getData().data().get(memAddr);

            // Allocate string space if necessary
            if (dest.getIntVal() == 0)
                dest.setIntVal(vm.allocString());

            // Store string
            vm.setString(dest.getIntVal(), str);

        }

    // Long running functions
    public  void beginLongRunningFunction(Basic4GLLongRunningFunction handler) {

            vm.beginLongRunningFunction(handler);
        }
    public  void endLongRunningFunction() {

            vm.endLongRunningFunction();
        }

    /**
     * Runtime error reporting
     */
    @Override
    public void  functionError(String text) {

            vm.functionError(text);
        }

    //

    /**
     * Conditional timesharing break
     */
    public void  isTimeshareBreakRequired() {

            vm.isTimeshareBreakRequired();
        }
}
