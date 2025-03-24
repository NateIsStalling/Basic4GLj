package com.basic4gl.library.debug.commands;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.debug.protocol.callbacks.EvaluateWatchCallback;
import com.basic4gl.debug.protocol.commands.EvaluateWatchCommand;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.VMState;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Mutable;
import com.google.gson.Gson;
import javax.websocket.Session;

public class EvaluateWatchHandler {

    static final int GB_STEPS_UNTIL_REFRESH = 1000;

    static final String DEFAULT_VALUE = "???";

    private final TomBasicCompiler compiler;
    private final TomVM vm;
    private final IVMDriver vmDriver;
    private final Gson gson;

    public EvaluateWatchHandler(IVMDriver vmDriver, TomBasicCompiler compiler, TomVM vm, Gson gson) {
        this.vmDriver = vmDriver;
        this.compiler = compiler;
        this.vm = vm;
        this.gson = gson;
    }

    public void handle(String watch, String context, int requestId, Session session) {
        String result = evaluateWatch(watch, context);

        EvaluateWatchCallback callback = new EvaluateWatchCallback();
        callback.setRequestId(requestId);
        callback.setResult(result);

        String json = gson.toJson(callback);
        message(session, json);
    }

    private void message(Session session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String evaluateWatch(String watch, String context) {
        // TODO sync editor state; consider checking this in the editor
        //        if (mHost.isApplicationRunning()) {
        //            return DEFAULT_VALUE;
        //        }

        // Save virtual machine state
        VMState state = vm.getState();
        // TODO sync editor UI state
        //        mHost.pushApplicationState();
        try {

            // Setup compiler "in function" state to match the the current VM user
            // stack state.
            int currentFunction;
            if (vm.getCurrentUserFrame() < 0 || vm.getUserCallStack().lastElement().userFuncIndex < 0) {
                currentFunction = -1;
            } else {
                currentFunction = vm.getUserCallStack().get(vm.getCurrentUserFrame()).userFuncIndex;
            }

            boolean inFunction = currentFunction >= 0;

            // Compile watch expression
            // This also gives us the expression result type
            int codeStart = vm.getInstructionCount();
            ValType valType = new ValType();
            // TODO Possibly means to pass parameters by ref
            if (!compiler.tempCompileExpression(watch, valType, inFunction, currentFunction)) {
                return compiler.getError();
            }

            boolean canCallFunc = canCallFunc(context);
            if (!canCallFunc) {
                // Expressions aren't allowed to call functions for mouse-over hints.
                // Scan compiled code for OP_CALL_FUNC or OP_CALL_OPERATOR_FUNC
                for (int i = codeStart; i < vm.getInstructionCount(); i++) {
                    if (vm.getInstruction(i).opCode == OpCode.OP_CALL_FUNC
                            || vm.getInstruction(i).opCode == OpCode.OP_CALL_OPERATOR_FUNC
                            || vm.getInstruction(i).opCode == OpCode.OP_CALL_DLL
                            || vm.getInstruction(i).opCode == OpCode.OP_CREATE_USER_FRAME) {
                        return "Mouse hints can't call functions. Use watch instead.";
                    }
                }
            }

            // Run compiled code
            vm.gotoInstruction(codeStart);

            continueApplication();

            // Error occurred?
            if (vm.hasError()) {
                return vm.getError();
            }

            // Execution didn't finish?
            if (!vm.isDone()) {
                return DEFAULT_VALUE;
            }

            // Convert expression result to string
            return displayVariable(valType);
        } finally {
            vm.setState(state);
            // TODO sync editor UI state
            // mHost.restoreHostState();
            // TODO Add VM viewer
            // VMView().RefreshVMView();
        }
    }

    private void continueApplication() {
        // TODO sync editor UI state; report progress
        //        mMode = ApMode.AP_RUNNING;
        do {

            // Run the virtual machine for a certain number of steps
            // TODO Continue
            vm.continueVM(GB_STEPS_UNTIL_REFRESH);

            // Process windows messages (to keep application responsive)
            // Application.ProcessMessages ();
            // mGLWin.ProcessWindowsMessages();
            // TODO Implement pausing
            // if (mTarget.PausePressed ())           // Check for pause key. (This allows us to pause
            // when in full screen mode. Useful for debugging.)
            //    mVM.Pause ();
        } while ( // mMode == ApMode.AP_RUNNING
        !vm.hasError() && !vm.isDone() && !vm.isPaused() && !vmDriver.isClosing());
    }

    private String displayVariable(ValType valType) {
        if (valType.matchesType(BasicValType.VTP_STRING)) { // String is special case.
            return "\"" + vm.getRegString() + "\""; // Stored in string register.
        } else {
            String temp;
            try {
                Mutable<Integer> maxChars = new Mutable<>(TomVM.DATA_TO_STRING_MAX_CHARS);
                temp = vm.valToString(vm.getReg(), valType, maxChars);
            } catch (Exception ex) {

                // Floating point errors can be raised when converting floats to string
                /*switch (ex.getCause()) {
                case EXCEPTION_FLT_DENORMAL_OPERAND:
                case EXCEPTION_FLT_DIVIDE_BY_ZERO:
                case EXCEPTION_FLT_INEXACT_RESULT:
                case EXCEPTION_FLT_INVALID_OPERATION:
                case EXCEPTION_FLT_OVERFLOW:
                case EXCEPTION_FLT_STACK_CHECK:
                case EXCEPTION_FLT_UNDERFLOW:
                case EXCEPTION_INT_DIVIDE_BY_ZERO:
                case EXCEPTION_INT_OVERFLOW:
                	temp = "Floating point exception";
                default:*/
                temp = "An exception occurred";
                // }
            }
            return temp;
        }
    }

    private boolean canCallFunc(String context) {
        if (context == null) {
            return false;
        }

        switch (context) {
            case EvaluateWatchCommand.EVALUATE_CONTEXT_WATCH:
            case EvaluateWatchCommand.EVALUATE_CONTEXT_REPL:
            case EvaluateWatchCommand.EVALUATE_CONTEXT_CLIPBOARD:
                return true;
            case EvaluateWatchCommand.EVALUATE_CONTEXT_VARIABLES:
            case EvaluateWatchCommand.EVALUATE_CONTEXT_HOVER:
            default:
                return false;
        }
    }
}
