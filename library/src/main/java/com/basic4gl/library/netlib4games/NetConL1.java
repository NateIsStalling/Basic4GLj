package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetL1Type.*;
import static com.basic4gl.library.netlib4games.NetLayer1.*;
import static com.basic4gl.library.netlib4games.NetLogger.netLog;
import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

import com.basic4gl.library.netlib4games.internal.Assert;
import com.basic4gl.library.netlib4games.internal.Thread;
import com.basic4gl.library.netlib4games.internal.ThreadEvent;
import com.basic4gl.library.netlib4games.internal.ThreadUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Layer 1 network connection implementation.
 * Note: This is an internal part of the network engine. Applications generally
 * don't construct NetConL1 objects directly. Instead, you should construct a
 * NetConL2 object to represent your connection.
 * See NetConL2, NetConLowUDP and NetListenLowUDP.
 * <p>
 * NetConL1 implements:
 * - Connection handling (incl. handshake sequence, timeouts and keepalives)
 * - Reliable packet sending.
 */
public class NetConL1 extends NetHasErrorStateThreadsafe implements Runnable {
    /**
     * Settings that cannot be changed once the object has been constructed.
     */
    private final NetSettingsStaticL1 settingsStatic;

    /**
     * Settings that can be changed at any time
     */
    private NetSettingsL1 settings = new NetSettingsL1();

    /**
     * Low level network connection object. E.g. NetConLowUDP for UDP/IP
     */
    private final NetConLow connection;

    private final List<NetInPacketL1> recvBuffer = new ArrayList<>();
    private final List<NetOutPacketL1> sendBuffer = new ArrayList<>();
    private boolean handShaking;
    private int sendIDReliable;
    private int sendIDUnreliable;

    /**
     * Used to coordinate keepalives
     */
    private long lastSent;

    /**
     * Used to detect timeouts
     */
    private long lastReceived;

    /**
     * Used to track which packets have already been received, and filter out duplicates.
     * Packets can be sent multiple times (e.g. if a confirmation packet is lost,
     * or deliberately duplicated if NetSettingsL1.dup > 1).
     */
    private final NetRevolvingBitBuffer reliableReceived, unreliableReceived;

    // Threading

    /**
     * Each connection has its own thread for processing network events.
     */
    private Thread processThread;

    /**
     * Used to lock any operation that could clash with the processing thread.
     */
    private final Object processLock = new Object();

    /**
     * Locks the incoming packet queue
     */
    private final Object inQueueLock = new Object();

    /**
     * Locks the outgoing packet queue
     */
    private final Object outQueueLock = new Object();

    /**
     * Used to lock any state data
     */
    private final Object stateLock = new Object();

    /**
     * Callback used to hook into the connection's processing thread.
     */
    private NetProcessThreadListener netProcessThreadListener;

    /**
     * Used by thread callback to request a wakeup, for special processing
     */
    private long wakeupTime;

    /**
     * Construct a network connection with supplied settings
     */
    public NetConL1(NetConLow connection, NetSettingsStaticL1 settings) {
        super();
        this.connection = connection;
        settingsStatic = settings;
        reliableReceived = new NetRevolvingBitBuffer(settingsStatic.getReliableBitBufSize(), 0);
        unreliableReceived = new NetRevolvingBitBuffer(settingsStatic.getUnreliableBitBufSize(), 0);
        sendIDReliable = 0;
        sendIDUnreliable = 0;
        wakeupTime = INFINITE;

        Assert.assertTrue(this.connection != null);

        netLog("Create L1 connection");

        // Init state
        handShaking = this.connection.isConnected();

        // Init timing
        lastSent = lastReceived = getTickCount();

        // Validate
        validate();
    }

    /**
     * Construct a network connection with default settings
     */
    public NetConL1(NetConLow connection) {
        super();
        this.connection = connection;
        settingsStatic = new NetSettingsStaticL1();
        reliableReceived = new NetRevolvingBitBuffer(settingsStatic.getReliableBitBufSize(), 0);
        unreliableReceived = new NetRevolvingBitBuffer(settingsStatic.getUnreliableBitBufSize(), 0);
        sendIDReliable = 0;
        sendIDUnreliable = 0;
        wakeupTime = INFINITE;

        Assert.assertTrue(this.connection != null);

        netLog("Create L1 connection");

        // Init state
        handShaking = this.connection.isConnected();

        // Init timing
        long tickCount = getTickCount();
        lastSent = tickCount;
        lastReceived = tickCount;

        // Validate
        validate();
    }

    /**
     * Destroy the connection.
     * Will automatically clean disconnect (if there is no error), or
     * simply drop the connection otherwise.
     */
    public void dispose() {
        disconnect(!hasError());

        // Close the processing thread
        processThread.terminate();

        // Clear send/receive buffers
        Iterator<NetInPacketL1> receivePacket = recvBuffer.iterator();
        while (receivePacket.hasNext()) {
            NetInPacketL1 packet = receivePacket.next();
            packet.dispose();
        }
        recvBuffer.clear();

        Iterator<NetOutPacketL1> sendPacket = sendBuffer.iterator();
        while (sendPacket.hasNext()) {
            NetOutPacketL1 packet = sendPacket.next();
            packet.dispose();
        }
        sendBuffer.clear();

        if (connection != null) {
            connection.dispose();
        }

        netLog("Delete L1 connection");
    }

    void validate() {

        // Validate the connection and ensure that it is useable.
        // Called by constructors.
        // Calling code should check error state to see whether connection is useable
        if (connection.hasError()) {
            setError(connection.getError());
        }

        // Check max packet size, but only if connected. The UDP protocol at least
        // must be connected before it can correctly return the maximum packet size
        else if (isConnected() && connection.getMaxPacketSize() <= NetPacketHeaderL1.SIZE) {
            setError("Underlying network protocol packet size is too small!");
        } else {
            clearError();
        }

        // TODO!: Validate this.settingsStatic

        // Disconnect if error
        if (hasError() && isConnected()) {
            disconnect(false);
        }
    }

    void buildHeader(NetPacketHeaderL1 header, boolean reliable, NetL1Type type, int id) {

        // Encode layer 1 packet header
        header.setId(id);
        header.setFlags((byte) ((type.getType() & NETL1_TYPEMASK) | (reliable ? NETL1_RELIABLE : 0)));
    }

    void buildAndSend(boolean reliable, NetL1Type type, int id, long tickCount) {
        buildAndSend(reliable, type, id, tickCount, true);
    }

    void buildAndSend(boolean reliable, NetL1Type type, int id, long tickCount, boolean dups) {
        NetPacketHeaderL1 header = new NetPacketHeaderL1();
        buildHeader(header, reliable, type, id);
        long dupFactor = dups ? settings.dup : 1L;
        for (int i = 0; i < dupFactor; i++) {
            connection.send(header.getBuffer(), NetPacketHeaderL1.SIZE);
        }
        synchronized (stateLock) {
            lastSent = tickCount;
        }
    }

    boolean processRecvPacket(long tickCount) {

        // Process the next inbound packet.
        if (!connection.isDataPending()) {
            return false;
        }

        // Record that data was received.
        lastReceived = tickCount;

        // Get packet size
        int size = connection.getPendingDataSize();
        // Note: If a packet is received that is smaller than our layer 1 header,
        // then it is erroneous. All we can do is ignore it and continue.

        netLog("Receive L1 packet, " + size + " bytes");

        if (size >= NetPacketHeaderL1.SIZE) {

            // Read packet header
            NetPacketHeaderL1 header = new NetPacketHeaderL1();
            int recvSize = NetPacketHeaderL1.SIZE;
            recvSize = connection.receivePart(header.getBuffer(), 0, recvSize);

            netLog("Incoming L1 packet. " + getDescription(header));

            // Decode header
            byte flags = header.getFlags();
            boolean reliable = (flags & NETL1_RELIABLE) != 0;
            boolean resent = (flags & NETL1_RESENT) != 0;
            NetL1Type type = NetLayer1.getNetLayerType(flags);
            int id = header.getId();

            // Process packet
            switch (type) {
                case l1User:
                    {
                        NetRevolvingBitBuffer buffer = reliable ? reliableReceived : unreliableReceived;

                        // Reserve room in reliables-received array
                        boolean falsesRemoved = buffer.setTop(id + 1, false);

                        // If we had to remove some "false" values from the bottom, this means
                        // that there are packets that were never received!
                        // If this is the reliable buffer, then we have an error as we are
                        // unable to guarantee that reliable packets were delivered.
                        if (falsesRemoved && reliable) {
                            disconnect(true);
                            setError("Reliable packet never arrived");
                            break;
                        }

                        // If the packet is in range, and we haven't already received
                        // one for this id, then process it. Otherwise discard it as
                        // a duplicate.
                        if (buffer.isInRange(id) && !buffer.getValueAt(id)) {

                            netLog("Queue incoming L1 packet");

                            int dataSize = size - NetPacketHeaderL1.SIZE;
                            NetInPacketL1 packet = new NetInPacketL1(dataSize, resent);
                            if (dataSize > 0) {
                                connection.receivePart(packet.getPacket().data, NetPacketHeaderL1.SIZE, dataSize);
                            }

                            // Create and queue received packet
                            synchronized (inQueueLock) {
                                recvBuffer.add(packet);
                            }

                            // Mark ID as received
                            buffer.set(id, true);
                        } else {
                            netLog("Already received this L1 packet, ignore duplicate");
                        }

                        // Always confirm reliable packets.
                        // Even duplicates, as the original confirmation message may
                        // have been lost.
                        if (reliable) {

                            netLog("Confirm L1 packet");
                            buildAndSend(false, l1Confirm, id, tickCount, false);
                        }
                    }
                    break;

                case l1KeepAlive:
                    break;

                case l1Confirm:
                    {

                        // Remove confirmed packet from send buffer
                        synchronized (outQueueLock) {
                            Iterator<NetOutPacketL1> sendIt = sendBuffer.iterator();
                            while (sendIt.hasNext()) {
                                NetOutPacketL1 packet = sendIt.next();
                                if (packet.getId() == id) {

                                    // Delete packet
                                    packet.dispose();

                                    // Remove entry from buffer
                                    sendIt.remove();
                                }
                            }
                        }
                    }
                    break;

                case l1Connect:
                    {
                        if (!connection.isClient()) {

                            // Connection request
                            // Send accepted packet.
                            // Note, we do this regardless of whether we are handshaking or not.
                            // This is because the other end may not know that we have progressed
                            // past the handshaking stage (eg a previous "accept" may not have
                            // gotten through.)

                            netLog("Send L1 accept");

                            // Send connection accepted packet
                            buildAndSend(false, l1Accept, 0, tickCount, false);

                            // Handshaking is now complete
                            synchronized (stateLock) {
                                handShaking = false;
                            }
                        }
                    }
                    break;

                case l1Accept:
                    {

                        // Connection accepted
                        if (connection.isClient()) {
                            synchronized (stateLock) {
                                handShaking = false;
                            }
                        }
                    }
                    break;

                case l1Disconnect:
                    {

                        // Clean disconnect received
                        disconnect(false);
                    }
                    break;
            }
        }

        // Finished with this packet
        connection.onDonePendingData();

        return true;
    }

    @Override
    public void run() {

        while (!processThread.isTerminating()) {

            // Wait for data event on connection, or until it is time to
            // perform a maintenance action such as sending a keepalive,
            // resending a reliable packet etc.

            // Calculate when the next maintenance action is due
            long wait;
            synchronized (processLock) {
                if (connection.isConnected()) {

                    long nextEvent = wakeupTime;

                    // Timeout?
                    long event = lastReceived + (handShaking ? settings.handshakeTimeout : settings.timeout);
                    if (event < nextEvent) {
                        nextEvent = event;
                    }

                    // Keepalive?
                    event = lastSent + (handShaking ? settings.reliableResend : settings.keepAlive);
                    if (event < nextEvent) {
                        nextEvent = event;
                    }

                    // Reliable resend?
                    if (!handShaking) {
                        synchronized (outQueueLock) {
                            Iterator<NetOutPacketL1> sendIt = sendBuffer.iterator();
                            while (sendIt.hasNext()) {
                                NetOutPacketL1 packet = sendIt.next();
                                event = packet.getDue();
                                if (event < nextEvent) {
                                    nextEvent = event;
                                }
                            }
                        }
                    }

                    // Calculate duration to wait
                    nextEvent += WAITEXTENSION;
                    long tickCount = getTickCount();
                    wait = nextEvent;
                    if (wait > tickCount) {
                        wait -= tickCount;
                    } else {
                        wait = 0;
                    }
                } else {
                    wait = INFINITE;
                }
            }

            // Wait for data or next due event
            ThreadEvent[] events = new ThreadEvent[] {connection.getEvent(), processThread.getTerminateEvent()};
            if (wait > 0) {
                ThreadUtils.waitForEvents(events, 2, false, wait);
            }
            wakeupTime = INFINITE;

            synchronized (processLock) {
                if (!processThread.isTerminating() && connection.isConnected()) {

                    long tickCount = getTickCount();

                    // Process incoming packets
                    synchronized (inQueueLock) {
                        while (processRecvPacket(tickCount)) {}
                    }

                    // Process outgoing packets
                    synchronized (outQueueLock) {
                        Iterator<NetOutPacketL1> sendIt = sendBuffer.iterator();
                        while (sendIt.hasNext()) {
                            NetOutPacketL1 i = sendIt.next();
                            if (tickCount > i.getDue()) {

                                // Send the packet
                                netLog("Send buffered outgoing L1 packet. " + getDescription(i.getPacket()));

                                // Mark the packet as resent
                                NetPacketHeaderL1 header = new NetPacketHeaderL1(i.getPacket().data);
                                header.setFlags((byte) (header.getFlags() | (byte) NETL1_RESENT));

                                connection.send(i.getPacket().data, i.getPacket().size);
                                lastSent = tickCount;

                                // If packet is reliable, leave it in the buffer
                                if (i.isReliable()) {

                                    // Update due date of next resend
                                    i.setDue(tickCount + settings.reliableResend);
                                } else {
                                    // Otherwise delete it, and remove from buffer.

                                    i.dispose();
                                    sendIt.remove();
                                }
                            }
                        }
                    }

                    ////////////////////////////
                    // Keepalives and timeouts

                    // Timeouts
                    long timeout = handShaking ? settings.handshakeTimeout : settings.timeout;
                    if (tickCount >= lastReceived + timeout) {
                        netLog("last received " + lastReceived);
                        netLog("tickcount " + tickCount);
                        netLog("timeout " + timeout);
                        netLog("L1 connection timed out");
                        disconnect(true);
                    }

                    // Keep alives
                    else if (connection.isConnected() && handShaking) {

                        // Keepalives are not sent during handshaking, but the client does keep
                        // sending requests until accepted, disconnected or timed out
                        if (connection.isClient() && tickCount >= lastSent + settings.reliableResend) {

                            netLog("Resend L1 connect request");
                            buildAndSend(false, l1Connect, 0, tickCount, false);
                        }
                    } else {

                        // Send keepalive
                        if (tickCount > lastSent + settings.keepAlive) {

                            netLog("Send L1 keepalive");
                            buildAndSend(false, l1KeepAlive, 0, tickCount, false);
                        }
                    }
                }

                // Process thread callback
                if (netProcessThreadListener != null) {
                    netProcessThreadListener.onProcessThread();
                }
            }
        }
    }

    // Member access

    /**
     * Read/write connection settings
     */
    public NetSettingsL1 getSettings() {

        // Locking unnecessary on read, as only UI thread writes to settings
        return settings;
    }

    public void setSettings(NetSettingsL1 settings) {
        synchronized (processLock) {
            this.settings = settings;
        }
    }

    /**
     * Return true if connection is a client connection, false if is a server connection.
     */
    public boolean isHandShaking() {
        boolean result;
        synchronized (stateLock) {
            result = handShaking;
        }
        return result;
    }

    /**
     * Connect to address, passing request string.
     * Request string will be returned by NetListenLow.getRequestString() on the server
     * when the connection request is received (NetListenLow.isConnectionPending() == true)
     */
    public boolean connect(String address, String connectionRequest) {
        boolean result;
        synchronized (processLock) {
            if (connection.isClient() && !isConnected() && connection.connect(address)) {

                netLog("Send L1 connection request");

                // Switch to handshaking mode
                handShaking = true;

                // Client connections must start the handshaking sequence
                if (connection.isClient()) {

                    // Build connection packet
                    byte[] buf = new byte[NetPacketHeaderL1.SIZE + MAX_CON_REQ_SIZE];
                    NetPacketHeaderL1 header = NetPacketHeaderL1.with(buf);
                    // Setup header
                    buildHeader(header, false, l1Connect, 0);

                    // Add connection string
                    // Calculate length
                    int size = connectionRequest.length();

                    // May need to trim to fit buffer
                    if (size >= MAX_CON_REQ_SIZE) {
                        size = MAX_CON_REQ_SIZE - 1;
                    }

                    // Or packet
                    if (size >= connection.getMaxPacketSize() - NetPacketHeaderL1.SIZE) {
                        size = connection.getMaxPacketSize() - NetPacketHeaderL1.SIZE - 1;
                    }

                    // Insert string
                    if (size > 0) {
                        System.arraycopy(
                                connectionRequest.getBytes(StandardCharsets.UTF_8),
                                0,
                                buf,
                                NetPacketHeaderL1.SIZE,
                                size);
                    }

                    // Send packet
                    for (int i = 0; i < settings.dup; i++) {
                        connection.send(buf, NetPacketHeaderL1.SIZE + size);
                    }

                    synchronized (stateLock) {
                        lastSent = getTickCount();
                    }
                }
            }

            result = isConnected();
        }
        return result;
    }

    /**
     * Connect to address, passing a blank request string ("").
     */
    public boolean connect(String address) {
        return connect(address, "");
    }

    /**
     * Disconnect.
     * \param clean	is true to attempt to disconnect cleanly. If successful,
     * <p>
     * the other connection will be notified of the disconnect.
     * If clean is false, the other connection will not be
     * notified and will simply receive no responses from this
     * connection until it times out.
     */
    public void disconnect(boolean clean) {
        synchronized (processLock) {
            if (isConnected()) {
                if (clean) {

                    netLog("Send clean disconnect");

                    // Send clean disconnect packet(s)
                    NetPacketHeaderL1 header = new NetPacketHeaderL1();
                    buildHeader(header, false, l1Disconnect, 0);

                    for (int i = 0; i < settings.dup; i++) {
                        connection.send(header.getBuffer(), NetPacketHeaderL1.SIZE);
                    }
                }

                // Low level disconnect
                connection.disconnect();
            }
        }
    }

    /**
     * Note: Connection is considered connected even in the handshaking stage.
     */
    public boolean isConnected() {

        return connection.isConnected();
    }

    /**
     * True if a data packet is pending
     */
    public boolean isDataPending() {
        boolean result;
        synchronized (inQueueLock) {
            result = !recvBuffer.isEmpty();
        }
        return result;
    }

    /**
     * Size of the pending data packet in bytes
     */
    public int getPendingDataSize() {
        int result;
        synchronized (inQueueLock) {
            result = isDataPending() ? recvBuffer.get(0).getPacket().size : 0;
        }
        return result;
    }

    /**
     * Returns true if the pending packet was resent.
     * Call this ONLY if isDataPending() returns true.
     */
    public boolean isPendingResent() {
        boolean result;
        synchronized (inQueueLock) {
            result = isDataPending() && recvBuffer.get(0).isResent();
        }
        return result;
    }

    /**
     * Receive part of the pending data packet.
     * PendingDataSize() must return true before you call this method.
     */
    public int receivePart(byte[] data, int offset, int size) {
        Assert.assertTrue(data != null);
        synchronized (inQueueLock) {
            if (isDataPending()) {

                // Find topmost queued packet
                NetSimplePacket packet = recvBuffer.get(0).getPacket();

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

    /**
     * Receive a pending packet and signal we have finished with it
     */
    public int receive(byte[] data, int size) {
        synchronized (inQueueLock) {
            size = receivePart(data, 0, size);
            onDonePendingData();
        }
        return size;
    }

    /**
     * Signal that we have finished with a pending data packet.
     */
    public void onDonePendingData() {
        synchronized (inQueueLock) {
            if (isDataPending()) {
                NetInPacketL1 packet = recvBuffer.get(0);
                recvBuffer.remove(0);
                packet.dispose();
            }
        }
    }

    /**
     * Maximum number of bytes that can be sent in a single packet.
     * (Not counting header data.)
     */
    public int getMaxPacketSize() {

        // Allow room for our header
        return Math.min(settingsStatic.getPrefMaxPacketSize(), connection.getMaxPacketSize()) - NetPacketHeaderL1.SIZE;
    }

    /**
     * Send a packet of data.
     *
     * @param reliable if true, the packet will be sent continually until a
     *                 confirmation is received.
     *                 <p>
     *                 If false, the packet will be sent once (or .getSettings().dup times) and forgotten about.
     *                 </p>
     */
    public void send(byte[] data, int size, boolean reliable) {
        if (!isConnected()) {
            return;
        }

        // Create new packet. Allow room for our header
        NetSimplePacket packet = new NetSimplePacket(size + NetPacketHeaderL1.SIZE);
        NetPacketHeaderL1 packetHeader = new NetPacketHeaderL1(packet.data);
        // TODO        char				*packetData	= packet->data + sizeof (NetPacketHeaderL1);

        // Setup header
        if (reliable) {
            buildHeader(packetHeader, true, l1User, sendIDReliable++);
        } else {
            buildHeader(packetHeader, false, l1User, sendIDUnreliable++);
        }

        netLog("Send L1 packet, " + getDescription(packetHeader));

        // Copy user data into the packet buffer after the header
        if (size > 0) {
            System.arraycopy(data, 0, packet.data, NetPacketHeaderL1.SIZE, size);
        }

        // Send packet
        // Note: Packets are not sent during the handshaking stage,
        // Instead they are queued, and flushed once the actual handshake succeeds.
        synchronized (stateLock) {
            if (!isHandShaking()) {
                for (int i = 0; i < settings.dup; i++) {
                    connection.send(packet.data, packet.size);
                }
            }

            // Queue packet if necessary
            if (isHandShaking() || reliable) {

                netLog("Queue outgoing L1 packet");

                // Calculate due time
                // Packets queued while handshaking are sent as soon as possible
                // Reliable packets are sent after a resend delay
                long due;
                if (isHandShaking()) {
                    due = getTickCount();
                } else {
                    due = getTickCount() + settings.reliableResend;
                }

                // Create queued outgoing packet
                synchronized (outQueueLock) {
                    sendBuffer.add(new NetOutPacketL1(due, packet));
                }
            } else {
                // Packet not queued, delete it
                packet.dispose();
            }
        }
    }

    public void startThread() {
        processThread = new Thread(NetConL1.class.getName());
        processThread.start(this);
    }

    /**
     * Hook into callback mechanism.
     * <p>
     * Note: Any object that calls this method should be sure to
     * later null out the callback before destruction.
     * </p>
     */
    public void setNetProcessThreadCallback(NetProcessThreadListener callback) {
        synchronized (processLock) {
            netProcessThreadListener = callback;
        }
    }

    /**
     * Request that this.callback be called in msec milliseconds.
     * <p>
     * The this.callback will then be called on the connections process thread.
     * </p>
     */
    public void requestWakeup(long millis) {

        // Must only be used by objects using the thread callback mechanism,
        // (and must only be called during that callback, from that thread.)
        if (wakeupTime == INFINITE || millis < wakeupTime) {
            wakeupTime = millis;
        }
    }

    public String getAddress() {
        return connection.getAddress();
    }
}
