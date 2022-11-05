package com.basic4gl.library.desktopgl;
import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.compiler.util.FuncSpec;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by Nate on 11/19/2015.
 */
public class JoystickBasicLib implements FunctionLibrary, IGLRenderer{
    static final int DEFAULT_JOY_THRESHHOLD = 0x4000;
    static final int JOY_BUTTONS = 4;
    // Global variables
    static GLWindow    appWindow;

    static FloatBuffer joyInfo;            // Current joystick state
    static ByteBuffer buttons;

    static int xPosIndex = 0;
    static int yPosIndex = 1;

    /*
    1. Left and right on left sticker.
    2. Up and down on left sticker.
    3. Left and right back triggers.
    4. Up and down on right sticker.
    5. Left and right on right sticker.
    */
    static boolean autoPoll = true;		// When true, joystick is automatically Polled before any function call
    static boolean initialised = false;
    static int threshHold = DEFAULT_JOY_THRESHHOLD;    // Number of units the joystick must be moved

    public void setWindow(GLWindow window){
        appWindow = window;
    }
    public void setTextGrid(GLTextGrid text){ //do nothing
    }
    @Override
    public String name() {
        return "JoystickBasicLib";
    }

    @Override
    public String description() {
        return "Joystick input handling";
    }

    @Override
    public void init(TomVM vm) {

    }

    @Override
    public void init(TomBasicCompiler comp) {
        // Init function
        comp.VM().AddInitFunc (new InitLibFunction());		// This function will be called before Basic4GL runs any program
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
    public Map<String, FuncSpec[]> specs() {
        // Register functions
        Map<String, FuncSpec[]> s = new HashMap<>();
        s.put("UpdateJoystick", new FuncSpec[]{ new FuncSpec( WrapUpdateJoystick.class, new ParamTypeList (),true, false, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Left", new FuncSpec[]{ new FuncSpec( WrapJoyLeft.class, new ParamTypeList(),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Right", new FuncSpec[]{ new FuncSpec( WrapJoyRight.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Up", new FuncSpec[]{ new FuncSpec( WrapJoyUp.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Down", new FuncSpec[]{ new FuncSpec( WrapJoyDown.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_0", new FuncSpec[]{ new FuncSpec( WrapJoyButton0.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_1", new FuncSpec[]{ new FuncSpec( WrapJoyButton1.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_2", new FuncSpec[]{ new FuncSpec( WrapJoyButton2.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_3", new FuncSpec[]{ new FuncSpec( WrapJoyButton3.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_X", new FuncSpec[]{ new FuncSpec( WrapJoyX.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Y", new FuncSpec[]{ new FuncSpec( WrapJoyY.class, new ParamTypeList (),true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Button", new FuncSpec[]{ new FuncSpec( WrapJoyButton.class, new ParamTypeList (ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
        s.put("Joy_Keys", new FuncSpec[]{ new FuncSpec( WrapJoyKeys.class, new ParamTypeList (),true, false, ValType.VTP_INT, false, false, null)});
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


    static void ClearJoyInfo () {
        if (joyInfo == null)
            joyInfo = BufferUtils.createFloatBuffer(2);
        if (buttons == null)
            buttons = BufferUtils.createByteBuffer(JOY_BUTTONS);
        if (joyInfo.capacity() < 2) {
            joyInfo = BufferUtils.createFloatBuffer(2);
        }
        if (buttons.capacity() < JOY_BUTTONS) {
            buttons = BufferUtils.createByteBuffer(JOY_BUTTONS);
        }
        joyInfo.rewind();
        for (int i = 0; i < joyInfo.capacity(); i++)
            joyInfo.put(0);
        buttons.rewind();
        for (int i = 0; i < buttons.capacity(); i++)
            buttons.put((byte)0);
        joyInfo.put(xPosIndex, 0x8000);
        joyInfo.put(yPosIndex, 0x8000);
    }

    static void PollJoystick () {

        // Read joystick position
        joyInfo = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
        buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_1);
        if (joyInfo == null || joyInfo.capacity() < 2 ||
                buttons == null || buttons.capacity() < JOY_BUTTONS)
            ClearJoyInfo();
    }

    static void AutoPoll () {
        if (autoPoll)
            PollJoystick ();
    }



    static int JoyX () {
        return (int) joyInfo.get(xPosIndex) - 0x8000;
    }
    static int JoyY () {
        return (int) joyInfo.get(yPosIndex) - 0x8000;
    }
    
    public final class InitLibFunction implements Function {
        public void run(TomVM vm) {
            // This function is called everytime a Basic4GL program starts.
            // Reset joystick related state.
            autoPoll = true;		// Automatically Poll joystick whenever it is accessed
            ClearJoyInfo ();
            threshHold = DEFAULT_JOY_THRESHHOLD;
        }
    }
    public final class WrapUpdateJoystick  implements Function {
        public void run(TomVM vm) {
            autoPoll = false;       // Explicitly polling the joystick disables automatic polling
            PollJoystick ();
        }
    }
    public final class WrapJoyLeft  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyX () <= -threshHold ? -1 : 0);
        }
    }
    public final class WrapJoyRight  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyX () >= threshHold ? -1 : 0);
        }
    }
    public final class WrapJoyUp  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyY () <= -threshHold ? -1 : 0);
        }
    }
    public final class WrapJoyDown  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyY () >= threshHold ? -1 : 0);
        }
    }
    public final class WrapJoyButton0  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( buttons != null && buttons.get(0) != 0 ? -1 : 0);
        }
    }
    public final class WrapJoyButton1  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( buttons != null && buttons.get(1) != 0 ? -1 : 0);
        }
    }
    public final class WrapJoyButton2  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( buttons != null && buttons.get(2) != 0 ? -1 : 0);
        }
    }
    public final class WrapJoyButton3  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( buttons != null && buttons.get(3) != 0 ? -1 : 0);
        }
    }
    public final class WrapJoyX  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyX ());
        }
    }
    public final class WrapJoyY  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            vm.Reg ().setIntVal ( JoyY ());
        }
    }
    public final class WrapJoyButton  implements Function {
        public void run(TomVM vm) {
            AutoPoll ();
            int index = vm.GetIntParam (1);
            if (index >= 0 && index < JOY_BUTTONS)
            vm.Reg ().setIntVal ( buttons != null && buttons.get(index) != 0 ? -1 : 0);
        }
    }
    public final class WrapJoyKeys  implements Function {
        public void run(TomVM vm) {
            AutoPoll();

            // Create fake keypresses based on the joystick state
            // Axis movement translates to cursor keys
            // Fire button 1 translates to space bar
            // Fire button 2 translates to control key (Ctrl)
            appWindow.fakeScanKey(GLFW_KEY_LEFT, 2, JoyX() < -threshHold);
            appWindow.fakeScanKey(GLFW_KEY_RIGHT, 2, JoyX() > threshHold);
            appWindow.fakeScanKey(GLFW_KEY_UP, 2, JoyY() < -threshHold);
            appWindow.fakeScanKey(GLFW_KEY_DOWN, 2, JoyY() > threshHold);
            appWindow.fakeScanKey(GLFW_KEY_SPACE, 2, buttons != null && buttons.get(0) != 0);
            //TODO Original source used VK_CONTROL
            appWindow.fakeScanKey(GLFW_KEY_LEFT_CONTROL, 2, buttons != null && buttons.get(1) != 0);
        }
    }
}
