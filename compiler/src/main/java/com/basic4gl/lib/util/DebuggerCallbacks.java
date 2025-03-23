package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.runtime.InstructionPosition;
import com.basic4gl.runtime.TomVM;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
private final DebuggerTaskCallback mCallback;
private final DebuggerCallbackMessage mMessage;
private final TomVM mVM;
private final IVMDriver mDriver;

protected DebuggerCallbacks(
	DebuggerTaskCallback callback,
	DebuggerCallbackMessage message,
	TomVM vm,
	// TODO circular dependency with start???
	IVMDriver driver) {

	mCallback = callback;
	mMessage = message;
	mVM = vm;
	mDriver = driver;
}

/**
* Occurs before IVMDriver onPreExecute
*/
public abstract void onPreLoad();

/**
* Occurs after IVMDriver onPreExecute
*/
public abstract void onPostLoad();

public DebuggerCallbackMessage getMessage() {
	return mMessage;
}

public void setMessage(DebuggerCallbackMessage mMessage) {
	this.mMessage.setMessage(mMessage);
}

public void pause(String message) {
	InstructionPosition instructionPosition = null;
	if (mVM.isIPValid()) {
	instructionPosition = mVM.getIPInSourceCode();
	}
	VMStatus vmStatus = new VMStatus(mVM.isDone(), mVM.hasError(), mVM.getError());
	mMessage.setMessage(CallbackMessage.PAUSED, message, vmStatus);
	mMessage.setInstructionPosition(instructionPosition);

	mCallback.message(mMessage);
	try {
	// Wait for IDE to unpause the application
	while (mMessage.status == CallbackMessage.PAUSED) {
		// Go easy on the processor
		Thread.sleep(10);

		// Keep driver responsive while paused
		mDriver.handleEvents();
		//                mMessage.wait(100);

		// Check if program was stopped while paused
		if (Thread.currentThread().isInterrupted()
			|| mVM.hasError()
			|| mVM.isDone()
			|| mDriver.isClosing()) {
		break;
		}
	}
	} catch (InterruptedException e) { // Do nothing
	}
}

public void message() {
	mCallback.message(mMessage);
}

public void message(DebuggerCallbackMessage message) {
	mMessage.setMessage(message);
	mCallback.message(message);
}

public void message(CallbackMessage message) {
	DebuggerCallbackMessage debuggerCallbackMessage = null;

	if (message != null) {
	VMStatus vmStatus = new VMStatus(mVM.isDone(), mVM.hasError(), mVM.getError());
	debuggerCallbackMessage =
		new DebuggerCallbackMessage(message.getStatus(), message.getText(), vmStatus);
	}
	mMessage.setMessage(debuggerCallbackMessage);
	mCallback.message(debuggerCallbackMessage);
}
}
