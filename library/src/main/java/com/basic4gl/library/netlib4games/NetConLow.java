package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.udp.NetConLowUDP;

/**
 * Low level network connection.
 * All protocol specific logic is bundled into an object that descends from
 * this class. For example, the NetConLowUDP class contains all the UDP/IP
 * specific code.
 * <br/>
 * The NetConLow object is passed to the constructor of the NetConL2 (i.e.
 * the main connection object of NetLib4Games).
 * So a UDP/IP NetLib4Games connection would be created like so:\code
 * NetConL2 *connection = new NetConL2(new NetConLowUDP());
 * \endcode
 * Other NetConLow descendants can support other low level network protocols.
 * (At the time of writing, only UDP/IP is supported.)
 * <br/>
 * NetConLow objects are also created by NetListenLow::AcceptConnection(), 
 * when a connection request is accepted by the server.
 * <br/>
 * Note: Typically applications don't use the NetConLow (descendant) directly.
 * Instead they pass it to a NetConLx network connection, which adds extra
 * functionality (like connection lifetime, reliability, ordering, multi-packet
 * messages, timing etc).
 * The NetConLow simply supports unreliable packet sending and receiving to/from
 * a set target address.
 * <br/>
 * For multithreaded support, the must object allow that the ::Refresh() method
 * (and anything that ::Refresh() itself calls) will likely be called from a
 * different thread than the other methods, particularly ::Send() and ::Receive().
 * In some low level implementations, server connections may be driven by the
 * NetListenLow object (such as with the UDP implementation), which would also
 * be in a different thread if multithreading is used.
 * <br/>
 *   A recommended approach for threadsafeness is to either:
 * 	* Lock the entire object around all method calls,
 *   OR
 * 	* Lock access to:
 * 		1. The send queue
 * 		2. The receive queue
 * 		3. The underlying networking object(s) (eg. the socket for UDP implementation)
 */
public abstract class NetConLow extends HasErrorStateThreadSafe {

    public NetConLow() {
        super();
    }

    public void dispose() {

    }

    /**
     * @return true if connection is a client connection, or false if is a server connection.
     */
    public abstract boolean Client ();



    /**
     * Connect to address.
     *
     * Note: Not all connections are connected this way. They are also created
     * in response to external connection requests.
     * @param address address to connect to. Meaning depends on underlying communication protocol. Eg. for UDP (internet) this would be a DNS or IP address.
     * @return
     */
    public abstract  boolean Connect (String address);

    /**
     * Disconnect
     */
    public abstract  void Disconnect ();

    /**
     * @return true if still connected.
     */
    public abstract  boolean Connected ();

    /**
     * Maximum packet size.
     * Can only be called on connected connections
     * @return Maximum packet size.
     */
    public abstract int MaxPacketSize ();

    /**
     * Send a packet to the destination address.
     * flags not used in layer 1. Reserved for higher layers.
     * If not connected, should simply do nothing.
     * @param data
     * @param size
     */
    public abstract  void Send (byte[] data, int size);


    /**
     * @return True if a data is waiting to be received. If not connected, should simply return false.
     */
    public abstract  boolean DataPending ();


    /**
     * @return Size of pending data.
     */
    public abstract int PendingDataSize ();

    /**
     * Receive pending data.
     * Can only be called on connected connections when {@link DataPending()} = true
     * Data will be truncated if it doesn't fit in buffer.
     * @param data buffer to receive data
     * @param size In = Amount of room in data, Out = # of bytes read.
     */
    public void Receive (byte[] data, int size) {
        ReceivePart (data, 0, size);
        DonePendingData ();
    }

    /**
     * Receive part of a pending packet.
     * Can only be called on connected connections when {@link #PacketPending()} = true
     * The packet remains in the receive queue until {@link #NextPacket()} is called
     * @param data buffer to receive data
     * @param offset
     * @param size In = Amount of room in data, Out = # of bytes read.
     */
    public abstract int ReceivePart (byte[] data, int offset, int size);


    /**
     * Discard the top-of-queue pending packet.
     */
    public abstract  void DonePendingData ();

    /**
     * Event is signalled whenever data is received, or a significant state
     * change occurs, such as a network error or disconnect.
     * @return Event object.
     */
    public abstract  ThreadEvent Event ();

    /**
     * Expose the destination address of a connection to the application.
     * @return destination address
     */
    public abstract String Address();
}
