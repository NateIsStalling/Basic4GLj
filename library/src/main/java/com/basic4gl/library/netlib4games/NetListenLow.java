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
public abstract class NetListenLow extends HasErrorStateThreadSafe {

	public NetListenLow() {
		super();
	}

	public void dispose() {}

	/**
	 * True if a client connection is pending.
	 * That is, if a connection request has been received in response to a
	 * client {@see com.basic4gl.library.netlib4games.NetConL2#connect()} call.
	 *
	 * @return True if a client connection is pending.
	 */
	public abstract boolean isConnectionPending();

	/**
	 * Read the request string for the current pending connection.
	 * (i.e. the second parameter of the client's {@see com.basic4gl.library.netlib4games.NetConL2#connect()} call.)
	 * Call this ONLY if {@link #isConnectionPending()} returns true.
	 *
	 * @return the request string for the current pending connection
	 */
	public abstract String getRequestString();

	/**
	 * Accept and return pending connection (see {@link #isConnectionPending()}).
	 * Call this ONLY if {@link #isConnectionPending()} returns true.
	 * <p>
	 * eg:
	 * <pre>
	 * {@code
	 *  if (listener.isConnectionPending()) {
	 *      NetConL2 connection = new NetConL2(listener.acceptConnection());
	 *      //...
	 *  }
	 * }
	 * </pre>
	 *
	 * @return Returns a low level network protocol connection that can be passed to
	 * the constructor of a NetConL2 connection.
	 */
	public abstract NetConLow acceptConnection();

	/**
	 * Reject the pending connection (see {@link #isConnectionPending()})
	 * Call this ONLY if {@link #isConnectionPending()} returns true.
	 */
	public abstract void rejectConnection();

	/**
	 * Used internally
	 *
	 * @param packet
	 * @param requestStringBuffer
	 * @return
	 */
	protected boolean isConnectionRequest(NetSimplePacket packet, String[] requestStringBuffer) {
		requestStringBuffer[0] = "";
		return NetLowLevel.isConnectionRequest(packet, requestStringBuffer);
	}
}
