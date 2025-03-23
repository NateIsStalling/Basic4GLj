package com.basic4gl.library.desktopgl;

import static org.lwjgl.opengl.GL11.*;

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
import java.nio.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.BufferUtils;

/**
 * Created by Nate on 4/16/2015.
 */
public class GLBasicLib_gl implements FunctionLibrary {

	private ByteBuffer byteBuffer16;
	private ShortBuffer shortBuffer16;
	private IntBuffer intBuffer16;
	private LongBuffer longBuffer16;
	private FloatBuffer floatBuffer16;
	private DoubleBuffer doubleBuffer16;

	@Override
	public String name() {
		return "GLBasicLib";
	}

	@Override
	public String description() {
		return "OpenGL 1 functions and constants";
	}

	@Override
	public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {
		byteBuffer16 = BufferUtils.createByteBuffer(16);
		shortBuffer16 = BufferUtils.createShortBuffer(16);
		intBuffer16 = BufferUtils.createIntBuffer(16);
		longBuffer16 = BufferUtils.createLongBuffer(16);
		floatBuffer16 = BufferUtils.createFloatBuffer(16);
		doubleBuffer16 = BufferUtils.createDoubleBuffer(16);
	}

	@Override
	public void init(TomBasicCompiler comp, IServiceCollection services) {}

	@Override
	public void cleanup() {
		// Do nothing
	}

	@Override
	public Map<String, Constant> constants() {
		Map<String, Constant> c = new HashMap<>();
		// c.put("GL_VERSION_1_1", GL_VERSION_1_1));
		c.put("GL_VERSION", new Constant(GL_VERSION));

		c.put("GL_ACCUM", new Constant(GL_ACCUM));
		c.put("GL_LOAD", new Constant(GL_LOAD));
		c.put("GL_RETURN", new Constant(GL_RETURN));
		c.put("GL_MULT", new Constant(GL_MULT));
		c.put("GL_ADD", new Constant(GL_ADD));
		c.put("GL_NEVER", new Constant(GL_NEVER));
		c.put("GL_LESS", new Constant(GL_LESS));
		c.put("GL_EQUAL", new Constant(GL_EQUAL));
		c.put("GL_LEQUAL", new Constant(GL_LEQUAL));
		c.put("GL_GREATER", new Constant(GL_GREATER));
		c.put("GL_NOTEQUAL", new Constant(GL_NOTEQUAL));
		c.put("GL_GEQUAL", new Constant(GL_GEQUAL));
		c.put("GL_ALWAYS", new Constant(GL_ALWAYS));
		c.put("GL_CURRENT_BIT", new Constant(GL_CURRENT_BIT));
		c.put("GL_POINT_BIT", new Constant(GL_POINT_BIT));
		c.put("GL_LINE_BIT", new Constant(GL_LINE_BIT));
		c.put("GL_POLYGON_BIT", new Constant(GL_POLYGON_BIT));
		c.put("GL_POLYGON_STIPPLE_BIT", new Constant(GL_POLYGON_STIPPLE_BIT));
		c.put("GL_PIXEL_MODE_BIT", new Constant(GL_PIXEL_MODE_BIT));
		c.put("GL_LIGHTING_BIT", new Constant(GL_LIGHTING_BIT));
		c.put("GL_FOG_BIT", new Constant(GL_FOG_BIT));
		c.put("GL_DEPTH_BUFFER_BIT", new Constant(GL_DEPTH_BUFFER_BIT));
		c.put("GL_ACCUM_BUFFER_BIT", new Constant(GL_ACCUM_BUFFER_BIT));
		c.put("GL_STENCIL_BUFFER_BIT", new Constant(GL_STENCIL_BUFFER_BIT));
		c.put("GL_VIEWPORT_BIT", new Constant(GL_VIEWPORT_BIT));
		c.put("GL_TRANSFORM_BIT", new Constant(GL_TRANSFORM_BIT));
		c.put("GL_ENABLE_BIT", new Constant(GL_ENABLE_BIT));
		c.put("GL_COLOR_BUFFER_BIT", new Constant(GL_COLOR_BUFFER_BIT));
		c.put("GL_HINT_BIT", new Constant(GL_HINT_BIT));
		c.put("GL_EVAL_BIT", new Constant(GL_EVAL_BIT));
		c.put("GL_LIST_BIT", new Constant(GL_LIST_BIT));
		c.put("GL_TEXTURE_BIT", new Constant(GL_TEXTURE_BIT));
		c.put("GL_SCISSOR_BIT", new Constant(GL_SCISSOR_BIT));
		c.put("GL_ALL_ATTRIB_BITS", new Constant(GL_ALL_ATTRIB_BITS));
		c.put("GL_POINTS", new Constant(GL_POINTS));
		c.put("GL_LINES", new Constant(GL_LINES));
		c.put("GL_LINE_LOOP", new Constant(GL_LINE_LOOP));
		c.put("GL_LINE_STRIP", new Constant(GL_LINE_STRIP));
		c.put("GL_TRIANGLES", new Constant(GL_TRIANGLES));
		c.put("GL_TRIANGLE_STRIP", new Constant(GL_TRIANGLE_STRIP));
		c.put("GL_TRIANGLE_FAN", new Constant(GL_TRIANGLE_FAN));
		c.put("GL_QUADS", new Constant(GL_QUADS));
		c.put("GL_QUAD_STRIP", new Constant(GL_QUAD_STRIP));
		c.put("GL_POLYGON", new Constant(GL_POLYGON));
		c.put("GL_ZERO", new Constant(GL_ZERO));
		c.put("GL_ONE", new Constant(GL_ONE));
		c.put("GL_SRC_COLOR", new Constant(GL_SRC_COLOR));
		c.put("GL_ONE_MINUS_SRC_COLOR", new Constant(GL_ONE_MINUS_SRC_COLOR));
		c.put("GL_SRC_ALPHA", new Constant(GL_SRC_ALPHA));
		c.put("GL_ONE_MINUS_SRC_ALPHA", new Constant(GL_ONE_MINUS_SRC_ALPHA));
		c.put("GL_DST_ALPHA", new Constant(GL_DST_ALPHA));
		c.put("GL_ONE_MINUS_DST_ALPHA", new Constant(GL_ONE_MINUS_DST_ALPHA));
		c.put("GL_DST_COLOR", new Constant(GL_DST_COLOR));
		c.put("GL_ONE_MINUS_DST_COLOR", new Constant(GL_ONE_MINUS_DST_COLOR));
		c.put("GL_SRC_ALPHA_SATURATE", new Constant(GL_SRC_ALPHA_SATURATE));
		c.put("GL_TRUE", new Constant(GL_TRUE));
		c.put("GL_FALSE", new Constant(GL_FALSE));
		c.put("GL_CLIP_PLANE0", new Constant(GL_CLIP_PLANE0));
		c.put("GL_CLIP_PLANE1", new Constant(GL_CLIP_PLANE1));
		c.put("GL_CLIP_PLANE2", new Constant(GL_CLIP_PLANE2));
		c.put("GL_CLIP_PLANE3", new Constant(GL_CLIP_PLANE3));
		c.put("GL_CLIP_PLANE4", new Constant(GL_CLIP_PLANE4));
		c.put("GL_CLIP_PLANE5", new Constant(GL_CLIP_PLANE5));
		c.put("GL_BYTE", new Constant(GL_BYTE));
		c.put("GL_UNSIGNED_BYTE", new Constant(GL_UNSIGNED_BYTE));
		c.put("GL_SHORT", new Constant(GL_SHORT));
		c.put("GL_UNSIGNED_SHORT", new Constant(GL_UNSIGNED_SHORT));
		c.put("GL_INT", new Constant(GL_INT));
		c.put("GL_UNSIGNED_INT", new Constant(GL_UNSIGNED_INT));
		c.put("GL_FLOAT", new Constant(GL_FLOAT));
		c.put("GL_2_BYTES", new Constant(GL_2_BYTES));
		c.put("GL_3_BYTES", new Constant(GL_3_BYTES));
		c.put("GL_4_BYTES", new Constant(GL_4_BYTES));
		c.put("GL_DOUBLE", new Constant(GL_DOUBLE));
		c.put("GL_NONE", new Constant(GL_NONE));
		c.put("GL_FRONT_LEFT", new Constant(GL_FRONT_LEFT));
		c.put("GL_FRONT_RIGHT", new Constant(GL_FRONT_RIGHT));
		c.put("GL_BACK_LEFT", new Constant(GL_BACK_LEFT));
		c.put("GL_BACK_RIGHT", new Constant(GL_BACK_RIGHT));
		c.put("GL_FRONT", new Constant(GL_FRONT));
		c.put("GL_BACK", new Constant(GL_BACK));
		c.put("GL_LEFT", new Constant(GL_LEFT));
		c.put("GL_RIGHT", new Constant(GL_RIGHT));
		c.put("GL_FRONT_AND_BACK", new Constant(GL_FRONT_AND_BACK));
		c.put("GL_AUX0", new Constant(GL_AUX0));
		c.put("GL_AUX1", new Constant(GL_AUX1));
		c.put("GL_AUX2", new Constant(GL_AUX2));
		c.put("GL_AUX3", new Constant(GL_AUX3));
		c.put("GL_NO_ERROR", new Constant(GL_NO_ERROR));
		c.put("GL_INVALID_ENUM", new Constant(GL_INVALID_ENUM));
		c.put("GL_INVALID_VALUE", new Constant(GL_INVALID_VALUE));
		c.put("GL_INVALID_OPERATION", new Constant(GL_INVALID_OPERATION));
		// TODO Constants unavailable in GL 2; original source used GL 1.1
		// c.put("GL_STACK_OVERFLOW", GL_STACK_OVERFLOW));
		// c.put("GL_STACK_UNDERFLOW", GL_STACK_UNDERFLOW));
		c.put("GL_OUT_OF_MEMORY", new Constant(GL_OUT_OF_MEMORY));
		c.put("GL_2D", new Constant(GL_2D));
		c.put("GL_3D", new Constant(GL_3D));
		c.put("GL_3D_COLOR", new Constant(GL_3D_COLOR));
		c.put("GL_3D_COLOR_TEXTURE", new Constant(GL_3D_COLOR_TEXTURE));
		c.put("GL_4D_COLOR_TEXTURE", new Constant(GL_4D_COLOR_TEXTURE));
		c.put("GL_PASS_THROUGH_TOKEN", new Constant(GL_PASS_THROUGH_TOKEN));
		c.put("GL_POINT_TOKEN", new Constant(GL_POINT_TOKEN));
		c.put("GL_LINE_TOKEN", new Constant(GL_LINE_TOKEN));
		c.put("GL_POLYGON_TOKEN", new Constant(GL_POLYGON_TOKEN));
		c.put("GL_BITMAP_TOKEN", new Constant(GL_BITMAP_TOKEN));
		c.put("GL_DRAW_PIXEL_TOKEN", new Constant(GL_DRAW_PIXEL_TOKEN));
		c.put("GL_COPY_PIXEL_TOKEN", new Constant(GL_COPY_PIXEL_TOKEN));
		c.put("GL_LINE_RESET_TOKEN", new Constant(GL_LINE_RESET_TOKEN));
		c.put("GL_EXP", new Constant(GL_EXP));
		c.put("GL_EXP2", new Constant(GL_EXP2));
		c.put("GL_CW", new Constant(GL_CW));
		c.put("GL_CCW", new Constant(GL_CCW));
		c.put("GL_COEFF", new Constant(GL_COEFF));
		c.put("GL_ORDER", new Constant(GL_ORDER));
		c.put("GL_DOMAIN", new Constant(GL_DOMAIN));
		c.put("GL_CURRENT_COLOR", new Constant(GL_CURRENT_COLOR));
		c.put("GL_CURRENT_INDEX", new Constant(GL_CURRENT_INDEX));
		c.put("GL_CURRENT_NORMAL", new Constant(GL_CURRENT_NORMAL));
		c.put("GL_CURRENT_TEXTURE_COORDS", new Constant(GL_CURRENT_TEXTURE_COORDS));
		c.put("GL_CURRENT_RASTER_COLOR", new Constant(GL_CURRENT_RASTER_COLOR));
		c.put("GL_CURRENT_RASTER_INDEX", new Constant(GL_CURRENT_RASTER_INDEX));
		c.put("GL_CURRENT_RASTER_TEXTURE_COORDS", new Constant(GL_CURRENT_RASTER_TEXTURE_COORDS));
		c.put("GL_CURRENT_RASTER_POSITION", new Constant(GL_CURRENT_RASTER_POSITION));
		c.put("GL_CURRENT_RASTER_POSITION_VALID", new Constant(GL_CURRENT_RASTER_POSITION_VALID));
		c.put("GL_CURRENT_RASTER_DISTANCE", new Constant(GL_CURRENT_RASTER_DISTANCE));
		c.put("GL_POINT_SMOOTH", new Constant(GL_POINT_SMOOTH));
		c.put("GL_POINT_SIZE", new Constant(GL_POINT_SIZE));
		c.put("GL_POINT_SIZE_RANGE", new Constant(GL_POINT_SIZE_RANGE));
		c.put("GL_POINT_SIZE_GRANULARITY", new Constant(GL_POINT_SIZE_GRANULARITY));
		c.put("GL_LINE_SMOOTH", new Constant(GL_LINE_SMOOTH));
		c.put("GL_LINE_WIDTH", new Constant(GL_LINE_WIDTH));
		c.put("GL_LINE_WIDTH_RANGE", new Constant(GL_LINE_WIDTH_RANGE));
		c.put("GL_LINE_WIDTH_GRANULARITY", new Constant(GL_LINE_WIDTH_GRANULARITY));
		c.put("GL_LINE_STIPPLE", new Constant(GL_LINE_STIPPLE));
		c.put("GL_LINE_STIPPLE_PATTERN", new Constant(GL_LINE_STIPPLE_PATTERN));
		c.put("GL_LINE_STIPPLE_REPEAT", new Constant(GL_LINE_STIPPLE_REPEAT));
		c.put("GL_LIST_MODE", new Constant(GL_LIST_MODE));
		c.put("GL_MAX_LIST_NESTING", new Constant(GL_MAX_LIST_NESTING));
		c.put("GL_LIST_BASE", new Constant(GL_LIST_BASE));
		c.put("GL_LIST_INDEX", new Constant(GL_LIST_INDEX));
		c.put("GL_POLYGON_MODE", new Constant(GL_POLYGON_MODE));
		c.put("GL_POLYGON_SMOOTH", new Constant(GL_POLYGON_SMOOTH));
		c.put("GL_POLYGON_STIPPLE", new Constant(GL_POLYGON_STIPPLE));
		c.put("GL_EDGE_FLAG", new Constant(GL_EDGE_FLAG));
		c.put("GL_CULL_FACE", new Constant(GL_CULL_FACE));
		c.put("GL_CULL_FACE_MODE", new Constant(GL_CULL_FACE_MODE));
		c.put("GL_FRONT_FACE", new Constant(GL_FRONT_FACE));
		c.put("GL_LIGHTING", new Constant(GL_LIGHTING));
		c.put("GL_LIGHT_MODEL_LOCAL_VIEWER", new Constant(GL_LIGHT_MODEL_LOCAL_VIEWER));
		c.put("GL_LIGHT_MODEL_TWO_SIDE", new Constant(GL_LIGHT_MODEL_TWO_SIDE));
		c.put("GL_LIGHT_MODEL_AMBIENT", new Constant(GL_LIGHT_MODEL_AMBIENT));
		c.put("GL_SHADE_MODEL", new Constant(GL_SHADE_MODEL));
		c.put("GL_COLOR_MATERIAL_FACE", new Constant(GL_COLOR_MATERIAL_FACE));
		c.put("GL_COLOR_MATERIAL_PARAMETER", new Constant(GL_COLOR_MATERIAL_PARAMETER));
		c.put("GL_COLOR_MATERIAL", new Constant(GL_COLOR_MATERIAL));
		c.put("GL_FOG", new Constant(GL_FOG));
		c.put("GL_FOG_INDEX", new Constant(GL_FOG_INDEX));
		c.put("GL_FOG_DENSITY", new Constant(GL_FOG_DENSITY));
		c.put("GL_FOG_START", new Constant(GL_FOG_START));
		c.put("GL_FOG_END", new Constant(GL_FOG_END));
		c.put("GL_FOG_MODE", new Constant(GL_FOG_MODE));
		c.put("GL_FOG_COLOR", new Constant(GL_FOG_COLOR));
		c.put("GL_DEPTH_RANGE", new Constant(GL_DEPTH_RANGE));
		c.put("GL_DEPTH_TEST", new Constant(GL_DEPTH_TEST));
		c.put("GL_DEPTH_WRITEMASK", new Constant(GL_DEPTH_WRITEMASK));
		c.put("GL_DEPTH_CLEAR_VALUE", new Constant(GL_DEPTH_CLEAR_VALUE));
		c.put("GL_DEPTH_FUNC", new Constant(GL_DEPTH_FUNC));
		c.put("GL_ACCUM_CLEAR_VALUE", new Constant(GL_ACCUM_CLEAR_VALUE));
		c.put("GL_STENCIL_TEST", new Constant(GL_STENCIL_TEST));
		c.put("GL_STENCIL_CLEAR_VALUE", new Constant(GL_STENCIL_CLEAR_VALUE));
		c.put("GL_STENCIL_FUNC", new Constant(GL_STENCIL_FUNC));
		c.put("GL_STENCIL_VALUE_MASK", new Constant(GL_STENCIL_VALUE_MASK));
		c.put("GL_STENCIL_FAIL", new Constant(GL_STENCIL_FAIL));
		c.put("GL_STENCIL_PASS_DEPTH_FAIL", new Constant(GL_STENCIL_PASS_DEPTH_FAIL));
		c.put("GL_STENCIL_PASS_DEPTH_PASS", new Constant(GL_STENCIL_PASS_DEPTH_PASS));
		c.put("GL_STENCIL_REF", new Constant(GL_STENCIL_REF));
		c.put("GL_STENCIL_WRITEMASK", new Constant(GL_STENCIL_WRITEMASK));
		c.put("GL_MATRIX_MODE", new Constant(GL_MATRIX_MODE));
		c.put("GL_NORMALIZE", new Constant(GL_NORMALIZE));
		c.put("GL_VIEWPORT", new Constant(GL_VIEWPORT));
		c.put("GL_MODELVIEW_STACK_DEPTH", new Constant(GL_MODELVIEW_STACK_DEPTH));
		c.put("GL_PROJECTION_STACK_DEPTH", new Constant(GL_PROJECTION_STACK_DEPTH));
		c.put("GL_TEXTURE_STACK_DEPTH", new Constant(GL_TEXTURE_STACK_DEPTH));
		c.put("GL_MODELVIEW_MATRIX", new Constant(GL_MODELVIEW_MATRIX));
		c.put("GL_PROJECTION_MATRIX", new Constant(GL_PROJECTION_MATRIX));
		c.put("GL_TEXTURE_MATRIX", new Constant(GL_TEXTURE_MATRIX));
		c.put("GL_ATTRIB_STACK_DEPTH", new Constant(GL_ATTRIB_STACK_DEPTH));
		c.put("GL_CLIENT_ATTRIB_STACK_DEPTH", new Constant(GL_CLIENT_ATTRIB_STACK_DEPTH));
		c.put("GL_ALPHA_TEST", new Constant(GL_ALPHA_TEST));
		c.put("GL_ALPHA_TEST_FUNC", new Constant(GL_ALPHA_TEST_FUNC));
		c.put("GL_ALPHA_TEST_REF", new Constant(GL_ALPHA_TEST_REF));
		c.put("GL_DITHER", new Constant(GL_DITHER));
		c.put("GL_BLEND_DST", new Constant(GL_BLEND_DST));
		c.put("GL_BLEND_SRC", new Constant(GL_BLEND_SRC));
		c.put("GL_BLEND", new Constant(GL_BLEND));
		c.put("GL_LOGIC_OP_MODE", new Constant(GL_LOGIC_OP_MODE));
		c.put("GL_INDEX_LOGIC_OP", new Constant(GL_INDEX_LOGIC_OP));
		c.put("GL_COLOR_LOGIC_OP", new Constant(GL_COLOR_LOGIC_OP));
		c.put("GL_AUX_BUFFERS", new Constant(GL_AUX_BUFFERS));
		c.put("GL_DRAW_BUFFER", new Constant(GL_DRAW_BUFFER));
		c.put("GL_READ_BUFFER", new Constant(GL_READ_BUFFER));
		c.put("GL_SCISSOR_BOX", new Constant(GL_SCISSOR_BOX));
		c.put("GL_SCISSOR_TEST", new Constant(GL_SCISSOR_TEST));
		c.put("GL_INDEX_CLEAR_VALUE", new Constant(GL_INDEX_CLEAR_VALUE));
		c.put("GL_INDEX_WRITEMASK", new Constant(GL_INDEX_WRITEMASK));
		c.put("GL_COLOR_CLEAR_VALUE", new Constant(GL_COLOR_CLEAR_VALUE));
		c.put("GL_COLOR_WRITEMASK", new Constant(GL_COLOR_WRITEMASK));
		c.put("GL_INDEX_MODE", new Constant(GL_INDEX_MODE));
		c.put("GL_RGBA_MODE", new Constant(GL_RGBA_MODE));
		c.put("GL_DOUBLEBUFFER", new Constant(GL_DOUBLEBUFFER));
		c.put("GL_STEREO", new Constant(GL_STEREO));
		c.put("GL_RENDER_MODE", new Constant(GL_RENDER_MODE));
		c.put("GL_PERSPECTIVE_CORRECTION_HINT", new Constant(GL_PERSPECTIVE_CORRECTION_HINT));
		c.put("GL_POINT_SMOOTH_HINT", new Constant(GL_POINT_SMOOTH_HINT));
		c.put("GL_LINE_SMOOTH_HINT", new Constant(GL_LINE_SMOOTH_HINT));
		c.put("GL_POLYGON_SMOOTH_HINT", new Constant(GL_POLYGON_SMOOTH_HINT));
		c.put("GL_FOG_HINT", new Constant(GL_FOG_HINT));
		c.put("GL_TEXTURE_GEN_S", new Constant(GL_TEXTURE_GEN_S));
		c.put("GL_TEXTURE_GEN_T", new Constant(GL_TEXTURE_GEN_T));
		c.put("GL_TEXTURE_GEN_R", new Constant(GL_TEXTURE_GEN_R));
		c.put("GL_TEXTURE_GEN_Q", new Constant(GL_TEXTURE_GEN_Q));
		c.put("GL_PIXEL_MAP_I_TO_I", new Constant(GL_PIXEL_MAP_I_TO_I));
		c.put("GL_PIXEL_MAP_S_TO_S", new Constant(GL_PIXEL_MAP_S_TO_S));
		c.put("GL_PIXEL_MAP_I_TO_R", new Constant(GL_PIXEL_MAP_I_TO_R));
		c.put("GL_PIXEL_MAP_I_TO_G", new Constant(GL_PIXEL_MAP_I_TO_G));
		c.put("GL_PIXEL_MAP_I_TO_B", new Constant(GL_PIXEL_MAP_I_TO_B));
		c.put("GL_PIXEL_MAP_I_TO_A", new Constant(GL_PIXEL_MAP_I_TO_A));
		c.put("GL_PIXEL_MAP_R_TO_R", new Constant(GL_PIXEL_MAP_R_TO_R));
		c.put("GL_PIXEL_MAP_G_TO_G", new Constant(GL_PIXEL_MAP_G_TO_G));
		c.put("GL_PIXEL_MAP_B_TO_B", new Constant(GL_PIXEL_MAP_B_TO_B));
		c.put("GL_PIXEL_MAP_A_TO_A", new Constant(GL_PIXEL_MAP_A_TO_A));
		c.put("GL_PIXEL_MAP_I_TO_I_SIZE", new Constant(GL_PIXEL_MAP_I_TO_I_SIZE));
		c.put("GL_PIXEL_MAP_S_TO_S_SIZE", new Constant(GL_PIXEL_MAP_S_TO_S_SIZE));
		c.put("GL_PIXEL_MAP_I_TO_R_SIZE", new Constant(GL_PIXEL_MAP_I_TO_R_SIZE));
		c.put("GL_PIXEL_MAP_I_TO_G_SIZE", new Constant(GL_PIXEL_MAP_I_TO_G_SIZE));
		c.put("GL_PIXEL_MAP_I_TO_B_SIZE", new Constant(GL_PIXEL_MAP_I_TO_B_SIZE));
		c.put("GL_PIXEL_MAP_I_TO_A_SIZE", new Constant(GL_PIXEL_MAP_I_TO_A_SIZE));
		c.put("GL_PIXEL_MAP_R_TO_R_SIZE", new Constant(GL_PIXEL_MAP_R_TO_R_SIZE));
		c.put("GL_PIXEL_MAP_G_TO_G_SIZE", new Constant(GL_PIXEL_MAP_G_TO_G_SIZE));
		c.put("GL_PIXEL_MAP_B_TO_B_SIZE", new Constant(GL_PIXEL_MAP_B_TO_B_SIZE));
		c.put("GL_PIXEL_MAP_A_TO_A_SIZE", new Constant(GL_PIXEL_MAP_A_TO_A_SIZE));
		c.put("GL_UNPACK_SWAP_BYTES", new Constant(GL_UNPACK_SWAP_BYTES));
		c.put("GL_UNPACK_LSB_FIRST", new Constant(GL_UNPACK_LSB_FIRST));
		c.put("GL_UNPACK_ROW_LENGTH", new Constant(GL_UNPACK_ROW_LENGTH));
		c.put("GL_UNPACK_SKIP_ROWS", new Constant(GL_UNPACK_SKIP_ROWS));
		c.put("GL_UNPACK_SKIP_PIXELS", new Constant(GL_UNPACK_SKIP_PIXELS));
		c.put("GL_UNPACK_ALIGNMENT", new Constant(GL_UNPACK_ALIGNMENT));
		c.put("GL_PACK_SWAP_BYTES", new Constant(GL_PACK_SWAP_BYTES));
		c.put("GL_PACK_LSB_FIRST", new Constant(GL_PACK_LSB_FIRST));
		c.put("GL_PACK_ROW_LENGTH", new Constant(GL_PACK_ROW_LENGTH));
		c.put("GL_PACK_SKIP_ROWS", new Constant(GL_PACK_SKIP_ROWS));
		c.put("GL_PACK_SKIP_PIXELS", new Constant(GL_PACK_SKIP_PIXELS));
		c.put("GL_PACK_ALIGNMENT", new Constant(GL_PACK_ALIGNMENT));
		c.put("GL_MAP_COLOR", new Constant(GL_MAP_COLOR));
		c.put("GL_MAP_STENCIL", new Constant(GL_MAP_STENCIL));
		c.put("GL_INDEX_SHIFT", new Constant(GL_INDEX_SHIFT));
		c.put("GL_INDEX_OFFSET", new Constant(GL_INDEX_OFFSET));
		c.put("GL_RED_SCALE", new Constant(GL_RED_SCALE));
		c.put("GL_RED_BIAS", new Constant(GL_RED_BIAS));
		c.put("GL_ZOOM_X", new Constant(GL_ZOOM_X));
		c.put("GL_ZOOM_Y", new Constant(GL_ZOOM_Y));
		c.put("GL_GREEN_SCALE", new Constant(GL_GREEN_SCALE));
		c.put("GL_GREEN_BIAS", new Constant(GL_GREEN_BIAS));
		c.put("GL_BLUE_SCALE", new Constant(GL_BLUE_SCALE));
		c.put("GL_BLUE_BIAS", new Constant(GL_BLUE_BIAS));
		c.put("GL_ALPHA_SCALE", new Constant(GL_ALPHA_SCALE));
		c.put("GL_ALPHA_BIAS", new Constant(GL_ALPHA_BIAS));
		c.put("GL_DEPTH_SCALE", new Constant(GL_DEPTH_SCALE));
		c.put("GL_DEPTH_BIAS", new Constant(GL_DEPTH_BIAS));
		c.put("GL_MAX_EVAL_ORDER", new Constant(GL_MAX_EVAL_ORDER));
		c.put("GL_MAX_LIGHTS", new Constant(GL_MAX_LIGHTS));
		c.put("GL_MAX_CLIP_PLANES", new Constant(GL_MAX_CLIP_PLANES));
		c.put("GL_MAX_TEXTURE_SIZE", new Constant(GL_MAX_TEXTURE_SIZE));
		c.put("GL_MAX_PIXEL_MAP_TABLE", new Constant(GL_MAX_PIXEL_MAP_TABLE));
		c.put("GL_MAX_ATTRIB_STACK_DEPTH", new Constant(GL_MAX_ATTRIB_STACK_DEPTH));
		c.put("GL_MAX_MODELVIEW_STACK_DEPTH", new Constant(GL_MAX_MODELVIEW_STACK_DEPTH));
		c.put("GL_MAX_NAME_STACK_DEPTH", new Constant(GL_MAX_NAME_STACK_DEPTH));
		c.put("GL_MAX_PROJECTION_STACK_DEPTH", new Constant(GL_MAX_PROJECTION_STACK_DEPTH));
		c.put("GL_MAX_TEXTURE_STACK_DEPTH", new Constant(GL_MAX_TEXTURE_STACK_DEPTH));
		c.put("GL_MAX_VIEWPORT_DIMS", new Constant(GL_MAX_VIEWPORT_DIMS));
		c.put("GL_MAX_CLIENT_ATTRIB_STACK_DEPTH", new Constant(GL_MAX_CLIENT_ATTRIB_STACK_DEPTH));
		c.put("GL_SUBPIXEL_BITS", new Constant(GL_SUBPIXEL_BITS));
		c.put("GL_INDEX_BITS", new Constant(GL_INDEX_BITS));
		c.put("GL_RED_BITS", new Constant(GL_RED_BITS));
		c.put("GL_GREEN_BITS", new Constant(GL_GREEN_BITS));
		c.put("GL_BLUE_BITS", new Constant(GL_BLUE_BITS));
		c.put("GL_ALPHA_BITS", new Constant(GL_ALPHA_BITS));
		c.put("GL_DEPTH_BITS", new Constant(GL_DEPTH_BITS));
		c.put("GL_STENCIL_BITS", new Constant(GL_STENCIL_BITS));
		c.put("GL_ACCUM_RED_BITS", new Constant(GL_ACCUM_RED_BITS));
		c.put("GL_ACCUM_GREEN_BITS", new Constant(GL_ACCUM_GREEN_BITS));
		c.put("GL_ACCUM_BLUE_BITS", new Constant(GL_ACCUM_BLUE_BITS));
		c.put("GL_ACCUM_ALPHA_BITS", new Constant(GL_ACCUM_ALPHA_BITS));
		c.put("GL_NAME_STACK_DEPTH", new Constant(GL_NAME_STACK_DEPTH));
		c.put("GL_AUTO_NORMAL", new Constant(GL_AUTO_NORMAL));
		c.put("GL_MAP1_COLOR_4", new Constant(GL_MAP1_COLOR_4));
		c.put("GL_MAP1_INDEX", new Constant(GL_MAP1_INDEX));
		c.put("GL_MAP1_NORMAL", new Constant(GL_MAP1_NORMAL));
		c.put("GL_MAP1_TEXTURE_COORD_1", new Constant(GL_MAP1_TEXTURE_COORD_1));
		c.put("GL_MAP1_TEXTURE_COORD_2", new Constant(GL_MAP1_TEXTURE_COORD_2));
		c.put("GL_MAP1_TEXTURE_COORD_3", new Constant(GL_MAP1_TEXTURE_COORD_3));
		c.put("GL_MAP1_TEXTURE_COORD_4", new Constant(GL_MAP1_TEXTURE_COORD_4));
		c.put("GL_MAP1_VERTEX_3", new Constant(GL_MAP1_VERTEX_3));
		c.put("GL_MAP1_VERTEX_4", new Constant(GL_MAP1_VERTEX_4));
		c.put("GL_MAP2_COLOR_4", new Constant(GL_MAP2_COLOR_4));
		c.put("GL_MAP2_INDEX", new Constant(GL_MAP2_INDEX));
		c.put("GL_MAP2_NORMAL", new Constant(GL_MAP2_NORMAL));
		c.put("GL_MAP2_TEXTURE_COORD_1", new Constant(GL_MAP2_TEXTURE_COORD_1));
		c.put("GL_MAP2_TEXTURE_COORD_2", new Constant(GL_MAP2_TEXTURE_COORD_2));
		c.put("GL_MAP2_TEXTURE_COORD_3", new Constant(GL_MAP2_TEXTURE_COORD_3));
		c.put("GL_MAP2_TEXTURE_COORD_4", new Constant(GL_MAP2_TEXTURE_COORD_4));
		c.put("GL_MAP2_VERTEX_3", new Constant(GL_MAP2_VERTEX_3));
		c.put("GL_MAP2_VERTEX_4", new Constant(GL_MAP2_VERTEX_4));
		c.put("GL_MAP1_GRID_DOMAIN", new Constant(GL_MAP1_GRID_DOMAIN));
		c.put("GL_MAP1_GRID_SEGMENTS", new Constant(GL_MAP1_GRID_SEGMENTS));
		c.put("GL_MAP2_GRID_DOMAIN", new Constant(GL_MAP2_GRID_DOMAIN));
		c.put("GL_MAP2_GRID_SEGMENTS", new Constant(GL_MAP2_GRID_SEGMENTS));
		c.put("GL_TEXTURE_1D", new Constant(GL_TEXTURE_1D));
		c.put("GL_TEXTURE_2D", new Constant(GL_TEXTURE_2D));
		c.put("GL_FEEDBACK_BUFFER_POINTER", new Constant(GL_FEEDBACK_BUFFER_POINTER));
		c.put("GL_FEEDBACK_BUFFER_SIZE", new Constant(GL_FEEDBACK_BUFFER_SIZE));
		c.put("GL_FEEDBACK_BUFFER_TYPE", new Constant(GL_FEEDBACK_BUFFER_TYPE));
		c.put("GL_SELECTION_BUFFER_POINTER", new Constant(GL_SELECTION_BUFFER_POINTER));
		c.put("GL_SELECTION_BUFFER_SIZE", new Constant(GL_SELECTION_BUFFER_SIZE));
		c.put("GL_TEXTURE_WIDTH", new Constant(GL_TEXTURE_WIDTH));
		c.put("GL_TEXTURE_HEIGHT", new Constant(GL_TEXTURE_HEIGHT));
		c.put("GL_TEXTURE_INTERNAL_FORMAT", new Constant(GL_TEXTURE_INTERNAL_FORMAT));
		c.put("GL_TEXTURE_BORDER_COLOR", new Constant(GL_TEXTURE_BORDER_COLOR));
		c.put("GL_TEXTURE_BORDER", new Constant(GL_TEXTURE_BORDER));
		c.put("GL_DONT_CARE", new Constant(GL_DONT_CARE));
		c.put("GL_FASTEST", new Constant(GL_FASTEST));
		c.put("GL_NICEST", new Constant(GL_NICEST));
		c.put("GL_LIGHT0", new Constant(GL_LIGHT0));
		c.put("GL_LIGHT1", new Constant(GL_LIGHT1));
		c.put("GL_LIGHT2", new Constant(GL_LIGHT2));
		c.put("GL_LIGHT3", new Constant(GL_LIGHT3));
		c.put("GL_LIGHT4", new Constant(GL_LIGHT4));
		c.put("GL_LIGHT5", new Constant(GL_LIGHT5));
		c.put("GL_LIGHT6", new Constant(GL_LIGHT6));
		c.put("GL_LIGHT7", new Constant(GL_LIGHT7));
		c.put("GL_AMBIENT", new Constant(GL_AMBIENT));
		c.put("GL_DIFFUSE", new Constant(GL_DIFFUSE));
		c.put("GL_SPECULAR", new Constant(GL_SPECULAR));
		c.put("GL_POSITION", new Constant(GL_POSITION));
		c.put("GL_SPOT_DIRECTION", new Constant(GL_SPOT_DIRECTION));
		c.put("GL_SPOT_EXPONENT", new Constant(GL_SPOT_EXPONENT));
		c.put("GL_SPOT_CUTOFF", new Constant(GL_SPOT_CUTOFF));
		c.put("GL_CONSTANT_ATTENUATION", new Constant(GL_CONSTANT_ATTENUATION));
		c.put("GL_LINEAR_ATTENUATION", new Constant(GL_LINEAR_ATTENUATION));
		c.put("GL_QUADRATIC_ATTENUATION", new Constant(GL_QUADRATIC_ATTENUATION));
		c.put("GL_COMPILE", new Constant(GL_COMPILE));
		c.put("GL_COMPILE_AND_EXECUTE", new Constant(GL_COMPILE_AND_EXECUTE));
		c.put("GL_CLEAR", new Constant(GL_CLEAR));
		c.put("GL_AND", new Constant(GL_AND));
		c.put("GL_AND_REVERSE", new Constant(GL_AND_REVERSE));
		c.put("GL_COPY", new Constant(GL_COPY));
		c.put("GL_AND_INVERTED", new Constant(GL_AND_INVERTED));
		c.put("GL_NOOP", new Constant(GL_NOOP));
		c.put("GL_XOR", new Constant(GL_XOR));
		c.put("GL_OR", new Constant(GL_OR));
		c.put("GL_NOR", new Constant(GL_NOR));
		c.put("GL_EQUIV", new Constant(GL_EQUIV));
		c.put("GL_INVERT", new Constant(GL_INVERT));
		c.put("GL_OR_REVERSE", new Constant(GL_OR_REVERSE));
		c.put("GL_COPY_INVERTED", new Constant(GL_COPY_INVERTED));
		c.put("GL_OR_INVERTED", new Constant(GL_OR_INVERTED));
		c.put("GL_NAND", new Constant(GL_NAND));
		c.put("GL_SET", new Constant(GL_SET));
		c.put("GL_EMISSION", new Constant(GL_EMISSION));
		c.put("GL_SHININESS", new Constant(GL_SHININESS));
		c.put("GL_AMBIENT_AND_DIFFUSE", new Constant(GL_AMBIENT_AND_DIFFUSE));
		c.put("GL_COLOR_INDEXES", new Constant(GL_COLOR_INDEXES));
		c.put("GL_MODELVIEW", new Constant(GL_MODELVIEW));
		c.put("GL_PROJECTION", new Constant(GL_PROJECTION));
		c.put("GL_TEXTURE", new Constant(GL_TEXTURE));
		c.put("GL_COLOR", new Constant(GL_COLOR));
		c.put("GL_DEPTH", new Constant(GL_DEPTH));
		c.put("GL_STENCIL", new Constant(GL_STENCIL));
		c.put("GL_COLOR_INDEX", new Constant(GL_COLOR_INDEX));
		c.put("GL_STENCIL_INDEX", new Constant(GL_STENCIL_INDEX));
		c.put("GL_DEPTH_COMPONENT", new Constant(GL_DEPTH_COMPONENT));
		c.put("GL_RED", new Constant(GL_RED));
		c.put("GL_GREEN", new Constant(GL_GREEN));
		c.put("GL_BLUE", new Constant(GL_BLUE));
		c.put("GL_ALPHA", new Constant(GL_ALPHA));
		c.put("GL_RGB", new Constant(GL_RGB));
		c.put("GL_RGBA", new Constant(GL_RGBA));
		c.put("GL_LUMINANCE", new Constant(GL_LUMINANCE));
		c.put("GL_LUMINANCE_ALPHA", new Constant(GL_LUMINANCE_ALPHA));
		c.put("GL_BITMAP", new Constant(GL_BITMAP));
		c.put("GL_POINT", new Constant(GL_POINT));
		c.put("GL_LINE", new Constant(GL_LINE));
		c.put("GL_FILL", new Constant(GL_FILL));
		c.put("GL_RENDER", new Constant(GL_RENDER));
		c.put("GL_FEEDBACK", new Constant(GL_FEEDBACK));
		c.put("GL_SELECT", new Constant(GL_SELECT));
		c.put("GL_FLAT", new Constant(GL_FLAT));
		c.put("GL_SMOOTH", new Constant(GL_SMOOTH));
		c.put("GL_KEEP", new Constant(GL_KEEP));
		c.put("GL_REPLACE", new Constant(GL_REPLACE));
		c.put("GL_INCR", new Constant(GL_INCR));
		c.put("GL_DECR", new Constant(GL_DECR));
		c.put("GL_VENDOR", new Constant(GL_VENDOR));
		c.put("GL_RENDERER", new Constant(GL_RENDERER));
		c.put("GL_EXTENSIONS", new Constant(GL_EXTENSIONS));
		c.put("GL_S", new Constant(GL_S));
		c.put("GL_T", new Constant(GL_T));
		c.put("GL_R", new Constant(GL_R));
		c.put("GL_Q", new Constant(GL_Q));
		c.put("GL_MODULATE", new Constant(GL_MODULATE));
		c.put("GL_DECAL", new Constant(GL_DECAL));
		c.put("GL_TEXTURE_ENV_MODE", new Constant(GL_TEXTURE_ENV_MODE));
		c.put("GL_TEXTURE_ENV_COLOR", new Constant(GL_TEXTURE_ENV_COLOR));
		c.put("GL_TEXTURE_ENV", new Constant(GL_TEXTURE_ENV));
		c.put("GL_EYE_LINEAR", new Constant(GL_EYE_LINEAR));
		c.put("GL_OBJECT_LINEAR", new Constant(GL_OBJECT_LINEAR));
		c.put("GL_SPHERE_MAP", new Constant(GL_SPHERE_MAP));
		c.put("GL_TEXTURE_GEN_MODE", new Constant(GL_TEXTURE_GEN_MODE));
		c.put("GL_OBJECT_PLANE", new Constant(GL_OBJECT_PLANE));
		c.put("GL_EYE_PLANE", new Constant(GL_EYE_PLANE));
		c.put("GL_NEAREST", new Constant(GL_NEAREST));
		c.put("GL_LINEAR", new Constant(GL_LINEAR));
		c.put("GL_NEAREST_MIPMAP_NEAREST", new Constant(GL_NEAREST_MIPMAP_NEAREST));
		c.put("GL_LINEAR_MIPMAP_NEAREST", new Constant(GL_LINEAR_MIPMAP_NEAREST));
		c.put("GL_NEAREST_MIPMAP_LINEAR", new Constant(GL_NEAREST_MIPMAP_LINEAR));
		c.put("GL_LINEAR_MIPMAP_LINEAR", new Constant(GL_LINEAR_MIPMAP_LINEAR));
		c.put("GL_TEXTURE_MAG_FILTER", new Constant(GL_TEXTURE_MAG_FILTER));
		c.put("GL_TEXTURE_MIN_FILTER", new Constant(GL_TEXTURE_MIN_FILTER));
		c.put("GL_TEXTURE_WRAP_S", new Constant(GL_TEXTURE_WRAP_S));
		c.put("GL_TEXTURE_WRAP_T", new Constant(GL_TEXTURE_WRAP_T));
		c.put("GL_CLAMP", new Constant(GL_CLAMP));
		c.put("GL_REPEAT", new Constant(GL_REPEAT));
		c.put("GL_CLIENT_PIXEL_STORE_BIT", new Constant(GL_CLIENT_PIXEL_STORE_BIT));
		c.put("GL_CLIENT_VERTEX_ARRAY_BIT", new Constant(GL_CLIENT_VERTEX_ARRAY_BIT));
		// NOTE: GL_CLIENT_ALL_ATTRIB_BITS was cast to unsigned int in original source
		c.put("GL_CLIENT_ALL_ATTRIB_BITS", new Constant(GL_CLIENT_ALL_ATTRIB_BITS));
		c.put("GL_POLYGON_OFFSET_FACTOR", new Constant(GL_POLYGON_OFFSET_FACTOR));
		c.put("GL_POLYGON_OFFSET_UNITS", new Constant(GL_POLYGON_OFFSET_UNITS));
		c.put("GL_POLYGON_OFFSET_POINT", new Constant(GL_POLYGON_OFFSET_POINT));
		c.put("GL_POLYGON_OFFSET_LINE", new Constant(GL_POLYGON_OFFSET_LINE));
		c.put("GL_POLYGON_OFFSET_FILL", new Constant(GL_POLYGON_OFFSET_FILL));
		c.put("GL_ALPHA4", new Constant(GL_ALPHA4));
		c.put("GL_ALPHA8", new Constant(GL_ALPHA8));
		c.put("GL_ALPHA12", new Constant(GL_ALPHA12));
		c.put("GL_ALPHA16", new Constant(GL_ALPHA16));
		c.put("GL_LUMINANCE4", new Constant(GL_LUMINANCE4));
		c.put("GL_LUMINANCE8", new Constant(GL_LUMINANCE8));
		c.put("GL_LUMINANCE12", new Constant(GL_LUMINANCE12));
		c.put("GL_LUMINANCE16", new Constant(GL_LUMINANCE16));
		c.put("GL_LUMINANCE4_ALPHA4", new Constant(GL_LUMINANCE4_ALPHA4));
		c.put("GL_LUMINANCE6_ALPHA2", new Constant(GL_LUMINANCE6_ALPHA2));
		c.put("GL_LUMINANCE8_ALPHA8", new Constant(GL_LUMINANCE8_ALPHA8));
		c.put("GL_LUMINANCE12_ALPHA4", new Constant(GL_LUMINANCE12_ALPHA4));
		c.put("GL_LUMINANCE12_ALPHA12", new Constant(GL_LUMINANCE12_ALPHA12));
		c.put("GL_LUMINANCE16_ALPHA16", new Constant(GL_LUMINANCE16_ALPHA16));
		c.put("GL_INTENSITY", new Constant(GL_INTENSITY));
		c.put("GL_INTENSITY4", new Constant(GL_INTENSITY4));
		c.put("GL_INTENSITY8", new Constant(GL_INTENSITY8));
		c.put("GL_INTENSITY12", new Constant(GL_INTENSITY12));
		c.put("GL_INTENSITY16", new Constant(GL_INTENSITY16));
		c.put("GL_R3_G3_B2", new Constant(GL_R3_G3_B2));
		c.put("GL_RGB4", new Constant(GL_RGB4));
		c.put("GL_RGB5", new Constant(GL_RGB5));
		c.put("GL_RGB8", new Constant(GL_RGB8));
		c.put("GL_RGB10", new Constant(GL_RGB10));
		c.put("GL_RGB12", new Constant(GL_RGB12));
		c.put("GL_RGB16", new Constant(GL_RGB16));
		c.put("GL_RGBA2", new Constant(GL_RGBA2));
		c.put("GL_RGBA4", new Constant(GL_RGBA4));
		c.put("GL_RGB5_A1", new Constant(GL_RGB5_A1));
		c.put("GL_RGBA8", new Constant(GL_RGBA8));
		c.put("GL_RGB10_A2", new Constant(GL_RGB10_A2));
		c.put("GL_RGBA12", new Constant(GL_RGBA12));
		c.put("GL_RGBA16", new Constant(GL_RGBA16));
		c.put("GL_TEXTURE_RED_SIZE", new Constant(GL_TEXTURE_RED_SIZE));
		c.put("GL_TEXTURE_GREEN_SIZE", new Constant(GL_TEXTURE_GREEN_SIZE));
		c.put("GL_TEXTURE_BLUE_SIZE", new Constant(GL_TEXTURE_BLUE_SIZE));
		c.put("GL_TEXTURE_ALPHA_SIZE", new Constant(GL_TEXTURE_ALPHA_SIZE));
		c.put("GL_TEXTURE_LUMINANCE_SIZE", new Constant(GL_TEXTURE_LUMINANCE_SIZE));
		c.put("GL_TEXTURE_INTENSITY_SIZE", new Constant(GL_TEXTURE_INTENSITY_SIZE));
		c.put("GL_PROXY_TEXTURE_1D", new Constant(GL_PROXY_TEXTURE_1D));
		c.put("GL_PROXY_TEXTURE_2D", new Constant(GL_PROXY_TEXTURE_2D));
		c.put("GL_TEXTURE_PRIORITY", new Constant(GL_TEXTURE_PRIORITY));
		c.put("GL_TEXTURE_RESIDENT", new Constant(GL_TEXTURE_RESIDENT));
		c.put("GL_TEXTURE_BINDING_1D", new Constant(GL_TEXTURE_BINDING_1D));
		c.put("GL_TEXTURE_BINDING_2D", new Constant(GL_TEXTURE_BINDING_2D));
		c.put("GL_VERTEX_ARRAY", new Constant(GL_VERTEX_ARRAY));
		c.put("GL_NORMAL_ARRAY", new Constant(GL_NORMAL_ARRAY));
		c.put("GL_COLOR_ARRAY", new Constant(GL_COLOR_ARRAY));
		c.put("GL_INDEX_ARRAY", new Constant(GL_INDEX_ARRAY));
		c.put("GL_TEXTURE_COORD_ARRAY", new Constant(GL_TEXTURE_COORD_ARRAY));
		c.put("GL_EDGE_FLAG_ARRAY", new Constant(GL_EDGE_FLAG_ARRAY));
		c.put("GL_VERTEX_ARRAY_SIZE", new Constant(GL_VERTEX_ARRAY_SIZE));
		c.put("GL_VERTEX_ARRAY_TYPE", new Constant(GL_VERTEX_ARRAY_TYPE));
		c.put("GL_VERTEX_ARRAY_STRIDE", new Constant(GL_VERTEX_ARRAY_STRIDE));
		c.put("GL_NORMAL_ARRAY_TYPE", new Constant(GL_NORMAL_ARRAY_TYPE));
		c.put("GL_NORMAL_ARRAY_STRIDE", new Constant(GL_NORMAL_ARRAY_STRIDE));
		c.put("GL_COLOR_ARRAY_SIZE", new Constant(GL_COLOR_ARRAY_SIZE));
		c.put("GL_COLOR_ARRAY_TYPE", new Constant(GL_COLOR_ARRAY_TYPE));
		c.put("GL_COLOR_ARRAY_STRIDE", new Constant(GL_COLOR_ARRAY_STRIDE));
		c.put("GL_INDEX_ARRAY_TYPE", new Constant(GL_INDEX_ARRAY_TYPE));
		c.put("GL_INDEX_ARRAY_STRIDE", new Constant(GL_INDEX_ARRAY_STRIDE));
		c.put("GL_TEXTURE_COORD_ARRAY_SIZE", new Constant(GL_TEXTURE_COORD_ARRAY_SIZE));
		c.put("GL_TEXTURE_COORD_ARRAY_TYPE", new Constant(GL_TEXTURE_COORD_ARRAY_TYPE));
		c.put("GL_TEXTURE_COORD_ARRAY_STRIDE", new Constant(GL_TEXTURE_COORD_ARRAY_STRIDE));
		c.put("GL_EDGE_FLAG_ARRAY_STRIDE", new Constant(GL_EDGE_FLAG_ARRAY_STRIDE));
		c.put("GL_VERTEX_ARRAY_POINTER", new Constant(GL_VERTEX_ARRAY_POINTER));
		c.put("GL_NORMAL_ARRAY_POINTER", new Constant(GL_NORMAL_ARRAY_POINTER));
		c.put("GL_COLOR_ARRAY_POINTER", new Constant(GL_COLOR_ARRAY_POINTER));
		c.put("GL_INDEX_ARRAY_POINTER", new Constant(GL_INDEX_ARRAY_POINTER));
		c.put("GL_TEXTURE_COORD_ARRAY_POINTER", new Constant(GL_TEXTURE_COORD_ARRAY_POINTER));
		c.put("GL_EDGE_FLAG_ARRAY_POINTER", new Constant(GL_EDGE_FLAG_ARRAY_POINTER));
		c.put("GL_V2F", new Constant(GL_V2F));
		c.put("GL_V3F", new Constant(GL_V3F));
		c.put("GL_C4UB_V2F", new Constant(GL_C4UB_V2F));
		c.put("GL_C4UB_V3F", new Constant(GL_C4UB_V3F));
		c.put("GL_C3F_V3F", new Constant(GL_C3F_V3F));
		c.put("GL_N3F_V3F", new Constant(GL_N3F_V3F));
		c.put("GL_C4F_N3F_V3F", new Constant(GL_C4F_N3F_V3F));
		c.put("GL_T2F_V3F", new Constant(GL_T2F_V3F));
		c.put("GL_T4F_V4F", new Constant(GL_T4F_V4F));
		c.put("GL_T2F_C4UB_V3F", new Constant(GL_T2F_C4UB_V3F));
		c.put("GL_T2F_C3F_V3F", new Constant(GL_T2F_C3F_V3F));
		c.put("GL_T2F_N3F_V3F", new Constant(GL_T2F_N3F_V3F));
		c.put("GL_T2F_C4F_N3F_V3F", new Constant(GL_T2F_C4F_N3F_V3F));
		c.put("GL_T4F_C4F_N3F_V4F", new Constant(GL_T4F_C4F_N3F_V4F));
		/*
		c.put("GL_EXT_vertex_array", GL_EXT_vertex_array));
		c.put("GL_EXT_bgra", GL_EXT_bgra));
		c.put("GL_EXT_paletted_texture", GL_EXT_paletted_texture));
		c.put("GL_WIN_swap_hint", GL_WIN_swap_hint));
		c.put("GL_WIN_draw_range_elements", GL_WIN_draw_range_elements));
		c.put("GL_VERTEX_ARRAY_EXT", GL_VERTEX_ARRAY_EXT));
		c.put("GL_NORMAL_ARRAY_EXT", GL_NORMAL_ARRAY_EXT));
		c.put("GL_COLOR_ARRAY_EXT", GL_COLOR_ARRAY_EXT));
		c.put("GL_INDEX_ARRAY_EXT", GL_INDEX_ARRAY_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_EXT", GL_TEXTURE_COORD_ARRAY_EXT));
		c.put("GL_EDGE_FLAG_ARRAY_EXT", GL_EDGE_FLAG_ARRAY_EXT));
		c.put("GL_VERTEX_ARRAY_SIZE_EXT", GL_VERTEX_ARRAY_SIZE_EXT));
		c.put("GL_VERTEX_ARRAY_TYPE_EXT", GL_VERTEX_ARRAY_TYPE_EXT));
		c.put("GL_VERTEX_ARRAY_STRIDE_EXT", GL_VERTEX_ARRAY_STRIDE_EXT));
		c.put("GL_VERTEX_ARRAY_COUNT_EXT", GL_VERTEX_ARRAY_COUNT_EXT));
		c.put("GL_NORMAL_ARRAY_TYPE_EXT", GL_NORMAL_ARRAY_TYPE_EXT));
		c.put("GL_NORMAL_ARRAY_STRIDE_EXT", GL_NORMAL_ARRAY_STRIDE_EXT));
		c.put("GL_NORMAL_ARRAY_COUNT_EXT", GL_NORMAL_ARRAY_COUNT_EXT));
		c.put("GL_COLOR_ARRAY_SIZE_EXT", GL_COLOR_ARRAY_SIZE_EXT));
		c.put("GL_COLOR_ARRAY_TYPE_EXT", GL_COLOR_ARRAY_TYPE_EXT));
		c.put("GL_COLOR_ARRAY_STRIDE_EXT", GL_COLOR_ARRAY_STRIDE_EXT));
		c.put("GL_COLOR_ARRAY_COUNT_EXT", GL_COLOR_ARRAY_COUNT_EXT));
		c.put("GL_INDEX_ARRAY_TYPE_EXT", GL_INDEX_ARRAY_TYPE_EXT));
		c.put("GL_INDEX_ARRAY_STRIDE_EXT", GL_INDEX_ARRAY_STRIDE_EXT));
		c.put("GL_INDEX_ARRAY_COUNT_EXT", GL_INDEX_ARRAY_COUNT_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_SIZE_EXT", GL_TEXTURE_COORD_ARRAY_SIZE_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_TYPE_EXT", GL_TEXTURE_COORD_ARRAY_TYPE_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_STRIDE_EXT", GL_TEXTURE_COORD_ARRAY_STRIDE_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_COUNT_EXT", GL_TEXTURE_COORD_ARRAY_COUNT_EXT));
		c.put("GL_EDGE_FLAG_ARRAY_STRIDE_EXT", GL_EDGE_FLAG_ARRAY_STRIDE_EXT));
		c.put("GL_EDGE_FLAG_ARRAY_COUNT_EXT", GL_EDGE_FLAG_ARRAY_COUNT_EXT));
		c.put("GL_VERTEX_ARRAY_POINTER_EXT", GL_VERTEX_ARRAY_POINTER_EXT));
		c.put("GL_NORMAL_ARRAY_POINTER_EXT", GL_NORMAL_ARRAY_POINTER_EXT));
		c.put("GL_COLOR_ARRAY_POINTER_EXT", GL_COLOR_ARRAY_POINTER_EXT));
		c.put("GL_INDEX_ARRAY_POINTER_EXT", GL_INDEX_ARRAY_POINTER_EXT));
		c.put("GL_TEXTURE_COORD_ARRAY_POINTER_EXT", GL_TEXTURE_COORD_ARRAY_POINTER_EXT));
		c.put("GL_EDGE_FLAG_ARRAY_POINTER_EXT", GL_EDGE_FLAG_ARRAY_POINTER_EXT));
		c.put("GL_DOUBLE_EXT", GL_DOUBLE_EXT));
		c.put("GL_BGR_EXT", GL_BGR_EXT));
		c.put("GL_BGRA_EXT", GL_BGRA_EXT));
		c.put("GL_COLOR_TABLE_FORMAT_EXT", GL_COLOR_TABLE_FORMAT_EXT));
		c.put("GL_COLOR_TABLE_WIDTH_EXT", GL_COLOR_TABLE_WIDTH_EXT));
		c.put("GL_COLOR_TABLE_RED_SIZE_EXT", GL_COLOR_TABLE_RED_SIZE_EXT));
		c.put("GL_COLOR_TABLE_GREEN_SIZE_EXT", GL_COLOR_TABLE_GREEN_SIZE_EXT));
		c.put("GL_COLOR_TABLE_BLUE_SIZE_EXT", GL_COLOR_TABLE_BLUE_SIZE_EXT));
		c.put("GL_COLOR_TABLE_ALPHA_SIZE_EXT", GL_COLOR_TABLE_ALPHA_SIZE_EXT));
		c.put("GL_COLOR_TABLE_LUMINANCE_SIZE_EXT", GL_COLOR_TABLE_LUMINANCE_SIZE_EXT));
		c.put("GL_COLOR_TABLE_INTENSITY_SIZE_EXT", GL_COLOR_TABLE_INTENSITY_SIZE_EXT));
		c.put("GL_COLOR_INDEX1_EXT", GLExtensions.GL_COLOR_INDEX1_EXT));
		c.put("GL_COLOR_INDEX2_EXT",GL_COLOR_INDEX2_EXT));
		c.put("GL_COLOR_INDEX4_EXT", GL_COLOR_INDEX4_EXT));
		c.put("GL_COLOR_INDEX8_EXT", GL_COLOR_INDEX8_EXT));
		c.put("GL_COLOR_INDEX12_EXT", GL_COLOR_INDEX12_EXT));
		c.put("GL_COLOR_INDEX16_EXT", GL_COLOR_INDEX16_EXT));
		c.put("GL_MAX_ELEMENTS_VERTICES_WIN", GL_MAX_ELEMENTS_VERTICES_WIN));
		c.put("GL_MAX_ELEMENTS_INDICES_WIN", GL_MAX_ELEMENTS_INDICES_WIN));
		c.put("GL_PHONG_WIN", GL_PHONG_WIN));
		c.put("GL_PHONG_HINT_WIN",GL_PHONG_HINT_WIN));
		c.put("GL_FOG_SPECULAR_TEXTURE_WIN", GL_FOG_SPECULAR_TEXTURE_WIN));

		c.put("GL_LOGIC_OP", new Constant(GL_LOGIC_OP));
		c.put("GL_TEXTURE_COMPONENTS",new Constant(GL_TEXTURE_COMPONENTS));
		*/
		return c;
	}

	@Override
	public Map<String, FunctionSpecification[]> specs() {
		Map<String, FunctionSpecification[]> s = new HashMap<>();

		s.put("glAccum", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglAccum.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glAlphaFunc", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglAlphaFunc.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glAreTexturesResident", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglAreTexturesResident.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glArrayElement", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglArrayElement.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glBindTexture", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglBindTexture.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glBlendFunc", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglBlendFunc.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCallList", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCallList.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClear", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClear.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClearAccum", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClearAccum.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClearColor", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClearColor.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClearDepth", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClearDepth.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClearIndex", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClearIndex.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClearStencil", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClearStencil.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glClipPlane", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglClipPlane.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglClipPlane_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3b", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3b.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3bv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3bv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3bv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3ub", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3ub.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3ubv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3ubv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3ubv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3ui", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3ui.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3uiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3uiv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3uiv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3us", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3us.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor3usv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor3usv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor3usv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4b", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4b.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4bv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4bv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4bv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4ub", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4ub.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4ubv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4ubv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4ubv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4ui", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4ui.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4uiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4uiv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4uiv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4us", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4us.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColor4usv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColor4usv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglColor4usv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColorMask", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColorMask.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glColorMaterial", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglColorMaterial.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCopyPixels", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCopyPixels.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCopyTexImage1D", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCopyTexImage1D.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCopyTexImage2D", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCopyTexImage2D.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCopyTexSubImage1D", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCopyTexSubImage1D.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCopyTexSubImage2D", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCopyTexSubImage2D.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glCullFace", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglCullFace.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDepthFunc", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDepthFunc.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDepthMask", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDepthMask.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDepthRange", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDepthRange.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDisable", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDisable.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDisableClientState", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDisableClientState.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDrawArrays", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDrawArrays.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glDrawBuffer", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglDrawBuffer.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEdgeFlag", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEdgeFlag.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEdgeFlagv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEdgeFlagv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglEdgeFlagv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEnable", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEnable.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEnableClientState", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEnableClientState.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEndList", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEndList.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord1d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord1d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord1dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord1dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglEvalCoord1dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord1f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord1f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord1fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord1fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglEvalCoord1fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord2d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord2d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord2dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord2dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglEvalCoord2dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord2f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord2f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalCoord2fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalCoord2fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglEvalCoord2fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalMesh1", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalMesh1.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalMesh2", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalMesh2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalPoint1", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalPoint1.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glEvalPoint2", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglEvalPoint2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFeedbackBuffer", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFeedbackBuffer.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFinish", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFinish.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFlush", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFlush.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFogf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFogf.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFogfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFogfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglFogfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFogi", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFogi.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFogiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFogiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglFogiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFrontFace", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFrontFace.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glFrustum", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglFrustum.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetBooleanv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetBooleanv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetBooleanv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetClipPlane", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetClipPlane.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetClipPlane_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetDoublev", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetDoublev.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetDoublev_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetError", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetError.class,
					new ParamTypeList(new ValType[] {}),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetFloatv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetFloatv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetFloatv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetIntegerv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetIntegerv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetIntegerv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetLightfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetLightfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetLightfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetLightiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetLightiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetLightiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetMaterialfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetMaterialfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetMaterialfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetMaterialiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetMaterialiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetMaterialiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetPixelMapuiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetPixelMapuiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetPixelMapuiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexEnvfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexEnvfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexEnvfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexEnviv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexEnviv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexEnviv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexGendv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexGendv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexGendv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexGenfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexGenfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexGenfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexGeniv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexGeniv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexGeniv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexLevelParameterfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexLevelParameterfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexLevelParameterfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexLevelParameteriv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexLevelParameteriv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexLevelParameteriv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexParameterfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexParameterfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexParameterfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glGetTexParameteriv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglGetTexParameteriv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglGetTexParameteriv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glHint", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglHint.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexMask", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexMask.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexd", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexd.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexdv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexdv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglIndexdv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexf.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexfv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglIndexfv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexi", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexi.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexiv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglIndexiv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexs", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexs.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexsv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexsv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglIndexsv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexub", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexub.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIndexubv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIndexubv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglIndexubv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glInitNames", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglInitNames.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIsEnabled", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIsEnabled.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIsList", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIsList.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glIsTexture", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglIsTexture.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightModelf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightModelf.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightModelfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightModelfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglLightModelfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightModeli", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightModeli.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightModeliv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightModeliv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglLightModeliv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglLightfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLighti", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLighti.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLightiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLightiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglLightiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLineStipple", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLineStipple.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLineWidth", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLineWidth.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glListBase", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglListBase.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLoadIdentity", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLoadIdentity.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLoadName", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLoadName.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glLogicOp", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglLogicOp.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMapGrid1d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMapGrid1d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMapGrid1f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMapGrid1f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMapGrid2d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMapGrid2d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMapGrid2f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMapGrid2f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMaterialf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMaterialf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMaterialfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMaterialfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglMaterialfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMateriali", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMateriali.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMaterialiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMaterialiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglMaterialiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glMatrixMode", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglMatrixMode.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNewList", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNewList.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3b", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3b.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3bv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3bv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglNormal3bv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglNormal3dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglNormal3fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglNormal3iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glNormal3sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglNormal3sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglNormal3sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glOrtho", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglOrtho.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPassThrough", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPassThrough.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPixelStoref", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPixelStoref.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPixelStorei", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPixelStorei.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPixelTransferf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPixelTransferf.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPixelTransferi", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPixelTransferi.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPixelZoom", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPixelZoom.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPointSize", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPointSize.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPolygonMode", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPolygonMode.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPolygonOffset", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPolygonOffset.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPopAttrib", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPopAttrib.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPopClientAttrib", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPopClientAttrib.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPopMatrix", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPopMatrix.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPopName", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPopName.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPrioritizeTextures", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPrioritizeTextures.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPushAttrib", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPushAttrib.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPushClientAttrib", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPushClientAttrib.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPushMatrix", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPushMatrix.class,
					new ParamTypeList(new ValType[] {}),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glPushName", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglPushName.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos2dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos2fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2i.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos2iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2s.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos2sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos2sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos2sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos3dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos3fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos3iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos3sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos3sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos3sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos4dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos4fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos4iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRasterPos4sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRasterPos4sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRasterPos4sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glReadBuffer", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglReadBuffer.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectd", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectd.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectdv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectdv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectdv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectdv_3.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectdv_4.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectfv_3.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectfv_4.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRecti", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRecti.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectiv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectiv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectiv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectiv_3.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectiv_4.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRects", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRects.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRectsv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRectsv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectsv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectsv_3.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglRectsv_4.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRenderMode", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRenderMode.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					true,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRotated", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRotated.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glRotatef", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglRotatef.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glScaled", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglScaled.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glScalef", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglScalef.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glScissor", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglScissor.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glSelectBuffer", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglSelectBuffer.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glShadeModel", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglShadeModel.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glStencilFunc", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglStencilFunc.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glStencilMask", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglStencilMask.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glStencilOp", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglStencilOp.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord1dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord1fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1i.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord1iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1s.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord1sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord1sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord1sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord2dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord2fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2i.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord2iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2s.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord2sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord2sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord2sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord3dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord3fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord3iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord3sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord3sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord3sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord4dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord4fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord4iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexCoord4sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexCoord4sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexCoord4sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexEnvf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexEnvf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexEnvfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexEnvfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexEnvfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexEnvi", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexEnvi.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexEnviv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexEnviv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexEnviv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGend", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGend.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGendv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGendv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexGendv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGenf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGenf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGenfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGenfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexGenfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGeni", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGeni.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexGeniv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexGeniv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexGeniv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexParameterf", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexParameterf.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexParameterfv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexParameterfv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexParameterfv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexParameteri", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexParameteri.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTexParameteriv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTexParameteriv.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglTexParameteriv_2.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTranslated", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTranslated.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glTranslatef", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglTranslatef.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2d.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex2dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2f.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL), new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex2fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2i.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex2iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2s.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT), new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex2sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex2sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex2sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex3dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex3fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex3iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex3sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex3sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex3sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4d", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4d.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4dv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4dv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex4dv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4f", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4f.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL),
							new ValType(BasicValType.VTP_REAL)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4fv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4fv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex4fv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_REAL, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4i", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4i.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4iv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4iv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex4iv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4s", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4s.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glVertex4sv", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglVertex4sv.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null),
			new FunctionSpecification(
					WrapglVertex4sv_2.class,
					new ParamTypeList(new ValType(BasicValType.VTP_INT, (byte) 0, (byte) 1, true)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
					false,
					false,
					null)
		});
		s.put("glViewport", new FunctionSpecification[] {
			new FunctionSpecification(
					WrapglViewport.class,
					new ParamTypeList(
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT),
							new ValType(BasicValType.VTP_INT)),
					true,
					false,
					new ValType(BasicValType.VTP_INT),
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

	public static final class WrapglAccum implements Function {
		public void run(TomVM vm) {
			glAccum(vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglAlphaFunc implements Function {
		public void run(TomVM vm) {
			glAlphaFunc(vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglAreTexturesResident implements Function {

		public void run(TomVM vm) {
			if (!Routines.validateSizeParam(vm, 3)) {
				return;
			}

			ByteBuffer a1 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());
			ByteBuffer a2 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());

			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2.array(),
					vm.getIntParam(3));

			IntBuffer b1 = a1.asIntBuffer();
			b1.limit(vm.getIntParam(3));

			vm.getReg().setIntVal(glAreTexturesResident(b1, a2) ? 1 : 0);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2.array(),
					vm.getIntParam(3));
		}
	}

	public static final class WrapglArrayElement implements Function {

		public void run(TomVM vm) {
			glArrayElement(vm.getIntParam(1));
		}
	}

	public static final class WrapglBindTexture implements Function {

		public void run(TomVM vm) {
			glBindTexture(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglBlendFunc implements Function {

		public void run(TomVM vm) {

			glBlendFunc(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglCallList implements Function {

		public void run(TomVM vm) {
			glCallList(vm.getIntParam(1));
		}
	}

	public static final class WrapglClear implements Function {

		public void run(TomVM vm) {
			glClear(vm.getIntParam(1));
		}
	}

	public static final class WrapglClearAccum implements Function {

		public void run(TomVM vm) {
			glClearAccum(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglClearColor implements Function {

		public void run(TomVM vm) {
			glClearColor(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglClearDepth implements Function {

		public void run(TomVM vm) {
			glClearDepth(vm.getRealParam(1));
		}
	}

	public static final class WrapglClearIndex implements Function {

		public void run(TomVM vm) {
			glClearIndex(vm.getRealParam(1));
		}
	}

	public static final class WrapglClearStencil implements Function {

		public void run(TomVM vm) {
			glClearStencil(vm.getIntParam(1));
		}
	}

	public final class WrapglClipPlane implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glClipPlane(vm.getIntParam(2), doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglClipPlane_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.put(0, vm.getRefParam(1).getRealVal());
			doubleBuffer16.rewind();
			glClipPlane(vm.getIntParam(2), doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglColor3b implements Function {

		public void run(TomVM vm) {
			glColor3b(
					vm.getIntParam(3).byteValue(),
					vm.getIntParam(2).byteValue(),
					vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglColor3bv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor3bv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3bv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor3bv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglColor3d implements Function {

		public void run(TomVM vm) {
			glColor3d(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglColor3dv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glColor3dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3dv_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glColor3dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglColor3f implements Function {

		public void run(TomVM vm) {
			glColor3f(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglColor3fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glColor3fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glColor3fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglColor3i implements Function {

		public void run(TomVM vm) {
			glColor3i(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglColor3iv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor3iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3iv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor3iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglColor3s implements Function {

		public void run(TomVM vm) {
			glColor3s(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglColor3sv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor3sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3sv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor3sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglColor3ub implements Function {

		public void run(TomVM vm) {
			glColor3ub(
					vm.getIntParam(3).byteValue(),
					vm.getIntParam(2).byteValue(),
					vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglColor3ubv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor3ubv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3ubv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor3ubv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglColor3ui implements Function {

		public void run(TomVM vm) {
			glColor3ui(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglColor3uiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor3uiv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3uiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor3uiv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglColor3us implements Function {

		public void run(TomVM vm) {
			glColor3us(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglColor3usv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor3usv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor3usv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor3usv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglColor4b implements Function {

		public void run(TomVM vm) {
			glColor4b(
					vm.getIntParam(4).byteValue(),
					vm.getIntParam(3).byteValue(),
					vm.getIntParam(2).byteValue(),
					vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglColor4bv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor4bv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4bv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor4bv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglColor4d implements Function {

		public void run(TomVM vm) {
			glColor4d(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglColor4dv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glColor4dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4dv_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glColor4dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglColor4f implements Function {

		public void run(TomVM vm) {
			glColor4f(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglColor4fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glColor4fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glColor4fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglColor4i implements Function {

		public void run(TomVM vm) {
			glColor4i(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglColor4iv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor4iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4iv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor4iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglColor4s implements Function {

		public void run(TomVM vm) {
			glColor4s(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglColor4sv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor4sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4sv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor4sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglColor4ub implements Function {

		public void run(TomVM vm) {
			glColor4ub(
					vm.getIntParam(4).byteValue(),
					vm.getIntParam(3).byteValue(),
					vm.getIntParam(2).byteValue(),
					vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglColor4ubv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor4ubv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4ubv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glColor4ubv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglColor4ui implements Function {

		public void run(TomVM vm) {
			glColor4ui(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglColor4uiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor4uiv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4uiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glColor4uiv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglColor4us implements Function {

		public void run(TomVM vm) {
			glColor4us(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglColor4usv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor4usv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglColor4usv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glColor4usv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglColorMask implements Function {

		public void run(TomVM vm) {
			glColorMask(vm.getIntParam(4) == 1, vm.getIntParam(3) == 1, vm.getIntParam(2) == 1, vm.getIntParam(1) == 1);
		}
	}

	public static final class WrapglColorMaterial implements Function {

		public void run(TomVM vm) {
			glColorMaterial(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglCopyPixels implements Function {

		public void run(TomVM vm) {
			glCopyPixels(vm.getIntParam(5), vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglCopyTexImage1D implements Function {

		public void run(TomVM vm) {
			glCopyTexImage1D(
					vm.getIntParam(7),
					vm.getIntParam(6),
					vm.getIntParam(5),
					vm.getIntParam(4),
					vm.getIntParam(3),
					vm.getIntParam(2),
					vm.getIntParam(1));
		}
	}

	public static final class WrapglCopyTexImage2D implements Function {

		public void run(TomVM vm) {
			glCopyTexImage2D(
					vm.getIntParam(8),
					vm.getIntParam(7),
					vm.getIntParam(6),
					vm.getIntParam(5),
					vm.getIntParam(4),
					vm.getIntParam(3),
					vm.getIntParam(2),
					vm.getIntParam(1));
		}
	}

	public static final class WrapglCopyTexSubImage1D implements Function {

		public void run(TomVM vm) {
			glCopyTexSubImage1D(
					vm.getIntParam(6),
					vm.getIntParam(5),
					vm.getIntParam(4),
					vm.getIntParam(3),
					vm.getIntParam(2),
					vm.getIntParam(1));
		}
	}

	public static final class WrapglCopyTexSubImage2D implements Function {

		public void run(TomVM vm) {
			glCopyTexSubImage2D(
					vm.getIntParam(8),
					vm.getIntParam(7),
					vm.getIntParam(6),
					vm.getIntParam(5),
					vm.getIntParam(4),
					vm.getIntParam(3),
					vm.getIntParam(2),
					vm.getIntParam(1));
		}
	}

	public static final class WrapglCullFace implements Function {

		public void run(TomVM vm) {
			glCullFace(vm.getIntParam(1));
		}
	}

	public static final class WrapglDepthFunc implements Function {

		public void run(TomVM vm) {
			glDepthFunc(vm.getIntParam(1));
		}
	}

	public static final class WrapglDepthMask implements Function {

		public void run(TomVM vm) {
			glDepthMask(vm.getIntParam(1) == 1);
		}
	}

	public static final class WrapglDepthRange implements Function {

		public void run(TomVM vm) {
			glDepthRange(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglDisable implements Function {

		public void run(TomVM vm) {
			glDisable(vm.getIntParam(1));
		}
	}

	public static final class WrapglDisableClientState implements Function {

		public void run(TomVM vm) {
			glDisableClientState(vm.getIntParam(1));
		}
	}

	public static final class WrapglDrawArrays implements Function {

		public void run(TomVM vm) {
			if (!Routines.validateSizeParam(vm, 1)) {
				return;
			}
			glDrawArrays(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglDrawBuffer implements Function {

		public void run(TomVM vm) {

			glDrawBuffer(vm.getIntParam(1));
		}
	}

	public static final class WrapglEdgeFlag implements Function {

		public void run(TomVM vm) {
			glEdgeFlag(vm.getIntParam(1) == 1);
		}
	}

	public final class WrapglEdgeFlagv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glEdgeFlagv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglEdgeFlagv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glEdgeFlagv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglEnable implements Function {

		public void run(TomVM vm) {
			glEnable(vm.getIntParam(1));
		}
	}

	public static final class WrapglEnableClientState implements Function {

		public void run(TomVM vm) {
			glEnableClientState(vm.getIntParam(1));
		}
	}

	public static final class WrapglEndList implements Function {

		public void run(TomVM vm) {
			glEndList();
		}
	}

	public static final class WrapglEvalCoord1d implements Function {

		public void run(TomVM vm) {
			glEvalCoord1d(vm.getRealParam(1));
		}
	}

	public final class WrapglEvalCoord1dv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glEvalCoord1dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglEvalCoord1dv_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glEvalCoord1dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglEvalCoord1f implements Function {

		public void run(TomVM vm) {
			glEvalCoord1f(vm.getRealParam(1));
		}
	}

	public final class WrapglEvalCoord1fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glEvalCoord1fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglEvalCoord1fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glEvalCoord1fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglEvalCoord2d implements Function {

		public void run(TomVM vm) {
			glEvalCoord2d(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglEvalCoord2dv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glEvalCoord2dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglEvalCoord2dv_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glEvalCoord2dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglEvalCoord2f implements Function {

		public void run(TomVM vm) {
			glEvalCoord2f(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglEvalCoord2fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glEvalCoord2fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglEvalCoord2fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glEvalCoord2fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglEvalMesh1 implements Function {

		public void run(TomVM vm) {
			glEvalMesh1(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglEvalMesh2 implements Function {

		public void run(TomVM vm) {
			glEvalMesh2(vm.getIntParam(5), vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglEvalPoint1 implements Function {

		public void run(TomVM vm) {
			glEvalPoint1(vm.getIntParam(1));
		}
	}

	public static final class WrapglEvalPoint2 implements Function {

		public void run(TomVM vm) {
			glEvalPoint2(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglFeedbackBuffer implements Function {

		public void run(TomVM vm) {
			if (!Routines.validateSizeParam(vm, 3)) {
				return;
			}
			ByteBuffer a1 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());

			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));

			FloatBuffer b1 = a1.asFloatBuffer();
			b1.limit(vm.getIntParam(3));

			glFeedbackBuffer(vm.getIntParam(2), b1);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));
		}
	}

	public static final class WrapglFinish implements Function {

		public void run(TomVM vm) {
			glFinish();
		}
	}

	public static final class WrapglFlush implements Function {

		public void run(TomVM vm) {
			glFlush();
		}
	}

	public static final class WrapglFogf implements Function {

		public void run(TomVM vm) {
			glFogf(vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglFogfv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glFogfv(vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglFogfv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glFogfv(vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglFogi implements Function {

		public void run(TomVM vm) {
			glFogi(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglFogiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glFogiv(vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglFogiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glFogiv(vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglFrontFace implements Function {

		public void run(TomVM vm) {
			glFrontFace(vm.getIntParam(1));
		}
	}

	public static final class WrapglFrustum implements Function {
		public void run(TomVM vm) {
			glFrustum(
					vm.getRealParam(6),
					vm.getRealParam(5),
					vm.getRealParam(4),
					vm.getRealParam(3),
					vm.getRealParam(2),
					vm.getRealParam(1));
		}
	}

	public final class WrapglGetBooleanv implements Function {
		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glGetBooleanv(vm.getIntParam(2), byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetBooleanv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glGetBooleanv(vm.getIntParam(2), byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public final class WrapglGetClipPlane implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetClipPlane(vm.getIntParam(2), doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetClipPlane_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetClipPlane(vm.getIntParam(2), doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public final class WrapglGetDoublev implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetDoublev(vm.getIntParam(2), doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetDoublev_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetDoublev(vm.getIntParam(2), doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglGetError implements Function {

		public void run(TomVM vm) {
			vm.getReg().setIntVal(glGetError());
		}
	}

	public final class WrapglGetFloatv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetFloatv(vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetFloatv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetFloatv(vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetIntegerv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetIntegerv(vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetIntegerv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetIntegerv(vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetLightfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetLightfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetLightfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetLightfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetLightiv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetLightiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetLightiv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetLightiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetMaterialfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetMaterialfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetMaterialfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetMaterialfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetMaterialiv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetMaterialiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetMaterialiv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetMaterialiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetPixelMapuiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetPixelMapuiv(vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetPixelMapuiv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetPixelMapuiv(vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetTexEnvfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexEnvfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexEnvfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexEnvfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetTexEnviv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexEnviv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexEnviv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexEnviv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetTexGendv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetTexGendv(vm.getIntParam(3), vm.getIntParam(2), doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexGendv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glGetTexGendv(vm.getIntParam(3), vm.getIntParam(2), doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public final class WrapglGetTexGenfv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexGenfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexGenfv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexGenfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetTexGeniv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexGeniv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexGeniv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexGeniv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetTexLevelParameterfv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexLevelParameterfv(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexLevelParameterfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexLevelParameterfv(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetTexLevelParameteriv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexLevelParameteriv(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexLevelParameteriv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexLevelParameteriv(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public final class WrapglGetTexParameterfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexParameterfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexParameterfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glGetTexParameterfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public final class WrapglGetTexParameteriv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexParameteriv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglGetTexParameteriv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glGetTexParameteriv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglHint implements Function {

		public void run(TomVM vm) {
			glHint(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglIndexMask implements Function {
		public void run(TomVM vm) {
			glIndexMask(vm.getIntParam(1));
		}
	}

	public static final class WrapglIndexd implements Function {
		public void run(TomVM vm) {
			glIndexd(vm.getRealParam(1));
		}
	}

	public final class WrapglIndexdv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glIndexdv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglIndexdv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glIndexdv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglIndexf implements Function {
		public void run(TomVM vm) {
			glIndexf(vm.getRealParam(1));
		}
	}

	public final class WrapglIndexfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glIndexfv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglIndexfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glIndexfv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglIndexi implements Function {
		public void run(TomVM vm) {
			glIndexi(vm.getIntParam(1));
		}
	}

	public final class WrapglIndexiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glIndexiv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglIndexiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glIndexiv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglIndexs implements Function {
		public void run(TomVM vm) {
			glIndexs(vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglIndexsv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glIndexsv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglIndexsv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glIndexsv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglIndexub implements Function {
		public void run(TomVM vm) {
			glIndexub(vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglIndexubv implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glIndexubv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglIndexubv_2 implements Function {

		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glIndexubv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglInitNames implements Function {

		public void run(TomVM vm) {
			glInitNames();
		}
	}

	public static final class WrapglIsEnabled implements Function {
		public void run(TomVM vm) {
			vm.getReg().setIntVal(glIsEnabled(vm.getIntParam(1)) ? 1 : 0);
		}
	}

	public static final class WrapglIsList implements Function {
		public void run(TomVM vm) {
			vm.getReg().setIntVal(glIsList(vm.getIntParam(1)) ? 1 : 0);
		}
	}

	public static final class WrapglIsTexture implements Function {
		public void run(TomVM vm) {
			vm.getReg().setIntVal(glIsTexture(vm.getIntParam(1)) ? 1 : 0);
		}
	}

	public static final class WrapglLightModelf implements Function {
		public void run(TomVM vm) {
			glLightModelf(vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglLightModelfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glLightModelfv(vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglLightModelfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glLightModelfv(vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglLightModeli implements Function {
		public void run(TomVM vm) {
			glLightModeli(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglLightModeliv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glLightModeliv(vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglLightModeliv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glLightModeliv(vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglLightf implements Function {
		public void run(TomVM vm) {
			glLightf(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglLightfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glLightfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglLightfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glLightfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglLighti implements Function {
		public void run(TomVM vm) {
			glLighti(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglLightiv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glLightiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglLightiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glLightiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglLineStipple implements Function {

		public void run(TomVM vm) {
			glLineStipple(vm.getIntParam(2), vm.getIntParam(1).shortValue());
		}
	}

	public static final class WrapglLineWidth implements Function {

		public void run(TomVM vm) {
			glLineWidth(vm.getRealParam(1));
		}
	}

	public static final class WrapglListBase implements Function {

		public void run(TomVM vm) {
			glListBase(vm.getIntParam(1));
		}
	}

	public static final class WrapglLoadIdentity implements Function {
		public void run(TomVM vm) {
			glLoadIdentity();
		}
	}

	public static final class WrapglLoadName implements Function {
		public void run(TomVM vm) {
			glLoadName(vm.getIntParam(1));
		}
	}

	public static final class WrapglLogicOp implements Function {

		public void run(TomVM vm) {
			glLogicOp(vm.getIntParam(1));
		}
	}

	public static final class WrapglMapGrid1d implements Function {

		public void run(TomVM vm) {
			glMapGrid1d(vm.getIntParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglMapGrid1f implements Function {
		public void run(TomVM vm) {
			glMapGrid1f(vm.getIntParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglMapGrid2d implements Function {

		public void run(TomVM vm) {
			glMapGrid2d(
					vm.getIntParam(6),
					vm.getRealParam(5),
					vm.getRealParam(4),
					vm.getIntParam(3),
					vm.getRealParam(2),
					vm.getRealParam(1));
		}
	}

	public static final class WrapglMapGrid2f implements Function {

		public void run(TomVM vm) {
			glMapGrid2f(
					vm.getIntParam(6),
					vm.getRealParam(5),
					vm.getRealParam(4),
					vm.getIntParam(3),
					vm.getRealParam(2),
					vm.getRealParam(1));
		}
	}

	public static final class WrapglMaterialf implements Function {

		public void run(TomVM vm) {
			glMaterialf(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglMaterialfv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glMaterialfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglMaterialfv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glMaterialfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglMateriali implements Function {

		public void run(TomVM vm) {
			glMateriali(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglMaterialiv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glMaterialiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglMaterialiv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glMaterialiv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglMatrixMode implements Function {
		public void run(TomVM vm) {
			glMatrixMode(vm.getIntParam(1));
		}
	}

	public static final class WrapglNewList implements Function {
		public void run(TomVM vm) {
			glNewList(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglNormal3b implements Function {
		public void run(TomVM vm) {
			glNormal3b(
					vm.getIntParam(3).byteValue(),
					vm.getIntParam(2).byteValue(),
					vm.getIntParam(1).byteValue());
		}
	}

	public final class WrapglNormal3bv implements Function {
		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glNormal3bv(byteBuffer16);
			byteBuffer16.rewind();
			byteBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglNormal3bv_2 implements Function {
		public void run(TomVM vm) {
			byte[] a = new byte[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			byteBuffer16.rewind();
			byteBuffer16.put(a);
			byteBuffer16.rewind();
			glNormal3bv(byteBuffer16);
			vm.getRefParam(1).setIntVal((int) byteBuffer16.get(0));
		}
	}

	public static final class WrapglNormal3d implements Function {
		public void run(TomVM vm) {
			glNormal3d(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglNormal3dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glNormal3dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglNormal3dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glNormal3dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglNormal3f implements Function {
		public void run(TomVM vm) {
			glNormal3f(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglNormal3fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glNormal3fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglNormal3fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glNormal3fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglNormal3i implements Function {
		public void run(TomVM vm) {
			glNormal3i(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglNormal3iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glNormal3iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglNormal3iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glNormal3iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglNormal3s implements Function {
		public void run(TomVM vm) {
			glNormal3s(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglNormal3sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glNormal3sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglNormal3sv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glNormal3sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglOrtho implements Function {
		public void run(TomVM vm) {
			glOrtho(
					vm.getRealParam(6),
					vm.getRealParam(5),
					vm.getRealParam(4),
					vm.getRealParam(3),
					vm.getRealParam(2),
					vm.getRealParam(1));
		}
	}

	public static final class WrapglPassThrough implements Function {
		public void run(TomVM vm) {
			glPassThrough(vm.getRealParam(1));
		}
	}

	public static final class WrapglPixelStoref implements Function {

		public void run(TomVM vm) {
			glPixelStoref(vm.getIntParam(2), vm.getIntParam(1));
			// TODO Original source used this line instead
			// glPixelStoref(vm.GetIntParam(2), vm.GetRealParam(1));
		}
	}

	public static final class WrapglPixelStorei implements Function {

		public void run(TomVM vm) {
			glPixelStorei(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglPixelTransferf implements Function {

		public void run(TomVM vm) {
			glPixelTransferf(vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglPixelTransferi implements Function {

		public void run(TomVM vm) {
			glPixelTransferi(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglPixelZoom implements Function {
		public void run(TomVM vm) {
			glPixelZoom(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglPointSize implements Function {

		public void run(TomVM vm) {
			glPointSize(vm.getRealParam(1));
		}
	}

	public static final class WrapglPolygonMode implements Function {
		public void run(TomVM vm) {
			glPolygonMode(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglPolygonOffset implements Function {

		public void run(TomVM vm) {
			glPolygonOffset(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglPopAttrib implements Function {

		public void run(TomVM vm) {
			glPopAttrib();
		}
	}

	public static final class WrapglPopClientAttrib implements Function {

		public void run(TomVM vm) {
			glPopClientAttrib();
		}
	}

	public static final class WrapglPopMatrix implements Function {
		public void run(TomVM vm) {
			glPopMatrix();
		}
	}

	public static final class WrapglPopName implements Function {
		public void run(TomVM vm) {
			glPopName();
		}
	}

	public static final class WrapglPrioritizeTextures implements Function {
		public void run(TomVM vm) {
			if (!Routines.validateSizeParam(vm, 3)) {
				return;
			}
			ByteBuffer a1 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());
			ByteBuffer a2 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2.array(),
					vm.getIntParam(3));

			IntBuffer b1 = a1.asIntBuffer();
			FloatBuffer b2 = a2.asFloatBuffer();
			b1.limit(vm.getIntParam(3));
			b2.limit(vm.getIntParam(3));

			glPrioritizeTextures(b1, b2);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1.array(),
					vm.getIntParam(3));
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2.array(),
					vm.getIntParam(3));
		}
	}

	public static final class WrapglPushAttrib implements Function {
		public void run(TomVM vm) {
			glPushAttrib(vm.getIntParam(1));
		}
	}

	public static final class WrapglPushClientAttrib implements Function {

		public void run(TomVM vm) {
			glPushClientAttrib(vm.getIntParam(1));
		}
	}

	public static final class WrapglPushMatrix implements Function {
		public void run(TomVM vm) {
			glPushMatrix();
		}
	}

	public static final class WrapglPushName implements Function {
		public void run(TomVM vm) {
			glPushName(vm.getIntParam(1));
		}
	}

	public static final class WrapglRasterPos2d implements Function {
		public void run(TomVM vm) {
			glRasterPos2d(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos2dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos2dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos2dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos2dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos2f implements Function {

		public void run(TomVM vm) {
			glRasterPos2f(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos2fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos2fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos2fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos2fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos2i implements Function {
		public void run(TomVM vm) {
			glRasterPos2i(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglRasterPos2iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos2iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos2iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos2iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos2s implements Function {

		public void run(TomVM vm) {
			glRasterPos2s(vm.getIntParam(2).shortValue(), vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglRasterPos2sv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos2sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos2sv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos2sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos3d implements Function {

		public void run(TomVM vm) {
			glRasterPos3d(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos3dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos3dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos3dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos3dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos3f implements Function {
		public void run(TomVM vm) {
			glRasterPos3f(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos3fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos3fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos3fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos3fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos3i implements Function {
		public void run(TomVM vm) {
			glRasterPos3i(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglRasterPos3iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos3iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos3iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos3iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos3s implements Function {
		public void run(TomVM vm) {
			glRasterPos3s(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglRasterPos3sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos3sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos3sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos3sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos4d implements Function {
		public void run(TomVM vm) {
			glRasterPos4d(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos4dv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos4dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos4dv_2 implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRasterPos4dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos4f implements Function {

		public void run(TomVM vm) {
			glRasterPos4f(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRasterPos4fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos4fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos4fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRasterPos4fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos4i implements Function {
		public void run(TomVM vm) {
			glRasterPos4i(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglRasterPos4iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos4iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos4iv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRasterPos4iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglRasterPos4s implements Function {

		public void run(TomVM vm) {
			glRasterPos4s(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglRasterPos4sv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos4sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglRasterPos4sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRasterPos4sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglReadBuffer implements Function {
		public void run(TomVM vm) {
			glReadBuffer(vm.getIntParam(1));
		}
	}

	public static final class WrapglRectd implements Function {

		public void run(TomVM vm) {
			glRectd(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRectdv implements Function {

		public void run(TomVM vm) {
			double[] a = new double[16];
			DoubleBuffer a2 = BufferUtils.createDoubleBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRectdv(doubleBuffer16, a2);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectdv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getRealVal();
			DoubleBuffer a2 = BufferUtils.createDoubleBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRectdv(doubleBuffer16, a2);
			vm.getRefParam(2).setRealVal((float) doubleBuffer16.get(0));
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectdv_3 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			DoubleBuffer a2 = BufferUtils.createDoubleBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getRealVal());
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRectdv(doubleBuffer16, a2);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			vm.getRefParam(1).setRealVal((float) a2.get(0));
		}
	}

	public final class WrapglRectdv_4 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getRealVal();
			DoubleBuffer a2 = BufferUtils.createDoubleBuffer(16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getRealVal());
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glRectdv(doubleBuffer16, a2);
			vm.getRefParam(2).setRealVal((float) doubleBuffer16.get(0));
			vm.getRefParam(1).setRealVal((float) a2.get(0));
		}
	}

	public static final class WrapglRectf implements Function {

		public void run(TomVM vm) {
			glRectf(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglRectfv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			FloatBuffer a2 = BufferUtils.createFloatBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRectfv(floatBuffer16, a2);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			FloatBuffer a2 = BufferUtils.createFloatBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
			glRectfv(floatBuffer16, a2);
			vm.getRefParam(2).setRealVal(floatBuffer16.get(0));

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectfv_3 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			FloatBuffer a2 = BufferUtils.createFloatBuffer(16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getRealVal());
			glRectfv(floatBuffer16, a2);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);

			vm.getRefParam(1).setRealVal(a2.get(0));
		}
	}

	public final class WrapglRectfv_4 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getRealVal();
			FloatBuffer a2 = BufferUtils.createFloatBuffer(16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getRealVal());

			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glRectfv(floatBuffer16, a2);
			vm.getRefParam(2).setRealVal(floatBuffer16.get(0));

			vm.getRefParam(1).setRealVal(a2.get(0));
		}
	}

	public static final class WrapglRecti implements Function {
		public void run(TomVM vm) {
			glRecti(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglRectiv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			IntBuffer a2 = BufferUtils.createIntBuffer(16);

			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRectiv(intBuffer16, a2);

			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectiv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getIntVal();
			IntBuffer a2 = BufferUtils.createIntBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);

			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRectiv(intBuffer16, a2);
			vm.getRefParam(2).setIntVal(intBuffer16.get(0));

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectiv_3 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			IntBuffer a2 = BufferUtils.createIntBuffer(16);

			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getIntVal());
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glRectiv(intBuffer16, a2);

			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);

			vm.getRefParam(1).setIntVal(a2.get(0));
		}
	}

	public final class WrapglRectiv_4 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = vm.getRefParam(2).getIntVal();
			IntBuffer a2 = BufferUtils.createIntBuffer(16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, vm.getRefParam(1).getIntVal());

			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();

			glRectiv(intBuffer16, a2);
			vm.getRefParam(2).setIntVal(intBuffer16.get(0));
			vm.getRefParam(1).setIntVal(a2.get(0));
		}
	}

	public static final class WrapglRects implements Function {
		public void run(TomVM vm) {
			glRects(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglRectsv implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			ShortBuffer a2 = BufferUtils.createShortBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRectsv(shortBuffer16, a2);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectsv_2 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = (short) vm.getRefParam(2).getIntVal();
			ShortBuffer a2 = BufferUtils.createShortBuffer(16);
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRectsv(shortBuffer16, a2);
			vm.getRefParam(2).setIntVal((int) shortBuffer16.get(0));

			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a2,
					16);
		}
	}

	public final class WrapglRectsv_3 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			ShortBuffer a2 = BufferUtils.createShortBuffer(16);

			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, (short) vm.getRefParam(1).getIntVal());
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRectsv(shortBuffer16, a2);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(2),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);

			vm.getRefParam(1).setIntVal((int) a2.get(0));
		}
	}

	public final class WrapglRectsv_4 implements Function {

		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(2)) {
				return;
			}
			a[0] = (short) vm.getRefParam(2).getIntVal();
			ShortBuffer a2 = BufferUtils.createShortBuffer(16);

			Data.zeroArray(a2, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a2.put(0, (short) vm.getRefParam(1).getIntVal());
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glRectsv(shortBuffer16, a2);
			vm.getRefParam(2).setIntVal((int) shortBuffer16.get(0));

			vm.getRefParam(1).setIntVal((int) a2.get(0));
		}
	}

	public static final class WrapglRenderMode implements Function {
		public void run(TomVM vm) {
			vm.getReg().setIntVal(glRenderMode(vm.getIntParam(1)));
		}
	}

	public static final class WrapglRotated implements Function {
		public void run(TomVM vm) {
			glRotated(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglRotatef implements Function {
		public void run(TomVM vm) {
			glRotatef(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglScaled implements Function {
		public void run(TomVM vm) {
			glScaled(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglScalef implements Function {
		public void run(TomVM vm) {
			glScalef(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglScissor implements Function {
		public void run(TomVM vm) {
			glScissor(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglSelectBuffer implements Function {
		public void run(TomVM vm) {
			if (!Routines.validateSizeParam(vm, 2)) {
				return;
			}
			ByteBuffer a1 = ByteBuffer.wrap(new byte[65536]).order(ByteOrder.nativeOrder());
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1,
					vm.getIntParam(2));
			IntBuffer b1 = a1.asIntBuffer();
			b1.limit(vm.getIntParam(2));
			glSelectBuffer(b1);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a1,
					vm.getIntParam(2));
		}
	}

	public static final class WrapglShadeModel implements Function {
		public void run(TomVM vm) {
			glShadeModel(vm.getIntParam(1));
		}
	}

	public static final class WrapglStencilFunc implements Function {
		public void run(TomVM vm) {
			glStencilFunc(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglStencilMask implements Function {

		public void run(TomVM vm) {
			glStencilMask(vm.getIntParam(1));
		}
	}

	public static final class WrapglStencilOp implements Function {
		public void run(TomVM vm) {
			glStencilOp(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public static final class WrapglTexCoord1d implements Function {
		public void run(TomVM vm) {
			glTexCoord1d(vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord1dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord1dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord1dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord1dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord1f implements Function {
		public void run(TomVM vm) {
			glTexCoord1f(vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord1fv implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord1fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord1fv_2 implements Function {

		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord1fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord1i implements Function {

		public void run(TomVM vm) {
			glTexCoord1i(vm.getIntParam(1));
		}
	}

	public final class WrapglTexCoord1iv implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord1iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord1iv_2 implements Function {

		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord1iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord1s implements Function {

		public void run(TomVM vm) {
			glTexCoord1s(vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglTexCoord1sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord1sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord1sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord1sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord2d implements Function {
		public void run(TomVM vm) {
			glTexCoord2d(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord2dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord2dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord2dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord2dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord2f implements Function {
		public void run(TomVM vm) {
			glTexCoord2f(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord2fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord2fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord2fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord2fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord2i implements Function {
		public void run(TomVM vm) {
			glTexCoord2i(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexCoord2iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord2iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord2iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord2iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord2s implements Function {
		public void run(TomVM vm) {
			glTexCoord2s(vm.getIntParam(2).shortValue(), vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglTexCoord2sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord2sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord2sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord2sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord3d implements Function {
		public void run(TomVM vm) {
			glTexCoord3d(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord3dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord3dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord3dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord3dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord3f implements Function {
		public void run(TomVM vm) {
			glTexCoord3f(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord3fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord3fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord3fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord3fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord3i implements Function {
		public void run(TomVM vm) {
			glTexCoord3i(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexCoord3iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord3iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord3iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord3iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord3s implements Function {
		public void run(TomVM vm) {
			glTexCoord3s(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglTexCoord3sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord3sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord3sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord3sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord4d implements Function {
		public void run(TomVM vm) {
			glTexCoord4d(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord4dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord4dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord4dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexCoord4dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord4f implements Function {
		public void run(TomVM vm) {
			glTexCoord4f(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexCoord4fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord4fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord4fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexCoord4fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord4i implements Function {
		public void run(TomVM vm) {
			glTexCoord4i(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexCoord4iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord4iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord4iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexCoord4iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexCoord4s implements Function {
		public void run(TomVM vm) {
			glTexCoord4s(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglTexCoord4sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord4sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexCoord4sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (byte) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glTexCoord4sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglTexEnvf implements Function {
		public void run(TomVM vm) {
			glTexEnvf(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexEnvfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexEnvfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexEnvfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexEnvfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexEnvi implements Function {
		public void run(TomVM vm) {
			glTexEnvi(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexEnviv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexEnviv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexEnviv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexEnviv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexGend implements Function {
		public void run(TomVM vm) {
			glTexGend(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexGendv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexGendv(vm.getIntParam(3), vm.getIntParam(2), doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexGendv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glTexGendv(vm.getIntParam(3), vm.getIntParam(2), doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglTexGenf implements Function {
		public void run(TomVM vm) {
			glTexGenf(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexGenfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexGenfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexGenfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexGenfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexGeni implements Function {
		public void run(TomVM vm) {
			glTexGeni(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexGeniv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexGeniv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexGeniv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexGeniv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTexParameterf implements Function {
		public void run(TomVM vm) {
			glTexParameterf(vm.getIntParam(3), vm.getIntParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglTexParameterfv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexParameterfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexParameterfv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glTexParameterfv(vm.getIntParam(3), vm.getIntParam(2), floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglTexParameteri implements Function {
		public void run(TomVM vm) {
			glTexParameteri(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglTexParameteriv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexParameteriv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglTexParameteriv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glTexParameteriv(vm.getIntParam(3), vm.getIntParam(2), intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglTranslated implements Function {
		public void run(TomVM vm) {
			glTranslated(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglTranslatef implements Function {
		public void run(TomVM vm) {
			glTranslatef(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public static final class WrapglVertex2d implements Function {
		public void run(TomVM vm) {
			glVertex2d(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex2dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex2dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex2dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex2dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglVertex2f implements Function {
		public void run(TomVM vm) {
			glVertex2f(vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex2fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex2fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex2fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex2fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglVertex2i implements Function {
		public void run(TomVM vm) {
			glVertex2i(vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglVertex2iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex2iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex2iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex2iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglVertex2s implements Function {
		public void run(TomVM vm) {
			glVertex2s(vm.getIntParam(2).shortValue(), vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglVertex2sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex2sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex2sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex2sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglVertex3d implements Function {
		public void run(TomVM vm) {
			glVertex3d(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex3dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex3dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex3dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex3dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglVertex3f implements Function {
		public void run(TomVM vm) {
			glVertex3f(vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex3fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);

			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex3fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex3fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();

			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex3fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglVertex3i implements Function {
		public void run(TomVM vm) {
			glVertex3i(vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglVertex3iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex3iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex3iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex3iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglVertex3s implements Function {
		public void run(TomVM vm) {
			glVertex3s(
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglVertex3sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex3sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex3sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex3sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglVertex4d implements Function {
		public void run(TomVM vm) {
			glVertex4d(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex4dv implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex4dv(doubleBuffer16);
			doubleBuffer16.rewind();
			doubleBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex4dv_2 implements Function {
		public void run(TomVM vm) {
			double[] a = new double[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			doubleBuffer16.rewind();
			doubleBuffer16.put(a);
			doubleBuffer16.rewind();
			glVertex4dv(doubleBuffer16);
			vm.getRefParam(1).setRealVal((float) doubleBuffer16.get(0));
		}
	}

	public static final class WrapglVertex4f implements Function {
		public void run(TomVM vm) {
			glVertex4f(vm.getRealParam(4), vm.getRealParam(3), vm.getRealParam(2), vm.getRealParam(1));
		}
	}

	public final class WrapglVertex4fv implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex4fv(floatBuffer16);
			floatBuffer16.rewind();
			floatBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_REAL, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex4fv_2 implements Function {
		public void run(TomVM vm) {
			float[] a = new float[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getRealVal();
			floatBuffer16.rewind();
			floatBuffer16.put(a);
			floatBuffer16.rewind();
			glVertex4fv(floatBuffer16);
			vm.getRefParam(1).setRealVal(floatBuffer16.get(0));
		}
	}

	public static final class WrapglVertex4i implements Function {
		public void run(TomVM vm) {
			glVertex4i(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}

	public final class WrapglVertex4iv implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex4iv(intBuffer16);
			intBuffer16.rewind();
			intBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex4iv_2 implements Function {
		public void run(TomVM vm) {
			int[] a = new int[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = vm.getRefParam(1).getIntVal();
			intBuffer16.rewind();
			intBuffer16.put(a);
			intBuffer16.rewind();
			glVertex4iv(intBuffer16);
			vm.getRefParam(1).setIntVal(intBuffer16.get(0));
		}
	}

	public static final class WrapglVertex4s implements Function {
		public void run(TomVM vm) {
			glVertex4s(
					vm.getIntParam(4).shortValue(),
					vm.getIntParam(3).shortValue(),
					vm.getIntParam(2).shortValue(),
					vm.getIntParam(1).shortValue());
		}
	}

	public final class WrapglVertex4sv implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.readAndZero(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex4sv(shortBuffer16);
			shortBuffer16.rewind();
			shortBuffer16.get(a);
			Data.writeArray(
					vm.getData(),
					vm.getIntParam(1),
					new ValType(BasicValType.VTP_INT, (byte) 1, (byte) 1, true),
					a,
					16);
		}
	}

	public final class WrapglVertex4sv_2 implements Function {
		public void run(TomVM vm) {
			short[] a = new short[16];
			Data.zeroArray(a, 16);
			if (!vm.checkNullRefParam(1)) {
				return;
			}
			a[0] = (short) vm.getRefParam(1).getIntVal();
			shortBuffer16.rewind();
			shortBuffer16.put(a);
			shortBuffer16.rewind();
			glVertex4sv(shortBuffer16);
			vm.getRefParam(1).setIntVal((int) shortBuffer16.get(0));
		}
	}

	public static final class WrapglViewport implements Function {
		public WrapglViewport() {}

		public void run(TomVM vm) {
			glViewport(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
		}
	}
}
