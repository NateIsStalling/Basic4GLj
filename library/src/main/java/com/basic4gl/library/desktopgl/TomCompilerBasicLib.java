package com.basic4gl.library.desktopgl;

/*  Created 17-Apr-06: Thomas Mulgrew (tmulgrew@slingshot.co.nz)
    Copyright (C) Thomas Mulgrew

    Functions for compiling and executing code at runtime.
*/
//#pragma hdrstop
//#include "TomCompilerBasicLib.h"
//#include "Basic4GLStandardObjects.h"
//#include <windows.h>

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.compiler.util.FuncSpec;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.compiler.util.IVMDriverAccess;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.Instruction;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.Value;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.*;

import static com.basic4gl.runtime.types.OpCode.*;
import static com.basic4gl.runtime.types.ValType.*;
import static com.basic4gl.runtime.util.Assert.assertTrue;

public class TomCompilerBasicLib implements FunctionLibrary, IFileAccess, IVMDriverAccess {
    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        Map<String, FuncSpec[]> s = new TreeMap<>();
        // Register functions
        s.put("compile", new FuncSpec[]{
                new FuncSpec(WrapCompile.class, new ParamTypeList(VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompile2.class, new ParamTypeList(VTP_STRING, VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompileList.class, new ParamTypeList(new ValType(VTP_STRING, (byte) 1, (byte) 1, true)), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompileList2.class, new ParamTypeList(new ValType(VTP_STRING, (byte) 1, (byte) 1, true), new ValType(VTP_STRING)), true, true, VTP_INT, true, false, null)
        });

        s.put("compilefile", new FuncSpec[]{
                new FuncSpec(WrapCompileFile.class, new ParamTypeList(VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompileFile2.class, new ParamTypeList(VTP_STRING, VTP_STRING), true, true, VTP_INT, true, false, null)
        });

        s.put("execute", new FuncSpec[]{new FuncSpec(WrapExecute.class, new ParamTypeList(VTP_INT), true, true, VTP_INT, true, false, null)});
        s.put("compilererror", new FuncSpec[]{new FuncSpec(WrapCompilerError.class, new ParamTypeList(), true, true, VTP_STRING, false, false, null)});
        s.put("compilererrorline", new FuncSpec[]{new FuncSpec(WrapCompilerErrorLine.class, new ParamTypeList(), true, true, VTP_INT, false, false, null)});
        s.put("compilererrorcol", new FuncSpec[]{new FuncSpec(WrapCompilerErrorCol.class, new ParamTypeList(), true, true, VTP_INT, false, false, null)});
        s.put("comp", new FuncSpec[]{
                new FuncSpec(WrapComp.class, new ParamTypeList(VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapComp2.class, new ParamTypeList(VTP_STRING, VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompList.class, new ParamTypeList(new ValType(VTP_STRING, (byte) 1, (byte) 1, true)), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompList2.class, new ParamTypeList(new ValType(VTP_STRING, (byte) 1, (byte) 1, true), new ValType(VTP_STRING)), true, true, VTP_INT, true, false, null)
        });
        s.put("compfile", new FuncSpec[]{
                new FuncSpec(WrapCompFile.class, new ParamTypeList(VTP_STRING), true, true, VTP_INT, true, false, null),
                new FuncSpec(WrapCompFile2.class, new ParamTypeList(VTP_STRING, VTP_STRING), true, true, VTP_INT, true, false, null)
        });
        return s;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public void init(FileOpener files) {
        TomCompilerBasicLib.files = files;
    }

    @Override
    public void init(IVMDriver driver) {
        host = driver;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public void init(TomVM vm) {

    }

    @Override
    public void init(TomBasicCompiler comp) {

        // Save pointer to compiler and window
        TomCompilerBasicLib.comp = comp;

        //TODO Work on extension interfaces
        // Hookup and register compiler plugin adapter
    /*comp.Plugins().RegisterInterface(
            static_cast < IB4GLCompiler * > (compilerAdapter),
            "IB4GLCompiler",
            IB4GLCOMPILER_MAJOR,
            IB4GLCOMPILER_MINOR,
            null);*/

        // Register initialisation function
        TomCompilerBasicLib.comp.VM().AddInitFunc(new InitFunc());
    }

    @Override
    public void cleanup() {

    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////
//  CompilerPluginAdapter
//
/// Exposes the compiler and virtual machine to plugins via the IB4GLCompiler
/// interface.
    public class CompilerPluginAdapter implements IB4GLCompiler {

        private int errorLine, errorCol;
        private String errorText;

        private void ClearError() {
            errorText = "";
            errorLine = 0;
            errorCol = 0;
        }


        // IB4GLCompiler interface
        public int Compile(String sourceText) {
            assertTrue(comp != null);
            TomVM vm = comp.VM();

            // Load source text into compiler
            comp.Parser().SourceCode().clear();
            comp.Parser().SourceCode().add(sourceText);

            // Compile it
            return DoNewCompile(vm);
        }

        public String GetErrorText() {

            return errorText;
        }

        public int GetErrorLine() {
            return errorLine;
        }

        public int GetErrorCol() {
            return errorCol;
        }

        public boolean Execute(int codeHandle) {
            TomVM vm = comp.VM();

            // Check code handle is valid
            if (codeHandle == 0 || !vm.IsCodeBlockValid(codeHandle)) {
                vm.FunctionError("Invalid code handle");
                return false;
            }

            // Save stack as if a sub is being called.
            // This is because we could be in a builtin/plugin function that is in the
            // middle of an expression, where temp data is saved to the stack.
            // Builtin/plugin functions don't normally protect the existing stack, so
            // there may be unprotected temp data that the callback code could trample.
            Mutable<Integer> stackTop = new Mutable<>(0), tempDataLock = new Mutable<>(0);
            vm.Data().SaveState(stackTop, tempDataLock);

            // Find code to execute
            // 2 op-codes earlier will be the callback hook.
            int offset = vm.GetCodeBlockOffset(codeHandle) - 2;

            // Execute code
            InternalExecute(vm, offset, true);

            // Check for error/end program
            if (vm.hasError() || vm.Done())
                return false;

            // Restore stack
            vm.Data().RestoreState(stackTop.get(), tempDataLock.get(), false);

            return true;
        }
    }

    ;

    // Globals
    static TomBasicCompiler comp = null;
    static IVMDriver host = null;
    static FileOpener files = null;
    CompilerPluginAdapter compilerAdapter;

    static String error = "";
    static int errorLine = 0, errorCol = 0;
    static Vector<Integer> runtimeRoutines = new Vector<>();

    ////////////////////////////////////////////////////////////////////////////////
//  Helper routines
    static void ClearError() {
        error = "";
        errorLine = 0;
        errorCol = 0;
    }

    ////////////////////////////////////////////////////////////////////////////////
//  Init function
    public class InitFunc implements Function {
        public void run(TomVM vm) {
            runtimeRoutines.clear();
            runtimeRoutines.add(0);
            ClearError();
        }
    }
////////////////////////////////////////////////////////////////////////////////
//  Runtime functions

    int DoNewCompile(TomVM vm) {
        // Comp routine internal implementation.
        // Note: "Comp" and "Exec" replace the deprecated "Compile" and "Execute" routines
        // (which are still maintained for backwards compatibility).
        // The main differences are that:
        //  * Comp adds an OP_RETURN to the end of the code, rather than an OP_END.
        //  * Exec is a built-in keyword that effectively GOSUBs to the code.
        com.basic4gl.compiler.TomBasicCompiler.RollbackPoint rollbackPoint = comp.GetRollbackPoint();
        comp.clearError();
        long saveIP = vm.IP();

        // Create hook for builtin/DLL function callbacks.
        // This consists of a GOSUB call to the code to be executed, followed by an
        // END CALLBACK op-code to trigger the return to the calling function.
        vm.AddInstruction(new Instruction(OP_CALL, VTP_INT, new Value((int) vm.InstructionCount() + 2)));    // Add 2 to call the code after these 2 op-codes
        vm.AddInstruction(new Instruction(OP_END_CALLBACK, VTP_INT, new Value()));

        int codeBlock = 0;
        if (comp.CompileOntoEnd()) {

            // Replace OP_END with OP_RETURN
            assertTrue(vm.InstructionCount() > 0);
            assertTrue(vm.Instruction(vm.InstructionCount() - 1).mOpCode == OP_END);
            vm.setInstruction(vm.InstructionCount() - 1, new Instruction(OP_RETURN, VTP_INT, new Value(), 0, 0));
            // No error
            ClearError();

            // Return code block index
            codeBlock = vm.CurrentCodeBlockIndex();
        } else {

            // Set error
            error = comp.getError();
            errorLine = (int) comp.Line();
            errorCol = (int) comp.Col();

            // Rollback compiler and virtual machine state
            comp.Rollback(rollbackPoint);
        }

        // Restore IP
        vm.GotoInstruction((int) saveIP);

        return codeBlock;
    }

    boolean CheckForFunctions(TomVM vm) {

        // Compile and execute cannot be used in programs that have functions/subs.
        // This is because errors during execute don't stop the program. The main
        // program can continue executing.
        // However functions/subs break this as the stack is too hard to clean up
        // correctly (with proper deallocation of evaluation stack and strings etc).

        // Therefore we simply disallow 'compile' and 'execute' for programs with
        // functions/subs.
        // This should be a non breaking change, as existing code that uses compile/
        // execute will be written in earlier Basic4GL versions, before functions
        // and subs were introduced.

        // The new commands 'comp' and 'exec' can be used instead.

        if (!vm.UserFunctionPrototypes().isEmpty()) {
            vm.FunctionError("'Compile' and 'Execute' cannot be used in programs that have functions/subs. Use 'Comp' and 'Exec' instead");
            return false;
        } else
            return true;
    }

    void DoOldCompile(TomVM vm) {

        // Not allowed in programs with functions/subs (see note in CheckForFunctions)
        if (!CheckForFunctions(vm))
            return;

        // Compiled code will be added to end of program
        long offset = vm.InstructionCount();

        // Attempt to compile text and append to end of existing program
        comp.clearError();
        int saveIP = vm.IP();      // Compiler can set IP back to 0, so we need to preserve it explicitly
        if (comp.CompileOntoEnd()) {

            // No error
            ClearError();

            // Register this code block and return handle
            vm.Reg().setIntVal(runtimeRoutines.size());
            runtimeRoutines.add((int) offset);
        } else {

            // Set error
            error = comp.getError();
            errorLine = (int) comp.Line();
            errorCol = (int) comp.Col();

            // Return 0
            vm.Reg().setIntVal(0);
        }

        // Restore IP
        vm.GotoInstruction(saveIP);
    }

    void DoCompile(TomVM vm, boolean useOldMethod) {
        if (useOldMethod)
            DoOldCompile(vm);
        else
            vm.Reg().setIntVal(DoNewCompile(vm));
    }

    void DoCompileText(TomVM vm, String text, boolean useOldMethod) {
        // Load it into compiler
        comp.Parser().SourceCode().clear();
        comp.Parser().SourceCode().add(text);

        // Compile it
        DoCompile(vm, useOldMethod);
    }

    void DoCompileList(TomVM vm, int index, boolean useOldMethod) {

        // Find array size
        int arraySize = vm.Data().Data().get(index).getIntVal();
        index += 2;

        // Load array into compiler
        comp.Parser().SourceCode().clear();
        for (int i = 0; i < arraySize; i++) {

            // Find string index
            int stringIndex = vm.Data().Data().get(index).getIntVal();

            // Find text
            String text = vm.GetString(stringIndex);

            // Add to parser text
            comp.Parser().SourceCode().add(text);

            // Next line
            index++;
        }

        // Compile it
        DoCompile(vm, useOldMethod);
    }

    void DoCompileFile(TomVM vm, String filename, boolean useOldMethod) {
        ClearError();

        // Attempt to open file
        IntBuffer length = IntBuffer.allocate(1);

        FileInputStream file = files.OpenRead(filename, false, length);

        if (file == null) {
            error = files.getError();
            vm.Reg().setIntVal(0);
        } else {

            // Read file into parser
            comp.Parser().SourceCode().clear();

            byte[] buffer = new byte[4096];
            int remaining, read;
            //Read file in chunks

            try {
                while ((remaining = file.available()) > 4096) {
                    file.read(buffer, 0, 4096);
                    comp.Parser().SourceCode().add(new String(buffer, "UTF-8"));
                }
                //Read remaining bytes
                if (remaining > 0) {
                    buffer = new byte[remaining];
                    file.read(buffer, 0, remaining);
                    comp.Parser().SourceCode().add(new String(buffer, "UTF-8"));
                }

                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Compile it
            DoCompile(vm, useOldMethod);
        }
    }


    void InternalExecute(TomVM vm, int offset) {
        InternalExecute(vm, offset, false);
    }

    void InternalExecute(TomVM vm, int offset, boolean isCallback) {

        // Move IP to offset
        int saveIP = vm.IP();          // (Save current IP)
        vm.GotoInstruction(offset);

        // Run code in virtual machine
        do {
            try {
                vm.Continue(1000);
            } catch (Exception e) {
                switch (e.getMessage()) {

                    // Skip mathematics errors (overflows, divide by 0 etc).
                    // This is quite important!, as some OpenGL drivers will trigger
                    // divide-by-zero and other conditions if geometry happens to
                    // be aligned in certain ways. The appropriate behaviour is to
                    // ignore these errors, and keep running, and NOT to stop the
                    // program!
                /*case EXCEPTION_FLT_DENORMAL_OPERAND:
                case EXCEPTION_FLT_DIVIDE_BY_ZERO:
                case EXCEPTION_FLT_INEXACT_RESULT:
                case EXCEPTION_FLT_INVALID_OPERATION:
                case EXCEPTION_FLT_OVERFLOW:
                case EXCEPTION_FLT_STACK_CHECK:
                case EXCEPTION_FLT_UNDERFLOW:
                case EXCEPTION_INT_DIVIDE_BY_ZERO:
                case EXCEPTION_INT_OVERFLOW:
                    vm.SkipInstruction();
                    break;
                */
                    // All other exceptions will stop the program.
                    default:
                        vm.MiscError("An exception occurred");
                }
            }
        } while (!vm.hasError()
                && !vm.Done()
                && (isCallback || !vm.Paused())            // Note: User cannot pause inside a callback
                && !(isCallback && vm.IsEndCallback())
                && host.handleEvents());

        // Restore IP
        vm.GotoInstruction(saveIP);
    }

    public final class WrapCompile implements Function {
        public void run(TomVM vm) {
            DoCompileText(vm, vm.GetStringParam(1), true);
        }
    }

    public final class WrapCompile2 implements Function {
        public void run(TomVM vm) {
            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile text
            DoCompileText(vm, vm.GetStringParam(2), true);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }


    public final class WrapCompileList implements Function {
        public void run(TomVM vm) {
            DoCompileList(vm, vm.GetIntParam(1), true);
        }
    }

    public final class WrapCompileList2 implements Function {
        public void run(TomVM vm) {

            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile list
            DoCompileList(vm, vm.GetIntParam(2), true);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }

    public final class WrapCompileFile implements Function {
        public void run(TomVM vm) {
            DoCompileFile(vm, vm.GetStringParam(1), true);
        }
    }

    public final class WrapCompileFile2 implements Function {
        public void run(TomVM vm) {

            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile list
            DoCompileFile(vm, vm.GetStringParam(2), true);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }

    public final class WrapExecute implements Function {
        public void run(TomVM vm) {

            // Not allowed in programs with functions/subs (see note in CheckForFunctions)
            if (!CheckForFunctions(vm))
                return;

            ClearError();
            int result = -1;

            // Get code handle
            int handle = vm.GetIntParam(1);

            // Validate it
            if (handle < 0 || handle >= runtimeRoutines.size()) {
                error = "Invalid handle";
                vm.Reg().setIntVal(0);
            } else if (handle > 0) {

                // Find code to execute
                int offset = runtimeRoutines.get(handle);

                // Run code in virtual machine
                InternalExecute(vm, offset);

                // Copy error text
                if (vm.hasError()) {

                    // Copy error to error variables
                    error = vm.getError();
                    Mutable<Integer> errorLineWrapper = new Mutable<>(errorLine), errorColWrapper = new Mutable<>(errorCol);
                    vm.GetIPInSourceCode(errorLineWrapper, errorColWrapper);
                    errorLine = errorLineWrapper.get();
                    errorCol = errorColWrapper.get();

                    // Clear error from virtual machine, so that parent program can keep
                    // on running.
                    vm.clearError();

                    result = 0;
                }
            }
            vm.Reg().setIntVal(result);
        }
    }

    public final class WrapCompilerError implements Function {
        public void run(TomVM vm) {
            vm.setRegString(error);
        }
    }

    public final class WrapCompilerErrorLine implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(errorLine);
        }
    }

    public final class WrapCompilerErrorCol implements Function {
        public void run(TomVM vm) {
            vm.Reg().setIntVal(errorCol);
        }
    }
////////////////////////////////////////////////////////////////////////////////
// New runtime compilation methods

    public final class WrapComp implements Function {
        public void run(TomVM vm) {
            DoCompileText(vm, vm.GetStringParam(1), false);
        }
    }

    public final class WrapComp2 implements Function {
        public void run(TomVM vm) {
            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile text
            DoCompileText(vm, vm.GetStringParam(2), false);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }

    public final class WrapCompList implements Function {
        public void run(TomVM vm) {
            DoCompileList(vm, vm.GetIntParam(1), false);
        }
    }

    public final class WrapCompList2 implements Function {
        public void run(TomVM vm) {

            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile list
            DoCompileList(vm, vm.GetIntParam(2), false);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }

    public final class WrapCompFile implements Function {
        public void run(TomVM vm) {
            DoCompileFile(vm, vm.GetStringParam(1), false);
        }
    }

    public final class WrapCompFile2 implements Function {
        public void run(TomVM vm) {

            // Set compiler symbol prefix
            String oldPrefix = comp.getSymbolPrefix();
            comp.setSymbolPrefix(comp.getSymbolPrefix() + vm.GetStringParam(1));

            // Compile list
            DoCompileFile(vm, vm.GetStringParam(2), false);

            // Restore symbol prefix
            comp.setSymbolPrefix(oldPrefix);
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Initialisation

    void InitTomCompilerBasicLib(
            TomBasicCompiler _comp,
            VMHostApplication _host,
            FileOpener _files) {

    }

}