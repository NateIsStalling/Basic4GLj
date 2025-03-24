package com.basic4gl.library.netlib4games.udp;

import static com.basic4gl.library.netlib4games.NetLogger.netLog;
import static com.basic4gl.library.netlib4games.udp.NetConLowUDP.SOCKET_TIMEOUT_MILLIS;

import com.basic4gl.library.netlib4games.NetConLow;
import com.basic4gl.library.netlib4games.NetListenLow;
import com.basic4gl.library.netlib4games.NetSimplePacket;
import com.basic4gl.library.netlib4games.internal.Assert;
import com.basic4gl.library.netlib4games.internal.Thread;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * UDP/IP (ie internet) implementation of NetListenLow
 */
public class NetListenLowUDP extends NetListenLow implements Runnable {

    // Listen socket
    //    DatagramSocket socket;
    private DatagramChannel channel;
    private InetSocketAddress address;
    private final int port;
    private int maxPacketSize;

    private ByteBuffer socketBuffer;

    // Incoming connection requests
    private final List<NetPendConLowUDP> pending = new ArrayList<>();

    // All connections sharing socket
    private final List<NetConLowUDP> connections = new ArrayList<>();

    // Thread handling
    private final Thread socketServiceThread;
    private final Object connectionLock = new Object();

    /**
     * Construct listener.
     * Note: If HasErrorState.hasError() returns true, the object should be deleted immediately and not used.
     *
     * @param port
     */
    public NetListenLowUDP(int port) {
        super();
        this.port = port;
        socketBuffer = null;
        channel = null;

        netLog("Create UDP listener, port " + port);

        // Create socket
        openSocket();

        // Start service thread
        socketServiceThread = new Thread(NetListenLowUDP.class.getName());
        socketServiceThread.start(this);
        socketServiceThread.raisePriority();
    }

    public void dispose() {
        netLog("Delete UDP listener");

        // Close down the thread
        socketServiceThread.terminate();

        // Close the socket
        closeSocket();

        // Delete temp buffer
        socketBuffer = null;
    }

    NetConLowUDP findConnection(InetSocketAddress addr) {
        if (addr == null) {
            return null;
        }
        // Find connection whose address matches addr
        Iterator<NetConLowUDP> connectionIter = connections.iterator();
        while (connectionIter.hasNext()) {
            NetConLowUDP i = connectionIter.next();
            if ((i).address.getPort() == addr.getPort()
                    && (i).address.getHostString().equals(addr.getHostString())) {
                return i;
            }
        }
        // None found
        return null;
    }

    boolean isPending(InetSocketAddress addr) {

        // Find a pending connection whose address matches addr
        Iterator<NetPendConLowUDP> it = pending.iterator();
        while (it.hasNext()) {
            NetPendConLowUDP i = it.next();
            if ((i).getAddress().getPort() == addr.getPort()
                    && (i).getAddress().getHostString().equals(addr.getHostString())) {
                return true;
            }
        }
        // None found
        return false;
    }

    void openSocket() {
        Assert.assertTrue(channel == null);

        // Porting Note - Original had additional handling to ensure winsock is running

        // Create socket
        try {
            channel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.configureBlocking(false);
        } catch (IOException e) {
            setError("Unable to create UDP socket");
            channel = null;
            return;
        }

        // Bind to address
        try {
            address = new InetSocketAddress(port);
            channel.socket().bind(address);

            netLog(" to bind to port " + address.toString());
        } catch (IOException e) {
            e.printStackTrace();
            netLog("Unable to bind to port " + (port));
            setError("Unable to bind to port " + (port));
            try {

                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            channel = null;
            return;
        }

        // Get the maximum packet size
        try {
            maxPacketSize = Math.min(
                    channel.socket().getSendBufferSize(), channel.socket().getReceiveBufferSize());
        } catch (SocketException e) {
            setError("Unable to determine maximum UDP packet size");
            try {

                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            channel = null;
            return;
        }

        netLog("Maximum UDP packet size: " + maxPacketSize);
    }

    void closeSocket() {
        // Close the socket
        if (channel != null) {
            try {

                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            channel = null;
        }

        // Close all connections using the socket
        Iterator<NetConLowUDP> it = connections.iterator();
        while (it.hasNext()) {
            NetConLowUDP connection = it.next();
            connection.freeNotify(this);
        }
        connections.clear();
    }

    @Override
    public void run() {
        try {
            while (!socketServiceThread.isTerminating()) {
                try {
                    // Get temp buffer for data
                    if (socketBuffer == null) {
                        socketBuffer = ByteBuffer.allocate(maxPacketSize);
                    }

                    // Read in packet
                    InetSocketAddress clientAddress = null;
                    try (Selector selector = Selector.open()) {
                        DatagramChannel channel = this.channel;
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
                            socketBuffer.clear();
                            clientAddress = (InetSocketAddress) client.receive(socketBuffer);
                            socketBuffer.flip();

                            byte[] bytes = new byte[socketBuffer.remaining()];
                            socketBuffer.get(bytes);
                            int size = bytes.length;

                            netLog("Read UDP packet, " + size + " bytes");

                            // Bundle packet
                            NetSimplePacket packet = new NetSimplePacket(bytes, size);
                            String[] requestStringBuffer = new String[1];

                            // Find target connection (by matching against packet address)
                            synchronized (connectionLock) {
                                NetConLowUDP connection = findConnection(clientAddress);
                                if (connection != null) {

                                    netLog("Queue packet in UDP net connection");

                                    // Queue connection packet
                                    connection.queuePendingPacket(packet);
                                }
                                // Check whether packet is a connection request
                                else if (isConnectionRequest(packet, requestStringBuffer) // Is a connection request
                                        && !isPending(clientAddress)) { // And connection is not already pending
                                    netLog("Create pending UDP connection");

                                    // If there is no existing pending connection then create one
                                    pending.add(new NetPendConLowUDP(clientAddress, requestStringBuffer[0], packet));
                                } else {

                                    // Unknown packet. Ignore it
                                    netLog("Discard stray UDP packet");
                                    packet.dispose();
                                }
                            }
                        }
                    } catch (IOException ex) {
                        netLog("Error reading UDP packet: " + ex.getMessage());

                        synchronized (connectionLock) {
                            if (clientAddress != null) {
                                NetConLowUDP connection = findConnection(clientAddress);
                                if (connection != null) {
                                    connection.disconnect();
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    netLog("Error reading UDP channel: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            netLog("Error reading UDP channel: " + ex.getMessage());
        }
    }

    private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Used by NetConLowUDP to notify listener of a deleted connection.
     *
     * @param connection
     */
    public void freeNotify(NetConLowUDP connection) {
        Assert.assertTrue(connection != null);

        // Remove connection from list
        synchronized (connectionLock) {
            connections.remove(connection);
        }
    }

    public boolean isConnectionPending() {
        boolean result;
        synchronized (connectionLock) {
            result = !pending.isEmpty();
        }
        return result;
    }

    public String getRequestString() {
        // TODO review suspicious lock chain . assert (isConnectionPending ())
        String result;
        synchronized (connectionLock) {
            Assert.assertTrue(isConnectionPending());
            result = pending.get(0).getRequestString();
        }
        return result;
    }

    public NetConLow acceptConnection() {
        Assert.assertTrue(!pending.isEmpty());

        netLog("Accept UDP connection");
        NetPendConLowUDP pendConnection;
        NetConLowUDP connection;
        synchronized (connectionLock) {

            // Extract connection request
            pendConnection = pending.get(0);
            pending.remove(0);

            // Create a connection, and add to list
            connection = new NetConLowUDP(channel, pendConnection.getAddress(), maxPacketSize, this);
            connections.add(connection);

            // Queue first packet
            connection.queuePendingPacket(
                    new NetSimplePacket(pendConnection.getPacket().data, pendConnection.getPacket().size));
        }

        // Finished with connection request
        pendConnection.dispose();

        return connection;
    }

    public void rejectConnection() {
        Assert.assertTrue(!pending.isEmpty());

        netLog("Reject UDP connection");

        NetPendConLowUDP pendConnection;

        synchronized (connectionLock) {
            // Extract connection request
            pendConnection = pending.get(0);
            pending.remove(0);
        }

        // Delete request
        pendConnection.dispose();
    }
}
