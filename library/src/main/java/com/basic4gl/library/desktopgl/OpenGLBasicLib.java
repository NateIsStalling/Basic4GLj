package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL13.*;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.IServiceCollection;
import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.PointerResourceStore;
import java.nio.*;
import java.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/**
 * Created by Nate on 11/3/2015.
 */
public class OpenGLBasicLib implements FunctionLibrary, IGLRenderer {
    // ImageResourceStore
    //
    // Stores pointers to Corona image objects
    // typedef vmPointerResourceStore<corona.Image> ImageResourceStore;

    private static final int MAXIMAGESIZE = (2048 * 2048);
    // Globals
    private static GLWindow appWindow;
    private static TextureResourceStore textures;
    private static PointerResourceStore<Image> images; // ImageResourceStore
    private static DisplayListResourceStore displayLists;

    // Global state
    private static boolean truncateBlankFrames; // Truncate blank frames from image strips
    private static boolean usingTransparentCol; // Use transparent colour when loading images
    // unsigned long int
    private static long transparentCol; // Transparent colour as RGB triplet
    private static boolean doMipmap; // Build mipmap textures when loading images
    private static boolean doLinearFilter; // Use linear filtering on textures (otherwise use nearest)

    private ByteBuffer byteBuffer16;
    private IntBuffer intBuffer16;
    private FloatBuffer floatBuffer16;
    private DoubleBuffer doubleBuffer16;

    public static GLWindow getAppWindow() {
        return appWindow;
    }

    @Override
    public String name() {
        return "OpenGLBasicLib";
    }

    @Override
    public String description() {
        return "OpenGL 1.* constants and image loading functions";
    }

    @Override
    public void setWindow(GLWindow window) {
        OpenGLBasicLib.appWindow = window;
    }

    @Override
    public void setTextGrid(GLTextGrid text) {
        // This library doesn't use the text grid
    }

    @Override
    public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {

        appWindow.clearKeyBuffers();

        if (textures == null) {
            textures = new TextureResourceStore();
        }
        if (images == null) {
            images = new PointerResourceStore<>();
        }
        if (displayLists == null) {
            displayLists = new DisplayListResourceStore();
        }

        textures.clear();
        images.clear();
        displayLists.clear();

        byteBuffer16 = BufferUtils.createByteBuffer(16);
        intBuffer16 = BufferUtils.createIntBuffer(16);
        floatBuffer16 = BufferUtils.createFloatBuffer(16);
        doubleBuffer16 = BufferUtils.createDoubleBuffer(16);

        // Set state behaviour defaults
        truncateBlankFrames = true;
        usingTransparentCol = false;
        doMipmap = true;
        doLinearFilter = true;
    }

    @Override
    public void init(TomBasicCompiler comp, IServiceCollection services) {
        if (textures == null) {
            textures = new TextureResourceStore();
        }
        if (images == null) {
            images = new PointerResourceStore<>();
        }
        if (displayLists == null) {
            displayLists = new DisplayListResourceStore();
        }
    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<>();

        c.put("GL_ACTIVE_TEXTURE", new Constant(GL_ACTIVE_TEXTURE));
        c.put("GL_CLIENT_ACTIVE_TEXTURE", new Constant(GL_CLIENT_ACTIVE_TEXTURE));
        c.put("GL_MAX_TEXTURE_UNITS", new Constant(GL_MAX_TEXTURE_UNITS));
        c.put("GL_TEXTURE0", new Constant(GL_TEXTURE0));
        c.put("GL_TEXTURE1", new Constant(GL_TEXTURE1));
        c.put("GL_TEXTURE2", new Constant(GL_TEXTURE2));
        c.put("GL_TEXTURE3", new Constant(GL_TEXTURE3));
        c.put("GL_TEXTURE4", new Constant(GL_TEXTURE4));
        c.put("GL_TEXTURE5", new Constant(GL_TEXTURE5));
        c.put("GL_TEXTURE6", new Constant(GL_TEXTURE6));
        c.put("GL_TEXTURE7", new Constant(GL_TEXTURE7));
        c.put("GL_TEXTURE8", new Constant(GL_TEXTURE8));
        c.put("GL_TEXTURE9", new Constant(GL_TEXTURE9));
        c.put("GL_TEXTURE10", new Constant(GL_TEXTURE10));
        c.put("GL_TEXTURE11", new Constant(GL_TEXTURE11));
        c.put("GL_TEXTURE12", new Constant(GL_TEXTURE12));
        c.put("GL_TEXTURE13", new Constant(GL_TEXTURE13));
        c.put("GL_TEXTURE14", new Constant(GL_TEXTURE14));
        c.put("GL_TEXTURE15", new Constant(GL_TEXTURE15));
        c.put("GL_TEXTURE16", new Constant(GL_TEXTURE16));
        c.put("GL_TEXTURE17", new Constant(GL_TEXTURE17));
        c.put("GL_TEXTURE18", new Constant(GL_TEXTURE18));
        c.put("GL_TEXTURE19", new Constant(GL_TEXTURE19));
        c.put("GL_TEXTURE20", new Constant(GL_TEXTURE20));
        c.put("GL_TEXTURE21", new Constant(GL_TEXTURE21));
        c.put("GL_TEXTURE22", new Constant(GL_TEXTURE22));
        c.put("GL_TEXTURE23", new Constant(GL_TEXTURE23));
        c.put("GL_TEXTURE24", new Constant(GL_TEXTURE24));
        c.put("GL_TEXTURE25", new Constant(GL_TEXTURE25));
        c.put("GL_TEXTURE26", new Constant(GL_TEXTURE26));
        c.put("GL_TEXTURE27", new Constant(GL_TEXTURE27));
        c.put("GL_TEXTURE28", new Constant(GL_TEXTURE28));
        c.put("GL_TEXTURE29", new Constant(GL_TEXTURE29));
        c.put("GL_TEXTURE30", new Constant(GL_TEXTURE30));
        c.put("GL_TEXTURE31", new Constant(GL_TEXTURE31));
        c.put("GL_ACTIVE_TEXTURE_ARB", new Constant(ARBMultitexture.GL_ACTIVE_TEXTURE_ARB));
        c.put("GL_CLIENT_ACTIVE_TEXTURE_ARB", new Constant(ARBMultitexture.GL_CLIENT_ACTIVE_TEXTURE_ARB));
        c.put("GL_MAX_TEXTURE_UNITS_ARB", new Constant(ARBMultitexture.GL_MAX_TEXTURE_UNITS_ARB));
        c.put("GL_TEXTURE0_ARB", new Constant(ARBMultitexture.GL_TEXTURE0_ARB));
        c.put("GL_TEXTURE1_ARB", new Constant(ARBMultitexture.GL_TEXTURE1_ARB));
        c.put("GL_TEXTURE2_ARB", new Constant(ARBMultitexture.GL_TEXTURE2_ARB));
        c.put("GL_TEXTURE3_ARB", new Constant(ARBMultitexture.GL_TEXTURE3_ARB));
        c.put("GL_TEXTURE4_ARB", new Constant(ARBMultitexture.GL_TEXTURE4_ARB));
        c.put("GL_TEXTURE5_ARB", new Constant(ARBMultitexture.GL_TEXTURE5_ARB));
        c.put("GL_TEXTURE6_ARB", new Constant(ARBMultitexture.GL_TEXTURE6_ARB));
        c.put("GL_TEXTURE7_ARB", new Constant(ARBMultitexture.GL_TEXTURE7_ARB));
        c.put("GL_TEXTURE8_ARB", new Constant(ARBMultitexture.GL_TEXTURE8_ARB));
        c.put("GL_TEXTURE9_ARB", new Constant(ARBMultitexture.GL_TEXTURE9_ARB));
        c.put("GL_TEXTURE10_ARB", new Constant(ARBMultitexture.GL_TEXTURE10_ARB));
        c.put("GL_TEXTURE11_ARB", new Constant(ARBMultitexture.GL_TEXTURE11_ARB));
        c.put("GL_TEXTURE12_ARB", new Constant(ARBMultitexture.GL_TEXTURE12_ARB));
        c.put("GL_TEXTURE13_ARB", new Constant(ARBMultitexture.GL_TEXTURE13_ARB));
        c.put("GL_TEXTURE14_ARB", new Constant(ARBMultitexture.GL_TEXTURE14_ARB));
        c.put("GL_TEXTURE15_ARB", new Constant(ARBMultitexture.GL_TEXTURE15_ARB));
        c.put("GL_TEXTURE16_ARB", new Constant(ARBMultitexture.GL_TEXTURE16_ARB));
        c.put("GL_TEXTURE17_ARB", new Constant(ARBMultitexture.GL_TEXTURE17_ARB));
        c.put("GL_TEXTURE18_ARB", new Constant(ARBMultitexture.GL_TEXTURE18_ARB));
        c.put("GL_TEXTURE19_ARB", new Constant(ARBMultitexture.GL_TEXTURE19_ARB));
        c.put("GL_TEXTURE20_ARB", new Constant(ARBMultitexture.GL_TEXTURE20_ARB));
        c.put("GL_TEXTURE21_ARB", new Constant(ARBMultitexture.GL_TEXTURE21_ARB));
        c.put("GL_TEXTURE22_ARB", new Constant(ARBMultitexture.GL_TEXTURE22_ARB));
        c.put("GL_TEXTURE23_ARB", new Constant(ARBMultitexture.GL_TEXTURE23_ARB));
        c.put("GL_TEXTURE24_ARB", new Constant(ARBMultitexture.GL_TEXTURE24_ARB));
        c.put("GL_TEXTURE25_ARB", new Constant(ARBMultitexture.GL_TEXTURE25_ARB));
        c.put("GL_TEXTURE26_ARB", new Constant(ARBMultitexture.GL_TEXTURE26_ARB));
        c.put("GL_TEXTURE27_ARB", new Constant(ARBMultitexture.GL_TEXTURE27_ARB));
        c.put("GL_TEXTURE28_ARB", new Constant(ARBMultitexture.GL_TEXTURE28_ARB));
        c.put("GL_TEXTURE29_ARB", new Constant(ARBMultitexture.GL_TEXTURE29_ARB));
        c.put("GL_TEXTURE30_ARB", new Constant(ARBMultitexture.GL_TEXTURE30_ARB));
        c.put("GL_TEXTURE31_ARB", new Constant(ARBMultitexture.GL_TEXTURE31_ARB));

        c.put("GL_COMBINE", new Constant(GL13.GL_COMBINE));
        c.put("GL_COMBINE_RGB", new Constant(GL13.GL_COMBINE_RGB));
        c.put("GL_COMBINE_ALPHA", new Constant(GL13.GL_COMBINE_ALPHA));
        c.put("GL_RGB_SCALE", new Constant(GL13.GL_RGB_SCALE));
        c.put("GL_ADD_SIGNED", new Constant(GL13.GL_ADD_SIGNED));
        c.put("GL_INTERPOLATE", new Constant(GL13.GL_INTERPOLATE));
        c.put("GL_CONSTANT", new Constant(GL13.GL_CONSTANT));
        c.put("GL_PRIMARY_COLOR", new Constant(GL13.GL_PRIMARY_COLOR));
        c.put("GL_PREVIOUS", new Constant(GL13.GL_PREVIOUS));
        c.put("GL_SOURCE0_RGB", new Constant(GL13.GL_SOURCE0_RGB));
        c.put("GL_SOURCE1_RGB", new Constant(GL13.GL_SOURCE1_RGB));
        c.put("GL_SOURCE2_RGB", new Constant(GL13.GL_SOURCE2_RGB));
        c.put("GL_SOURCE0_ALPHA", new Constant(GL13.GL_SOURCE0_ALPHA));
        c.put("GL_SOURCE1_ALPHA", new Constant(GL13.GL_SOURCE1_ALPHA));
        c.put("GL_SOURCE2_ALPHA", new Constant(GL13.GL_SOURCE2_ALPHA));
        c.put("GL_OPERAND0_RGB", new Constant(GL13.GL_OPERAND0_RGB));
        c.put("GL_OPERAND1_RGB", new Constant(GL13.GL_OPERAND1_RGB));
        c.put("GL_OPERAND2_RGB", new Constant(GL13.GL_OPERAND2_RGB));
        c.put("GL_OPERAND0_ALPHA", new Constant(GL13.GL_OPERAND0_ALPHA));
        c.put("GL_OPERAND1_ALPHA", new Constant(GL13.GL_OPERAND1_ALPHA));
        c.put("GL_OPERAND2_ALPHA", new Constant(GL13.GL_OPERAND2_ALPHA));
        c.put("GL_COMBINE_EXT", new Constant(GL_COMBINE));
        c.put("GL_COMBINE_RGB_EXT", new Constant(GL_COMBINE_RGB));
        c.put("GL_COMBINE_ALPHA_EXT", new Constant(GL_COMBINE_ALPHA));
        c.put("GL_RGB_SCALE_EXT", new Constant(GL_RGB_SCALE));
        c.put("GL_ADD_SIGNED_EXT", new Constant(GL_ADD_SIGNED));
        c.put("GL_INTERPOLATE_EXT", new Constant(GL_INTERPOLATE));
        c.put("GL_CONSTANT_EXT", new Constant(GL_CONSTANT));
        c.put("GL_PRIMARY_COLOR_EXT", new Constant(GL_PRIMARY_COLOR));
        c.put("GL_PREVIOUS_EXT", new Constant(GL_PREVIOUS));
        c.put("GL_SOURCE0_RGB_EXT", new Constant(GL_SOURCE0_RGB));
        c.put("GL_SOURCE1_RGB_EXT", new Constant(GL_SOURCE1_RGB));
        c.put("GL_SOURCE2_RGB_EXT", new Constant(GL_SOURCE2_RGB));
        c.put("GL_SOURCE0_ALPHA_EXT", new Constant(GL_SOURCE0_ALPHA));
        c.put("GL_SOURCE1_ALPHA_EXT", new Constant(GL_SOURCE1_ALPHA));
        c.put("GL_SOURCE2_ALPHA_EXT", new Constant(GL_SOURCE2_ALPHA));
        c.put("GL_OPERAND0_RGB_EXT", new Constant(GL_OPERAND0_RGB));
        c.put("GL_OPERAND1_RGB_EXT", new Constant(GL_OPERAND1_RGB));
        c.put("GL_OPERAND2_RGB_EXT", new Constant(GL_OPERAND2_RGB));
        c.put("GL_OPERAND0_ALPHA_EXT", new Constant(GL_OPERAND0_ALPHA));
        c.put("GL_OPERAND1_ALPHA_EXT", new Constant(GL_OPERAND1_ALPHA));
        c.put("GL_OPERAND2_ALPHA_EXT", new Constant(GL_OPERAND2_ALPHA));
        return c;
    }

    @Override
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<>();
        s.put("loadimage", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapLoadImage.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("deleteimage", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapDeleteImage.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("imagewidth", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapImageWidth.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("imageheight", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapImageHeight.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("imageformat", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapImageFormat.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("imagedatatype", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapImageDataType.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("loadtexture", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapLoadTexture.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("loadmipmaptexture", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapLoadMipmapTexture.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glgentexture", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGenTexture.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)
        });
        s.put("gldeletetexture", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglDeleteTexture.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glteximage2d", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglTexImage2D.class,
                    new ParamTypeList(
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapglTexImage2D_2.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapglTexImage2D_3.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapglTexImage2D_4.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapglTexImage2D_5.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("gltexsubimage2d", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglTexSubImage2D.class,
                    new ParamTypeList(
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glubuild2dmipmaps", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapgluBuild2DMipmaps.class,
                    new ParamTypeList(
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT,
                            BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapgluBuild2DMipmaps_2.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapgluBuild2DMipmaps_3.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapgluBuild2DMipmaps_4.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapgluBuild2DMipmaps_5.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glgetString", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetString.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_STRING,
                    false,
                    false,
                    null)
        });
        s.put("extensionsupported", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapExtensionSupported.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glmultitexcoord2f", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglMultiTexCoord2f.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glmultitexcoord2d", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglMultiTexCoord2d.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_REAL, BasicValType.VTP_REAL),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glactivetexture", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglActiveTexture.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("maxtextureunits", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapMaxTextureUnits.class,
                    new ParamTypeList(),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("windowwidth", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWindowWidth.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)
        });
        s.put("windowheight", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapWindowHeight.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)
        });
        s.put("glgentextures", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGenTextures.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("gldeletetextures", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglDeleteTextures.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glLoadMatrixd", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglLoadMatrixd.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glLoadMatrixf", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglLoadMatrixf.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glMultMatrixd", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglMultMatrixd.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glMultMatrixf", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglMultMatrixf.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glgetpolygonstipple", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetPolygonStipple.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glpolygonstipple", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglPolygonStipple.class,
                    new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glgenlists", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGenLists.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("gldeletelists", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglDeleteLists.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glcalllists", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglCallLists.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapglCallLists_2.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glBegin", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglBegin.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glEnd", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglEnd.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, false, false, null)
        });

        s.put("imagestripframes", new FunctionSpecification[] {
            new FunctionSpecification(
                    OldSquare_WrapImageStripFrames.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    OldSquare_WrapImageStripFrames_2.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    WrapImageStripFrames.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("loadimagestrip", new FunctionSpecification[] {
            new FunctionSpecification(
                    OldSquare_WrapLoadImageStrip.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    OldSquare_WrapLoadImageStrip_2.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    WrapLoadImageStrip.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null)
        });
        s.put("loadmipmapimagestrip", new FunctionSpecification[] {
            new FunctionSpecification(
                    OldSquare_WrapLoadMipmapImageStrip.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    OldSquare_WrapLoadMipmapImageStrip_2.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    WrapLoadMipmapImageStrip.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null)
        });

        s.put("glGetFloatv", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetFloatv_2D.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glGetDoublev", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetDoublev_2D.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glGetIntegerv", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetIntegerv_2D.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("glGetBooleanv", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapglGetBooleanv_2D.class,
                    new ParamTypeList(
                            new ValType(BasicValType.VTP_INT),
                            new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true)),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });

        // New texture loading
        s.put("LoadTex", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapLoadTex.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("LoadTexStrip", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapLoadTexStrip.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    WrapLoadTexStrip2.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    true,
                    false,
                    null)
        });
        s.put("TexStripFrames", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapTexStripFrames.class,
                    new ParamTypeList(BasicValType.VTP_STRING),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null),
            new FunctionSpecification(
                    WrapTexStripFrames2.class,
                    new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    true,
                    BasicValType.VTP_INT,
                    true,
                    false,
                    null)
        });
        s.put("SetTexIgnoreBlankFrames", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSetTexIgnoreBlankFrames.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("SetTexTransparentCol", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSetTexTransparentCol.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null),
            new FunctionSpecification(
                    WrapSetTexTransparentCol2.class,
                    new ParamTypeList(BasicValType.VTP_INT, BasicValType.VTP_INT, BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("SetTexNoTransparentCol", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSetTexNoTransparentCol.class,
                    new ParamTypeList(),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("SetTexMipmap", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSetTexMipmap.class,
                    new ParamTypeList(BasicValType.VTP_INT),
                    true,
                    false,
                    BasicValType.VTP_INT,
                    false,
                    false,
                    null)
        });
        s.put("SetTexLinearFilter", new FunctionSpecification[] {
            new FunctionSpecification(
                    WrapSetTexLinearFilter.class,
                    new ParamTypeList(BasicValType.VTP_INT),
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

    // region New image strip loading routines
    static void calculateImageStripFrames(
            Image image, int frameWidth, int frameHeight, IntBuffer frames, IntBuffer width, IntBuffer height) {

        // Return the # of frames in the image
        assertTrue(image != null);
        int w = image.getWidth(), h = image.getHeight();
        if (frames != null) {
            frames.put(0, (w / frameWidth) * (h / frameHeight));
        }
        if (width != null) {
            width.put(0, w);
        }
        if (height != null) {
            height.put(0, h);
        }
    }

    static boolean checkFrameSize(int frameSize) {
        return frameSize >= 1 && frameSize <= 1024 && OpenGLBasicLib.isPowerOf2(frameSize);
    }

    static int imageStripFrames(TomVM vm, String filename, int frameWidth, int frameHeight) {

        if (!checkFrameSize(frameWidth)) {
            vm.functionError("Frame width must be a power of 2 from 1-1024");
            return 0;
        }
        if (!checkFrameSize(frameHeight)) {
            vm.functionError("Frame height must be a power of 2 from 1-1024");
            return 0;
        }

        Image image = LoadImage.loadImage(filename);
        if (image != null) {
            IntBuffer result = BufferUtils.createIntBuffer(0);
            OpenGLBasicLib.calculateImageStripFrames(image, frameWidth, frameHeight, result, null, null);

            return result.get(0);
        }
        return 0;
    }

    static void loadImageStrip(TomVM vm, String filename, int frameWidth, int frameHeight, boolean mipmap) {

        if (!OpenGLBasicLib.checkFrameSize(frameWidth)) {
            vm.functionError("Frame width must be a power of 2 from 1-1024");
            return;
        }
        if (!OpenGLBasicLib.checkFrameSize(frameHeight)) {
            vm.functionError("Frame height must be a power of 2 from 1-1024");
            return;
        }

        // Load image strip
        Image image = LoadImage.loadImage(filename);
        if (image != null) {
            IntBuffer frameCount = BufferUtils.createIntBuffer(1),
                    width = BufferUtils.createIntBuffer(1),
                    height = BufferUtils.createIntBuffer(1);
            OpenGLBasicLib.calculateImageStripFrames(image, frameWidth, frameHeight, frameCount, width, height);
            if (frameCount.get(0) > 65536) {
                vm.functionError("Cannot load more than 65536 images in an image strip");
                return;
            }
            if (frameCount.get(0) > 0) {

                // Generate some OpenGL textures
                ByteBuffer tex = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE * 65536);
                IntBuffer texIntBuffer = tex.asIntBuffer();
                texIntBuffer.limit(frameCount.get(0));

                glGenTextures(texIntBuffer);

                // Store texture handles in texture store object so Basic4GL can track them
                for (int i = 0; i < frameCount.get(0); i++) {
                    textures.addHandle(tex.asIntBuffer().get(i));
                }

                // Iterate over image in grid pattern, extracting each frame
                int frame = 0;
                ByteBuffer buffer = BufferUtils.createByteBuffer(frameWidth * frameHeight * 4);
                int bytesPerPixel = image.getBPP(); // corona.PF_R8G8B8 ? 3 : 4;
                int format = LoadImage.getImageFormat(image);
                for (int y = 0; y < height.get(0) / frameHeight; y++) {
                    for (int x = 0; x < width.get(0) / frameWidth; x++) {

                        // Extract block of pixels
                        copyPixels(
                                image.getPixels(),
                                image.getWidth(),
                                image.getHeight(),
                                x * frameWidth,
                                height.get(0) - (y + 1) * frameHeight,
                                buffer,
                                frameWidth,
                                frameHeight,
                                bytesPerPixel);

                        // Upload texture
                        glBindTexture(GL_TEXTURE_2D, tex.get(frame));
                        if (mipmap) {
                            // GLU deprecated
                            /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
                            bytesPerPixel,
                            frameWidth,
                            frameHeight,
                            format,
                            GL_UNSIGNED_BYTE,
                            buffer);*/
                            GL11.glTexImage2D(
                                    GL11.GL_TEXTURE_2D,
                                    0,
                                    image.getFormat(),
                                    frameWidth,
                                    frameHeight,
                                    0,
                                    format,
                                    GL11.GL_UNSIGNED_BYTE,
                                    buffer);
                            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                        } else {
                            glTexImage2D(
                                    GL_TEXTURE_2D,
                                    0,
                                    image.getFormat(),
                                    frameWidth,
                                    frameHeight,
                                    0,
                                    format,
                                    GL_UNSIGNED_BYTE,
                                    buffer);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                        }
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

                        // Increase frame counter
                        frame++;
                    }
                }
                int[] t = new int[tex.asIntBuffer().capacity()];
                tex.asIntBuffer().get(t);
                // Return array of textures
                vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), frameCount.get(0), t));
                return;
            }
        }

        // Load failed.
        // Return 1 element array containing a 0.
        int blankFrame = 0;
        vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), 1, new int[] {blankFrame}));
    }

    static int uploadTexture(Image image) {
        assertTrue(image != null);

        // Generate texture
        int texture;
        ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
        glGenTextures(buffer.asIntBuffer());
        glBindTexture(GL_TEXTURE_2D, texture = buffer.asIntBuffer().get(0));

        int width = image.getWidth();
        int height = image.getHeight();
        int pixelSize = image.getBPP(); // GetPixelSize(image.getFormat());
        int format = LoadImage.getImageFormat(image);
        ByteBuffer pixels = image.getPixels();

        if (doMipmap) {
            // Set filtering for mipmap
            if (doLinearFilter) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            }

            // Build mipmaps
            // GLU deprecated
            /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
            pixelSize,
            width,
            height,
            format,
            GL_UNSIGNED_BYTE,
            pixels);*/
            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D, 0, image.getFormat(), width, height, 0, format, GL11.GL_UNSIGNED_BYTE, pixels);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } else {
            // Set filtering for texture
            if (doLinearFilter) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            }

            // Upload texture
            glTexImage2D(GL_TEXTURE_2D, 0, image.getFormat(), width, height, 0, format, GL_UNSIGNED_BYTE, pixels);
        }

        return texture;
    }

    static int loadTex(String filename) {

        // Load image
        Image image = LoadImage.loadImage(filename);
        if (image != null) {

            // Process image
            if (usingTransparentCol) {
                image = LoadImage.applyTransparentColor(image, transparentCol);
            }
            // TODO Confirm texture dimensions are powers of 2
            // image = LoadImage.ResizeImageForOpenGL(image);

            // Upload into texture
            int texture = OpenGLBasicLib.uploadTexture(image);

            return texture;
        } else {
            return 0;
        }
    }

    static String getFileExt(String filename) {
        String ext = "";
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            ext = filename.substring(i + 1);
        }
        return ext.toLowerCase();
    }

    static boolean isPowerOf2(int value) {
        if (value <= 0) {
            return false;
        }
        while ((value & 1) == 0) {
            value >>= 1;
        }
        return value == 1;
    }

    static void copyPixels(
            ByteBuffer src, // Source image
            int srcWidth, // Image size
            int srcHeight,
            int srcX, // Offset in image
            int srcY,
            ByteBuffer dst, // Destination image
            int dstWidth, // And size
            int dstHeight,
            int bytesPerPixel) { // (Source and dest)
        assertTrue(src != null);
        assertTrue(dst != null);
        assertTrue(srcWidth >= srcX + dstWidth);
        assertTrue(srcHeight >= srcY + dstHeight);

        // Copy image data line by line
        int y;
        for (y = 0; y < dstHeight; y++) {
            dst.position(y * dstWidth * bytesPerPixel);
            src.position(((y + srcY) * srcWidth + srcX) * bytesPerPixel);
            for (int x = 0; x < dstWidth * bytesPerPixel; x++) {
                dst.put(src.get());
            }
        }
        // dst.rewind();
    }

    static Vector<Image> loadTexStripImages(String filename, int frameXSize, int frameYSize) {

        // Load main image
        Image image = LoadImage.loadImage(filename);
        if (image != null) {

            // Split into frames
            Vector<Image> images = LoadImage.splitImageStrip(image, frameXSize, frameYSize);

            // Process images
            if (usingTransparentCol) {
                for (int i = 0; i < images.size(); i++) {
                    Image it = images.get(i);
                    images.set(i, LoadImage.applyTransparentColor(it, transparentCol));
                }
            }

            if (truncateBlankFrames) {
                while (images.size() > 1 && LoadImage.isImageBlank(images.lastElement())) {
                    images.remove(images.lastElement());
                }
            }

            return images;
        } else {
            return new Vector<>();
        }
    }

    static void deleteImages(Vector<Image> images) {
        for (Image i : images) {
            i.getPixels().clear().limit(0);
        }
        images.clear();
    }

    static int getTexStripFrames(String filename) {
        return OpenGLBasicLib.getTexStripFrames(filename, 0, 0);
    }

    static int getTexStripFrames(String filename, int frameXSize, int frameYSize) {

        // Load image
        Vector<Image> images = loadTexStripImages(filename, frameXSize, frameYSize);

        // Count frames
        int result = images.size();

        deleteImages(images);
        return result;
    }

    static Vector<Integer> loadTexStrip(String filename) {
        return loadTexStrip(filename, 0, 0);
    }

    static Vector<Integer> loadTexStrip(String filename, int frameXSize) {
        return loadTexStrip(filename, frameXSize, 0);
    }

    static Vector<Integer> loadTexStrip(String filename, int frameXSize, int frameYSize) {

        // Load images
        Vector<Image> images = loadTexStripImages(filename, frameXSize, frameYSize);

        // Upload into textures
        Vector<Integer> textures = new Vector<>();
        for (int i = 0; i < images.size(); i++) {
            // TODO Confirm texture dimensions are powers of 2
            // images.set(i, ResizeImageForOpenGL(images.get(i)));
            textures.add(uploadTexture(images.get(i)));
        }

        deleteImages(images);
        return textures;
    }

    static int loadTexture(String filename, boolean mipmap) {

        // Load texture
        int result = 0;
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        // Generate and load image
        Image image = LoadImage.loadImage(filename);
        if (image != null) {
            // TODO Confirm texture dimensions are powers of 2
            // image = ResizeImageForOpenGL (image);

            // Generate texture
            int texture;
            ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
            glGenTextures(buffer.asIntBuffer());
            texture = buffer.asIntBuffer().get(0);
            glBindTexture(GL_TEXTURE_2D, texture);

            // Build mipmaps
            if (mipmap) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                // GLU deprecated
                /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
                (image.getFormat () & 0xffff) == 3 ? 3 : 4, //corona.PF_R8G8B8 ? 3 : 4,
                image.getWidth (),
                image.getHeight (),
                LoadImage.ImageFormat(image),
                GL_UNSIGNED_BYTE,
                image.getPixels ());*/
                GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        0,
                        image.getFormat(),
                        image.getWidth(),
                        image.getHeight(),
                        0,
                        LoadImage.getImageFormat(image),
                        GL11.GL_UNSIGNED_BYTE,
                        image.getPixels());
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        image.getFormat(), // corona.PF_R8G8B8 ? 3 : 4,
                        image.getWidth(),
                        image.getHeight(),
                        0,
                        LoadImage.getImageFormat(image),
                        GL_UNSIGNED_BYTE,
                        image.getPixels());
            }

            // Store and return texture
            textures.addHandle(texture);
            result = texture;
        }

        // Clean up OpenGL state
        glPopAttrib();
        return result;
    }

    static void internalWrapglTexImage2D(TomVM vm, ValType elementType, int dimensions, boolean mipmap) {
        // Find array param, and extract dimensions
        int arrayOffset = vm.getIntParam(1);
        int maxSize;
        if (dimensions == 2) {
            int xSize = Data.getArrayDimensionSize(vm.getData(), arrayOffset, 0),
                    ySize = Data.getArrayDimensionSize(vm.getData(), arrayOffset, 1);

            // Verify size
            if (xSize <= 0 || ySize <= 0) {
                vm.functionError("Bad array size");
                return;
            }
            if (ySize > (OpenGLBasicLib.MAXIMAGESIZE / xSize)) {
                vm.functionError("Cannot upload arrays greater than 2048 x 2048 into images");
                return;
            }
            maxSize = xSize * ySize;
        } else {
            assertTrue(dimensions == 1);
            int size = Data.getArrayDimensionSize(vm.getData(), arrayOffset, 0);
            if (size <= 0) {
                vm.functionError("Bad array size");
                return;
            }
            if (size > OpenGLBasicLib.MAXIMAGESIZE) {
                vm.functionError("Cannot upload arrays greater than 2048 x 2048 into images");
                return;
            }
            maxSize = size;
        }

        // Read data, converted requested type
        int type = vm.getIntParam(2);
        ByteBuffer data = BufferUtils.createByteBuffer(maxSize * 4);
        Routines.readArrayDynamic(
                vm, 1, new ValType(elementType.basicType, (byte) dimensions, (byte) 1, true), type, data, maxSize);

        data.rewind();
        // Generate image
        if (mipmap) {
            // GLU deprecated
            /*
            gluBuild2DMipmaps (     vm.GetIntParam (7),
            		vm.GetIntParam (6),
            		vm.GetIntParam (5),
            		vm.GetIntParam (4),
            		vm.GetIntParam (3),
            		type,
            		data);*/
            GL11.glTexImage2D(
                    vm.getIntParam(7),
                    0,
                    vm.getIntParam(6),
                    vm.getIntParam(5),
                    vm.getIntParam(4),
                    0,
                    vm.getIntParam(3),
                    type,
                    data);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } else {
            glTexImage2D(
                    vm.getIntParam(9),
                    vm.getIntParam(8),
                    vm.getIntParam(7),
                    vm.getIntParam(6),
                    vm.getIntParam(5),
                    vm.getIntParam(4),
                    vm.getIntParam(3),
                    type,
                    data);
        }
    }

    // endregion

    // region Old square image strip routines.
    // These ones take only a single frame size param and assume the frame is square.
    // I'm not too sure of the logic behind these original versions, but I'm keeping
    // them for backwards compatibility.
    void calculateOldSquareImageStripFrames(
            Image image,
            IntBuffer frameSize, // Input/Output
            IntBuffer frames, // Output
            IntBuffer width, // "
            IntBuffer height) { // "

        // Return the # of frames in the currently bound image
        // Also adjusts the framesize if appropriate
        assertTrue(image != null);

        // Get image dimensions
        width.put(0, image.getWidth());
        height.put(0, image.getHeight());

        // Calculate frame size (each frame is square, so this represents the width
        // AND height of each frame.)
        int size = frameSize.get(0);
        while (size > image.getWidth()) {
            size >>= 1;
        }
        while (size > image.getHeight()) {
            size >>= 1;
        }
        frameSize.put(0, size);
        frames.put(0, (width.get(0) / size) * (height.get(0) / size));
    }

    int getOldSquareImageStripFrames(TomVM vm, String filename, IntBuffer frameSize) {

        // Image size must be power of 2
        final int size = frameSize.get(0);
        if (size < 1 || size > 1024 || !isPowerOf2(size)) {
            vm.functionError("Frame size must be a power of 2 from 1-1024");
            return 0;
        }

        // Calculate and return # of frames in image strip
        IntBuffer result = BufferUtils.createIntBuffer(1),
                width = BufferUtils.createIntBuffer(1),
                height = BufferUtils.createIntBuffer(1);

        Image image = LoadImage.loadImage(filename);
        if (image != null) {
            calculateOldSquareImageStripFrames(image, frameSize, result, width, height);
        }
        return result.get(0);
    }

    void loadOldSquareImageStrip(TomVM vm, String filename, IntBuffer frameSize, boolean mipmap) {

        // Image size must be power of 2
        int size = frameSize.get(0);
        if (size < 1 || size > 1024 || !isPowerOf2(size)) {
            vm.functionError("Frame size must be a power of 2 from 1-1024");
            return;
        }

        // Load image strip
        Image image = LoadImage.loadImage(filename);
        if (image != null) {
            IntBuffer frameCount = BufferUtils.createIntBuffer(1),
                    width = BufferUtils.createIntBuffer(1),
                    height = BufferUtils.createIntBuffer(1);
            calculateOldSquareImageStripFrames(image, frameSize, frameCount, width, height);
            int count = frameCount.get(0);
            size = frameSize.get(0);
            if (count > 65536) {
                vm.functionError("Cannot load more than 65536 images in an image strip");
                return;
            }
            if (count > 0) {

                // Generate some OpenGL textures
                ByteBuffer tex = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE * 65536);
                IntBuffer texbuffer = tex.asIntBuffer();
                texbuffer.limit(count);
                glGenTextures(texbuffer);
                // Store texture handles in texture store object so Basic4GL can track them
                for (int i = 0; i < count; i++) {
                    textures.addHandle(texbuffer.get(i));
                }

                // Iterate over image in grid pattern, extracting each frame
                int frame = 0;
                ByteBuffer buffer = BufferUtils.createByteBuffer(
                        size * size * image.getBPP()); // TODO see if 4 should be used instead of image.getBPP () here
                int bytesPerPixel = image.getBPP();
                int format = image.getFormat();
                for (int y = 0; y < height.get(0) / size; y++) {
                    for (int x = 0; x < width.get(0) / size; x++) {

                        // Extract block of pixels
                        copyPixels(
                                image.getPixels(),
                                image.getWidth(),
                                image.getHeight(),
                                x * size,
                                height.get(0) - (y + 1) * size,
                                buffer,
                                size,
                                size,
                                bytesPerPixel);
                        buffer.rewind();

                        // Upload texture
                        glBindTexture(GL_TEXTURE_2D, tex.asIntBuffer().get(frame));
                        if (mipmap) {
                            // GLU deprecated
                            /*
                            gluBuild2DMipmaps ( GL_TEXTURE_2D,
                            		bytesPerPixel,
                            		frameSize,
                            		frameSize,
                            		format,
                            		GL_UNSIGNED_BYTE,
                            		buffer);
                            */
                            GL11.glTexImage2D(
                                    GL11.GL_TEXTURE_2D,
                                    0,
                                    format,
                                    frameSize.get(0),
                                    frameSize.get(0),
                                    0,
                                    format,
                                    GL11.GL_UNSIGNED_BYTE,
                                    buffer);
                            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                        } else {
                            glTexImage2D(GL_TEXTURE_2D, 0, format, size, size, 0, format, GL_UNSIGNED_BYTE, buffer);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                        }
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

                        // Increase frame counter
                        frame++;
                    }
                }
                tex.rewind();
                int[] t = new int[tex.asIntBuffer().capacity()];
                tex.asIntBuffer().get(t);
                // Return array of textures
                vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), frameCount.get(0), t));
                return;
            }
        }

        // Load failed.
        // Return 1 element array containing a 0.
        int blankFrame = 0;
        vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), 1, new int[] {blankFrame}));
    }

    // endregion

    public static final class WrapLoadTex implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            int texture = OpenGLBasicLib.loadTex(vm.getStringParam(1));
            OpenGLBasicLib.textures.addHandle(texture);
            vm.getReg().setIntVal(texture);
            glPopAttrib();
        }
    }

    public static final class WrapLoadTexStrip implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            Vector<Integer> texs = OpenGLBasicLib.loadTexStrip(vm.getStringParam(1));

            // Convert to array and return
            if (!texs.isEmpty()) {
                int[] array = new int[texs.size()];
                for (int i = 0; i < texs.size(); i++) {
                    array[i] = texs.get(i);
                    OpenGLBasicLib.textures.addHandle(texs.get(i));
                }
                vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), texs.size(), array));
            } else {
                int[] array = new int[1];
                array[0] = 0;
                vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), 1, array));
            }
            glPopAttrib();
        }
    }

    public static final class WrapLoadTexStrip2 implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            Vector<Integer> texs =
                    OpenGLBasicLib.loadTexStrip(vm.getStringParam(3), vm.getIntParam(2), vm.getIntParam(1));

            // Convert to array and return
            if (!texs.isEmpty()) {
                Integer[] array = new Integer[texs.size()];
                for (int i = 0; i < texs.size(); i++) {
                    array[i] = texs.get(i);
                    OpenGLBasicLib.textures.addHandle(texs.get(i));
                }
                vm.getReg()
                        .setIntVal(Data.fillTempIntArray(
                                vm.getData(), vm.getDataTypes(), texs.size(), Arrays.asList(array)));
            } else {
                Integer[] array = new Integer[1];
                array[0] = 0;
                vm.getReg().setIntVal(Data.fillTempIntArray(vm.getData(), vm.getDataTypes(), 1, Arrays.asList(array)));
            }
            glPopAttrib();
        }
    }

    public static final class WrapTexStripFrames implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(OpenGLBasicLib.getTexStripFrames(vm.getStringParam(1)));
        }
    }

    public static final class WrapTexStripFrames2 implements Function {
        public void run(TomVM vm) {
            vm.getReg()
                    .setIntVal(OpenGLBasicLib.getTexStripFrames(
                            vm.getStringParam(3), vm.getIntParam(2), vm.getIntParam(1)));
        }
    }

    public static final class WrapSetTexIgnoreBlankFrames implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.truncateBlankFrames = vm.getIntParam(1) != 0;
        }
    }

    public static final class WrapSetTexTransparentCol implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.transparentCol = vm.getIntParam(1);
            OpenGLBasicLib.usingTransparentCol = true;
        }
    }

    public static final class WrapSetTexTransparentCol2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.transparentCol =
                    (vm.getIntParam(3) & 0xff) | ((vm.getIntParam(2) & 0xff) << 8) | ((vm.getIntParam(1) & 0xff) << 16);
            OpenGLBasicLib.usingTransparentCol = true;
        }
    }

    public static final class WrapSetTexNoTransparentCol implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.usingTransparentCol = false;
        }
    }

    public static final class WrapSetTexMipmap implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.doMipmap = vm.getIntParam(1) != 0;
        }
    }

    public static final class WrapSetTexLinearFilter implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.doLinearFilter = vm.getIntParam(1) != 0;
        }
    }

    public static final class WrapglGenTexture implements Function {
        public void run(TomVM vm) {
            int texture;
            ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
            glGenTextures(buffer.asIntBuffer());
            texture = buffer.asIntBuffer().get(0);
            OpenGLBasicLib.textures.addHandle(texture);
            vm.getReg().setIntVal(texture);
        }
    }

    public static final class WrapglDeleteTexture implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.textures.freeHandle(vm.getIntParam(1));
        }
    }

    public static final class WrapLoadTexture implements Function {
        public void run(TomVM vm) {

            // Load and return non-mipmapped texture
            vm.getReg().setIntVal(OpenGLBasicLib.loadTexture(vm.getStringParam(1), false));
        }
    }

    public static final class WrapLoadMipmapTexture implements Function {
        public void run(TomVM vm) {

            // Load and return mipmapped texture
            vm.getReg().setIntVal(OpenGLBasicLib.loadTexture(vm.getStringParam(1), true));
        }
    }

    public static final class WrapLoadImage implements Function {
        public void run(TomVM vm) {

            // Attempt to load image
            Image image = LoadImage.loadImage(vm.getStringParam(1));

            // If successful, store it and return handle
            vm.getReg().setIntVal((image != null) ? OpenGLBasicLib.images.alloc(image) : 0);
        }
    }

    public static final class WrapDeleteImage implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.images.free(vm.getIntParam(1));
        }
    }

    public static final class WrapglTexImage2D implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.getIntParam(1);
            if (OpenGLBasicLib.images.isIndexStored(index)) {
                ByteBuffer pixels = images.getValueAt(index).getPixels();

                // Generate image
                glTexImage2D(
                        vm.getIntParam(9), // target
                        vm.getIntParam(8), // level
                        vm.getIntParam(7), // components
                        vm.getIntParam(6), // width
                        vm.getIntParam(5), // height
                        vm.getIntParam(4), // border
                        vm.getIntParam(3), // format
                        vm.getIntParam(2), // type
                        pixels); // pixels
            }
        }
    }

    public static final class WrapglTexImage2D_2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_INT), 1, false);
        }
    }

    public static final class WrapglTexImage2D_3 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_REAL), 1, false);
        }
    }

    public static final class WrapglTexImage2D_4 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_INT), 2, false);
        }
    }

    public static final class WrapglTexImage2D_5 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_REAL), 2, false);
        }
    }

    public static final class WrapgluBuild2DMipmaps_2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_INT), 1, true);
        }
    }

    public static final class WrapgluBuild2DMipmaps_3 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_REAL), 1, true);
        }
    }

    public static final class WrapgluBuild2DMipmaps_4 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_INT), 2, true);
        }
    }

    public static final class WrapgluBuild2DMipmaps_5 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.internalWrapglTexImage2D(vm, new ValType(BasicValType.VTP_REAL), 2, true);
        }
    }

    public static final class WrapglTexSubImage2D implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.getIntParam(1);
            if (OpenGLBasicLib.images.isIndexStored(index)) {
                ByteBuffer pixels = OpenGLBasicLib.images.getValueAt(index).getPixels();

                // Generate image
                glTexSubImage2D(
                        vm.getIntParam(9), // target
                        vm.getIntParam(8), // level
                        vm.getIntParam(7), // xoffset
                        vm.getIntParam(6), // y offset
                        vm.getIntParam(5), // width
                        vm.getIntParam(4), // height
                        vm.getIntParam(3), // format
                        vm.getIntParam(2), // type
                        pixels); // pixels
            }
        }
    }

    public static final class WrapImageWidth implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(1);
            vm.getReg()
                    .setIntVal(
                            OpenGLBasicLib.images.isIndexStored(index)
                                    ? OpenGLBasicLib.images.getValueAt(index).getWidth()
                                    : 0);
        }
    }

    public static final class WrapImageHeight implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(1);
            vm.getReg()
                    .setIntVal(
                            OpenGLBasicLib.images.isIndexStored(index)
                                    ? OpenGLBasicLib.images.getValueAt(index).getHeight()
                                    : 0);
        }
    }

    public static final class WrapImageFormat implements Function {
        public void run(TomVM vm) {
            int index = vm.getIntParam(1);
            vm.getReg()
                    .setIntVal(
                            OpenGLBasicLib.images.isIndexStored(index)
                                    ? LoadImage.getImageFormat(OpenGLBasicLib.images.getValueAt(index))
                                    : 0);
        }
    }

    public static final class WrapImageDataType implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(GL_UNSIGNED_BYTE); // Images always stored as unsigned bytes
        }
    }

    public static final class WrapgluBuild2DMipmaps implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.getIntParam(1);
            if (OpenGLBasicLib.images.isIndexStored(index)) {
                ByteBuffer pixels = OpenGLBasicLib.images.getValueAt(index).getPixels();

                // Build 2D mipmaps
                // GLU deprecated
                /*gluBuild2DMipmaps ( vm.GetIntParam (7),         // target
                		vm.GetIntParam (6),         // components
                		vm.GetIntParam (5),         // width
                		vm.GetIntParam (4),         // height
                		vm.GetIntParam (3),         // format
                		vm.GetIntParam (2),         // type
                		pixels);                    // pixels
                */
                GL11.glTexImage2D(
                        vm.getIntParam(7),
                        0,
                        vm.getIntParam(6),
                        vm.getIntParam(5),
                        vm.getIntParam(4),
                        0,
                        vm.getIntParam(3),
                        vm.getIntParam(2),
                        pixels);
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
        }
    }

    public static final class WrapglMultiTexCoord2f implements Function {
        public void run(TomVM vm) {
            glMultiTexCoord2f(vm.getIntParam(3), vm.getRealParam(2), vm.getRealParam(1));
        }
    }

    public static final class WrapglMultiTexCoord2d implements Function {
        public void run(TomVM vm) {
            glMultiTexCoord2d(vm.getIntParam(3), vm.getRealParam(2), vm.getRealParam(1));
        }
    }

    public static final class WrapglActiveTexture implements Function {
        public void run(TomVM vm) {
            glActiveTexture(vm.getIntParam(1));
        }
    }

    public static final class WrapglGetString implements Function {
        public void run(TomVM vm) {
            vm.setRegString(glGetString(vm.getIntParam(1)));
        }
    }

    public static final class WrapExtensionSupported implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(OpenGLBasicLib.appWindow.isExtensionSupported(vm.getStringParam(1)) ? 1 : 0);
        }
    }

    public static final class WrapMaxTextureUnits implements Function {
        public void run(TomVM vm) {
            IntBuffer units = BufferUtils.createIntBuffer(1);
            glGetIntegerv(ARBMultitexture.GL_MAX_TEXTURE_UNITS_ARB, units);
            vm.getReg().setIntVal(units.get(0));
        }
    }

    public static final class WrapWindowWidth implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(OpenGLBasicLib.appWindow.getWidth());
        }
    }

    public static final class WrapWindowHeight implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal(OpenGLBasicLib.appWindow.getHeight());
        }
    }

    public static final class WrapglGenTextures implements Function {
        public void run(TomVM vm) {
            int count = vm.getIntParam(2);
            if (count > 65536) {
                vm.functionError("Count must be 0 - 65536 (Basic4GL restriction)");
                return;
            }
            if (count <= 0) {
                return;
            }

            // Generate some texture handles
            ByteBuffer handles = BufferUtils.createByteBuffer(
                    (Integer.SIZE / Byte.SIZE) * 65536); // 64K should be enough for anybody ;)
            IntBuffer handleBuffer = handles.asIntBuffer();

            handleBuffer.limit(count);
            glGenTextures(handleBuffer);

            // Store textures in resource store (so Basic4GL can track them and ensure they have been
            // deallocated)
            for (int i = 0; i < count; i++) {
                OpenGLBasicLib.textures.addHandle(handleBuffer.get(i));
            }

            // Store handles in Basic4GL array
            int[] t = new int[count];
            handleBuffer.rewind();
            handleBuffer.get(t);
            Data.writeArray(vm.getData(), vm.getIntParam(1), new ValType(BasicValType.VTP_INT, (byte) 1), t, count);
        }
    }

    public static final class WrapglDeleteTextures implements Function {
        public void run(TomVM vm) {
            int count = vm.getIntParam(2);
            if (count > 65536) {
                vm.functionError("Count must be 0 - 65536 (Basic4GL restriction)");
                return;
            }
            if (count <= 0) {
                return;
            }

            // Read texture handles
            ByteBuffer handles = BufferUtils.createByteBuffer(
                    (Integer.SIZE / Byte.SIZE) * 65536); // 64K should be enough for anybody ;)
            int[] array = new int[65536];
            Data.readAndZero(
                    vm.getData(), vm.getIntParam(1), new ValType(BasicValType.VTP_INT, (byte) 1), array, count);

            IntBuffer handlesIntBuffer = handles.asIntBuffer();
            handlesIntBuffer.put(array);

            // Delete texture handles
            handlesIntBuffer.limit(count);
            glDeleteTextures(handlesIntBuffer);
        }
    }

    public final class WrapglLoadMatrixd implements Function {
        public void run(TomVM vm) {
            double[] a = new double[16];
            Data.readAndZero(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
            doubleBuffer16.rewind();
            doubleBuffer16.put(a);
            doubleBuffer16.rewind();
            glLoadMatrixd(doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(a);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
        }
    }

    public final class WrapglLoadMatrixf implements Function {
        public void run(TomVM vm) {
            float[] a = new float[16];
            Data.readAndZero(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
            floatBuffer16.rewind();
            floatBuffer16.put(a);
            floatBuffer16.rewind();
            glLoadMatrixf(floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(a);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
        }
    }

    public final class WrapglMultMatrixd implements Function {
        public void run(TomVM vm) {
            double[] a = new double[16];
            Data.readAndZero(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
            doubleBuffer16.rewind();
            doubleBuffer16.put(a);
            doubleBuffer16.rewind();
            glMultMatrixd(doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(a);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
        }
    }

    public final class WrapglMultMatrixf implements Function {
        public void run(TomVM vm) {
            float[] a = new float[16];
            Data.readAndZero(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
            floatBuffer16.rewind();
            floatBuffer16.put(a);
            floatBuffer16.rewind();
            glMultMatrixf(floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(a);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    a,
                    16);
        }
    }

    public static final class WrapglGetPolygonStipple implements Function {
        public void run(TomVM vm) {
            ByteBuffer mask = ByteBuffer.wrap(new byte[128]).order(ByteOrder.nativeOrder());
            glGetPolygonStipple(mask);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    mask.array(),
                    128);
        }
    }

    public static final class WrapglPolygonStipple implements Function {
        public void run(TomVM vm) {
            ByteBuffer mask = ByteBuffer.wrap(new byte[128]).order(ByteOrder.nativeOrder());
            Data.readAndZero(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
                    mask.array(),
                    128);
            glPolygonStipple(mask);
        }
    }

    public static final class WrapglGenLists implements Function {
        public void run(TomVM vm) {

            // Validate params
            int count = vm.getIntParam(1);

            // Get display lists
            int base = glGenLists(count);

            // Track display lists, so Basic4GL can delete them if necessary
            if (base != GL_INVALID_VALUE || base != GL_INVALID_OPERATION) {
                OpenGLBasicLib.displayLists.store(base, count);
            }

            // Return result
            vm.getReg().setIntVal(base);
        }
    }

    public static final class WrapglDeleteLists implements Function {
        public void run(TomVM vm) {

            // Get params
            int base = vm.getIntParam(2), count = vm.getIntParam(1);

            // Delete display lists
            glDeleteLists(base, count);

            // Remove display lists entry (if the range was correctly deleted)
            if (OpenGLBasicLib.displayLists.isHandleValid(base)
                    && OpenGLBasicLib.displayLists.getCount(base) <= count) {
                OpenGLBasicLib.displayLists.removeHandle(base);
            }
        }
    }

    public static final class WrapglCallLists implements Function {
        public void run(TomVM vm) {
            throw new RuntimeException("Not Implemented!");
            //
            //            // VTP_REAL array version
            //
            //            // Get size and type params
            //            int n = vm.GetIntParam(3), type = vm.GetIntParam(2);
            //            if (n > 65536) {
            //                vm.FunctionError("Count must be 0 - 65536 (Basic4GL restriction)");
            //                return;
            //            }
            //            if (n <= 0)
            //                return;
            //
            //            // Get array parameter
            //            ByteBuffer array = BufferUtils.createByteBuffer(65536 * 4);
            //            if (Routines.ReadArrayDynamic(vm, 1, new ValType (BasicValType.VTP_REAL, (byte)
            // 1, (byte) 1, true), type, array, n) == 0)
            //                return;
            //
            //            // Call glCallLists
            //            glCallLists(n, type, array);
        }
    }

    public static final class WrapglCallLists_2 implements Function {
        public void run(TomVM vm) {
            throw new RuntimeException("Not Implemented!");
            //
            //            // VTP_INT array version
            //
            //            // Get size and type params
            //            int n = vm.GetIntParam(3), type = vm.GetIntParam(2);
            //            if (n > 65536) {
            //                vm.FunctionError("Count must be 0 - 65536 (Basic4GL restriction)");
            //                return;
            //            }
            //            if (n <= 0)
            //                return;
            //
            //            // Get array parameter
            //            ByteBuffer array = BufferUtils.createByteBuffer(65536 * 4);
            //            if (Routines.ReadArrayDynamic(vm, 1, new ValType (BasicValType.VTP_INT, (byte)
            // 1, (byte) 1, true), type, array, n) == 0)
            //                return;
            //
            //            // Call glCallLists
            //            glCallLists(n, type, array);
        }
    }

    public static final class WrapglBegin implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.appWindow.setDontPaint(
                    true); // Dont paint on WM_PAINT messages when between a glBegin() and a glEnd ()
            glBegin(vm.getIntParam(
                    1)); // This doesn't effect running code, but helps when stepping through calls in the
            // debugger
        }
    }

    public static final class WrapglEnd implements Function {
        public void run(TomVM vm) {
            glEnd();
            OpenGLBasicLib.appWindow.setDontPaint(false);
        }
    }

    public final class WrapglGetFloatv_2D implements Function {
        public void run(TomVM vm) {
            float[] data = new float[16];
            Arrays.fill(data, 0);
            floatBuffer16.rewind();
            floatBuffer16.put(data);
            floatBuffer16.rewind();
            glGetFloatv(vm.getIntParam(2), floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(data);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    data,
                    16);
        }
    }

    public final class WrapglGetDoublev_2D implements Function {
        public void run(TomVM vm) {
            double[] data = new double[16];
            Arrays.fill(data, 0);
            doubleBuffer16.rewind();
            doubleBuffer16.put(data);
            doubleBuffer16.rewind();
            glGetDoublev(vm.getIntParam(2), doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(data);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_REAL, (byte) 2, (byte) 1, true),
                    data,
                    16);
        }
    }

    public final class WrapglGetIntegerv_2D implements Function {
        public void run(TomVM vm) {
            int[] data = new int[16];
            Arrays.fill(data, 0);
            intBuffer16.rewind();
            intBuffer16.put(data);
            intBuffer16.rewind();
            glGetIntegerv(vm.getIntParam(2), intBuffer16);
            intBuffer16.rewind();
            intBuffer16.get(data);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_INT, (byte) 2, (byte) 1, true),
                    data,
                    16);
        }
    }

    public final class WrapglGetBooleanv_2D implements Function {
        public void run(TomVM vm) {
            byte[] data = new byte[16];
            Arrays.fill(data, (byte) 0);
            byteBuffer16.rewind();
            byteBuffer16.put(data);
            byteBuffer16.rewind();
            glGetBooleanv(vm.getIntParam(2), byteBuffer16);
            byteBuffer16.rewind();
            byteBuffer16.get(data);
            Data.writeArray(
                    vm.getData(),
                    vm.getIntParam(1),
                    new ValType(BasicValType.VTP_INT, (byte) 2, (byte) 1, true),
                    data,
                    16);
        }
    }

    public static final class WrapLoadImageStrip implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.loadImageStrip(vm, vm.getStringParam(3), vm.getIntParam(2), vm.getIntParam(1), false);
        }
    }

    public static final class WrapLoadMipmapImageStrip implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.loadImageStrip(vm, vm.getStringParam(3), vm.getIntParam(2), vm.getIntParam(1), true);
        }
    }

    public static final class WrapImageStripFrames implements Function {
        public void run(TomVM vm) {
            vm.getReg()
                    .setIntVal(OpenGLBasicLib.imageStripFrames(
                            vm, vm.getStringParam(3), vm.getIntParam(2), vm.getIntParam(1)));
        }
    }

    public final class OldSquare_WrapLoadImageStrip implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            loadOldSquareImageStrip(vm, vm.getStringParam(1), frameSize, false);
        }
    }

    public final class OldSquare_WrapLoadImageStrip_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.getIntParam(1));
            loadOldSquareImageStrip(vm, vm.getStringParam(2), frameSize, false);
        }
    }

    public final class OldSquare_WrapLoadMipmapImageStrip implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            loadOldSquareImageStrip(vm, vm.getStringParam(1), frameSize, true);
        }
    }

    public final class OldSquare_WrapLoadMipmapImageStrip_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.getIntParam(1));
            loadOldSquareImageStrip(vm, vm.getStringParam(2), frameSize, true);
        }
    }

    public final class OldSquare_WrapImageStripFrames implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            vm.getReg().setIntVal(getOldSquareImageStripFrames(vm, vm.getStringParam(1), frameSize));
        }
    }

    public final class OldSquare_WrapImageStripFrames_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.getIntParam(1));
            vm.getReg().setIntVal(getOldSquareImageStripFrames(vm, vm.getStringParam(2), frameSize));
        }
    }
}
