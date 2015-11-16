package com.basic4gl.lib.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.BinOperExt;
import com.basic4gl.compiler.util.UnOperExt;
import com.basic4gl.lib.util.Library;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.util.Mutable;
import com.basic4gl.vm.Data;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.OpCode;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 11/1/2015.
 */
public class TrigBasicLib implements Library {
    @Override
    public String name() {
        return "TrigBasicLib";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public String description() {
        return "Matrix and vector routines.";
    }

    @Override
    public String author() {
        return "";
    }

    @Override
    public String contact() {
        return "";
    }

    @Override
    public String id() {
        return "trig";
    }

    @Override
    public String[] compat() {
        return new String[]{"desktopgl"};
    }

    @Override
    public void init(TomVM vm) {
        //////////////////////////////////
        // Register overloaded operators
        scaleVec            = vm.AddOperatorFunction (new OpScaleVec());
        scaleVec2           = vm.AddOperatorFunction (new OpScaleVec2());
        scaleMatrix         = vm.AddOperatorFunction (new OpScaleMatrix());
        scaleMatrix2        = vm.AddOperatorFunction (new OpScaleMatrix2());
        divVec              = vm.AddOperatorFunction (new OpDivVec());
        divMatrix           = vm.AddOperatorFunction (new OpDivMatrix());
        matrixVec           = vm.AddOperatorFunction (new OpMatrixVec());
        matrixMatrix        = vm.AddOperatorFunction (new OpMatrixMatrix());
        vecVec              = vm.AddOperatorFunction (new OpVecVec());
        vecPlusVec          = vm.AddOperatorFunction (new OpVecPlusVec());
        vecMinusVec         = vm.AddOperatorFunction (new OpVecMinusVec());
        matrixPlusMatrix    = vm.AddOperatorFunction (new OpMatrixPlusMatrix());
        matrixMinusMatrix   = vm.AddOperatorFunction (new OpMatrixMinusMatrix());
        negVec              = vm.AddOperatorFunction (new OpNegVec());
        negMatrix           = vm.AddOperatorFunction (new OpNegMatrix());


    }
    @Override
    public void init(TomBasicCompiler comp){
        // Compiler callback
        comp.AddUnOperExt   (new TrigUnOperatorExtension());
        comp.AddBinOperExt  (new TrigBinOperatorExtension());
    }
    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new HashMap<String, FuncSpec[]>();
        s.put ("Vec4", new FuncSpec[]{ new FuncSpec(WrapVec4.class, new ParamTypeList( ValType.VTP_REAL, ValType.VTP_REAL, ValType.VTP_REAL, ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Vec3", new FuncSpec[]{ new FuncSpec(WrapVec3.class, new ParamTypeList ( ValType.VTP_REAL, ValType.VTP_REAL, ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Vec2", new FuncSpec[]{ new FuncSpec(WrapVec2.class, new ParamTypeList ( ValType.VTP_REAL, ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("MatrixZero", new FuncSpec[]{ new FuncSpec(WrapMatrixZero.class, new ParamTypeList (), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixIdentity", new FuncSpec[]{ new FuncSpec(WrapMatrixIdentity.class, new ParamTypeList (), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixScale", new FuncSpec[]{ new FuncSpec(WrapMatrixScale.class, new ParamTypeList ( ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixScale", new FuncSpec[]{ new FuncSpec(WrapMatrixScale_2.class, new ParamTypeList ( ValType.VTP_REAL, ValType.VTP_REAL, ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixTranslate", new FuncSpec[]{ new FuncSpec(WrapMatrixTranslate.class, new ParamTypeList ( ValType.VTP_REAL, ValType.VTP_REAL, ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateX", new FuncSpec[]{ new FuncSpec(WrapMatrixRotateX.class, new ParamTypeList ( ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateY", new FuncSpec[]{ new FuncSpec(WrapMatrixRotateY.class, new ParamTypeList ( ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotateZ", new FuncSpec[]{ new FuncSpec(WrapMatrixRotateZ.class, new ParamTypeList ( ValType.VTP_REAL), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixRotate", new FuncSpec[]{ new FuncSpec(WrapMatrixRotate.class, new ParamTypeList ( new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixBasis", new FuncSpec[]{ new FuncSpec(WrapMatrixBasis.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("MatrixCrossProduct", new FuncSpec[]{ new FuncSpec(WrapMatrixCrossProduct.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("CrossProduct", new FuncSpec[]{ new FuncSpec(WrapCross.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Length", new FuncSpec[]{ new FuncSpec(WrapLength.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, ValType.VTP_REAL, false, false, null)});
        s.put ("Normalize", new FuncSpec[]{ new FuncSpec(WrapNormalize.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put ("Determinant", new FuncSpec[]{ new FuncSpec(WrapDeterminant.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, ValType.VTP_REAL, false, false, null)});
        s.put ("Transpose", new FuncSpec[]{ new FuncSpec(WrapTranspose.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("RTInvert", new FuncSpec[]{ new FuncSpec(WrapRTInvert.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
        s.put ("Orthonormalize", new FuncSpec[]{ new FuncSpec(WrapOrthonormalize.class, new ParamTypeList ( new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true)), true, true, new ValType (ValType.VTP_REAL, (byte)2, (byte)1, true), false, true, null)});
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

    // Matrix constructors.
// Note: These all drop their result into the global "matrix" variable (below)
    public static float matrix[] = new float[16];
    private static float[]
            v1  = new float[4],
            v2 = new float[4],
            m1 = new float[16],
            m2 = new float[16];

    public static float[] getGlobalMatrix(){ return matrix;}
    public static void ClearMatrix () {
        Arrays.fill(matrix, 0f);
    }
    public static void Identity () {
        ClearMatrix();
        matrix [0]  = 1;
        matrix [5]  = 1;
        matrix [10] = 1;
        matrix [15] = 1;
    }
    public static void Scale (float scale) {
        ClearMatrix ();
        matrix [0]  = scale;
        matrix [5]  = scale;
        matrix [10] = scale;
        matrix [15] = 1;
    }
    public static void Scale (float x, float y, float z) {
        ClearMatrix ();
        matrix [0]  = x;
        matrix [5]  = y;
        matrix [10] = z;
        matrix [15] = 1;
    }
    public static void Translate (float x, float y, float z) {
        Identity ();
        matrix [12] = x;
        matrix [13] = y;
        matrix [14] = z;
    }
    public static void RotateAxis (float ang, int main, int a1, int a2) {
        ClearMatrix ();
        float  cosa = (float) Math.cos(ang * Standard.M_DEG2RAD),
                sina = (float) Math.sin(ang * Standard.M_DEG2RAD);
        matrix [15] = 1;
        matrix [main * 4 + main] = 1.0f;
        matrix [a1 + a1 * 4] =  cosa;
        matrix [a1 + a2 * 4] =  sina;
        matrix [a2 + a1 * 4] = -sina;
        matrix [a2 + a2 * 4] =  cosa;
    }
    public static void RotateX (float ang) {  RotateAxis (ang, 0, 2, 1); }
    public static void RotateY (float ang) {  RotateAxis (ang, 1, 0, 2); }
    public static void RotateZ (float ang) {  RotateAxis (ang, 2, 1, 0); }
    public static void CrossProduct (float[] vec) {

        // Create a matrix which corresponds to the cross product with vec
        // I.e Mr = vec x r
        ClearMatrix ();
        matrix [1]  =  vec [2];		// Fill in non zero bits
        matrix [2]  = -vec [1];
        matrix [4]  = -vec [2];
        matrix [6]  =  vec [0];
        matrix [8]  =  vec [1];
        matrix [9]  = -vec [0];
        matrix [15] = 1;
    }
    public static void CopyMatrix (float[] dst, float[] src) {
        System.arraycopy(src, 0, dst, 0, dst.length);
    }

    ////////////////////////////////////////////////////////////////////////////////
// Basic trig functions
    public static float CalcW (float[] v1, float[] v2) {
        if (v1 [3] == 0 && v2 [3] == 0) return 0;
        else                            return 1;
    }

    public static void CrossProduct (float[] v1, float[] v2, float[] result) {
        assert (v1 != null);
        assert (v1 != null);
        assert (result != null);
        result [0]	= v1 [1] * v2 [2] - v1 [2] * v2 [1];
        result [1]	= v1 [2] * v2 [0] - v1 [0] * v2 [2];
        result [2]	= v1 [0] * v2 [1] - v1 [1] * v2 [0];

        // Calculate w
        result [3] = CalcW (v1, v2);
    }
    public static void CrossProduct (float[] v1, int offset1, float[] v2, int offset2, float[] result, int offset3) {
        assert (v1 != null);
        assert (v1 != null);
        assert (result != null);
        result [0 + offset3]	= v1 [1 + offset1] * v2 [2 + offset2] - v1 [2 + offset1] * v2 [1 + offset2];
        result [1 + offset3]	= v1 [2 + offset1] * v2 [0 + offset2] - v1 [0 + offset1] * v2 [2 + offset2];
        result [2 + offset3]	= v1 [0 + offset1] * v2 [1 + offset2] - v1 [1 + offset1] * v2 [0 + offset2];

        // Calculate w
        result [3 + offset3] = CalcW (Arrays.copyOfRange(v1, offset1, v1.length), Arrays.copyOfRange(v2, offset2, v2.length));
    }
    public static float DotProduct (float[] v1, float[] v2) {
        assert (v1 != null);
        assert (v2 != null);
        return v1 [0] * v2 [0] + v1 [1] * v2 [1] + v1 [2] * v2 [2];
    }
    public static float DotProduct (float[] v1, int offset1, float[] v2, int offset2) {
        assert (v1 != null);
        assert (v2 != null);
        return v1 [0 + offset1] * v2 [0 + offset2] + v1 [1 + offset1] * v2 [1 + offset2] + v1 [2 + offset1] * v2 [2 + offset2];
    }
    public static float Length (float[] v) {
        assert (v != null);
        float dp = DotProduct(v, v);
        return (float)Math.sqrt (dp);
    }
    public static float Length (float[] v, int offset) {
        assert (v != null);
        float dp = DotProduct(v, offset, v, offset);
        return (float)Math.sqrt (dp);
    }
    public static void Scale (float[] v, float scale) {
        assert (v != null);
        v [0] *= scale;
        v [1] *= scale;
        v [2] *= scale;
        if (v [3] != 0)
            v [3] = 1;
    }
    public static void Scale (float[] v, int offset, float scale) {
        assert (v != null);
        v [0 + offset] *= scale;
        v [1 + offset] *= scale;
        v [2 + offset] *= scale;
        if (v [3 + offset] != 0)
            v [3 + offset] = 1;
    }
    public static void ScaleMatrix (float[] m, float scale) {
        assert (m != null);
        for (int i = 0; i < 16; i++)
            m [i] *= scale;
    }
    public static void Normalize (float[] v) {
        float len = Length (v);
        if (len > 0.0001)
            Scale (v, 1.0f / Length (v));
    }
    public static void Normalize (float[] v, int offset) {
        float len = Length (v, offset);
        if (len > 0.0001)
            Scale (v, offset, 1.0f / (Length (v, offset)));
    }
    public static float Determinant (float[] m) {
        assert (m != null);

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
    public static void Transpose (float[] src, float[] dst) {
        assert (src != null);
        assert (dst != null);

        // Transpose matrix
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 4; x++)
                dst [x * 4 + y] = src [y * 4 + x];
    }
    public static void MatrixTimesVec (float[] m, float[] v, float[] result) {
        assert (m != null);
        assert (v != null);
        assert (result != null);
        for (int y = 0; y < 4; y++) {
            float coord = 0;
            for (int x = 0; x < 4; x++)
                coord += v [x] * m [x * 4 + y];
            result [y] = coord;
        }
    }
    public static void MatrixTimesVec (float[] m, int offset1, float[] v, int offset2, float[] result, int offset3) {
        assert (m != null);
        assert (v != null);
        assert (result != null);
        for (int y = 0; y < 4; y++) {
            float coord = 0;
            for (int x = 0; x < 4; x++)
                coord += v [x + offset2] * m [x * 4 + y + offset1];
            result [y + offset3] = coord;
        }
    }
    public static void MatrixTimesMatrix (float[] m1, float[] m2, float[] result) {
        assert (m1 != null);
        assert (m2 != null);
        assert (result != null);
        assert (result != m1);
        assert (result != m2);
        for (int x2 = 0; x2 < 4; x2++) {
            for (int y1 = 0; y1 < 4; y1++) {
                float coord = 0;
                for (int i = 0; i < 4; i++)
                    coord += m2 [x2 * 4 + i] * m1 [i * 4 + y1];
                result [x2 * 4 + y1] = coord;
            }
        }
    }
    public static void VecPlus (float[] v1, float[] v2, float[] result) {
        assert (v1 != null);
        assert (v2 != null);
        assert (result != null);

        // Add vectors
        result [0] = v1 [0] + v2 [0];
        result [1] = v1 [1] + v2 [1];
        result [2] = v1 [2] + v2 [2];

        // Calculate w
        result [3] = CalcW (v1, v2);
    }
    public static void VecMinus (float[] v1, float[] v2, float[] result) {
        assert (v1 != null);
        assert (v2 != null);
        assert (result != null);

        // Subtract vectors
        result [0] = v1 [0] - v2 [0];
        result [1] = v1 [1] - v2 [1];
        result [2] = v1 [2] - v2 [2];

        // Calculate w
        result [3] = CalcW (v1, v2);
    }
    public static void MatrixPlus (float[]  m1, float[] m2, float[] result) {
        assert (m1 != null);
        assert (m2 != null);
        assert (result != null);

        // Add matrices
        for (int i = 0; i < 16; i++)
            result [i] = m1 [i] + m2 [i];
    }
    public static void MatrixMinus (float[] m1, float[] m2, float[] result) {
        assert (m1 != null);
        assert (m2 != null);
        assert (result != null);

        // Add matrices
        for (int i = 0; i < 16; i++)
            result [i] = m1 [i] - m2 [i];
    }
    public static void RTInvert (float[] m, float[] result) {

        // Invert an "RT" matrix, where "RT" means that the matrix is made from
        // rotations and translations only.

        // Transpose matrix to invert the rotation part
        Transpose (m, result);

        // Clear out the transposed translation component of the original matrix
        result [3]  = 0;
        result [7]  = 0;
        result [11] = 0;

        // Calculate the new translation component
        float t[] = new float[4];
        MatrixTimesVec (result, 0, m, 12, t, 0);
        result [12] = -t[0];
        result [13] = -t[1];
        result [14] = -t[2];
    }
    public static void Orthonormalize (float[] m) {

        // Ensure rotation component of matrix is orthonormal, via a series of
        // normalizations and cross products.

        // Normalize Z vector
        Normalize (m, 8);

        // Cross product Y with Z to get X
        // Then normalize resulting X
        CrossProduct (m, 4, m, 8, m , 0);
        Normalize (m);

        // Cross product Z with X to get Y
        CrossProduct (m, 8, m, 0, m, 4);
    }

    // Arbitrary axis rotation
    public static void RotateAxis(float ang, float[] v) {

        // Thanks to Satin Hinge for sending me the arbitrary axis maths
        // Normalize vector
        float N[] = new float[3];
        N[0] = v[0];
        N[1] = v[1];
        N[2] = v[2];
        Normalize(N);

        // Precalc sin/cos
        float c = (float) Math.cos(ang * Standard.M_DEG2RAD), s = (float) Math.sin(ang * Standard.M_DEG2RAD);

        // Construct matrix
        ClearMatrix();
        matrix[0] = (1-c)*N[0]*N[0]+c;      matrix[4] = (1-c)*N[0]*N[1]-s*N[2]; matrix[8] = (1-c)*N[0]*N[2]+s*N[1];
        matrix[1] = (1-c)*N[0]*N[1]+s*N[2]; matrix[5] = (1-c)*N[1]*N[1]+c;      matrix[9] = (1-c)*N[1]*N[2]-s*N[0];
        matrix[2] = (1-c)*N[0]*N[2]-s*N[1]; matrix[6] = (1-c)*N[1]*N[2]+s*N[0]; matrix[10]= (1-c)*N[2]*N[2]+c;
        matrix[15] = 1;
    }

    static void ReturnMatrix (TomVM vm) {
        vm.Reg ().setIntVal (Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, matrix));
    }

    ////////////////////////////////////////////////////////////////////////////////
// Read vector/matrix

    static int ReadVec (TomVM vm, int index, float [] v) {
        assert (v != null);

        // Read a 3D vector.
        // This can be a 2, 3 or 4 element vector, but will always be returned as a 4
        // element vector. (z = 0 & w = 1 if not specified.)
        int size = Data.ArrayDimensionSize(vm.Data(), index, 0);
        if (size < 2 || size > 4) {
            vm.FunctionError ("Vector must be 2, 3 or 4 element vector");
            return -1;                  // -1 = error
        }

        // Read in vector and convert to 4 element format
        v [2] = 0;
        v [3] = 1;
        Data.ReadArray(vm.Data(), index, new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), v, size);

        // Return original size
        return size;
    }

    static boolean ReadMatrix (TomVM vm, int index, float[] m) {
        assert (m != null);

        // Read 3D matrix.
        // Matrix must be 4x4
        if (Data.ArrayDimensionSize(vm.Data(), index, 0) != 4
                ||  Data.ArrayDimensionSize(vm.Data(), index, 1) != 4) {
            vm.FunctionError ("Matrix must be a 4x4 matrix (e.g 'dim matrix#(3)(3)' )");
            return false;
        }

        // Read in matrix
        Data.ReadArray(vm.Data(), index, new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), m, 16);
        return true;
    }
    ////////////////////////////////////////////////////////////////////////////////
// Function wrappers

    public final class WrapVec4 implements Function { public void run(TomVM vm){

        Float vec4[] = new Float[]{ vm.GetRealParam (4), vm.GetRealParam (3), vm.GetRealParam (2), vm.GetRealParam (1) };
        vm.Reg ().setIntVal ( Data.FillTempRealArray(vm.Data(), vm.DataTypes(), 4, Arrays.asList(vec4)));
    }
    }
    public final class WrapVec3 implements Function { public void run(TomVM vm){

        Float vec3 [] = new Float[]{ vm.GetRealParam (3), vm.GetRealParam (2), vm.GetRealParam (1) };
        vm.Reg ().setIntVal(Data.FillTempRealArray(vm.Data(), vm.DataTypes(), 3, Arrays.asList(vec3)));
    }
    }
    public final class WrapVec2 implements Function { public void run(TomVM vm){

        Float vec2 [] = new Float[]{ vm.GetRealParam (2), vm.GetRealParam (1) };
        vm.Reg ().setIntVal ( Data.FillTempRealArray(vm.Data(), vm.DataTypes(), 2, Arrays.asList(vec2)));
    }
    }
    public final class WrapMatrixZero implements Function { public void run(TomVM vm){

        ClearMatrix ();
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixIdentity implements Function { public void run(TomVM vm){

        Identity();
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixScale implements Function { public void run(TomVM vm){

        Scale(vm.GetRealParam(1));
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixScale_2 implements Function { public void run(TomVM vm){

        Scale(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixTranslate implements Function { public void run(TomVM vm){

        Translate(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixRotateX implements Function { public void run(TomVM vm){
        RotateX(vm.GetRealParam(1)); ReturnMatrix (vm); }
    }
    public final class WrapMatrixRotateY implements Function { public void run(TomVM vm){
        RotateY(vm.GetRealParam(1)); ReturnMatrix (vm); }
    }
    public final class WrapMatrixRotateZ implements Function { public void run(TomVM vm){
        RotateZ(vm.GetRealParam(1)); ReturnMatrix (vm); }
    }
    public final class WrapMatrixRotate implements Function { public void run(TomVM vm){

        if (ReadVec (vm, vm.GetIntParam(1), v1) < 0)
            return;
        RotateAxis(vm.GetRealParam(2), v1);
        ReturnMatrix(vm);
    }
    }
    public final class WrapMatrixBasis implements Function { public void run(TomVM vm){

        ClearMatrix ();
        matrix [15] = 1;
        Data.ReadArray(vm.Data(), vm.GetIntParam(3), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), Arrays.copyOfRange(matrix, 0, 4), 4);
        Data.ReadArray(vm.Data(), vm.GetIntParam(2), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), Arrays.copyOfRange(matrix, 4, 8), 4);
        Data.ReadArray (vm.Data (), vm.GetIntParam (1), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true), Arrays.copyOfRange(matrix, 8, 12), 4);
        ReturnMatrix (vm);
    }
    }
    public final class WrapMatrixCrossProduct implements Function { public void run(TomVM vm){

        if (ReadVec (vm, vm.GetIntParam (1), v1) < 0)
            return;
        CrossProduct (v1);
        ReturnMatrix (vm);
    }
    }
    public final class WrapCross implements Function { public void run(TomVM vm){


// Fetch vectors
        int s1 = ReadVec (vm, vm.GetIntParam (2), v1),
                s2 = ReadVec (vm, vm.GetIntParam (1), v2);
        if (s1 < 0 || s2 < 0)
            return;

// Calculate cross product vector
        float result[] = new float [4];
        CrossProduct (v1, v2, result);

// Return resulting vector
// (Vector will be the same length as the first source vector)
        vm.Reg ().setIntVal ( Data.FillTempRealArray(vm.Data(), vm.DataTypes(), Math.max(Math.max(s1, s2), 3), result));
    }
    }
    public final class WrapLength implements Function { public void run(TomVM vm){


// Fetch vector
        if (ReadVec (vm, vm.GetIntParam (1), v1) < 0)
            return;

// Calculate length
        vm.Reg ().setRealVal(Length(v1));
    }
    }
    public final class WrapNormalize implements Function { public void run(TomVM vm){


// Fetch vector
        int size = ReadVec (vm, vm.GetIntParam (1), v1);
        if (size < 0)
            return;

// Normalize vector
        Normalize (v1);

// Return resulting vector
        vm.Reg ().setIntVal(Data.FillTempRealArray(vm.Data(), vm.DataTypes(), size, v1));
    }
    }
    public final class WrapDeterminant implements Function { public void run(TomVM vm){


// Fetch matrix
        if (!ReadMatrix (vm, vm.GetIntParam (1), m1))
            return;

// Return result
        vm.Reg ().setRealVal(Determinant(m1));
    }
    }
    public final class WrapTranspose implements Function { public void run(TomVM vm){


// Fetch matrix
        if (!ReadMatrix (vm, vm.GetIntParam (1), m1))
            return;

// Transpose
        Transpose (m1, m2);

// Create new matrix and assign to register
        vm.Reg ().setIntVal(Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(m2)));
    }
    }
    public final class WrapRTInvert implements Function { public void run(TomVM vm){


// Fetch matrix
        if (!ReadMatrix (vm, vm.GetIntParam (1), m1))
            return;

// RTInvert
        RTInvert(m1, m2);

// Create new matrix and assign to register
        vm.Reg ().setIntVal (Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(m2)));
    }
    }
    public final class WrapOrthonormalize implements Function { public void run(TomVM vm){


// Fetch matrix
        if (!ReadMatrix (vm, vm.GetIntParam (1), m1))
            return;

// Orthonormalize
        Orthonormalize(m1);

// Create new matrix and assign to register
        vm.Reg ().setIntVal (Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(m1)));
    }
    }

    ////////////////////////////////////////////////////////////////////////////////
// Overloaded operators
    void DoScaleVec (TomVM vm, float scale, int vecIndex) {

        // Extract data
        int size = ReadVec (vm, vecIndex, v1);
        if (size < 0)
            return;

        // Scale 3D vector
        Scale(v1, scale);

        // Return as temp vector (using original size)
        vm.Reg ().setIntVal ( Data.FillTempRealArray(vm.Data(), vm.DataTypes(), size, v1));
    }
    public final class OpScaleVec implements Function {
        public void run(TomVM vm) {
            DoScaleVec(vm, vm.Reg().getRealVal(), vm.Reg2().getIntVal());
        }
    }
    public final class OpScaleVec2 implements Function {
        public void run(TomVM vm) {
        DoScaleVec (vm, vm.Reg2().getRealVal(), vm.Reg().getIntVal ());
    }}
    public final class OpDivVec implements Function {
        public void run(TomVM vm) {
        DoScaleVec (vm, (float) (1.0 / vm.Reg ().getRealVal()), vm.Reg2().getIntVal ());
    }}
    void DoScaleMatrix (TomVM vm, float scale, int matrixIndex) {

        // Read in matrix
        if (!ReadMatrix (vm, matrixIndex, m1))
            return;

        // Scale matrix
        ScaleMatrix (m1, scale);

        // Create new matrix and assign to register
        vm.Reg ().setIntVal ( Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(m1)));
    }
    public final class OpScaleMatrix implements Function { public void run(TomVM vm){
        DoScaleMatrix (vm, vm.Reg ().getRealVal(), vm.Reg2 ().getIntVal ());
    }}
    public final class OpScaleMatrix2 implements Function { public void run(TomVM vm){
        DoScaleMatrix (vm, vm.Reg2().getRealVal (), vm.Reg().getIntVal ());
    }}
    public final class OpDivMatrix implements Function { public void run(TomVM vm){
        DoScaleMatrix (vm, (float) (1.0 / vm.Reg ().getRealVal()), vm.Reg2().getIntVal ());
    }}
    public final class OpMatrixVec implements Function { public void run(TomVM vm){

        // Matrix at reg2. Vector at reg.

        // Read in matrix
        if (!ReadMatrix (vm, vm.Reg2 ().getIntVal(), m1))
            return;

        // Read in vector
        int size = ReadVec (vm, vm.Reg ().getIntVal (), v1);
        if (size < 0)
            return;

        // Calculate resulting vector
        float result[] =new float[4];
        MatrixTimesVec (m1, v1, result);

        // Return as temporary vector
        vm.Reg().setIntVal(Data.FillTempRealArray(vm.Data(), vm.DataTypes(), size, result));
    }}
    public final class OpMatrixMatrix implements Function { public void run(TomVM vm){

        // Matrix * Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!ReadMatrix (vm, vm.Reg2 ().getIntVal(), m1)
                ||  !ReadMatrix (vm, vm.Reg  ().getIntVal(), m2))
            return;

        // Multiply them out
        float result[] = new float [16];
        MatrixTimesMatrix(m1, m2, result);

        // Return as temporary matrix
        vm.Reg ().setIntVal( Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(result)));
    }}
    public final class OpVecVec implements Function { public void run(TomVM vm){

        // Vector * Vector = dot product

        // Fetch vectors
        if (ReadVec (vm, vm.Reg2 ().getIntVal (), v1) < 0
                || ReadVec (vm, vm.Reg  ().getIntVal (), v2) < 0)
            return;

        // Return result
        vm.Reg ().setRealVal(DotProduct(v1, v2));
    }}
    public final class OpVecPlusVec implements Function { public void run(TomVM vm){

        // Fetch vectors
        int s1 = ReadVec (vm, vm.Reg2 ().getIntVal (), v1),
                s2 = ReadVec (vm, vm.Reg  ().getIntVal (), v2);
        if (s1 < 0 || s2 < 0)
            return;

        // Calculate result
        float result[] = new float [4];
        VecPlus (v1, v2, result);

        // Return as temporary vector
        vm.Reg().setIntVal(Data.FillTempRealArray(vm.Data(), vm.DataTypes(), Math.max(s1, s2), result));
    }}
    public final class OpVecMinusVec implements Function { public void run(TomVM vm){

        // Fetch vectors
        int s1 = ReadVec (vm, vm.Reg2 ().getIntVal (), v1),
                s2 = ReadVec (vm, vm.Reg  ().getIntVal (), v2);
        if (s1 < 0 || s2 < 0)
            return;

        // Calculate result
        float result[] = new float[4];
        VecMinus(v1, v2, result);

        // Return as temporary vector
        vm.Reg ().setIntVal ( Data.FillTempRealArray(vm.Data(), vm.DataTypes(), Math.max(s1, s2), result));
    }}
    public final class OpMatrixPlusMatrix implements Function { public void run(TomVM vm){

        // Matrix + Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!ReadMatrix (vm, vm.Reg2 ().getIntVal (), m1)
                || !ReadMatrix (vm, vm.Reg  ().getIntVal (), m2))
            return;

        // Add them
        float result[] = new float[16];
        MatrixPlus(m1, m2, result);

        // Return as temporary matrix
        vm.Reg ().setIntVal ( Data.FillTempRealArray2D(vm.Data(), vm.DataTypes(), 4, 4, Arrays.asList(result)));
    }}
    public final class OpMatrixMinusMatrix implements Function { public void run(TomVM vm){

        // Matrix - Matrix
        // Left matrix at reg2, right matrix at reg1
        if (!ReadMatrix (vm, vm.Reg2 ().getIntVal(), m1)
                || !ReadMatrix (vm, vm.Reg  ().getIntVal (), m2))
            return;

        // Add them
        float result[] = new float [16];
        MatrixMinus(m1, m2, result);

        // Return as temporary matrix
        vm.Reg ().setIntVal (Data.FillTempRealArray2D (vm.Data (), vm.DataTypes (), 4, 4, Arrays.asList(result)));
    }}
    public final class OpNegVec implements Function { public void run(TomVM vm)      { DoScaleVec(vm, -1, vm.Reg().getIntVal()); }}
    public final class OpNegMatrix  implements Function { public void run(TomVM vm){ DoScaleMatrix (vm, -1, vm.Reg ().getIntVal ()); }}

    // Indices
    int scaleVec, scaleVec2, scaleMatrix, scaleMatrix2, matrixVec, matrixMatrix,
            divVec, divMatrix, vecVec, vecPlusVec, vecMinusVec,
            matrixPlusMatrix, matrixMinusMatrix, negVec, negMatrix;

    // Compiler callback
    public final class TrigUnOperatorExtension implements UnOperExt {

        public boolean run (  Mutable<ValType> regType,     // IN: Current type in register.                                                        OUT: Required type cast before calling function
                                    short oper,          // IN: Operator being applied; OpCode
                                    Mutable<Integer> operFunction,      // OUT: Index of VM_CALL_OPERATOR_FUNC function to call
                                    Mutable<ValType> resultType,  // OUT: Resulting value type
                                    Mutable<Boolean> freeTempData) {   // OUT: Set to true if temp data needs to be freed

        // Must be real values, and not pointers (references are OK however)
        if (regType.get().VirtualPointerLevel() > 0 || regType.get().m_basicType != ValType.VTP_REAL)
            return false;

        if (oper == OpCode.OP_OP_NEG) {
            if (regType.get().m_arrayLevel == 1) {                // -Vector
                operFunction.set(negVec);
                resultType.get().Set(regType.get());
                freeTempData.set(true);
                return true;
            }
            if (regType.get().m_arrayLevel == 2) {                // -Matrix
                operFunction.set(negMatrix);
                resultType.get().Set(regType.get());
                freeTempData.set(true);
                return true;
            }
        }

        return false;
    }
    }
    public final class TrigBinOperatorExtension implements BinOperExt {

        public boolean run(Mutable<ValType> regType,     // IN: Current type in register.                                                        OUT: Required type cast before calling function
                           Mutable<ValType> reg2Type,    // IN: Current type in second register (operation is reg2 OP reg1, e.g reg2 + reg1):    OUT: Required type cast before calling function
                           short oper,          // IN: Operator being applied
                           Mutable<Integer> operFunction,      // OUT: Index of VM_CALL_OPERATOR_FUNC function to call
                           Mutable<ValType> resultType,  // OUT: Resulting value type
                           Mutable<Boolean> freeTempData) {   // OUT: Set to true if temp data needs to be freed

            // Pointers not accepted (references are OK however)
            if (regType.get().VirtualPointerLevel() > 0 || reg2Type.get().VirtualPointerLevel() > 0)
                return false;

            // Validate data types. We will only work with ints and reals
            if (regType.get().m_basicType != ValType.VTP_REAL && regType.get().m_basicType != ValType.VTP_INT)
                return false;
            if (reg2Type.get().m_basicType != ValType.VTP_REAL && reg2Type.get().m_basicType != ValType.VTP_INT)
                return false;

            // Is acceptible to have an integer value, but must be typecast to a real
            // Arrays of integers not acceptible
            if (regType.get().m_basicType == ValType.VTP_INT) {
                if (regType.get().IsBasic()) regType.get().m_basicType = ValType.VTP_REAL;
                else return false;
            }
            if (reg2Type.get().m_basicType == ValType.VTP_INT) {
                if (reg2Type.get().IsBasic()) reg2Type.get().m_basicType = ValType.VTP_REAL;
                else return false;
            }

            // Look for recognised combinations
            if (oper == OpCode.OP_OP_TIMES) {
                if (regType.get().m_arrayLevel == 0 && reg2Type.get().m_arrayLevel == 1) {          // Vector * scalar
                    operFunction.set(scaleVec);
                    resultType.get().Set(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().m_arrayLevel == 1 && reg2Type.get().m_arrayLevel == 0) {     // Scalar * vector
                    operFunction.set(scaleVec2);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                }
                if (regType.get().m_arrayLevel == 0 && reg2Type.get().m_arrayLevel == 2) {          // Vector * scalar
                    operFunction.set(scaleMatrix);
                    resultType.get().Set(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().m_arrayLevel == 2 && reg2Type.get().m_arrayLevel == 0) {     // Scalar * vector
                    operFunction.set(scaleMatrix2);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (reg2Type.get().m_arrayLevel == 2 && regType.get().m_arrayLevel == 1) {     // Matrix * vector
                    operFunction.set(matrixVec);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (reg2Type.get().m_arrayLevel == 2 && regType.get().m_arrayLevel == 2) {     // Matrix * matrix
                    operFunction.set(matrixMatrix);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().m_arrayLevel == 1 && reg2Type.get().m_arrayLevel == 1) {     // Vec * Vec (Dot product)
                    operFunction.set(vecVec);
                    resultType.get().Set(ValType.VTP_REAL);
                    freeTempData.set(false);
                    return true;
                }
                return false;
            } else if (oper == OpCode.OP_OP_DIV) {
                if (regType.get().m_arrayLevel == 0 && reg2Type.get().m_arrayLevel == 1) {          // Vector / scalar
                    operFunction.set(divVec);
                    resultType.get().Set(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                }
                if (regType.get().m_arrayLevel == 0 && reg2Type.get().m_arrayLevel == 2) {          // Matrix / scalar
                    operFunction.set(divMatrix);
                    resultType.get().Set(reg2Type.get());
                    freeTempData.set(true);
                    return true;
                }
            } else if (oper == OpCode.OP_OP_PLUS) {
                if (regType.get().m_arrayLevel == 1 && reg2Type.get().m_arrayLevel == 1) {          // Vector + vector
                    operFunction.set(vecPlusVec);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().m_arrayLevel == 2 && reg2Type.get().m_arrayLevel == 2) {     // Matrix + matrix
                    operFunction.set(matrixPlusMatrix);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                }
            } else if (oper == OpCode.OP_OP_MINUS) {
                if (regType.get().m_arrayLevel == 1 && reg2Type.get().m_arrayLevel == 1) {          // Vector - vector
                    operFunction.set(vecMinusVec);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                } else if (regType.get().m_arrayLevel == 2 && reg2Type.get().m_arrayLevel == 2) {     // Matrix - matrix
                    operFunction.set(matrixMinusMatrix);
                    resultType.get().Set(regType.get());
                    freeTempData.set(true);
                    return true;
                }
            }
            return false;
        }
    }
}
