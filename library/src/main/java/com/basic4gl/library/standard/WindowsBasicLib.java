package com.basic4gl.library.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.FileOpener;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IFileAccess;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.util.Function;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nate on 11/19/2015.
 */
public class WindowsBasicLib implements FunctionLibrary, IFileAccess {

    // Globals
    static FileOpener files = null;
    static long performanceFreq;

    WindowsWavStore wavFiles;

    @Override
    public String name() {
        return "WindowsBasicLib";
    }

    @Override
    public String description() {
        return "Miscellaneous system functions";
    }

    @Override
    public void init(TomVM vm) {
        performanceFreq = System.nanoTime();
    }

    @Override
    public void init(TomBasicCompiler comp) {
        wavFiles = new WindowsWavStore();
        // Register resources
        comp.VM().addResources(wavFiles);
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
    public Map<String, FunctionSpecification[]> specs() {
        Map<String, FunctionSpecification[]> s = new HashMap<>();
        s.put("beep", new FunctionSpecification[]{new FunctionSpecification(WrapBeep.class, new ParamTypeList(), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put("sleep", new FunctionSpecification[]{new FunctionSpecification(WrapSleep.class, new ParamTypeList (BasicValType.VTP_INT), true, false, BasicValType.VTP_INT, true, false, null)});
        s.put("tickcount", new FunctionSpecification[]{new FunctionSpecification(WrapTickCount.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)});
        s.put("performancecounter", new FunctionSpecification[]{new FunctionSpecification(WrapPerformanceCounter.class, new ParamTypeList(), true, true, BasicValType.VTP_INT, false, false, null)});
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

    @Override
    public void init(FileOpener files) {
        WindowsBasicLib.files = files;
    }

    /**
     * Performance counter
     */
    long getPerformanceCounter() {
        if (performanceFreq == 0)       // No performance counter?
        {
            return System.currentTimeMillis();      // Degrade to tick counter
        } else {
            long counter;
            counter = System.nanoTime();
            return (counter / performanceFreq) * 1000;
        }
    }

    public static final class WrapBeep implements Function {
        public void run(TomVM vm) {
            // TODO without AWT!
//            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    public static final class WrapSleep implements Function {
        public void run(TomVM vm)          {
        int msec = vm.getIntParam(1);
        if (msec > 5000) {
            msec = 5000;
        }
        if (msec > 0) {
            try {
                Thread.sleep(msec);
            } catch (InterruptedException e) { // do nothing
            }
        }
    }
    }
    public static final class  WrapTickCount implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal((int)(System.currentTimeMillis() % Integer.MAX_VALUE));
        }
    }
    public final class WrapPerformanceCounter implements Function {
        public void run(TomVM vm) {
            vm.getReg().setIntVal((int)(getPerformanceCounter() % Integer.MAX_VALUE));
        }
    }
}
