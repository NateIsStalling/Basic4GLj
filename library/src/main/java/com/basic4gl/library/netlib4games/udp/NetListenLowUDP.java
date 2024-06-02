package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.*;
import com.basic4gl.library.netlib4games.Thread;
import org.eclipse.jetty.util.InetAddressSet;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
//: public NetListenLow, public Threaded, public WinsockUser {

    // Listen socket
    DatagramSocket m_socket;
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
    ThreadLock m_connectionLock;

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
        while (it.hasNext()){
            NetPendConLowUDP i = it.next();
            if (( i).addr.getPort() == addr.getPort()
                    && ( i).addr.getHostString().equals(addr.getHostString())) {
                return true;
            }
        }
        // None found
        return false;
    }

    void OpenSocket() {
        assert (m_socket == null);

        // Porting Note - Original had additional handling to ensure winsock is running

        // Create socket
        try {
            m_socket = new DatagramSocket();
        } catch (SocketException e) {
            setError("Unable to create UDP socket");
            m_socket = null;
            return;
        }

        // Bind to address
        try {
            m_socket.bind(m_addr);
        } catch (SocketException e) {
            setError("Unable to bind to port " + (m_port));
            m_socket.close();
            m_socket = null;
            return;
        }

        // Get the maximum packet size
        try {
            m_maxPacketSize = Math.min(m_socket.getSendBufferSize(), m_socket.getReceiveBufferSize());
        } catch (SocketException e) {
            setError("Unable to determine maximum UDP packet size");
            m_socket.close();
            m_socket = null;
            return;
        }

        NetLog("Maximum UDP packet size: " + m_maxPacketSize);
    }

    void CloseSocket() {
        // Close the socket
        if (m_socket != null) {
            m_socket.close();
            m_socket = null;
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

        while (!m_socketServiceThread.Terminating()) {


            try (Selector selector = Selector.open()) {
                DatagramChannel channel = m_socket.getChannel();
                channel.register(selector, SelectionKey.OP_READ);

                selector.select(SOCKET_TIMEOUT_MILLIS);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

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
                    InetSocketAddress addr = null;
                    try {
                        addr = (InetSocketAddress) client.getRemoteAddress();

                        ByteBuffer buffer = ByteBuffer.wrap(m_buffer);
                        int size = client.read(buffer);

                        NetLog("Read UDP packet, " + size + " bytes");

                        // Bundle packet
                        NetSimplePacket packet = new NetSimplePacket(buffer.array(), size);
                        String[] requestStringBuffer = new String[1];

                        // Find target connection (by matching against packet address)
                        m_connectionLock.Lock();

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

                        m_connectionLock.Unlock();
                    } catch (IOException ex) {
                        NetLog("Error reading UDP packet: " + ex.getMessage());

                        m_connectionLock.Lock();
                        if (addr != null) {
                            NetConLowUDP connection = FindConnection(addr);
                            if (connection != null) {
                                connection.Disconnect();
                            }
                        }
                        m_connectionLock.Unlock();
                    }

                    iter.remove();
                }
            } catch (IOException ex) {
                NetLog("Error reading UDP channel: " + ex.getMessage());
            }
        }

        System.out.println("Hey listen!");
    }


    /// Construct listener.
    /// Note: If HasErrorState::Error () returns true, the object should
    /// be deleted immediately and not used.
    public NetListenLowUDP(int port) {
        m_port = port;
        m_buffer = null;
        m_socket = null;

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
        if (m_buffer != null)
            m_buffer = null;
    }

    /// Used by NetConLowUDP to notify listener of a deleted connection.
    public void FreeNotify(NetConLowUDP connection) {
        assert (connection != null);

        // Remove connection from list
        m_connectionLock.Lock();
        m_connections.remove(connection);
        m_connectionLock.Unlock();
    }

    public boolean ConnectionPending() {
        m_connectionLock.Lock();
        boolean result = !m_pending.isEmpty();
        m_connectionLock.Unlock();
        return result;
    }

    public String RequestString() {
        // TODO suspicious lock chain . assert (ConnectionPending ())
        m_connectionLock.Lock();
        assert (ConnectionPending());
        String result = m_pending.get(0).requestString;
        m_connectionLock.Unlock();
        return result;
    }

    public NetConLow AcceptConnection() {
        assert (!m_pending.isEmpty());

        NetLog("Accept UDP connection");

        m_connectionLock.Lock();

        // Extract connection request
        NetPendConLowUDP pendConnection = m_pending.get(0);
        m_pending.remove(0);

        // Create a connection, and add to list
        NetConLowUDP connection = new NetConLowUDP(
                m_socket,
                pendConnection.addr,
                m_maxPacketSize,
                this);
        m_connections.add(connection);

        // Queue first packet
        connection.QueuePendingPacket(new NetSimplePacket(pendConnection.packet.data, pendConnection.packet.size));

        m_connectionLock.Unlock();

        // Finished with connection request
        pendConnection.dispose();

        return connection;
    }

    public void RejectConnection() {
        assert (!m_pending.isEmpty());

        NetLog("Reject UDP connection");

        m_connectionLock.Lock();

        // Extract connection request
        NetPendConLowUDP pendConnection = m_pending.get(0);
        m_pending.remove(0);

        m_connectionLock.Unlock();

        // Delete request
        pendConnection.dispose();
    }
}
