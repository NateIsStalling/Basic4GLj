package com.basic4gl.library.netlib4games;

public class NetLowLevel {

    // globals
    // TODO inject singleton
    private static NetConReqValidator validator = null;
    private static ThreadLock validatorLock = new ThreadLock();

    public static void setValidator(NetConReqValidator validator) {

        NetLowLevel.validatorLock.Lock ();
        NetLowLevel.validator = validator;
        NetLowLevel.validatorLock.Unlock ();
    }

    public static void removeValidator(NetConReqValidator validator) {
        NetLowLevel.validatorLock.Lock ();
        if (NetLowLevel.validator == validator) {
            NetLowLevel.validator = null;
        }
        NetLowLevel.validatorLock.Unlock ();
    }

    public static boolean isConnectionRequest(NetSimplePacket packet, String[] requestStringBuffer) {
        validatorLock.Lock ();
        boolean result;
        if (validator != null) {
            result = validator.IsConnectionRequest (packet, requestStringBuffer);
        } else {
            result = false;
        }
        validatorLock.Unlock ();
        return result;
    }

}
