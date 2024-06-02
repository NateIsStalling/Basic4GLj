package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.*;
import com.basic4gl.library.netlib4games.Thread;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

import static com.basic4gl.library.netlib4games.NetLogger.NetLog;

/**
 * UDP/IP (ie internet) implementation of NetConLow
 */
public class NetConLowUDP extends NetConLow implements Runnable {

    private static final long TIMEOUTSECS = 0;
    public static final long TIMEOUTUSECS = 500000; // microseconds NOT milliseconds
    public static final long SOCKET_TIMEOUT_MILLIS = 500; // microseconds NOT milliseconds
    // public NetConLow, public Threaded, public WinsockUser

    // The socket operates in either of two modes.
    // It either owns the socket or shares the socket with a number of other connections.
    // If the socket is owned:
    //		m_socket is set to the socket owned by the connection
    //		m_listen is null
    //		::Refresh() automatically polls the socket and queues any incoming packets
    // If the socket is shared:
    //		m_socket is set to the shared socket stored in a NetListenLowUDP object
    //		m_listen points to that net listener object
    //		::Refresh() does nothing. Instead NetListenLowUDP::Refresh() will poll the
    //		socket and pass any packets to this connection to be queued.

    DatagramChannel m_socket;
    InetSocketAddress m_addr;
    boolean m_connected;
    boolean m_ownSocket;
    NetListenLowUDP m_listen;
    int m_maxPacketSize;
    List<NetSimplePacket> m_pendingPackets = new ArrayList<>();

    // TODO is this the right buffer size type
    byte[] m_buffer;

    //	Thread handling
    com.basic4gl.library.netlib4games.Thread m_socketServiceThread;
    ThreadEvent m_connectedEvent,        // Signalled when connection is connected
            m_dataEvent;            // Signalled when data arrives
//    ThreadLock m_serviceLock,
//            m_inQueueLock;
//            m_stateLock;
    private final Object serviceLock = new Object();
    private final Object inQueueLock = new Object();
    private final Object stateLock = new Object();

    /// Alternative constructor for incoming connections.
    /// Note: These connections don't store their own socket. Instead they
    /// share the socket of the NetListenLowUDP.
    NetConLowUDP(
            DatagramChannel sharedSocket,
            InetSocketAddress addr,
            int maxPacketSize,
            NetListenLowUDP listen) {
        m_socket = (sharedSocket);
        m_addr = (addr);
        m_maxPacketSize = (maxPacketSize);
        m_listen = (listen);
        m_connected = (true);
        m_ownSocket = (false);
        m_connectedEvent = new ThreadEvent("NetConLowUDP.m_connectedEvent", true);
        m_dataEvent = new ThreadEvent("m_dataEvent");
        m_buffer = null;

        NetLog("Create UDP server connection");

        assert (m_listen != null);

        // Note: Don't start the socket service thread.
        // The socket is owned and serviced by the m_listen object, which
        // shares it between multiple connections.
    }

    void QueuePendingPacket(NetSimplePacket packet) {
        synchronized (inQueueLock) {
            m_pendingPackets.add(packet);
            m_dataEvent.set();
        }
    }

    /// Used by NetListenLowUDP to register that it has been freed
    void FreeNotify(NetListenLowUDP connection) {
        synchronized (serviceLock) {
            if (m_listen == connection) {

                // We deliberately clear the listen pointer BEFORE disconnecting
                // This prevents Disconnect() from trying to notify the listener (which is unneccesary and would cause problems)
                m_listen = null;
                Disconnect();
            }
        }
    }

    void UnhookFromListen() {
        if (m_listen != null) {
            m_listen.FreeNotify(this);
            m_listen = null;
        }
        NetLog("Unhook from UDP listen socket");
    }

    void ClearQueue() {

        // Recycle any allocated pending packets
        Iterator<NetSimplePacket> it = m_pendingPackets.iterator();
        while (it.hasNext()) {
            NetSimplePacket i = it.next();
            i.dispose();
        }
        m_pendingPackets.clear();
    }

    @Override
    public void run() {
        // Always lock around all service thread activity
        // (except when waiting for events or for data on the socket.)
        // Once service is locked, we can abide by the following rule.
        //	1. State can be read without locking (must lock to read with m_stateLock)
        //	2. Access to in packet queue must be locked (with m_inQueueLock)
        //
        // Note: 1 is true because UI thread always locks state for reading with m_stateLock
        // and for writing with m_serviceLock.
        while (!m_socketServiceThread.Terminating()) {

            boolean connected = false;
            synchronized (serviceLock) {
                connected = m_connected;
            }
            // If not connected, wait until we are
            if (!connected) {

                ThreadEvent[] events = new ThreadEvent[]{
                        m_connectedEvent,
                        m_socketServiceThread.TerminateEvent()
                };

//                foobar check this is correct
                NetLogger.NetLog("get stuck");
                ThreadUtils.waitForEvents(events, 2);
                NetLogger.NetLog("unstuck");
            } else {

                // Select and wait for data.
                // Note: We can't block forever, as we need to periodically check
                // whether the thread has been terminated.
                try (Selector selector = Selector.open()) {
                    DatagramChannel channel = m_socket;
                    channel.register(selector, SelectionKey.OP_READ);

                    selector.select(SOCKET_TIMEOUT_MILLIS);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();

                    synchronized (serviceLock) {
                        // Found one?
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();

                            if (!key.isAcceptable()) {
                                iter.remove();
                                continue;
                            }


                            SocketChannel client = (SocketChannel) key.channel();


                            // Get temp buffer for data
                            if (m_buffer == null) {
                                m_buffer = new byte[m_maxPacketSize];
                            }

                            // Read in packet
                            try {
                                int size = Math.min(client.socket().getSendBufferSize(), client.socket().getReceiveBufferSize());
                                NetLog("Read and queue UDP packet, " + size + " bytes");

                                // Add to end of queue
                                synchronized (inQueueLock) {
                                    m_pendingPackets.add(new NetSimplePacket(m_buffer, size));
                                    m_dataEvent.set();
                                }
                            } catch (SocketException ex) {
                                NetLog("Error reading UDP packet: " + ex.getMessage());
                            }
                        }
                    }

                } catch (IOException ex) {
                    NetLog("Error reading UDP channel: " + ex.getMessage());
                }

            }
        }
    }

    /// Default constructor. Connection must then be connected with ::Connect
    public NetConLowUDP() {
        m_connected = (false);
        m_listen = null;
        m_ownSocket = (true);
        m_connectedEvent = new ThreadEvent("m_connectedEvent2", false);
        m_dataEvent = new ThreadEvent("m_dataEvent2");
        m_buffer = null;

        NetLog("Create UDP client connection");

        // Start the socket service thread
        m_socketServiceThread = new Thread();
        m_socketServiceThread.Start(this);
        // Thread can fail to start if OS refuses to create it
        if (m_socketServiceThread.Running()) {
            m_socketServiceThread.RaisePriority();
        } else {
            setError("Unable to create thread");
        }
    }

    public void dispose() {

        // Disconnect
        Disconnect();

        // Terminate thread
        m_socketServiceThread.Terminate();

        // Unhook from listener
        UnhookFromListen();

        // Clear queue
        ClearQueue();

        // Delete temp buffer
        if (m_buffer != null) {
            m_buffer = null;
        }

        NetLog("Delete UDP connection");
    }

    /// Connect to internet address.
    /// address format:
    ///		ip:port
    /// eg:
    ///		192.168.0.1:8000
    /// or:
    ///		somedomain.com:9999
    public boolean Connect(String address) {
        boolean result = false;
        synchronized (serviceLock) {
            if (Client() && !Connected()) {

                NetLog("Connect to: " + address);

                // Porting Note - Original had additional handling to ensure winsock is running

                // Address format:
                // IP:port, e.g. "192.168.0.3:8000" or "somedomain.com:9999"

                // Separate address from port
                String addressStr, portStr;

                int pos = address.indexOf(':');            // Look for colon
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

                // Create socket address; porting note - original source had additional handling around resolving the hostname
                m_addr = new InetSocketAddress(addressStr, Integer.parseInt(portStr));


                // Create socket
                try {
                    m_socket = DatagramChannel
                            .open(StandardProtocolFamily.INET)
                            .setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    m_socket.configureBlocking(false);
                    m_socket.bind(m_addr);
                } catch (IOException e) {
                    setError("Unable to create UDP socket");
                    return false;
                }

                // Get the maximum packet size
                try {
                    m_maxPacketSize = Math.min(m_socket.socket().getSendBufferSize(), m_socket.socket().getReceiveBufferSize());
                } catch (SocketException e) {
                    setError("Unable to determine maximum UDP packet size");
                    try {

                        m_socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    m_socket = null;
                    return false;
                }
                NetLog("Maximum UDP packet size: " + (m_maxPacketSize));

                // Note: At this point there in no real concept of connection,
                // handshaking, keepalives, timeouts etc.
                // Thus if the address resolved, we treat it as a successful connection.
                m_connected = true;
                m_connectedEvent.set();
                m_dataEvent.set();
            }
            result = Connected();
        }

        return result;
    }

    public void Disconnect() {

        NetLog("Disconnect UDP connection");

        synchronized (serviceLock) {

            if (m_connected) {

                // If we own the socket, then close it down
                if (m_ownSocket) {

                    NetLog("Close socket");
                    try {

                        m_socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    m_socket = null;
                }

                // Unhook from listener.
                UnhookFromListen();

                // Clear packet queue
                ClearQueue();

                // Disconnect from listen
                m_connected = false;
                m_connectedEvent.reset();
                m_dataEvent.set();
            }

        }
    }

    public boolean Connected() {
        boolean result = false;
        synchronized (stateLock) {
            result = m_connected;
        }
        return result;
    }

    public int MaxPacketSize() {
        int result = 0;
        synchronized (stateLock) {
            result = m_maxPacketSize;
        }
        return result;
    }

    public void Send(byte[] data, int size) {
        if (!m_connected) {
            return;
        }

        NetLog("Send UDP packet, " + (size) + " bytes");

        assert (data != null);
        assert (size <= m_maxPacketSize);


        try {
            if (!m_socket.isConnected()) {
                m_socket.connect(m_addr);
            }
            try (Selector selector = Selector.open()) {
                DatagramChannel channel = m_socket;
                channel.register(selector, SelectionKey.OP_WRITE);

                selector.select(SOCKET_TIMEOUT_MILLIS);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                synchronized (serviceLock) {
                    // Found one?
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();

                        if (!key.isAcceptable()) {
                            iter.remove();
                            continue;
                        }


                        DatagramChannel client = (DatagramChannel) key.channel();
                        client.socket().send(new DatagramPacket(data, size));
                    }
                }
            }
                        NetLog("Sent!");
        } catch (IOException e) {
            e.printStackTrace();
            setError("Error sending UDP packet, " + e.getMessage());
        }
    }

    public boolean Client() {

        // Client connections own their own socket.
        // Server connections share the socket of the listener.

        // m_ownSocket set by constructor and does not change.
        // Thus thread locking is unnecessary.
        boolean result = false;
        synchronized (stateLock) {
            result = m_ownSocket;
        }
        return result;
    }

    public boolean DataPending() {
        boolean result = false;
        synchronized (inQueueLock) {
            if (!m_connected) {
                return false;
            }

            // Note: Rather than performing the select in here,
            // we perform the select in ::Refresh, add any received packets to
            // the incoming queue and then return true if any packets are queued here.
            result = !m_pendingPackets.isEmpty();
        }
        return result;
    }

    public int PendingDataSize() {
        int result;
        synchronized (inQueueLock) {
            result = DataPending() ? m_pendingPackets.get(0).size : 0;
        }
        return result;
    }

    @Override
    public int ReceivePart(byte[] data, int offset, int size) {
        assert (data != null);

        synchronized (inQueueLock) {
            if (DataPending()) {

                // Find topmost queued packet
                NetSimplePacket packet = m_pendingPackets.get(0);

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

    public void DonePendingData() {
        synchronized (inQueueLock) {
            if (DataPending()) {

                // Pop the topmost queued packet
                NetSimplePacket packet = m_pendingPackets.get(0);
                m_pendingPackets.remove(0);

                // Delete it
                packet.dispose();
            }
        }
    }

    public ThreadEvent Event() {
        return m_dataEvent;
    }

    public String Address() {
        // Porting note: original source return an IP address string
        // constructed from m_addr.sin_addr.S_un.S_un_b.s_b1 ... etc
        return m_addr.getHostString();
    }
}
