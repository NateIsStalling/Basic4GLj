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

    private final TomBasicCompiler mComp;
    private final TomVM mVM;
    private final IVMDriver mVMDriver;
    private final Gson mGson;

    public EvaluateWatchHandler(
        IVMDriver vmDriver,
        TomBasicCompiler comp,
        TomVM vm,
        Gson gson) {
        mVMDriver = vmDriver;
        mComp = comp;
        mVM = vm;
        mGson = gson;
    }

    public void handle(String watch, String context, int requestId, Session session) {
        String result = EvaluateWatch(watch, context);

        EvaluateWatchCallback callback = new EvaluateWatchCallback();
        callback.setRequestId(requestId);
        callback.setResult(result);

        String json = mGson.toJson(callback);
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
    public String EvaluateWatch(String watch, String context) {
        //TODO sync editor state; consider checking this in the editor
        //        if (mHost.isApplicationRunning()) {
//            return DEFAULT_VALUE;
//        }

        // Save virtual machine state
        VMState state = mVM.getState();
        //TODO sync editor UI state
//        mHost.pushApplicationState();
        try {

            // Setup compiler "in function" state to match the the current VM user
            // stack state.
            int currentFunction;
            if (mVM.getCurrentUserFrame() < 0 ||
                    mVM.getUserCallStack().lastElement().userFuncIndex < 0) {
                currentFunction = -1;
            } else {
                currentFunction = mVM.getUserCallStack().get(mVM.getCurrentUserFrame()).userFuncIndex;
            }

            boolean inFunction = currentFunction >= 0;

            // Compile watch expression
            // This also gives us the expression result type
            int codeStart = mVM.getInstructionCount();
            ValType valType = new ValType();
            //TODO Possibly means to pass parameters by ref
            if (!mComp.TempCompileExpression(watch, valType, inFunction, currentFunction)) {
                return mComp.getError();
            }

            boolean canCallFunc = canCallFunc(context);
            if (!canCallFunc) {
                // Expressions aren't allowed to call functions for mouse-over hints.
                // Scan compiled code for OP_CALL_FUNC or OP_CALL_OPERATOR_FUNC
                for (int i = codeStart; i < mVM.getInstructionCount(); i++) {
                    if (mVM.getInstruction(i).opCode == OpCode.OP_CALL_FUNC
                            || mVM.getInstruction(i).opCode == OpCode.OP_CALL_OPERATOR_FUNC
                            || mVM.getInstruction(i).opCode == OpCode.OP_CALL_DLL
                            || mVM.getInstruction(i).opCode == OpCode.OP_CREATE_USER_FRAME) {
                        return "Mouse hints can't call functions. Use watch instead.";
                    }
                }
            }

            // Run compiled code
            mVM.gotoInstruction(codeStart);

            continueApplication();

            // Error occurred?
            if (mVM.hasError()) {
                return mVM.getError();
            }

            // Execution didn't finish?
            if (!mVM.isDone()) {
                return DEFAULT_VALUE;
            }

            // Convert expression result to string
            return DisplayVariable(valType);
        } finally {
            mVM.SetState(state);
            //TODO sync editor UI state
            //mHost.restoreHostState();
            // TODO Add VM viewer
            //VMView().RefreshVMView();
        }
    }

    private void continueApplication() {
        //TODO sync editor UI state; report progress
//        mMode = ApMode.AP_RUNNING;
        do {

            // Run the virtual machine for a certain number of steps
            //TODO Continue
            mVM.continueVM(GB_STEPS_UNTIL_REFRESH);

            // Process windows messages (to keep application responsive)
            //Application.ProcessMessages ();
            //mGLWin.ProcessWindowsMessages();
            //TODO Implement pausing
            //if (mTarget.PausePressed ())           // Check for pause key. (This allows us to pause when in full screen mode. Useful for debugging.)
            //    mVM.Pause ();
        } while (//mMode == ApMode.AP_RUNNING
                !mVM.hasError()
                && !mVM.isDone()
                && !mVM.isPaused()
                && !mVMDriver.isClosing());
    }

    private String DisplayVariable(ValType valType) {
        if (valType.matchesType(BasicValType.VTP_STRING)) {                              // String is special case.
            return "\"" + mVM.getRegString() + "\"";                 // Stored in string register.
        } else {
            String temp;
            try {
                Mutable<Integer> maxChars = new Mutable<Integer>(TomVM.DATA_TO_STRING_MAX_CHARS);
                temp = mVM.ValToString(mVM.getReg(), valType, maxChars);
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
                //}
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
