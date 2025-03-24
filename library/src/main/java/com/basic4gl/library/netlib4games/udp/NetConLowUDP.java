package com.basic4gl.library.netlib4games.udp;

import static com.basic4gl.library.netlib4games.NetLogger.netLog;

import com.basic4gl.library.netlib4games.NetConLow;
import com.basic4gl.library.netlib4games.NetSimplePacket;
import com.basic4gl.library.netlib4games.internal.Assert;
import com.basic4gl.library.netlib4games.internal.Thread;
import com.basic4gl.library.netlib4games.internal.ThreadEvent;
import com.basic4gl.library.netlib4games.internal.ThreadUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

/**
 * UDP/IP (ie internet) implementation of NetConLow
 */
public class NetConLowUDP extends NetConLow implements Runnable {

    private static final long TIMEOUTSECS = 0;
    public static final int TIMEOUTUSECS = 500000; // microseconds NOT milliseconds
    public static final int SOCKET_TIMEOUT_MILLIS = TIMEOUTUSECS / 1000;

    /**
     * The socket operates in either of two modes.
     * It either owns the socket or shares the socket with a number of other connections.
     * <p>If the socket is owned:
     * <ul>
     * <li>this.socket is set to the socket owned by the connection</li>
     * <li>this.listen is null</li>
     * </ul>
     * <p>If the socket is shared:
     * <ul>
     * <li>this.socket is set to the shared socket stored in a NetListenLowUDP object</li>
     * <li>this.listen points to that net listener object socket and pass any packets to this connection to be queued.</li>
     * </ul>
     */
    protected DatagramChannel socket;

    protected InetSocketAddress address;
    protected boolean connected;
    private final boolean ownSocket;
    private NetListenLowUDP listen;
    private int maxPacketSize;
    private final List<NetSimplePacket> pendingPackets = new ArrayList<>();

    private ByteBuffer socketBuffer;

    //	Thread handling
    private final Thread socketServiceThread;

    /**
     * Signalled when connection is connected
     */
    private final ThreadEvent connectedEvent;

    /**
     * Signalled when data arrives
     */
    private final ThreadEvent dataEvent;

    private final Object serviceLock = new Object();
    private final Object inQueueLock = new Object();
    private final Object stateLock = new Object();

    /**
     * Alternative constructor for incoming connections.
     * Note: These connections don't store their own socket.
     * Instead, they share the socket of the NetListenLowUDP.
     *
     * @param sharedSocket
     * @param address
     * @param maxPacketSize
     * @param listen
     */
    NetConLowUDP(DatagramChannel sharedSocket, InetSocketAddress address, int maxPacketSize, NetListenLowUDP listen) {
        super();

        Assert.assertTrue(listen != null);

        socket = sharedSocket;
        this.address = address;
        this.maxPacketSize = maxPacketSize;
        this.listen = listen;
        connected = true;
        ownSocket = false;
        connectedEvent = new ThreadEvent("NetConLowUDP.connectedEvent#1", true);
        dataEvent = new ThreadEvent("NetConLowUDP.dataEvent#1");
        socketBuffer = null;

        netLog("Create UDP server connection");

        // Note: Don't start the socket service thread.
        // The socket is owned and serviced by the this.listen object, which
        // shares it between multiple connections.
        socketServiceThread = new Thread(NetConLowUDP.class.getName());
    }

    /**
     * Default constructor. Connection must then be connected with {@link #connect}
     */
    public NetConLowUDP() {
        super();
        connected = false;
        listen = null;
        ownSocket = true;
        connectedEvent = new ThreadEvent("NetConLowUDP.connectedEvent#2", false);
        dataEvent = new ThreadEvent("NetConLowUDP.dataEvent#2");
        socketBuffer = null;

        netLog("Create UDP client connection");

        // Start the socket service thread
        socketServiceThread = new Thread(NetConLowUDP.class.getName());
        socketServiceThread.start(this);
        // Thread can fail to start if OS refuses to create it
        if (socketServiceThread.isRunning()) {
            socketServiceThread.raisePriority();
        } else {
            setError("Unable to create thread");
        }
    }

    public void dispose() {

        // Disconnect
        disconnect();

        // Terminate thread
        socketServiceThread.terminate();

        // Unhook from listener
        unhookFromListen();

        // Clear queue
        clearQueue();

        // Delete temp buffer
        socketBuffer = null;

        netLog("Delete UDP connection");
    }

    void queuePendingPacket(NetSimplePacket packet) {
        synchronized (inQueueLock) {
            pendingPackets.add(packet);
            dataEvent.set();
        }
    }

    /**
     * Used by NetListenLowUDP to register that it has been freed
     *
     * @param connection
     */
    void freeNotify(NetListenLowUDP connection) {
        synchronized (serviceLock) {
            if (listen == connection) {

                // We deliberately clear the listen pointer BEFORE disconnecting
                // This prevents Disconnect() from trying to notify the listener (which is unneccesary and
                // would cause problems)
                listen = null;
                disconnect();
            }
        }
    }

    void unhookFromListen() {
        if (listen != null) {
            listen.freeNotify(this);
            listen = null;
        }
    }

    void clearQueue() {

        // Recycle any allocated pending packets
        Iterator<NetSimplePacket> it = pendingPackets.iterator();
        while (it.hasNext()) {
            NetSimplePacket i = it.next();
            i.dispose();
        }
        pendingPackets.clear();
    }

    @Override
    public void run() {
        // Always lock around all service thread activity
        // (except when waiting for events or for data on the socket.)
        // Once service is locked, we can abide by the following rule.
        //	1. State can be read without locking (must lock to read with stateLock)
        //	2. Access to in packet queue must be locked (with inQueueLock)
        //
        // Note: 1 is true because UI thread always locks state for reading with stateLock
        // and for writing with serviceLock.
        while (!socketServiceThread.isTerminating()) {

            boolean connected = false;
            synchronized (serviceLock) {
                connected = this.connected;
            }
            // If not connected, wait until we are
            if (!connected) {

                ThreadEvent[] events = new ThreadEvent[] {connectedEvent, socketServiceThread.getTerminateEvent()};

                ThreadUtils.waitForEvents(events, 2);
            } else {
                // Select and wait for data.
                // Note: We can't block forever, as we need to periodically check
                // whether the thread has been terminated.
                try {

                    synchronized (serviceLock) {
                        // Get temp buffer for data
                        if (socketBuffer == null) {
                            socketBuffer = ByteBuffer.allocate(maxPacketSize);
                        }

                        // Read in packet
                        try (Selector selector = Selector.open()) {
                            DatagramChannel channel = socket;
                            channel.register(selector, SelectionKey.OP_READ);

                            selector.select(SOCKET_TIMEOUT_MILLIS);
                            Set<SelectionKey> selectedKeys = selector.selectedKeys();
                            Iterator<SelectionKey> iter = selectedKeys.iterator();

                            // Found one?
                            while (iter.hasNext()) {
                                SelectionKey key = iter.next();

                                if (!key.isReadable()) {
                                    iter.remove();
                                    continue;
                                }

                                DatagramChannel client = (DatagramChannel) key.channel();
                                int size = Math.min(
                                        socket.socket().getSendBufferSize(),
                                        socket.socket().getReceiveBufferSize());

                                socketBuffer.clear();
                                InetSocketAddress remoteAddress = (InetSocketAddress) client.receive(socketBuffer);
                                socketBuffer.flip();
                                byte[] bytes = new byte[socketBuffer.remaining()];
                                socketBuffer.get(bytes);
                                size = bytes.length;

                                netLog("Read and queue UDP packet, " + size + " bytes");

                                // Add to end of queue
                                synchronized (inQueueLock) {
                                    pendingPackets.add(new NetSimplePacket(bytes, size));
                                    dataEvent.set();
                                }
                            }

                        } catch (SocketException ex) {
                            netLog("Error reading UDP packet: " + ex.getMessage());
                        }
                    }

                } catch (Exception ex) {
                    netLog("Error reading UDP channel: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Connect to internet address.
     * address format: ip:port
     * eg: 192.168.0.1:8000 or somedomain.com:9999
     *
     * @param address address to connect to. Meaning depends on underlying communication protocol. Eg. for UDP (internet) this would be a DNS or IP address.
     * @return
     */
    public boolean connect(String address) {
        boolean result = false;
        synchronized (serviceLock) {
            if (isClient() && !isConnected()) {

                netLog("Connect to: " + address);

                // Porting Note - Original had additional handling to ensure winsock is running

                // Address format:
                // IP:port, e.g. "192.168.0.3:8000" or "somedomain.com:9999"

                // Separate address from port
                String addressStr, portStr;

                int pos = address.indexOf(':'); // Look for colon
                if (pos != -1) {
                    addressStr = address.substring(0, pos);
                    portStr = address.substring(pos + 1);
                } else {
                    addressStr = address;
                    portStr = "8000";
                }

                // "localhost" is special
                if (addressStr.toLowerCase(Locale.ENGLISH).equals("localhost")) {
                    addressStr = "127.0.0.1";
                }

                // Create socket address; porting note - original source had additional handling around
                // resolving the hostname
                this.address = new InetSocketAddress(addressStr, Integer.parseInt(portStr));

                // Create socket
                try {
                    socket = DatagramChannel.open(StandardProtocolFamily.INET)
                            .setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    socket.configureBlocking(false);
                    socket.bind(new InetSocketAddress(0));
                    netLog(" to bind to port " + this.address.toString());
                } catch (IOException e) {
                    netLog("Unable to create UDP socket");
                    setError("Unable to create UDP socket");
                    return false;
                }

                // Get the maximum packet size
                try {
                    maxPacketSize = Math.min(
                            socket.socket().getSendBufferSize(), socket.socket().getReceiveBufferSize());
                } catch (SocketException e) {
                    netLog("Unable to determine maximum UDP packet size");
                    setError("Unable to determine maximum UDP packet size");
                    try {

                        socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    socket = null;
                    return false;
                }
                netLog("Maximum UDP packet size: " + maxPacketSize);

                // Note: At this point there in no real concept of connection,
                // handshaking, keepalives, timeouts etc.
                // Thus, if the address resolved, we treat it as a successful connection.
                connected = true;
                connectedEvent.set();
                dataEvent.set();
            }
            result = isConnected();
        }

        return result;
    }

    public void disconnect() {

        netLog("Disconnect UDP connection");

        synchronized (serviceLock) {
            if (connected) {
                // If we own the socket, then close it down
                if (ownSocket) {

                    netLog("Close socket");
                    try {

                        socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    socket = null;
                }

                // Unhook from listener.
                unhookFromListen();

                // Clear packet queue
                clearQueue();

                // Disconnect from listen
                connected = false;
                connectedEvent.reset();
                dataEvent.set();
            }
        }
    }

    public boolean isConnected() {
        boolean result = false;
        synchronized (stateLock) {
            result = connected;
        }
        return result;
    }

    public int getMaxPacketSize() {
        int result = 0;
        synchronized (stateLock) {
            result = maxPacketSize;
        }
        return result;
    }

    public void send(byte[] data, int size) {
        if (!connected) {
            return;
        }

        netLog("Send UDP packet, " + size + " bytes");

        Assert.assertTrue(data != null);
        Assert.assertTrue(size <= maxPacketSize);

        try {
            netLog(address != null ? address.getClass().getName() + ":" + address : "address is null");
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, size);
            socket.send(buffer, address);
        } catch (IOException e) {
            e.printStackTrace();
            setError("Error sending UDP packet, " + e.getMessage());
        }
    }

    public boolean isClient() {

        // Client connections own their own socket.
        // Server connections share the socket of the listener.

        // ownSocket set by constructor and does not change.
        // Thus thread locking is unnecessary.
        boolean result = false;
        synchronized (stateLock) {
            result = ownSocket;
        }
        return result;
    }

    public boolean isDataPending() {
        boolean result = false;
        synchronized (inQueueLock) {
            if (!connected) {
                return false;
            }

            // Porting Note: original source had the following comment here,
            // but the original class did not appear to implement ::Refresh as commented:
            // > Rather than performing the select in here,
            // > we perform the select in ::Refresh, add any received packets to
            // > the incoming queue and then return true if any packets are queued here.
            result = !pendingPackets.isEmpty();
        }
        return result;
    }

    public int getPendingDataSize() {
        int result;
        synchronized (inQueueLock) {
            result = isDataPending() ? pendingPackets.get(0).size : 0;
        }
        return result;
    }

    @Override
    public int receivePart(byte[] data, int offset, int size) {
        Assert.assertTrue(data != null);

        synchronized (inQueueLock) {
            if (isDataPending()) {

                // Find topmost queued packet
                NetSimplePacket packet = pendingPackets.get(0);

                // Number of bytes to copy
                int remainingBytes = offset >= packet.size ? 0 : packet.size - offset;
                if (remainingBytes < size) {
                    size = remainingBytes;
                }

                // Copy data
                if (size > 0) {
                    System.arraycopy(packet.data, offset, data, 0, size);
                }
            } else {
                size = 0;
            }
        }

        return size;
    }

    public void onDonePendingData() {
        synchronized (inQueueLock) {
            if (isDataPending()) {

                // Pop the topmost queued packet
                NetSimplePacket packet = pendingPackets.get(0);
                pendingPackets.remove(0);

                // Delete it
                packet.dispose();
            }
        }
    }

    public ThreadEvent getEvent() {
        return dataEvent;
    }

    public String getAddress() {
        // Porting note: original source return an IP address string
        // constructed from this.address.sin_addr.S_un.S_un_b.s_b1 ... etc
        return address.getHostString();
    }
}
