package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.compiler.util.FuncSpec;
import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.ResourceStore;
import com.basic4gl.library.desktopgl.GLSpriteEngine.*;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.*;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by Nate on 11/1/2015.
 */
public class TextBasicLib implements FunctionLibrary, TextAdapter, IGLRenderer{

    // Global variables
    static GLWindow    appWindow;
    static GLTextGrid  appText;

    public static final int MAX_SPRITES = 100000;

    static int spriteCount;

    @Override
    public String name() {
        return "TextBasicLib";
    }

    @Override
    public String description() {
        return "Text IO and sprite functions";
    }

    @Override
    public void setWindow(GLWindow window){
        TextBasicLib.appWindow = window;
    }
    @Override
    public void setTextGrid(GLTextGrid text){
        TextBasicLib.appText = text;
    }

    @Override
    public void init(TomVM vm) {
        // Text rendering defaults
        textMode = TextMode.TEXT_SIMPLE;
        appText.Resize (25, 40);
        appText.HideCursor ();
        appText.SetColour (GLTextGrid.MakeColour((short)220, (short)220, (short)255));
        appText.SetTexture (appText.DefaultTexture ());
        appText.setScroll (true);
        appWindow.SetDontPaint (false);

        // Sprite engine defaults
        sprites = new GLSpriteStore();
        sprites.clear();
        GLSpriteEngine spriteEngine = (GLSpriteEngine) appText;
        spriteEngine.SetDefaults ();
        boundSprite = 0;

        TextBasicLib.spriteCount = 0;
    }

    @Override
    public void init(TomBasicCompiler comp){

    }

    @Override
    public void cleanup() {
        //Do nothing
    }

    @Override
    public Map<String, Constant> constants() {
        Map<String, Constant> c = new HashMap<String, Constant>();
        // Register constants
        c.put("TEXT_SIMPLE", new Constant(TextMode.TEXT_SIMPLE.getMode()));
        c.put("TEXT_BUFFERED", new Constant(TextMode.TEXT_BUFFERED.getMode()));
        c.put("TEXT_OVERLAID", new Constant(TextMode.TEXT_OVERLAID.getMode()));
        c.put("DRAW_TEXT", new Constant(GLTextGrid.DRAW_TEXT));
        c.put("DRAW_SPRITES_BEHIND", new Constant(GLSpriteEngine.DRAW_SPRITES_BEHIND));
        c.put("DRAW_SPRITES_INFRONT", new Constant(GLSpriteEngine.DRAW_SPRITES_INFRONT));
        c.put("DRAW_SPRITES", new Constant(GLSpriteEngine.DRAW_SPRITES));

        // COMPATIBILITY NOTE:
        // Virtual keycodes used in the Windows version are not supported by GLFW; virtual key constants available in
        // the Windows version of Basic4GL that would normally be registered in this library have been mapped to their
        // corresponding GLFW key codes where possible - vk constants with unknown corresponding GLFW constants
        // have simply been excluded at this time.
        // All documented GLFW keys are registered below for use in Basic4GL programs

        /* From GLFW Keyboard Keys documentation:
        *   These key codes are inspired by the USB HID Usage Tables v1.12 (p. 53-60),
        *   but re-arranged to map to 7-bit ASCII for printable keys (function keys are put in the 256+ range).
        *
        *   The naming of the key codes follow these rules:
        *       The US keyboard layout is used
        *       Names of printable alpha-numeric characters are used (e.g. "A", "R", "3", etc.)
        *       For non-alphanumeric characters,
        *           Unicode:ish names are used (e.g. "COMMA", "LEFT_SQUARE_BRACKET", etc.).
        *           Note that some names do not correspond to the Unicode standard (usually for brevity)
        *       Keys that lack a clear US mapping are named "WORLD_x"
        *       For non-printable keys, custom names are used (e.g. "F4", "BACKSPACE", etc.)
        */

        c.put("VK_BACK", new Constant(GLFW_KEY_BACKSPACE));
        c.put("VK_TAB", new Constant(GLFW_KEY_TAB));
        c.put("VK_SPACE", new Constant(GLFW_KEY_SPACE));
        c.put("VK_PRIOR", new Constant(GLFW_KEY_PAGE_UP));
        c.put("VK_NEXT", new Constant(GLFW_KEY_PAGE_DOWN));
        c.put("VK_CAPITAL", new Constant(GLFW_KEY_CAPS_LOCK));
        c.put("VK_ESCAPE", new Constant(GLFW_KEY_ESCAPE));
        c.put("VK_END", new Constant(GLFW_KEY_END));
        c.put("VK_HOME", new Constant(GLFW_KEY_HOME));
        c.put("VK_LEFT", new Constant(GLFW_KEY_LEFT));
        c.put("VK_UP", new Constant(GLFW_KEY_UP));
        c.put("VK_RIGHT", new Constant(GLFW_KEY_RIGHT));
        c.put("VK_DOWN", new Constant(GLFW_KEY_DOWN));
        c.put("VK_INSERT", new Constant(GLFW_KEY_INSERT));
        c.put("VK_DELETE", new Constant(GLFW_KEY_DELETE));
        c.put("VK_NUMPAD0", new Constant(GLFW_KEY_KP_0));
        c.put("VK_NUMPAD1", new Constant(GLFW_KEY_KP_1));
        c.put("VK_NUMPAD2", new Constant(GLFW_KEY_KP_2));
        c.put("VK_NUMPAD3", new Constant(GLFW_KEY_KP_3));
        c.put("VK_NUMPAD4", new Constant(GLFW_KEY_KP_4));
        c.put("VK_NUMPAD5", new Constant(GLFW_KEY_KP_5));
        c.put("VK_NUMPAD6", new Constant(GLFW_KEY_KP_6));
        c.put("VK_NUMPAD7", new Constant(GLFW_KEY_KP_7));
        c.put("VK_NUMPAD8", new Constant(GLFW_KEY_KP_8));
        c.put("VK_NUMPAD9", new Constant(GLFW_KEY_KP_9));
        c.put("VK_MULTIPLY", new Constant(GLFW_KEY_KP_MULTIPLY));
        c.put("VK_ADD", new Constant(GLFW_KEY_KP_ADD));
        c.put("VK_SUBTRACT", new Constant(GLFW_KEY_KP_SUBTRACT));
        c.put("VK_DECIMAL", new Constant(GLFW_KEY_KP_DECIMAL));
        c.put("VK_DIVIDE", new Constant(GLFW_KEY_KP_DIVIDE));
        c.put("VK_F1", new Constant(GLFW_KEY_F1));
        c.put("VK_F2", new Constant(GLFW_KEY_F2));
        c.put("VK_F3", new Constant(GLFW_KEY_F3));
        c.put("VK_F4", new Constant(GLFW_KEY_F4));
        c.put("VK_F5", new Constant(GLFW_KEY_F5));
        c.put("VK_F6", new Constant(GLFW_KEY_F6));
        c.put("VK_F7", new Constant(GLFW_KEY_F7));
        c.put("VK_F8", new Constant(GLFW_KEY_F8));
        c.put("VK_F9", new Constant(GLFW_KEY_F9));
        c.put("VK_F10", new Constant(GLFW_KEY_F10));
        c.put("VK_F11", new Constant(GLFW_KEY_F11));
        c.put("VK_F12", new Constant(GLFW_KEY_F12));
        c.put("VK_F13", new Constant(GLFW_KEY_F13));
        c.put("VK_F14", new Constant(GLFW_KEY_F14));
        c.put("VK_F15", new Constant(GLFW_KEY_F15));
        c.put("VK_F16", new Constant(GLFW_KEY_F16));
        c.put("VK_F17", new Constant(GLFW_KEY_F17));
        c.put("VK_F18", new Constant(GLFW_KEY_F18));
        c.put("VK_F19", new Constant(GLFW_KEY_F19));
        c.put("VK_F20", new Constant(GLFW_KEY_F20));
        c.put("VK_F21", new Constant(GLFW_KEY_F21));
        c.put("VK_F22", new Constant(GLFW_KEY_F22));
        c.put("VK_F23", new Constant(GLFW_KEY_F23));
        c.put("VK_F24", new Constant(GLFW_KEY_F24));
        c.put("VK_LSHIFT", new Constant(GLFW_KEY_LEFT_SHIFT));
        c.put("VK_RSHIFT", new Constant(GLFW_KEY_RIGHT_SHIFT));
        c.put("VK_LCONTROL", new Constant(GLFW_KEY_LEFT_CONTROL));
        c.put("VK_RCONTROL", new Constant(GLFW_KEY_RIGHT_CONTROL ));
        c.put("VK_NUMLOCK", new Constant(GLFW_KEY_NUM_LOCK));
        c.put("VK_SCROLL", new Constant(GLFW_KEY_SCROLL_LOCK));

        c.put("GLFW_KEY_UNKNOWN",	new Constant(GLFW_KEY_UNKNOWN));
        c.put("GLFW_KEY_SPACE",	new Constant(GLFW_KEY_SPACE));
        c.put("GLFW_KEY_APOSTROPHE",	new Constant(GLFW_KEY_APOSTROPHE));
        c.put("GLFW_KEY_COMMA",	new Constant(GLFW_KEY_COMMA));
        c.put("GLFW_KEY_MINUS",	new Constant(GLFW_KEY_MINUS));
        c.put("GLFW_KEY_PERIOD",	new Constant(GLFW_KEY_PERIOD));
        c.put("GLFW_KEY_SLASH",	new Constant(GLFW_KEY_SLASH));
        c.put("GLFW_KEY_0",	new Constant(GLFW_KEY_0));
        c.put("GLFW_KEY_1",	new Constant(GLFW_KEY_1));
        c.put("GLFW_KEY_2",	new Constant(GLFW_KEY_2));
        c.put("GLFW_KEY_3",	new Constant(GLFW_KEY_3));
        c.put("GLFW_KEY_4",	new Constant(GLFW_KEY_4));
        c.put("GLFW_KEY_5",	new Constant(GLFW_KEY_5));
        c.put("GLFW_KEY_6",	new Constant(GLFW_KEY_6));
        c.put("GLFW_KEY_7",	new Constant(GLFW_KEY_7));
        c.put("GLFW_KEY_8",	new Constant(GLFW_KEY_8));
        c.put("GLFW_KEY_9",	new Constant(GLFW_KEY_9));
        c.put("GLFW_KEY_SEMICOLON",	new Constant(GLFW_KEY_SEMICOLON));
        c.put("GLFW_KEY_EQUAL",	new Constant(GLFW_KEY_EQUAL));
        c.put("GLFW_KEY_A",	new Constant(GLFW_KEY_A));
        c.put("GLFW_KEY_B",	new Constant(GLFW_KEY_B));
        c.put("GLFW_KEY_C",	new Constant(GLFW_KEY_C));
        c.put("GLFW_KEY_D",	new Constant(GLFW_KEY_D));
        c.put("GLFW_KEY_E",	new Constant(GLFW_KEY_E));
        c.put("GLFW_KEY_F",	new Constant(GLFW_KEY_F));
        c.put("GLFW_KEY_G",	new Constant(GLFW_KEY_G));
        c.put("GLFW_KEY_H",	new Constant(GLFW_KEY_H));
        c.put("GLFW_KEY_I",	new Constant(GLFW_KEY_I));
        c.put("GLFW_KEY_J",	new Constant(GLFW_KEY_J));
        c.put("GLFW_KEY_K",	new Constant(GLFW_KEY_K));
        c.put("GLFW_KEY_L",	new Constant(GLFW_KEY_L));
        c.put("GLFW_KEY_M",	new Constant(GLFW_KEY_M));
        c.put("GLFW_KEY_N",	new Constant(GLFW_KEY_N));
        c.put("GLFW_KEY_O",	new Constant(GLFW_KEY_O));
        c.put("GLFW_KEY_P",	new Constant(GLFW_KEY_P));
        c.put("GLFW_KEY_Q",	new Constant(GLFW_KEY_Q));
        c.put("GLFW_KEY_R",	new Constant(GLFW_KEY_R));
        c.put("GLFW_KEY_S",	new Constant(GLFW_KEY_S));
        c.put("GLFW_KEY_T",	new Constant(GLFW_KEY_T));
        c.put("GLFW_KEY_U",	new Constant(GLFW_KEY_U));
        c.put("GLFW_KEY_V",	new Constant(GLFW_KEY_V));
        c.put("GLFW_KEY_W",	new Constant(GLFW_KEY_W));
        c.put("GLFW_KEY_X",	new Constant(GLFW_KEY_X));
        c.put("GLFW_KEY_Y",	new Constant(GLFW_KEY_Y));
        c.put("GLFW_KEY_Z",	new Constant(GLFW_KEY_Z));
        c.put("GLFW_KEY_LEFT_BRACKET",	new Constant(GLFW_KEY_LEFT_BRACKET));
        c.put("GLFW_KEY_BACKSLASH",	new Constant(GLFW_KEY_BACKSLASH));
        c.put("GLFW_KEY_RIGHT_BRACKET",	new Constant(GLFW_KEY_RIGHT_BRACKET));
        c.put("GLFW_KEY_GRAVE_ACCENT",	new Constant(GLFW_KEY_GRAVE_ACCENT));
        c.put("GLFW_KEY_WORLD_1",	new Constant(GLFW_KEY_WORLD_1));
        c.put("GLFW_KEY_WORLD_2",	new Constant(GLFW_KEY_WORLD_2));
        c.put("GLFW_KEY_ESCAPE",	new Constant(GLFW_KEY_ESCAPE));
        c.put("GLFW_KEY_ENTER",	new Constant(GLFW_KEY_ENTER));
        c.put("GLFW_KEY_TAB",	new Constant(GLFW_KEY_TAB));
        c.put("GLFW_KEY_BACKSPACE",	new Constant(GLFW_KEY_BACKSPACE));
        c.put("GLFW_KEY_INSERT",	new Constant(GLFW_KEY_INSERT));
        c.put("GLFW_KEY_DELETE",	new Constant(GLFW_KEY_DELETE));
        c.put("GLFW_KEY_RIGHT",	new Constant(GLFW_KEY_RIGHT));
        c.put("GLFW_KEY_LEFT",	new Constant(GLFW_KEY_LEFT));
        c.put("GLFW_KEY_DOWN",	new Constant(GLFW_KEY_DOWN));
        c.put("GLFW_KEY_UP",	new Constant(GLFW_KEY_UP));
        c.put("GLFW_KEY_PAGE_UP",	new Constant(GLFW_KEY_PAGE_UP));
        c.put("GLFW_KEY_PAGE_DOWN",	new Constant(GLFW_KEY_PAGE_DOWN));
        c.put("GLFW_KEY_HOME",	new Constant(GLFW_KEY_HOME));
        c.put("GLFW_KEY_END",	new Constant(GLFW_KEY_END));
        c.put("GLFW_KEY_CAPS_LOCK",	new Constant(GLFW_KEY_CAPS_LOCK));
        c.put("GLFW_KEY_SCROLL_LOCK",	new Constant(GLFW_KEY_SCROLL_LOCK));
        c.put("GLFW_KEY_NUM_LOCK",	new Constant(GLFW_KEY_NUM_LOCK));
        c.put("GLFW_KEY_PRINT_SCREEN",	new Constant(GLFW_KEY_PRINT_SCREEN));
        c.put("GLFW_KEY_PAUSE",	new Constant(GLFW_KEY_PAUSE));
        c.put("GLFW_KEY_F1",	new Constant(GLFW_KEY_F1));
        c.put("GLFW_KEY_F2",	new Constant(GLFW_KEY_F2));
        c.put("GLFW_KEY_F3",	new Constant(GLFW_KEY_F3));
        c.put("GLFW_KEY_F4",	new Constant(GLFW_KEY_F4));
        c.put("GLFW_KEY_F5",	new Constant(GLFW_KEY_F5));
        c.put("GLFW_KEY_F6",	new Constant(GLFW_KEY_F6));
        c.put("GLFW_KEY_F7",	new Constant(GLFW_KEY_F7));
        c.put("GLFW_KEY_F8",	new Constant(GLFW_KEY_F8));
        c.put("GLFW_KEY_F9",	new Constant(GLFW_KEY_F9));
        c.put("GLFW_KEY_F10",	new Constant(GLFW_KEY_F10));
        c.put("GLFW_KEY_F11",	new Constant(GLFW_KEY_F11));
        c.put("GLFW_KEY_F12",	new Constant(GLFW_KEY_F12));
        c.put("GLFW_KEY_F13",	new Constant(GLFW_KEY_F13));
        c.put("GLFW_KEY_F14",	new Constant(GLFW_KEY_F14));
        c.put("GLFW_KEY_F15",	new Constant(GLFW_KEY_F15));
        c.put("GLFW_KEY_F16",	new Constant(GLFW_KEY_F16));
        c.put("GLFW_KEY_F17",	new Constant(GLFW_KEY_F17));
        c.put("GLFW_KEY_F18",	new Constant(GLFW_KEY_F18));
        c.put("GLFW_KEY_F19",	new Constant(GLFW_KEY_F19));
        c.put("GLFW_KEY_F20",	new Constant(GLFW_KEY_F20));
        c.put("GLFW_KEY_F21",	new Constant(GLFW_KEY_F21));
        c.put("GLFW_KEY_F22",	new Constant(GLFW_KEY_F22));
        c.put("GLFW_KEY_F23",	new Constant(GLFW_KEY_F23));
        c.put("GLFW_KEY_F24",	new Constant(GLFW_KEY_F24));
        c.put("GLFW_KEY_F25",	new Constant(GLFW_KEY_F25));
        c.put("GLFW_KEY_KP_0",	new Constant(GLFW_KEY_KP_0));
        c.put("GLFW_KEY_KP_1",	new Constant(GLFW_KEY_KP_1));
        c.put("GLFW_KEY_KP_2",	new Constant(GLFW_KEY_KP_2));
        c.put("GLFW_KEY_KP_3",	new Constant(GLFW_KEY_KP_3));
        c.put("GLFW_KEY_KP_4",	new Constant(GLFW_KEY_KP_4));
        c.put("GLFW_KEY_KP_5",	new Constant(GLFW_KEY_KP_5));
        c.put("GLFW_KEY_KP_6",	new Constant(GLFW_KEY_KP_6));
        c.put("GLFW_KEY_KP_7",	new Constant(GLFW_KEY_KP_7));
        c.put("GLFW_KEY_KP_8",	new Constant(GLFW_KEY_KP_8));
        c.put("GLFW_KEY_KP_9",	new Constant(GLFW_KEY_KP_9));
        c.put("GLFW_KEY_KP_DECIMAL",	new Constant(GLFW_KEY_KP_DECIMAL));
        c.put("GLFW_KEY_KP_DIVIDE",	new Constant(GLFW_KEY_KP_DIVIDE));
        c.put("GLFW_KEY_KP_MULTIPLY",	new Constant(GLFW_KEY_KP_MULTIPLY));
        c.put("GLFW_KEY_KP_SUBTRACT",	new Constant(GLFW_KEY_KP_SUBTRACT));
        c.put("GLFW_KEY_KP_ADD",	new Constant(GLFW_KEY_KP_ADD));
        c.put("GLFW_KEY_KP_ENTER",	new Constant(GLFW_KEY_KP_ENTER));
        c.put("GLFW_KEY_KP_EQUAL",	new Constant(GLFW_KEY_KP_EQUAL));
        c.put("GLFW_KEY_LEFT_SHIFT",	new Constant(GLFW_KEY_LEFT_SHIFT));
        c.put("GLFW_KEY_LEFT_CONTROL",	new Constant(GLFW_KEY_LEFT_CONTROL));
        c.put("GLFW_KEY_LEFT_ALT",	new Constant(GLFW_KEY_LEFT_ALT));
        c.put("GLFW_KEY_LEFT_SUPER",	new Constant(GLFW_KEY_LEFT_SUPER));
        c.put("GLFW_KEY_RIGHT_SHIFT",	new Constant(GLFW_KEY_RIGHT_SHIFT));
        c.put("GLFW_KEY_RIGHT_CONTROL",	new Constant(GLFW_KEY_RIGHT_CONTROL));
        c.put("GLFW_KEY_RIGHT_ALT",	new Constant(GLFW_KEY_RIGHT_ALT));
        c.put("GLFW_KEY_RIGHT_SUPER",	new Constant(GLFW_KEY_RIGHT_SUPER));
        c.put("GLFW_KEY_MENU",	new Constant(GLFW_KEY_MENU));
        c.put("GLFW_KEY_LAST",	new Constant(GLFW_KEY_LAST));

        c.put("MOUSE_LBUTTON", new Constant(0));
        c.put("MOUSE_RBUTTON", new Constant(1));
        c.put("MOUSE_MBUTTON", new Constant(2));


        c.put("SPR_INVALID", new Constant(GLSpriteType.SPR_INVALID.getType()));
        c.put("SPR_SPRITE", new Constant(GLSpriteType.SPR_SPRITE.getType()));
        c.put("SPR_TILEMAP", new Constant(GLSpriteType.SPR_TILEMAP.getType()));
        return c;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {

        Map<String, FuncSpec[]> s = new HashMap<String, FuncSpec[]>();

        s.put("cls", new FuncSpec[]{ new FuncSpec( WrapCls.class, new ParamTypeList (), false, false, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("print", new FuncSpec[]{ new FuncSpec( WrapPrint.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("printr", new FuncSpec[]{
                new FuncSpec( WrapPrintr.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),false, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapPrintr_2.class, new ParamTypeList (), false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("locate", new FuncSpec[]{ new FuncSpec( WrapLocate.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("swapbuffers", new FuncSpec[]{ new FuncSpec( WrapSwapBuffers.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("drawtext", new FuncSpec[]{
                new FuncSpec( WrapDrawText.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapDrawText2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textmode", new FuncSpec[]{ new FuncSpec( WrapTextMode.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("resizetext", new FuncSpec[]{ new FuncSpec( WrapResizeText.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textrows", new FuncSpec[]{ new FuncSpec( WrapTextRows.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textcols", new FuncSpec[]{ new FuncSpec( WrapTextCols.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("input$", new FuncSpec[]{ new FuncSpec( WrapInput.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_STRING),true, false, null)});
    s.put("inkey$", new FuncSpec[]{ new FuncSpec( WrapInkey.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_STRING), false, false, null)});
    s.put("inscankey", new FuncSpec[]{ new FuncSpec( WrapInScanKey.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearkeys", new FuncSpec[]{ new FuncSpec( WrapClearKeys.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("showcursor", new FuncSpec[]{ new FuncSpec( WrapShowCursor.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("hidecursor", new FuncSpec[]{ new FuncSpec( WrapHideCursor.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("keydown", new FuncSpec[]{ new FuncSpec( WrapKeyDown.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("scankeydown", new FuncSpec[]{ new FuncSpec( WrapScanKeyDown.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("charat$", new FuncSpec[]{ new FuncSpec( WrapCharAt.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_STRING), false, false, null)});
    s.put("color", new FuncSpec[]{ new FuncSpec( WrapColour.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("font", new FuncSpec[]{ new FuncSpec( WrapFont.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("defaultfont", new FuncSpec[]{ new FuncSpec( WrapDefaultFont.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("mouse_x", new FuncSpec[]{ new FuncSpec( WrapMouseX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_y", new FuncSpec[]{ new FuncSpec( WrapMouseY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_xd", new FuncSpec[]{ new FuncSpec( WrapMouseXD.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_yd", new FuncSpec[]{ new FuncSpec( WrapMouseYD.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_button", new FuncSpec[]{ new FuncSpec( WrapMouseButton.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("mouse_wheel", new FuncSpec[]{ new FuncSpec( WrapMouseWheel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearline", new FuncSpec[]{ new FuncSpec( WrapClearLine.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearregion", new FuncSpec[]{ new FuncSpec( WrapClearRegion.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("textscroll", new FuncSpec[]{ new FuncSpec( WrapTextScroll.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("settextscroll", new FuncSpec[]{ new FuncSpec( WrapSetTextScroll.class, new ParamTypeList(new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("CursorCol", new FuncSpec[]{ new FuncSpec( WrapCursorCol.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("CursorRow", new FuncSpec[]{ new FuncSpec( WrapCursorRow.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

    s.put("newsprite", new FuncSpec[]{
            new FuncSpec( WrapNewSprite.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapNewSprite_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapNewSprite_3.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("newtilemap", new FuncSpec[]{
            new FuncSpec( WrapNewTileMap.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapNewTileMap_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapNewTileMap_3.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("deletesprite", new FuncSpec[]{ new FuncSpec( WrapDeleteSprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("bindsprite", new FuncSpec[]{ new FuncSpec( WrapBindSprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsettexture", new FuncSpec[]{ new FuncSpec( WrapSprSetTexture.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsettextures", new FuncSpec[]{ new FuncSpec( WrapSprSetTextures.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spraddtexture", new FuncSpec[]{ new FuncSpec( WrapSprAddTexture.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spraddtextures", new FuncSpec[]{ new FuncSpec( WrapSprAddTextures.class, new ParamTypeList (new ValType (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetframe", new FuncSpec[]{ new FuncSpec( WrapSprSetFrame.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetx", new FuncSpec[]{ new FuncSpec( WrapSprSetX.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsety", new FuncSpec[]{ new FuncSpec( WrapSprSetY.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetzorder", new FuncSpec[]{ new FuncSpec( WrapSprSetZOrder.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetpos", new FuncSpec[]{
            new FuncSpec( WrapSprSetPos.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL ,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprSetPos_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxsize", new FuncSpec[]{ new FuncSpec( WrapSprSetXSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetysize", new FuncSpec[]{ new FuncSpec( WrapSprSetYSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetsize", new FuncSpec[]{
            new FuncSpec( WrapSprSetSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprSetSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetscale", new FuncSpec[]{ new FuncSpec( WrapSprSetScale.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxcentre", new FuncSpec[]{ new FuncSpec( WrapSprSetXCentre.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetycentre", new FuncSpec[]{ new FuncSpec( WrapSprSetYCentre.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxflip", new FuncSpec[]{ new FuncSpec( WrapSprSetXFlip.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetyflip", new FuncSpec[]{ new FuncSpec( WrapSprSetYFlip.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetvisible", new FuncSpec[]{ new FuncSpec( WrapSprSetVisible.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetangle", new FuncSpec[]{ new FuncSpec( WrapSprSetAngle.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetcolor", new FuncSpec[]{
            new FuncSpec( WrapSprSetColour.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprSetColour_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprSetColour_3.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetalpha", new FuncSpec[]{ new FuncSpec( WrapSprSetAlpha.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetparallax", new FuncSpec[]{ new FuncSpec( WrapSprSetParallax.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetsolid", new FuncSpec[]{ new FuncSpec( WrapSprSetSolid.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("resizespritearea", new FuncSpec[]{ new FuncSpec( WrapResizeSpriteArea.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spriteareawidth", new FuncSpec[]{ new FuncSpec( WrapSpriteAreaWidth.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("spriteareaheight", new FuncSpec[]{ new FuncSpec( WrapSpriteAreaHeight.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("sprframe", new FuncSpec[]{
            new FuncSpec( WrapSprFrame.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprFrame_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprx", new FuncSpec[]{
                new FuncSpec( WrapSprX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprX_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("spry", new FuncSpec[]{
            new FuncSpec( WrapSprY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprY_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("sprpos", new FuncSpec[]{
            new FuncSpec( WrapSprPos.class, new ParamTypeList(), true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
            new FuncSpec( WrapSprPos_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType( BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});


        s.put("sprzorder", new FuncSpec[]{
                new FuncSpec( WrapSprZOrder.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprZOrder_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxsize", new FuncSpec[]{
                new FuncSpec( WrapSprXSize.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprXSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprysize", new FuncSpec[]{
                new FuncSpec( WrapSprYSize.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprYSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprscale", new FuncSpec[]{
                new FuncSpec( WrapSprScale.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprScale_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxcentre", new FuncSpec[]{
                new FuncSpec( WrapSprXCentre.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprXCentre_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprycentre", new FuncSpec[]{
            new FuncSpec( WrapSprYCentre.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprYCentre_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});

        s.put("sprxflip", new FuncSpec[]{
            new FuncSpec( WrapSprXFlip.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprXFlip_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spryflip", new FuncSpec[]{
            new FuncSpec( WrapSprYFlip.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprYFlip_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprvisible", new FuncSpec[]{
            new FuncSpec( WrapSprVisible.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprVisible_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprangle", new FuncSpec[]{
            new FuncSpec( WrapSprAngle.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprAngle_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcolor", new FuncSpec[]{
            new FuncSpec( WrapSprColour.class, new ParamTypeList (), true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
            new FuncSpec( WrapSprColour_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});
        s.put("spralpha", new FuncSpec[]{
            new FuncSpec( WrapSprAlpha.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprAlpha_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprparallax", new FuncSpec[]{
            new FuncSpec( WrapSprParallax.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprParallax_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsolid", new FuncSpec[]{
            new FuncSpec( WrapSprSolid.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprSolid_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprleft", new FuncSpec[]{
            new FuncSpec( WrapSprLeft.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprLeft_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprright", new FuncSpec[]{
            new FuncSpec( WrapSprRight.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprRight_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprtop", new FuncSpec[]{
            new FuncSpec( WrapSprTop.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprTop_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprbottom", new FuncSpec[]{
            new FuncSpec( WrapSprBottom.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprBottom_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxvel", new FuncSpec[]{
            new FuncSpec( WrapSprXVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FuncSpec( WrapSprXVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spryvel", new FuncSpec[]{
                new FuncSpec( WrapSprYVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprYVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprvel", new FuncSpec[]{
                new FuncSpec( WrapSprVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
                new FuncSpec( WrapSprVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});
        s.put("sprspin", new FuncSpec[]{
                new FuncSpec( WrapSprSpin.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprSpin_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spranimspeed", new FuncSpec[]{
                new FuncSpec( WrapSprAnimSpeed.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FuncSpec( WrapSprAnimSpeed_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spranimloop", new FuncSpec[]{
                new FuncSpec( WrapSprAnimLoop.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprAnimLoop_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spranimdone", new FuncSpec[]{
            new FuncSpec( WrapSprAnimDone.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FuncSpec( WrapSprAnimDone_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprsetxvel", new FuncSpec[]{ new FuncSpec( WrapSprSetXVel.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetyvel", new FuncSpec[]{ new FuncSpec( WrapSprSetYVel.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetvel", new FuncSpec[]{
                new FuncSpec( WrapSprSetVel.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprSetVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetspin", new FuncSpec[]{ new FuncSpec( WrapSprSetSpin.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetanimspeed", new FuncSpec[]{ new FuncSpec( WrapSprSetAnimSpeed.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetanimloop", new FuncSpec[]{ new FuncSpec( WrapSprSetAnimLoop.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("animatesprites", new FuncSpec[]{ new FuncSpec( WrapAnimateSprites.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("animatespriteframes", new FuncSpec[]{ new FuncSpec( WrapAnimateSpriteFrames.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("copysprite", new FuncSpec[]{ new FuncSpec( WrapCopySprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprtype", new FuncSpec[]{
                new FuncSpec( WrapSprType.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprType_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprxtiles", new FuncSpec[]{
                new FuncSpec( WrapSprXTiles.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprXTiles_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprytiles", new FuncSpec[]{
                new FuncSpec( WrapSprYTiles.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprYTiles_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprsettiles", new FuncSpec[]{ new FuncSpec( WrapSprSetTiles.class, new ParamTypeList (new ValType (BasicValType.VTP_INT,(byte)2, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetxrepeat", new FuncSpec[]{ new FuncSpec( WrapSprSetXRepeat.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetyrepeat", new FuncSpec[]{ new FuncSpec( WrapSprSetYRepeat.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprxrepeat", new FuncSpec[]{
                new FuncSpec( WrapSprXRepeat.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprXRepeat_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spryrepeat", new FuncSpec[]{
                new FuncSpec( WrapSprYRepeat.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprYRepeat_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetx", new FuncSpec[]{ new FuncSpec( WrapSprCameraSetX.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasety", new FuncSpec[]{ new FuncSpec( WrapSprCameraSetY.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetz", new FuncSpec[]{ new FuncSpec( WrapSprCameraSetZ.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetpos", new FuncSpec[]{
                new FuncSpec( WrapSprCameraSetPos.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FuncSpec( WrapSprCameraSetPos_2.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetangle", new FuncSpec[]{ new FuncSpec( WrapSprCameraSetAngle.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerax", new FuncSpec[]{ new FuncSpec( WrapSprCameraX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcameray", new FuncSpec[]{ new FuncSpec( WrapSprCameraY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcameraz", new FuncSpec[]{ new FuncSpec( WrapSprCameraZ.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerapos", new FuncSpec[]{ new FuncSpec( WrapSprCameraPos.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put("sprcameraangle", new FuncSpec[]{ new FuncSpec( WrapSprCameraAngle.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerafov", new FuncSpec[]{ new FuncSpec( WrapSprCameraFOV.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerasetfov", new FuncSpec[]{ new FuncSpec( WrapSprCameraSetFOV.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("clearsprites", new FuncSpec[]{ new FuncSpec( WrapClearSprites.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetblendfunc", new FuncSpec[]{ new FuncSpec( WrapSprSetBlendFunc.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spritecount", new FuncSpec[]{ new FuncSpec( WrapSpriteCount.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
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


    public void print(String text, boolean newline) {
        TextBasicLib.appText.Write(text);
        if (newline) {
            TextBasicLib.appText.NewLine();
        }
        TextBasicLib.Redraw();
    }
    public void locate(int x, int y) {
        TextBasicLib.appText.MoveCursor(x, y);
    }
    public void cls() {
        TextBasicLib.appText.Clear();
        TextBasicLib.Redraw();
    }
    public void clearRegion(int x1, int y1, int x2, int y2) {
        TextBasicLib.appText.ClearRegion(x1, y1, x2, y2);
        TextBasicLib.Redraw();
    }
    public int getTextRows() {
        return TextBasicLib.appText.Rows();
    }
    public int getTextCols() {
        return TextBasicLib.appText.Cols();
    }
    public void resizeText(int cols, int rows) {
        if (rows < 1) {
            rows = 1;
        }
        if (rows > 500) {
            rows = 500;
        }
        if (cols < 1) {
            cols = 1;
        }
        if (cols > 500) {
            cols = 500;
        }
        TextBasicLib.appText.Resize (rows, cols);
        TextBasicLib.Redraw ();
    }
    public void setTextScrollEnabled(boolean scroll) {
        TextBasicLib.appText.setScroll(scroll);
    }
    public boolean getTextScrollEnabled() {
        return TextBasicLib.appText.Scroll();
    }
    public void drawText() {
        TextBasicLib.ForceDraw();
    }
    public char getCharAt(int x, int y) {
        return TextBasicLib.appText.TextAt(x, y);
    }
    public void setFont(int fontTexture) {
        TextBasicLib.appText.SetTexture(fontTexture);
    }
    public int getDefaultFont() {
        return TextBasicLib.appText.DefaultTexture();
    }
    public void setTextMode(TextMode mode) {
        textMode = mode;
    }
    public void setColor(byte red, byte green, byte blue) {
        TextBasicLib.appText.SetColour(GLTextGrid.MakeColour(red, green, blue));
    }

    /**
     * A store of glSprites
     */
    class GLSpriteStore extends ResourceStore<GLBasicSprite> {
        protected void deleteElement(int index){
            setValue(index, null);
        }

        public GLSpriteStore () {
        super(null);
        }
    }

    static GLSpriteStore sprites;
    static int boundSprite;

    static TextMode textMode = TextMode.TEXT_SIMPLE;

    static void ForceDraw(){ ForceDraw((byte)(GLTextGrid.DRAW_TEXT | GLSpriteEngine.DRAW_SPRITES));}
    static void ForceDraw(byte flags) {
        if (textMode == TextMode.TEXT_SIMPLE || textMode == TextMode.TEXT_BUFFERED) {
            glClear (GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        }

        appText.Draw(flags);

        if (textMode == TextMode.TEXT_SIMPLE || textMode == TextMode.TEXT_BUFFERED) {
            appWindow.SwapBuffers ();
        }
    }

    static void Redraw () {
        if (textMode == TextMode.TEXT_SIMPLE) {
            ForceDraw();
        }
    }



    boolean GetTextures (TomVM vm, int paramIndex, Vector<Integer> dest) {

        // Read in texture array and convert to vector (for storage in sprite)
        int[] frames = new int[65536];
        int size = Data.getArrayDimensionSize(vm.getData(), vm.getIntParam(paramIndex), 0);
        if (size < 1 || size > 65536) {
            vm.functionError("Texture array size must be 1-65536");
            return false;
        }
        Data.readArray(vm.getData(), vm.getIntParam(paramIndex), new ValType (BasicValType.VTP_INT, (byte) 1, (byte) 1, true), frames, size);

        // Convert to vector
        dest.clear ();
        for (int i = 0; i < size; i++) {
            dest.add (frames [i]);
        }

        return true;
    }

    static boolean IsBasicSprite (int index) {
        return TextBasicLib.sprites.isIndexStored(index);
    }
    static GLBasicSprite BasicSprite (int index) {
        assertTrue(IsBasicSprite (index));
        return TextBasicLib.sprites.getValueAt(index);
    }
    static boolean IsSprite (int index) {
        return IsBasicSprite (index)
                && BasicSprite (index).getGLSpriteType() == GLSpriteType.SPR_SPRITE;
    }
    static GLSprite  Sprite (int index) {
        assertTrue(IsSprite (index));
        return (GLSprite ) BasicSprite (index);
    }
    static boolean IsTileMap (int index) {
        return IsBasicSprite (index)
                && BasicSprite (index).getGLSpriteType() == GLSpriteType.SPR_TILEMAP;
    }
    static GLTileMap  TileMap (int index) {
        assertTrue(IsTileMap (index));
        return (GLTileMap ) BasicSprite (index);
    }
    public static void GetTiles (TomVM vm, int paramIndex, IntBuffer xSize, IntBuffer ySize, Vector<Integer> dest) {

        // Read in texture array and convert to vector (for storage in sprite)
        int index = vm.getIntParam(paramIndex);
        xSize.put(0, Data.getArrayDimensionSize(vm.getData(), index, 0));
        ySize.put(0, Data.getArrayDimensionSize(vm.getData(), index, 1));
        int x = xSize.get(0);
        int y = ySize.get(0);
        // Size must be valid and add up to 1 million or less tiles
        dest.clear();
        if (x > 0 && y > 0) {

            // Read data into temp buffer
            int[] buffer = new int [x * y];
            Data.readArray(vm.getData(), index, new ValType  (BasicValType.VTP_INT, (byte) 2, (byte) 1, true), buffer, x * y);

            // Convert to vector
            for (int i = 0; i < x * y; i++) {
                dest.add(buffer[i]);
            }

            // Free temp buffer
            buffer = null;
        }
    }

    float[] vec = new float[4];
    void ReadVec (TomVM vm, int paramIndex) {

        // Read data
        vec [0] = 0;
        vec [1] = 0;
        vec [2] = 0;
        vec [3] = 1;
        Data.readArray(vm.getData(), vm.getIntParam(paramIndex), new ValType (BasicValType.VTP_REAL, (byte) 1, (byte) 1, true), vec, 4);
    }

    public final class WrapTextMode implements Function { public void run(TomVM vm)       {
        int m = vm.getIntParam(1);
            switch (m) {
                case 0:
                    textMode = TextMode.TEXT_SIMPLE;
                    break;
                case 1:
                    textMode = TextMode.TEXT_BUFFERED;
                    break;
                case 2:
                    textMode = TextMode.TEXT_OVERLAID;
                    break;
            }
        }
    }
    public final class WrapCls implements Function { public void run(TomVM vm)            { appText.Clear ();  Redraw (); }}
    public final class WrapPrint implements Function { public void run(TomVM vm)          { appText.Write (vm.getStringParam(1)); Redraw (); }}
    public final class WrapPrintr implements Function { public void run(TomVM vm)         { appText.Write (vm.getStringParam(1)); appText.NewLine (); Redraw (); }}
    public final class WrapPrintr_2 implements Function { public void run(TomVM vm)       { appText.NewLine (); Redraw (); }}
    public final class WrapLocate implements Function
        { public void run(TomVM vm)         { appText.MoveCursor (vm.getIntParam(2), vm.getIntParam(1)); }}
    public final class WrapSwapBuffers implements Function { public void run(TomVM vm)    {
        appWindow.SwapBuffers ();
        appWindow.SetDontPaint (false);
    }}
    public final class WrapDrawText implements Function { public void run(TomVM vm)        { TextBasicLib.ForceDraw(); }}
    public final class WrapDrawText2 implements Function {  public void run(TomVM vm)       { TextBasicLib.ForceDraw(vm.getIntParam(1).byteValue()); }}
    public final class WrapResizeText implements Function { public void run(TomVM vm) {
        int rows = vm.getIntParam(1), cols = vm.getIntParam(2);
        if (rows < 1) {
            rows = 1;
        }
        if (rows > 500) {
            rows = 500;
        }
        if (cols < 1) {
            cols = 1;
        }
        if (cols > 500) {
            cols = 500;
        }
        TextBasicLib.appText.Resize (rows, cols);
        TextBasicLib.Redraw();
    }}
    public final class WrapTextRows implements Function { public void run(TomVM vm)   { vm.getReg().setIntVal( appText.Rows ()); }}
    public final class WrapTextCols implements Function { public void run(TomVM vm)   { vm.getReg().setIntVal( appText.Cols ()); }}
    public final class WrapInput implements Function { public void run(TomVM vm)      { vm.setRegString (appText.GetString (TextBasicLib.appWindow)); }}
    public final class WrapInkey implements Function { public void run(TomVM vm)      {
        int key = TextBasicLib.appWindow.getKey ();
        if (key != 0 && key <= Character.MAX_VALUE && key >= Character.MIN_VALUE) {
            vm.setRegString ( String.valueOf((char)key));
        } else {
            vm.setRegString ( "");
        }
    }}
    public final class WrapInScanKey implements Function { public void run(TomVM vm)  {
        vm.getReg().setIntVal( Character.getNumericValue(TextBasicLib.appWindow.getScanKey ()));
    }}
    public final class WrapClearKeys implements Function { public void run(TomVM vm) {
        appWindow.ClearKeyBuffers ();
    }}
    public final class WrapShowCursor implements Function { public void run(TomVM vm) { appText.ShowCursor (); Redraw (); }}
    public final class WrapHideCursor implements Function { public void run(TomVM vm) { appText.HideCursor (); Redraw (); }}
    public final class WrapKeyDown implements Function { public void run(TomVM vm)    {
        String s = vm.getStringParam(1);
        if (s.equals("")) {
            vm.getReg().setIntVal( 0);
        } else {
            vm.getReg().setIntVal( appWindow.isKeyDown (s.charAt(0)) ? -1 : 0);
        }
    }}
    public final class WrapScanKeyDown implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        // Windows version of Basic4GL only supports index range 0 - 255,
        // though this version uses a different input library that has a wider value range
        if (index < 0 || index > Character.MAX_VALUE) {
            vm.getReg().setIntVal( 0);
        } else {
            vm.getReg().setIntVal( appWindow.isKeyDown ((char)index) ? -1 : 0);
        }
    }}
    public final class WrapCharAt implements Function { public void run(TomVM vm) {
        char c = appText.TextAt (vm.getIntParam(2), vm.getIntParam(1));
        if (c == 0) {
            vm.setRegString ( "");
        } else {
            vm.setRegString ( String.valueOf(c));
        }
    }}
    public final class WrapColour implements Function { public void run(TomVM vm) {
        appText.SetColour (GLTextGrid.MakeColour(vm.getIntParam(3).shortValue(), vm.getIntParam(2).shortValue(), vm.getIntParam(1).shortValue()));
    }}
    public final class WrapFont implements Function { public void run(TomVM vm) {
        appText.SetTexture (vm.getIntParam(1));
    }}
    public final class WrapDefaultFont implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appText.DefaultTexture ());
    }}
    public final class WrapMouseX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseX ()) / appWindow.Width ());
    }}
    public final class WrapMouseY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseY ()) / appWindow.Height ());
    }}
    public final class WrapMouseXD implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseXD ()) / appWindow.Width () * 2f);
    }}
    public final class WrapMouseYD implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseYD ()) / appWindow.Height () * 2f);
    }}
    public final class WrapMouseButton implements Function { public void run(TomVM vm) {
        int button = vm.getIntParam(1);
        if (button >= 0 && button <= 2) {
            vm.getReg().setIntVal( appWindow.MouseButton (button) ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public final class WrapMouseWheel implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appWindow.MouseWheel ());
    }}

    public final class WrapCursorCol implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appText.CursorX());
    }}

    public final class WrapCursorRow implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appText.CursorY());
    }}

// Sprite functions
    GLSprite NewSprite(TomVM vm) {
        if (TextBasicLib.spriteCount < TextBasicLib.MAX_SPRITES)
        {
            // Create sprite object
            GLSprite sprite = new GLSprite();
            TextBasicLib.spriteCount++;

            // Add to sprite engine (so we can draw it)
            ((GLSpriteEngine) appText).AddSprite(sprite);

            // Add to store (so we can track it), and return index to VM
            boundSprite = sprites.alloc(sprite);
            vm.getReg().setIntVal( boundSprite);
            return sprite;
        }
        else {
            vm.getReg().setIntVal( 0);
            return null;
        }
    }
    GLTileMap NewTileMap(TomVM vm) {

        if (TextBasicLib.spriteCount < TextBasicLib.MAX_SPRITES)
        {
            // Create tile map object
            GLTileMap tileMap = new GLTileMap();
            TextBasicLib.spriteCount++;

            // Add to sprite engine (so we can draw it)
            ((GLSpriteEngine) appText).AddSprite(tileMap);

            // Add to store (so we can track it), and return index to VM
            boundSprite = sprites.alloc(tileMap);
            vm.getReg().setIntVal( boundSprite);
            return tileMap;
        }
        else {
            vm.getReg().setIntVal( 0);
            return null;
        }
    }

    public final class WrapNewSprite implements Function { public void run(TomVM vm) {

        // Allocate sprite
        NewSprite (vm);
        TextBasicLib.Redraw();
    }}
    public final class WrapNewSprite_2 implements Function { public void run(TomVM vm) {

        // Allocate sprite, and set single texture
        GLSprite sprite = NewSprite(vm);
        if (sprite != null) {
            sprite.SetTexture(vm.getIntParam(1));
        }
        TextBasicLib.Redraw();
    }}
    public final class WrapNewSprite_3 implements Function { public void run(TomVM vm) {

        // Allocate sprite and set an array of textures
        // Read textures
        Vector<Integer> textures = new Vector<Integer>();
        if (!GetTextures (vm, 1, textures)) {
            return;
        }

        // Allocate sprite
        GLBasicSprite sprite = NewSprite(vm);

        // Set textures
        if (sprite != null) {
            sprite.SetTextures(textures);
        }
        TextBasicLib.Redraw();
    }}
    public final class WrapNewTileMap implements Function { public void run(TomVM vm) {

        // Allocate sprite
        NewTileMap (vm);
        TextBasicLib.Redraw();
    }}
    public final class WrapNewTileMap_2 implements Function { public void run(TomVM vm) {

        // Allocate sprite, and set single texture
        GLTileMap  sprite = NewTileMap(vm);
        if (sprite != null) {
            sprite.SetTexture (vm.getIntParam(1));
        }
        TextBasicLib.Redraw();
    }}
    public final class WrapNewTileMap_3 implements Function { public void run(TomVM vm) {

        // Allocate sprite and set an array of textures
        // Read textures
        Vector<Integer> textures = new Vector<Integer>();
        if (!GetTextures(vm, 1, textures)) {
            return;
        }

        // Allocate sprite
        GLBasicSprite sprite = NewTileMap (vm);

        // Set textures
        if (sprite != null) {
            sprite.SetTextures (textures);
        }
        TextBasicLib.Redraw();
    }}
    public final class WrapDeleteSprite implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsBasicSprite (index)) {
            TextBasicLib.sprites.free(index);
            TextBasicLib.spriteCount--;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapBindSprite implements Function { public void run(TomVM vm) {
        TextBasicLib.boundSprite = vm.getIntParam(1);
    }}
    public final class WrapSprSetTexture implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).SetTexture(vm.getIntParam(1));
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetTextures implements Function { public void run(TomVM vm) {
        Vector<Integer> textures = new Vector<Integer>();
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) && GetTextures (vm, 1, textures)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).SetTextures(textures);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprAddTexture implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).AddTexture(vm.getIntParam(1));
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprAddTextures implements Function { public void run(TomVM vm) {
        Vector<Integer> textures = new Vector<Integer>();
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) && GetTextures(vm, 1, textures)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).AddTextures(textures);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetFrame implements Function { public void run(TomVM vm) {
        if (IsSprite(TextBasicLib.boundSprite)) {
            Sprite(TextBasicLib.boundSprite).SetFrame(vm.getRealParam(1));
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetX implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetY implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vm.getRealParam(1);
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetPos implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            ReadVec (vm, 1);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vec [0];
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vec [1];
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetPos_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vm.getRealParam(1);
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetZOrder implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).SetZOrder(vm.getRealParam(1));
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetXSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vm.getRealParam(1);
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetYSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            ReadVec (vm, 1);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vec [0];
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vec [1];
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetSize_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetScale implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).scale = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetXCentre implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerX = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetYCentre implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerY = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetXFlip implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipX = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetYFlip implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipY = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetVisible implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).visible = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetAngle implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).angle = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetColour implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            ReadVec (vm, 1);
            int size = Data.getArrayDimensionSize(vm.getData(), vm.getIntParam(1), 0);
            if (size > 4) {
                size = 4;
            }

            for (int i = 0; i < size; i++) {
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[i] = vec [i];
            }

            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetColour_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[0] = vm.getRealParam(3);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[1] = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[2] = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetColour_3 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[0] = vm.getRealParam(4);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[1] = vm.getRealParam(3);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[2] = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetAlpha implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] = vm.getRealParam(1);
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetParallax implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).parallax = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapSprSetSolid implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).solid = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw();
        }
    }}
    public final class WrapResizeSpriteArea implements Function { public void run(TomVM vm) {
        int width = vm.getRealParam(2).intValue(), height = vm.getRealParam(1).intValue();
        if (width <= 0 || height <= 0) {
            vm.functionError("Width and height must both be greater than 0");
            return;
        }
        ((GLSpriteEngine) TextBasicLib.appText).SetWidth  (width);
        ((GLSpriteEngine) TextBasicLib.appText).SetHeight (height);
    }}
    public final class WrapSpriteAreaWidth implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).Width());
    }}
    public final class WrapSpriteAreaHeight implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).Height());
    }}
    public final class WrapSprFrame implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite(TextBasicLib.boundSprite) ?
                Sprite (TextBasicLib.boundSprite).Frame () : 0);
    }}
    public final class WrapSprX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX : 0);
    }}
    public final class WrapSprY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY : 0);
    }}
    public final class WrapSprPos implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            result [0] = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX;
            result [1] = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public final class WrapSprZOrder implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).ZOrder() : 0);
    }}
    public final class WrapSprXSize implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX : 0);
    }}
    public final class WrapSprYSize implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY : 0);
    }}
    public final class WrapSprScale implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).scale : 0);
    }}
    public final class WrapSprXCentre implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerX : 0);
    }}
    public final class WrapSprYCentre implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerY : 0);
    }}
    public final class WrapSprXFlip implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipX ? -1f : 0) : 0);
    }}
    public final class WrapSprYFlip implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipY ? -1f : 0) : 0);
    }}
    public final class WrapSprVisible implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).visible ? -1f : 0) : 0);
    }}
    public final class WrapSprAngle implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).angle : 0);
    }}
    public final class WrapSprColour implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f, 0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            for (int i = 0; i < 4; i ++) {
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[i] = result[i];
            }
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 4, Arrays.asList(result)));
    }}
    public final class WrapSprAlpha implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] : 0);
    }}
    public final class WrapSprParallax implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).parallax ? -1f : 0) : 0);
    }}
    public final class WrapSprSolid implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).solid ? -1f : 0) : 0);
    }}
    public final class WrapSprLeft implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            GLSprite sprite = Sprite (TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionX + -sprite.centerX * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprRight implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            GLSprite sprite = Sprite (TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionX + (1 - sprite.centerX) * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprTop implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            GLSprite sprite = Sprite (TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionY + -sprite.centerY * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprBottom implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            GLSprite sprite = Sprite (TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionY + (1 - sprite.centerY) * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprXVel implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite (TextBasicLib.boundSprite) ?
                Sprite (TextBasicLib.boundSprite).m_xd : 0);
    }}
    public final class WrapSprYVel implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite (TextBasicLib.boundSprite) ?
                Sprite (TextBasicLib.boundSprite).m_yd : 0);
    }}
    public final class WrapSprVel implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        if (IsSprite (TextBasicLib.boundSprite)) {
            result [0] = Sprite (TextBasicLib.boundSprite).m_xd;
            result [1] = Sprite (TextBasicLib.boundSprite).m_yd;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public final class WrapSprSpin implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(IsSprite(TextBasicLib.boundSprite) ?
                Sprite(TextBasicLib.boundSprite).m_angled : 0);
    }}
    public final class WrapSprAnimSpeed implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite (TextBasicLib.boundSprite) ?
                Sprite (TextBasicLib.boundSprite).m_angled : 0);
    }}

    public final class WrapSprAnimLoop implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite (TextBasicLib.boundSprite) ?
                (Sprite (TextBasicLib.boundSprite).m_animLoop ? -1f : 0) : 0);
    }}
    public final class WrapSprAnimDone implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( IsSprite (TextBasicLib.boundSprite) ?
                (Sprite (TextBasicLib.boundSprite).AnimDone () ? -1f : 0) : 0);
    }}
    public final class WrapSprFrame_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( IsSprite (index) ?
                Sprite (index).Frame () : 0);
    }}
    public final class WrapSprX_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).positionX : 0);
    }}
    public final class WrapSprY_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).positionY : 0);
    }}
    public final class WrapSprPos_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(index)) {
            result [0] = TextBasicLib.sprites.getValueAt(index).positionX;
            result [1] = TextBasicLib.sprites.getValueAt(index).positionY;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public final class WrapSprZOrder_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).ZOrder() : 0);
    }}
    public final class WrapSprXSize_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).sizeX : 0);
    }}
    public final class WrapSprYSize_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).sizeY : 0);
    }}
    public final class WrapSprScale_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(  TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).scale : 0);
    }}
    public final class WrapSprXCentre_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).centerX : 0);
    }}
    public final class WrapSprYCentre_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).centerY : 0);
    }}
    public final class WrapSprXFlip_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).flipX ? -1f : 0) : 0);
    }}
    public final class WrapSprYFlip_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).flipY ? -1f : 0) : 0);
    }}
    public final class WrapSprVisible_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).visible ? -1f : 0) : 0);
    }}
    public final class WrapSprAngle_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).angle : 0);
    }}
    public final class WrapSprColour_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f, 0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(index)) {
            for ( int i = 0; i < 4; i ++) {
                TextBasicLib.sprites.getValueAt(index).color[i] = result[i];
            }
        }

        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 4, Arrays.asList(result)));
    }}
    public final class WrapSprAlpha_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).color[3] : 0);
    }}
    public final class WrapSprParallax_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).parallax ? -1f : 0) : 0);
    }}
    public final class WrapSprSolid_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).solid ? -1f : 0) : 0);
    }}
    public final class WrapSprLeft_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsSprite (index)) {
            GLSprite sprite = Sprite (index);
            vm.getReg().setRealVal( sprite.positionX + -sprite.centerX * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprRight_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsSprite (index)) {
            GLSprite sprite = Sprite (index);
            vm.getReg().setRealVal( sprite.positionX + (1 - sprite.centerX) * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprTop_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsSprite (index)) {
            GLSprite sprite = Sprite (index);
            vm.getReg().setRealVal( sprite.positionY + -sprite.centerY * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprBottom_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsSprite (index)) {
            GLSprite sprite = Sprite (index);
            vm.getReg().setRealVal( sprite.positionY + (1 - sprite.centerY) * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public final class WrapSprXVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(  IsSprite (index) ?
                Sprite (index).m_xd : 0);
    }}
    public final class WrapSprYVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( IsSprite (index) ?
                Sprite (index).m_yd : 0);
    }}
    public final class WrapSprVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f};
        if (IsSprite (index)) {
            result [0] = Sprite (index).m_xd;
            result [1] = Sprite (index).m_yd;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public final class WrapSprSpin_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(IsSprite(index) ?
                Sprite(index).m_angled : 0);
    }}
    public final class WrapSprAnimSpeed_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( IsSprite (index) ?
                Sprite (index).m_angled : 0);
    }}
    public final class WrapSprAnimLoop_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( IsSprite (index) ?
                (Sprite (index).m_animLoop ? -1f : 0) : 0);
    }}
    public final class WrapSprAnimDone_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( IsSprite (index) ?
                (Sprite (index).AnimDone () ? -1f : 0) : 0);
    }}
    public final class WrapSprSetXVel implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_xd = vm.getRealParam(1);
        }
    }}
    public final class WrapSprSetYVel implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_yd = vm.getRealParam(1);
        }
    }}
    public final class WrapSprSetVel implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            ReadVec (vm, 1);
            Sprite (TextBasicLib.boundSprite).m_xd = vec [0];
            Sprite (TextBasicLib.boundSprite).m_yd = vec [1];
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetVel_2 implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_xd = vm.getRealParam(2);
            Sprite (TextBasicLib.boundSprite).m_yd = vm.getRealParam(1);
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetSpin implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_angled = vm.getRealParam(1);
        }
    }}
    public final class WrapSprSetAnimSpeed implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_framed = vm.getRealParam(1);
        }
    }}
    public final class WrapSprSetAnimLoop implements Function { public void run(TomVM vm) {
        if (IsSprite (TextBasicLib.boundSprite)) {
            Sprite (TextBasicLib.boundSprite).m_animLoop = vm.getIntParam(1) != 0;
        }
    }}
    public final class WrapAnimateSprites implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).Animate ();
        TextBasicLib.Redraw ();
    }}
    public final class WrapAnimateSpriteFrames implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine)TextBasicLib.appText).AnimateFrames();
        TextBasicLib.Redraw ();
    }}
    public final class WrapCopySprite implements Function { public void run(TomVM vm) {

        // CopySprite (dest, source)
        // Copies sprite "source" to sprite "dest"
        // Sprites must be the same type. I.e. you can't copy a standard sprite
        // to a tile map or vice versa.
        int index = vm.getIntParam(1);
        if (    IsBasicSprite (TextBasicLib.boundSprite) && IsBasicSprite (index)
                &&  BasicSprite(TextBasicLib.boundSprite).getGLSpriteType() == BasicSprite (index).getGLSpriteType()) {
            if (IsSprite(TextBasicLib.boundSprite)) {
                Sprite (TextBasicLib.boundSprite).Copy (Sprite (index));
            } else if (IsTileMap (TextBasicLib.boundSprite)) {
                TileMap (TextBasicLib.boundSprite).Copy (TileMap (index));
            }
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprType implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   IsBasicSprite (TextBasicLib.boundSprite) ?
                BasicSprite (TextBasicLib.boundSprite).getGLSpriteType().getType() : GLSpriteType.SPR_INVALID.getType());
    }}
    public final class WrapSprXTiles implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   IsTileMap(boundSprite) ?
                TileMap(TextBasicLib.boundSprite).getTilesX() : 0);
    }}
    public final class WrapSprYTiles implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   IsTileMap (boundSprite ) ?
                TileMap (TextBasicLib.boundSprite).getTilesY() : 0);
    }}
    public final class WrapSprType_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(   IsBasicSprite(index) ?
                BasicSprite(index).getGLSpriteType().getType() : GLSpriteType.SPR_INVALID.getType());
    }}
    public final class WrapSprXTiles_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(   IsTileMap(index) ?
                TileMap(index).getTilesX() : 0);
    }}
    public final class WrapSprYTiles_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(IsTileMap(index) ?
                TileMap(index).getTilesY() : 0);
    }
    }
    public final class WrapSprSetTiles implements Function { public void run(TomVM vm) {

        if (IsTileMap (TextBasicLib.boundSprite)) {

            // Get tiles from array param
            Vector<Integer> tiles = new Vector<Integer>();
            IntBuffer xSize = BufferUtils.createIntBuffer(1), ySize = BufferUtils.createIntBuffer(1);
            TextBasicLib.GetTiles(vm, 1, xSize, ySize, tiles);

            // Set
            TileMap (TextBasicLib.boundSprite).setTiles(xSize.get(0), ySize.get(0), tiles);
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetXRepeat implements Function { public void run(TomVM vm) {
        if (IsTileMap (TextBasicLib.boundSprite)) {
            TileMap (TextBasicLib.boundSprite).repeatX = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprSetYRepeat implements Function { public void run(TomVM vm) {
        if (IsTileMap (TextBasicLib.boundSprite)) {
            TileMap (TextBasicLib.boundSprite).repeatY = vm.getIntParam(1) != 0;
            TextBasicLib.Redraw ();
        }
    }}
    public final class WrapSprXRepeat implements Function { public void run(TomVM vm) {
        if (IsTileMap (TextBasicLib.boundSprite)) {
            vm.getReg().setIntVal( TileMap (TextBasicLib.boundSprite).repeatX ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public final class WrapSprYRepeat implements Function { public void run(TomVM vm) {
        if (IsTileMap (TextBasicLib.boundSprite)) {
            vm.getReg().setIntVal( TileMap (TextBasicLib.boundSprite).repeatY ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public final class WrapSprXRepeat_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsTileMap (index)) {
            vm.getReg().setIntVal( TileMap (index).repeatX ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public final class WrapSprYRepeat_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (IsTileMap (index)) {
            vm.getReg().setIntVal( TileMap (index).repeatY ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public final class WrapSprCameraSetX implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).m_camX = vm.getRealParam(1);
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraSetY implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).m_camY = vm.getRealParam(1);
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraSetZ implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).m_camZ = vm.getRealParam(1);
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraSetPos implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).m_camX = vm.getRealParam(2);
        ((GLSpriteEngine) TextBasicLib.appText).m_camY = vm.getRealParam(1);
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraSetPos_2 implements Function { public void run(TomVM vm) {
        ReadVec (vm, 1);
        ((GLSpriteEngine) TextBasicLib.appText).m_camX = vec [0];
        ((GLSpriteEngine) TextBasicLib.appText).m_camY = vec [1];
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraSetAngle implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).m_camAngle = vm.getRealParam(1);
        TextBasicLib.Redraw ();
    }}
    public final class WrapSprCameraX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).m_camX);
    }}
    public final class WrapSprCameraY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).m_camY);
    }}
    public final class WrapSprCameraZ implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).m_camZ);
    }}
    public final class WrapSprCameraPos implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        result [0] = ((GLSpriteEngine) TextBasicLib.appText).m_camX;
        result [1] = ((GLSpriteEngine) TextBasicLib.appText).m_camY;
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public final class WrapSprCameraAngle implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).m_camAngle);
    }}
    public final class WrapSprCameraFOV implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).FOV ());
    }}
    public final class WrapSprCameraSetFOV implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).SetFOV (vm.getRealParam(1));
        TextBasicLib.Redraw();
    }}
    public final class WrapClearLine implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.ClearLine();
        TextBasicLib.Redraw ();
    }}
    public final class WrapClearRegion implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.ClearRegion(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
        TextBasicLib.Redraw ();
    }}

    public final class WrapTextScroll implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(TextBasicLib.appText.Scroll() ? 1 : 0);
    }}

    public final class WrapSetTextScroll implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.setScroll(vm.getIntParam(1) == 1);
    }}

    public final class WrapClearSprites implements Function { public void run(TomVM vm) {
        TextBasicLib.sprites.clear();
        TextBasicLib.spriteCount = 0;
        TextBasicLib.Redraw();
    }}

    public final class WrapSprSetBlendFunc implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            GLBasicSprite sprite = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite);
            sprite.srcBlend = vm.getIntParam(2);
            sprite.dstBlend = vm.getIntParam(1);
        }
    }}


//DLLFUNC
public final class WrapSpriteCount implements Function {
    public void run(TomVM vm) {
        vm.getReg().setIntVal( TextBasicLib.spriteCount);
    }
}




}
