package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.callbacks.EvaluateWatchCallback;
import com.basic4gl.debug.protocol.commands.EvaluateWatchCommand;
import com.basic4gl.language.core.extensions.Basic4GLCompiler;
import com.basic4gl.language.core.runtime.IVMDriver;
import com.basic4gl.language.core.runtime.VM;
import com.basic4gl.language.core.runtime.VMState;
import com.basic4gl.language.core.types.OpCode;
import com.basic4gl.language.core.types.ValType;
import com.google.gson.Gson;
import javax.websocket.Session;

public class EvaluateWatchHandler {

    static final int GB_STEPS_UNTIL_REFRESH = 1000;

    static final String DEFAULT_VALUE = "???";

    private final Basic4GLCompiler compiler;
    private final VM vm;
    private final IVMDriver vmDriver;
    private final Gson gson;

    public EvaluateWatchHandler(IVMDriver vmDriver, Basic4GLCompiler compiler, VM vm, Gson gson) {
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
                throw new RuntimeException("Failed to send evaluate-watch callback", e);
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
            int currentFunction = -1;
            int currentUserFrame = vm.getCurrentUserFrame();
            if (currentUserFrame >= 0
                    && currentUserFrame < vm.getUserCallStack().size()) {
                int userFuncIndex = vm.getUserCallStack().get(currentUserFrame).userFuncIndex;
                if (userFuncIndex >= 0) {
                    currentFunction = userFuncIndex;
                }
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
            return vm.getDisplayVariable(valType);
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
