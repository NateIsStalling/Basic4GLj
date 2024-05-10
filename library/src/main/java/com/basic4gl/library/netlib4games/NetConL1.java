package com.basic4gl.library.netlib4games;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.basic4gl.library.netlib4games.NetL1Type.*;
import static com.basic4gl.library.netlib4games.NetLayer1.*;
import static com.basic4gl.library.netlib4games.NetLogger.NetLog;
import static com.basic4gl.library.netlib4games.ThreadUtils.INFINITE;

/**
 * Layer 1 network connection implementation.
 * Note: This is an internal part of the network engine. Applications generally
 * don't construct NetConL1 objects directly. Instead you should construct a
 * NetConL2 object to represent your connection.
 * See NetConL2, NetConLowUDP and NetListenLowUDP.
 * <p>
 * NetConL1 implements:
 * - Connection handling (incl. handshake sequence, timeouts and keepalives)
 * - Reliable packet sending.
 */
public class NetConL1 extends NetHasErrorStateThreadsafe implements Runnable {
    // : public NetHasErrorStateThreadsafe, public Threaded {
    /**
     * Settings that cannot be changed once the object has been constructed.
     */
    NetSettingsStaticL1 m_settingsStatic;

    /**
     * Settings that can be changed at any time
     */
    NetSettingsL1 m_settings = new NetSettingsL1();

    /**
     * Low level network connection object. E.g. NetConLowUDP for UDP/IP
     */
    NetConLow m_connection;

    List<NetInPacketL1> m_recvBuffer = new ArrayList<>();
    List<NetOutPacketL1> m_sendBuffer = new ArrayList<>();
    boolean m_handShaking;
    int m_sendIDReliable;
    int m_sendIDUnreliable;
    long m_lastSent,            // Used to coordinate keepalives
            m_lastReceived;        // Used to detect timeouts

    /**
     * Used to track which packets have already been received, and filter out duplicates.
     * Packets can be sent multiple times (e.g. if a confirmation packet is lost,
     * or deliberately duplicated if NetSettingsL1::dup > 1).
     */
    NetRevolvingBitBuffer m_reliableReceived,
            m_unreliableReceived;

    // Threading

    /**
     * Each connection has its own thread for processing network events.
     */
    Thread m_processThread;

    /**
     * Used to lock any operation that could clash with the processing thread.
     */
    ThreadLock m_processLock = new ThreadLock(),

    /**
     * Locks the incoming packet queue
     */
    m_inQueueLock = new ThreadLock(),

    /**
     * Locks the outgoing packet queue
     */
    m_outQueueLock = new ThreadLock(),

    /**
     * Used to lock any state data
     */
    m_stateLock = new ThreadLock();

    /**
     * Callback used to hook into the connection's processing thread.
     */
    NetProcessThreadCallback m_callback;

    /**
     * Used by thread callback to request a wakeup, for special processing
     */
    long m_wakeupTime;

    void Validate() {

        // Validate the connection and ensure that it is useable.
        // Called by constructors.
        // Calling code should check error state to see whether connection is useable
        if (m_connection.error()) {
            setError(m_connection.getError());
        }

        // Check max packet size, but only if connected. The UDP protocol at least
        // must be connected before it can correctly return the maximum packet size
        else if (Connected() && m_connection.MaxPacketSize() <= NetPacketHeaderL1.SIZE) {
            setError("Underlying network protocol packet size is too small!");
        } else {
            clearError();
        }

        // TODO!: Validate m_settingsStatic

        // Disconnect if error
        if (error() && Connected()) {
            Disconnect(false);
        }
    }

    void BuildHeader(
            NetPacketHeaderL1 header,
            boolean reliable,
            NetL1Type type,
            int id) {

        // Encode layer 1 packet header		
        header.setId(id);
        header.setFlags((byte) ((type.getType() & NETL1_TYPEMASK) | (reliable ? NETL1_RELIABLE : 0)));
    }

    void BuildAndSend(boolean reliable, NetL1Type type, int id, long tickCount) {
        BuildAndSend(reliable, type, id, tickCount, true);
    }

    void BuildAndSend(boolean reliable, NetL1Type type, int id, long tickCount, boolean dups) {
        NetPacketHeaderL1 header = new NetPacketHeaderL1();
        BuildHeader(header, reliable, type, id);
        long dupFactor = dups ? m_settings.dup : 1L;
        for (int i = 0; i < dupFactor; i++) {
            m_connection.Send(header.getBuffer(), NetPacketHeaderL1.SIZE);
        }
        m_stateLock.Lock();
        m_lastSent = tickCount;
        m_stateLock.Unlock();
    }

    boolean ProcessRecvPacket(long tickCount) {

        // Process the next inbound packet.
        if (!m_connection.DataPending()) {
            return false;
        }

        // Record that data was received.
        m_lastReceived = tickCount;

        // Get packet size
        int size = m_connection.PendingDataSize();
        // Note: If a packet is received that is smaller than our layer 1 header,
        // then it is erroneous. All we can do is ignore it and continue.

        NetLog("Receive L1 packet, " + size + " bytes");

        if (size >= NetPacketHeaderL1.SIZE) {

            // Read packet header
            NetPacketHeaderL1 header = new NetPacketHeaderL1();
            int recvSize = NetPacketHeaderL1.SIZE;
            recvSize = m_connection.ReceivePart(header.getBuffer(), 0, recvSize);

            NetLog("Incoming L1 packet. " + Desc(header));

            // Decode header
            byte flags = header.getFlags();
            boolean reliable = (flags & NETL1_RELIABLE) != 0;
            boolean resent = (flags & NETL1_RESENT) != 0;
            NetL1Type type = (NetL1Type) NetLayer1.getNetLayerType(flags);
            int id = header.getId();

            // Process packet
            switch (type) {
                case l1User: {

                    NetRevolvingBitBuffer buffer = reliable ? m_reliableReceived : m_unreliableReceived;

                    // Reserve room in reliables-received array
                    boolean falsesRemoved = buffer.SetTop(id + 1, false);

                    // If we had to remove some "false" values from the bottom, this means
                    // that there are packets that were never received!
                    // If this is the reliable buffer, then we have an error as we are
                    // unable to guarantee that reliable packets were delivered.
                    if (falsesRemoved && reliable) {
                        Disconnect(true);
                        setError("Reliable packet never arrived");
                        break;
                    }

                    // If the packet is in range, and we haven't already received
                    // one for this id, then process it. Otherwise discard it as 
                    // a duplicate.
                    if (buffer.InRange(id) && !buffer.Value(id)) {

                        NetLog("Queue incoming L1 packet");

                        int dataSize = size - NetPacketHeaderL1.SIZE;
                        NetInPacketL1 packet = new NetInPacketL1(dataSize, resent);
                        if (dataSize > 0) {
                            m_connection.ReceivePart(packet.packet.data, NetPacketHeaderL1.SIZE, dataSize);
                        }

                        // Create and queue received packet
                        m_inQueueLock.Lock();
                        m_recvBuffer.add(packet);
                        m_inQueueLock.Unlock();

                        // Mark ID as received
                        buffer.set(id, true);
                    } else {
                        NetLog("Already received this L1 packet, ignore duplicate");
                    }

                    // Always confirm reliable packets.
                    // Even duplicates, as the original confirmation message may
                    // have been lost.
                    if (reliable) {

                        NetLog("Confirm L1 packet");
                        BuildAndSend(false, l1Confirm, id, tickCount, false);
                    }
                }
                break;

                case l1KeepAlive:
                    break;

                case l1Confirm: {

                    // Remove confirmed packet from send buffer
                    m_outQueueLock.Lock();
                    Iterator<NetOutPacketL1> sendIt = m_sendBuffer.iterator();
                    while (sendIt.hasNext()) {
                        NetOutPacketL1 packet = sendIt.next();
                        if (packet.id == id) {

                            // Delete packet
                            packet.dispose();

                            // Remove entry from buffer
                            sendIt.remove();
                        }
                    }
                    m_outQueueLock.Unlock();
                }
                break;

                case l1Connect: {

                    if (!m_connection.Client()) {

                        // Connection request
                        // Send accepted packet.
                        // Note, we do this regardless of whether we are handshaking or not.
                        // This is because the other end may not know that we have progressed
                        // past the handshaking stage (eg a previous "accept" may not have
                        // gotten through.)

                        NetLog("Send L1 accept");

                        // Send connection accepted packet
                        BuildAndSend(false, l1Accept, 0, tickCount, false);

                        // Handshaking is now complete
                        m_stateLock.Lock();
                        m_handShaking = false;
                        m_stateLock.Unlock();
                    }
                }
                break;

                case l1Accept: {

                    // Connection accepted
                    if (m_connection.Client()) {
                        m_stateLock.Lock();
                        m_handShaking = false;
                        m_stateLock.Unlock();
                    }
                }
                break;

                case l1Disconnect: {

                    // Clean disconnect received
                    Disconnect(false);
                }
                break;
            }
        }

        // Finished with this packet
        m_connection.DonePendingData();

        return true;
    }

    @Override
    public void run() {

        while (!m_processThread.Terminating()) {

            // Wait for data event on connection, or until it is time to 
            // perform a maintenance action such as sending a keepalive,
            // resending a reliable packet etc.		

            // Calculate when the next maintenance action is due
            m_processLock.Lock();

            long wait;
            if (m_connection.Connected()) {

                long nextEvent = m_wakeupTime;

                // Timeout?
                long event = m_lastReceived + (m_handShaking ? m_settings.handshakeTimeout : m_settings.timeout);
                if (event < nextEvent) {
                    nextEvent = event;
                }

                // Keepalive?
                event = m_lastSent + (m_handShaking ? m_settings.reliableResend : m_settings.keepAlive);
                if (event < nextEvent) {
                    nextEvent = event;
                }

                // Reliable resend?
                if (!m_handShaking) {
                    m_outQueueLock.Lock();

                    Iterator<NetOutPacketL1> sendIt = m_sendBuffer.iterator();
                    while (sendIt.hasNext()) {
                        NetOutPacketL1 packet = sendIt.next();
                        event = packet.due;
                        if (event < nextEvent) {
                            nextEvent = event;
                        }
                    }

                    m_outQueueLock.Unlock();
                }

                // Calculate duration to wait
                nextEvent += WAITEXTENSION;
                long tickCount = GetTickCount();
                wait = nextEvent;
                if (wait > tickCount) {
                    wait -= tickCount;
                } else {
                    wait = 0;
                }
            } else {
                wait = INFINITE;
            }

            m_processLock.Unlock();

            // Wait for data or next due event
            ThreadEvent[] events = new ThreadEvent[]{m_connection.Event(), m_processThread.TerminateEvent()};
            if (wait > 0) {
                ThreadUtils.waitForEvents(events, 2, false, wait);
            }
            m_wakeupTime = INFINITE;

            m_processLock.Lock();
            if (!m_processThread.Terminating() && m_connection.Connected()) {

                long tickCount = GetTickCount();

                // Process incoming packets
                m_inQueueLock.Lock();
                while (ProcessRecvPacket(tickCount)) {
                    ;
                }
                m_inQueueLock.Unlock();

                // Process outgoing packets
                m_outQueueLock.Lock();

                Iterator<NetOutPacketL1> sendIt = m_sendBuffer.iterator();
                while(sendIt.hasNext()) {
                    NetOutPacketL1 i = sendIt.next();
                    if (tickCount > i.due){

                        // Send the packet
                        NetLog("Send buffered outgoing L1 packet. " + Desc(i.packet));

                        // Mark the packet as resent
                        NetPacketHeaderL1 header = new NetPacketHeaderL1(i.packet.data);
                        header.setFlags((byte) (header.getFlags() | (byte) NETL1_RESENT));

                        m_connection.Send( i.packet.data,  i.packet.size);
                        m_lastSent = tickCount;

                        // If packet is reliable, leave it in the buffer
                        if (i.reliable){

                            // Update due date of next resend
                            i.due = tickCount + m_settings.reliableResend;
                        }
							else{
                            // Otherwise delete it, and remove from buffer.

                            i.dispose();
                            sendIt.remove();
                        }
                    }
                }

                m_outQueueLock.Unlock();

                ////////////////////////////
                // Keepalives and timeouts

                // Timeouts
                long timeout = m_handShaking ? m_settings.handshakeTimeout : m_settings.timeout;
                if (tickCount >= m_lastReceived + timeout) {

                    NetLog("L1 connection timed out");
                    Disconnect(true);
                }

                // Keep alives
                else if (m_connection.Connected() && m_handShaking) {

                    // Keepalives are not sent during handshaking, but the client does keep
                    // sending requests until accepted, disconnected or timed out
                    if (m_connection.Client() && tickCount >= m_lastSent + m_settings.reliableResend) {

                        NetLog("Resend L1 connect request");
                        BuildAndSend(false, l1Connect, 0, tickCount, false);
                    }
                } else {

                    // Send keepalive
                    if (tickCount > m_lastSent + m_settings.keepAlive) {

                        NetLog("Send L1 keepalive");
                        BuildAndSend(false, l1KeepAlive, 0, tickCount, false);
                    }
                }
            }

            // Process thread callback
            if (m_callback != null) {
                m_callback.ProcessThreadCallback();
            }

            m_processLock.Unlock();
        }
    }

    /**
     * Construct a network connection with supplied settings
     */
    public NetConL1(NetConLow connection, NetSettingsStaticL1 settings) {
        super();
        m_connection = (connection);
        m_settingsStatic = (settings);
        m_reliableReceived = new NetRevolvingBitBuffer(m_settingsStatic.reliableBitBufSize, 0);
        m_unreliableReceived = new NetRevolvingBitBuffer(m_settingsStatic.unreliableBitBufSize, 0);
        m_sendIDReliable = (0);
        m_sendIDUnreliable = (0);
        m_wakeupTime = (INFINITE);

        m_processLock = new ThreadLock();

        assert (m_connection != null);

        NetLog("Create L1 connection");

        // Init state
        m_handShaking = m_connection.Connected();

        // Init timing
        m_lastSent =
                m_lastReceived = GetTickCount();

        // Validate
        Validate();
    }


    /**
     * Construct a network connection with default settings
     */
    public NetConL1(NetConLow connection) {
        super();
        m_connection = connection;
        m_settingsStatic = new NetSettingsStaticL1();
        m_reliableReceived = new NetRevolvingBitBuffer(m_settingsStatic.reliableBitBufSize, 0);
        m_unreliableReceived = new NetRevolvingBitBuffer(m_settingsStatic.unreliableBitBufSize, 0);
        m_sendIDReliable = (0);
        m_sendIDUnreliable = (0);
        m_wakeupTime = (INFINITE);

        m_processLock = new ThreadLock();

        assert (m_connection != null);

        NetLog("Create L1 connection");

        // Init state
        m_handShaking = m_connection.Connected();

        // Init timing
        long tickCount = GetTickCount();
        m_lastSent = tickCount;
        m_lastReceived = tickCount;

        // Validate
        Validate();
    }

    /**
     * Destroy the connection.
     * Will automatically clean disconnect (if there is no error), or
     * simply drop the connection otherwise.
     */
    public void dispose() {
        Disconnect(!error());

        // Close the processing thread
        m_processThread.Terminate();

        // Clear send/receive buffers
        Iterator<NetInPacketL1> receivePacket = m_recvBuffer.iterator();
        while (receivePacket.hasNext()) {
            NetInPacketL1 packet = receivePacket.next();
            packet.dispose();
        }
        m_recvBuffer.clear();

        Iterator<NetOutPacketL1> sendPacket = m_sendBuffer.iterator();
        while (sendPacket.hasNext()) {
            NetOutPacketL1 packet = sendPacket.next();
            packet.dispose();
        }
        m_sendBuffer.clear();

        if (m_connection != null) {
            m_connection.dispose();
        }

        NetLog("Delete L1 connection");
    }

    // Member access

    /**
     * Read/write connection settings
     */
    public NetSettingsL1 Settings() {

        // Locking unnecessary on read, as only UI thread writes to settings
        return m_settings;
    }

    public void SetSettings(NetSettingsL1 settings) {
        m_processLock.Lock();
        m_settings = settings;
        m_processLock.Unlock();
    }

    /**
     * Return true if connection is a client connection, false if is a server connection.
     */
    public boolean HandShaking() {
        m_stateLock.Lock();
        boolean result = m_handShaking;
        m_stateLock.Unlock();
        return result;
    }

    /**
     * Connect to address, passing request string.
     * Request string will be returned by NetListenLow::RequestString on the server
     * when the connection request is received (NetListenLow::ConnectionPending() == true)
     */
    public boolean Connect(String address, String connectionRequest) {

        m_processLock.Lock();
        if (m_connection.Client() && !Connected() && m_connection.Connect(address)) {

            NetLog("Send L1 connection request");

            // Switch to handshaking mode
            m_handShaking = true;

            // Client connections must start the handshaking sequence
            if (m_connection.Client()) {

                // Build connection packet
                byte[] buf = new byte[ NetPacketHeaderL1.SIZE + MAX_CON_REQ_SIZE];
                NetPacketHeaderL1 header = NetPacketHeaderL1.with(buf);
                // Setup header
                BuildHeader(header, false, l1Connect, 0);

                // Add connection string
                // Calculate length
                int size = connectionRequest.length();

                // May need to trim to fit buffer
                if (size >= MAX_CON_REQ_SIZE) {
                    size = MAX_CON_REQ_SIZE - 1;
                }

                // Or packet
                if (size >= m_connection.MaxPacketSize() - NetPacketHeaderL1.SIZE) {
                    size = m_connection.MaxPacketSize() - NetPacketHeaderL1.SIZE - 1;
                }

                // Insert string
                if (size > 0) {
                    System.arraycopy(connectionRequest.getBytes(StandardCharsets.UTF_8), 0, buf, NetPacketHeaderL1.SIZE, size);
                }

                // Send packet
                for (int i = 0; i < m_settings.dup; i++) {
                    m_connection.Send(buf, NetPacketHeaderL1.SIZE + size);
                }
                m_stateLock.Lock();
                m_lastSent = GetTickCount();
                m_stateLock.Unlock();
            }

        }

        boolean result = Connected();
        m_processLock.Unlock();
        return result;
    }

    /**
     * Connect to address, passing a blank request string ("").
     */
    public boolean Connect(String address) {
        return Connect(address, "");
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
    public void Disconnect(boolean clean) {
        m_processLock.Lock();
        if (Connected()) {
            if (clean) {

                NetLog("Send clean disconnect");

                // Send clean disconnect packet(s)
                NetPacketHeaderL1 header = new NetPacketHeaderL1();
                BuildHeader(header,
                        false,
                        l1Disconnect,
                        0);

                for (int i = 0; i < m_settings.dup; i++) {
                    m_connection.Send(header.getBuffer(), NetPacketHeaderL1.SIZE);
                }
            }

            // Low level disconnect
            m_connection.Disconnect();
        }
        m_processLock.Unlock();
    }

    /**
     * Note: Connection is considered connected even in the handshaking stage.
     */

    public boolean Connected() {

        return m_connection.Connected();
    }

    /**
     * True if a data packet is pending
     */
    public boolean DataPending() {
        m_inQueueLock.Lock();
        boolean result = !m_recvBuffer.isEmpty();
        m_inQueueLock.Unlock();
        return result;

    }

    /**
     * Size of the pending data packet in bytes
     */
    public int PendingDataSize() {
        m_inQueueLock.Lock();
        int result = DataPending() ? m_recvBuffer.get(0).packet.size : 0;
        m_inQueueLock.Unlock();
        return result;
    }

    /**
     * Returns true if the pending packet was resent.
     * Call this ONLY if DataPending() returns true.
     */
    public boolean PendingIsResent() {
        m_inQueueLock.Lock();
        boolean result = DataPending() ? m_recvBuffer.get(0).resent : false;
        m_inQueueLock.Unlock();
        return result;
    }

    /**
     * Receive part of the pending data packet.
     * PendingDataSize() must return true before you call this method.
     */
    public int ReceivePart(byte[] data, int offset, int size) {
        assert (data != null);
        m_inQueueLock.Lock();
        if (DataPending()) {

            // Find topmost queued packet
            NetSimplePacket packet = m_recvBuffer.get(0).packet;

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
        m_inQueueLock.Unlock();
        return size;
    }

    /**
     * Receive a pending packet and signal we have finished with it
     */
    public int Receive(byte[] data, int size) {
        m_inQueueLock.Lock();
        size = ReceivePart(data, 0, size);
        DonePendingData();
        m_inQueueLock.Unlock();
        return size;
    }

    /**
     * Signal that we have finished with a pending data packet.
     */
    public void DonePendingData() {
        m_inQueueLock.Lock();
        if (DataPending()) {
            NetInPacketL1 packet = m_recvBuffer.get(0);
            m_recvBuffer.remove(0);
            packet.dispose();
        }
        m_inQueueLock.Unlock();
    }

    /**
     * Maximum number of bytes that can be sent in a single packet.
     * (Not counting header data.)
     */
    public long MaxPacketSize() {

        // Allow room for our header
        return Math.min(m_settingsStatic.prefMaxPacketSize, m_connection.MaxPacketSize()) - NetPacketHeaderL1.SIZE;
    }

    /**
     * Send a packet of data.
     *
     * @param reliable if true, the packet will be sent continually until a
     *                 <p>
     *                 confirmation is received. If false, the packet will
     *                 be sent once (or ::Settings().dup times) and forgotten
     *                 about.
     */
    public void Send(byte[] data, int size, boolean reliable) {
        if (!Connected()) {
            return;
        }

        // Create new packet. Allow room for our header
        NetSimplePacket packet = new NetSimplePacket(size + NetPacketHeaderL1.SIZE);
        NetPacketHeaderL1 packetHeader = new NetPacketHeaderL1(packet.data);
//TODO        char				*packetData	= packet->data + sizeof (NetPacketHeaderL1);

        // Setup header
        if (reliable) {
            BuildHeader(packetHeader, true, l1User, m_sendIDReliable++);
        } else {
            BuildHeader(packetHeader, false, l1User, m_sendIDUnreliable++);
        }

        NetLog("Send L1 packet, " + Desc(packetHeader));

        // Copy user data into the packet buffer after the header
        if (size > 0) {
            System.arraycopy(data, 0, packet.data, NetPacketHeaderL1.SIZE, size);
        }

        // Send packet
        // Note: Packets are not sent during the handshaking stage, 
        // Instead they are queued, and flushed once the actual handshake succeeds.
        m_stateLock.Lock();
        if (!HandShaking()) {
            for (int i = 0; i < m_settings.dup; i++) {
                m_connection.Send(packet.data, packet.size);
            }
        }

        // Queue packet if necessary
        if (HandShaking() || reliable) {

            NetLog("Queue outgoing L1 packet");

            // Calculate due time
            // Packets queued while handshaking are sent as soon as possible
            // Reliable packets are sent after a resend delay
            long due;
            if (HandShaking()) {
                due = GetTickCount();
            } else {
                due = GetTickCount() + m_settings.reliableResend;
            }

            // Create queued outgoing packet
            m_outQueueLock.Lock();
            m_sendBuffer.add(new NetOutPacketL1(due, packet));
            m_outQueueLock.Unlock();
        } else {
            // Packet not queued, delete it
            packet.dispose();
        }
        m_stateLock.Unlock();
    }

    public void StartThread() {
        m_processThread = new Thread();
        m_processThread.Start(this);
    }

    /**
     * Hook into callback mechanism.
     * Note: Any object that calls this method should be sure to
     * <p>
     * later null out the callback before destruction.
     */
    public void HookCallback(NetProcessThreadCallback callback) {
        m_processLock.Lock();
        m_callback = callback;
        m_processLock.Unlock();
    }

    /**
     * Lock out the callback thread.
     * Use to ensure the callback code (setup via HookCallback) is not running.
     */
    public void LockCallback() {
        m_processLock.Lock();
    }

    public void UnlockCallback() {
        m_processLock.Unlock();
    }

    /**
     * Request that m_callback be called in msec milliseconds.
     * <p>
     * The m_callback will then be called on the connections process thread.
     */
    public void RequestWakeup(long millis) {

        // Must only be used by objects using the thread callback mechanism,
        // (and must only be called during that callback, from that thread.)
        if (m_wakeupTime == INFINITE || millis < m_wakeupTime) {
            m_wakeupTime = millis;
        }
    }

    public String Address() {
        return m_connection.Address();
    }
}
