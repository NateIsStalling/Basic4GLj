package com.basic4gl.library.netlib4games;

/**
 * Abstract low level network listener.
 * Listens on the network for connection requests (ConnectionPending() = true) 
 * and can accept (AcceptConnection()) them - to create a NetConL2 network 
 * connection - or reject them.
 * <br>
 * Use the appropriate descendant class for the underlying network protocol
 * you wish to use.
 * E.g. the UDP/IP implementation is NetListenLowUDP.
 * <br>
 * IMPORTANT:
 * For some NetListenLow implementations, the NetListenLow object itself handles
 * maintaining the underlying network state.
 * Therefore you should NOT destroy the NetListenLow object while you have 
 * NetConL2 connections that have been accepted by it.
 */
public abstract class NetListenLow extends HasErrorStateThreadSafe{



    public void dispose() {

    }

    /// True if a client connection is pending.
    /// That is, if a connection request has been received in response to a
    /// client NetConL2::Connect call.
    public abstract  boolean ConnectionPending () ;

    /// Read the request string for the current pending connection.
    /// (i.e. the second parameter of the client's NetConL2::Connect call.)
    /// Call this ONLY if ConnectionPending() returns true.
    public abstract String RequestString () ;

    /// Accept and return pending connection (see ::ConnectionPending).
    /// Call this ONLY if ConnectionPending() returns true.
    /// Returns a low level network protocol connection that can be passed to
    /// the constructor of a NetConL2 connection. E.g.:\code
    /// if (listener.ConnectionPending()) {
    ///		NetConL2 *connection = new NetConL2(listener.AcceptConnection());
    /// 	...
    ///	}
    /// \endcode
    public abstract NetConLow AcceptConnection ();

    /// Reject the pending connection (see ConnectionPending())
    /// Call this ONLY if ConnectionPending() returns true.
    public abstract  void RejectConnection ();

    /// [Used internally]
    protected boolean IsConnectionRequest (NetSimplePacket packet, String[] requestStringBuffer) {
        requestStringBuffer[0] = "";
        return NetLowLevel.isConnectionRequest(packet, requestStringBuffer);
    }
}
