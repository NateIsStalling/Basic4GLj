package com.basic4gl.library.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.BinaryOperatorExtension;
import com.basic4gl.compiler.util.UnaryOperatorExtension;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Created by Nate on 11/1/2015.
 */
public class TrigBasicLib implements FunctionLibrary {

    // Matrix constructors.
    // Note: These all drop their result into the global "matrix" variable (below)
    public static float[] matrix = new float[16];
    private static float[]
            v1  = new float[4],
            v2 = new float[4],
            m1 = new float[16],
            m2 = new float[16];

    // Indices
    int scaleVec, scaleVec2, scaleMatrix, scaleMatrix2, matrixVec, matrixMatrix,
            divVec, divMatrix, vecVec, vecPlusVec, vecMinusVec,
            matrixPlusMatrix, matrixMinusMatrix, negVec, negMatrix;

    @Override
    public String name() {
        return "TrigBasicLib";
    }
    @Override
    public String description() {
        return "Matrix and vector routines.";
    }

    @Override
    public void init(TomVM vm, String[] args) {
        //////////////////////////////////
        // Register overloaded operators
        scaleVec            = vm.addOperatorFunction(new OpScaleVec());
        scaleVec2           = vm.addOperatorFunction(new OpScaleVec2());
        scaleMatrix         = vm.addOperatorFunction(new OpScaleMatrix());
        scaleMatrix2        = vm.addOperatorFunction(new OpScaleMatrix2());
        divVec              = vm.addOperatorFunction(new OpDivVec());
        divMatrix           = vm.addOperatorFunction(new OpDivMatrix());
        matrixVec           = vm.addOperatorFunction(new OpMatrixVec());
        matrixMatrix        = vm.addOperatorFunction(new OpMatrixMatrix());
        vecVec              = vm.addOperatorFunction(new OpVecVec());
        vecPlusVec          = vm.addOperatorFunction(new OpVecPlusVec());
        vecMinusVec         = vm.addOperatorFunction(new OpVecMinusVec());
        matrixPlusMatrix    = vm.addOperatorFunction(new OpMatrixPlusMatrix());
        matrixMinusMatrix   = vm.addOperatorFunction(new OpMatrixMinusMatrix());
        negVec              = vm.addOperatorFunction(new OpNegVec());
        negMatrix           = vm.addOperatorFunction(new OpNegMatrix());

    }
    @Override
    public void init(TomBasicCompiler comp){
        // Compiler callback
        comp.addUnOperExtension(new TrigUnOperatorExtension());
        comp.addBinOperExtension(new TrigBinOperatorExtension());

        scaleVec            = comp.VM().addOperatorFunction(new OpScaleVec());
        scaleVec2           = comp.VM().addOperatorFunction(new OpScaleVec2());
        scaleMatrix         = comp.VM().addOperatorFunction(new OpScaleMatrix());
        scaleMatrix2        = comp.VM().addOperatorFunction(new OpScaleMatrix2());
        divVec              = comp.VM().addOperatorFunction(new OpDivVec());
        divMatrix           = comp.VM().addOperatorFunction(new OpDivMatrix());
        matrixVec           = comp.VM().addOperatorFunction(new OpMatrixVec());
        matrixMatrix        = comp.VM().addOperatorFunction(new OpMatrixMatrix());
        vecVec              = comp.VM().addOperatorFunction(new OpVecVec());
        vecPlusVec          = comp.VM().addOperatorFunction(new OpVecPlusVec());
        vecMinusVec         = comp.VM().addOperatorFunction(new OpVecMinusVec());
        matrixPlusMatrix    = comp.VM().addOperatorFunction(new OpMatrixPlusMatrix());
        matrixMinusMatrix   = comp.VM().addOperatorFunction(new OpMatrixMinusMatrix());
        negVec              = comp.VM().addOperatorFunction(new OpNegVec());
        negMatrix           = comp.VM().addOperatorFunction(new OpNegMatrix());
    }

    @Override
    public void cleanup() {
        //Do nothing
    }

    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<String, FunctionSpecification[]>();
        s.put ("Vec4", new FunctionSpecification[]{ new FunctionSpecification(WrapVec4.class, new ParamTypeList( BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Vec3", new FunctionSpecification[]{ new FunctionSpecification(WrapVec3.class, new ParamTypeList ( BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Vec2", new FunctionSpecification[]{ new FunctionSpecification(WrapVec2.class, new ParamTypeList ( BasicValType.VTP_REAL, BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("MatrixZero", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixZero.class, new ParamTypeList (), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixIdentity", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixIdentity.class, new ParamTypeList (), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixScale", new FunctionSpecification[]{
                new FunctionSpecification(WrapMatrixScale.class, new ParamTypeList ( BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null),
                new FunctionSpecification(WrapMatrixScale_2.class, new ParamTypeList ( BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)
        });
        s.put ("MatrixTranslate", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixTranslate.class, new ParamTypeList ( BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateX", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixRotateX.class, new ParamTypeList ( BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateY", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixRotateY.class, new ParamTypeList ( BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateZ", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixRotateZ.class, new ParamTypeList ( BasicValType.VTP_REAL), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotate", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixRotate.class, new ParamTypeList ( new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixBasis", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixBasis.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixCrossProduct", new FunctionSpecification[]{ new FunctionSpecification(WrapMatrixCrossProduct.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("CrossProduct", new FunctionSpecification[]{ new FunctionSpecification(WrapCross.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Length", new FunctionSpecification[]{ new FunctionSpecification(WrapLength.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, BasicValType.VTP_REAL, false, false, null)});
        s.put ("Normalize", new FunctionSpecification[]{ new FunctionSpecification(WrapNormalize.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Determinant", new FunctionSpecification[]{ new FunctionSpecification(WrapDeterminant.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, BasicValType.VTP_REAL, false, false, null)});
        s.put ("Transpose", new FunctionSpecification[]{ new FunctionSpecification(WrapTranspose.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("RTInvert", new FunctionSpecification[]{ new FunctionSpecification(WrapRTInvert.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("Orthonormalize", new FunctionSpecification[]{ new FunctionSpecification(WrapOrthonormalize.class, new ParamTypeList ( new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType  (BasicValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        return s;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }


    public static float[] getGlobalMatrix(){ return matrix;}
    public static void clearMatrix() {
        Arrays.fill(matrix, 0f);
    }
    public static void identity() {
        clearMatrix();
        matrix [0]  = 1;
        matrix [5]  = 1;
        matrix [10] = 1;
        matrix [15] = 1;
    }
    public static void scale(float scale) {
        clearMatrix();
        matrix [0]  = scale;
        matrix [5]  = scale;
        matrix [10] = scale;
        matrix [15] = 1;
    }
    public static void scale(float x, float y, float z) {
        clearMatrix();
        matrix [0]  = x;
        matrix [5]  = y;
        matrix [10] = z;
        matrix [15] = 1;
    }
    public static void translate(float x, float y, float z) {
        identity();
        matrix [12] = x;
        matrix [13] = y;
        matrix [14] = z;
    }
    public static void rotateAxis(float ang, int main, int a1, int a2) {
        clearMatrix();
        float  cosa = (float) Math.cos(ang * Standard.M_DEG2RAD),
                sina = (float) Math.sin(ang * Standard.M_DEG2RAD);
        matrix [15] = 1;
        matrix [main * 4 + main] = 1.0f;
        matrix [a1 + a1 * 4] =  cosa;
        matrix [a1 + a2 * 4] =  sina;
        matrix [a2 + a1 * 4] = -sina;
        matrix [a2 + a2 * 4] =  cosa;
    }
    public static void rotateX(float ang) {  rotateAxis(ang, 0, 2, 1); }
    public static void rotateY(float ang) {  rotateAxis(ang, 1, 0, 2); }
    public static void rotateZ(float ang) {  rotateAxis(ang, 2, 1, 0); }
    public static void crossProduct(float[] vec) {

        // Create a matrix which corresponds to the cross product with vec
        // I.e Mr = vec x r
        clearMatrix();
        matrix [1]  =  vec [2];		// Fill in non zero bits
        matrix [2]  = -vec [1];
        matrix [4]  = -vec [2];
        matrix [6]  =  vec [0];
        matrix [8]  =  vec [1];
        matrix [9]  = -vec [0];
        matrix [15] = 1;
    }
    public static void copyMatrix(float[] dst, float[] src) {
        System.arraycopy(src, 0, dst, 0, dst.length);
    }

    //region Basic trig functions
    public static float calcW(float[] v1, float[] v2) {
        if (v1 [3] == 0 && v2 [3] == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public static void crossProduct(float[] v1, float[] v2, float[] result) {
        assertTrue(v1 != null);
        assertTrue(v1 != null);
        assertTrue(result != null);
        result [0]	= v1 [1] * v2 [2] - v1 [2] * v2 [1];
        result [1]	= v1 [2] * v2 [0] - v1 [0] * v2 [2];
        result [2]	= v1 [0] * v2 [1] - v1 [1] * v2 [0];

        // Calculate w
        result [3] = calcW(v1, v2);
    }
    public static void crossProduct(float[] v1, int offset1, float[] v2, int offset2, float[] result, int offset3) {
        assertTrue(v1 != null);
        assertTrue(v1 != null);
        assertTrue(result != null);
        result [0 + offset3]	= v1 [1 + offset1] * v2 [2 + offset2] - v1 [2 + offset1] * v2 [1 + offset2];
        result [1 + offset3]	= v1 [2 + offset1] * v2 [0 + offset2] - v1 [0 + offset1] * v2 [2 + offset2];
        result [2 + offset3]	= v1 [0 + offset1] * v2 [1 + offset2] - v1 [1 + offset1] * v2 [0 + offset2];

        // Calculate w
        result [3 + offset3] = calcW(Arrays.copyOfRange(v1, offset1, v1.length), Arrays.copyOfRange(v2, offset2, v2.length));
    }
    public static float dotProduct(float[] v1, float[] v2) {
        assertTrue(v1 != null);
        assertTrue(v2 != null);
        return v1 [0] * v2 [0] + v1 [1] * v2 [1] + v1 [2] * v2 [2];
    }
    public static float dotProduct(float[] v1, int offset1, float[] v2, int offset2) {
        assertTrue(v1 != null);
        assertTrue(v2 != null);
        return v1 [0 + offset1] * v2 [0 + offset2] + v1 [1 + offset1] * v2 [1 + offset2] + v1 [2 + offset1] * v2 [2 + offset2];
    }
    public static float length(float[] v) {
        assertTrue(v != null);
        float dp = dotProduct(v, v);
        return (float)Math.sqrt (dp);
    }
    public static float length(float[] v, int offset) {
        assertTrue(v != null);
        float dp = dotProduct(v, offset, v, offset);
        return (float)Math.sqrt (dp);
    }
    public static void scale(float[] v, float scale) {
        assertTrue(v != null);
        v [0] *= scale;
        v [1] *= scale;
        v [2] *= scale;
        if (v [3] != 0) {
            v [3] = 1;
        }
    }
    public static void scale(float[] v, int offset, float scale) {
        assertTrue(v != null);
        v [0 + offset] *= scale;
        v [1 + offset] *= scale;
        v [2 + offset] *= scale;
        if (v [3 + offset] != 0) {
            v [3 + offset] = 1;
        }
    }
    public static void scaleMatrix(float[] m, float scale) {
        assertTrue(m != null);
        for (int i = 0; i < 16; i++) {
            m [i] *= scale;
        }
    }
    public static void normalize(float[] v) {
        float len = length(v);
        if (len > 0.0001) {
            scale(v, 1.0f / length(v));
        }
    }
    public static void normalize(float[] v, int offset) {
        float len = length(v, offset);
        if (len > 0.0001) {
            scale(v, offset, 1.0f / (length(v, offset)));
        }
    }
    public static float determinant(float[] m) {
        assertTrue(m != null);

        // Calculate matrix determinant
        float res = 0;
        for (int y = 0; y < 4; y++) {
            res += m [y]
                    * m [4  + ((y + 1) & 3)]
                    * m [8  + ((y + 2) & 3)]
                    * m [12 + ((y + 3) & 3)];
            res -= m [y]
                    * m [4  + ((y - 1) & 3)]
                    * m [8  + ((y - 2) & 3)]
                    * m [12 + ((y - 3) & 3)];
        }
        return res;
    }
    public static void transpose(float[] src, float[] dst) {
        assertTrue(src != null);
        assertTrue(dst != null);

        // Transpose matrix
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                dst[x * 4 + y] = src[y * 4 + x];
            }
        }
    }
    public static void matrixTimesVec(float[] m, float[] v, float[] result) {
        assertTrue(m != null);
        assertTrue(v != null);
        assertTrue(result != null);
        for (int y = 0; y < 4; y++) {
            float coord = 0;
            for (int x = 0; x < 4; x++) {
                coord += v [x] * m [x * 4 + y];
            }
            result [y] = coord;
        }
    }
    public static void matrixTimesVec(float[] m, int offset1, float[] v, int offset2, float[] result, int offset3) {
        assertTrue(m != null);
        assertTrue(v != null);
        assertTrue(result != null);
        for (int y = 0; y < 4; y++) {
            float coord = 0;
            for (int x = 0; x < 4; x++) {
                coord += v [x + offset2] * m [x * 4 + y + offset1];
            }
            result [y + offset3] = coord;
        }
    }
    public static void matrixTimesMatrix(float[] m1, float[] m2, float[] result) {
        assertTrue(m1 != null);
        assertTrue(m2 != null);
        assertTrue(result != null);
        assertTrue(result != m1);
        assertTrue(result != m2);
        for (int x2 = 0; x2 < 4; x2++) {
            for (int y1 = 0; y1 < 4; y1++) {
                float coord = 0;
                for (int i = 0; i < 4; i++) {
                    coord += m2 [x2 * 4 + i] * m1 [i * 4 + y1];
                }
                result [x2 * 4 + y1] = coord;
            }
        }
    }
    public static void vecPlus(float[] v1, float[] v2, float[] result) {
        assertTrue(v1 != null);
        assertTrue(v2 != null);
        assertTrue(result != null);

        // Add vectors
        result [0] = v1 [0] + v2 [0];
        result [1] = v1 [1] + v2 [1];
        result [2] = v1 [2] + v2 [2];

        // Calculate w
        result [3] = calcW(v1, v2);
    }
    public static void vecMinus(float[] v1, float[] v2, float[] result) {
        assertTrue(v1 != null);
        assertTrue(v2 != null);
        assertTrue(result != null);

        // Subtract vectors
        result [0] = v1 [0] - v2 [0];
        result [1] = v1 [1] - v2 [1];
        result [2] = v1 [2] - v2 [2];

        // Calculate w
        result [3] = calcW(v1, v2);
    }
    public static void matrixPlus(float[]  m1, float[] m2, float[] result) {
        assertTrue(m1 != null);
        assertTrue(m2 != null);
        assertTrue(result != null);

        // Add matrices
        for (int i = 0; i < 16; i++) {
            result [i] = m1 [i] + m2 [i];
        }
    }
    public static void matrixMinus(float[] m1, float[] m2, float[] result) {
        assertTrue(m1 != null);
        assertTrue(m2 != null);
        assertTrue(result != null);

        // Add matrices
        for (int i = 0; i < 16; i++) {
            result [i] = m1 [i] - m2 [i];
        }
    }

    /**
     * Invert an "RT" matrix, where "RT" means that the matrix is made from
     * rotations and translations only.
     * @param m
     * @param result
     */
    public static void matrixRTInvert(float[] m, float[] result) {

        // Transpose matrix to invert the rotation part
        transpose(m, result);

        // Clear out the transposed translation component of the original matrix
        result [3]  = 0;
        result [7]  = 0;
        result [11] = 0;

        // Calculate the new translation component
        float[] t = new float[4];
        matrixTimesVec(result, 0, m, 12, t, 0);
        result [12] = -t[0];
        result [13] = -t[1];
        result [14] = -t[2];
    }

    /**
     * Ensure rotation component of matrix is orthonormal, via a series of
     * normalizations and cross products.
     * @param m
     */
    public static void orthonormalize(float[] m) {
        // Normalize Z vector
        normalize(m, 8);

        // Cross product Y with Z to get X
        // Then normalize resulting X
        crossProduct(m, 4, m, 8, m , 0);
        normalize(m);

        // Cross product Z with X to get Y
        crossProduct(m, 8, m, 0, m, 4);
    }

    /**
     * Arbitrary axis rotation
     * @param ang
     * @param v
     */
    public static void rotateAxis(float ang, float[] v) {

        // Thanks to Satin Hinge for sending me the arbitrary axis maths
        // Normalize vector
        float[] N = new float[3];
        N[0] = v[0];
        N[1] = v[1];
        N[2] = v[2];
        normalize(N);

        // Precalc sin/cos
        float c = (float) Math.cos(ang * Standard.M_DEG2RAD), s = (float) Math.sin(ang * Standard.M_DEG2RAD);

        // Construct matrix
        clearMatrix();
        matrix[0] = (1-c)*N[0]*N[0]+c;      matrix[4] = (1-c)*N[0]*N[1]-s*N[2]; matrix[8] = (1-c)*N[0]*N[2]+s*N[1];
        matrix[1] = (1-c)*N[0]*N[1]+s*N[2]; matrix[5] = (1-c)*N[1]*N[1]+c;      matrix[9] = (1-c)*N[1]*N[2]-s*N[0];
        matrix[2] = (1-c)*N[0]*N[2]-s*N[1]; matrix[6] = (1-c)*N[1]*N[2]+s*N[0]; matrix[10]= (1-c)*N[2]*N[2]+c;
        matrix[15] = 1;
    }

    static void returnMatrix(TomVM vm) {
        vm.getReg().setIntVal (Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, matrix));
    }

    /**
     * Read a 3D vector.
     * This can be a 2, 3 or 4 element vector, but will always be returned as a 4
     * element vector. (z = 0 & w = 1 if not specified.)
     * @param vm
     * @param index
     * @param v
     * @return vector size, or -1 if error
     */
    static int readVec(TomVM vm, int index, float [] v) {
        assertTrue(v != null);

        int size = Data.getArrayDimensionSize(vm.getData(), index, 0);
        if (size < 2 || size > 4) {
            vm.functionError("Vector must be 2, 3 or 4 element vector");
            return -1; // -1 = error
        }

        // Read in vector and convert to 4 element format
        v [2] = 0;
        v [3] = 1;
        Data.readArray(vm.getData(), index, new ValType (BasicValType.VTP_REAL, (byte) 1, (byte) 1, true), v, size);

        // Return original size
        return size;
    }

    /**
     * Read 3D matrix.
     * Matrix must be 4x4
     * @param vm
     * @param index
     * @param m
     * @return
     */
    static boolean readMatrix(TomVM vm, int index, float[] m) {
        assertTrue(m != null);

        if (Data.getArrayDimensionSize(vm.getData(), index, 0) != 4
                ||  Data.getArrayDimensionSize(vm.getData(), index, 1) != 4) {
            vm.functionError("Matrix must be a 4x4 matrix (e.g 'dim matrix#(3)(3)' )");
            return false;
        }

        // Read in matrix
        Data.readArray(vm.getData(), index, new ValType (BasicValType.VTP_REAL, (byte) 2, (byte) 1, true), m, 16);
        return true;
    }

    //endregion

    //region Function wrappers

    public static final class WrapVec4 implements Function { public void run(TomVM vm){

        float[] vec4 = new float[]{ vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1) };
        vm.getReg().setIntVal ( Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 4, vec4));
    }
    }
    public static final class WrapVec3 implements Function { public void run(TomVM vm){

        float[] vec3 = new float[]{ vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1) };
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 3, vec3));
    }
    }
    public static final class WrapVec2 implements Function { public void run(TomVM vm){

        float[] vec2 = new float[]{ vm.getRealParam(2), vm.getRealParam(1) };
        vm.getReg().setIntVal ( Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, vec2));
    }
    }
    public static final class WrapMatrixZero implements Function { public void run(TomVM vm){

        clearMatrix();
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixIdentity implements Function { public void run(TomVM vm){

        identity();
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixScale implements Function { public void run(TomVM vm){

        scale(vm.getRealParam(1));
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixScale_2 implements Function { public void run(TomVM vm){

        scale(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixTranslate implements Function { public void run(TomVM vm){

        translate(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixRotateX implements Function { public void run(TomVM vm){
        rotateX(vm.getRealParam(1)); returnMatrix(vm); }
    }
    public static final class WrapMatrixRotateY implements Function { public void run(TomVM vm){
        rotateY(vm.getRealParam(1)); returnMatrix(vm); }
    }
    public static final class WrapMatrixRotateZ implements Function { public void run(TomVM vm){
        rotateZ(vm.getRealParam(1)); returnMatrix(vm); }
    }
    public static final class WrapMatrixRotate implements Function { public void run(TomVM vm){

        if (readVec(vm, vm.getIntParam(1), v1) < 0) {
            return;
        }
        rotateAxis(vm.getRealParam(2), v1);
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixBasis implements Function { public void run(TomVM vm){

        clearMatrix();
        matrix [15] = 1;
        Data.readArray(vm.getData(), vm.getIntParam(3), new ValType (BasicValType.VTP_REAL, (byte) 1, (byte) 1, true), Arrays.copyOfRange(matrix, 0, 4), 4);
        Data.readArray(vm.getData(), vm.getIntParam(2), new ValType (BasicValType.VTP_REAL, (byte) 1, (byte) 1, true), Arrays.copyOfRange(matrix, 4, 8), 4);
        Data.readArray(vm.getData(), vm.getIntParam(1), new ValType (BasicValType.VTP_REAL, (byte)1, (byte)1, true), Arrays.copyOfRange(matrix, 8, 12), 4);
        returnMatrix(vm);
    }
    }
    public static final class WrapMatrixCrossProduct implements Function { public void run(TomVM vm){

        if (readVec(vm, vm.getIntParam(1), v1) < 0) {
            return;
        }
        crossProduct(v1);
        returnMatrix(vm);
    }
    }
    public static final class WrapCross implements Function { public void run(TomVM vm){


        // Fetch vectors
        int s1 = readVec(vm, vm.getIntParam(2), v1),
                s2 = readVec(vm, vm.getIntParam(1), v2);
        if (s1 < 0 || s2 < 0) {
            return;
        }

        // Calculate cross product vector
        float[] result = new float [4];
        crossProduct(v1, v2, result);

        // Return resulting vector
        // (Vector will be the same length as the first source vector)
        vm.getReg().setIntVal ( Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), Math.max(Math.max(s1, s2), 3), result));
    }
    }
    public static final class WrapLength implements Function { public void run(TomVM vm){


        // Fetch vector
        if (readVec(vm, vm.getIntParam(1), v1) < 0) {
            return;
        }

        // Calculate length
        vm.getReg().setRealVal(length(v1));
    }
    }
    public static final class WrapNormalize implements Function { public void run(TomVM vm){


        // Fetch vector
        int size = readVec(vm, vm.getIntParam(1), v1);
        if (size < 0) {
            return;
        }

        // Normalize vector
        normalize(v1);

        // Return resulting vector
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), size, v1));
    }
    }
    public static final class WrapDeterminant implements Function { public void run(TomVM vm){


        // Fetch matrix
        if (!readMatrix(vm, vm.getIntParam(1), m1)) {
            return;
        }

        // Return result
        vm.getReg().setRealVal(determinant(m1));
    }
    }
    public static final class WrapTranspose implements Function { public void run(TomVM vm){


        // Fetch matrix
        if (!readMatrix(vm, vm.getIntParam(1), m1)) {
            return;
        }

        // Transpose
        transpose(m1, m2);

        // Create new matrix and assign to register
        vm.getReg().setIntVal(Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, m2));
    }
    }
    public static final class WrapRTInvert implements Function { public void run(TomVM vm){


        // Fetch matrix
        if (!readMatrix(vm, vm.getIntParam(1), m1)) {
            return;
        }

        // RTInvert
        matrixRTInvert(m1, m2);

        // Create new matrix and assign to register
        vm.getReg().setIntVal (Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, m2));
    }
    }
    public static final class WrapOrthonormalize implements Function { public void run(TomVM vm){


        // Fetch matrix
        if (!readMatrix(vm, vm.getIntParam(1), m1)) {
            return;
        }

        // Orthonormalize
        orthonormalize(m1);

        // Create new matrix and assign to register
        vm.getReg().setIntVal (Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, m1));
    }
    }

    //endregion

    //region Overloaded operators

    void doScaleVec(TomVM vm, float scale, int vecIndex) {

        // Extract data
        int size = readVec(vm, vecIndex, v1);
        if (size < 0) {
            return;
        }

        // Scale 3D vector
        scale(v1, scale);

        // Return as temp vector (using original size)
        vm.getReg().setIntVal ( Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), size, v1));
    }

    void doScaleMatrix(TomVM vm, float scale, int matrixIndex) {

        // Read in matrix
        if (!readMatrix(vm, matrixIndex, m1)) {
            return;
        }

        // Scale matrix
        scaleMatrix(m1, scale);

        // Create new matrix and assign to register
        vm.getReg().setIntVal ( Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, m1));
    }
    public final class OpScaleVec implements Function {
        public void run(TomVM vm) {
            doScaleVec(vm, vm.getReg().getRealVal(), vm.getReg2().getIntVal());
        }
    }
    public final class OpScaleVec2 implements Function {
        public void run(TomVM vm) {
        doScaleVec(vm, vm.getReg2().getRealVal(), vm.getReg().getIntVal ());
    }}
    public final class OpDivVec implements Function {
        public void run(TomVM vm) {
        doScaleVec(vm, (float) (1.0 / vm.getReg().getRealVal()), vm.getReg2().getIntVal ());
    }}

    public final class OpScaleMatrix implements Function { public void run(TomVM vm){
        doScaleMatrix(vm, vm.getReg().getRealVal(), vm.getReg2().getIntVal ());
    }}
    public final class OpScaleMatrix2 implements Function { public void run(TomVM vm){
        doScaleMatrix(vm, vm.getReg2().getRealVal (), vm.getReg().getIntVal ());
    }}
    public final class OpDivMatrix implements Function { public void run(TomVM vm){
        doScaleMatrix(vm, (float) (1.0 / vm.getReg().getRealVal()), vm.getReg2().getIntVal ());
    }}
    public static final class OpMatrixVec implements Function { public void run(TomVM vm){

        // Matrix at reg2. Vector at reg.

        // Read in matrix
        if (!readMatrix(vm, vm.getReg2().getIntVal(), m1)) {
            return;
        }

        // Read in vector
        int size = readVec(vm, vm.getReg().getIntVal (), v1);
        if (size < 0) {
            return;
        }

        // Calculate resulting vector
        float[] result =new float[4];
        matrixTimesVec(m1, v1, result);

        // Return as temporary vector
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), size, result));
    }}
    public static final class OpMatrixMatrix implements Function { public void run(TomVM vm){

        // Matrix * Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!readMatrix(vm, vm.getReg2().getIntVal(), m1)
                ||  !readMatrix(vm, vm.getReg().getIntVal(), m2)) {
            return;
        }

        // Multiply them out
        float[] result = new float [16];
        matrixTimesMatrix(m1, m2, result);

        // Return as temporary matrix
        vm.getReg().setIntVal( Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, result));
    }}
    public static final class OpVecVec implements Function { public void run(TomVM vm){

        // Vector * Vector = dot product

        // Fetch vectors
        if (readVec(vm, vm.getReg2().getIntVal (), v1) < 0
                || readVec(vm, vm.getReg().getIntVal (), v2) < 0) {
            return;
        }

        // Return result
        vm.getReg().setRealVal(dotProduct(v1, v2));
    }}
    public static final class OpVecPlusVec implements Function { public void run(TomVM vm){

        // Fetch vectors
        int s1 = readVec(vm, vm.getReg2().getIntVal (), v1),
                s2 = readVec(vm, vm.getReg().getIntVal (), v2);
        if (s1 < 0 || s2 < 0) {
            return;
        }

        // Calculate result
        float[] result = new float [4];
        vecPlus(v1, v2, result);

        // Return as temporary vector
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), Math.max(s1, s2), result));
    }}
    public static final class OpVecMinusVec implements Function { public void run(TomVM vm){

        // Fetch vectors
        int s1 = readVec(vm, vm.getReg2().getIntVal (), v1),
                s2 = readVec(vm, vm.getReg().getIntVal (), v2);
        if (s1 < 0 || s2 < 0) {
            return;
        }

        // Calculate result
        float[] result = new float[4];
        vecMinus(v1, v2, result);

        // Return as temporary vector
        vm.getReg().setIntVal ( Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), Math.max(s1, s2), result));
    }}
    public static final class OpMatrixPlusMatrix implements Function { public void run(TomVM vm){

        // Matrix + Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!readMatrix(vm, vm.getReg2().getIntVal (), m1)
                || !readMatrix(vm, vm.getReg().getIntVal (), m2)) {
            return;
        }

        // Add them
        float[] result = new float[16];
        matrixPlus(m1, m2, result);

        // Return as temporary matrix
        vm.getReg().setIntVal ( Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, Arrays.asList(result)));
    }}
    public static final class OpMatrixMinusMatrix implements Function { public void run(TomVM vm){

        // Matrix - Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!readMatrix(vm, vm.getReg2().getIntVal(), m1)
                || !readMatrix(vm, vm.getReg().getIntVal (), m2)) {
            return;
        }

        // Add them
        float[] result = new float [16];
        matrixMinus(m1, m2, result);

        // Return as temporary matrix
        vm.getReg().setIntVal (Data.fillTempRealArray2D(vm.getData(), vm.getDataTypes(), 4, 4, Arrays.asList(result)));
    }}
    public final class OpNegVec implements Function { public void run(TomVM vm)      { doScaleVec(vm, -1, vm.getReg().getIntVal()); }}
    public final class OpNegMatrix  implements Function { public void run(TomVM vm){ doScaleMatrix(vm, -1, vm.getReg().getIntVal ()); }}


    // Compiler callback
    public final class TrigUnOperatorExtension implements UnaryOperatorExtension {

        public boolean run (  Mutable<ValType> regType,     // IN: Current type in register.                                                        OUT: Required type cast before calling function
                                    short oper,          // IN: Operator being applied; OpCode
                                    Mutable<Integer> operFunction,      // OUT: Index of VM_CALL_OPERATOR_FUNC function to call
                                    Mutable<ValType> resultType,  // OUT: Resulting value type
                                    Mutable<Boolean> freeTempData) {   // OUT: Set to true if temp data needs to be freed

        // Must be real values, and not pointers (references are OK however)
        if (regType.get().getVirtualPointerLevel() > 0 || regType.get().basicType != BasicValType.VTP_REAL) {
            return false;
        }

        if (oper == OpCode.OP_OP_NEG) {
            if (regType.get().arrayLevel == 1) {                // -Vector
                operFunction.set(negVec);
                resultType.get().setType(regType.get());
                freeTempData.set(true);
                return true;
            }
            if (regType.get().arrayLevel == 2) {                // -Matrix
                operFunction.set(negMatrix);
                resultType.get().setType(regType.get());
                freeTempData.set(true);
                return true;
            }
        }

        return false;
    }
    }
    public final class TrigBinOperatorExtension implements BinaryOperatorExtension {

        public boolean run(Mutable<ValType> regType,     // IN: Current type in register.                                                        OUT: Required type cast before calling function
                           Mutable<ValType> reg2Type,    // IN: Current type in second register (operation is reg2 OP reg1, e.g reg2 + reg1):    OUT: Required type cast before calling function
                           short oper,          // IN: Operator being applied
                           Mutable<Integer> operFunction,      // OUT: Index of VM_CALL_OPERATOR_FUNC function to call
                           Mutable<ValType> resultType,  // OUT: Resulting value type
                           Mutable<Boolean> freeTempData) {   // OUT: Set to true if temp data needs to be freed

            // Pointers not accepted (references are OK however)
            if (regType.get().getVirtualPointerLevel() > 0 || reg2Type.get().getVirtualPointerLevel() > 0) {
                return false;
            }

            // Validate data types. We will only work with ints and reals
            if (regType.get().basicType != BasicValType.VTP_REAL && regType.get().basicType != BasicValType.VTP_INT) {
                return false;
            }
            if (reg2Type.get().basicType != BasicValType.VTP_REAL && reg2Type.get().basicType != BasicValType.VTP_INT) {
                return false;
            }

            // Is acceptible to have an integer value, but must be typecast to a real
            // Arrays of integers not acceptible
            if (regType.get().basicType == BasicValType.VTP_INT) {
                if (regType.get().isBasicType()) {
                    regType.get().basicType = BasicValType.VTP_REAL;
                } else {
                    return false;
                }
            }
            if (reg2Type.get().basicType == BasicValType.VTP_INT) {
                if (reg2Type.get().isBasicType()) {
                    reg2Type.get().basicType = BasicValType.VTP_REAL;
                } else {
                    return false;
                }
            }

            // Look for recognised combinations
            if (oper == OpCode.OP_OP_TIMES) {
                if (regType.get().arrayLevel == 0 && reg2Type.get().arrayLevel == 1) {          // Vector * scalar
                    operFunction.set(scaleVec);
                    resultType.get().setType(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().arrayLevel == 1 && reg2Type.get().arrayLevel == 0) {     // Scalar * vector
                    operFunction.set(scaleVec2);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                }
                if (regType.get().arrayLevel == 0 && reg2Type.get().arrayLevel == 2) {          // Vector * scalar
                    operFunction.set(scaleMatrix);
                    resultType.get().setType(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().arrayLevel == 2 && reg2Type.get().arrayLevel == 0) {     // Scalar * vector
                    operFunction.set(scaleMatrix2);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (reg2Type.get().arrayLevel == 2 && regType.get().arrayLevel == 1) {     // Matrix * vector
                    operFunction.set(matrixVec);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (reg2Type.get().arrayLevel == 2 && regType.get().arrayLevel == 2) {     // Matrix * matrix
                    operFunction.set(matrixMatrix);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().arrayLevel == 1 && reg2Type.get().arrayLevel == 1) {     // Vec * Vec (Dot product)
                    operFunction.set(vecVec);
                    resultType.get().setType(BasicValType.VTP_REAL);
                    freeTempData.set(false);
                    return true;
                }
                return false;
            } else if (oper == OpCode.OP_OP_DIV) {
                if (regType.get().arrayLevel == 0 && reg2Type.get().arrayLevel == 1) {          // Vector / scalar
                    operFunction.set(divVec);
                    resultType.get().setType(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                }
                if (regType.get().arrayLevel == 0 && reg2Type.get().arrayLevel == 2) {          // Matrix / scalar
                    operFunction.set(divMatrix);
                    resultType.get().setType(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                }
            } else if (oper == OpCode.OP_OP_PLUS) {
                if (regType.get().arrayLevel == 1 && reg2Type.get().arrayLevel == 1) {          // Vector + vector
                    operFunction.set(vecPlusVec);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().arrayLevel == 2 && reg2Type.get().arrayLevel == 2) {     // Matrix + matrix
                    operFunction.set(matrixPlusMatrix);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                }
            } else if (oper == OpCode.OP_OP_MINUS) {
                if (regType.get().arrayLevel == 1 && reg2Type.get().arrayLevel == 1) {          // Vector - vector
                    operFunction.set(vecMinusVec);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().arrayLevel == 2 && reg2Type.get().arrayLevel == 2) {     // Matrix - matrix
                    operFunction.set(matrixMinusMatrix);
                    resultType.get().setType(regType.get());
                    freeTempData.set(true);
                    return true;
                }
            }
            return false;
        }
    }

    //endregion
}
