package com.basic4gl.library.netlib4games;


/**
 * Abstract base class for an object that validates connection requests.
 * Because we don't interpret the contents of packets at this layer, we leave
 * a hook open to call a higher layer object to tell us whether we have
 * received a valid connection request (rather than just a stray packet).
 */
public abstract class NetConReqValidator {
    public NetConReqValidator() {
    }

    public abstract boolean IsConnectionRequest (NetSimplePacket packet, String[] requestStringBuffer);
}
