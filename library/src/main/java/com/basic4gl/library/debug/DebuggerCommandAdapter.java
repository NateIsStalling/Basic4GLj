package com.basic4gl.library.debug;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.commands.*;
import com.basic4gl.debug.protocol.types.VMStatus;
import com.basic4gl.debug.websocket.DebugClientSocket;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.debug.websocket.IDebugCommandListener;
import com.basic4gl.lib.util.CallbackMessage;
import com.basic4gl.lib.util.DebuggerCallbackMessage;
import com.basic4gl.lib.util.DebuggerTaskCallback;
import com.basic4gl.library.debug.commands.*;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.InstructionPosition;
import com.basic4gl.runtime.TomVM;
import com.google.gson.Gson;
import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.eclipse.jetty.util.component.LifeCycle;

public class DebuggerCommandAdapter implements DebuggerTaskCallback, IDebugCommandListener, IDebugCallbackListener {
    private static final int MAX_TEXT_MESSAGE_SIZE_BYTES = 1024 * 1024;
    private static final int MAX_CALLBACK_TEXT_CHARS = 16 * 1024;

    private final DebuggerCallbackMessage callbackMessage;
    private final Debugger debugger;
    private final IVMDriver vmDriver;
    private final TomBasicCompiler compiler;
    private final TomVM vm;

    private final Gson gson = new Gson();

    private WebSocketContainer container;
    private Session session;

    public DebuggerCommandAdapter(
            DebuggerCallbackMessage message, Debugger debugger, IVMDriver vmDriver, TomBasicCompiler comp, TomVM vm) {
        callbackMessage = message;
        this.debugger = debugger;
        this.vmDriver = vmDriver;
        compiler = comp;
        this.vm = vm;
    }

    public void connect(URI debugSocketUri) {
        try {
            container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_SIZE_BYTES);

            // Create client side endpoint
            DebugClientSocket clientEndpoint = new DebugClientSocket(this, this);

            // Attempt Connect
            session = container.connectToServer(clientEndpoint, debugSocketUri);

            onDebuggerConnected();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    public void stop() {
        try {
            System.out.println("stopping debugger");
            if (session != null) {
                session.close();
            }
            System.out.println("session closed");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            // Force lifecycle stop when done with container.
            // This is to free up threads and resources that the
            // JSR-356 container allocates. But unfortunately
            // the JSR-356 spec does not handle lifecycles (yet)
            LifeCycle.stop(container);
        }
    }

    @Override
    public void onDebuggerConnected() {
        // do nothing
    }

    @Override
    public void message(DebuggerCallbackMessage message) {
        if (message == null) {
            return;
        }

        VMStatus status = null;
        if (message.getVMStatus() != null) {
            status = new VMStatus(
                    message.getVMStatus().isDone(),
                    message.getVMStatus().hasError(),
                    abbreviate(message.getVMStatus().getError(), MAX_CALLBACK_TEXT_CHARS));
        }
        com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback =
                new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(
                        message.getStatus(), message.getText(), status);
        InstructionPosition instructionPosition = message.getInstructionPosition();

        if (instructionPosition != null) {
            callback.setSourcePosition(instructionPosition.getSourceLine(), instructionPosition.getSourceColumn());
        }

        String json = gson.toJson(callback);
        message(json);
    }

    @Override
    public void message(CallbackMessage message) {
        if (message == null) {
            return;
        }

        VMStatus status = null;
        com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback =
                new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(
                        message.getStatus(), message.getText(), status);
        String json = gson.toJson(callback);
        message(json);
    }

    @Override
    public void messageObject(Object message) {
        // not used in the command adapter;
        // TODO consider removing IDebugCallbackListener from this class; not really used
    }

    @Override
    public void onDebuggerDisconnected() {
        // do nothing
    }

    private void message(String json) {
        if (session != null && session.isOpen()) {
            try {
                if (json.length() > MAX_TEXT_MESSAGE_SIZE_BYTES) {
                    loggerTooLargeCallback(json.length());
                    json = gson.toJson(new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(
                            com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage.FAILED,
                            "Debug callback too large; request stack/variables/disassembly separately.",
                            null));
                }
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String abbreviate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "... [truncated]";
    }

    private void loggerTooLargeCallback(int length) {
        System.err.println("Dropping oversized callback payload (chars=" + length + ")");
    }

    @Override
    public void onDebugCallbackReceived(com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback) {
        synchronized (callbackMessage) {
            com.basic4gl.lib.util.VMStatus vmStatus = null;
            if (callback.getVMStatus() != null) {
                vmStatus = new com.basic4gl.lib.util.VMStatus(
                        callback.getVMStatus().isDone(),
                        callback.getVMStatus().hasError(),
                        callback.getVMStatus().getError());
            }
            callbackMessage.setMessage(callback.getStatus(), callback.getText(), vmStatus);
            InstructionPosition instructionPosition = null;
            if (callback.getSourcePosition() != null) {
                instructionPosition =
                        new InstructionPosition(callback.getSourcePosition().line, callback.getSourcePosition().column);
            }
            callbackMessage.setInstructionPosition(instructionPosition);
            callbackMessage.notify();
        }
    }

    @Override
    public void onCallbackReceived(Callback callback) {}

    @Override
    public void onDebugCommandReceived(DebugCommand command) {
        System.out.println("Received command: " + command.getCommand());

        switch (command.getCommand()) {
            case ContinueCommand.COMMAND:
                ContinueHandler continueHandler = new ContinueHandler(vm, callbackMessage);
                continueHandler.handle();
                break;
            case DisassembleCommand.COMMAND:
                DisassembleCommand disassembleCommand = (DisassembleCommand) command;
                DisassembleHandler disassembleHandler = new DisassembleHandler(debugger, compiler, vm, gson);
                disassembleHandler.handle(disassembleCommand, disassembleCommand.getId(), session);
                break;
            case EvaluateWatchCommand.COMMAND:
                EvaluateWatchCommand c = (EvaluateWatchCommand) command;
                EvaluateWatchHandler evaluateWatchHandler = new EvaluateWatchHandler(vmDriver, compiler, vm, gson);
                evaluateWatchHandler.handle(c.watch, c.context, c.getId(), session);
                break;
            case PauseCommand.COMMAND:
                PauseHandler pauseHandler = new PauseHandler(vm);
                pauseHandler.pause();
                break;
            case ResumeCommand.COMMAND:
                continueHandler = new ContinueHandler(vm, callbackMessage);
                continueHandler.handle();
                break;
            case StackTraceCommand.COMMAND:
                StackTraceCommandHandler stackTraceCommandHandler = new StackTraceCommandHandler(vm, gson);
                stackTraceCommandHandler.handle(session);
                break;
            case StepCommand.COMMAND:
                StepCommand stepCommand = (StepCommand) command;
                StepHandler handler = new StepHandler(callbackMessage, vm);
                handler.doStep(stepCommand.stepType);
                break;
            case StopCommand.COMMAND:
                StopHandler stopHandler = new StopHandler(vmDriver);
                stopHandler.stop();
                break;
            case DisconnectCommand.COMMAND:
                stop();
                break;
            case SetBreakpointsCommand.COMMAND:
                SetBreakpointsCommand setBreakpointsCommand = (SetBreakpointsCommand) command;
                SetBreakpointsHandler setBreakpointsHandler = new SetBreakpointsHandler(debugger, vm);
                setBreakpointsHandler.handle(setBreakpointsCommand);
                break;
            case TerminateCommand.COMMAND:
                vmDriver.terminate();
                break;
            case ToggleBreakpointCommand.COMMAND:
                ToggleBreakpointCommand toggleBreakpointCommand = (ToggleBreakpointCommand) command;
                ToggleBreakPointHandler toggleBreakPointHandler = new ToggleBreakPointHandler(debugger, vm);
                toggleBreakPointHandler.toggleBreakPoint(
                        toggleBreakpointCommand.filename, toggleBreakpointCommand.line);
                break;
            case VariablesCommand.COMMAND:
                VariablesCommand variablesCommand = (VariablesCommand) command;
                VariablesHandler variablesHandler = new VariablesHandler(vm, gson);
                variablesHandler.handle(variablesCommand, variablesCommand.getId(), session);
                break;
            default:
                System.out.println("Ignored unsupported command: " + command.getCommand());
        }
    }

    @Override
    public void onDisconnected() {
        // do nothing
    }
}
