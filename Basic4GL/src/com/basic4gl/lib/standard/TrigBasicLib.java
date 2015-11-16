package com.basic4gl.lib.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.Library;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;

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
    }
    @Override
    public void init(TomBasicCompiler comp){

    }
    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        return null;
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
}
