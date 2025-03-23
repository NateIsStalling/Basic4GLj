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

public class DebuggerCommandAdapter
	implements DebuggerTaskCallback, IDebugCommandListener, IDebugCallbackListener {

private final DebuggerCallbackMessage mMessage;
private final Debugger mDebugger;
private final IVMDriver mVMDriver;
private final TomBasicCompiler mComp;
private final TomVM mVM;

private final Gson gson = new Gson();

private WebSocketContainer container;
private Session session;

public DebuggerCommandAdapter(
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

public void connect(URI debugSocketUri) {
	try {
	container = ContainerProvider.getWebSocketContainer();

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
	status =
		new VMStatus(
			message.getVMStatus().isDone(),
			message.getVMStatus().hasError(),
			message.getVMStatus().getError());
	}
	com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback =
		new com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage(
			message.getStatus(), message.getText(), status);
	InstructionPosition instructionPosition = message.getInstructionPosition();

	if (instructionPosition != null) {
	callback.setSourcePosition(
		instructionPosition.getSourceLine(), instructionPosition.getSourceColumn());
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
		session.getBasicRemote().sendText(json);
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
}

@Override
public void onDebugCallbackReceived(
	com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback) {
	synchronized (mMessage) {
	com.basic4gl.lib.util.VMStatus vmStatus = null;
	if (callback.getVMStatus() != null) {
		vmStatus =
			new com.basic4gl.lib.util.VMStatus(
				callback.getVMStatus().isDone(),
				callback.getVMStatus().hasError(),
				callback.getVMStatus().getError());
	}
	mMessage.setMessage(callback.getStatus(), callback.getText(), vmStatus);
	InstructionPosition instructionPosition = null;
	if (callback.getSourcePosition() != null) {
		instructionPosition =
			new InstructionPosition(
				callback.getSourcePosition().line, callback.getSourcePosition().column);
	}
	mMessage.setInstructionPosition(instructionPosition);
	mMessage.notify();
	}
}

@Override
public void onCallbackReceived(Callback callback) {}

@Override
public void onDebugCommandReceived(DebugCommand command) {
	System.out.println("Received command: " + command.getCommand());

	switch (command.getCommand()) {
	case ContinueCommand.COMMAND:
		ContinueHandler continueHandler = new ContinueHandler(mVM, mMessage);
		continueHandler.handle();
		break;
	case EvaluateWatchCommand.COMMAND:
		EvaluateWatchCommand c = (EvaluateWatchCommand) command;
		EvaluateWatchHandler evaluateWatchHandler =
			new EvaluateWatchHandler(mVMDriver, mComp, mVM, gson);
		evaluateWatchHandler.handle(c.watch, c.context, c.getId(), session);
		break;
	case PauseCommand.COMMAND:
		PauseHandler pauseHandler = new PauseHandler(mVM);
		pauseHandler.pause();
		break;
	case ResumeCommand.COMMAND:
		continueHandler = new ContinueHandler(mVM, mMessage);
		continueHandler.handle();
		break;
	case StackTraceCommand.COMMAND:
		StackTraceCommandHandler stackTraceCommandHandler = new StackTraceCommandHandler(mVM, gson);
		stackTraceCommandHandler.handle(session);
		break;
	case StepCommand.COMMAND:
		StepCommand stepCommand = (StepCommand) command;
		StepHandler handler = new StepHandler(mMessage, mVM);
		handler.doStep(stepCommand.stepType);
		break;
	case StopCommand.COMMAND:
		StopHandler stopHandler = new StopHandler(mVMDriver);
		stopHandler.stop();
		break;
	case DisconnectCommand.COMMAND:
		stop();
		break;
	case SetBreakpointsCommand.COMMAND:
		SetBreakpointsCommand setBreakpointsCommand = (SetBreakpointsCommand) command;
		SetBreakpointsHandler setBreakpointsHandler = new SetBreakpointsHandler(mDebugger, mVM);
		setBreakpointsHandler.handle(setBreakpointsCommand);
		break;
	case TerminateCommand.COMMAND:
		mVMDriver.terminate();
		break;
	case ToggleBreakpointCommand.COMMAND:
		ToggleBreakpointCommand toggleBreakpointCommand = (ToggleBreakpointCommand) command;
		ToggleBreakPointHandler toggleBreakPointHandler =
			new ToggleBreakPointHandler(mDebugger, mVM);
		toggleBreakPointHandler.toggleBreakPoint(
			toggleBreakpointCommand.filename, toggleBreakpointCommand.line);
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
