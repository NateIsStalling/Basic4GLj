package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.compiler.util.FuncSpec;
import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.IntHandleResources;
import com.basic4gl.runtime.util.PointerResourceStore;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.*;
import java.util.*;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.stb.STBImage.stbi_info;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;

/**
 * Created by Nate on 11/3/2015.
 */
public class OpenGLBasicLib implements FunctionLibrary, IGLRenderer {
    // ImageResourceStore
//
// Stores pointers to Corona image objects
    //typedef vmPointerResourceStore<corona.Image> ImageResourceStore;

    static final int MAXIMAGESIZE = (2048 * 2048);
    // Globals
    static GLWindow appWindow;
    static TextureResourceStore textures;
    static PointerResourceStore<Image> images; // ImageResourceStore
    static DisplayListResourceStore displayLists;

    // Global state
    static boolean truncateBlankFrames;           // Truncate blank frames from image strips
    static boolean usingTransparentCol;           // Use transparent colour when loading images
    //unsigned long int
    static long transparentCol;                    // Transparent colour as RGB triplet
    static boolean doMipmap;                      // Build mipmap textures when loading images
    static boolean doLinearFilter;                // Use linear filtering on textures (otherwise use nearest)

    ByteBuffer byteBuffer16;
    IntBuffer intBuffer16;
    FloatBuffer floatBuffer16;
    DoubleBuffer doubleBuffer16;

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
        //This library doesn't use the text grid
    }

    @Override
    public void init(TomVM vm) {

        appWindow.ClearKeyBuffers();


        if (textures == null)
            textures = new TextureResourceStore();
        if (images == null)
            images = new PointerResourceStore<>();
        if (displayLists == null)
            displayLists = new DisplayListResourceStore();

        textures.Clear();
        images.Clear();
        displayLists.Clear();

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
    public void init(TomBasicCompiler comp){
        if (textures == null)
            textures = new TextureResourceStore();
        if (images == null)
            images = new PointerResourceStore<>();
        if (displayLists == null)
            displayLists = new DisplayListResourceStore();
    }

    @Override
    public void cleanup() {
        //Do nothing
    }

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<String, Constant>();

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
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new HashMap<String, FuncSpec[]>();
        s.put("loadimage", new FuncSpec[]{new FuncSpec(WrapLoadImage.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
        s.put("deleteimage", new FuncSpec[]{new FuncSpec(WrapDeleteImage.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("imagewidth", new FuncSpec[]{new FuncSpec(WrapImageWidth.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("imageheight", new FuncSpec[]{new FuncSpec(WrapImageHeight.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("imageformat", new FuncSpec[]{new FuncSpec(WrapImageFormat.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("imagedatatype", new FuncSpec[]{new FuncSpec(WrapImageDataType.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("loadtexture", new FuncSpec[]{new FuncSpec(WrapLoadTexture.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
        s.put("loadmipmaptexture", new FuncSpec[]{new FuncSpec(WrapLoadMipmapTexture.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
        s.put("glgentexture", new FuncSpec[]{new FuncSpec(WrapglGenTexture.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
        s.put("gldeletetexture", new FuncSpec[]{new FuncSpec(WrapglDeleteTexture.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glteximage2d", new FuncSpec[]{new FuncSpec(WrapglTexImage2D.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapglTexImage2D_2.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapglTexImage2D_3.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapglTexImage2D_4.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapglTexImage2D_5.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("gltexsubimage2d", new FuncSpec[]{new FuncSpec(WrapglTexSubImage2D.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glubuild2dmipmaps", new FuncSpec[]{new FuncSpec(WrapgluBuild2DMipmaps.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapgluBuild2DMipmaps_2.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapgluBuild2DMipmaps_3.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapgluBuild2DMipmaps_4.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapgluBuild2DMipmaps_5.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glgetString", new FuncSpec[]{new FuncSpec(WrapglGetString.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});
        s.put("extensionsupported", new FuncSpec[]{new FuncSpec(WrapExtensionSupported.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
        s.put("glmultitexcoord2f", new FuncSpec[]{new FuncSpec(WrapglMultiTexCoord2f.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_REAL, ValType.VTP_REAL), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glmultitexcoord2d", new FuncSpec[]{new FuncSpec(WrapglMultiTexCoord2d.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_REAL, ValType.VTP_REAL), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glactivetexture", new FuncSpec[]{new FuncSpec(WrapglActiveTexture.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("maxtextureunits", new FuncSpec[]{new FuncSpec(WrapMaxTextureUnits.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
        s.put("windowwidth", new FuncSpec[]{new FuncSpec(WrapWindowWidth.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
        s.put("windowheight", new FuncSpec[]{new FuncSpec(WrapWindowHeight.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
        s.put("glgentextures", new FuncSpec[]{new FuncSpec(WrapglGenTextures.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("gldeletetextures", new FuncSpec[]{new FuncSpec(WrapglDeleteTextures.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glLoadMatrixd", new FuncSpec[]{new FuncSpec(WrapglLoadMatrixd.class, new ParamTypeList(new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glLoadMatrixf", new FuncSpec[]{new FuncSpec(WrapglLoadMatrixf.class, new ParamTypeList(new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glMultMatrixd", new FuncSpec[]{new FuncSpec(WrapglMultMatrixd.class, new ParamTypeList(new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glMultMatrixf", new FuncSpec[]{new FuncSpec(WrapglMultMatrixf.class, new ParamTypeList(new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glgetpolygonstipple", new FuncSpec[]{new FuncSpec(WrapglGetPolygonStipple.class, new ParamTypeList(new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glpolygonstipple", new FuncSpec[]{new FuncSpec(WrapglPolygonStipple.class, new ParamTypeList(new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glgenlists", new FuncSpec[]{new FuncSpec(WrapglGenLists.class, new ParamTypeList(ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("gldeletelists", new FuncSpec[]{new FuncSpec(WrapglDeleteLists.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glcalllists", new FuncSpec[]{
                new FuncSpec(WrapglCallLists.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapglCallLists_2.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glBegin", new FuncSpec[]{new FuncSpec(WrapglBegin.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glEnd", new FuncSpec[]{new FuncSpec(WrapglEnd.class, new ParamTypeList(), true, false, ValType.VTP_INT, false, false, null)});

        s.put("imagestripframes", new FuncSpec[]{new FuncSpec(OldSquare_WrapImageStripFrames.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, true, false, null),
                new FuncSpec(OldSquare_WrapImageStripFrames_2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT), true, true, ValType.VTP_INT, true, false, null),
                new FuncSpec(WrapImageStripFrames.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT, ValType.VTP_INT), true, true, ValType.VTP_INT, true, false, null)});
        s.put("loadimagestrip", new FuncSpec[]{new FuncSpec(OldSquare_WrapLoadImageStrip.class, new ParamTypeList(ValType.VTP_STRING), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null),
                new FuncSpec(OldSquare_WrapLoadImageStrip_2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null),
                new FuncSpec(WrapLoadImageStrip.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT, ValType.VTP_INT), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null)});
        s.put("loadmipmapimagestrip", new FuncSpec[]{new FuncSpec(OldSquare_WrapLoadMipmapImageStrip.class, new ParamTypeList(ValType.VTP_STRING), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null),
                new FuncSpec(OldSquare_WrapLoadMipmapImageStrip_2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null),
                new FuncSpec(WrapLoadMipmapImageStrip.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT, ValType.VTP_INT), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null)});

        s.put("glGetFloatv", new FuncSpec[]{new FuncSpec(WrapglGetFloatv_2D.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glGetDoublev", new FuncSpec[]{new FuncSpec(WrapglGetDoublev_2D.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glGetIntegerv", new FuncSpec[]{new FuncSpec(WrapglGetIntegerv_2D.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});
        s.put("glGetBooleanv", new FuncSpec[]{new FuncSpec(WrapglGetBooleanv_2D.class, new ParamTypeList(new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true)), true, false, ValType.VTP_INT, false, false, null)});


// New texture loading
        s.put("LoadTex", new FuncSpec[]{new FuncSpec(WrapLoadTex.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, true, false, null)});
        s.put("LoadTexStrip", new FuncSpec[]{
                new FuncSpec(WrapLoadTexStrip.class, new ParamTypeList(ValType.VTP_STRING), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null),
                new FuncSpec(WrapLoadTexStrip2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT, ValType.VTP_INT), true, true, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), true, false, null)});
        s.put("TexStripFrames", new FuncSpec[]{
                new FuncSpec(WrapTexStripFrames.class, new ParamTypeList(ValType.VTP_STRING), true, true, ValType.VTP_INT, true, false, null),
                new FuncSpec(WrapTexStripFrames2.class, new ParamTypeList(ValType.VTP_STRING, ValType.VTP_INT, ValType.VTP_INT), true, true, ValType.VTP_INT, true, false, null)});
        s.put("SetTexIgnoreBlankFrames", new FuncSpec[]{new FuncSpec(WrapSetTexIgnoreBlankFrames.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("SetTexTransparentCol", new FuncSpec[]{
                new FuncSpec(WrapSetTexTransparentCol.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null),
                new FuncSpec(WrapSetTexTransparentCol2.class, new ParamTypeList(ValType.VTP_INT, ValType.VTP_INT, ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("SetTexNoTransparentCol", new FuncSpec[]{new FuncSpec(WrapSetTexNoTransparentCol.class, new ParamTypeList(), true, false, ValType.VTP_INT, false, false, null)});
        s.put("SetTexMipmap", new FuncSpec[]{new FuncSpec(WrapSetTexMipmap.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
        s.put("SetTexLinearFilter", new FuncSpec[]{new FuncSpec(WrapSetTexLinearFilter.class, new ParamTypeList(ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});
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

    // TextureResourceStore
//
// Stores OpenGL texture handles
    public class TextureResourceStore extends IntHandleResources {
        protected void DeleteHandle(int handle) {
            int texture = handle;//(GLuint) handle;
            ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
            buffer.asIntBuffer().put(texture);
            buffer.rewind();
            glDeleteTextures(buffer.asIntBuffer());
        }
    }


    // DisplayListResourceStore
//
// Stores OpenGL display lists handles
    public class DisplayListResourceStore extends IntHandleResources {

        private Map<Integer, Integer> m_countMap = new HashMap<Integer, Integer>();                 // Maps base to count

        protected void DeleteHandle(int handle) {
            glDeleteLists(handle, m_countMap.get(handle));
        }

        public void Clear() {
            super.Clear();
            m_countMap.clear();
        }

        public void Store(int handle, int count) {
            if (!Valid(handle) || m_countMap.get(handle) < count) {   // Not already stored, or new value covers a bigger range
                super.Store(handle);
                m_countMap.put(handle, count);
            }
        }

        int GetCount(int base) {
            assertTrue(Valid(base));
            return m_countMap.get(base);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Interface for DLLs

    class WindowAdapter implements IB4GLOpenGLWindow {
        public int Width() {
            return OpenGLBasicLib.appWindow.Width();
        }

        public int Height() {
            return OpenGLBasicLib.appWindow.Height();
        }

        public int BPP() {
            return OpenGLBasicLib.appWindow.Bpp();
        }

        public boolean Fullscreen() {
            return OpenGLBasicLib.appWindow.FullScreen();
        }

        public void SwapBuffers() {
            OpenGLBasicLib.appWindow.SwapBuffers();
        }

        public String Title() {
            return OpenGLBasicLib.appWindow.Title();
        }
    }


    //------------------------------------------------------------------------------
// New image strip loading routines
    static void CalcImageStripFrames(
            Image image,
            int frameWidth,
            int frameHeight,
            IntBuffer frames,
            IntBuffer width,
            IntBuffer height) {

        // Return the # of frames in the image
        assertTrue(image != null);
        int w = image.getWidth(), h = image.getHeight();
        if (frames != null)
            frames.put(0, (w / frameWidth) * (h / frameHeight));
        if (width != null)
            width.put(0, w);
        if (height != null)
            height.put(0, h);
    }

    static boolean CheckFrameSize(int frameSize) {
        return frameSize >= 1 && frameSize <= 1024 && OpenGLBasicLib.IsPowerOf2(frameSize);
    }

    static int ImageStripFrames(
            TomVM vm,
            String filename,
            int frameWidth,
            int frameHeight) {

        if (!CheckFrameSize(frameWidth)) {
            vm.FunctionError("Frame width must be a power of 2 from 1-1024");
            return 0;
        }
        if (!CheckFrameSize(frameHeight)) {
            vm.FunctionError("Frame height must be a power of 2 from 1-1024");
            return 0;
        }

        IntBuffer result = BufferUtils.createIntBuffer(0);
        Image image = new Image(filename);
        OpenGLBasicLib.CalcImageStripFrames(image, frameWidth, frameHeight, result, null, null);

        return result.get(0);
    }

    static void LoadImageStrip(
            TomVM vm,
            String filename,
            int frameWidth,
            int frameHeight,
            boolean mipmap) {

        if (!OpenGLBasicLib.CheckFrameSize(frameWidth)) {
            vm.FunctionError("Frame width must be a power of 2 from 1-1024");
            return;
        }
        if (!OpenGLBasicLib.CheckFrameSize(frameHeight)) {
            vm.FunctionError("Frame height must be a power of 2 from 1-1024");
            return;
        }

        // Load image strip
        Image image = LoadImage.LoadImage(filename);
        if (image != null) {
            IntBuffer frameCount = BufferUtils.createIntBuffer(1),
                    width = BufferUtils.createIntBuffer(1), height = BufferUtils.createIntBuffer(1);
            OpenGLBasicLib.CalcImageStripFrames(image, frameWidth, frameHeight, frameCount, width, height);
            if (frameCount.get(0) > 65536) {
                vm.FunctionError("Cannot load more than 65536 images in an image strip");
                return;
            }
            if (frameCount.get(0) > 0) {

                // Generate some OpenGL textures
                ByteBuffer tex = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE * 65536);
                IntBuffer texIntBuffer = tex.asIntBuffer();
                texIntBuffer.limit(frameCount.get(0));

                glGenTextures(texIntBuffer);

                // Store texture handles in texture store object so Basic4GL can track them
                for (int i = 0; i < frameCount.get(0); i++)
                    textures.Store(tex.asIntBuffer().get(i));

                // Iterate over image in grid pattern, extracting each frame
                int frame = 0;
                ByteBuffer buffer = BufferUtils.createByteBuffer(frameWidth * frameHeight * 4);
                int bytesPerPixel = image.getBPP();//corona.PF_R8G8B8 ? 3 : 4;
                int format = LoadImage.ImageFormat(image);
                for (int y = 0; y < height.get(0) / frameHeight; y++) {
                    for (int x = 0; x < width.get(0) / frameWidth; x++) {

                        // Extract block of pixels
                        CopyPixels(image.getPixels(),
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
                            //GLU deprecated
                            /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
                                    bytesPerPixel,
                                    frameWidth,
                                    frameHeight,
                                    format,
                                    GL_UNSIGNED_BYTE,
                                    buffer);*/
                            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                                    0,
                                    image.getFormat(),
                                    frameWidth, frameHeight, 0,
                                    format, GL11.GL_UNSIGNED_BYTE,
                                    buffer);
                            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                        } else {
                            glTexImage2D(GL_TEXTURE_2D,
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
                int t[] = new int[tex.asIntBuffer().capacity()];
                tex.asIntBuffer().get(t);
                // Return array of textures
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), frameCount.get(0), t));
                return;
            }
        }

        // Load failed.
        // Return 1 element array containing a 0.
        int blankFrame = 0;
        vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), 1, new int[]{blankFrame}));
    }

    ////////////////////////////////////////////////////////////////////////////////
// Helper functions

    static int UploadTexture(Image image) {
        assertTrue(image != null);

        // Generate texture
        int texture;
        ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
        glGenTextures(buffer.asIntBuffer());
        glBindTexture(GL_TEXTURE_2D, texture = buffer.asIntBuffer().get(0));

        int width = image.getWidth();
        int height = image.getHeight();
        int pixelSize = image.getBPP();//GetPixelSize(image.getFormat());
        int format = LoadImage.ImageFormat(image);
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
            //GLU deprecated
            /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
                    pixelSize,
                    width,
                    height,
                    format,
                    GL_UNSIGNED_BYTE,
                    pixels);*/
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                    0,
                    image.getFormat(),
                    width, height, 0,
                    format, GL11.GL_UNSIGNED_BYTE,
                    pixels);
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
            glTexImage2D(GL_TEXTURE_2D,
                    0,
                    image.getFormat(),
                    width,
                    height,
                    0,
                    format,
                    GL_UNSIGNED_BYTE,
                    pixels);
        }

        return texture;
    }

    static int LoadTex(String filename) {

        // Load image
        Image image = LoadImage.LoadImage(filename);
        if (image != null) {

            // Process image
            if (usingTransparentCol)
                image = LoadImage.ApplyTransparentColour(image, transparentCol);
            //TODO Confirm texture dimensions are powers of 2
            //image = LoadImage.ResizeImageForOpenGL(image);

            // Upload into texture
            int texture = OpenGLBasicLib.UploadTexture(image);

            return texture;
        } else
            return 0;
    }

    static String FileExt(String filename) {
        String ext = "";
        int i = filename.lastIndexOf('.');
        if (i > 0)
            ext = filename.substring(i + 1);
        return ext.toLowerCase();
    }

    static boolean IsPowerOf2(int value) {
        if (value <= 0)
            return false;
        while ((value & 1) == 0)
            value >>= 1;
        return value == 1;
    }

    static void CopyPixels(ByteBuffer src,                      // Source image
                           int srcWidth,          // Image size
                           int srcHeight,
                           int srcX,              // Offset in image
                           int srcY,
                           ByteBuffer dst,                      // Destination image
                           int dstWidth,          // And size
                           int dstHeight,
                           int bytesPerPixel) {   // (Source and dest)
        assertTrue(src != null);
        assertTrue(dst != null);
        assertTrue(srcWidth >= srcX + dstWidth);
        assertTrue(srcHeight >= srcY + dstHeight);

        // Copy image data line by line
        int y;
        for (y = 0; y < dstHeight; y++) {
            dst.position(y * dstWidth * bytesPerPixel);
            src.position(((y + srcY) * srcWidth + srcX) * bytesPerPixel);
            for (int x = 0; x < dstWidth * bytesPerPixel; x++)
                dst.put(src.get());
        }
        //dst.rewind();
    }


    static Vector<Image> LoadTexStripImages(String filename, int frameXSize, int frameYSize) {

        // Load main image
        Image image = new Image(filename);//LoadImage(filename);
        if (image != null) {

            // Split into frames
            Vector<Image> images = LoadImage.SplitUpImageStrip(image, frameXSize, frameYSize);

            // Process images
            if (usingTransparentCol)
                for (int i = 0; i < images.size(); i++)
                    images.set(i, LoadImage.ApplyTransparentColour(image, transparentCol));

            if (truncateBlankFrames) {
                while (images.size() > 1 && LoadImage.ImageIsBlank(images.lastElement())) {
                    images.remove(images.lastElement());
                }
            }

            return images;
        } else
            return new Vector<Image>();
    }

    static void DeleteImages(Vector<Image> images) {
        for (Image i : images)
            i.getPixels().clear().limit(0);
        images.clear();
    }

    static int TexStripFrames(String filename) {
        return OpenGLBasicLib.TexStripFrames(filename, 0, 0);
    }

    static int TexStripFrames(String filename, int frameXSize, int frameYSize) {

        // Load image
        Vector<Image> images = LoadTexStripImages(filename, frameXSize, frameYSize);

        // Count frames
        int result = images.size();

        DeleteImages(images);
        return result;
    }

    static Vector<Integer> LoadTexStrip(String filename) {
        return LoadTexStrip(filename, 0, 0);
    }

    static Vector<Integer> LoadTexStrip(String filename, int frameXSize) {
        return LoadTexStrip(filename, frameXSize, 0);
    }

    static Vector<Integer> LoadTexStrip(String filename, int frameXSize, int frameYSize) {

        // Load images
        Vector<Image> images = LoadTexStripImages(filename, frameXSize, frameYSize);

        // Upload into textures
        Vector<Integer> textures = new Vector<Integer>();
        for (int i = 0; i < images.size(); i++) {
            //TODO Confirm texture dimensions are powers of 2
            //images.set(i, ResizeImageForOpenGL(images.get(i)));
            textures.add(UploadTexture(images.get(i)));
        }

        DeleteImages(images);
        return textures;
    }

    static int LoadTexture(String filename, boolean mipmap) {

        // Load texture
        int result = 0;
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        // Generate and load image
        Image image = LoadImage.LoadImage(filename);
        if (image != null) {
            //TODO Confirm texture dimensions are powers of 2
            //image = ResizeImageForOpenGL (image);

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
                //GLU deprecated
                /*gluBuild2DMipmaps ( GL_TEXTURE_2D,
                        (image.getFormat () & 0xffff) == 3 ? 3 : 4, //corona.PF_R8G8B8 ? 3 : 4,
                        image.getWidth (),
                        image.getHeight (),
                        LoadImage.ImageFormat(image),
                        GL_UNSIGNED_BYTE,
                        image.getPixels ());*/
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,
                        image.getFormat(),
                        image.getWidth(), image.getHeight(), 0,
                        LoadImage.ImageFormat(image),
                        GL11.GL_UNSIGNED_BYTE,
                        image.getPixels());
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0,
                        image.getFormat(),//corona.PF_R8G8B8 ? 3 : 4,
                        image.getWidth(),
                        image.getHeight(),
                        0,
                        LoadImage.ImageFormat(image),
                        GL_UNSIGNED_BYTE,
                        image.getPixels());
            }

            // Store and return texture
            textures.Store(texture);
            result = texture;

        }

        // Clean up OpenGL state
        glPopAttrib();
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////
// Resources


////////////////////////////////////////////////////////////////////////////////
// Function wrappers

    static void InternalWrapglTexImage2D(TomVM vm, ValType elementType, int dimensions, boolean mipmap) {
        // Find array param, and extract dimensions
        int arrayOffset = vm.GetIntParam(1);
        int maxSize;
        if (dimensions == 2) {
            int xSize = Data.ArrayDimensionSize(vm.Data(), arrayOffset, 0),
                    ySize = Data.ArrayDimensionSize(vm.Data(), arrayOffset, 1);

            // Verify size
            if (xSize <= 0 || ySize <= 0) {
                vm.FunctionError("Bad array size");
                return;
            }
            if (ySize > (OpenGLBasicLib.MAXIMAGESIZE / xSize)) {
                vm.FunctionError("Cannot upload arrays greater than 2048 x 2048 into images");
                return;
            }
            maxSize = xSize * ySize;
        } else {
            assertTrue(dimensions == 1);
            int size = Data.ArrayDimensionSize(vm.Data(), arrayOffset, 0);
            if (size <= 0) {
                vm.FunctionError("Bad array size");
                return;
            }
            if (size > OpenGLBasicLib.MAXIMAGESIZE) {
                vm.FunctionError("Cannot upload arrays greater than 2048 x 2048 into images");
                return;
            }
            maxSize = size;
        }

        // Read data, converted requested type
        int type = vm.GetIntParam(2);
        ByteBuffer data = BufferUtils.createByteBuffer(maxSize * 4);
        Routines.ReadArrayDynamic(vm, 1, new ValType(elementType.m_basicType, (byte) dimensions, (byte) 1, true), type, data, maxSize);

        // Generate image
        if (mipmap) {
            //GLU deprecated
            /*
            gluBuild2DMipmaps (     vm.GetIntParam (7),
                    vm.GetIntParam (6),
                    vm.GetIntParam (5),
                    vm.GetIntParam (4),
                    vm.GetIntParam (3),
                    type,
                    data);*/
            GL11.glTexImage2D(vm.GetIntParam(7),
                    0,
                    vm.GetIntParam(6),
                    vm.GetIntParam(5),
                    vm.GetIntParam(4),
                    0,
                    vm.GetIntParam(3),
                    type,
                    data);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } else {
            glTexImage2D(vm.GetIntParam(9),
                    vm.GetIntParam(8),
                    vm.GetIntParam(7),
                    vm.GetIntParam(6),
                    vm.GetIntParam(5),
                    vm.GetIntParam(4),
                    vm.GetIntParam(3),
                    type,
                    data);
        }

    }


    public final class WrapLoadTex implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            int texture = OpenGLBasicLib.LoadTex(vm.GetStringParam(1));
            OpenGLBasicLib.textures.Store(texture);
            vm.Reg().setIntVal(texture);
            glPopAttrib();
        }

    }

    public final class WrapLoadTexStrip implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            Vector<Integer> texs = OpenGLBasicLib.LoadTexStrip(vm.GetStringParam(1));

            // Convert to array and return
            if (texs.size() > 0) {
                int array[] = new int[texs.size()];
                for (int i = 0; i < texs.size(); i++) {
                    array[i] = texs.get(i);
                    OpenGLBasicLib.textures.Store(texs.get(i));
                }
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), texs.size(), array));
            } else {
                int array[] = new int[1];
                array[0] = 0;
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), 1, array));
            }
            glPopAttrib();
        }

    }

    public final class WrapLoadTexStrip2 implements Function {
        public void run(TomVM vm) {
            glPushAttrib(GL_ALL_ATTRIB_BITS);
            Vector<Integer> texs = OpenGLBasicLib.LoadTexStrip(vm.GetStringParam(3), vm.GetIntParam(2), vm.GetIntParam(1));

            // Convert to array and return
            if (texs.size() > 0) {
                Integer array[] = new Integer[texs.size()];
                for (int i = 0; i < texs.size(); i++) {
                    array[i] = texs.get(i);
                    OpenGLBasicLib.textures.Store(texs.get(i));
                }
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), texs.size(), Arrays.asList(array)));
            } else {
                Integer array[] = new Integer[1];
                array[0] = 0;
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), 1, Arrays.asList(array)));
            }
            glPopAttrib();
        }

    }

    public final class WrapTexStripFrames implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.TexStripFrames(vm.GetStringParam(1)));
        }

    }

    public final class WrapTexStripFrames2 implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.TexStripFrames(vm.GetStringParam(3), vm.GetIntParam(2), vm.GetIntParam(1)));
        }

    }

    public final class WrapSetTexIgnoreBlankFrames implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.truncateBlankFrames = vm.GetIntParam(1) != 0;
        }

    }

    public final class WrapSetTexTransparentCol implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.transparentCol = vm.GetIntParam(1);
            OpenGLBasicLib.usingTransparentCol = true;
        }

    }

    public final class WrapSetTexTransparentCol2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.transparentCol =
                    (vm.GetIntParam(3) & 0xff) |
                            ((vm.GetIntParam(2) & 0xff) << 8) |
                            ((vm.GetIntParam(1) & 0xff) << 16);
            OpenGLBasicLib.usingTransparentCol = true;
        }

    }

    public final class WrapSetTexNoTransparentCol implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.usingTransparentCol = false;
        }

    }

    public final class WrapSetTexMipmap implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.doMipmap = vm.GetIntParam(1) != 0;
        }

    }

    public final class WrapSetTexLinearFilter implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.doLinearFilter = vm.GetIntParam(1) != 0;
        }

    }

    public final class WrapglGenTexture implements Function {
        public void run(TomVM vm) {
            int texture;
            ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
            glGenTextures(buffer.asIntBuffer());
            texture = buffer.asIntBuffer().get(0);
            OpenGLBasicLib.textures.Store(texture);
            vm.Reg().setIntVal(texture);
        }
    }

    public final class WrapglDeleteTexture implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.textures.Free(vm.GetIntParam(1));
        }

    }


    public final class WrapLoadTexture implements Function {
        public void run(TomVM vm) {

            // Load and return non-mipmapped texture
            vm.Reg().setIntVal(OpenGLBasicLib.LoadTexture(vm.GetStringParam(1), false));
        }
    }

    public final class WrapLoadMipmapTexture implements Function {
        public void run(TomVM vm) {

            // Load and return mipmapped texture
            vm.Reg().setIntVal(OpenGLBasicLib.LoadTexture(vm.GetStringParam(1), true));
        }
    }

    public final class WrapLoadImage implements Function {
        public void run(TomVM vm) {

            // Attempt to load image
            Image image = LoadImage.LoadImage(vm.GetStringParam(1));

            // If successful, store it and return handle
            vm.Reg().setIntVal((image != null) ? OpenGLBasicLib.images.Alloc(image) : 0);
        }
    }

    public final class WrapDeleteImage implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.images.Free(vm.GetIntParam(1));
        }
    }

    public final class WrapglTexImage2D implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.GetIntParam(1);
            if (OpenGLBasicLib.images.IndexStored(index)) {
                ByteBuffer pixels = images.Value(index).getPixels();

                // Generate image
                glTexImage2D(vm.GetIntParam(9),         // target
                        vm.GetIntParam(8),         // level
                        vm.GetIntParam(7),         // components
                        vm.GetIntParam(6),         // width
                        vm.GetIntParam(5),         // height
                        vm.GetIntParam(4),         // border
                        vm.GetIntParam(3),         // format
                        vm.GetIntParam(2),         // type
                        pixels);                    // pixels
            }
        }
    }

    public final class WrapglTexImage2D_2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_INT), 1, false);
        }
    }

    public final class WrapglTexImage2D_3 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_REAL), 1, false);
        }
    }

    public final class WrapglTexImage2D_4 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_INT), 2, false);
        }
    }

    public final class WrapglTexImage2D_5 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_REAL), 2, false);
        }
    }

    public final class WrapgluBuild2DMipmaps_2 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_INT), 1, true);
        }
    }

    public final class WrapgluBuild2DMipmaps_3 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_REAL), 1, true);
        }
    }

    public final class WrapgluBuild2DMipmaps_4 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_INT), 2, true);
        }
    }

    public final class WrapgluBuild2DMipmaps_5 implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.InternalWrapglTexImage2D(vm, new ValType(ValType.VTP_REAL), 2, true);
        }
    }

    public final class WrapglTexSubImage2D implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.GetIntParam(1);
            if (OpenGLBasicLib.images.IndexStored(index)) {
                ByteBuffer pixels = OpenGLBasicLib.images.Value(index).getPixels();

                // Generate image
                glTexSubImage2D(vm.GetIntParam(9),     // target
                        vm.GetIntParam(8),     // level
                        vm.GetIntParam(7),     // xoffset
                        vm.GetIntParam(6),     // y offset
                        vm.GetIntParam(5),     // width
                        vm.GetIntParam(4),     // height
                        vm.GetIntParam(3),     // format
                        vm.GetIntParam(2),     // type
                        pixels);                // pixels
            }
        }
    }

    public final class WrapImageWidth implements Function {
        public void run(TomVM vm) {
            int index = vm.GetIntParam(1);
            vm.Reg().setIntVal(OpenGLBasicLib.images.IndexStored(index) ? OpenGLBasicLib.images.Value(index).getWidth() : 0);
        }
    }

    public final class WrapImageHeight implements Function {
        public void run(TomVM vm) {
            int index = vm.GetIntParam(1);
            vm.Reg().setIntVal(OpenGLBasicLib.images.IndexStored(index) ? OpenGLBasicLib.images.Value(index).getHeight() : 0);
        }
    }

    public final class WrapImageFormat implements Function {
        public void run(TomVM vm) {
            int index = vm.GetIntParam(1);
            vm.Reg().setIntVal(OpenGLBasicLib.images.IndexStored(index) ? LoadImage.ImageFormat(OpenGLBasicLib.images.Value(index)) : 0);
        }
    }

    public final class WrapImageDataType implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(GL_UNSIGNED_BYTE);             // Images always stored as unsigned bytes
        }
    }

    public final class WrapgluBuild2DMipmaps implements Function {
        public void run(TomVM vm) {

            // Find image data
            int index = vm.GetIntParam(1);
            if (OpenGLBasicLib.images.IndexStored(index)) {
                ByteBuffer pixels = OpenGLBasicLib.images.Value(index).getPixels();

                // Build 2D mipmaps
                //GLU deprecated
                /*gluBuild2DMipmaps ( vm.GetIntParam (7),         // target
                        vm.GetIntParam (6),         // components
                        vm.GetIntParam (5),         // width
                        vm.GetIntParam (4),         // height
                        vm.GetIntParam (3),         // format
                        vm.GetIntParam (2),         // type
                        pixels);                    // pixels
                */
                GL11.glTexImage2D(vm.GetIntParam(7),
                        0,
                        vm.GetIntParam(6),
                        vm.GetIntParam(5),
                        vm.GetIntParam(4),
                        0,
                        vm.GetIntParam(3),
                        vm.GetIntParam(2),
                        pixels);
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
        }
    }

    public final class WrapglMultiTexCoord2f implements Function {
        public void run(TomVM vm) {
            glMultiTexCoord2f(vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMultiTexCoord2d implements Function {
        public void run(TomVM vm) {
            glMultiTexCoord2d(vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglActiveTexture implements Function {
        public void run(TomVM vm) {
            glActiveTexture(vm.GetIntParam(1));
        }
    }

    public final class WrapglGetString implements Function {
        public void run(TomVM vm) {
            vm.setRegString(glGetString(vm.GetIntParam(1)));
        }
    }

    public final class WrapExtensionSupported implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.appWindow.ExtensionSupported(vm.GetStringParam(1)) ? 1 : 0);
        }
    }

    public final class WrapMaxTextureUnits implements Function {
        public void run(TomVM vm) {
            IntBuffer units = BufferUtils.createIntBuffer(1);
            glGetIntegerv(ARBMultitexture.GL_MAX_TEXTURE_UNITS_ARB, units);
            vm.Reg().setIntVal(units.get(0));
        }
    }

    public final class WrapWindowWidth implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.appWindow.Width());
        }
    }

    public final class WrapWindowHeight implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.appWindow.Height());
        }
    }

    public final class WrapglGenTextures implements Function {
        public void run(TomVM vm) {
            int count = vm.GetIntParam(2);
            if (count > 65536) {
                vm.FunctionError("Count must be 0 - 65536 (Basic4GL restriction)");
                return;
            }
            if (count <= 0)
                return;

            // Generate some texture handles
            ByteBuffer handles = BufferUtils.createByteBuffer((Integer.SIZE / Byte.SIZE) * 65536);            // 64K should be enough for anybody ;)
            IntBuffer handleBuffer = handles.asIntBuffer();

            handleBuffer.limit(count);
            glGenTextures(handleBuffer);

            // Store textures in resource store (so Basic4GL can track them and ensure they have been deallocated)
            for (int i = 0; i < count; i++)
                OpenGLBasicLib.textures.Store(handleBuffer.get(i));

            // Store handles in Basic4GL array
            int t[] = new int[count];
            handleBuffer.rewind();
            handleBuffer.get(t);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 1), t, count);
        }
    }

    public final class WrapglDeleteTextures implements Function {
        public void run(TomVM vm) {
            int count = vm.GetIntParam(2);
            if (count > 65536) {
                vm.FunctionError("Count must be 0 - 65536 (Basic4GL restriction)");
                return;
            }
            if (count <= 0)
                return;

            // Read texture handles
            ByteBuffer handles = BufferUtils.createByteBuffer((Integer.SIZE / Byte.SIZE) * 65536);            // 64K should be enough for anybody ;)
            int[] array = new int[65536];
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 1), array, count);

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
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
            doubleBuffer16.rewind();
            doubleBuffer16.put(a);
            doubleBuffer16.rewind();
            glLoadMatrixd(doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(a);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
        }
    }

    public final class WrapglLoadMatrixf implements Function {
        public void run(TomVM vm) {
            float[] a = new float[16];
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
            floatBuffer16.rewind();
            floatBuffer16.put(a);
            floatBuffer16.rewind();
            glLoadMatrixf(floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(a);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
        }
    }

    public final class WrapglMultMatrixd implements Function {
        public void run(TomVM vm) {
            double[] a = new double[16];
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
            doubleBuffer16.rewind();
            doubleBuffer16.put(a);
            doubleBuffer16.rewind();
            glMultMatrixd(doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(a);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
        }
    }

    public final class WrapglMultMatrixf implements Function {
        public void run(TomVM vm) {
            float[] a = new float[16];
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
            floatBuffer16.rewind();
            floatBuffer16.put(a);
            floatBuffer16.rewind();
            glMultMatrixf(floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(a);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), a, 16);
        }
    }

    public final class WrapglGetPolygonStipple implements Function {
        public void run(TomVM vm) {
            ByteBuffer mask = ByteBuffer.wrap(new byte[128]).order(ByteOrder.nativeOrder());
            glGetPolygonStipple(mask);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), mask.array(), 128);
        }
    }

    public final class WrapglPolygonStipple implements Function {
        public void run(TomVM vm) {
            ByteBuffer mask = ByteBuffer.wrap(new byte[128]).order(ByteOrder.nativeOrder());
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), mask.array(), 128);
            glPolygonStipple(mask);
        }
    }

    public final class WrapglGenLists implements Function {
        public void run(TomVM vm) {

            // Validate params
            int count = vm.GetIntParam(1);

            // Get display lists
            int base = glGenLists(count);

            // Track display lists, so Basic4GL can delete them if necessary
            if (base != GL_INVALID_VALUE || base != GL_INVALID_OPERATION)
                OpenGLBasicLib.displayLists.Store(base, count);

            // Return result
            vm.Reg().setIntVal(base);
        }
    }

    public final class WrapglDeleteLists implements Function {
        public void run(TomVM vm) {

            // Get params
            int base = vm.GetIntParam(2),
                    count = vm.GetIntParam(1);

            // Delete display lists
            glDeleteLists(base, count);

            // Remove display lists entry (if the range was correctly deleted)
            if (OpenGLBasicLib.displayLists.Valid(base) && OpenGLBasicLib.displayLists.GetCount(base) <= count)
                OpenGLBasicLib.displayLists.Remove(base);
        }
    }

    public final class WrapglCallLists implements Function {
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
//            if (Routines.ReadArrayDynamic(vm, 1, new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), type, array, n) == 0)
//                return;
//
//            // Call glCallLists
//            glCallLists(n, type, array);
        }

    }

    public final class WrapglCallLists_2 implements Function {
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
//            if (Routines.ReadArrayDynamic(vm, 1, new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), type, array, n) == 0)
//                return;
//
//            // Call glCallLists
//            glCallLists(n, type, array);
        }
    }

    public final class WrapglBegin implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.appWindow.SetDontPaint(true);             // Dont paint on WM_PAINT messages when between a glBegin() and a glEnd ()
            glBegin(vm.GetIntParam(1));                // This doesn't effect running code, but helps when stepping through calls in the debugger
        }
    }

    public final class WrapglEnd implements Function {
        public void run(TomVM vm) {
            glEnd();
            OpenGLBasicLib.appWindow.SetDontPaint(false);
        }

    }


    public final class WrapglGetFloatv_2D implements Function {
        public void run(TomVM vm) {
            float[] data = new float[16];
            Arrays.fill(data, 0);
            floatBuffer16.rewind();
            floatBuffer16.put(data);
            floatBuffer16.rewind();
            glGetFloatv(vm.GetIntParam(2), floatBuffer16);
            floatBuffer16.rewind();
            floatBuffer16.get(data);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), data, 16);
        }
    }

    public final class WrapglGetDoublev_2D implements Function {
        public void run(TomVM vm) {
            double[] data = new double[16];
            Arrays.fill(data, 0);
            doubleBuffer16.rewind();
            doubleBuffer16.put(data);
            doubleBuffer16.rewind();
            glGetDoublev(vm.GetIntParam(2), doubleBuffer16);
            doubleBuffer16.rewind();
            doubleBuffer16.get(data);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 2, (byte) 1, true), data, 16);
        }
    }

    public final class WrapglGetIntegerv_2D implements Function {
        public void run(TomVM vm) {
            int[] data = new int[16];
            Arrays.fill(data, 0);
            intBuffer16.rewind();
            intBuffer16.put(data);
            intBuffer16.rewind();
            glGetIntegerv(vm.GetIntParam(2), intBuffer16);
            intBuffer16.rewind();
            intBuffer16.get(data);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 2, (byte) 1, true), data, 16);
        }
    }

    public final class WrapglGetBooleanv_2D implements Function {
        public void run(TomVM vm) {
            byte[] data = new byte[16];
            Arrays.fill(data, (byte) 0);
            byteBuffer16.rewind();
            byteBuffer16.put(data);
            byteBuffer16.rewind();
            glGetBooleanv(vm.GetIntParam(2), byteBuffer16);
            byteBuffer16.rewind();
            byteBuffer16.get(data);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_INT, (byte) 2, (byte) 1, true), data, 16);
        }
    }

    public final class WrapLoadImageStrip implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.LoadImageStrip(vm, vm.GetStringParam(3), vm.GetIntParam(2), vm.GetIntParam(1), false);
        }
    }

    public final class WrapLoadMipmapImageStrip implements Function {
        public void run(TomVM vm) {
            OpenGLBasicLib.LoadImageStrip(vm, vm.GetStringParam(3), vm.GetIntParam(2), vm.GetIntParam(1), true);
        }
    }

    public final class WrapImageStripFrames implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(OpenGLBasicLib.ImageStripFrames(vm, vm.GetStringParam(3), vm.GetIntParam(2), vm.GetIntParam(1)));
        }


    }

    // Old square image strip routines.
// These ones take only a single frame size param and assume the frame is square.
// I'm not too sure of the logic behind these original versions, but I'm keeping
// them for backwards compatibility.
    void OldSquare_CalcImageStripFrames(
            Image image,
            IntBuffer frameSize,         // Input/Output
            IntBuffer frames,            // Output
            IntBuffer width,             // "
            IntBuffer height) {          // "

        // Return the # of frames in the currently bound image
        // Also adjusts the framesize if appropriate
        assertTrue(image != null);

        // Get image dimensions
        width.put(0, image.getWidth());
        height.put(0, image.getHeight());

        // Calculate frame size (each frame is square, so this represents the width
        // AND height of each frame.)
        int size = frameSize.get(0);
        while (size > image.getWidth())
            size >>= 1;
        while (size > image.getHeight())
            size >>= 1;
        frameSize.put(0, size);
        frames.put(0, (width.get(0) / size) * (height.get(0) / size));
    }

    int OldSquare_ImageStripFrames(TomVM vm, String filename, IntBuffer frameSize) {

        // Image size must be power of 2
        final int size = frameSize.get(0);
        if (size < 1 || size > 1024 || !IsPowerOf2(size)) {
            vm.FunctionError("Frame size must be a power of 2 from 1-1024");
            return 0;
        }

        // Calculate and return # of frames in image strip
        IntBuffer result = BufferUtils.createIntBuffer(1),
                width = BufferUtils.createIntBuffer(1), height = BufferUtils.createIntBuffer(1);

        Image image = LoadImage.LoadImage(filename);
        if (image != null) {
            OldSquare_CalcImageStripFrames(image, frameSize, result, width, height);
        }
        return result.get(0);
    }

    void OldSquare_LoadImageStrip(TomVM vm, String filename, IntBuffer frameSize, boolean mipmap) {

        // Image size must be power of 2
        int size = frameSize.get(0);
        if (size < 1 || size > 1024 || !IsPowerOf2(size)) {
            vm.FunctionError("Frame size must be a power of 2 from 1-1024");
            return;
        }

        // Load image strip
        Image image = LoadImage.LoadImage(filename);
        if (image != null) {
            IntBuffer frameCount = BufferUtils.createIntBuffer(1), width = BufferUtils.createIntBuffer(1), height = BufferUtils.createIntBuffer(1);
            OldSquare_CalcImageStripFrames(image, frameSize, frameCount, width, height);
            int count = frameCount.get(0);
            size = frameSize.get(0);
            if (count > 65536) {
                vm.FunctionError("Cannot load more than 65536 images in an image strip");
                return;
            }
            if (count > 0) {

                // Generate some OpenGL textures
                ByteBuffer tex = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE * 65536);
                IntBuffer texbuffer = tex.asIntBuffer();
                texbuffer.limit(count);
                glGenTextures(texbuffer);
                // Store texture handles in texture store object so Basic4GL can track them
                for (int i = 0; i < count; i++)
                    textures.Store(texbuffer.get(i));

                // Iterate over image in grid pattern, extracting each frame
                int frame = 0;
                ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * image.getBPP()); //TODO see if 4 should be used instead of image.getBPP () here
                int bytesPerPixel = image.getBPP();
                int format = image.getFormat();
                for (int y = 0; y < height.get(0) / size; y++) {
                    for (int x = 0; x < width.get(0) / size; x++) {

                        // Extract block of pixels
                        CopyPixels(image.getPixels(),
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
                            //GLU deprecated
                            /*
                            gluBuild2DMipmaps ( GL_TEXTURE_2D,
                                    bytesPerPixel,
                                    frameSize,
                                    frameSize,
                                    format,
                                    GL_UNSIGNED_BYTE,
                                    buffer);
                            */
                            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                                    0,
                                    format,
                                    frameSize.get(0),
                                    frameSize.get(0),
                                    0,
                                    format, GL11.GL_UNSIGNED_BYTE,
                                    buffer);
                            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
                        } else {
                            glTexImage2D(GL_TEXTURE_2D,
                                    0,
                                    format,
                                    size,
                                    size,
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
                tex.rewind();
                int t[] = new int[tex.asIntBuffer().capacity()];
                tex.asIntBuffer().get(t);
                // Return array of textures
                vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), frameCount.get(0), t));
                return;
            }
        }

        // Load failed.
        // Return 1 element array containing a 0.
        int blankFrame = 0;
        vm.Reg().setIntVal(Data.FillTempIntArray(vm.Data(), vm.DataTypes(), 1, new int[]{blankFrame}));
    }

    public final class OldSquare_WrapLoadImageStrip implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            OldSquare_LoadImageStrip(vm, vm.GetStringParam(1), frameSize, false);
        }
    }

    public final class OldSquare_WrapLoadImageStrip_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.GetIntParam(1));
            OldSquare_LoadImageStrip(vm, vm.GetStringParam(2), frameSize, false);
        }
    }

    public final class OldSquare_WrapLoadMipmapImageStrip implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            OldSquare_LoadImageStrip(vm, vm.GetStringParam(1), frameSize, true);
        }
    }

    public final class OldSquare_WrapLoadMipmapImageStrip_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.GetIntParam(1));
            OldSquare_LoadImageStrip(vm, vm.GetStringParam(2), frameSize, true);
        }
    }

    public final class OldSquare_WrapImageStripFrames implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, 1024);
            vm.Reg().setIntVal(OldSquare_ImageStripFrames(vm, vm.GetStringParam(1), frameSize));
        }
    }

    public final class OldSquare_WrapImageStripFrames_2 implements Function {
        public void run(TomVM vm) {
            IntBuffer frameSize = BufferUtils.createIntBuffer(1).put(0, vm.GetIntParam(1));
            vm.Reg().setIntVal(OldSquare_ImageStripFrames(vm, vm.GetStringParam(2), frameSize));
        }
    }

}
