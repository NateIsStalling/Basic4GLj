package com.basic4gl.library.netlib4games;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.basic4gl.library.netlib4games.NetLayer2.*;
import static com.basic4gl.library.netlib4games.NetLogger.NetLog;
import static com.basic4gl.library.netlib4games.NetTimingBufferL2.NET_L2TIMINGBUFFERSIZE;

/**
 * Layer 2 network connection implementation.
 * <br/>
 * This is the main network connection of NetLib4Games. 
 * For every network connection there will be one of these objects constructed
 * on the client, and another one on the server that is constructed when the 
 * connection is accepted.
 * <br/>
 * (NetLib4Games was built up in layers, where each layer sits atop the previous 
 * one and introduces new functionality. It just happened that by layer 2 it
 * was functional enough to be useful. Therefore all connection objects are 
 * NetConL2!)
 */
public class NetConL2  extends HasErrorState implements NetProcessThreadCallback, NetInChannelL2Callback{

    // Underlying layer 1 connection
    NetConL1					m_connection;

    // Channels
    NetOutChannelL2[]				m_outChannels = new NetOutChannelL2[NETL2_MAXCHANNELS];
    NetInChannelL2[]				m_inChannels = new NetInChannelL2 [NETL2_MAXCHANNELS];
    List<NetMessageL2> m_messageQueue = new ArrayList<>();

    // Layer 2 settings
    NetSettingsL2				m_settings = new NetSettingsL2();
    NetSettingsStaticL2			m_settingsStatic = new NetSettingsStaticL2();

    // Thread handling
//    ThreadLock					m_inQueueLock = new ThreadLock();
    private final Object inQueueLock = new Object();

    // Timing
    NetTimingBufferL2			m_timingBuffer = new NetTimingBufferL2();

    void Init () {
        NetLog ("Create L2 connection");

        // Clear channel pointers
        for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
            m_outChannels [i] = null;
            m_inChannels  [i] = null;
        }

        // Start the processing thread
        m_connection.HookCallback (this);
        m_connection.StartThread ();
    }
    void CheckError () {
        m_connection.LockError ();
        if (m_connection.error ()) {
            setError ("Layer 1: " + m_connection.getError ());
            m_connection.UnlockError ();
            if (Connected ()) {
                Disconnect (true);
            }
        }
        else {
            m_connection.UnlockError();
        }
    }
    void CheckObject (HasErrorState obj) {
        if (obj.error ()) {
            setError (obj.getError ());
            if (Connected ()) {
                Disconnect(true);
            }
        }
    }
    void CheckObject (HasErrorStateThreadSafe obj) {
        obj.LockError ();
        if (obj.error ()) {
            setError (obj.getError ());
            obj.UnlockError ();
            if (Connected ()) {
                Disconnect(true);
            }
        }
        else {
            obj.UnlockError();
        }
    }

    

    /// Construct a new network connection.
    /// \param connection Is a low level NetConLow object that handles
    /// the network protocol specific part of the network connection.
    ///	(The UDP/IP version is NetConLowUDP.)
    /// \param settings (optional) the static network settings.
    /// The static settings cannot be changed once the connection is 
    /// constructed. (Unlike the L1Settings() and L2Settings() which 
    /// can be.)	
    ///
    /// For client NetConL2 objects, the connection parameter is typically 
    /// constructed at the same time, like so:\code
    /// NetConL2 *clientConnection = new NetConL2(new NetConLowUDP());
    /// \endcode
    /// The NetConL2 is constructed as unconnected and must be connected
    /// by calling Connect().
    /// 
    /// For server connections NetListenLow::AcceptConnection() returns a 
    /// low level connection object that can be passed to the constructor like so:\code
    /// if (listener->ConnectionPending()) {
    ///		NetConL2 *serverConnection = new NetConL2(listener->AcceptConnection());
    ///		...
    ///	\endcode
    /// This constructs a NetConL2 representing the newly accepted connection.
    /// The NetConL2 is constructed as connected. You do not need to call
    /// Connect().
    public NetConL2 (NetConLow connection, NetSettingsStaticL2 settings) {
        m_settingsStatic = settings;
        m_connection = new NetConL1(connection, settings.l1Settings);
        Init();
    }
    public NetConL2 (NetConLow connection) {
        m_connection = new NetConL1(connection);
        Init();
    }
    public void dispose() {
        // Unhook from service thread callback
        m_connection.HookCallback (null);
        m_connection.dispose();

        // Free channels
        for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
            if (m_outChannels [i] != null) {
                m_outChannels[i].dispose();
                m_outChannels[i] = null;
            }
            if (m_inChannels [i] != null) {
                m_inChannels[i].dispose();
                m_inChannels[i] = null;
            }
        }

        // Clear pending queue
        Iterator<NetMessageL2> it = m_messageQueue.iterator();
        while (it.hasNext()) {
            NetMessageL2 message = it.next();
            message.dispose();
        }

        NetLog ("Delete L2 connection");
    }

    // Passed through to layer 1 connection

    /// Returns true if the connection is in handshaking stage.
    /// The handshaking stage applies to client connections only, and 
    /// refers to the stage where the client is looking for the remote
    /// server and waiting for a connection accepted/rejected response
    /// (or a timeout).
    ///
    /// While handshaking:
    ///	\li Connected() returns true!
    ///	\li	HandShaking() returns true
    ///	
    /// After a handshake failed:
    ///	\li Connected() returns false
    ///	\li HandShaking() is undefined
    ///
    /// After a handshake succeeded:
    ///	\li Connected() returns true
    /// \li HandShaking() returns false
    ///
    /// Therefore to wait for handshaking to complete (over-simplified example): \code
    /// while (connection.Connected() && connection.HandShaking())
    ///		Sleep(50);
    ///
    /// if (connection.Connected()) {
    ///		// Connection accepted
    ///		...
    /// } 
    /// else {
    ///		// Connection rejected
    ///		...
    /// }
    /// \endcode
    /// 
    /// Note: It is not strictly necessary to wait for the handshaking stage to complete.
    /// You can also simply proceed as if the connection was successful.
    /// No network messages will be received by the connection while in handshaking
    /// stage, and any messages sent will be automatically queued, and then sent
    /// when (and if) the connection is accepted.
    ///
    /// The main consequence of simply proceeding is that if the connection fails, it
    /// will appear to have succeeded for a few seconds, and then been disconnected.
    public boolean HandShaking () {
        return m_connection.HandShaking ();
    }

    /// Attempt to connect to network address.
    /// For client connections only. (Server connections are automatically connected
    /// as soon as they are accepted.)
    /// 
    /// The format of the address string depends on the underlying protocol.
    /// For UDP/IP this is "address:port#", e.g:
    ///	\li localhost:8000
    ///	\li www.imadethisup.com:1234
    ///	\li 127.0.0.1:5555
    ///
    /// The request string (optional) is passed to the server as part of the connection
    /// request, and can be read (NetListenLow::RequestString()) before the connection
    /// is accepted or rejected.
    ///
    /// If the connection manages to contact the server, it will result in a pending 
    /// connection request (NetListenLow::ConnectionPending() returns true at the server 
    /// end.)
    public boolean Connect (String address, String requestString) {
        synchronized (inQueueLock) {
            m_timingBuffer.Clear();
        }
        boolean result = m_connection.Connect (address, requestString);
        CheckError ();
        return result;
    }
    public boolean Connect (String address) {
        return Connect (address, "");
    }

    /// Disconnect the network connection.
    /// \param clean Is true to attempt to send a clean disconnect notification to the remote PC.
    ///				 or false to simply drop the connection.
    public void Disconnect (boolean clean) {
        m_connection.Disconnect (clean);
        CheckError ();
    }

    /// Returns true if the connection is connected.
    /// Also returns true if the connection is in the handshaking stage (see ::HandShaking)
    public boolean Connected () {
        return m_connection.Connected ();
    }
    public NetSettingsL1 L1Settings () {
        return m_connection.Settings ();
    }
    public void SetL1Settings (NetSettingsL1 settings) {
        m_connection.SetSettings (settings);
    }
    public NetSettingsL2 Settings () {
        return m_settings;
    }
    public void SetSettings (NetSettingsL2 settings) {
        synchronized (inQueueLock) {
            m_settings = settings;
        }
    }

    // Network operations

    /// Send a message to the remote PC.	
    ///	\param data is the message data.
    ///	\param size	is the size of the data.
    ///	\param channel is used to specify whether message order is important. Valid values are 0-31 inclusive.
    ///	Channel 0 is the unordered channel, and NetLib4Games will NOT enforce that messages are received in the same order that they are sent.
    /// Channels 1-31 are ordered channels. NetLib4Games will ensure that messages within the SAME channel are received in the 
    /// same order that they were sent, by either dropping out-of-order non-reliable messages, or by stalling the channel while it waits
    /// for an out-of-order reliable message to be received and slotted into it's correct position.
    /// \param reliable is true if the message MUST get through. NetLib4Games will automatically keep sending reliable messages
    /// until they are confirmed by the remote connection.
    /// Unreliable messages may be dropped.
    /// \smoothed is true to use the latency smoothing algorithm. This attempts to correct fluctuating lag and ensure that 
    /// network packets that are sent out at regular time intervals are received at regular time intervals.
    /// Be careful when using this option, as it effectively works by ADDING EXTRA LAG to messages that arrive quicker than
    /// average.
    ///
    /// When the message arrives at the remote connection, it will cause that connection's DataPending() to return true,
    /// and the message can be read with the Receive() method (or similar).
    ///
    /// Note:
    ///
    /// Carefully choosing which messages are reliable and when ordering is important can
    /// make the difference between a game that is playable over internet conditions and
    /// one that isn't.
    ///
    /// The intention is to minimise the chance of the network stalling when a packet is 
    /// dropped or arrives out of order, to maximise the chance that NetLib4Games can continue
    /// processing other packets.
    ///
    /// For example, if you separate "chat messages" and "gameplay updates" into different channels,
    /// then the gameplay can keep running if a chat channel stalls (waiting for a dropped chat message
    /// to be resent).
    ///
    /// Or, often position updates do not need to be sent reliably because they superceed each other.
    /// So if one update gets dropped, the game can simply continue and pick up the position from the
    /// next update.
    public void Send (byte [] data, int size, int channel, boolean reliable, boolean smoothed) {
        assert (data != null);
        assert (channel >= 0);
        assert (channel < NETL2_MAXCHANNELS);

        // Create channel if necessary
        // By convention: Channel 0 is unordered, all other channels are ordered.
        if (m_outChannels [channel] == null) {
            NetLog ("Create outgoing channel #" + channel + (channel == 0 ? ", unordered" : ", ordered"));
            m_outChannels [channel] = new NetOutChannelL2 (channel, channel != 0);
        }

        // Send data through channel
        NetLog (	"Send L2 message, "
                + size + " bytes, channel # " + channel
                + (reliable ? ", reliable" : ", unreliable")
                + (smoothed ? ", smoothed" : ", unsmoothed"));
        m_outChannels [channel].Send (m_connection, data, size, reliable, smoothed, NetLayer1.getTickCount());
        CheckObject (m_outChannels [channel]);
    }

    /// Returns true if a network message has been received.
    /// The message can then be read with Receive() or ReceivePart().
    public boolean DataPending () {
        // Note:
        // _Must_ return true if data in the buffer, even if not connected.
        // Otherwise the following code would not work:
        //
        //  if (connection.isDataPending ()) {
        //      connection.receive(...)
        //      ...
        //
        // Because connection status is volatile, and hence isDataPending() would also
        // be.
        boolean result;
        synchronized (inQueueLock) {
            result = !m_messageQueue.isEmpty();
        }
        return result;
    }

    /// Returns the size of the pending network message.
    /// Call this ONLY if DataPending() returns true.
    public long PendingDataSize () {
        assert (DataPending ());
        long result;
        synchronized (inQueueLock) {
            result = m_messageQueue.get(0).dataSize;
        }
        return result;
    }

    /// Receive part of the pending network message.
    /// Call this ONLY if DataPending() returns true.
    ///	\param data The destination buffer.
    ///	\param offset The offset in the pending network message to copy data from.
    /// \param size The number of bytes to copy.
    ///
    /// Calling code MUST ensure that offset and size refer to a valid range of 
    /// data inside the pending message.
    /// Use PendingDataSize() to get the size of the pending message.
    ///
    /// After calling ReceivePart, the pending message will still be pending.
    /// If/when you have finished with the pending message, you should call 
    /// DonePendingData() to inform NetLib4Games that you have finished with it.
    public int ReceivePart (byte[] data, int offset, int size) {
        assert (DataPending ());
        assert (data != null);
        assert (offset <= PendingDataSize ());
        synchronized (inQueueLock) {
            size = m_messageQueue.get(0).CopyData(data, offset, size);
        }
        return size;
    }

    ///	Receive a network message.
    /// Call this ONLY if DataPending() returns true.
    ///	\param data The destination buffer.
    /// \param size The number of bytes to copy from the pending network message.
    ///
    /// Calling code MUST ensure that size is not greater than the size of the 
    /// pending network message. Use PendingDataSize() to get the size
    /// of the pending message.
    ///
    /// Receive automatically discards the network message after it has copied the
    /// data, so calling code MUST NOT CALL DonePendingData() itself.
    public int Receive (byte[] data, int size) {
        synchronized (inQueueLock) {
            size = ReceivePart(data, 0, size);
            DonePendingData();
        }
        return size;
    }

    ///	Indicates that we have finished with the pending message.
    /// Call this ONLY if DataPending() returns true.
    /// Use this method if you are reading a message with ReceivePart().
    /// You do not need to call DonePendingData() if you are using Receive() to 
    /// read incoming messages (as Receive() does this automatically).
    public void DonePendingData () {
        assert (DataPending ());
        NetMessageL2 message;
        synchronized (inQueueLock) {
            message = m_messageQueue.get(0);
            m_messageQueue.remove(0);
        }
        message.dispose();
    }

    // Other pending data functions

    /// Return the channel # of the pending network message.
    /// Call this ONLY if DataPending() returns true.
    /// See Send() for more info.
    public int PendingChannel (){
        assert (DataPending ());
        int result;
        synchronized (inQueueLock) {
            result = m_messageQueue.get(0).channel;
        }
        return result;
    }

    /// Return the reliable flag of the pending network message.
    /// Call this ONLY if DataPending() returns true.
    /// See Send() for more info.
    public boolean PendingReliable (){
        assert (DataPending ());
        boolean result;
        synchronized (inQueueLock) {
            result = m_messageQueue.get(0).reliable;
        }
        return result;
    }

    /// Return the smoothed flag of the pending network message.
    /// Call this ONLY if DataPending() returns true.
    /// See Send() for more info.
    public boolean PendingSmoothed (){
        assert (DataPending ());
        boolean result;
        synchronized (inQueueLock) {
            result = m_messageQueue.get(0).smoothed;
        }
        return result;
    }

    // Thread callback
    public  void ProcessThreadCallback () {

        // Handle incoming packets
        while (m_connection.Connected () && m_connection.DataPending ()) {

            int size = m_connection.PendingDataSize ();
            NetLog ("Receive L2 packet, " + size + " bytes");
            if (size >= NetPacketHeaderL2.SIZE) {

                // Read header

                int headerSize = NetPacketHeaderL2.SIZE;
                byte[] headerBuffer = new byte[headerSize];
                headerSize = m_connection.ReceivePart (headerBuffer, 0, headerSize);
                NetPacketHeaderL2 header = new NetPacketHeaderL2(headerBuffer);

                NetLog ("Incoming L2 packet. " + NetLayer2.Desc (header));

                // Decode header
                byte channelFlags = header.getChannelFlags();
                int		channel		= NetLayer2.getChannel (channelFlags);
                boolean	reliable	= (channelFlags & NETL2_RELIABLE) != 0,
                        smoothed	= (channelFlags & NETL2_SMOOTHED) != 0,
                        ordered		= (channelFlags & NETL2_ORDERED)	 != 0;

                // Get resent flag (from layer 1 header)
                boolean    resent      = m_connection.PendingIsResent();

                if (channel >= 0 && channel < NETL2_MAXCHANNELS) {

                    // If channel does not exist, create it
                    if (m_inChannels [channel] == null) {
                        NetLog ("Create incoming channel #" + channel + (ordered ? ", ordered" : ", unordered"));
                        m_inChannels [channel] = new NetInChannelL2 (channel, ordered, m_settingsStatic.maxBufferPackets);
                    }

                    // Read data
                    NetSimplePacket packet = new NetSimplePacket (size - headerSize);
                    if (packet.size > 0) {
                        packet.size = m_connection.ReceivePart (packet.data, headerSize, packet.size);
                    }

                    // Buffer packet
                    m_inChannels [channel].Buffer (packet, reliable, smoothed, resent, header.getMessageIndex(), header.getReliableIndex(), header.getPacketIndex(), header.getPacketCount(), header.getTickCount());
                    CheckObject (m_inChannels [channel]);
                }
            }

            m_connection.DonePendingData ();
        }

        // Promote completed messages
        int i;

        synchronized (inQueueLock) {

            // Calculate adjusted tick count, based on smoothing data.
            long tickCount = NetLayer1.getTickCount(),
                    adjustment = 0;
            boolean doSmoothing = false;

            // Timing buffer must be full
            if (m_timingBuffer.BufferFull()) {

                // Can apply smoothing
                doSmoothing = true;

                // Find sorted position
                assert (m_settings.smoothingPercentage >= 0);
                int index = (NET_L2TIMINGBUFFERSIZE * m_settings.smoothingPercentage) / 100;
                if (index >= NET_L2TIMINGBUFFERSIZE) {
                    index = NET_L2TIMINGBUFFERSIZE - 1;
                }

                // Adjust tick count by sorted difference
                adjustment = m_timingBuffer.Difference(index);
            }

            for (i = 0; i < NETL2_MAXCHANNELS; i++) {
                if (m_inChannels[i] != null) {
                    m_inChannels[i].PromoteMessages(this, tickCount, adjustment, doSmoothing);
                }
            }
        }

        // Cull old messages to prevent buffer overflowing
        for (i = 0; i < NETL2_MAXCHANNELS; i++) {
            if (m_inChannels [i] != null) {
                m_inChannels [i].CullMessages ();
            }
        }
    }

    // NetInChannelL2Callback
    @Override
    public void QueueMessage (NetMessageL2 msg){
        m_messageQueue.add (msg);
    }
    @Override
    public void RegisterTickCountDifference (long difference){
        m_timingBuffer.LogDifference (difference);
    }
    @Override
    public void RequestWakeup (long millis){
        m_connection.RequestWakeup(millis);
    }

    public String Address() { return m_connection.Address(); }
}
