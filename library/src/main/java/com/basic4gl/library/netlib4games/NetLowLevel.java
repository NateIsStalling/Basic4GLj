package com.basic4gl.library.netlib4games;

public class NetLowLevel {

// globals
private static NetConReqValidator validator = null;
private static final Object validatorLock = new Object();

public static void setValidator(NetConReqValidator validator) {

	synchronized (validatorLock) {
	NetLowLevel.validator = validator;
	}
}

public static void removeValidator(NetConReqValidator validator) {
	synchronized (validatorLock) {
	if (NetLowLevel.validator == validator) {
		NetLowLevel.validator = null;
	}
	}
}

public static boolean isConnectionRequest(NetSimplePacket packet, String[] requestStringBuffer) {
	boolean result;
	synchronized (validatorLock) {
	if (validator != null) {
		result = validator.isConnectionRequest(packet, requestStringBuffer);
	} else {
		result = false;
	}
	}
	return result;
}
}
