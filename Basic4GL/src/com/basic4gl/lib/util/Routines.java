package com.basic4gl.lib.util;

import com.basic4gl.vm.Data;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.Buffer;
import java.nio.ByteBuffer;


/**
 * Created by Nate on 4/19/2015.
 */
public class Routines {
    public static boolean ValidateSizeParam (TomVM vm, int paramIndex) {
        int size = vm.GetIntParam (paramIndex);
        if (size <= 0)                      // If size is 0, do nothing.
            return false;                   // Program will still proceed.
        if (size > 65536) {                 // If size is greater 64K, this is a run-time error
            vm.FunctionError ("Size must be 0 - 65536 (Basic4GL restriction)");
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
            case GL11.GL_BYTE:           return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.array(), maxSize);
            case GL11.GL_UNSIGNED_BYTE:  return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.array(), maxSize);
            case GL11.GL_SHORT:          return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_UNSIGNED_SHORT: return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_INT:            return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_UNSIGNED_INT:   return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_FLOAT:          return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asFloatBuffer().array(), maxSize);
            case GL11.GL_2_BYTES:        return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_3_BYTES:
                vm.FunctionError ("Data type GL_3_BYTES not supported. (Basic4GL restriction).");
                return 0;
            case GL11.GL_4_BYTES:        return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_DOUBLE:         return Data.ReadAndZero(vm.Data(), vm.GetIntParam(paramIndex), type, array.asDoubleBuffer().array(), maxSize);
            default:
                vm.FunctionError ("Data type must be a GL_xxx data type. (Basic4GL restriction).");
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
            case GL11.GL_BYTE:           return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.array(), maxSize);
            case GL11.GL_UNSIGNED_BYTE:  return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.array(), maxSize);
            case GL11.GL_SHORT:          return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_UNSIGNED_SHORT: return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_INT:            return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_UNSIGNED_INT:   return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_FLOAT:          return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asFloatBuffer().array(), maxSize);
            case GL11.GL_2_BYTES:        return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asShortBuffer().array(), maxSize);
            case GL11.GL_3_BYTES:
                vm.FunctionError ("Data type GL_3_BYTES not supported. (Basic4GL restriction).");
                return 0;
            case GL11.GL_4_BYTES:        return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asIntBuffer().array(), maxSize);
            case GL11.GL_DOUBLE:         return Data.WriteArray(vm.Data(), vm.GetIntParam(paramIndex), type, array.asDoubleBuffer().array(), maxSize);
            default:
                vm.FunctionError ("Data type must be a GL_xxx data type. (Basic4GL restriction).");
                return 0;
        }
    }
}