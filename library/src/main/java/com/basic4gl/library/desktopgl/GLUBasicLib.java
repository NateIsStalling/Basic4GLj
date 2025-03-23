package com.basic4gl.library.desktopgl;

import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glOrtho;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.IServiceCollection;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 11/5/2015.
 */
public class GLUBasicLib implements FunctionLibrary {
    @Override
    public String name() {
        return "GLUBasicLib";
    }

    @Override
    public String description() {
        return "GLU functions (deprecated)";
    }

    @Override
    public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {}

    @Override
    public void init(TomBasicCompiler comp, IServiceCollection services) {}

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<>();
        /*c.put("GLU_VERSION_1_1", new Constant(GLU_VERSION_1_1));
        c.put("GLU_VERSION_1_2", new Constant(GLU_VERSION_1_2));
        c.put("GLU_INVALID_ENUM", new Constant(GLU_INVALID_ENUM));
        c.put("GLU_INVALID_VALUE", new Constant(GLU_INVALID_VALUE));
        c.put("GLU_OUT_OF_MEMORY", new Constant(GLU_OUT_OF_MEMORY));
        c.put("GLU_INCOMPATIBLE_GL_VERSION", new Constant(GLU_INCOMPATIBLE_GL_VERSION));
        c.put("GLU_VERSION", new Constant(GLU_VERSION));
        c.put("GLU_EXTENSIONS", new Constant(GLU_EXTENSIONS));
        c.put("GLU_TRUE", new Constant(GLU_TRUE));
        c.put("GLU_FALSE", new Constant(GLU_FALSE));
        c.put("GLU_SMOOTH", new Constant(GLU_SMOOTH));
        c.put("GLU_FLAT", new Constant(GLU_FLAT));
        c.put("GLU_NONE", new Constant(GLU_NONE));
        c.put("GLU_POINT", new Constant(GLU_POINT));
        c.put("GLU_LINE", new Constant(GLU_LINE));
        c.put("GLU_FILL", new Constant(GLU_FILL));
        c.put("GLU_SILHOUETTE", new Constant(GLU_SILHOUETTE));
        c.put("GLU_OUTSIDE", new Constant(GLU_OUTSIDE));
        c.put("GLU_INSIDE", new Constant(GLU_INSIDE));
        c.put("GLU_TESS_WINDING_RULE", new Constant(GLU_TESS_WINDING_RULE));
        c.put("GLU_TESS_BOUNDARY_ONLY", new Constant(GLU_TESS_BOUNDARY_ONLY));
        c.put("GLU_TESS_TOLERANCE", new Constant(GLU_TESS_TOLERANCE));
        c.put("GLU_TESS_WINDING_ODD", new Constant(GLU_TESS_WINDING_ODD));
        c.put("GLU_TESS_WINDING_NONZERO", new Constant(GLU_TESS_WINDING_NONZERO));
        c.put("GLU_TESS_WINDING_POSITIVE", new Constant(GLU_TESS_WINDING_POSITIVE));
        c.put("GLU_TESS_WINDING_NEGATIVE", new Constant(GLU_TESS_WINDING_NEGATIVE));
        c.put("GLU_TESS_WINDING_ABS_GEQ_TWO", new Constant(GLU_TESS_WINDING_ABS_GEQ_TWO));
        c.put("GLU_TESS_BEGIN", new Constant(GLU_TESS_BEGIN));
        c.put("GLU_TESS_VERTEX", new Constant(GLU_TESS_VERTEX));
        c.put("GLU_TESS_END", new Constant(GLU_TESS_END));
        c.put("GLU_TESS_ERROR", new Constant(GLU_TESS_ERROR));
        c.put("GLU_TESS_EDGE_FLAG", new Constant(GLU_TESS_EDGE_FLAG));
        c.put("GLU_TESS_COMBINE", new Constant(GLU_TESS_COMBINE));
        c.put("GLU_TESS_BEGIN_DATA", new Constant(GLU_TESS_BEGIN_DATA));
        c.put("GLU_TESS_VERTEX_DATA", new Constant(GLU_TESS_VERTEX_DATA));
        c.put("GLU_TESS_END_DATA", new Constant(GLU_TESS_END_DATA));
        c.put("GLU_TESS_ERROR_DATA", new Constant(GLU_TESS_ERROR_DATA));
        c.put("GLU_TESS_EDGE_FLAG_DATA", new Constant(GLU_TESS_EDGE_FLAG_DATA));
        c.put("GLU_TESS_COMBINE_DATA", new Constant(GLU_TESS_COMBINE_DATA));
        c.put("GLU_TESS_ERROR1", new Constant(GLU_TESS_ERROR1));
        c.put("GLU_TESS_ERROR2", new Constant(GLU_TESS_ERROR2));
        c.put("GLU_TESS_ERROR3", new Constant(GLU_TESS_ERROR3));
        c.put("GLU_TESS_ERROR4", new Constant(GLU_TESS_ERROR4));
        c.put("GLU_TESS_ERROR5", new Constant(GLU_TESS_ERROR5));
        c.put("GLU_TESS_ERROR6", new Constant(GLU_TESS_ERROR6));
        c.put("GLU_TESS_ERROR7", new Constant(GLU_TESS_ERROR7));
        c.put("GLU_TESS_ERROR8", new Constant(GLU_TESS_ERROR8));
        c.put("GLU_TESS_MISSING_BEGIN_POLYGON", new Constant(GLU_TESS_MISSING_BEGIN_POLYGON));
        c.put("GLU_TESS_MISSING_BEGIN_CONTOUR", new Constant(GLU_TESS_MISSING_BEGIN_CONTOUR));
        c.put("GLU_TESS_MISSING_END_POLYGON", new Constant(GLU_TESS_MISSING_END_POLYGON));
        c.put("GLU_TESS_MISSING_END_CONTOUR", new Constant(GLU_TESS_MISSING_END_CONTOUR));
        c.put("GLU_TESS_COORD_TOO_LARGE", new Constant(GLU_TESS_COORD_TOO_LARGE));
        c.put("GLU_TESS_NEED_COMBINE_CALLBACK", new Constant(GLU_TESS_NEED_COMBINE_CALLBACK));
        c.put("GLU_AUTO_LOAD_MATRIX", new Constant(GLU_AUTO_LOAD_MATRIX));
        c.put("GLU_CULLING", new Constant(GLU_CULLING));
        c.put("GLU_SAMPLING_TOLERANCE", new Constant(GLU_SAMPLING_TOLERANCE));
        c.put("GLU_DISPLAY_MODE", new Constant(GLU_DISPLAY_MODE));
        c.put("GLU_PARAMETRIC_TOLERANCE", new Constant(GLU_PARAMETRIC_TOLERANCE));
        c.put("GLU_SAMPLING_METHOD", new Constant(GLU_SAMPLING_METHOD));
        c.put("GLU_U_STEP", new Constant(GLU_U_STEP));
        c.put("GLU_V_STEP", new Constant(GLU_V_STEP));
        c.put("GLU_PATH_LENGTH", new Constant(GLU_PATH_LENGTH));
        c.put("GLU_PARAMETRIC_ERROR", new Constant(GLU_PARAMETRIC_ERROR));
        c.put("GLU_DOMAIN_DISTANCE", new Constant(GLU_DOMAIN_DISTANCE));
        c.put("GLU_MAP1_TRIM_2", new Constant(GLU_MAP1_TRIM_2));
        c.put("GLU_MAP1_TRIM_3", new Constant(GLU_MAP1_TRIM_3));
        c.put("GLU_OUTLINE_POLYGON", new Constant(GLU_OUTLINE_POLYGON));
        c.put("GLU_OUTLINE_PATCH", new Constant(GLU_OUTLINE_PATCH));
        c.put("GLU_NURBS_ERROR1", new Constant(GLU_NURBS_ERROR1));
        c.put("GLU_NURBS_ERROR2", new Constant(GLU_NURBS_ERROR2));
        c.put("GLU_NURBS_ERROR3", new Constant(GLU_NURBS_ERROR3));
        c.put("GLU_NURBS_ERROR4", new Constant(GLU_NURBS_ERROR4));
        c.put("GLU_NURBS_ERROR5", new Constant(GLU_NURBS_ERROR5));
        c.put("GLU_NURBS_ERROR6", new Constant(GLU_NURBS_ERROR6));
        c.put("GLU_NURBS_ERROR7", new Constant(GLU_NURBS_ERROR7));
        c.put("GLU_NURBS_ERROR8", new Constant(GLU_NURBS_ERROR8));
        c.put("GLU_NURBS_ERROR9", new Constant(GLU_NURBS_ERROR9));
        c.put("GLU_NURBS_ERROR10", new Constant(GLU_NURBS_ERROR10));
        c.put("GLU_NURBS_ERROR11", new Constant(GLU_NURBS_ERROR11));
        c.put("GLU_NURBS_ERROR12", new Constant(GLU_NURBS_ERROR12));
        c.put("GLU_NURBS_ERROR13", new Constant(GLU_NURBS_ERROR13));
        c.put("GLU_NURBS_ERROR14", new Constant(GLU_NURBS_ERROR14));
        c.put("GLU_NURBS_ERROR15", new Constant(GLU_NURBS_ERROR15));
        c.put("GLU_NURBS_ERROR16", new Constant(GLU_NURBS_ERROR16));
        c.put("GLU_NURBS_ERROR17", new Constant(GLU_NURBS_ERROR17));
        c.put("GLU_NURBS_ERROR18", new Constant(GLU_NURBS_ERROR18));
        c.put("GLU_NURBS_ERROR19", new Constant(GLU_NURBS_ERROR19));
        c.put("GLU_NURBS_ERROR20", new Constant(GLU_NURBS_ERROR20));
        c.put("GLU_NURBS_ERROR21", new Constant(GLU_NURBS_ERROR21));
        c.put("GLU_NURBS_ERROR22", new Constant(GLU_NURBS_ERROR22));
        c.put("GLU_NURBS_ERROR23", new Constant(GLU_NURBS_ERROR23));
        c.put("GLU_NURBS_ERROR24", new Constant(GLU_NURBS_ERROR24));
        c.put("GLU_NURBS_ERROR25", new Constant(GLU_NURBS_ERROR25));
        c.put("GLU_NURBS_ERROR26", new Constant(GLU_NURBS_ERROR26));
        c.put("GLU_NURBS_ERROR27", new Constant(GLU_NURBS_ERROR27));
        c.put("GLU_NURBS_ERROR28", new Constant(GLU_NURBS_ERROR28));
        c.put("GLU_NURBS_ERROR29", new Constant(GLU_NURBS_ERROR29));
        c.put("GLU_NURBS_ERROR30", new Constant(GLU_NURBS_ERROR30));
        c.put("GLU_NURBS_ERROR31", new Constant(GLU_NURBS_ERROR31));
        c.put("GLU_NURBS_ERROR32", new Constant(GLU_NURBS_ERROR32));
        c.put("GLU_NURBS_ERROR33", new Constant(GLU_NURBS_ERROR33));
        c.put("GLU_NURBS_ERROR34", new Constant(GLU_NURBS_ERROR34));
        c.put("GLU_NURBS_ERROR35", new Constant(GLU_NURBS_ERROR35));
        c.put("GLU_NURBS_ERROR36", new Constant(GLU_NURBS_ERROR36));
        c.put("GLU_NURBS_ERROR37", new Constant(GLU_NURBS_ERROR37));
        c.put("GLU_CW", new Constant(GLU_CW));
        c.put("GLU_CCW", new Constant(GLU_CCW));
        c.put("GLU_INTERIOR", new Constant(GLU_INTERIOR));
        c.put("GLU_EXTERIOR", new Constant(GLU_EXTERIOR));
        c.put("GLU_UNKNOWN", new Constant(GLU_UNKNOWN));
        c.put("GLU_BEGIN", new Constant(GLU_BEGIN));
        c.put("GLU_VERTEX", new Constant(GLU_VERTEX));
        c.put("GLU_END", new Constant(GLU_END));
        c.put("GLU_ERROR", new Constant(GLU_ERROR));
        c.put("GLU_EDGE_FLAG", new Constant(GLU_EDGE_FLAG));*/
        return c;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<>();
        s.put("gluOrtho2D", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapgluOrtho2D.class,
                    new ParamTypeList(
                            BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("gluPerspective", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapgluPerspective.class,
                    new ParamTypeList(
                            BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("gluLookAt", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapgluLookAt.class,
                    new ParamTypeList(
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL,
                            BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
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

    public static final class WrapgluOrtho2D implements Function {

        public void run(TomVM vm) {
            // gluOrtho2D();   //Replaced with glOrtho
            glOrtho(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1), -1, 1);
        }
    }

    public static final class WrapgluPerspective implements Function {

        public void run(TomVM vm) {
            // gluPerspective(); //Replaced with glFrustrum

            perspectiveGL(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
        }

        private void perspectiveGL(double fovY, double aspect, double zNear, double zFar) {
            double fW, fH;
            fH = Math.tan(fovY / 360.0 * Math.PI) * zNear;
            fW = fH * aspect;
            glFrustum(-fW, fW, -fH, fH, zNear, zFar);
        }
    }

    public static final class WrapgluLookAt implements Function {

        public void run(TomVM vm) {
            // TODO implement gluLookAt
            throw new UnsupportedOperationException();
            /*gluLookAt(vm.GetRealParam(9),
            		vm.GetRealParam(8),
            		vm.GetRealParam(7),
            		vm.GetRealParam(6),
            		vm.GetRealParam(5),
            		vm.GetRealParam(4),
            		vm.GetRealParam(3),
            		vm.GetRealParam(2),
            		vm.GetRealParam(1));
            */
        }
    }
}
