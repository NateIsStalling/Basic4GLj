package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.ThreadEvent;

/**
 * Low level network connection.
 * All protocol specific logic is bundled into an object that descends from
 * this class. For example, the NetConLowUDP class contains all the UDP/IP
 * specific code.
 * <br/>
 * The NetConLow object is passed to the constructor of the NetConL2 (i.e.
 * the main connection object of NetLib4Games).
 * So a UDP/IP NetLib4Games connection would be created like so:
 * <pre>
 * {@code
 * NetConL2 connection = new NetConL2(new NetConLowUDP());
 * }
 * </pre>
 * Other NetConLow descendants can support other low level network protocols.
 * (At the time of writing, only UDP/IP is supported.)
 * <br/>
 * NetConLow objects are also created by NetListenLow.acceptConnection(),
 * when a connection request is accepted by the server.
 * <br/>
 * Note: Typically applications don't use the NetConLow (descendant) directly.
 * Instead they pass it to a NetConLx network connection, which adds extra
 * functionality (like connection lifetime, reliability, ordering, multi-packet
 * messages, timing etc).
 * The NetConLow simply supports unreliable packet sending and receiving to/from
 * a set target address.
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
    public abstract boolean isClient();


    /**
     * Connect to address.
     * <p>
     * Note: Not all connections are connected this way. They are also created
     * in response to external connection requests.
     * </p>
     *
     * @param address address to connect to. Meaning depends on underlying communication protocol. Eg. for UDP (internet) this would be a DNS or IP address.
     * @return
     */
    public abstract boolean connect(String address);

    /**
     * Disconnect
     */
    public abstract void disconnect();

    /**
     * @return true if still connected.
     */
    public abstract boolean isConnected();

    /**
     * Maximum packet size.
     * Can only be called on connected connections
     *
     * @return Maximum packet size.
     */
    public abstract int getMaxPacketSize();

    /**
     * Send a packet to the destination address.
     * flags not used in layer 1. Reserved for higher layers.
     * If not connected, should simply do nothing.
     *
     * @param data
     * @param size
     */
    public abstract void send(byte[] data, int size);


    /**
     * @return True if a data is waiting to be received. If not connected, should simply return false.
     */
    public abstract boolean isDataPending();


    /**
     * @return Size of pending data.
     */
    public abstract int getPendingDataSize();

    /**
     * Receive pending data.
     * Can only be called on connected connections when {@link #isDataPending()} = true
     * Data will be truncated if it doesn't fit in buffer.
     *
     * @param data buffer to receive data
     * @param size In = Amount of room in data, Out = # of bytes read.
     */
    public void receive(byte[] data, int size) {
        receivePart(data, 0, size);
        onDonePendingData();
    }

    /**
     * Receive part of a pending packet.
     * Can only be called on connected connections when {@link #isPacketPending()} = true
     * The packet remains in the receive queue until {@link #nextPacket()} is called
     *
     * @param data   buffer to receive data
     * @param offset
     * @param size   In = Amount of room in data, Out = # of bytes read.
     */
    public abstract int receivePart(byte[] data, int offset, int size);


    /**
     * Discard the top-of-queue pending packet.
     */
    public abstract void onDonePendingData();

    /**
     * Event is signalled whenever data is received, or a significant state
     * change occurs, such as a network error or disconnect.
     *
     * @return Event object.
     */
    public abstract ThreadEvent getEvent();

    /**
     * Expose the destination address of a connection to the application.
     *
     * @return destination address
     */
    public abstract String getAddress();
}
