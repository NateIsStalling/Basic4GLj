package com.basic4gl.lib.util;

import com.basic4gl.runtime.InstructionPosition;
import java.util.Objects;

/**
 * Created by Nate on 12/26/2022.
 */
public class DebuggerCallbackMessage {
	public static final int FAILED = -1;
	public static final int STOPPED = 0;
	public static final int WORKING = 1;
	public static final int SUCCESS = 2;
	public static final int PAUSED = 3;

	protected int status;
	protected String text;
	protected InstructionPosition instructionPosition;
	public VMStatus vmStatus;

	public DebuggerCallbackMessage() {
		this.status = STOPPED;
		this.text = "";
	}

	public DebuggerCallbackMessage(int status, String message, VMStatus vmStatus) {
		this.status = status;
		this.text = message;
		this.vmStatus = vmStatus;
	}

	public DebuggerCallbackMessage(CallbackMessage message, VMStatus vmStatus) {
		this.status = message.getStatus();
		this.text = message.getText();
		this.vmStatus = vmStatus;
	}

	public boolean setMessage(int status, String message, VMStatus vmStatus) {
		if (this.status == status && Objects.equals(this.text, message) && Objects.equals(this.vmStatus, vmStatus)) {
			// no change
			return false;
		}
		this.status = status;
		this.text = message;

		this.vmStatus = vmStatus;

		return true;
	}

	public boolean setMessage(DebuggerCallbackMessage message) {
		if (message == null) {
			// no change
			return false;
		}
		return setMessage(message.status, message.text, message.vmStatus);
	}

	public boolean setMessage(CallbackMessage message, VMStatus vmStatus) {
		if (message == null && vmStatus == null) {
			// no change
			return false;
		}
		return setMessage(message.status, message.text, vmStatus);
	}

	public boolean setStatus(int status) {
		boolean didChange = this.status != status;
		this.status = status;
		// setStatus is only called for continue() which does not return an error callback
		this.vmStatus = null;
		return didChange;
	}

	public int getStatus() {
		return status;
	}

	public String getText() {
		return text;
	}

	public void setInstructionPosition(InstructionPosition instructionPosition) {
		this.instructionPosition = instructionPosition;
	}

	public void setInstructionPosition(int row, int column) {
		this.instructionPosition = new InstructionPosition(row, column);
	}

	public InstructionPosition getInstructionPosition() {
		return instructionPosition;
	}

	public VMStatus getVMStatus() {
		return vmStatus;
	}
}
