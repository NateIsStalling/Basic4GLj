package com.basic4gl.library.desktopgl;

import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.ValType;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;


/**
 * Created by Nate on 4/19/2015.
 */
public class Routines {
    public static boolean ValidateSizeParam (TomVM vm, int paramIndex) {
        int size = vm.getIntParam(paramIndex);
        if (size <= 0)                      // If size is 0, do nothing.
        {
            return false;                   // Program will still proceed.
        }
        if (size > 65536) {                 // If size is greater 64K, this is a run-time error
            vm.functionError("Size must be 0 - 65536 (Basic4GL restriction)");
            return false;
        }

        // Size is valid
        return true;
    }

    public static int ReadArrayDynamic (   TomVM vm,
                                    int paramIndex,
                                    ValType type,
                                    int cType,
                                    ByteBuffer array,
                                    int maxSize) {

        // Use the appropriate template function for the given type
        switch (cType) {
            case GL11.GL_BYTE:           return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array, maxSize);
            case GL11.GL_UNSIGNED_BYTE:  return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array, maxSize);
            case GL11.GL_SHORT:          return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_UNSIGNED_SHORT: return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_INT:            return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_UNSIGNED_INT:   return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_FLOAT:          return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asFloatBuffer(), maxSize);
            case GL11.GL_2_BYTES:        return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_3_BYTES:
                vm.functionError("Data type GL_3_BYTES not supported. (Basic4GL restriction).");
                return 0;
            case GL11.GL_4_BYTES:        return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_DOUBLE:         return Data.readAndZero(vm.getData(), vm.getIntParam(paramIndex), type, array.asDoubleBuffer(), maxSize);
            default:
                vm.functionError("Data type must be a GL_xxx data type. (Basic4GL restriction).");
                return 0;
        }
    }

    public static int WriteArrayDynamic (   TomVM vm,
                                     int paramIndex,
                                     ValType type,
                                     int cType,
                                     ByteBuffer array,
                                     int maxSize) {

        // Use the appropriate template function for the given type
        switch (cType) {
            case GL11.GL_BYTE:           return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array, maxSize);
            case GL11.GL_UNSIGNED_BYTE:  return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array, maxSize);
            case GL11.GL_SHORT:          return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_UNSIGNED_SHORT: return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_INT:            return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_UNSIGNED_INT:   return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_FLOAT:          return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asFloatBuffer(), maxSize);
            case GL11.GL_2_BYTES:        return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asShortBuffer(), maxSize);
            case GL11.GL_3_BYTES:
                vm.functionError("Data type GL_3_BYTES not supported. (Basic4GL restriction).");
                return 0;
            case GL11.GL_4_BYTES:        return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asIntBuffer(), maxSize);
            case GL11.GL_DOUBLE:         return Data.writeArray(vm.getData(), vm.getIntParam(paramIndex), type, array.asDoubleBuffer(), maxSize);
            default:
                vm.functionError("Data type must be a GL_xxx data type. (Basic4GL restriction).");
                return 0;
        }
    }
}
