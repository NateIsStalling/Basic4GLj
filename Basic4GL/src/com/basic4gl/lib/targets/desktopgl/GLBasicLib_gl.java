package com.basic4gl.lib.targets.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Routines;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.Data;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;
import com.jogamp.opengl.GLExtensions;


import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.nio.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 4/16/2015.
 */
public class GLBasicLib_gl implements Library {
    GL2 gl;
    @Override
    public boolean isTarget() {
        return false;
    }    //Library is not a build target

    @Override
    public String name() {
        return "GLBasic Lib";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public String description() {
        return "OpenGL functions and constants";
    }

    @Override
    public String author() {
        return "";
    }

    @Override
    public String contact() {
        return "N/A";
    }

    @Override
    public String id() {
        return "glbasiclib_gl";
    }

    @Override
    public String[] compat() {
        return new String[]{"desktopgl"};
    }

    @Override
    public void initVM(TomVM vm) {

    }

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<String, Constant>();
        //c.put("GL_VERSION_1_1", GL_VERSION_1_1));
        c.put("GL_VERSION", new Constant(GL2.GL_VERSION));

        c.put("GL_ACCUM", new Constant(GL2.GL_ACCUM));
        c.put("GL_LOAD", new Constant(GL2.GL_LOAD));
        c.put("GL_RETURN", new Constant(GL2.GL_RETURN));
        c.put("GL_MULT", new Constant(GL2.GL_MULT));
        c.put("GL_ADD", new Constant(GL2.GL_ADD));
        c.put("GL_NEVER", new Constant(GL2.GL_NEVER));
        c.put("GL_LESS", new Constant(GL2.GL_LESS));
        c.put("GL_EQUAL", new Constant(GL2.GL_EQUAL));
        c.put("GL_LEQUAL", new Constant(GL2.GL_LEQUAL));
        c.put("GL_GREATER", new Constant(GL2.GL_GREATER));
        c.put("GL_NOTEQUAL", new Constant(GL2.GL_NOTEQUAL));
        c.put("GL_GEQUAL", new Constant(GL2.GL_GEQUAL));
        c.put("GL_ALWAYS", new Constant(GL2.GL_ALWAYS));
        c.put("GL_CURRENT_BIT", new Constant(GL2.GL_CURRENT_BIT));
        c.put("GL_POINT_BIT", new Constant(GL2.GL_POINT_BIT));
        c.put("GL_LINE_BIT", new Constant(GL2.GL_LINE_BIT));
        c.put("GL_POLYGON_BIT", new Constant(GL2.GL_POLYGON_BIT));
        c.put("GL_POLYGON_STIPPLE_BIT", new Constant(GL2.GL_POLYGON_STIPPLE_BIT));
        c.put("GL_PIXEL_MODE_BIT", new Constant(GL2.GL_PIXEL_MODE_BIT));
        c.put("GL_LIGHTING_BIT", new Constant(GL2.GL_LIGHTING_BIT));
        c.put("GL_FOG_BIT", new Constant(GL2.GL_FOG_BIT));
        c.put("GL_DEPTH_BUFFER_BIT", new Constant(GL2.GL_DEPTH_BUFFER_BIT));
        c.put("GL_ACCUM_BUFFER_BIT", new Constant(GL2.GL_ACCUM_BUFFER_BIT));
        c.put("GL_STENCIL_BUFFER_BIT", new Constant(GL2.GL_STENCIL_BUFFER_BIT));
        c.put("GL_VIEWPORT_BIT", new Constant(GL2.GL_VIEWPORT_BIT));
        c.put("GL_TRANSFORM_BIT", new Constant(GL2.GL_TRANSFORM_BIT));
        c.put("GL_ENABLE_BIT", new Constant(GL2.GL_ENABLE_BIT));
        c.put("GL_COLOR_BUFFER_BIT", new Constant(GL2.GL_COLOR_BUFFER_BIT));
        c.put("GL_HINT_BIT", new Constant(GL2.GL_HINT_BIT));
        c.put("GL_EVAL_BIT", new Constant(GL2.GL_EVAL_BIT));
        c.put("GL_LIST_BIT", new Constant(GL2.GL_LIST_BIT));
        c.put("GL_TEXTURE_BIT", new Constant(GL2.GL_TEXTURE_BIT));
        c.put("GL_SCISSOR_BIT", new Constant(GL2.GL_SCISSOR_BIT));
        c.put("GL_ALL_ATTRIB_BITS", new Constant(GL2.GL_ALL_ATTRIB_BITS));
        c.put("GL_POINTS", new Constant(GL2.GL_POINTS));
        c.put("GL_LINES", new Constant(GL2.GL_LINES));
        c.put("GL_LINE_LOOP", new Constant(GL2.GL_LINE_LOOP));
        c.put("GL_LINE_STRIP", new Constant(GL2.GL_LINE_STRIP));
        c.put("GL_TRIANGLES", new Constant(GL2.GL_TRIANGLES));
        c.put("GL_TRIANGLE_STRIP", new Constant(GL2.GL_TRIANGLE_STRIP));
        c.put("GL_TRIANGLE_FAN", new Constant(GL2.GL_TRIANGLE_FAN));
        c.put("GL_QUADS", new Constant(GL2.GL_QUADS));
        c.put("GL_QUAD_STRIP", new Constant(GL2.GL_QUAD_STRIP));
        c.put("GL_POLYGON", new Constant(GL2.GL_POLYGON));
        c.put("GL_ZERO", new Constant(GL2.GL_ZERO));
        c.put("GL_ONE", new Constant(GL2.GL_ONE));
        c.put("GL_SRC_COLOR", new Constant(GL2.GL_SRC_COLOR));
        c.put("GL_ONE_MINUS_SRC_COLOR", new Constant(GL2.GL_ONE_MINUS_SRC_COLOR));
        c.put("GL_SRC_ALPHA", new Constant(GL2.GL_SRC_ALPHA));
        c.put("GL_ONE_MINUS_SRC_ALPHA", new Constant(GL2.GL_ONE_MINUS_SRC_ALPHA));
        c.put("GL_DST_ALPHA", new Constant(GL2.GL_DST_ALPHA));
        c.put("GL_ONE_MINUS_DST_ALPHA", new Constant(GL2.GL_ONE_MINUS_DST_ALPHA));
        c.put("GL_DST_COLOR", new Constant(GL2.GL_DST_COLOR));
        c.put("GL_ONE_MINUS_DST_COLOR", new Constant(GL2.GL_ONE_MINUS_DST_COLOR));
        c.put("GL_SRC_ALPHA_SATURATE", new Constant(GL2.GL_SRC_ALPHA_SATURATE));
        c.put("GL_TRUE", new Constant(GL2.GL_TRUE));
        c.put("GL_FALSE", new Constant(GL2.GL_FALSE));
        c.put("GL_CLIP_PLANE0", new Constant(GL2.GL_CLIP_PLANE0));
        c.put("GL_CLIP_PLANE1", new Constant(GL2.GL_CLIP_PLANE1));
        c.put("GL_CLIP_PLANE2", new Constant(GL2.GL_CLIP_PLANE2));
        c.put("GL_CLIP_PLANE3", new Constant(GL2.GL_CLIP_PLANE3));
        c.put("GL_CLIP_PLANE4", new Constant(GL2.GL_CLIP_PLANE4));
        c.put("GL_CLIP_PLANE5", new Constant(GL2.GL_CLIP_PLANE5));
        c.put("GL_BYTE", new Constant(GL2.GL_BYTE));
        c.put("GL_UNSIGNED_BYTE", new Constant(GL2.GL_UNSIGNED_BYTE));
        c.put("GL_SHORT", new Constant(GL2.GL_SHORT));
        c.put("GL_UNSIGNED_SHORT", new Constant(GL2.GL_UNSIGNED_SHORT));
        c.put("GL_INT", new Constant(GL2.GL_INT));
        c.put("GL_UNSIGNED_INT", new Constant(GL2.GL_UNSIGNED_INT));
        c.put("GL_FLOAT", new Constant(GL2.GL_FLOAT));
        c.put("GL_2_BYTES", new Constant(GL2.GL_2_BYTES));
        c.put("GL_3_BYTES", new Constant(GL2.GL_3_BYTES));
        c.put("GL_4_BYTES", new Constant(GL2.GL_4_BYTES));
        c.put("GL_DOUBLE", new Constant(GL2.GL_DOUBLE));
        c.put("GL_NONE", new Constant(GL2.GL_NONE));
        c.put("GL_FRONT_LEFT", new Constant(GL2.GL_FRONT_LEFT));
        c.put("GL_FRONT_RIGHT", new Constant(GL2.GL_FRONT_RIGHT));
        c.put("GL_BACK_LEFT", new Constant(GL2.GL_BACK_LEFT));
        c.put("GL_BACK_RIGHT", new Constant(GL2.GL_BACK_RIGHT));
        c.put("GL_FRONT", new Constant(GL2.GL_FRONT));
        c.put("GL_BACK", new Constant(GL2.GL_BACK));
        c.put("GL_LEFT", new Constant(GL2.GL_LEFT));
        c.put("GL_RIGHT", new Constant(GL2.GL_RIGHT));
        c.put("GL_FRONT_AND_BACK", new Constant(GL2.GL_FRONT_AND_BACK));
        c.put("GL_AUX0", new Constant(GL2.GL_AUX0));
        c.put("GL_AUX1", new Constant(GL2.GL_AUX1));
        c.put("GL_AUX2", new Constant(GL2.GL_AUX2));
        c.put("GL_AUX3", new Constant(GL2.GL_AUX3));
        c.put("GL_NO_ERROR", new Constant(GL2.GL_NO_ERROR));
        c.put("GL_INVALID_ENUM", new Constant(GL2.GL_INVALID_ENUM));
        c.put("GL_INVALID_VALUE", new Constant(GL2.GL_INVALID_VALUE));
        c.put("GL_INVALID_OPERATION", new Constant(GL2.GL_INVALID_OPERATION));
        //TODO Constants unavailable in GL 2; original source used GL 1.1
        //c.put("GL_STACK_OVERFLOW", GL2.GL_STACK_OVERFLOW));
        //c.put("GL_STACK_UNDERFLOW", GL2.GL_STACK_UNDERFLOW));
        c.put("GL_OUT_OF_MEMORY", new Constant(GL2.GL_OUT_OF_MEMORY));
        c.put("GL_2D", new Constant(GL2.GL_2D));
        c.put("GL_3D", new Constant(GL2.GL_3D));
        c.put("GL_3D_COLOR", new Constant(GL2.GL_3D_COLOR));
        c.put("GL_3D_COLOR_TEXTURE", new Constant(GL2.GL_3D_COLOR_TEXTURE));
        c.put("GL_4D_COLOR_TEXTURE", new Constant(GL2.GL_4D_COLOR_TEXTURE));
        c.put("GL_PASS_THROUGH_TOKEN", new Constant(GL2.GL_PASS_THROUGH_TOKEN));
        c.put("GL_POINT_TOKEN", new Constant(GL2.GL_POINT_TOKEN));
        c.put("GL_LINE_TOKEN", new Constant(GL2.GL_LINE_TOKEN));
        c.put("GL_POLYGON_TOKEN", new Constant(GL2.GL_POLYGON_TOKEN));
        c.put("GL_BITMAP_TOKEN", new Constant(GL2.GL_BITMAP_TOKEN));
        c.put("GL_DRAW_PIXEL_TOKEN", new Constant(GL2.GL_DRAW_PIXEL_TOKEN));
        c.put("GL_COPY_PIXEL_TOKEN", new Constant(GL2.GL_COPY_PIXEL_TOKEN));
        c.put("GL_LINE_RESET_TOKEN", new Constant(GL2.GL_LINE_RESET_TOKEN));
        c.put("GL_EXP", new Constant(GL2.GL_EXP));
        c.put("GL_EXP2", new Constant(GL2.GL_EXP2));
        c.put("GL_CW", new Constant(GL2.GL_CW));
        c.put("GL_CCW", new Constant(GL2.GL_CCW));
        c.put("GL_COEFF", new Constant(GL2.GL_COEFF));
        c.put("GL_ORDER", new Constant(GL2.GL_ORDER));
        c.put("GL_DOMAIN", new Constant(GL2.GL_DOMAIN));
        c.put("GL_CURRENT_COLOR", new Constant(GL2.GL_CURRENT_COLOR));
        c.put("GL_CURRENT_INDEX", new Constant(GL2.GL_CURRENT_INDEX));
        c.put("GL_CURRENT_NORMAL", new Constant(GL2.GL_CURRENT_NORMAL));
        c.put("GL_CURRENT_TEXTURE_COORDS", new Constant(GL2.GL_CURRENT_TEXTURE_COORDS));
        c.put("GL_CURRENT_RASTER_COLOR", new Constant(GL2.GL_CURRENT_RASTER_COLOR));
        c.put("GL_CURRENT_RASTER_INDEX", new Constant(GL2.GL_CURRENT_RASTER_INDEX));
        c.put("GL_CURRENT_RASTER_TEXTURE_COORDS", new Constant(GL2.GL_CURRENT_RASTER_TEXTURE_COORDS));
        c.put("GL_CURRENT_RASTER_POSITION", new Constant(GL2.GL_CURRENT_RASTER_POSITION));
        c.put("GL_CURRENT_RASTER_POSITION_VALID", new Constant(GL2.GL_CURRENT_RASTER_POSITION_VALID));
        c.put("GL_CURRENT_RASTER_DISTANCE", new Constant(GL2.GL_CURRENT_RASTER_DISTANCE));
        c.put("GL_POINT_SMOOTH", new Constant(GL2.GL_POINT_SMOOTH));
        c.put("GL_POINT_SIZE", new Constant(GL2.GL_POINT_SIZE));
        c.put("GL_POINT_SIZE_RANGE", new Constant(GL2.GL_POINT_SIZE_RANGE));
        c.put("GL_POINT_SIZE_GRANULARITY", new Constant(GL2.GL_POINT_SIZE_GRANULARITY));
        c.put("GL_LINE_SMOOTH", new Constant(GL2.GL_LINE_SMOOTH));
        c.put("GL_LINE_WIDTH", new Constant(GL2.GL_LINE_WIDTH));
        c.put("GL_LINE_WIDTH_RANGE", new Constant(GL2.GL_LINE_WIDTH_RANGE));
        c.put("GL_LINE_WIDTH_GRANULARITY", new Constant(GL2.GL_LINE_WIDTH_GRANULARITY));
        c.put("GL_LINE_STIPPLE", new Constant(GL2.GL_LINE_STIPPLE));
        c.put("GL_LINE_STIPPLE_PATTERN", new Constant(GL2.GL_LINE_STIPPLE_PATTERN));
        c.put("GL_LINE_STIPPLE_REPEAT", new Constant(GL2.GL_LINE_STIPPLE_REPEAT));
        c.put("GL_LIST_MODE", new Constant(GL2.GL_LIST_MODE));
        c.put("GL_MAX_LIST_NESTING", new Constant(GL2.GL_MAX_LIST_NESTING));
        c.put("GL_LIST_BASE", new Constant(GL2.GL_LIST_BASE));
        c.put("GL_LIST_INDEX", new Constant(GL2.GL_LIST_INDEX));
        c.put("GL_POLYGON_MODE", new Constant(GL2.GL_POLYGON_MODE));
        c.put("GL_POLYGON_SMOOTH", new Constant(GL2.GL_POLYGON_SMOOTH));
        c.put("GL_POLYGON_STIPPLE", new Constant(GL2.GL_POLYGON_STIPPLE));
        c.put("GL_EDGE_FLAG", new Constant(GL2.GL_EDGE_FLAG));
        c.put("GL_CULL_FACE", new Constant(GL2.GL_CULL_FACE));
        c.put("GL_CULL_FACE_MODE", new Constant(GL2.GL_CULL_FACE_MODE));
        c.put("GL_FRONT_FACE", new Constant(GL2.GL_FRONT_FACE));
        c.put("GL_LIGHTING", new Constant(GL2.GL_LIGHTING));
        c.put("GL_LIGHT_MODEL_LOCAL_VIEWER", new Constant(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER));
        c.put("GL_LIGHT_MODEL_TWO_SIDE", new Constant(GL2.GL_LIGHT_MODEL_TWO_SIDE));
        c.put("GL_LIGHT_MODEL_AMBIENT", new Constant(GL2.GL_LIGHT_MODEL_AMBIENT));
        c.put("GL_SHADE_MODEL", new Constant(GL2.GL_SHADE_MODEL));
        c.put("GL_COLOR_MATERIAL_FACE", new Constant(GL2.GL_COLOR_MATERIAL_FACE));
        c.put("GL_COLOR_MATERIAL_PARAMETER", new Constant(GL2.GL_COLOR_MATERIAL_PARAMETER));
        c.put("GL_COLOR_MATERIAL", new Constant(GL2.GL_COLOR_MATERIAL));
        c.put("GL_FOG", new Constant(GL2.GL_FOG));
        c.put("GL_FOG_INDEX", new Constant(GL2.GL_FOG_INDEX));
        c.put("GL_FOG_DENSITY", new Constant(GL2.GL_FOG_DENSITY));
        c.put("GL_FOG_START", new Constant(GL2.GL_FOG_START));
        c.put("GL_FOG_END", new Constant(GL2.GL_FOG_END));
        c.put("GL_FOG_MODE", new Constant(GL2.GL_FOG_MODE));
        c.put("GL_FOG_COLOR", new Constant(GL2.GL_FOG_COLOR));
        c.put("GL_DEPTH_RANGE", new Constant(GL2.GL_DEPTH_RANGE));
        c.put("GL_DEPTH_TEST", new Constant(GL2.GL_DEPTH_TEST));
        c.put("GL_DEPTH_WRITEMASK", new Constant(GL2.GL_DEPTH_WRITEMASK));
        c.put("GL_DEPTH_CLEAR_VALUE", new Constant(GL2.GL_DEPTH_CLEAR_VALUE));
        c.put("GL_DEPTH_FUNC", new Constant(GL2.GL_DEPTH_FUNC));
        c.put("GL_ACCUM_CLEAR_VALUE", new Constant(GL2.GL_ACCUM_CLEAR_VALUE));
        c.put("GL_STENCIL_TEST", new Constant(GL2.GL_STENCIL_TEST));
        c.put("GL_STENCIL_CLEAR_VALUE", new Constant(GL2.GL_STENCIL_CLEAR_VALUE));
        c.put("GL_STENCIL_FUNC", new Constant(GL2.GL_STENCIL_FUNC));
        c.put("GL_STENCIL_VALUE_MASK", new Constant(GL2.GL_STENCIL_VALUE_MASK));
        c.put("GL_STENCIL_FAIL", new Constant(GL2.GL_STENCIL_FAIL));
        c.put("GL_STENCIL_PASS_DEPTH_FAIL", new Constant(GL2.GL_STENCIL_PASS_DEPTH_FAIL));
        c.put("GL_STENCIL_PASS_DEPTH_PASS", new Constant(GL2.GL_STENCIL_PASS_DEPTH_PASS));
        c.put("GL_STENCIL_REF", new Constant(GL2.GL_STENCIL_REF));
        c.put("GL_STENCIL_WRITEMASK", new Constant(GL2.GL_STENCIL_WRITEMASK));
        c.put("GL_MATRIX_MODE", new Constant(GL2.GL_MATRIX_MODE));
        c.put("GL_NORMALIZE", new Constant(GL2.GL_NORMALIZE));
        c.put("GL_VIEWPORT", new Constant(GL2.GL_VIEWPORT));
        c.put("GL_MODELVIEW_STACK_DEPTH", new Constant(GL2.GL_MODELVIEW_STACK_DEPTH));
        c.put("GL_PROJECTION_STACK_DEPTH", new Constant(GL2.GL_PROJECTION_STACK_DEPTH));
        c.put("GL_TEXTURE_STACK_DEPTH", new Constant(GL2.GL_TEXTURE_STACK_DEPTH));
        c.put("GL_MODELVIEW_MATRIX", new Constant(GL2.GL_MODELVIEW_MATRIX));
        c.put("GL_PROJECTION_MATRIX", new Constant(GL2.GL_PROJECTION_MATRIX));
        c.put("GL_TEXTURE_MATRIX", new Constant(GL2.GL_TEXTURE_MATRIX));
        c.put("GL_ATTRIB_STACK_DEPTH", new Constant(GL2.GL_ATTRIB_STACK_DEPTH));
        c.put("GL_CLIENT_ATTRIB_STACK_DEPTH", new Constant(GL2.GL_CLIENT_ATTRIB_STACK_DEPTH));
        c.put("GL_ALPHA_TEST", new Constant(GL2.GL_ALPHA_TEST));
        c.put("GL_ALPHA_TEST_FUNC", new Constant(GL2.GL_ALPHA_TEST_FUNC));
        c.put("GL_ALPHA_TEST_REF", new Constant(GL2.GL_ALPHA_TEST_REF));
        c.put("GL_DITHER", new Constant(GL2.GL_DITHER));
        c.put("GL_BLEND_DST", new Constant(GL2.GL_BLEND_DST));
        c.put("GL_BLEND_SRC", new Constant(GL2.GL_BLEND_SRC));
        c.put("GL_BLEND", new Constant(GL2.GL_BLEND));
        c.put("GL_LOGIC_OP_MODE", new Constant(GL2.GL_LOGIC_OP_MODE));
        c.put("GL_INDEX_LOGIC_OP", new Constant(GL2.GL_INDEX_LOGIC_OP));
        c.put("GL_COLOR_LOGIC_OP", new Constant(GL2.GL_COLOR_LOGIC_OP));
        c.put("GL_AUX_BUFFERS", new Constant(GL2.GL_AUX_BUFFERS));
        c.put("GL_DRAW_BUFFER", new Constant(GL2.GL_DRAW_BUFFER));
        c.put("GL_READ_BUFFER", new Constant(GL2.GL_READ_BUFFER));
        c.put("GL_SCISSOR_BOX", new Constant(GL2.GL_SCISSOR_BOX));
        c.put("GL_SCISSOR_TEST", new Constant(GL2.GL_SCISSOR_TEST));
        c.put("GL_INDEX_CLEAR_VALUE", new Constant(GL2.GL_INDEX_CLEAR_VALUE));
        c.put("GL_INDEX_WRITEMASK", new Constant(GL2.GL_INDEX_WRITEMASK));
        c.put("GL_COLOR_CLEAR_VALUE", new Constant(GL2.GL_COLOR_CLEAR_VALUE));
        c.put("GL_COLOR_WRITEMASK", new Constant(GL2.GL_COLOR_WRITEMASK));
        c.put("GL_INDEX_MODE", new Constant(GL2.GL_INDEX_MODE));
        c.put("GL_RGBA_MODE", new Constant(GL2.GL_RGBA_MODE));
        c.put("GL_DOUBLEBUFFER", new Constant(GL2.GL_DOUBLEBUFFER));
        c.put("GL_STEREO", new Constant(GL2.GL_STEREO));
        c.put("GL_RENDER_MODE", new Constant(GL2.GL_RENDER_MODE));
        c.put("GL_PERSPECTIVE_CORRECTION_HINT", new Constant(GL2.GL_PERSPECTIVE_CORRECTION_HINT));
        c.put("GL_POINT_SMOOTH_HINT", new Constant(GL2.GL_POINT_SMOOTH_HINT));
        c.put("GL_LINE_SMOOTH_HINT", new Constant(GL2.GL_LINE_SMOOTH_HINT));
        c.put("GL_POLYGON_SMOOTH_HINT", new Constant(GL2.GL_POLYGON_SMOOTH_HINT));
        c.put("GL_FOG_HINT", new Constant(GL2.GL_FOG_HINT));
        c.put("GL_TEXTURE_GEN_S", new Constant(GL2.GL_TEXTURE_GEN_S));
        c.put("GL_TEXTURE_GEN_T", new Constant(GL2.GL_TEXTURE_GEN_T));
        c.put("GL_TEXTURE_GEN_R", new Constant(GL2.GL_TEXTURE_GEN_R));
        c.put("GL_TEXTURE_GEN_Q", new Constant(GL2.GL_TEXTURE_GEN_Q));
        c.put("GL_PIXEL_MAP_I_TO_I", new Constant(GL2.GL_PIXEL_MAP_I_TO_I));
        c.put("GL_PIXEL_MAP_S_TO_S", new Constant(GL2.GL_PIXEL_MAP_S_TO_S));
        c.put("GL_PIXEL_MAP_I_TO_R", new Constant(GL2.GL_PIXEL_MAP_I_TO_R));
        c.put("GL_PIXEL_MAP_I_TO_G", new Constant(GL2.GL_PIXEL_MAP_I_TO_G));
        c.put("GL_PIXEL_MAP_I_TO_B", new Constant(GL2.GL_PIXEL_MAP_I_TO_B));
        c.put("GL_PIXEL_MAP_I_TO_A", new Constant(GL2.GL_PIXEL_MAP_I_TO_A));
        c.put("GL_PIXEL_MAP_R_TO_R", new Constant(GL2.GL_PIXEL_MAP_R_TO_R));
        c.put("GL_PIXEL_MAP_G_TO_G", new Constant(GL2.GL_PIXEL_MAP_G_TO_G));
        c.put("GL_PIXEL_MAP_B_TO_B", new Constant(GL2.GL_PIXEL_MAP_B_TO_B));
        c.put("GL_PIXEL_MAP_A_TO_A", new Constant(GL2.GL_PIXEL_MAP_A_TO_A));
        c.put("GL_PIXEL_MAP_I_TO_I_SIZE", new Constant(GL2.GL_PIXEL_MAP_I_TO_I_SIZE));
        c.put("GL_PIXEL_MAP_S_TO_S_SIZE", new Constant(GL2.GL_PIXEL_MAP_S_TO_S_SIZE));
        c.put("GL_PIXEL_MAP_I_TO_R_SIZE", new Constant(GL2.GL_PIXEL_MAP_I_TO_R_SIZE));
        c.put("GL_PIXEL_MAP_I_TO_G_SIZE", new Constant(GL2.GL_PIXEL_MAP_I_TO_G_SIZE));
        c.put("GL_PIXEL_MAP_I_TO_B_SIZE", new Constant(GL2.GL_PIXEL_MAP_I_TO_B_SIZE));
        c.put("GL_PIXEL_MAP_I_TO_A_SIZE", new Constant(GL2.GL_PIXEL_MAP_I_TO_A_SIZE));
        c.put("GL_PIXEL_MAP_R_TO_R_SIZE", new Constant(GL2.GL_PIXEL_MAP_R_TO_R_SIZE));
        c.put("GL_PIXEL_MAP_G_TO_G_SIZE", new Constant(GL2.GL_PIXEL_MAP_G_TO_G_SIZE));
        c.put("GL_PIXEL_MAP_B_TO_B_SIZE", new Constant(GL2.GL_PIXEL_MAP_B_TO_B_SIZE));
        c.put("GL_PIXEL_MAP_A_TO_A_SIZE", new Constant(GL2.GL_PIXEL_MAP_A_TO_A_SIZE));
        c.put("GL_UNPACK_SWAP_BYTES", new Constant(GL2.GL_UNPACK_SWAP_BYTES));
        c.put("GL_UNPACK_LSB_FIRST", new Constant(GL2.GL_UNPACK_LSB_FIRST));
        c.put("GL_UNPACK_ROW_LENGTH", new Constant(GL2.GL_UNPACK_ROW_LENGTH));
        c.put("GL_UNPACK_SKIP_ROWS", new Constant(GL2.GL_UNPACK_SKIP_ROWS));
        c.put("GL_UNPACK_SKIP_PIXELS", new Constant(GL2.GL_UNPACK_SKIP_PIXELS));
        c.put("GL_UNPACK_ALIGNMENT", new Constant(GL2.GL_UNPACK_ALIGNMENT));
        c.put("GL_PACK_SWAP_BYTES", new Constant(GL2.GL_PACK_SWAP_BYTES));
        c.put("GL_PACK_LSB_FIRST", new Constant(GL2.GL_PACK_LSB_FIRST));
        c.put("GL_PACK_ROW_LENGTH", new Constant(GL2.GL_PACK_ROW_LENGTH));
        c.put("GL_PACK_SKIP_ROWS", new Constant(GL2.GL_PACK_SKIP_ROWS));
        c.put("GL_PACK_SKIP_PIXELS", new Constant(GL2.GL_PACK_SKIP_PIXELS));
        c.put("GL_PACK_ALIGNMENT", new Constant(GL2.GL_PACK_ALIGNMENT));
        c.put("GL_MAP_COLOR", new Constant(GL2.GL_MAP_COLOR));
        c.put("GL_MAP_STENCIL", new Constant(GL2.GL_MAP_STENCIL));
        c.put("GL_INDEX_SHIFT", new Constant(GL2.GL_INDEX_SHIFT));
        c.put("GL_INDEX_OFFSET", new Constant(GL2.GL_INDEX_OFFSET));
        c.put("GL_RED_SCALE", new Constant(GL2.GL_RED_SCALE));
        c.put("GL_RED_BIAS", new Constant(GL2.GL_RED_BIAS));
        c.put("GL_ZOOM_X", new Constant(GL2.GL_ZOOM_X));
        c.put("GL_ZOOM_Y", new Constant(GL2.GL_ZOOM_Y));
        c.put("GL_GREEN_SCALE", new Constant(GL2.GL_GREEN_SCALE));
        c.put("GL_GREEN_BIAS", new Constant(GL2.GL_GREEN_BIAS));
        c.put("GL_BLUE_SCALE", new Constant(GL2.GL_BLUE_SCALE));
        c.put("GL_BLUE_BIAS", new Constant(GL2.GL_BLUE_BIAS));
        c.put("GL_ALPHA_SCALE", new Constant(GL2.GL_ALPHA_SCALE));
        c.put("GL_ALPHA_BIAS", new Constant(GL2.GL_ALPHA_BIAS));
        c.put("GL_DEPTH_SCALE", new Constant(GL2.GL_DEPTH_SCALE));
        c.put("GL_DEPTH_BIAS", new Constant(GL2.GL_DEPTH_BIAS));
        c.put("GL_MAX_EVAL_ORDER", new Constant(GL2.GL_MAX_EVAL_ORDER));
        c.put("GL_MAX_LIGHTS", new Constant(GL2.GL_MAX_LIGHTS));
        c.put("GL_MAX_CLIP_PLANES", new Constant(GL2.GL_MAX_CLIP_PLANES));
        c.put("GL_MAX_TEXTURE_SIZE", new Constant(GL2.GL_MAX_TEXTURE_SIZE));
        c.put("GL_MAX_PIXEL_MAP_TABLE", new Constant(GL2.GL_MAX_PIXEL_MAP_TABLE));
        c.put("GL_MAX_ATTRIB_STACK_DEPTH", new Constant(GL2.GL_MAX_ATTRIB_STACK_DEPTH));
        c.put("GL_MAX_MODELVIEW_STACK_DEPTH", new Constant(GL2.GL_MAX_MODELVIEW_STACK_DEPTH));
        c.put("GL_MAX_NAME_STACK_DEPTH", new Constant(GL2.GL_MAX_NAME_STACK_DEPTH));
        c.put("GL_MAX_PROJECTION_STACK_DEPTH", new Constant(GL2.GL_MAX_PROJECTION_STACK_DEPTH));
        c.put("GL_MAX_TEXTURE_STACK_DEPTH", new Constant(GL2.GL_MAX_TEXTURE_STACK_DEPTH));
        c.put("GL_MAX_VIEWPORT_DIMS", new Constant(GL2.GL_MAX_VIEWPORT_DIMS));
        c.put("GL_MAX_CLIENT_ATTRIB_STACK_DEPTH", new Constant(GL2.GL_MAX_CLIENT_ATTRIB_STACK_DEPTH));
        c.put("GL_SUBPIXEL_BITS", new Constant(GL2.GL_SUBPIXEL_BITS));
        c.put("GL_INDEX_BITS", new Constant(GL2.GL_INDEX_BITS));
        c.put("GL_RED_BITS", new Constant(GL2.GL_RED_BITS));
        c.put("GL_GREEN_BITS", new Constant(GL2.GL_GREEN_BITS));
        c.put("GL_BLUE_BITS", new Constant(GL2.GL_BLUE_BITS));
        c.put("GL_ALPHA_BITS", new Constant(GL2.GL_ALPHA_BITS));
        c.put("GL_DEPTH_BITS", new Constant(GL2.GL_DEPTH_BITS));
        c.put("GL_STENCIL_BITS", new Constant(GL2.GL_STENCIL_BITS));
        c.put("GL_ACCUM_RED_BITS", new Constant(GL2.GL_ACCUM_RED_BITS));
        c.put("GL_ACCUM_GREEN_BITS", new Constant(GL2.GL_ACCUM_GREEN_BITS));
        c.put("GL_ACCUM_BLUE_BITS", new Constant(GL2.GL_ACCUM_BLUE_BITS));
        c.put("GL_ACCUM_ALPHA_BITS", new Constant(GL2.GL_ACCUM_ALPHA_BITS));
        c.put("GL_NAME_STACK_DEPTH", new Constant(GL2.GL_NAME_STACK_DEPTH));
        c.put("GL_AUTO_NORMAL", new Constant(GL2.GL_AUTO_NORMAL));
        c.put("GL_MAP1_COLOR_4", new Constant(GL2.GL_MAP1_COLOR_4));
        c.put("GL_MAP1_INDEX", new Constant(GL2.GL_MAP1_INDEX));
        c.put("GL_MAP1_NORMAL", new Constant(GL2.GL_MAP1_NORMAL));
        c.put("GL_MAP1_TEXTURE_COORD_1", new Constant(GL2.GL_MAP1_TEXTURE_COORD_1));
        c.put("GL_MAP1_TEXTURE_COORD_2", new Constant(GL2.GL_MAP1_TEXTURE_COORD_2));
        c.put("GL_MAP1_TEXTURE_COORD_3", new Constant(GL2.GL_MAP1_TEXTURE_COORD_3));
        c.put("GL_MAP1_TEXTURE_COORD_4", new Constant(GL2.GL_MAP1_TEXTURE_COORD_4));
        c.put("GL_MAP1_VERTEX_3", new Constant(GL2.GL_MAP1_VERTEX_3));
        c.put("GL_MAP1_VERTEX_4", new Constant(GL2.GL_MAP1_VERTEX_4));
        c.put("GL_MAP2_COLOR_4", new Constant(GL2.GL_MAP2_COLOR_4));
        c.put("GL_MAP2_INDEX", new Constant(GL2.GL_MAP2_INDEX));
        c.put("GL_MAP2_NORMAL", new Constant(GL2.GL_MAP2_NORMAL));
        c.put("GL_MAP2_TEXTURE_COORD_1", new Constant(GL2.GL_MAP2_TEXTURE_COORD_1));
        c.put("GL_MAP2_TEXTURE_COORD_2", new Constant(GL2.GL_MAP2_TEXTURE_COORD_2));
        c.put("GL_MAP2_TEXTURE_COORD_3", new Constant(GL2.GL_MAP2_TEXTURE_COORD_3));
        c.put("GL_MAP2_TEXTURE_COORD_4", new Constant(GL2.GL_MAP2_TEXTURE_COORD_4));
        c.put("GL_MAP2_VERTEX_3", new Constant(GL2.GL_MAP2_VERTEX_3));
        c.put("GL_MAP2_VERTEX_4", new Constant(GL2.GL_MAP2_VERTEX_4));
        c.put("GL_MAP1_GRID_DOMAIN", new Constant(GL2.GL_MAP1_GRID_DOMAIN));
        c.put("GL_MAP1_GRID_SEGMENTS", new Constant(GL2.GL_MAP1_GRID_SEGMENTS));
        c.put("GL_MAP2_GRID_DOMAIN", new Constant(GL2.GL_MAP2_GRID_DOMAIN));
        c.put("GL_MAP2_GRID_SEGMENTS", new Constant(GL2.GL_MAP2_GRID_SEGMENTS));
        c.put("GL_TEXTURE_1D", new Constant(GL2.GL_TEXTURE_1D));
        c.put("GL_TEXTURE_2D", new Constant(GL2.GL_TEXTURE_2D));
        c.put("GL_FEEDBACK_BUFFER_POINTER", new Constant(GL2.GL_FEEDBACK_BUFFER_POINTER));
        c.put("GL_FEEDBACK_BUFFER_SIZE", new Constant(GL2.GL_FEEDBACK_BUFFER_SIZE));
        c.put("GL_FEEDBACK_BUFFER_TYPE", new Constant(GL2.GL_FEEDBACK_BUFFER_TYPE));
        c.put("GL_SELECTION_BUFFER_POINTER", new Constant(GL2.GL_SELECTION_BUFFER_POINTER));
        c.put("GL_SELECTION_BUFFER_SIZE", new Constant(GL2.GL_SELECTION_BUFFER_SIZE));
        c.put("GL_TEXTURE_WIDTH", new Constant(GL2.GL_TEXTURE_WIDTH));
        c.put("GL_TEXTURE_HEIGHT", new Constant(GL2.GL_TEXTURE_HEIGHT));
        c.put("GL_TEXTURE_INTERNAL_FORMAT", new Constant(GL2.GL_TEXTURE_INTERNAL_FORMAT));
        c.put("GL_TEXTURE_BORDER_COLOR", new Constant(GL2.GL_TEXTURE_BORDER_COLOR));
        c.put("GL_TEXTURE_BORDER", new Constant(GL2.GL_TEXTURE_BORDER));
        c.put("GL_DONT_CARE", new Constant(GL2.GL_DONT_CARE));
        c.put("GL_FASTEST", new Constant(GL2.GL_FASTEST));
        c.put("GL_NICEST", new Constant(GL2.GL_NICEST));
        c.put("GL_LIGHT0", new Constant(GL2.GL_LIGHT0));
        c.put("GL_LIGHT1", new Constant(GL2.GL_LIGHT1));
        c.put("GL_LIGHT2", new Constant(GL2.GL_LIGHT2));
        c.put("GL_LIGHT3", new Constant(GL2.GL_LIGHT3));
        c.put("GL_LIGHT4", new Constant(GL2.GL_LIGHT4));
        c.put("GL_LIGHT5", new Constant(GL2.GL_LIGHT5));
        c.put("GL_LIGHT6", new Constant(GL2.GL_LIGHT6));
        c.put("GL_LIGHT7", new Constant(GL2.GL_LIGHT7));
        c.put("GL_AMBIENT", new Constant(GL2.GL_AMBIENT));
        c.put("GL_DIFFUSE", new Constant(GL2.GL_DIFFUSE));
        c.put("GL_SPECULAR", new Constant(GL2.GL_SPECULAR));
        c.put("GL_POSITION", new Constant(GL2.GL_POSITION));
        c.put("GL_SPOT_DIRECTION", new Constant(GL2.GL_SPOT_DIRECTION));
        c.put("GL_SPOT_EXPONENT", new Constant(GL2.GL_SPOT_EXPONENT));
        c.put("GL_SPOT_CUTOFF", new Constant(GL2.GL_SPOT_CUTOFF));
        c.put("GL_CONSTANT_ATTENUATION", new Constant(GL2.GL_CONSTANT_ATTENUATION));
        c.put("GL_LINEAR_ATTENUATION", new Constant(GL2.GL_LINEAR_ATTENUATION));
        c.put("GL_QUADRATIC_ATTENUATION", new Constant(GL2.GL_QUADRATIC_ATTENUATION));
        c.put("GL_COMPILE", new Constant(GL2.GL_COMPILE));
        c.put("GL_COMPILE_AND_EXECUTE", new Constant(GL2.GL_COMPILE_AND_EXECUTE));
        c.put("GL_CLEAR", new Constant(GL2.GL_CLEAR));
        c.put("GL_AND", new Constant(GL2.GL_AND));
        c.put("GL_AND_REVERSE", new Constant(GL2.GL_AND_REVERSE));
        c.put("GL_COPY", new Constant(GL2.GL_COPY));
        c.put("GL_AND_INVERTED", new Constant(GL2.GL_AND_INVERTED));
        c.put("GL_NOOP", new Constant(GL2.GL_NOOP));
        c.put("GL_XOR", new Constant(GL2.GL_XOR));
        c.put("GL_OR", new Constant(GL2.GL_OR));
        c.put("GL_NOR", new Constant(GL2.GL_NOR));
        c.put("GL_EQUIV", new Constant(GL2.GL_EQUIV));
        c.put("GL_INVERT", new Constant(GL2.GL_INVERT));
        c.put("GL_OR_REVERSE", new Constant(GL2.GL_OR_REVERSE));
        c.put("GL_COPY_INVERTED", new Constant(GL2.GL_COPY_INVERTED));
        c.put("GL_OR_INVERTED", new Constant(GL2.GL_OR_INVERTED));
        c.put("GL_NAND", new Constant(GL2.GL_NAND));
        c.put("GL_SET", new Constant(GL2.GL_SET));
        c.put("GL_EMISSION", new Constant(GL2.GL_EMISSION));
        c.put("GL_SHININESS", new Constant(GL2.GL_SHININESS));
        c.put("GL_AMBIENT_AND_DIFFUSE", new Constant(GL2.GL_AMBIENT_AND_DIFFUSE));
        c.put("GL_COLOR_INDEXES", new Constant(GL2.GL_COLOR_INDEXES));
        c.put("GL_MODELVIEW", new Constant(GL2.GL_MODELVIEW));
        c.put("GL_PROJECTION", new Constant(GL2.GL_PROJECTION));
        c.put("GL_TEXTURE", new Constant(GL2.GL_TEXTURE));
        c.put("GL_COLOR", new Constant(GL2.GL_COLOR));
        c.put("GL_DEPTH", new Constant(GL2.GL_DEPTH));
        c.put("GL_STENCIL", new Constant(GL2.GL_STENCIL));
        c.put("GL_COLOR_INDEX", new Constant(GL2.GL_COLOR_INDEX));
        c.put("GL_STENCIL_INDEX", new Constant(GL2.GL_STENCIL_INDEX));
        c.put("GL_DEPTH_COMPONENT", new Constant(GL2.GL_DEPTH_COMPONENT));
        c.put("GL_RED", new Constant(GL2.GL_RED));
        c.put("GL_GREEN", new Constant(GL2.GL_GREEN));
        c.put("GL_BLUE", new Constant(GL2.GL_BLUE));
        c.put("GL_ALPHA", new Constant(GL2.GL_ALPHA));
        c.put("GL_RGB", new Constant(GL2.GL_RGB));
        c.put("GL_RGBA", new Constant(GL2.GL_RGBA));
        c.put("GL_LUMINANCE", new Constant(GL2.GL_LUMINANCE));
        c.put("GL_LUMINANCE_ALPHA", new Constant(GL2.GL_LUMINANCE_ALPHA));
        c.put("GL_BITMAP", new Constant(GL2.GL_BITMAP));
        c.put("GL_POINT", new Constant(GL2.GL_POINT));
        c.put("GL_LINE", new Constant(GL2.GL_LINE));
        c.put("GL_FILL", new Constant(GL2.GL_FILL));
        c.put("GL_RENDER", new Constant(GL2.GL_RENDER));
        c.put("GL_FEEDBACK", new Constant(GL2.GL_FEEDBACK));
        c.put("GL_SELECT", new Constant(GL2.GL_SELECT));
        c.put("GL_FLAT", new Constant(GL2.GL_FLAT));
        c.put("GL_SMOOTH", new Constant(GL2.GL_SMOOTH));
        c.put("GL_KEEP", new Constant(GL2.GL_KEEP));
        c.put("GL_REPLACE", new Constant(GL2.GL_REPLACE));
        c.put("GL_INCR", new Constant(GL2.GL_INCR));
        c.put("GL_DECR", new Constant(GL2.GL_DECR));
        c.put("GL_VENDOR", new Constant(GL2.GL_VENDOR));
        c.put("GL_RENDERER", new Constant(GL2.GL_RENDERER));
        c.put("GL_VERSION", new Constant(GL2.GL_VERSION));
        c.put("GL_EXTENSIONS", new Constant(GL2.GL_EXTENSIONS));
        c.put("GL_S", new Constant(GL2.GL_S));
        c.put("GL_T", new Constant(GL2.GL_T));
        c.put("GL_R", new Constant(GL2.GL_R));
        c.put("GL_Q", new Constant(GL2.GL_Q));
        c.put("GL_MODULATE", new Constant(GL2.GL_MODULATE));
        c.put("GL_DECAL", new Constant(GL2.GL_DECAL));
        c.put("GL_TEXTURE_ENV_MODE", new Constant(GL2.GL_TEXTURE_ENV_MODE));
        c.put("GL_TEXTURE_ENV_COLOR", new Constant(GL2.GL_TEXTURE_ENV_COLOR));
        c.put("GL_TEXTURE_ENV", new Constant(GL2.GL_TEXTURE_ENV));
        c.put("GL_EYE_LINEAR", new Constant(GL2.GL_EYE_LINEAR));
        c.put("GL_OBJECT_LINEAR", new Constant(GL2.GL_OBJECT_LINEAR));
        c.put("GL_SPHERE_MAP", new Constant(GL2.GL_SPHERE_MAP));
        c.put("GL_TEXTURE_GEN_MODE", new Constant(GL2.GL_TEXTURE_GEN_MODE));
        c.put("GL_OBJECT_PLANE", new Constant(GL2.GL_OBJECT_PLANE));
        c.put("GL_EYE_PLANE", new Constant(GL2.GL_EYE_PLANE));
        c.put("GL_NEAREST", new Constant(GL2.GL_NEAREST));
        c.put("GL_LINEAR", new Constant(GL2.GL_LINEAR));
        c.put("GL_NEAREST_MIPMAP_NEAREST", new Constant(GL2.GL_NEAREST_MIPMAP_NEAREST));
        c.put("GL_LINEAR_MIPMAP_NEAREST", new Constant(GL2.GL_LINEAR_MIPMAP_NEAREST));
        c.put("GL_NEAREST_MIPMAP_LINEAR", new Constant(GL2.GL_NEAREST_MIPMAP_LINEAR));
        c.put("GL_LINEAR_MIPMAP_LINEAR", new Constant(GL2.GL_LINEAR_MIPMAP_LINEAR));
        c.put("GL_TEXTURE_MAG_FILTER", new Constant(GL2.GL_TEXTURE_MAG_FILTER));
        c.put("GL_TEXTURE_MIN_FILTER", new Constant(GL2.GL_TEXTURE_MIN_FILTER));
        c.put("GL_TEXTURE_WRAP_S", new Constant(GL2.GL_TEXTURE_WRAP_S));
        c.put("GL_TEXTURE_WRAP_T", new Constant(GL2.GL_TEXTURE_WRAP_T));
        c.put("GL_CLAMP", new Constant(GL2.GL_CLAMP));
        c.put("GL_REPEAT", new Constant(GL2.GL_REPEAT));
        c.put("GL_CLIENT_PIXEL_STORE_BIT", new Constant(GL2.GL_CLIENT_PIXEL_STORE_BIT));
        c.put("GL_CLIENT_VERTEX_ARRAY_BIT", new Constant(GL2.GL_CLIENT_VERTEX_ARRAY_BIT));
        //NOTE: GL_CLIENT_ALL_ATTRIB_BITS was cast to unsigned int in original source
        c.put("GL_CLIENT_ALL_ATTRIB_BITS", new Constant(GL2.GL_CLIENT_ALL_ATTRIB_BITS));
        c.put("GL_POLYGON_OFFSET_FACTOR", new Constant(GL2.GL_POLYGON_OFFSET_FACTOR));
        c.put("GL_POLYGON_OFFSET_UNITS", new Constant(GL2.GL_POLYGON_OFFSET_UNITS));
        c.put("GL_POLYGON_OFFSET_POINT", new Constant(GL2.GL_POLYGON_OFFSET_POINT));
        c.put("GL_POLYGON_OFFSET_LINE", new Constant(GL2.GL_POLYGON_OFFSET_LINE));
        c.put("GL_POLYGON_OFFSET_FILL", new Constant(GL2.GL_POLYGON_OFFSET_FILL));
        c.put("GL_ALPHA4", new Constant(GL2.GL_ALPHA4));
        c.put("GL_ALPHA8", new Constant(GL2.GL_ALPHA8));
        c.put("GL_ALPHA12", new Constant(GL2.GL_ALPHA12));
        c.put("GL_ALPHA16", new Constant(GL2.GL_ALPHA16));
        c.put("GL_LUMINANCE4", new Constant(GL2.GL_LUMINANCE4));
        c.put("GL_LUMINANCE8", new Constant(GL2.GL_LUMINANCE8));
        c.put("GL_LUMINANCE12", new Constant(GL2.GL_LUMINANCE12));
        c.put("GL_LUMINANCE16", new Constant(GL2.GL_LUMINANCE16));
        c.put("GL_LUMINANCE4_ALPHA4", new Constant(GL2.GL_LUMINANCE4_ALPHA4));
        c.put("GL_LUMINANCE6_ALPHA2", new Constant(GL2.GL_LUMINANCE6_ALPHA2));
        c.put("GL_LUMINANCE8_ALPHA8", new Constant(GL2.GL_LUMINANCE8_ALPHA8));
        c.put("GL_LUMINANCE12_ALPHA4", new Constant(GL2.GL_LUMINANCE12_ALPHA4));
        c.put("GL_LUMINANCE12_ALPHA12", new Constant(GL2.GL_LUMINANCE12_ALPHA12));
        c.put("GL_LUMINANCE16_ALPHA16", new Constant(GL2.GL_LUMINANCE16_ALPHA16));
        c.put("GL_INTENSITY", new Constant(GL2.GL_INTENSITY));
        c.put("GL_INTENSITY4", new Constant(GL2.GL_INTENSITY4));
        c.put("GL_INTENSITY8", new Constant(GL2.GL_INTENSITY8));
        c.put("GL_INTENSITY12", new Constant(GL2.GL_INTENSITY12));
        c.put("GL_INTENSITY16", new Constant(GL2.GL_INTENSITY16));
        c.put("GL_R3_G3_B2", new Constant(GL2.GL_R3_G3_B2));
        c.put("GL_RGB4", new Constant(GL2.GL_RGB4));
        c.put("GL_RGB5", new Constant(GL2.GL_RGB5));
        c.put("GL_RGB8", new Constant(GL2.GL_RGB8));
        c.put("GL_RGB10", new Constant(GL2.GL_RGB10));
        c.put("GL_RGB12", new Constant(GL2.GL_RGB12));
        c.put("GL_RGB16", new Constant(GL2.GL_RGB16));
        c.put("GL_RGBA2", new Constant(GL2.GL_RGBA2));
        c.put("GL_RGBA4", new Constant(GL2.GL_RGBA4));
        c.put("GL_RGB5_A1", new Constant(GL2.GL_RGB5_A1));
        c.put("GL_RGBA8", new Constant(GL2.GL_RGBA8));
        c.put("GL_RGB10_A2", new Constant(GL2.GL_RGB10_A2));
        c.put("GL_RGBA12", new Constant(GL2.GL_RGBA12));
        c.put("GL_RGBA16", new Constant(GL2.GL_RGBA16));
        c.put("GL_TEXTURE_RED_SIZE", new Constant(GL2.GL_TEXTURE_RED_SIZE));
        c.put("GL_TEXTURE_GREEN_SIZE", new Constant(GL2.GL_TEXTURE_GREEN_SIZE));
        c.put("GL_TEXTURE_BLUE_SIZE", new Constant(GL2.GL_TEXTURE_BLUE_SIZE));
        c.put("GL_TEXTURE_ALPHA_SIZE", new Constant(GL2.GL_TEXTURE_ALPHA_SIZE));
        c.put("GL_TEXTURE_LUMINANCE_SIZE", new Constant(GL2.GL_TEXTURE_LUMINANCE_SIZE));
        c.put("GL_TEXTURE_INTENSITY_SIZE", new Constant(GL2.GL_TEXTURE_INTENSITY_SIZE));
        c.put("GL_PROXY_TEXTURE_1D", new Constant(GL2.GL_PROXY_TEXTURE_1D));
        c.put("GL_PROXY_TEXTURE_2D", new Constant(GL2.GL_PROXY_TEXTURE_2D));
        c.put("GL_TEXTURE_PRIORITY", new Constant(GL2.GL_TEXTURE_PRIORITY));
        c.put("GL_TEXTURE_RESIDENT", new Constant(GL2.GL_TEXTURE_RESIDENT));
        c.put("GL_TEXTURE_BINDING_1D", new Constant(GL2.GL_TEXTURE_BINDING_1D));
        c.put("GL_TEXTURE_BINDING_2D", new Constant(GL2.GL_TEXTURE_BINDING_2D));
        c.put("GL_VERTEX_ARRAY", new Constant(GL2.GL_VERTEX_ARRAY));
        c.put("GL_NORMAL_ARRAY", new Constant(GL2.GL_NORMAL_ARRAY));
        c.put("GL_COLOR_ARRAY", new Constant(GL2.GL_COLOR_ARRAY));
        c.put("GL_INDEX_ARRAY", new Constant(GL2.GL_INDEX_ARRAY));
        c.put("GL_TEXTURE_COORD_ARRAY", new Constant(GL2.GL_TEXTURE_COORD_ARRAY));
        c.put("GL_EDGE_FLAG_ARRAY", new Constant(GL2.GL_EDGE_FLAG_ARRAY));
        c.put("GL_VERTEX_ARRAY_SIZE", new Constant(GL2.GL_VERTEX_ARRAY_SIZE));
        c.put("GL_VERTEX_ARRAY_TYPE", new Constant(GL2.GL_VERTEX_ARRAY_TYPE));
        c.put("GL_VERTEX_ARRAY_STRIDE", new Constant(GL2.GL_VERTEX_ARRAY_STRIDE));
        c.put("GL_NORMAL_ARRAY_TYPE", new Constant(GL2.GL_NORMAL_ARRAY_TYPE));
        c.put("GL_NORMAL_ARRAY_STRIDE", new Constant(GL2.GL_NORMAL_ARRAY_STRIDE));
        c.put("GL_COLOR_ARRAY_SIZE", new Constant(GL2.GL_COLOR_ARRAY_SIZE));
        c.put("GL_COLOR_ARRAY_TYPE", new Constant(GL2.GL_COLOR_ARRAY_TYPE));
        c.put("GL_COLOR_ARRAY_STRIDE", new Constant(GL2.GL_COLOR_ARRAY_STRIDE));
        c.put("GL_INDEX_ARRAY_TYPE", new Constant(GL2.GL_INDEX_ARRAY_TYPE));
        c.put("GL_INDEX_ARRAY_STRIDE", new Constant(GL2.GL_INDEX_ARRAY_STRIDE));
        c.put("GL_TEXTURE_COORD_ARRAY_SIZE", new Constant(GL2.GL_TEXTURE_COORD_ARRAY_SIZE));
        c.put("GL_TEXTURE_COORD_ARRAY_TYPE", new Constant(GL2.GL_TEXTURE_COORD_ARRAY_TYPE));
        c.put("GL_TEXTURE_COORD_ARRAY_STRIDE", new Constant(GL2.GL_TEXTURE_COORD_ARRAY_STRIDE));
        c.put("GL_EDGE_FLAG_ARRAY_STRIDE", new Constant(GL2.GL_EDGE_FLAG_ARRAY_STRIDE));
        c.put("GL_VERTEX_ARRAY_POINTER", new Constant(GL2.GL_VERTEX_ARRAY_POINTER));
        c.put("GL_NORMAL_ARRAY_POINTER", new Constant(GL2.GL_NORMAL_ARRAY_POINTER));
        c.put("GL_COLOR_ARRAY_POINTER", new Constant(GL2.GL_COLOR_ARRAY_POINTER));
        c.put("GL_INDEX_ARRAY_POINTER", new Constant(GL2.GL_INDEX_ARRAY_POINTER));
        c.put("GL_TEXTURE_COORD_ARRAY_POINTER", new Constant(GL2.GL_TEXTURE_COORD_ARRAY_POINTER));
        c.put("GL_EDGE_FLAG_ARRAY_POINTER", new Constant(GL2.GL_EDGE_FLAG_ARRAY_POINTER));
        c.put("GL_V2F", new Constant(GL2.GL_V2F));
        c.put("GL_V3F", new Constant(GL2.GL_V3F));
        c.put("GL_C4UB_V2F", new Constant(GL2.GL_C4UB_V2F));
        c.put("GL_C4UB_V3F", new Constant(GL2.GL_C4UB_V3F));
        c.put("GL_C3F_V3F", new Constant(GL2.GL_C3F_V3F));
        c.put("GL_N3F_V3F", new Constant(GL2.GL_N3F_V3F));
        c.put("GL_C4F_N3F_V3F", new Constant(GL2.GL_C4F_N3F_V3F));
        c.put("GL_T2F_V3F", new Constant(GL2.GL_T2F_V3F));
        c.put("GL_T4F_V4F", new Constant(GL2.GL_T4F_V4F));
        c.put("GL_T2F_C4UB_V3F", new Constant(GL2.GL_T2F_C4UB_V3F));
        c.put("GL_T2F_C3F_V3F", new Constant(GL2.GL_T2F_C3F_V3F));
        c.put("GL_T2F_N3F_V3F", new Constant(GL2.GL_T2F_N3F_V3F));
        c.put("GL_T2F_C4F_N3F_V3F", new Constant(GL2.GL_T2F_C4F_N3F_V3F));
        c.put("GL_T4F_C4F_N3F_V4F", new Constant(GL2.GL_T4F_C4F_N3F_V4F));
        /*
        c.put("GL_EXT_vertex_array", GL2.GL_EXT_vertex_array));
        c.put("GL_EXT_bgra", GL2.GL_EXT_bgra));
        c.put("GL_EXT_paletted_texture", GL2.GL_EXT_paletted_texture));
        c.put("GL_WIN_swap_hint", GL2.GL_WIN_swap_hint));
        c.put("GL_WIN_draw_range_elements", GL2.GL_WIN_draw_range_elements));
        c.put("GL_VERTEX_ARRAY_EXT", GL2.GL_VERTEX_ARRAY_EXT));
        c.put("GL_NORMAL_ARRAY_EXT", GL2.GL_NORMAL_ARRAY_EXT));
        c.put("GL_COLOR_ARRAY_EXT", GL2.GL_COLOR_ARRAY_EXT));
        c.put("GL_INDEX_ARRAY_EXT", GL2.GL_INDEX_ARRAY_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_EXT", GL2.GL_TEXTURE_COORD_ARRAY_EXT));
        c.put("GL_EDGE_FLAG_ARRAY_EXT", GL2.GL_EDGE_FLAG_ARRAY_EXT));
        c.put("GL_VERTEX_ARRAY_SIZE_EXT", GL2.GL_VERTEX_ARRAY_SIZE_EXT));
        c.put("GL_VERTEX_ARRAY_TYPE_EXT", GL2.GL_VERTEX_ARRAY_TYPE_EXT));
        c.put("GL_VERTEX_ARRAY_STRIDE_EXT", GL2.GL_VERTEX_ARRAY_STRIDE_EXT));
        c.put("GL_VERTEX_ARRAY_COUNT_EXT", GL2.GL_VERTEX_ARRAY_COUNT_EXT));
        c.put("GL_NORMAL_ARRAY_TYPE_EXT", GL2.GL_NORMAL_ARRAY_TYPE_EXT));
        c.put("GL_NORMAL_ARRAY_STRIDE_EXT", GL2.GL_NORMAL_ARRAY_STRIDE_EXT));
        c.put("GL_NORMAL_ARRAY_COUNT_EXT", GL2.GL_NORMAL_ARRAY_COUNT_EXT));
        c.put("GL_COLOR_ARRAY_SIZE_EXT", GL2.GL_COLOR_ARRAY_SIZE_EXT));
        c.put("GL_COLOR_ARRAY_TYPE_EXT", GL2.GL_COLOR_ARRAY_TYPE_EXT));
        c.put("GL_COLOR_ARRAY_STRIDE_EXT", GL2.GL_COLOR_ARRAY_STRIDE_EXT));
        c.put("GL_COLOR_ARRAY_COUNT_EXT", GL2.GL_COLOR_ARRAY_COUNT_EXT));
        c.put("GL_INDEX_ARRAY_TYPE_EXT", GL2.GL_INDEX_ARRAY_TYPE_EXT));
        c.put("GL_INDEX_ARRAY_STRIDE_EXT", GL2.GL_INDEX_ARRAY_STRIDE_EXT));
        c.put("GL_INDEX_ARRAY_COUNT_EXT", GL2.GL_INDEX_ARRAY_COUNT_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_SIZE_EXT", GL2.GL_TEXTURE_COORD_ARRAY_SIZE_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_TYPE_EXT", GL2.GL_TEXTURE_COORD_ARRAY_TYPE_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_STRIDE_EXT", GL2.GL_TEXTURE_COORD_ARRAY_STRIDE_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_COUNT_EXT", GL2.GL_TEXTURE_COORD_ARRAY_COUNT_EXT));
        c.put("GL_EDGE_FLAG_ARRAY_STRIDE_EXT", GL2.GL_EDGE_FLAG_ARRAY_STRIDE_EXT));
        c.put("GL_EDGE_FLAG_ARRAY_COUNT_EXT", GL2.GL_EDGE_FLAG_ARRAY_COUNT_EXT));
        c.put("GL_VERTEX_ARRAY_POINTER_EXT", GL2.GL_VERTEX_ARRAY_POINTER_EXT));
        c.put("GL_NORMAL_ARRAY_POINTER_EXT", GL2.GL_NORMAL_ARRAY_POINTER_EXT));
        c.put("GL_COLOR_ARRAY_POINTER_EXT", GL2.GL_COLOR_ARRAY_POINTER_EXT));
        c.put("GL_INDEX_ARRAY_POINTER_EXT", GL2.GL_INDEX_ARRAY_POINTER_EXT));
        c.put("GL_TEXTURE_COORD_ARRAY_POINTER_EXT", GL2.GL_TEXTURE_COORD_ARRAY_POINTER_EXT));
        c.put("GL_EDGE_FLAG_ARRAY_POINTER_EXT", GL2.GL_EDGE_FLAG_ARRAY_POINTER_EXT));
        c.put("GL_DOUBLE_EXT", GL2.GL_DOUBLE_EXT));
        c.put("GL_BGR_EXT", GL2.GL_BGR_EXT));
        c.put("GL_BGRA_EXT", GL2.GL_BGRA_EXT));
        c.put("GL_COLOR_TABLE_FORMAT_EXT", GL2.GL_COLOR_TABLE_FORMAT_EXT));
        c.put("GL_COLOR_TABLE_WIDTH_EXT", GL2.GL_COLOR_TABLE_WIDTH_EXT));
        c.put("GL_COLOR_TABLE_RED_SIZE_EXT", GL2.GL_COLOR_TABLE_RED_SIZE_EXT));
        c.put("GL_COLOR_TABLE_GREEN_SIZE_EXT", GL2.GL_COLOR_TABLE_GREEN_SIZE_EXT));
        c.put("GL_COLOR_TABLE_BLUE_SIZE_EXT", GL2.GL_COLOR_TABLE_BLUE_SIZE_EXT));
        c.put("GL_COLOR_TABLE_ALPHA_SIZE_EXT", GL2.GL_COLOR_TABLE_ALPHA_SIZE_EXT));
        c.put("GL_COLOR_TABLE_LUMINANCE_SIZE_EXT", GL2.GL_COLOR_TABLE_LUMINANCE_SIZE_EXT));
        c.put("GL_COLOR_TABLE_INTENSITY_SIZE_EXT", GL2.GL_COLOR_TABLE_INTENSITY_SIZE_EXT));
        c.put("GL_COLOR_INDEX1_EXT", GLExtensions.GL_COLOR_INDEX1_EXT));
        c.put("GL_COLOR_INDEX2_EXT", GL.GL_COLOR_INDEX2_EXT));
        c.put("GL_COLOR_INDEX4_EXT", GL2.GL_COLOR_INDEX4_EXT));
        c.put("GL_COLOR_INDEX8_EXT", GL2.GL_COLOR_INDEX8_EXT));
        c.put("GL_COLOR_INDEX12_EXT", GL2.GL_COLOR_INDEX12_EXT));
        c.put("GL_COLOR_INDEX16_EXT", GL2.GL_COLOR_INDEX16_EXT));
        c.put("GL_MAX_ELEMENTS_VERTICES_WIN", GL2.GL_MAX_ELEMENTS_VERTICES_WIN));
        c.put("GL_MAX_ELEMENTS_INDICES_WIN", GL2.GL_MAX_ELEMENTS_INDICES_WIN));
        c.put("GL_PHONG_WIN", GL2.GL_PHONG_WIN));
        c.put("GL_PHONG_HINT_WIN", GL.GL_PHONG_HINT_WIN));
        c.put("GL_FOG_SPECULAR_TEXTURE_WIN", GL2.GL_FOG_SPECULAR_TEXTURE_WIN));

        c.put("GL_LOGIC_OP", new Constant(GL2.GL_LOGIC_OP));
        c.put("GL_TEXTURE_COMPONENTS",new Constant(GL2.GL_TEXTURE_COMPONENTS));*/
        return c;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new HashMap<String, FuncSpec[]>();

        s.put("glAccum", new FuncSpec[]{new FuncSpec(WrapglAccum.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)}),
                true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glAlphaFunc", new FuncSpec[]{new FuncSpec(WrapglAlphaFunc.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)}),
                true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glAreTexturesResident", new FuncSpec[]{new FuncSpec(WrapglAreTexturesResident.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)}),
                true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glArrayElement", new FuncSpec[]{new FuncSpec(WrapglArrayElement.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)}),
                true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glBindTexture", new FuncSpec[]{new FuncSpec(WrapglBindTexture.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glBlendFunc", new FuncSpec[]{new FuncSpec(WrapglBlendFunc.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCallList", new FuncSpec[]{new FuncSpec(WrapglCallList.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClear", new FuncSpec[]{new FuncSpec(WrapglClear.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClearAccum", new FuncSpec[]{new FuncSpec(WrapglClearAccum.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClearColor", new FuncSpec[]{new FuncSpec(WrapglClearColor.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClearDepth", new FuncSpec[]{new FuncSpec(WrapglClearDepth.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClearIndex", new FuncSpec[]{new FuncSpec(WrapglClearIndex.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClearStencil", new FuncSpec[]{new FuncSpec(WrapglClearStencil.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClipPlane", new FuncSpec[]{new FuncSpec(WrapglClipPlane.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glClipPlane", new FuncSpec[]{new FuncSpec(WrapglClipPlane_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3b", new FuncSpec[]{new FuncSpec(WrapglColor3b.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3bv", new FuncSpec[]{new FuncSpec(WrapglColor3bv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3bv", new FuncSpec[]{new FuncSpec(WrapglColor3bv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3d", new FuncSpec[]{new FuncSpec(WrapglColor3d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3dv", new FuncSpec[]{new FuncSpec(WrapglColor3dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3dv", new FuncSpec[]{new FuncSpec(WrapglColor3dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3f", new FuncSpec[]{new FuncSpec(WrapglColor3f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3fv", new FuncSpec[]{new FuncSpec(WrapglColor3fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3fv", new FuncSpec[]{new FuncSpec(WrapglColor3fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3i", new FuncSpec[]{new FuncSpec(WrapglColor3i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3iv", new FuncSpec[]{new FuncSpec(WrapglColor3iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3iv", new FuncSpec[]{new FuncSpec(WrapglColor3iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3s", new FuncSpec[]{new FuncSpec(WrapglColor3s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3sv", new FuncSpec[]{new FuncSpec(WrapglColor3sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3sv", new FuncSpec[]{new FuncSpec(WrapglColor3sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3ub", new FuncSpec[]{new FuncSpec(WrapglColor3ub.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3ubv", new FuncSpec[]{new FuncSpec(WrapglColor3ubv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3ubv", new FuncSpec[]{new FuncSpec(WrapglColor3ubv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3ui", new FuncSpec[]{new FuncSpec(WrapglColor3ui.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3uiv", new FuncSpec[]{new FuncSpec(WrapglColor3uiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3uiv", new FuncSpec[]{new FuncSpec(WrapglColor3uiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3us", new FuncSpec[]{new FuncSpec(WrapglColor3us.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3usv", new FuncSpec[]{new FuncSpec(WrapglColor3usv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor3usv", new FuncSpec[]{new FuncSpec(WrapglColor3usv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4b", new FuncSpec[]{new FuncSpec(WrapglColor4b.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4bv", new FuncSpec[]{new FuncSpec(WrapglColor4bv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4bv", new FuncSpec[]{new FuncSpec(WrapglColor4bv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4d", new FuncSpec[]{new FuncSpec(WrapglColor4d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4dv", new FuncSpec[]{new FuncSpec(WrapglColor4dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4dv", new FuncSpec[]{new FuncSpec(WrapglColor4dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4f", new FuncSpec[]{new FuncSpec(WrapglColor4f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4fv", new FuncSpec[]{new FuncSpec(WrapglColor4fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4fv", new FuncSpec[]{new FuncSpec(WrapglColor4fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4i", new FuncSpec[]{new FuncSpec(WrapglColor4i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4iv", new FuncSpec[]{new FuncSpec(WrapglColor4iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4iv", new FuncSpec[]{new FuncSpec(WrapglColor4iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4s", new FuncSpec[]{new FuncSpec(WrapglColor4s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4sv", new FuncSpec[]{new FuncSpec(WrapglColor4sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4sv", new FuncSpec[]{new FuncSpec(WrapglColor4sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4ub", new FuncSpec[]{new FuncSpec(WrapglColor4ub.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4ubv", new FuncSpec[]{new FuncSpec(WrapglColor4ubv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4ubv", new FuncSpec[]{new FuncSpec(WrapglColor4ubv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4ui", new FuncSpec[]{new FuncSpec(WrapglColor4ui.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4uiv", new FuncSpec[]{new FuncSpec(WrapglColor4uiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4uiv", new FuncSpec[]{new FuncSpec(WrapglColor4uiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4us", new FuncSpec[]{new FuncSpec(WrapglColor4us.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4usv", new FuncSpec[]{new FuncSpec(WrapglColor4usv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColor4usv", new FuncSpec[]{new FuncSpec(WrapglColor4usv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColorMask", new FuncSpec[]{new FuncSpec(WrapglColorMask.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glColorMaterial", new FuncSpec[]{new FuncSpec(WrapglColorMaterial.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCopyPixels", new FuncSpec[]{new FuncSpec(WrapglCopyPixels.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCopyTexImage1D", new FuncSpec[]{new FuncSpec(WrapglCopyTexImage1D.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCopyTexImage2D", new FuncSpec[]{new FuncSpec(WrapglCopyTexImage2D.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCopyTexSubImage1D", new FuncSpec[]{new FuncSpec(WrapglCopyTexSubImage1D.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCopyTexSubImage2D", new FuncSpec[]{new FuncSpec(WrapglCopyTexSubImage2D.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glCullFace", new FuncSpec[]{new FuncSpec(WrapglCullFace.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDepthFunc", new FuncSpec[]{new FuncSpec(WrapglDepthFunc.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDepthMask", new FuncSpec[]{new FuncSpec(WrapglDepthMask.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDepthRange", new FuncSpec[]{new FuncSpec(WrapglDepthRange.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDisable", new FuncSpec[]{new FuncSpec(WrapglDisable.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDisableClientState", new FuncSpec[]{new FuncSpec(WrapglDisableClientState.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDrawArrays", new FuncSpec[]{new FuncSpec(WrapglDrawArrays.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glDrawBuffer", new FuncSpec[]{new FuncSpec(WrapglDrawBuffer.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEdgeFlag", new FuncSpec[]{new FuncSpec(WrapglEdgeFlag.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEdgeFlagv", new FuncSpec[]{new FuncSpec(WrapglEdgeFlagv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEdgeFlagv", new FuncSpec[]{new FuncSpec(WrapglEdgeFlagv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEnable", new FuncSpec[]{new FuncSpec(WrapglEnable.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEnableClientState", new FuncSpec[]{new FuncSpec(WrapglEnableClientState.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEndList", new FuncSpec[]{new FuncSpec(WrapglEndList.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1d", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1dv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1dv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1f", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1fv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord1fv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord1fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2d", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2dv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2dv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2f", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2fv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalCoord2fv", new FuncSpec[]{new FuncSpec(WrapglEvalCoord2fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalMesh1", new FuncSpec[]{new FuncSpec(WrapglEvalMesh1.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalMesh2", new FuncSpec[]{new FuncSpec(WrapglEvalMesh2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalPoint1", new FuncSpec[]{new FuncSpec(WrapglEvalPoint1.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glEvalPoint2", new FuncSpec[]{new FuncSpec(WrapglEvalPoint2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFeedbackBuffer", new FuncSpec[]{new FuncSpec(WrapglFeedbackBuffer.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFinish", new FuncSpec[]{new FuncSpec(WrapglFinish.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFlush", new FuncSpec[]{new FuncSpec(WrapglFlush.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogf", new FuncSpec[]{new FuncSpec(WrapglFogf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogfv", new FuncSpec[]{new FuncSpec(WrapglFogfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogfv", new FuncSpec[]{new FuncSpec(WrapglFogfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogi", new FuncSpec[]{new FuncSpec(WrapglFogi.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogiv", new FuncSpec[]{new FuncSpec(WrapglFogiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFogiv", new FuncSpec[]{new FuncSpec(WrapglFogiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFrontFace", new FuncSpec[]{new FuncSpec(WrapglFrontFace.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glFrustum", new FuncSpec[]{new FuncSpec(WrapglFrustum.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetBooleanv", new FuncSpec[]{new FuncSpec(WrapglGetBooleanv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetBooleanv", new FuncSpec[]{new FuncSpec(WrapglGetBooleanv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetClipPlane", new FuncSpec[]{new FuncSpec(WrapglGetClipPlane.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetClipPlane", new FuncSpec[]{new FuncSpec(WrapglGetClipPlane_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetDoublev", new FuncSpec[]{new FuncSpec(WrapglGetDoublev.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetDoublev", new FuncSpec[]{new FuncSpec(WrapglGetDoublev_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetError", new FuncSpec[]{new FuncSpec(WrapglGetError.class,
                new ParamTypeList(new ValType[]{})
                , true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetFloatv", new FuncSpec[]{new FuncSpec(WrapglGetFloatv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetFloatv", new FuncSpec[]{new FuncSpec(WrapglGetFloatv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetIntegerv", new FuncSpec[]{new FuncSpec(WrapglGetIntegerv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetIntegerv", new FuncSpec[]{new FuncSpec(WrapglGetIntegerv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetLightfv", new FuncSpec[]{new FuncSpec(WrapglGetLightfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetLightfv", new FuncSpec[]{new FuncSpec(WrapglGetLightfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetLightiv", new FuncSpec[]{new FuncSpec(WrapglGetLightiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetLightiv", new FuncSpec[]{new FuncSpec(WrapglGetLightiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetMaterialfv", new FuncSpec[]{new FuncSpec(WrapglGetMaterialfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetMaterialfv", new FuncSpec[]{new FuncSpec(WrapglGetMaterialfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetMaterialiv", new FuncSpec[]{new FuncSpec(WrapglGetMaterialiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetMaterialiv", new FuncSpec[]{new FuncSpec(WrapglGetMaterialiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetPixelMapuiv", new FuncSpec[]{new FuncSpec(WrapglGetPixelMapuiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetPixelMapuiv", new FuncSpec[]{new FuncSpec(WrapglGetPixelMapuiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexEnvfv", new FuncSpec[]{new FuncSpec(WrapglGetTexEnvfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexEnvfv", new FuncSpec[]{new FuncSpec(WrapglGetTexEnvfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexEnviv", new FuncSpec[]{new FuncSpec(WrapglGetTexEnviv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexEnviv", new FuncSpec[]{new FuncSpec(WrapglGetTexEnviv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGendv", new FuncSpec[]{new FuncSpec(WrapglGetTexGendv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGendv", new FuncSpec[]{new FuncSpec(WrapglGetTexGendv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGenfv", new FuncSpec[]{new FuncSpec(WrapglGetTexGenfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGenfv", new FuncSpec[]{new FuncSpec(WrapglGetTexGenfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGeniv", new FuncSpec[]{new FuncSpec(WrapglGetTexGeniv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexGeniv", new FuncSpec[]{new FuncSpec(WrapglGetTexGeniv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexLevelParameterfv", new FuncSpec[]{new FuncSpec(WrapglGetTexLevelParameterfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexLevelParameterfv", new FuncSpec[]{new FuncSpec(WrapglGetTexLevelParameterfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexLevelParameteriv", new FuncSpec[]{new FuncSpec(WrapglGetTexLevelParameteriv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexLevelParameteriv", new FuncSpec[]{new FuncSpec(WrapglGetTexLevelParameteriv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexParameterfv", new FuncSpec[]{new FuncSpec(WrapglGetTexParameterfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexParameterfv", new FuncSpec[]{new FuncSpec(WrapglGetTexParameterfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexParameteriv", new FuncSpec[]{new FuncSpec(WrapglGetTexParameteriv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glGetTexParameteriv", new FuncSpec[]{new FuncSpec(WrapglGetTexParameteriv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glHint", new FuncSpec[]{new FuncSpec(WrapglHint.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)
        });
        s.put("glIndexMask", new FuncSpec[]{new FuncSpec(WrapglIndexMask.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexd", new FuncSpec[]{new FuncSpec(WrapglIndexd.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexdv", new FuncSpec[]{new FuncSpec(WrapglIndexdv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexdv", new FuncSpec[]{new FuncSpec(WrapglIndexdv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexf", new FuncSpec[]{new FuncSpec(WrapglIndexf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexfv", new FuncSpec[]{new FuncSpec(WrapglIndexfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexfv", new FuncSpec[]{new FuncSpec(WrapglIndexfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexi", new FuncSpec[]{new FuncSpec(WrapglIndexi.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexiv", new FuncSpec[]{new FuncSpec(WrapglIndexiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexiv", new FuncSpec[]{new FuncSpec(WrapglIndexiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexs", new FuncSpec[]{new FuncSpec(WrapglIndexs.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexsv", new FuncSpec[]{new FuncSpec(WrapglIndexsv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexsv", new FuncSpec[]{new FuncSpec(WrapglIndexsv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexub", new FuncSpec[]{new FuncSpec(WrapglIndexub.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexubv", new FuncSpec[]{new FuncSpec(WrapglIndexubv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIndexubv", new FuncSpec[]{new FuncSpec(WrapglIndexubv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glInitNames", new FuncSpec[]{new FuncSpec(WrapglInitNames.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIsEnabled", new FuncSpec[]{new FuncSpec(WrapglIsEnabled.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIsList", new FuncSpec[]{new FuncSpec(WrapglIsList.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glIsTexture", new FuncSpec[]{new FuncSpec(WrapglIsTexture.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModelf", new FuncSpec[]{new FuncSpec(WrapglLightModelf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModelfv", new FuncSpec[]{new FuncSpec(WrapglLightModelfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModelfv", new FuncSpec[]{new FuncSpec(WrapglLightModelfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModeli", new FuncSpec[]{new FuncSpec(WrapglLightModeli.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModeliv", new FuncSpec[]{new FuncSpec(WrapglLightModeliv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightModeliv", new FuncSpec[]{new FuncSpec(WrapglLightModeliv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightf", new FuncSpec[]{new FuncSpec(WrapglLightf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightfv", new FuncSpec[]{new FuncSpec(WrapglLightfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightfv", new FuncSpec[]{new FuncSpec(WrapglLightfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLighti", new FuncSpec[]{new FuncSpec(WrapglLighti.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightiv", new FuncSpec[]{new FuncSpec(WrapglLightiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLightiv", new FuncSpec[]{new FuncSpec(WrapglLightiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLineStipple", new FuncSpec[]{new FuncSpec(WrapglLineStipple.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLineWidth", new FuncSpec[]{new FuncSpec(WrapglLineWidth.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glListBase", new FuncSpec[]{new FuncSpec(WrapglListBase.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLoadIdentity", new FuncSpec[]{new FuncSpec(WrapglLoadIdentity.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLoadName", new FuncSpec[]{new FuncSpec(WrapglLoadName.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glLogicOp", new FuncSpec[]{new FuncSpec(WrapglLogicOp.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMapGrid1d", new FuncSpec[]{new FuncSpec(WrapglMapGrid1d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMapGrid1f", new FuncSpec[]{new FuncSpec(WrapglMapGrid1f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMapGrid2d", new FuncSpec[]{new FuncSpec(WrapglMapGrid2d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMapGrid2f", new FuncSpec[]{new FuncSpec(WrapglMapGrid2f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMaterialf", new FuncSpec[]{new FuncSpec(WrapglMaterialf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMaterialfv", new FuncSpec[]{new FuncSpec(WrapglMaterialfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMaterialfv", new FuncSpec[]{new FuncSpec(WrapglMaterialfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMateriali", new FuncSpec[]{new FuncSpec(WrapglMateriali.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMaterialiv", new FuncSpec[]{new FuncSpec(WrapglMaterialiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMaterialiv", new FuncSpec[]{new FuncSpec(WrapglMaterialiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glMatrixMode", new FuncSpec[]{new FuncSpec(WrapglMatrixMode.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNewList", new FuncSpec[]{new FuncSpec(WrapglNewList.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3b", new FuncSpec[]{new FuncSpec(WrapglNormal3b.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3bv", new FuncSpec[]{new FuncSpec(WrapglNormal3bv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3bv", new FuncSpec[]{new FuncSpec(WrapglNormal3bv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3d", new FuncSpec[]{new FuncSpec(WrapglNormal3d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3dv", new FuncSpec[]{new FuncSpec(WrapglNormal3dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3dv", new FuncSpec[]{new FuncSpec(WrapglNormal3dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3f", new FuncSpec[]{new FuncSpec(WrapglNormal3f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3fv", new FuncSpec[]{new FuncSpec(WrapglNormal3fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3fv", new FuncSpec[]{new FuncSpec(WrapglNormal3fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3i", new FuncSpec[]{new FuncSpec(WrapglNormal3i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3iv", new FuncSpec[]{new FuncSpec(WrapglNormal3iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3iv", new FuncSpec[]{new FuncSpec(WrapglNormal3iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3s", new FuncSpec[]{new FuncSpec(WrapglNormal3s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3sv", new FuncSpec[]{new FuncSpec(WrapglNormal3sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glNormal3sv", new FuncSpec[]{new FuncSpec(WrapglNormal3sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glOrtho", new FuncSpec[]{new FuncSpec(WrapglOrtho.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPassThrough", new FuncSpec[]{new FuncSpec(WrapglPassThrough.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPixelStoref", new FuncSpec[]{new FuncSpec(WrapglPixelStoref.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPixelStorei", new FuncSpec[]{new FuncSpec(WrapglPixelStorei.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPixelTransferf", new FuncSpec[]{new FuncSpec(WrapglPixelTransferf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPixelTransferi", new FuncSpec[]{new FuncSpec(WrapglPixelTransferi.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPixelZoom", new FuncSpec[]{new FuncSpec(WrapglPixelZoom.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPointSize", new FuncSpec[]{new FuncSpec(WrapglPointSize.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPolygonMode", new FuncSpec[]{new FuncSpec(WrapglPolygonMode.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPolygonOffset", new FuncSpec[]{new FuncSpec(WrapglPolygonOffset.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPopAttrib", new FuncSpec[]{new FuncSpec(WrapglPopAttrib.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPopClientAttrib", new FuncSpec[]{new FuncSpec(WrapglPopClientAttrib.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPopMatrix", new FuncSpec[]{new FuncSpec(WrapglPopMatrix.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPopName", new FuncSpec[]{new FuncSpec(WrapglPopName.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPrioritizeTextures", new FuncSpec[]{new FuncSpec(WrapglPrioritizeTextures.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPushAttrib", new FuncSpec[]{new FuncSpec(WrapglPushAttrib.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPushClientAttrib", new FuncSpec[]{new FuncSpec(WrapglPushClientAttrib.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPushMatrix", new FuncSpec[]{new FuncSpec(WrapglPushMatrix.class,
                new ParamTypeList(new ValType[]{})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glPushName", new FuncSpec[]{new FuncSpec(WrapglPushName.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2d", new FuncSpec[]{new FuncSpec(WrapglRasterPos2d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2f", new FuncSpec[]{new FuncSpec(WrapglRasterPos2f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2i", new FuncSpec[]{new FuncSpec(WrapglRasterPos2i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2s", new FuncSpec[]{new FuncSpec(WrapglRasterPos2s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos2sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos2sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3d", new FuncSpec[]{new FuncSpec(WrapglRasterPos3d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3f", new FuncSpec[]{new FuncSpec(WrapglRasterPos3f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3i", new FuncSpec[]{new FuncSpec(WrapglRasterPos3i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3s", new FuncSpec[]{new FuncSpec(WrapglRasterPos3s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos3sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos3sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4d", new FuncSpec[]{new FuncSpec(WrapglRasterPos4d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4dv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4f", new FuncSpec[]{new FuncSpec(WrapglRasterPos4f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4fv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4i", new FuncSpec[]{new FuncSpec(WrapglRasterPos4i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4iv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4s", new FuncSpec[]{new FuncSpec(WrapglRasterPos4s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRasterPos4sv", new FuncSpec[]{new FuncSpec(WrapglRasterPos4sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glReadBuffer", new FuncSpec[]{new FuncSpec(WrapglReadBuffer.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectd", new FuncSpec[]{new FuncSpec(WrapglRectd.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectdv", new FuncSpec[]{new FuncSpec(WrapglRectdv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectdv", new FuncSpec[]{new FuncSpec(WrapglRectdv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectdv", new FuncSpec[]{new FuncSpec(WrapglRectdv_3.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectdv", new FuncSpec[]{new FuncSpec(WrapglRectdv_4.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectf", new FuncSpec[]{new FuncSpec(WrapglRectf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectfv", new FuncSpec[]{new FuncSpec(WrapglRectfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectfv", new FuncSpec[]{new FuncSpec(WrapglRectfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectfv", new FuncSpec[]{new FuncSpec(WrapglRectfv_3.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectfv", new FuncSpec[]{new FuncSpec(WrapglRectfv_4.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRecti", new FuncSpec[]{new FuncSpec(WrapglRecti.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectiv", new FuncSpec[]{new FuncSpec(WrapglRectiv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectiv", new FuncSpec[]{new FuncSpec(WrapglRectiv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectiv", new FuncSpec[]{new FuncSpec(WrapglRectiv_3.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectiv", new FuncSpec[]{new FuncSpec(WrapglRectiv_4.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRects", new FuncSpec[]{new FuncSpec(WrapglRects.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectsv", new FuncSpec[]{new FuncSpec(WrapglRectsv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectsv", new FuncSpec[]{new FuncSpec(WrapglRectsv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectsv", new FuncSpec[]{new FuncSpec(WrapglRectsv_3.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRectsv", new FuncSpec[]{new FuncSpec(WrapglRectsv_4.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRenderMode", new FuncSpec[]{new FuncSpec(WrapglRenderMode.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, true, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRotated", new FuncSpec[]{new FuncSpec(WrapglRotated.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glRotatef", new FuncSpec[]{new FuncSpec(WrapglRotatef.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glScaled", new FuncSpec[]{new FuncSpec(WrapglScaled.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glScalef", new FuncSpec[]{new FuncSpec(WrapglScalef.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glScissor", new FuncSpec[]{new FuncSpec(WrapglScissor.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glSelectBuffer", new FuncSpec[]{new FuncSpec(WrapglSelectBuffer.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glShadeModel", new FuncSpec[]{new FuncSpec(WrapglShadeModel.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glStencilFunc", new FuncSpec[]{new FuncSpec(WrapglStencilFunc.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glStencilMask", new FuncSpec[]{new FuncSpec(WrapglStencilMask.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glStencilOp", new FuncSpec[]{new FuncSpec(WrapglStencilOp.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1d", new FuncSpec[]{new FuncSpec(WrapglTexCoord1d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1f", new FuncSpec[]{new FuncSpec(WrapglTexCoord1f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1i", new FuncSpec[]{new FuncSpec(WrapglTexCoord1i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1s", new FuncSpec[]{new FuncSpec(WrapglTexCoord1s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord1sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord1sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2d", new FuncSpec[]{new FuncSpec(WrapglTexCoord2d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2f", new FuncSpec[]{new FuncSpec(WrapglTexCoord2f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2i", new FuncSpec[]{new FuncSpec(WrapglTexCoord2i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2s", new FuncSpec[]{new FuncSpec(WrapglTexCoord2s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord2sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord2sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3d", new FuncSpec[]{new FuncSpec(WrapglTexCoord3d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3f", new FuncSpec[]{new FuncSpec(WrapglTexCoord3f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3i", new FuncSpec[]{new FuncSpec(WrapglTexCoord3i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3s", new FuncSpec[]{new FuncSpec(WrapglTexCoord3s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord3sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord3sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4d", new FuncSpec[]{new FuncSpec(WrapglTexCoord4d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4dv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4f", new FuncSpec[]{new FuncSpec(WrapglTexCoord4f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4fv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4i", new FuncSpec[]{new FuncSpec(WrapglTexCoord4i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4iv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4s", new FuncSpec[]{new FuncSpec(WrapglTexCoord4s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexCoord4sv", new FuncSpec[]{new FuncSpec(WrapglTexCoord4sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnvf", new FuncSpec[]{new FuncSpec(WrapglTexEnvf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnvfv", new FuncSpec[]{new FuncSpec(WrapglTexEnvfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnvfv", new FuncSpec[]{new FuncSpec(WrapglTexEnvfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnvi", new FuncSpec[]{new FuncSpec(WrapglTexEnvi.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnviv", new FuncSpec[]{new FuncSpec(WrapglTexEnviv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexEnviv", new FuncSpec[]{new FuncSpec(WrapglTexEnviv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGend", new FuncSpec[]{new FuncSpec(WrapglTexGend.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGendv", new FuncSpec[]{new FuncSpec(WrapglTexGendv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGendv", new FuncSpec[]{new FuncSpec(WrapglTexGendv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGenf", new FuncSpec[]{new FuncSpec(WrapglTexGenf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGenfv", new FuncSpec[]{new FuncSpec(WrapglTexGenfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGenfv", new FuncSpec[]{new FuncSpec(WrapglTexGenfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGeni", new FuncSpec[]{new FuncSpec(WrapglTexGeni.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGeniv", new FuncSpec[]{new FuncSpec(WrapglTexGeniv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexGeniv", new FuncSpec[]{new FuncSpec(WrapglTexGeniv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameterf", new FuncSpec[]{new FuncSpec(WrapglTexParameterf.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameterfv", new FuncSpec[]{new FuncSpec(WrapglTexParameterfv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameterfv", new FuncSpec[]{new FuncSpec(WrapglTexParameterfv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameteri", new FuncSpec[]{new FuncSpec(WrapglTexParameteri.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameteriv", new FuncSpec[]{new FuncSpec(WrapglTexParameteriv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTexParameteriv", new FuncSpec[]{new FuncSpec(WrapglTexParameteriv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTranslated", new FuncSpec[]{new FuncSpec(WrapglTranslated.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glTranslatef", new FuncSpec[]{new FuncSpec(WrapglTranslatef.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2d", new FuncSpec[]{new FuncSpec(WrapglVertex2d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2dv", new FuncSpec[]{new FuncSpec(WrapglVertex2dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2dv", new FuncSpec[]{new FuncSpec(WrapglVertex2dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2f", new FuncSpec[]{new FuncSpec(WrapglVertex2f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2fv", new FuncSpec[]{new FuncSpec(WrapglVertex2fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2fv", new FuncSpec[]{new FuncSpec(WrapglVertex2fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2i", new FuncSpec[]{new FuncSpec(WrapglVertex2i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2iv", new FuncSpec[]{new FuncSpec(WrapglVertex2iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2iv", new FuncSpec[]{new FuncSpec(WrapglVertex2iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2s", new FuncSpec[]{new FuncSpec(WrapglVertex2s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2sv", new FuncSpec[]{new FuncSpec(WrapglVertex2sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex2sv", new FuncSpec[]{new FuncSpec(WrapglVertex2sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3d", new FuncSpec[]{new FuncSpec(WrapglVertex3d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3dv", new FuncSpec[]{new FuncSpec(WrapglVertex3dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3dv", new FuncSpec[]{new FuncSpec(WrapglVertex3dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3f", new FuncSpec[]{new FuncSpec(WrapglVertex3f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3fv", new FuncSpec[]{new FuncSpec(WrapglVertex3fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3fv", new FuncSpec[]{new FuncSpec(WrapglVertex3fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3i", new FuncSpec[]{new FuncSpec(WrapglVertex3i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3iv", new FuncSpec[]{new FuncSpec(WrapglVertex3iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3iv", new FuncSpec[]{new FuncSpec(WrapglVertex3iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3s", new FuncSpec[]{new FuncSpec(WrapglVertex3s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3sv", new FuncSpec[]{new FuncSpec(WrapglVertex3sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex3sv", new FuncSpec[]{new FuncSpec(WrapglVertex3sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4d", new FuncSpec[]{new FuncSpec(WrapglVertex4d.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4dv", new FuncSpec[]{new FuncSpec(WrapglVertex4dv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4dv", new FuncSpec[]{new FuncSpec(WrapglVertex4dv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4f", new FuncSpec[]{new FuncSpec(WrapglVertex4f.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL), new ValType(ValType.VTP_REAL)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4fv", new FuncSpec[]{new FuncSpec(WrapglVertex4fv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4fv", new FuncSpec[]{new FuncSpec(WrapglVertex4fv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_REAL, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4i", new FuncSpec[]{new FuncSpec(WrapglVertex4i.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4iv", new FuncSpec[]{new FuncSpec(WrapglVertex4iv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4iv", new FuncSpec[]{new FuncSpec(WrapglVertex4iv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4s", new FuncSpec[]{new FuncSpec(WrapglVertex4s.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4sv", new FuncSpec[]{new FuncSpec(WrapglVertex4sv.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 1, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glVertex4sv", new FuncSpec[]{new FuncSpec(WrapglVertex4sv_2.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT, (byte) 0, (byte) 1, true)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});
        s.put("glViewport", new FuncSpec[]{new FuncSpec(WrapglViewport.class,
                new ParamTypeList(new ValType[]{new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT), new ValType(ValType.VTP_INT)})
                , true, false, new ValType(ValType.VTP_INT), false, false, null)});

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
    public void setContext(Object context) {
        if (context instanceof GL2)
            gl = (GL2) context;
    }


    public final class WrapglAccum implements Function {
        public void run(TomVM vm) {
            gl.glAccum(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglAlphaFunc implements Function {
        public void run(TomVM vm) {
            gl.glAlphaFunc(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglAreTexturesResident implements Function {

        public void run(TomVM vm) {
            if (!Routines.ValidateSizeParam(vm, 3)) return;
            IntBuffer a1 = IntBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));
            ByteBuffer a2 = ByteBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), vm.GetIntParam(3));
            vm.Reg().setIntVal(gl.glAreTexturesResident(vm.GetIntParam(3), a1, a2) ? 1 : 0);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), vm.GetIntParam(3));
        }
    }

    public final class WrapglArrayElement implements Function {

        public void run(TomVM vm) {
            gl.glArrayElement(vm.GetIntParam(1));
        }
    }

    public final class WrapglBindTexture implements Function
    {

        public void run (TomVM vm){
            gl.glBindTexture(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglBlendFunc implements Function {

        public void run(TomVM vm) {

            gl.glBlendFunc(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCallList implements Function {

        public void run(TomVM vm) {
            gl.glCallList(vm.GetIntParam(1));
        }
    }

    public final class WrapglClear implements Function {

        public void run(TomVM vm) {
            gl.glClear(vm.GetIntParam(1));
        }
    }

    public final class WrapglClearAccum implements Function {

        public void run(TomVM vm) {
            gl.glClearAccum(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglClearColor implements Function {

        public void run(TomVM vm) {
            gl.glClearColor(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglClearDepth implements Function {

        public void run(TomVM vm) {
            gl.glClearDepth(vm.GetRealParam(1));
        }
    }

    public final class WrapglClearIndex implements Function {

        public void run(TomVM vm) {
            gl.glClearIndex(vm.GetRealParam(1));
        }
    }

    public final class WrapglClearStencil implements Function
    {

        public void run (TomVM vm){
            gl.glClearStencil(vm.GetIntParam(1));
        }
    }

    public final class WrapglClipPlane implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glClipPlane(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglClipPlane_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glClipPlane(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal((float)a1.get(0));
        }
    }

    public final class WrapglColor3b implements Function
    {

        public void run (TomVM vm){
            gl.glColor3b(vm.GetIntParam(3).byteValue(), vm.GetIntParam(2).byteValue(), vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglColor3bv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3bv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3bv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glColor3bv(a1);
            vm.GetRefParam(1).setIntVal((int) a1.get(0));
        }
    }

    public final class WrapglColor3d implements Function
    {

        public void run (TomVM vm){
            gl.glColor3d(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglColor3dv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3dv_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glColor3dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglColor3f implements Function
    {

        public void run (TomVM vm){
            gl.glColor3f(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglColor3fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glColor3fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglColor3i implements Function
    {

        public void run (TomVM vm){
            gl.glColor3i(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglColor3iv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3iv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glColor3iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglColor3s implements Function
    {

        public void run (TomVM vm){
            gl.glColor3s(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglColor3sv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3sv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glColor3sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColor3ub implements Function
    {

        public void run (TomVM vm){
            gl.glColor3ub(vm.GetIntParam(3).byteValue(), vm.GetIntParam(2).byteValue(), vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglColor3ubv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3ubv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3ubv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glColor3ubv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColor3ui implements Function
    {

        public void run (TomVM vm){
            gl.glColor3ui(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglColor3uiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3uiv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3uiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glColor3uiv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglColor3us implements Function
    {

        public void run (TomVM vm){
            gl.glColor3us(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglColor3usv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor3usv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor3usv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glColor3usv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColor4b implements Function
    {

        public void run (TomVM vm){
            gl.glColor4b(vm.GetIntParam(4).byteValue(), vm.GetIntParam(3).byteValue(), vm.GetIntParam(2).byteValue(), vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglColor4bv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4bv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4bv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glColor4bv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColor4d implements Function
    {

        public void run (TomVM vm){
            gl.glColor4d(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglColor4dv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4dv_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glColor4dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglColor4f implements Function
    {

        public void run (TomVM vm){
            gl.glColor4f(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglColor4fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glColor4fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglColor4i implements Function
    {

        public void run (TomVM vm){
            gl.glColor4i(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglColor4iv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4iv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glColor4iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglColor4s implements Function
    {

        public void run (TomVM vm){
            gl.glColor4s(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglColor4sv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4sv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glColor4sv(a1);
            vm.GetRefParam(1).setIntVal((int) a1.get(0));
        }
    }

    public final class WrapglColor4ub implements Function
    {

        public void run (TomVM vm){
            gl.glColor4ub(vm.GetIntParam(4).byteValue(), vm.GetIntParam(3).byteValue(), vm.GetIntParam(2).byteValue(), vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglColor4ubv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4ubv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4ubv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glColor4ubv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColor4ui implements Function
    {

        public void run (TomVM vm){
            gl.glColor4ui(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglColor4uiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4uiv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4uiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glColor4uiv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglColor4us implements Function
    {

        public void run (TomVM vm){
            gl.glColor4us(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglColor4usv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glColor4usv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglColor4usv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glColor4usv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglColorMask implements Function
    {

        public void run (TomVM vm){
            gl.glColorMask(vm.GetIntParam(4) == 1, vm.GetIntParam(3) == 1, vm.GetIntParam(2) == 1, vm.GetIntParam(1) == 1);
        }
    }

    public final class WrapglColorMaterial implements Function
    {

        public void run (TomVM vm){
            gl.glColorMaterial(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCopyPixels implements Function
    {

        public void run (TomVM vm){
            gl.glCopyPixels(vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCopyTexImage1D implements Function
    {

        public void run (TomVM vm){
            gl.glCopyTexImage1D(vm.GetIntParam(7), vm.GetIntParam(6), vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCopyTexImage2D implements Function
    {

        public void run (TomVM vm){
            gl.glCopyTexImage2D(vm.GetIntParam(8), vm.GetIntParam(7), vm.GetIntParam(6), vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCopyTexSubImage1D implements Function
    {

        public void run (TomVM vm){
            gl.glCopyTexSubImage1D(vm.GetIntParam(6), vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCopyTexSubImage2D implements Function
    {

        public void run (TomVM vm){
            gl.glCopyTexSubImage2D(vm.GetIntParam(8), vm.GetIntParam(7), vm.GetIntParam(6), vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglCullFace implements Function
    {

        public void run (TomVM vm){
            gl.glCullFace(vm.GetIntParam(1));
        }
    }

    public final class WrapglDepthFunc implements Function
    {

        public void run (TomVM vm){
            gl.glDepthFunc(vm.GetIntParam(1));
        }
    }

    public final class WrapglDepthMask implements Function
    {

        public void run (TomVM vm){
            gl.glDepthMask(vm.GetIntParam(1) == 1);
        }
    }

    public final class WrapglDepthRange implements Function
    {

        public void run (TomVM vm){
            gl.glDepthRange(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglDisable implements Function
    {

        public void run (TomVM vm){
            gl.glDisable(vm.GetIntParam(1));
        }
    }

    public final class WrapglDisableClientState implements Function
    {

        public void run (TomVM vm){
            gl.glDisableClientState(vm.GetIntParam(1));
        }
    }

    public final class WrapglDrawArrays implements Function
    {

        public void run (TomVM vm){
            if (!Routines.ValidateSizeParam(vm, 1)) return;
            gl.glDrawArrays(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglDrawBuffer implements Function
    {

        public void run (TomVM vm){

            gl.glDrawBuffer(vm.GetIntParam(1));
        }
    }

    public final class WrapglEdgeFlag implements Function
    {

        public void run (TomVM vm){
            gl.glEdgeFlag(vm.GetIntParam(1) == 1);
        }
    }

    public final class WrapglEdgeFlagv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glEdgeFlagv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglEdgeFlagv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glEdgeFlagv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglEnable implements Function
    {

        public void run (TomVM vm){
            gl.glEnable(vm.GetIntParam(1));
        }
    }

    public final class WrapglEnableClientState implements Function
    {

        public void run (TomVM vm){
            gl.glEnableClientState(vm.GetIntParam(1));
        }
    }

    public final class WrapglEndList implements Function
    {

        public void run (TomVM vm){
            gl.glEndList();
        }
    }

    public final class WrapglEvalCoord1d implements Function
    {

        public void run (TomVM vm){
            gl.glEvalCoord1d(vm.GetRealParam(1));
        }
    }

    public final class WrapglEvalCoord1dv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glEvalCoord1dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglEvalCoord1dv_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glEvalCoord1dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglEvalCoord1f implements Function
    {

        public void run (TomVM vm){
            gl.glEvalCoord1f(vm.GetRealParam(1));
        }
    }

    public final class WrapglEvalCoord1fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glEvalCoord1fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglEvalCoord1fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glEvalCoord1fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglEvalCoord2d implements Function
    {

        public void run (TomVM vm){
            gl.glEvalCoord2d(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglEvalCoord2dv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glEvalCoord2dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglEvalCoord2dv_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glEvalCoord2dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglEvalCoord2f implements Function
    {

        public void run (TomVM vm){
            gl.glEvalCoord2f(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglEvalCoord2fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glEvalCoord2fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglEvalCoord2fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glEvalCoord2fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglEvalMesh1 implements Function
    {

        public void run (TomVM vm){
            gl.glEvalMesh1(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglEvalMesh2 implements Function
    {

        public void run (TomVM vm){
            gl.glEvalMesh2(vm.GetIntParam(5), vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglEvalPoint1 implements Function
    {

        public void run (TomVM vm){
            gl.glEvalPoint1(vm.GetIntParam(1));
        }
    }

    public final class WrapglEvalPoint2 implements Function
    {

        public void run (TomVM vm){
            gl.glEvalPoint2(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglFeedbackBuffer implements Function
    {

        public void run (TomVM vm){
            if (!Routines.ValidateSizeParam(vm, 3)) return;
            FloatBuffer a1 = FloatBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));
            gl.glFeedbackBuffer(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));
        }
    }

    public final class WrapglFinish implements Function
    {

        public void run (TomVM vm){
            gl.glFinish();
        }
    }

    public final class WrapglFlush implements Function
    {

        public void run (TomVM vm){
            gl.glFlush();
        }
    }

    public final class WrapglFogf implements Function
    {

        public void run (TomVM vm){
            gl.glFogf(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglFogfv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glFogfv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglFogfv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glFogfv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglFogi implements Function
    {

        public void run (TomVM vm){
            gl.glFogi(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglFogiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glFogiv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglFogiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glFogiv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglFrontFace implements Function
    {

        public void run (TomVM vm){
            gl.glFrontFace(vm.GetIntParam(1));
        }
    }

    public final class WrapglFrustum implements Function
    {
        public void run (TomVM vm){
            gl.glFrustum(vm.GetRealParam(6), vm.GetRealParam(5), vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglGetBooleanv implements Function
    {
        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetBooleanv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetBooleanv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glGetBooleanv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglGetClipPlane implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetClipPlane(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetClipPlane_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetClipPlane(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglGetDoublev implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetDoublev(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetDoublev_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetDoublev(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglGetError implements Function
    {

        public void run (TomVM vm){
            vm.Reg().setIntVal(gl.glGetError());
        }
    }

    public final class WrapglGetFloatv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetFloatv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetFloatv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetFloatv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetIntegerv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetIntegerv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetIntegerv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetIntegerv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetLightfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetLightfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetLightfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetLightfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetLightiv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetLightiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetLightiv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetLightiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetMaterialfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetMaterialfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetMaterialfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetMaterialfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetMaterialiv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetMaterialiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetMaterialiv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetMaterialiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetPixelMapuiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetPixelMapuiv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetPixelMapuiv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetPixelMapuiv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetTexEnvfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexEnvfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexEnvfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetTexEnvfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetTexEnviv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexEnviv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexEnviv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetTexEnviv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetTexGendv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexGendv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexGendv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetTexGendv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglGetTexGenfv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexGenfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexGenfv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetTexGenfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetTexGeniv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexGeniv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexGeniv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetTexGeniv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetTexLevelParameterfv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), a1.array(), 16);
            gl.glGetTexLevelParameterfv(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte) 1, (byte) 1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexLevelParameterfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetTexLevelParameterfv(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetTexLevelParameteriv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexLevelParameteriv(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexLevelParameteriv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetTexLevelParameteriv(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglGetTexParameterfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexParameterfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexParameterfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glGetTexParameterfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglGetTexParameteriv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glGetTexParameteriv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglGetTexParameteriv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glGetTexParameteriv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglHint implements Function
    {

        public void run (TomVM vm){
            gl.glHint(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglIndexMask implements Function
    {
        public void run (TomVM vm){
            gl.glIndexMask(vm.GetIntParam(1));
        }
    }

    public final class WrapglIndexd implements Function
    {
        public void run (TomVM vm){
            gl.glIndexd(vm.GetRealParam(1));
        }
    }

    public final class WrapglIndexdv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glIndexdv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglIndexdv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glIndexdv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglIndexf implements Function
    {
        public void run (TomVM vm){
            gl.glIndexf(vm.GetRealParam(1));
        }
    }

    public final class WrapglIndexfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glIndexfv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglIndexfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glIndexfv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglIndexi implements Function
    {
        public void run (TomVM vm){
            gl.glIndexi(vm.GetIntParam(1));
        }
    }

    public final class WrapglIndexiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glIndexiv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglIndexiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glIndexiv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglIndexs implements Function
    {
        public void run (TomVM vm){
            gl.glIndexs(vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglIndexsv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glIndexsv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglIndexsv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glIndexsv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglIndexub implements Function
    {
        public void run (TomVM vm){
            gl.glIndexub(vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglIndexubv implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glIndexubv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglIndexubv_2 implements Function
    {

        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glIndexubv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglInitNames implements Function
    {

        public void run (TomVM vm){
            gl.glInitNames();

        }
    }

    public final class WrapglIsEnabled implements Function
    {
        public void run (TomVM vm){
            vm.Reg().setIntVal(gl.glIsEnabled(vm.GetIntParam(1))? 1 : 0);
        }

    }

    public final class WrapglIsList implements Function
    {
        public void run (TomVM vm){
            vm.Reg().setIntVal(gl.glIsList(vm.GetIntParam(1))? 1:0);
        }
    }

    public final class WrapglIsTexture implements Function
    {
        public void run (TomVM vm){
            vm.Reg().setIntVal( gl.glIsTexture(vm.GetIntParam(1))? 1:0);
        }
    }

    public final class WrapglLightModelf implements Function
    {
        public void run (TomVM vm){
            gl.glLightModelf(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglLightModelfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glLightModelfv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglLightModelfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glLightModelfv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglLightModeli implements Function
    {
        public void run (TomVM vm){
            gl.glLightModeli(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglLightModeliv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glLightModeliv(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglLightModeliv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glLightModeliv(vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglLightf implements Function
    {
        public void run (TomVM vm){
            gl.glLightf(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglLightfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glLightfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglLightfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glLightfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglLighti implements Function
    {
        public void run (TomVM vm){
            gl.glLighti(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglLightiv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glLightiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglLightiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glLightiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglLineStipple implements Function
    {

        public void run (TomVM vm){
            gl.glLineStipple(vm.GetIntParam(2), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglLineWidth implements Function
    {

        public void run (TomVM vm){
            gl.glLineWidth(vm.GetRealParam(1));
        }
    }

    public final class WrapglListBase implements Function
    {

        public void run (TomVM vm){
            gl.glListBase(vm.GetIntParam(1));
        }
    }

    public final class WrapglLoadIdentity implements Function
    {
        public void run (TomVM vm){
            gl.glLoadIdentity();
        }
    }

    public final class WrapglLoadName implements Function
    {
        public void run (TomVM vm){
            gl.glLoadName(vm.GetIntParam(1));
        }

    }

    public final class WrapglLogicOp implements Function
    {

        public void run (TomVM vm){
            gl.glLogicOp(vm.GetIntParam(1));
        }
    }

    public final class WrapglMapGrid1d implements Function
    {

        public void run (TomVM vm){
            gl.glMapGrid1d(vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMapGrid1f implements Function
    {
        public void run (TomVM vm){
            gl.glMapGrid1f(vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMapGrid2d implements Function
    {

        public void run (TomVM vm){
            gl.glMapGrid2d(vm.GetIntParam(6), vm.GetRealParam(5), vm.GetRealParam(4), vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMapGrid2f implements Function
    {

        public void run (TomVM vm){
            gl.glMapGrid2f(vm.GetIntParam(6), vm.GetRealParam(5), vm.GetRealParam(4), vm.GetIntParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMaterialf implements Function
    {

        public void run (TomVM vm){
            gl.glMaterialf(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglMaterialfv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glMaterialfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglMaterialfv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glMaterialfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglMateriali implements Function
    {

        public void run (TomVM vm){
            gl.glMateriali(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglMaterialiv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glMaterialiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglMaterialiv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glMaterialiv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglMatrixMode implements Function
    {
        public void run (TomVM vm){
            gl.glMatrixMode(vm.GetIntParam(1));
        }
    }

    public final class WrapglNewList implements Function
    {
        public void run (TomVM vm){
            gl.glNewList(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglNormal3b implements Function
    {
        public void run (TomVM vm){
            gl.glNormal3b(vm.GetIntParam(3).byteValue(), vm.GetIntParam(2).byteValue(), vm.GetIntParam(1).byteValue());
        }
    }

    public final class WrapglNormal3bv implements Function
    {
        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glNormal3bv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglNormal3bv_2 implements Function
    {
        public void run (TomVM vm){
            ByteBuffer a1 = ByteBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glNormal3bv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglNormal3d implements Function
    {
        public void run (TomVM vm){
            gl.glNormal3d(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglNormal3dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glNormal3dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglNormal3dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glNormal3dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglNormal3f implements Function
    {
        public void run (TomVM vm){
            gl.glNormal3f(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglNormal3fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glNormal3fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglNormal3fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glNormal3fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglNormal3i implements Function
    {
        public void run (TomVM vm){
            gl.glNormal3i(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglNormal3iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glNormal3iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglNormal3iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glNormal3iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglNormal3s implements Function
    {
        public void run (TomVM vm){
            gl.glNormal3s(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglNormal3sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glNormal3sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglNormal3sv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short) vm.GetRefParam(1).getIntVal());
            gl.glNormal3sv(a1);
            vm.GetRefParam(1).setIntVal((int) a1.get(0));
        }
    }

    public final class WrapglOrtho implements Function
    {
        public void run (TomVM vm){
            gl.glOrtho(vm.GetRealParam(6), vm.GetRealParam(5), vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglPassThrough implements Function
    {
        public void run (TomVM vm){
            gl.glPassThrough(vm.GetRealParam(1));
        }
    }

    public final class WrapglPixelStoref implements Function
    {

        public void run (TomVM vm){
            gl.glPixelStoref(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglPixelStorei implements Function
    {

        public void run (TomVM vm){
            gl.glPixelStorei(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglPixelTransferf implements Function
    {

        public void run (TomVM vm){
            gl.glPixelTransferf(vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglPixelTransferi implements Function
    {

        public void run (TomVM vm){
            gl.glPixelTransferi(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglPixelZoom implements Function
    {
        public void run (TomVM vm){
            gl.glPixelZoom(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglPointSize implements Function
    {

        public void run (TomVM vm){
            gl.glPointSize(vm.GetRealParam(1));
        }
    }

    public final class WrapglPolygonMode implements Function
    {
        public void run (TomVM vm){
            gl.glPolygonMode(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglPolygonOffset implements Function
    {

        public void run (TomVM vm){
            gl.glPolygonOffset(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglPopAttrib implements Function
    {

        public void run (TomVM vm){
            gl.glPopAttrib();
        }
    }

    public final class WrapglPopClientAttrib implements Function
    {

        public void run (TomVM vm){
            gl.glPopClientAttrib();
        }
    }

    public final class WrapglPopMatrix implements Function
    {
        public void run (TomVM vm){
            gl.glPopMatrix();
        }
    }

    public final class WrapglPopName implements Function
    {
        public void run (TomVM vm){
            gl.glPopName();
        }
    }

    public final class WrapglPrioritizeTextures implements Function
    {
        public void run (TomVM vm){
            if (!Routines.ValidateSizeParam(vm, 3)) return;
            IntBuffer a1 = IntBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));
            FloatBuffer a2 = FloatBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), vm.GetIntParam(3));
            gl.glPrioritizeTextures(vm.GetIntParam(3), a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(3));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), vm.GetIntParam(3));
        }
    }

    public final class WrapglPushAttrib implements Function
    {
        public void run (TomVM vm){
            gl.glPushAttrib(vm.GetIntParam(1));
        }
    }

    public final class WrapglPushClientAttrib implements Function
    {

        public void run (TomVM vm){
            gl.glPushClientAttrib(vm.GetIntParam(1));
        }
    }

    public final class WrapglPushMatrix implements Function
    {
        public void run (TomVM vm){
            gl.glPushMatrix();
        }
    }

    public final class WrapglPushName implements Function
    {
        public void run (TomVM vm){
            gl.glPushName(vm.GetIntParam(1));
        }
    }

    public final class WrapglRasterPos2d implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos2d(vm.GetRealParam(2), vm.GetRealParam(1));
        }

    }

    public final class WrapglRasterPos2dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos2dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos2dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos2dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglRasterPos2f implements Function
    {

        public void run (TomVM vm){
            gl.glRasterPos2f(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRasterPos2fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos2fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos2fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos2fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos2i implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos2i(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglRasterPos2iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos2iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos2iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glRasterPos2iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos2s implements Function
    {

        public void run (TomVM vm){
            gl.glRasterPos2s(vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglRasterPos2sv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos2sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos2sv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glRasterPos2sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglRasterPos3d implements Function
    {

        public void run (TomVM vm){
            gl.glRasterPos3d(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRasterPos3dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos3dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos3dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos3dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglRasterPos3f implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos3f(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRasterPos3fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos3fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos3fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos3fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos3i implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos3i(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglRasterPos3iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos3iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos3iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glRasterPos3iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos3s implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos3s(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglRasterPos3sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos3sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos3sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glRasterPos3sv(a1);
            vm.GetRefParam(1).setIntVal((int) a1.get(0));
        }
    }

    public final class WrapglRasterPos4d implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos4d(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRasterPos4dv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos4dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos4dv_2 implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos4dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglRasterPos4f implements Function
    {

        public void run (TomVM vm){
            gl.glRasterPos4f(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRasterPos4fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos4fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos4fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRasterPos4fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos4i implements Function
    {
        public void run (TomVM vm){
            gl.glRasterPos4i(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }

    }

    public final class WrapglRasterPos4iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos4iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos4iv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glRasterPos4iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglRasterPos4s implements Function
    {

        public void run (TomVM vm){
            gl.glRasterPos4s(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglRasterPos4sv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glRasterPos4sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglRasterPos4sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short) vm.GetRefParam(1).getIntVal());
            gl.glRasterPos4sv(a1);
            vm.GetRefParam(1).setIntVal((int) a1.get(0));
        }
    }

    public final class WrapglReadBuffer implements Function
    {
        public void run (TomVM vm){
            gl.glReadBuffer(vm.GetIntParam(1));
        }
    }

    public final class WrapglRectd implements Function
    {

        public void run (TomVM vm){
            gl.glRectd(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRectdv implements Function
    {

        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            DoubleBuffer a2 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectdv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectdv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getRealVal());
            DoubleBuffer a2 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectdv(a1, a2);
            vm.GetRefParam(2).setRealVal( (float) a1.get(0));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectdv_3 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            DoubleBuffer a2 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, vm.GetRefParam(1).getRealVal());
            gl.glRectdv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);

            vm.GetRefParam(1).setRealVal( (float) a2.get(0));
        }
    }

    public final class WrapglRectdv_4 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getRealVal());
            DoubleBuffer a2 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, vm.GetRefParam(1).getRealVal());
            gl.glRectdv(a1, a2);
            vm.GetRefParam(2).setRealVal( (float) a1.get(0));

            vm.GetRefParam(1).setRealVal( (float) a2.get(0));
        }
    }

    public final class WrapglRectf implements Function
    {

        public void run (TomVM vm){
            gl.glRectf(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRectfv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            FloatBuffer a2 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectfv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getRealVal());
            FloatBuffer a2 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectfv(a1, a2);
            vm.GetRefParam(2).setRealVal( a1.get(0));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectfv_3 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            FloatBuffer a2 = FloatBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, vm.GetRefParam(1).getRealVal());
            gl.glRectfv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);

            vm.GetRefParam(1).setRealVal( a2.get(0));
        }
    }

    public final class WrapglRectfv_4 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getRealVal());
            FloatBuffer a2 = FloatBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put(0, vm.GetRefParam(1).getRealVal());
            gl.glRectfv(a1, a2);
            vm.GetRefParam(2).setRealVal( a1.get(0));

            vm.GetRefParam(1).setRealVal( a2.get(0));
        }
    }

    public final class WrapglRecti implements Function
    {
        public void run (TomVM vm){
            gl.glRecti(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglRectiv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            IntBuffer a2 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectiv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectiv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getIntVal());
            IntBuffer a2 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectiv(a1, a2);
            vm.GetRefParam(2).setIntVal(a1.get(0));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectiv_3 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            IntBuffer a2 = IntBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put(0, vm.GetRefParam(1).getIntVal());
            gl.glRectiv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);

            vm.GetRefParam(1).setIntVal(a2.get(0));
        }
    }

    public final class WrapglRectiv_4 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, vm.GetRefParam(2).getIntVal());
            IntBuffer a2 = IntBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, vm.GetRefParam(1).getIntVal());
            gl.glRectiv(a1, a2);
            vm.GetRefParam(2).setIntVal(a1.get(0));

            vm.GetRefParam(1).setIntVal(a2.get(0));
        }
    }

    public final class WrapglRects implements Function
    {
        public void run (TomVM vm){
            gl.glRects(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglRectsv implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            ShortBuffer a2 = ShortBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectsv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectsv_2 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, (short)vm.GetRefParam(2).getIntVal());
            ShortBuffer a2 = ShortBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
            gl.glRectsv(a1, a2);
            vm.GetRefParam(2).setIntVal((int) a1.get(0));

            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a2.array(), 16);
        }
    }

    public final class WrapglRectsv_3 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            ShortBuffer a2 = ShortBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, (short)vm.GetRefParam(1).getIntVal());
            gl.glRectsv(a1, a2);
            Data.WriteArray(vm.Data(), vm.GetIntParam(2), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);

            vm.GetRefParam(1).setIntVal((int)a2.get(0));
        }
    }

    public final class WrapglRectsv_4 implements Function
    {

        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(2)) return;
            a1.put(0, (short) vm.GetRefParam(2).getIntVal());
            ShortBuffer a2 = ShortBuffer.allocate(16);
            Data.ZeroArray(a2.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a2.put( 0, (short) vm.GetRefParam(1).getIntVal());
            gl.glRectsv(a1, a2);
            vm.GetRefParam(2).setIntVal((int)a1.get(0));

            vm.GetRefParam(1).setIntVal((int)a2.get(0));
        }
    }

    public final class WrapglRenderMode implements Function
    {
        public void run (TomVM vm){
            vm.Reg().setIntVal(gl.glRenderMode(vm.GetIntParam(1)));
        }
    }

    public final class WrapglRotated implements Function
    {
        public void run (TomVM vm){
            gl.glRotated(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglRotatef implements Function
    {
        public void run (TomVM vm){
            gl.glRotatef(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglScaled implements Function
    {
        public void run (TomVM vm){
            gl.glScaled(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglScalef implements Function
    {
        public void run (TomVM vm){
            gl.glScalef(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglScissor implements Function
    {
        public void run (TomVM vm){
            gl.glScissor(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglSelectBuffer implements Function
    {
        public void run (TomVM vm){
            if (!Routines.ValidateSizeParam(vm, 2)) return;
            IntBuffer a1 = IntBuffer.allocate(65536);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(2));
            gl.glSelectBuffer(vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), vm.GetIntParam(2));
        }
    }

    public final class WrapglShadeModel implements Function
    {
        public void run (TomVM vm){
            gl.glShadeModel(vm.GetIntParam(1));
        }
    }

    public final class WrapglStencilFunc implements Function
    {
        public void run (TomVM vm){
            gl.glStencilFunc(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglStencilMask implements Function
    {

        public void run (TomVM vm){
            gl.glStencilMask(vm.GetIntParam(1));
        }
    }

    public final class WrapglStencilOp implements Function
    {
        public void run (TomVM vm){
            gl.glStencilOp(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexCoord1d implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord1d(vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord1dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord1dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType(ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord1dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord1dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglTexCoord1f implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord1f(vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord1fv implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord1fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord1fv_2 implements Function
    {

        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord1fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord1i implements Function
    {

        public void run (TomVM vm){
            gl.glTexCoord1i(vm.GetIntParam(1));
        }
    }

    public final class WrapglTexCoord1iv implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord1iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord1iv_2 implements Function
    {

        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexCoord1iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord1s implements Function
    {

        public void run (TomVM vm){
            gl.glTexCoord1s(vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglTexCoord1sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord1sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord1sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short) vm.GetRefParam(1).getIntVal());
            gl.glTexCoord1sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglTexCoord2d implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord2d(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord2dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord2dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord2dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord2dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglTexCoord2f implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord2f(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord2fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord2fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord2fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord2fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord2i implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord2i(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexCoord2iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord2iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord2iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexCoord2iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord2s implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord2s(vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglTexCoord2sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord2sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord2sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glTexCoord2sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglTexCoord3d implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord3d(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord3dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord3dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord3dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord3dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglTexCoord3f implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord3f(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord3fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord3fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord3fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord3fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord3i implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord3i(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexCoord3iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord3iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord3iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexCoord3iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord3s implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord3s(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglTexCoord3sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord3sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord3sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glTexCoord3sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglTexCoord4d implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord4d(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord4dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord4dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord4dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord4dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglTexCoord4f implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord4f(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexCoord4fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord4fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord4fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexCoord4fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord4i implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord4i(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexCoord4iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord4iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord4iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexCoord4iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexCoord4s implements Function
    {
        public void run (TomVM vm){
            gl.glTexCoord4s(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglTexCoord4sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexCoord4sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexCoord4sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (byte)vm.GetRefParam(1).getIntVal());
            gl.glTexCoord4sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglTexEnvf implements Function
    {
        public void run (TomVM vm){
            gl.glTexEnvf(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexEnvfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexEnvfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexEnvfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexEnvfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexEnvi implements Function
    {
        public void run (TomVM vm){
            gl.glTexEnvi(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexEnviv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexEnviv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexEnviv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexEnviv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexGend implements Function
    {
        public void run (TomVM vm){
            gl.glTexGend(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexGendv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexGendv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexGendv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexGendv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglTexGenf implements Function
    {
        public void run (TomVM vm){
            gl.glTexGenf(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexGenfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexGenfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexGenfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexGenfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexGeni implements Function
    {
        public void run (TomVM vm){
            gl.glTexGeni(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexGeniv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexGeniv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexGeniv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexGeniv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTexParameterf implements Function
    {
        public void run (TomVM vm){
            gl.glTexParameterf(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTexParameterfv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexParameterfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexParameterfv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glTexParameterfv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglTexParameteri implements Function
    {
        public void run (TomVM vm){
            gl.glTexParameteri(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglTexParameteriv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glTexParameteriv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglTexParameteriv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glTexParameteriv(vm.GetIntParam(3), vm.GetIntParam(2), a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglTranslated implements Function
    {
        public void run (TomVM vm){
            gl.glTranslated(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglTranslatef implements Function
    {
        public void run (TomVM vm){
            gl.glTranslatef(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex2d implements Function
    {
        public void run (TomVM vm){
            gl.glVertex2d(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex2dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex2dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex2dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex2dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglVertex2f implements Function
    {
        public void run (TomVM vm){
            gl.glVertex2f(vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex2fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex2fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex2fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex2fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglVertex2i implements Function
    {
        public void run (TomVM vm){
            gl.glVertex2i(vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglVertex2iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex2iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex2iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glVertex2iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglVertex2s implements Function
    {
        public void run (TomVM vm){
            gl.glVertex2s(vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglVertex2sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex2sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex2sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short)vm.GetRefParam(1).getIntVal());
            gl.glVertex2sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglVertex3d implements Function
    {
        public void run (TomVM vm){
            gl.glVertex3d(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex3dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex3dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex3dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex3dv(a1);
            vm.GetRefParam(1).setRealVal( (float) a1.get(0));
        }
    }

    public final class WrapglVertex3f implements Function
    {
        public void run (TomVM vm){
            gl.glVertex3f(vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex3fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex3fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex3fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex3fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglVertex3i implements Function
    {
        public void run (TomVM vm){
            gl.glVertex3i(vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglVertex3iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex3iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex3iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glVertex3iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglVertex3s implements Function
    {
        public void run (TomVM vm){
            gl.glVertex3s(vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglVertex3sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex3sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex3sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short) vm.GetRefParam(1).getIntVal());
            gl.glVertex3sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglVertex4d implements Function
    {
        public void run (TomVM vm){
            gl.glVertex4d(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex4dv implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex4dv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex4dv_2 implements Function
    {
        public void run (TomVM vm){
            DoubleBuffer a1 = DoubleBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex4dv(a1);
            vm.GetRefParam(1).setRealVal( (float)a1.get(0));
        }
    }

    public final class WrapglVertex4f implements Function
    {
        public void run (TomVM vm){
            gl.glVertex4f(vm.GetRealParam(4), vm.GetRealParam(3), vm.GetRealParam(2), vm.GetRealParam(1));
        }
    }

    public final class WrapglVertex4fv implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex4fv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_REAL, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex4fv_2 implements Function
    {
        public void run (TomVM vm){
            FloatBuffer a1 = FloatBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getRealVal());
            gl.glVertex4fv(a1);
            vm.GetRefParam(1).setRealVal(a1.get(0));
        }
    }

    public final class WrapglVertex4i implements Function
    {
        public void run (TomVM vm){
            gl.glVertex4i(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }

    public final class WrapglVertex4iv implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex4iv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex4iv_2 implements Function
    {
        public void run (TomVM vm){
            IntBuffer a1 = IntBuffer.allocate(16);
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, vm.GetRefParam(1).getIntVal());
            gl.glVertex4iv(a1);
            vm.GetRefParam(1).setIntVal(a1.get(0));
        }
    }

    public final class WrapglVertex4s implements Function
    {
        public void run (TomVM vm){
            gl.glVertex4s(vm.GetIntParam(4).shortValue(), vm.GetIntParam(3).shortValue(), vm.GetIntParam(2).shortValue(), vm.GetIntParam(1).shortValue());
        }
    }

    public final class WrapglVertex4sv implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ReadAndZero(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
            gl.glVertex4sv(a1);
            Data.WriteArray(vm.Data(), vm.GetIntParam(1), new ValType( ValType.VTP_INT, (byte)1, (byte)1, true), a1.array(), 16);
        }
    }

    public final class WrapglVertex4sv_2 implements Function
    {
        public void run (TomVM vm){
            ShortBuffer a1 = ShortBuffer.allocate(16) ;
            Data.ZeroArray(a1.array(), 16);
            if (!vm.CheckNullRefParam(1)) return;
            a1.put(0, (short) vm.GetRefParam(1).getIntVal());
            gl.glVertex4sv(a1);
            vm.GetRefParam(1).setIntVal((int)a1.get(0));
        }
    }

    public final class WrapglViewport implements Function
    {
        public WrapglViewport() {
        }
        public void run (TomVM vm){
            gl.glViewport(vm.GetIntParam(4), vm.GetIntParam(3), vm.GetIntParam(2), vm.GetIntParam(1));
        }
    }
}
