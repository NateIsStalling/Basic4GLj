package com.basic4gl.library.debug;

//
//  ========================================================================
//  Copyright (c) Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.debug.protocol.callbacks.VMStatus;
import com.basic4gl.debug.protocol.commands.*;
import com.basic4gl.debug.websocket.DebugClientSocket;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.debug.websocket.IDebugCommandListener;
import com.basic4gl.lib.util.CallbackMessage;
import com.basic4gl.lib.util.DebuggerCallbackMessage;
import com.basic4gl.lib.util.DebuggerTaskCallback;
import com.basic4gl.library.debug.commands.*;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.InstructionPos;
import com.basic4gl.runtime.TomVM;
import com.google.gson.Gson;
import org.eclipse.jetty.util.component.LifeCycle;

public class DebuggerCallbacksAdapter //extends DebuggerCallbacks
        implements DebuggerTaskCallback, IDebugCommandListener, IDebugCallbackListener
{

    private final DebuggerCallbackMessage mMessage;
    private final Debugger mDebugger;
    private final IVMDriver mVMDriver;
    private final TomBasicCompiler mComp;
    private final TomVM mVM;

    public DebuggerCallbacksAdapter(
            DebuggerCallbackMessage message,
            Debugger debugger,
            IVMDriver vmDriver,
            TomBasicCompiler comp,
            TomVM vm) {
        mMessage = message;
        mDebugger = debugger;
        mVMDriver = vmDriver;
        mComp = comp;
        mVM = vm;
    }

    public static void main(String[] args)
    {
//        DebuggerCallbacksAdapter adapter = new DebuggerCallbacksAdapter(
//                dnull, null);
////                null,
////                new CallMeMaybe(null),
////                null
////        );
//        adapter.connect();
    }
    WebSocketContainer container;
    Session session;
    CallMeMaybe callMeMaybe;

//    public DebuggerCallbacksAdapter(
//        TaskCallback callback,
//        CallbackMessage message,
//        IVMDriver driver) {
//        super(callback, new CallMeMaybe(message), driver);
//    }

    public void connect() {
        URI uri = URI.create("ws://localhost:6796/debug/");

        try
        {
            container = ContainerProvider.getWebSocketContainer();

            try
            {
                // Create client side endpoint
                DebugClientSocket clientEndpoint = new DebugClientSocket(this, this);

                // Attempt Connect
                session = container.connectToServer(clientEndpoint,uri);


//                callMeMaybe.session = session;
//
//                // Send a message
//                session.getBasicRemote().sendText("Hello");
//
//                // Send another message
//                session.getBasicRemote().sendText("Goodbye");
//
//                // Wait for remote to close
//                clientEndpoint.awaitClosure();
//
//                // Close session
//                session.close();
            }
            finally
            {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
//                LifeCycle.stop(container);
            }
        }
        catch (Throwable t)
        {
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
    public void message(DebuggerCallbackMessage message) {
        if (message == null) {
            return;
        }

        com.basic4gl.debug.protocol.callbacks.VMStatus status = null;
        if (message.getVMStatus() != null) {
            status = new VMStatus(
                message.getVMStatus().isDone(),
                message.getVMStatus().hasError(),
                message.getVMStatus().getError());
        }
        com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback = new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(message.getStatus(), message.getText(), status);
        InstructionPos instructionPos = message.getInstructionPosition();

        if (instructionPos != null) {
            callback.setSourcePosition(instructionPos.getSourceLine(), instructionPos.getSourceColumn());
        }

        String json = gson.toJson(callback);
        message(json);
    }

    @Override
    public void message(CallbackMessage message) {
        if (message == null) {
            return;
        }

        com.basic4gl.debug.protocol.callbacks.VMStatus status = null;
        com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback = new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(message.getStatus(), message.getText(), status);
        String json = gson.toJson(callback);
        message(json);
    }

    private void message(String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Gson gson = new Gson();

    @Override
    public void OnDebugCallbackReceived(com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback) {
        synchronized (mMessage) {
            com.basic4gl.lib.util.VMStatus vmStatus = null;
            if (callback.getVMStatus() != null) {
                vmStatus = new com.basic4gl.lib.util.VMStatus(
                    callback.getVMStatus().isDone(),
                    callback.getVMStatus().hasError(),
                    callback.getVMStatus().getError()
                );
            }
            mMessage.setMessage(callback.status, callback.text, vmStatus);
            InstructionPos instructionPos = null;
            if (callback.getSourcePosition() != null) {
                instructionPos = new InstructionPos(callback.getSourcePosition().line, callback.getSourcePosition().column);
            }
            mMessage.setInstructionPosition(instructionPos);
            mMessage.notify();
        }
    }

    private static class CallMeMaybe extends CallbackMessage {
        Gson gson = new Gson();
        public Session session;

        CallMeMaybe(CallbackMessage message) {
            if (message != null) {
                this.status = message.getStatus();
                this.text = message.getText();
            }
        }
    }

    @Override
    public void OnDebugCommandReceived(DebugCommand command) {
        System.out.println("Received command: " + command.getCommand());

        switch (command.getCommand()) {
            case ContinueCommand.COMMAND:
                ContinueHandler continueHandler = new ContinueHandler(mMessage);
                continueHandler.Continue();
                break;
            case EvaluateWatchCommand.COMMAND:
                EvaluateWatchCommand c = (EvaluateWatchCommand) command;
                EvaluateWatchHandler evaluateWatchHandler = new EvaluateWatchHandler(mVMDriver, mComp, mVM);
                evaluateWatchHandler.EvaluateWatch(c.watch, c.canCallFunc, session);
                break;
            case PauseCommand.COMMAND:
                PauseHandler pauseHandler = new PauseHandler(mVM);
                pauseHandler.pause();
                break;
            case ResumeCommand.COMMAND:
                continueHandler = new ContinueHandler(mMessage);
                continueHandler.Continue();
                break;
            case StepCommand.COMMAND:
                StepCommand stepCommand = (StepCommand) command;
                StepHandler handler = new StepHandler(mMessage, mVM);
                handler.DoStep(stepCommand.type);
                break;
            case StopCommand.COMMAND:
                StopHandler stopHandler = new StopHandler(mVMDriver);
                stopHandler.stop();
                break;
            case ToggleBreakpointCommand.COMMAND:
                ToggleBreakpointCommand toggleBreakpointCommand = (ToggleBreakpointCommand) command;
                ToggleBreakPointHandler toggleBreakPointHandler = new ToggleBreakPointHandler(mDebugger, mVM);
                toggleBreakPointHandler.toggleBreakPoint(toggleBreakpointCommand.filename, toggleBreakpointCommand.line);
                break;
            default:
                System.out.println("Ignored unsupported command: " + command.getCommand());
        }
    }

//
//    @Override
//    public void onPreLoad() {
//
//    }
//
//    @Override
//    public void onPostLoad() {
//
//    }
}
