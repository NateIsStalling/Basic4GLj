package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
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
    public void init(TomVM vm, IAppSettings settings, String[] args) {
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
        spriteEngine.setDefaults();
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
    public Map<String, FunctionSpecification[]> specs() {

        Map<String, FunctionSpecification[]> s = new HashMap<String, FunctionSpecification[]>();

        s.put("cls", new FunctionSpecification[]{ new FunctionSpecification( WrapCls.class, new ParamTypeList (), false, false, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("print", new FunctionSpecification[]{ new FunctionSpecification( WrapPrint.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("printr", new FunctionSpecification[]{
                new FunctionSpecification( WrapPrintr.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),false, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapPrintr_2.class, new ParamTypeList (), false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("locate", new FunctionSpecification[]{ new FunctionSpecification( WrapLocate.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),false, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("swapbuffers", new FunctionSpecification[]{ new FunctionSpecification( WrapSwapBuffers.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("drawtext", new FunctionSpecification[]{
                new FunctionSpecification( WrapDrawText.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapDrawText2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textmode", new FunctionSpecification[]{ new FunctionSpecification( WrapTextMode.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("resizetext", new FunctionSpecification[]{ new FunctionSpecification( WrapResizeText.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textrows", new FunctionSpecification[]{ new FunctionSpecification( WrapTextRows.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("textcols", new FunctionSpecification[]{ new FunctionSpecification( WrapTextCols.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("input$", new FunctionSpecification[]{ new FunctionSpecification( WrapInput.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_STRING),true, false, null)});
    s.put("inkey$", new FunctionSpecification[]{ new FunctionSpecification( WrapInkey.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_STRING), false, false, null)});
    s.put("inscankey", new FunctionSpecification[]{ new FunctionSpecification( WrapInScanKey.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearkeys", new FunctionSpecification[]{ new FunctionSpecification( WrapClearKeys.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("showcursor", new FunctionSpecification[]{ new FunctionSpecification( WrapShowCursor.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("hidecursor", new FunctionSpecification[]{ new FunctionSpecification( WrapHideCursor.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("keydown", new FunctionSpecification[]{ new FunctionSpecification( WrapKeyDown.class, new ParamTypeList (new ValType (BasicValType.VTP_STRING)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("scankeydown", new FunctionSpecification[]{ new FunctionSpecification( WrapScanKeyDown.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("charat$", new FunctionSpecification[]{ new FunctionSpecification( WrapCharAt.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_STRING), false, false, null)});
    s.put("color", new FunctionSpecification[]{ new FunctionSpecification( WrapColour.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("font", new FunctionSpecification[]{ new FunctionSpecification( WrapFont.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("defaultfont", new FunctionSpecification[]{ new FunctionSpecification( WrapDefaultFont.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("mouse_x", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_y", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_xd", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseXD.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_yd", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseYD.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("mouse_button", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseButton.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("mouse_wheel", new FunctionSpecification[]{ new FunctionSpecification( WrapMouseWheel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearline", new FunctionSpecification[]{ new FunctionSpecification( WrapClearLine.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("clearregion", new FunctionSpecification[]{ new FunctionSpecification( WrapClearRegion.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("textscroll", new FunctionSpecification[]{ new FunctionSpecification( WrapTextScroll.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("settextscroll", new FunctionSpecification[]{ new FunctionSpecification( WrapSetTextScroll.class, new ParamTypeList(new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("CursorCol", new FunctionSpecification[]{ new FunctionSpecification( WrapCursorCol.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("CursorRow", new FunctionSpecification[]{ new FunctionSpecification( WrapCursorRow.class, new ParamTypeList(), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

    s.put("newsprite", new FunctionSpecification[]{
            new FunctionSpecification( WrapNewSprite.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapNewSprite_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapNewSprite_3.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("newtilemap", new FunctionSpecification[]{
            new FunctionSpecification( WrapNewTileMap.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapNewTileMap_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapNewTileMap_3.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("deletesprite", new FunctionSpecification[]{ new FunctionSpecification( WrapDeleteSprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("bindsprite", new FunctionSpecification[]{ new FunctionSpecification( WrapBindSprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsettexture", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetTexture.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsettextures", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetTextures.class, new ParamTypeList (new ValType  (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spraddtexture", new FunctionSpecification[]{ new FunctionSpecification( WrapSprAddTexture.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spraddtextures", new FunctionSpecification[]{ new FunctionSpecification( WrapSprAddTextures.class, new ParamTypeList (new ValType (BasicValType.VTP_INT,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetframe", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetFrame.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetx", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetX.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsety", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetY.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetzorder", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetZOrder.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetpos", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprSetPos.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL ,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprSetPos_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxsize", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetXSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetysize", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetYSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetsize", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprSetSize.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprSetSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetscale", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetScale.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxcentre", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetXCentre.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetycentre", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetYCentre.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetxflip", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetXFlip.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetyflip", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetYFlip.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetvisible", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetVisible.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetangle", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetAngle.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetcolor", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprSetColour.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprSetColour_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprSetColour_3.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetalpha", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetAlpha.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetparallax", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetParallax.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("sprsetsolid", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetSolid.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("resizespritearea", new FunctionSpecification[]{ new FunctionSpecification( WrapResizeSpriteArea.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
    s.put("spriteareawidth", new FunctionSpecification[]{ new FunctionSpecification( WrapSpriteAreaWidth.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("spriteareaheight", new FunctionSpecification[]{ new FunctionSpecification( WrapSpriteAreaHeight.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("sprframe", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprFrame.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprFrame_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprx", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprX_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("spry", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprY_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
    s.put("sprpos", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprPos.class, new ParamTypeList(), true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
            new FunctionSpecification( WrapSprPos_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType( BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});


        s.put("sprzorder", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprZOrder.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprZOrder_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxsize", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprXSize.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprXSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprysize", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprYSize.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprYSize_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprscale", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprScale.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprScale_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxcentre", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprXCentre.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprXCentre_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprycentre", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprYCentre.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprYCentre_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});

        s.put("sprxflip", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprXFlip.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprXFlip_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spryflip", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprYFlip.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprYFlip_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprvisible", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprVisible.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprVisible_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprangle", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprAngle.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprAngle_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcolor", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprColour.class, new ParamTypeList (), true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
            new FunctionSpecification( WrapSprColour_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});
        s.put("spralpha", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprAlpha.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprAlpha_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprparallax", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprParallax.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprParallax_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsolid", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprSolid.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprSolid_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprleft", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprLeft.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprLeft_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprright", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprRight.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprRight_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprtop", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprTop.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprTop_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprbottom", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprBottom.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprBottom_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprxvel", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprXVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
            new FunctionSpecification( WrapSprXVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spryvel", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprYVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprYVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprvel", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprVel.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null),
                new FunctionSpecification( WrapSprVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true), false, true, null)});
        s.put("sprspin", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprSpin.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprSpin_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spranimspeed", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprAnimSpeed.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null),
                new FunctionSpecification( WrapSprAnimSpeed_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("spranimloop", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprAnimLoop.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprAnimLoop_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spranimdone", new FunctionSpecification[]{
            new FunctionSpecification( WrapSprAnimDone.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
            new FunctionSpecification( WrapSprAnimDone_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprsetxvel", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetXVel.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetyvel", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetYVel.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetvel", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprSetVel.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL,(byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprSetVel_2.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetspin", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetSpin.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetanimspeed", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetAnimSpeed.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetanimloop", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetAnimLoop.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("animatesprites", new FunctionSpecification[]{ new FunctionSpecification( WrapAnimateSprites.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("animatespriteframes", new FunctionSpecification[]{ new FunctionSpecification( WrapAnimateSpriteFrames.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("copysprite", new FunctionSpecification[]{ new FunctionSpecification( WrapCopySprite.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprtype", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprType.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprType_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprxtiles", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprXTiles.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprXTiles_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprytiles", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprYTiles.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprYTiles_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});

        s.put("sprsettiles", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetTiles.class, new ParamTypeList (new ValType (BasicValType.VTP_INT,(byte)2, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetxrepeat", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetXRepeat.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetyrepeat", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetYRepeat.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprxrepeat", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprXRepeat.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprXRepeat_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spryrepeat", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprYRepeat.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprYRepeat_2.class, new ParamTypeList (new ValType (BasicValType.VTP_INT)),true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetx", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraSetX.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasety", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraSetY.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetz", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraSetZ.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetpos", new FunctionSpecification[]{
                new FunctionSpecification( WrapSprCameraSetPos.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL), new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null),
                new FunctionSpecification( WrapSprCameraSetPos_2.class, new ParamTypeList (new ValType  (BasicValType.VTP_REAL, (byte)1, (byte)1, true)), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerasetangle", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraSetAngle.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprcamerax", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraX.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcameray", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraY.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcameraz", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraZ.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerapos", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraPos.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL, (byte)1, (byte)1, true), false, true, null)});
        s.put("sprcameraangle", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraAngle.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerafov", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraFOV.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_REAL), false, false, null)});
        s.put("sprcamerasetfov", new FunctionSpecification[]{ new FunctionSpecification( WrapSprCameraSetFOV.class, new ParamTypeList (new ValType (BasicValType.VTP_REAL)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("clearsprites", new FunctionSpecification[]{ new FunctionSpecification( WrapClearSprites.class, new ParamTypeList (), true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("sprsetblendfunc", new FunctionSpecification[]{ new FunctionSpecification( WrapSprSetBlendFunc.class, new ParamTypeList (new ValType (BasicValType.VTP_INT), new ValType (BasicValType.VTP_INT)),true, false, new ValType (BasicValType.VTP_INT), false, false, null)});
        s.put("spritecount", new FunctionSpecification[]{ new FunctionSpecification( WrapSpriteCount.class, new ParamTypeList (), true, true, new ValType (BasicValType.VTP_INT), false, false, null)});
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
        TextBasicLib.redraw();
    }
    public void locate(int x, int y) {
        TextBasicLib.appText.MoveCursor(x, y);
    }
    public void cls() {
        TextBasicLib.appText.Clear();
        TextBasicLib.redraw();
    }
    public void clearRegion(int x1, int y1, int x2, int y2) {
        TextBasicLib.appText.ClearRegion(x1, y1, x2, y2);
        TextBasicLib.redraw();
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
        TextBasicLib.redraw();
    }
    public void setTextScrollEnabled(boolean scroll) {
        TextBasicLib.appText.setScroll(scroll);
    }
    public boolean getTextScrollEnabled() {
        return TextBasicLib.appText.Scroll();
    }
    public void drawText() {
        TextBasicLib.forceDraw();
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

    static GLSpriteStore sprites;
    static int boundSprite;

    static TextMode textMode = TextMode.TEXT_SIMPLE;

    static void forceDraw(){ forceDraw((byte)(GLTextGrid.DRAW_TEXT | GLSpriteEngine.DRAW_SPRITES));}
    static void forceDraw(byte flags) {
        if (textMode == TextMode.TEXT_SIMPLE || textMode == TextMode.TEXT_BUFFERED) {
            glClear (GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        }

        appText.Draw(flags);

        if (textMode == TextMode.TEXT_SIMPLE || textMode == TextMode.TEXT_BUFFERED) {
            appWindow.SwapBuffers ();
        }
    }

    static void redraw() {
        if (textMode == TextMode.TEXT_SIMPLE) {
            forceDraw();
        }
    }



    boolean getTextures(TomVM vm, int paramIndex, Vector<Integer> dest) {

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

    static boolean isBasicSprite(int index) {
        return TextBasicLib.sprites.isIndexStored(index);
    }
    static GLBasicSprite getBasicSprite(int index) {
        assertTrue(isBasicSprite(index));
        return TextBasicLib.sprites.getValueAt(index);
    }
    static boolean isSprite(int index) {
        return isBasicSprite(index)
                && getBasicSprite(index).getGLSpriteType() == GLSpriteType.SPR_SPRITE;
    }
    static GLSprite getSprite(int index) {
        assertTrue(isSprite(index));
        return (GLSprite ) getBasicSprite(index);
    }
    static boolean isTileMap(int index) {
        return isBasicSprite(index)
                && getBasicSprite(index).getGLSpriteType() == GLSpriteType.SPR_TILEMAP;
    }
    static GLTileMap getTileMap(int index) {
        assertTrue(isTileMap(index));
        return (GLTileMap ) getBasicSprite(index);
    }
    public static void getTiles(TomVM vm, int paramIndex, IntBuffer xSize, IntBuffer ySize, Vector<Integer> dest) {

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
    void readVec(TomVM vm, int paramIndex) {

        // Read data
        vec [0] = 0;
        vec [1] = 0;
        vec [2] = 0;
        vec [3] = 1;
        Data.readArray(vm.getData(), vm.getIntParam(paramIndex), new ValType (BasicValType.VTP_REAL, (byte) 1, (byte) 1, true), vec, 4);
    }

    public static final class WrapTextMode implements Function { public void run(TomVM vm)       {
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
    public static final class WrapCls implements Function { public void run(TomVM vm)            { appText.Clear ();  redraw(); }}
    public static final class WrapPrint implements Function { public void run(TomVM vm)          { appText.Write (vm.getStringParam(1)); redraw(); }}
    public static final class WrapPrintr implements Function { public void run(TomVM vm)         { appText.Write (vm.getStringParam(1)); appText.NewLine (); redraw(); }}
    public static final class WrapPrintr_2 implements Function { public void run(TomVM vm)       { appText.NewLine (); redraw(); }}
    public static final class WrapLocate implements Function
        { public void run(TomVM vm)         { appText.MoveCursor (vm.getIntParam(2), vm.getIntParam(1)); }}
    public static final class WrapSwapBuffers implements Function { public void run(TomVM vm)    {
        appWindow.SwapBuffers ();
        appWindow.SetDontPaint (false);
    }}
    public static final class WrapDrawText implements Function { public void run(TomVM vm)        { TextBasicLib.forceDraw(); }}
    public static final class WrapDrawText2 implements Function {  public void run(TomVM vm)       { TextBasicLib.forceDraw(vm.getIntParam(1).byteValue()); }}
    public static final class WrapResizeText implements Function { public void run(TomVM vm) {
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
        TextBasicLib.redraw();
    }}
    public static final class WrapTextRows implements Function { public void run(TomVM vm)   { vm.getReg().setIntVal( appText.Rows ()); }}
    public static final class WrapTextCols implements Function { public void run(TomVM vm)   { vm.getReg().setIntVal( appText.Cols ()); }}
    public static final class WrapInput implements Function { public void run(TomVM vm)      { vm.setRegString (appText.GetString (TextBasicLib.appWindow)); }}
    public static final class WrapInkey implements Function { public void run(TomVM vm)      {
        int key = TextBasicLib.appWindow.getKey ();
        if (key != 0 && key <= Character.MAX_VALUE && key >= Character.MIN_VALUE) {
            vm.setRegString ( String.valueOf((char)key));
        } else {
            vm.setRegString ( "");
        }
    }}
    public static final class WrapInScanKey implements Function { public void run(TomVM vm)  {
        vm.getReg().setIntVal( Character.getNumericValue(TextBasicLib.appWindow.getScanKey ()));
    }}
    public static final class WrapClearKeys implements Function { public void run(TomVM vm) {
        appWindow.ClearKeyBuffers ();
    }}
    public static final class WrapShowCursor implements Function { public void run(TomVM vm) { appText.ShowCursor (); redraw(); }}
    public static final class WrapHideCursor implements Function { public void run(TomVM vm) { appText.HideCursor (); redraw(); }}
    public static final class WrapKeyDown implements Function { public void run(TomVM vm)    {
        String s = vm.getStringParam(1);
        if (s.equals("")) {
            vm.getReg().setIntVal( 0);
        } else {
            vm.getReg().setIntVal( appWindow.isKeyDown (s.charAt(0)) ? -1 : 0);
        }
    }}
    public static final class WrapScanKeyDown implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        // Windows version of Basic4GL only supports index range 0 - 255,
        // though this version uses a different input library that has a wider value range
        if (index < 0 || index > Character.MAX_VALUE) {
            vm.getReg().setIntVal( 0);
        } else {
            vm.getReg().setIntVal( appWindow.isKeyDown ((char)index) ? -1 : 0);
        }
    }}
    public static final class WrapCharAt implements Function { public void run(TomVM vm) {
        char c = appText.TextAt (vm.getIntParam(2), vm.getIntParam(1));
        if (c == 0) {
            vm.setRegString ( "");
        } else {
            vm.setRegString ( String.valueOf(c));
        }
    }}
    public static final class WrapColour implements Function { public void run(TomVM vm) {
        appText.SetColour (GLTextGrid.MakeColour(vm.getIntParam(3).shortValue(), vm.getIntParam(2).shortValue(), vm.getIntParam(1).shortValue()));
    }}
    public static final class WrapFont implements Function { public void run(TomVM vm) {
        appText.SetTexture (vm.getIntParam(1));
    }}
    public static final class WrapDefaultFont implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appText.DefaultTexture ());
    }}
    public static final class WrapMouseX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseX ()) / appWindow.Width ());
    }}
    public static final class WrapMouseY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseY ()) / appWindow.Height ());
    }}
    public static final class WrapMouseXD implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseXD ()) / appWindow.Width () * 2f);
    }}
    public static final class WrapMouseYD implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((float) appWindow.MouseYD ()) / appWindow.Height () * 2f);
    }}
    public static final class WrapMouseButton implements Function { public void run(TomVM vm) {
        int button = vm.getIntParam(1);
        if (button >= 0 && button <= 2) {
            vm.getReg().setIntVal( appWindow.MouseButton (button) ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public static final class WrapMouseWheel implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appWindow.MouseWheel ());
    }}

    public static final class WrapCursorCol implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal( appText.CursorX());
    }}

    public static final class WrapCursorRow implements Function { public void run(TomVM vm) {
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
            ((GLSpriteEngine) appText).addSprite(sprite);

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
            ((GLSpriteEngine) appText).addSprite(tileMap);

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
        TextBasicLib.redraw();
    }}
    public final class WrapNewSprite_2 implements Function { public void run(TomVM vm) {

        // Allocate sprite, and set single texture
        GLSprite sprite = NewSprite(vm);
        if (sprite != null) {
            sprite.setTexture(vm.getIntParam(1));
        }
        TextBasicLib.redraw();
    }}
    public final class WrapNewSprite_3 implements Function { public void run(TomVM vm) {

        // Allocate sprite and set an array of textures
        // Read textures
        Vector<Integer> textures = new Vector<Integer>();
        if (!getTextures(vm, 1, textures)) {
            return;
        }

        // Allocate sprite
        GLBasicSprite sprite = NewSprite(vm);

        // Set textures
        if (sprite != null) {
            sprite.setTextures(textures);
        }
        TextBasicLib.redraw();
    }}
    public final class WrapNewTileMap implements Function { public void run(TomVM vm) {

        // Allocate sprite
        NewTileMap (vm);
        TextBasicLib.redraw();
    }}
    public final class WrapNewTileMap_2 implements Function { public void run(TomVM vm) {

        // Allocate sprite, and set single texture
        GLTileMap  sprite = NewTileMap(vm);
        if (sprite != null) {
            sprite.setTexture(vm.getIntParam(1));
        }
        TextBasicLib.redraw();
    }}
    public final class WrapNewTileMap_3 implements Function { public void run(TomVM vm) {

        // Allocate sprite and set an array of textures
        // Read textures
        Vector<Integer> textures = new Vector<Integer>();
        if (!getTextures(vm, 1, textures)) {
            return;
        }

        // Allocate sprite
        GLBasicSprite sprite = NewTileMap (vm);

        // Set textures
        if (sprite != null) {
            sprite.setTextures(textures);
        }
        TextBasicLib.redraw();
    }}
    public static final class WrapDeleteSprite implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isBasicSprite(index)) {
            TextBasicLib.sprites.free(index);
            TextBasicLib.spriteCount--;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapBindSprite implements Function { public void run(TomVM vm) {
        TextBasicLib.boundSprite = vm.getIntParam(1);
    }}
    public static final class WrapSprSetTexture implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).setTexture(vm.getIntParam(1));
            TextBasicLib.redraw();
        }
    }}
    public final class WrapSprSetTextures implements Function { public void run(TomVM vm) {
        Vector<Integer> textures = new Vector<Integer>();
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) && getTextures(vm, 1, textures)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).setTextures(textures);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprAddTexture implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).addTexture(vm.getIntParam(1));
            TextBasicLib.redraw();
        }
    }}
    public final class WrapSprAddTextures implements Function { public void run(TomVM vm) {
        Vector<Integer> textures = new Vector<Integer>();
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) && getTextures(vm, 1, textures)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).addTextures(textures);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetFrame implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).setFrame(vm.getRealParam(1));
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetX implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetY implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public final class WrapSprSetPos implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            readVec(vm, 1);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vec [0];
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vec [1];
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetPos_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetZOrder implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).setZOrder(vm.getRealParam(1));
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetXSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetYSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public final class WrapSprSetSize implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            readVec(vm, 1);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vec [0];
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vec [1];
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetSize_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetScale implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).scale = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetXCentre implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerX = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetYCentre implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerY = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetXFlip implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipX = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetYFlip implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipY = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetVisible implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).visible = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetAngle implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).angle = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public final class WrapSprSetColour implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            readVec(vm, 1);
            int size = Data.getArrayDimensionSize(vm.getData(), vm.getIntParam(1), 0);
            if (size > 4) {
                size = 4;
            }

            for (int i = 0; i < size; i++) {
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[i] = vec [i];
            }

            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetColour_2 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[0] = vm.getRealParam(3);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[1] = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[2] = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetColour_3 implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[0] = vm.getRealParam(4);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[1] = vm.getRealParam(3);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[2] = vm.getRealParam(2);
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetAlpha implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetParallax implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).parallax = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetSolid implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).solid = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapResizeSpriteArea implements Function { public void run(TomVM vm) {
        int width = vm.getRealParam(2).intValue(), height = vm.getRealParam(1).intValue();
        if (width <= 0 || height <= 0) {
            vm.functionError("Width and height must both be greater than 0");
            return;
        }
        ((GLSpriteEngine) TextBasicLib.appText).setWidth(width);
        ((GLSpriteEngine) TextBasicLib.appText).setHeight(height);
    }}
    public static final class WrapSpriteAreaWidth implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).getWidth());
    }}
    public static final class WrapSpriteAreaHeight implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).getHeight());
    }}
    public static final class WrapSprFrame implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                getSprite(TextBasicLib.boundSprite).getFrame() : 0);
    }}
    public static final class WrapSprX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX : 0);
    }}
    public static final class WrapSprY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY : 0);
    }}
    public static final class WrapSprPos implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            result [0] = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionX;
            result [1] = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).positionY;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public static final class WrapSprZOrder implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).getZOrder() : 0);
    }}
    public static final class WrapSprXSize implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeX : 0);
    }}
    public static final class WrapSprYSize implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).sizeY : 0);
    }}
    public static final class WrapSprScale implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).scale : 0);
    }}
    public static final class WrapSprXCentre implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerX : 0);
    }}
    public static final class WrapSprYCentre implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).centerY : 0);
    }}
    public static final class WrapSprXFlip implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipX ? -1f : 0) : 0);
    }}
    public static final class WrapSprYFlip implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).flipY ? -1f : 0) : 0);
    }}
    public static final class WrapSprVisible implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).visible ? -1f : 0) : 0);
    }}
    public static final class WrapSprAngle implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).angle : 0);
    }}
    public static final class WrapSprColour implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f, 0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            for (int i = 0; i < 4; i ++) {
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[i] = result[i];
            }
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 4, Arrays.asList(result)));
    }}
    public static final class WrapSprAlpha implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).color[3] : 0);
    }}
    public static final class WrapSprParallax implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).parallax ? -1f : 0) : 0);
    }}
    public static final class WrapSprSolid implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite) ?
                (TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite).solid ? -1f : 0) : 0);
    }}
    public static final class WrapSprLeft implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            GLSprite sprite = getSprite(TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionX + -sprite.centerX * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprRight implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            GLSprite sprite = getSprite(TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionX + (1 - sprite.centerX) * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprTop implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            GLSprite sprite = getSprite(TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionY + -sprite.centerY * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprBottom implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            GLSprite sprite = getSprite(TextBasicLib.boundSprite);
            vm.getReg().setRealVal( sprite.positionY + (1 - sprite.centerY) * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprXVel implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                getSprite(TextBasicLib.boundSprite).xd : 0);
    }}
    public static final class WrapSprYVel implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                getSprite(TextBasicLib.boundSprite).yd : 0);
    }}
    public static final class WrapSprVel implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        if (isSprite(TextBasicLib.boundSprite)) {
            result [0] = getSprite(TextBasicLib.boundSprite).xd;
            result [1] = getSprite(TextBasicLib.boundSprite).yd;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public static final class WrapSprSpin implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(isSprite(TextBasicLib.boundSprite) ?
                getSprite(TextBasicLib.boundSprite).angled : 0);
    }}
    public static final class WrapSprAnimSpeed implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                getSprite(TextBasicLib.boundSprite).angled : 0);
    }}

    public static final class WrapSprAnimLoop implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                (getSprite(TextBasicLib.boundSprite).animLoop ? -1f : 0) : 0);
    }}
    public static final class WrapSprAnimDone implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( isSprite(TextBasicLib.boundSprite) ?
                (getSprite(TextBasicLib.boundSprite).isAnimationDone() ? -1f : 0) : 0);
    }}
    public static final class WrapSprFrame_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( isSprite(index) ?
                getSprite(index).getFrame() : 0);
    }}
    public static final class WrapSprX_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).positionX : 0);
    }}
    public static final class WrapSprY_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).positionY : 0);
    }}
    public static final class WrapSprPos_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(index)) {
            result [0] = TextBasicLib.sprites.getValueAt(index).positionX;
            result [1] = TextBasicLib.sprites.getValueAt(index).positionY;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public static final class WrapSprZOrder_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).getZOrder() : 0);
    }}
    public static final class WrapSprXSize_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).sizeX : 0);
    }}
    public static final class WrapSprYSize_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).sizeY : 0);
    }}
    public static final class WrapSprScale_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(  TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).scale : 0);
    }}
    public static final class WrapSprXCentre_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).centerX : 0);
    }}
    public static final class WrapSprYCentre_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).centerY : 0);
    }}
    public static final class WrapSprXFlip_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).flipX ? -1f : 0) : 0);
    }}
    public static final class WrapSprYFlip_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).flipY ? -1f : 0) : 0);
    }}
    public static final class WrapSprVisible_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).visible ? -1f : 0) : 0);
    }}
    public static final class WrapSprAngle_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).angle : 0);
    }}
    public static final class WrapSprColour_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f, 0f, 0f};
        if (TextBasicLib.sprites.isIndexStored(index)) {
            for ( int i = 0; i < 4; i ++) {
                TextBasicLib.sprites.getValueAt(index).color[i] = result[i];
            }
        }

        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 4, Arrays.asList(result)));
    }}
    public static final class WrapSprAlpha_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(TextBasicLib.sprites.isIndexStored(index) ?
                TextBasicLib.sprites.getValueAt(index).color[3] : 0);
    }}
    public static final class WrapSprParallax_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).parallax ? -1f : 0) : 0);
    }}
    public static final class WrapSprSolid_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( TextBasicLib.sprites.isIndexStored(index) ?
                (TextBasicLib.sprites.getValueAt(index).solid ? -1f : 0) : 0);
    }}
    public static final class WrapSprLeft_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isSprite(index)) {
            GLSprite sprite = getSprite(index);
            vm.getReg().setRealVal( sprite.positionX + -sprite.centerX * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprRight_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isSprite(index)) {
            GLSprite sprite = getSprite(index);
            vm.getReg().setRealVal( sprite.positionX + (1 - sprite.centerX) * (sprite.sizeX * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprTop_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isSprite(index)) {
            GLSprite sprite = getSprite(index);
            vm.getReg().setRealVal( sprite.positionY + -sprite.centerY * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprBottom_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isSprite(index)) {
            GLSprite sprite = getSprite(index);
            vm.getReg().setRealVal( sprite.positionY + (1 - sprite.centerY) * (sprite.sizeY * sprite.scale));
        }
        else {
            vm.getReg().setRealVal( 0f);
        }
    }}
    public static final class WrapSprXVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(  isSprite(index) ?
                getSprite(index).xd : 0);
    }}
    public static final class WrapSprYVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( isSprite(index) ?
                getSprite(index).yd : 0);
    }}
    public static final class WrapSprVel_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        Float[] result = {0f, 0f};
        if (isSprite(index)) {
            result [0] = getSprite(index).xd;
            result [1] = getSprite(index).yd;
        }
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public static final class WrapSprSpin_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal(isSprite(index) ?
                getSprite(index).angled : 0);
    }}
    public static final class WrapSprAnimSpeed_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( isSprite(index) ?
                getSprite(index).angled : 0);
    }}
    public static final class WrapSprAnimLoop_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( isSprite(index) ?
                (getSprite(index).animLoop ? -1f : 0) : 0);
    }}
    public static final class WrapSprAnimDone_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setRealVal( isSprite(index) ?
                (getSprite(index).isAnimationDone() ? -1f : 0) : 0);
    }}
    public static final class WrapSprSetXVel implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).xd = vm.getRealParam(1);
        }
    }}
    public static final class WrapSprSetYVel implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).yd = vm.getRealParam(1);
        }
    }}
    public final class WrapSprSetVel implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            readVec(vm, 1);
            getSprite(TextBasicLib.boundSprite).xd = vec [0];
            getSprite(TextBasicLib.boundSprite).yd = vec [1];
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetVel_2 implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).xd = vm.getRealParam(2);
            getSprite(TextBasicLib.boundSprite).yd = vm.getRealParam(1);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetSpin implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).angled = vm.getRealParam(1);
        }
    }}
    public static final class WrapSprSetAnimSpeed implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).framed = vm.getRealParam(1);
        }
    }}
    public static final class WrapSprSetAnimLoop implements Function { public void run(TomVM vm) {
        if (isSprite(TextBasicLib.boundSprite)) {
            getSprite(TextBasicLib.boundSprite).animLoop = vm.getIntParam(1) != 0;
        }
    }}
    public static final class WrapAnimateSprites implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).animate();
        TextBasicLib.redraw();
    }}
    public static final class WrapAnimateSpriteFrames implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine)TextBasicLib.appText).animateFrames();
        TextBasicLib.redraw();
    }}
    public static final class WrapCopySprite implements Function { public void run(TomVM vm) {

        // CopySprite (dest, source)
        // Copies sprite "source" to sprite "dest"
        // Sprites must be the same type. I.e. you can't copy a standard sprite
        // to a tile map or vice versa.
        int index = vm.getIntParam(1);
        if (    isBasicSprite(TextBasicLib.boundSprite) && isBasicSprite(index)
                &&  getBasicSprite(TextBasicLib.boundSprite).getGLSpriteType() == getBasicSprite(index).getGLSpriteType()) {
            if (isSprite(TextBasicLib.boundSprite)) {
                getSprite(TextBasicLib.boundSprite).copy(getSprite(index));
            } else if (isTileMap(TextBasicLib.boundSprite)) {
                getTileMap(TextBasicLib.boundSprite).copy(getTileMap(index));
            }
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprType implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   isBasicSprite(TextBasicLib.boundSprite) ?
                getBasicSprite(TextBasicLib.boundSprite).getGLSpriteType().getType() : GLSpriteType.SPR_INVALID.getType());
    }}
    public static final class WrapSprXTiles implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   isTileMap(boundSprite) ?
                getTileMap(TextBasicLib.boundSprite).getTilesX() : 0);
    }}
    public static final class WrapSprYTiles implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(   isTileMap(boundSprite ) ?
                getTileMap(TextBasicLib.boundSprite).getTilesY() : 0);
    }}
    public static final class WrapSprType_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(   isBasicSprite(index) ?
                getBasicSprite(index).getGLSpriteType().getType() : GLSpriteType.SPR_INVALID.getType());
    }}
    public static final class WrapSprXTiles_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(   isTileMap(index) ?
                getTileMap(index).getTilesX() : 0);
    }}
    public static final class WrapSprYTiles_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        vm.getReg().setIntVal(isTileMap(index) ?
                getTileMap(index).getTilesY() : 0);
    }
    }
    public static final class WrapSprSetTiles implements Function { public void run(TomVM vm) {

        if (isTileMap(TextBasicLib.boundSprite)) {

            // Get tiles from array param
            Vector<Integer> tiles = new Vector<Integer>();
            IntBuffer xSize = BufferUtils.createIntBuffer(1), ySize = BufferUtils.createIntBuffer(1);
            TextBasicLib.getTiles(vm, 1, xSize, ySize, tiles);

            // Set
            getTileMap(TextBasicLib.boundSprite).setTiles(xSize.get(0), ySize.get(0), tiles);
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetXRepeat implements Function { public void run(TomVM vm) {
        if (isTileMap(TextBasicLib.boundSprite)) {
            getTileMap(TextBasicLib.boundSprite).repeatX = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprSetYRepeat implements Function { public void run(TomVM vm) {
        if (isTileMap(TextBasicLib.boundSprite)) {
            getTileMap(TextBasicLib.boundSprite).repeatY = vm.getIntParam(1) != 0;
            TextBasicLib.redraw();
        }
    }}
    public static final class WrapSprXRepeat implements Function { public void run(TomVM vm) {
        if (isTileMap(TextBasicLib.boundSprite)) {
            vm.getReg().setIntVal( getTileMap(TextBasicLib.boundSprite).repeatX ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public static final class WrapSprYRepeat implements Function { public void run(TomVM vm) {
        if (isTileMap(TextBasicLib.boundSprite)) {
            vm.getReg().setIntVal( getTileMap(TextBasicLib.boundSprite).repeatY ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public static final class WrapSprXRepeat_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isTileMap(index)) {
            vm.getReg().setIntVal( getTileMap(index).repeatX ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public static final class WrapSprYRepeat_2 implements Function { public void run(TomVM vm) {
        int index = vm.getIntParam(1);
        if (isTileMap(index)) {
            vm.getReg().setIntVal( getTileMap(index).repeatY ? -1 : 0);
        } else {
            vm.getReg().setIntVal( 0);
        }
    }}
    public static final class WrapSprCameraSetX implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).camX = vm.getRealParam(1);
        TextBasicLib.redraw();
    }}
    public static final class WrapSprCameraSetY implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).camY = vm.getRealParam(1);
        TextBasicLib.redraw();
    }}
    public static final class WrapSprCameraSetZ implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) appText).camZ = vm.getRealParam(1);
        TextBasicLib.redraw();
    }}
    public static final class WrapSprCameraSetPos implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).camX = vm.getRealParam(2);
        ((GLSpriteEngine) TextBasicLib.appText).camY = vm.getRealParam(1);
        TextBasicLib.redraw();
    }}
    public final class WrapSprCameraSetPos_2 implements Function { public void run(TomVM vm) {
        readVec(vm, 1);
        ((GLSpriteEngine) TextBasicLib.appText).camX = vec [0];
        ((GLSpriteEngine) TextBasicLib.appText).camY = vec [1];
        TextBasicLib.redraw();
    }}
    public static final class WrapSprCameraSetAngle implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).camAngle = vm.getRealParam(1);
        TextBasicLib.redraw();
    }}
    public static final class WrapSprCameraX implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).camX);
    }}
    public static final class WrapSprCameraY implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).camY);
    }}
    public static final class WrapSprCameraZ implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).camZ);
    }}
    public static final class WrapSprCameraPos implements Function { public void run(TomVM vm) {
        Float[] result = {0f, 0f};
        result [0] = ((GLSpriteEngine) TextBasicLib.appText).camX;
        result [1] = ((GLSpriteEngine) TextBasicLib.appText).camY;
        vm.getReg().setIntVal(Data.fillTempRealArray(vm.getData(), vm.getDataTypes(), 2, Arrays.asList(result)));
    }}
    public static final class WrapSprCameraAngle implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal(((GLSpriteEngine) TextBasicLib.appText).camAngle);
    }}
    public static final class WrapSprCameraFOV implements Function { public void run(TomVM vm) {
        vm.getReg().setRealVal( ((GLSpriteEngine) TextBasicLib.appText).getFOV());
    }}
    public static final class WrapSprCameraSetFOV implements Function { public void run(TomVM vm) {
        ((GLSpriteEngine) TextBasicLib.appText).setFOV(vm.getRealParam(1));
        TextBasicLib.redraw();
    }}
    public static final class WrapClearLine implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.ClearLine();
        TextBasicLib.redraw();
    }}
    public static final class WrapClearRegion implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.ClearRegion(vm.getIntParam(4), vm.getIntParam(3), vm.getIntParam(2), vm.getIntParam(1));
        TextBasicLib.redraw();
    }}

    public static final class WrapTextScroll implements Function { public void run(TomVM vm) {
        vm.getReg().setIntVal(TextBasicLib.appText.Scroll() ? 1 : 0);
    }}

    public static final class WrapSetTextScroll implements Function { public void run(TomVM vm) {
        TextBasicLib.appText.setScroll(vm.getIntParam(1) == 1);
    }}

    public static final class WrapClearSprites implements Function { public void run(TomVM vm) {
        TextBasicLib.sprites.clear();
        TextBasicLib.spriteCount = 0;
        TextBasicLib.redraw();
    }}

    public static final class WrapSprSetBlendFunc implements Function { public void run(TomVM vm) {
        if (TextBasicLib.sprites.isIndexStored(TextBasicLib.boundSprite)) {
            GLBasicSprite sprite = TextBasicLib.sprites.getValueAt(TextBasicLib.boundSprite);
            sprite.srcBlend = vm.getIntParam(2);
            sprite.dstBlend = vm.getIntParam(1);
        }
    }}


//DLLFUNC
public static final class WrapSpriteCount implements Function {
    public void run(TomVM vm) {
        vm.getReg().setIntVal( TextBasicLib.spriteCount);
    }
}




}
