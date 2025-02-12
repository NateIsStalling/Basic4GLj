package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.*;
import com.basic4gl.library.netlib4games.Thread;
import org.eclipse.jetty.util.InetAddressSet;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.basic4gl.library.netlib4games.NetLogger.NetLog;
import static com.basic4gl.library.netlib4games.udp.NetConLowUDP.*;

/**
 * UDP/IP (ie internet) implementation of NetListenLow
 */
public class NetListenLowUDP extends NetListenLow implements Runnable {

    // Listen socket
//    DatagramSocket m_socket;
    DatagramChannel m_channel;
    InetSocketAddress m_addr;
    int m_port;
    int m_maxPacketSize;

    // TODO verify this buffer is the right size type
    byte[] m_buffer;

    // Incoming connection requests
    List<NetPendConLowUDP> m_pending = new ArrayList<>();

    // All connections sharing m_socket
    List<NetConLowUDP> m_connections = new ArrayList<>();

    // Thread handling
    com.basic4gl.library.netlib4games.Thread m_socketServiceThread;
//    ThreadLock m_connectionLock = new ThreadLock();

    private final Object connectionLock = new Object();

    NetConLowUDP FindConnection(InetSocketAddress addr) {
        // Find connection whose address matches addr
        Iterator<NetConLowUDP> connectionIter = m_connections.iterator();
        while (connectionIter.hasNext()) {
            NetConLowUDP i = connectionIter.next();
            if ((i).m_addr.getPort() == addr.getPort()
                    && (i).m_addr.getHostString().equals(addr.getHostString())) {
                return i;
            }
        }
        // None found
        return null;
    }

    boolean IsPending(InetSocketAddress addr) {

        // Find a pending connection whose address matches addr
        Iterator<NetPendConLowUDP> it = m_pending.iterator();
        while (it.hasNext()) {
            NetPendConLowUDP i = it.next();
            if ((i).addr.getPort() == addr.getPort()
                    && (i).addr.getHostString().equals(addr.getHostString())) {
                return true;
            }
        }
        // None found
        return false;
    }

    void OpenSocket() {
        assert (m_channel == null);

        // Porting Note - Original had additional handling to ensure winsock is running

        // Create socket
        try {
            m_channel = DatagramChannel
                    .open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true);
//            m_channel.configureBlocking(false);
        } catch (IOException e) {
            setError("Unable to create UDP socket");
            m_channel = null;
            return;
        }

        // Bind to address
        try {
            m_addr = new InetSocketAddress(m_port);
            m_channel.socket().bind(m_addr);

            NetLog(" to bind to port " + m_addr.toString());
        } catch (IOException e) {
            e.printStackTrace();
            NetLog("Unable to bind to port " + (m_port));
            setError("Unable to bind to port " + (m_port));
            try {

                m_channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            m_channel = null;
            return;
        }

        // Get the maximum packet size
        try {
            m_maxPacketSize = Math.min(m_channel.socket().getSendBufferSize(), m_channel.socket().getReceiveBufferSize());
        } catch (SocketException e) {
            setError("Unable to determine maximum UDP packet size");
            try {

                m_channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            m_channel = null;
            return;
        }

        NetLog("Maximum UDP packet size: " + m_maxPacketSize);
    }

    void CloseSocket() {
        // Close the socket
        if (m_channel != null) {
            try {

                m_channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            m_channel = null;
        }

        // Close all connections using the socket
        Iterator<NetConLowUDP> it = m_connections.iterator();
        while (it.hasNext()) {
            NetConLowUDP connection = it.next();
            connection.FreeNotify(this);
        }
        m_connections.clear();
    }

    @Override
    public void run() {

        NetLog("run!");
        try  {
        while (!m_socketServiceThread.Terminating()) {
            try {
// Get temp buffer for data
                if (m_buffer == null) {
                    m_buffer = new byte[m_maxPacketSize];
                }

                NetLog("faster!");
                // Read in packet
                InetSocketAddress addr = null;
                try {
                    ByteBuffer buffer = ByteBuffer.wrap(m_buffer);
                    m_channel.socket().setSoTimeout((int)1000);

                    DatagramPacket in = new DatagramPacket(buffer.array(), m_maxPacketSize);
                    m_channel.socket().receive(in);
                    addr = (InetSocketAddress) in.getSocketAddress();

//                    buffer.flip();
                    byte[] bytes = in.getData();
                    int size = bytes.length;

                    NetLog("Read UDP packet, " + size + " bytes");

                    // Bundle packet
                    NetSimplePacket packet = new NetSimplePacket(bytes, size);
                    String[] requestStringBuffer = new String[1];

                    // Find target connection (by matching against packet address)
                    synchronized (connectionLock) {
                        NetConLowUDP connection = FindConnection(addr);
                        if (connection != null) {

                            NetLog("Queue packet in UDP net connection");

                            // Queue connection packet
                            connection.QueuePendingPacket(packet);
                        }
                        // Check whether packet is a connection request
                        else if (IsConnectionRequest(packet, requestStringBuffer)            // Is a connection request
                                && !IsPending(addr)) {                                    // And connection is not already pending
                            NetLog("Create pending UDP connection");

                            // If there is no existing pending connection then create one
                            m_pending.add(new NetPendConLowUDP(addr, requestStringBuffer[0], packet));
                        } else {

                            // Unknown packet. Ignore it
                            NetLog("Discard stray UDP packet");
                            packet.dispose();
                        }

                    }
                } catch (IOException ex) {
                    NetLog("Error reading UDP packet: " + ex.getMessage());

                    synchronized (connectionLock) {
                        if (addr != null) {
                            NetConLowUDP connection = FindConnection(addr);
                            if (connection != null) {
                                connection.Disconnect();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                NetLog("Error reading UDP channel: " + ex.getMessage());
            }
        }
        } catch (Exception ex) {
            NetLog("Error reading UDP channel: " + ex.getMessage());
        }

        System.out.println("Hey listen!");
    }

    private static void register(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    /// Construct listener.
    /// Note: If HasErrorState::Error () returns true, the object should
    /// be deleted immediately and not used.
    public NetListenLowUDP(int port) {
        m_port = port;
        m_buffer = null;
        m_channel = null;

        NetLog("Create UDP listener, port " + port);

        // Create socket
        OpenSocket();

        // Start service thread
        m_socketServiceThread = new Thread();
        m_socketServiceThread.Start(this);
        m_socketServiceThread.RaisePriority();
    }

    public void dispose() {
        NetLog("Delete UDP listener");

        // Close down the thread
        m_socketServiceThread.Terminate();

        // Close the socket
        CloseSocket();

        // Delete temp buffer
        if (m_buffer != null) {
            m_buffer = null;
        }
    }

    /// Used by NetConLowUDP to notify listener of a deleted connection.
    public void FreeNotify(NetConLowUDP connection) {
        assert (connection != null);

        // Remove connection from list
        synchronized (connectionLock) {
            m_connections.remove(connection);
        }
    }

    public boolean ConnectionPending() {
        boolean result;
        synchronized (connectionLock) {
            result = !m_pending.isEmpty();
        }
        return result;
    }

    public String RequestString() {
        // TODO suspicious lock chain . assert (ConnectionPending ())
        String result;
        synchronized (connectionLock) {
            assert (ConnectionPending());
            result = m_pending.get(0).requestString;
        }
        return result;
    }

    public NetConLow AcceptConnection() {
        assert (!m_pending.isEmpty());

        NetLog("Accept UDP connection");
        NetPendConLowUDP pendConnection;
        NetConLowUDP connection;
        synchronized (connectionLock) {

            // Extract connection request
            pendConnection = m_pending.get(0);
            m_pending.remove(0);

            // Create a connection, and add to list
            connection = new NetConLowUDP(
                    m_channel,
                    pendConnection.addr,
                    m_maxPacketSize,
                    this);
            m_connections.add(connection);

            // Queue first packet
            connection.QueuePendingPacket(new NetSimplePacket(pendConnection.packet.data, pendConnection.packet.size));

        }

        // Finished with connection request
        pendConnection.dispose();

        return connection;
    }

    public void RejectConnection() {
        assert (!m_pending.isEmpty());

        NetLog("Reject UDP connection");

        NetPendConLowUDP pendConnection;

        synchronized (connectionLock) {
            // Extract connection request
            pendConnection = m_pending.get(0);
            m_pending.remove(0);

        }

        // Delete request
        pendConnection.dispose();
    }
}
