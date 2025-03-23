package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLayer2.*;
import static com.basic4gl.library.netlib4games.NetLogger.netLog;
import static com.basic4gl.library.netlib4games.NetTimingBufferL2.NET_L2TIMINGBUFFERSIZE;

import com.basic4gl.library.netlib4games.internal.Assert;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class NetConL2 extends HasErrorState implements NetProcessThreadListener, NetInChannelL2Callback {

	/**
	 * Underlying layer 1 connection
	 */
	private final NetConL1 m_connection;

	// Channels
	private final NetOutChannelL2[] m_outChannels = new NetOutChannelL2[NETL2_MAXCHANNELS];
	private final NetInChannelL2[] m_inChannels = new NetInChannelL2[NETL2_MAXCHANNELS];
	private final List<NetMessageL2> m_messageQueue = new ArrayList<>();

	// Layer 2 settings
	private NetSettingsL2 m_settings = new NetSettingsL2();
	private NetSettingsStaticL2 m_settingsStatic = new NetSettingsStaticL2();

	// Thread handling
	private final Object inQueueLock = new Object();

	// Timing
	private final NetTimingBufferL2 m_timingBuffer = new NetTimingBufferL2();

	/**
	 * Construct a new network connection.
	 *
	 * <p>
	 * For client NetConL2 objects, the connection parameter is typically
	 * constructed at the same time, like so:
	 * </p>
	 * <pre>
	 * {@code
	 *  NetConL2 clientConnection = new NetConL2(new NetConLowUDP());
	 * }
	 * </pre>
	 * <p>
	 * The NetConL2 is constructed as unconnected and must be connected by calling {@link #connect}.
	 * </p>
	 *
	 * <p>
	 * For server connections {@see com.basic4gl.library.netlib4games.NetListenLow#acceptConnection()} returns a
	 * low level connection object that can be passed to the constructor like so:
	 * </p>
	 * <pre>
	 * {@code
	 *  if (listener.isConnectionPending()) {
	 *      NetConL2 serverConnection = new NetConL2(listener.acceptConnection());
	 *  }
	 * }
	 * </pre>
	 * <p>
	 * This constructs a NetConL2 representing the newly accepted connection.
	 * The NetConL2 is constructed as connected. You do not need to call {@link #connect}.
	 * </p>
	 *
	 * @param connection Is a low level NetConLow object
	 *                   that handles the network protocol specific part of the network connection.
	 *                   (The UDP/IP version is NetConLowUDP.)
	 * @param settings   (optional) the static network settings.
	 *                   The static settings cannot be changed once the connection is
	 *                   constructed. (Unlike the L1Settings() and L2Settings() which can be.)
	 */
	public NetConL2(NetConLow connection, NetSettingsStaticL2 settings) {
		m_settingsStatic = settings;
		m_connection = new NetConL1(connection, settings.getL1Settings());
		init();
	}

	public NetConL2(NetConLow connection) {
		m_connection = new NetConL1(connection);
		init();
	}

	public void dispose() {
		// Unhook from service thread callback
		m_connection.setNetProcessThreadCallback(null);
		m_connection.dispose();

		// Free channels
		for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
			if (m_outChannels[i] != null) {
				m_outChannels[i].dispose();
				m_outChannels[i] = null;
			}
			if (m_inChannels[i] != null) {
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

		netLog("Delete L2 connection");
	}

	private void init() {
		netLog("Create L2 connection");

		// Clear channel pointers
		for (int i = 0; i < NETL2_MAXCHANNELS; i++) {
			m_outChannels[i] = null;
			m_inChannels[i] = null;
		}

		// Start the processing thread
		m_connection.setNetProcessThreadCallback(this);
		m_connection.startThread();
	}

	private void checkError() {
		m_connection.lockError();
		if (m_connection.hasError()) {
			setError("Layer 1: " + m_connection.getError());
			m_connection.unlockError();
			if (isConnected()) {
				disconnect(true);
			}
		} else {
			m_connection.unlockError();
		}
	}

	private void checkObject(HasErrorState obj) {
		if (obj.hasError()) {
			setError(obj.getError());
			if (isConnected()) {
				disconnect(true);
			}
		}
	}

	private void checkObject(HasErrorStateThreadSafe obj) {
		obj.lockError();
		if (obj.hasError()) {
			setError(obj.getError());
			obj.unlockError();
			if (isConnected()) {
				disconnect(true);
			}
		} else {
			obj.unlockError();
		}
	}

	// Passed through to layer 1 connection

	/**
	 *
	 */
	/**
	 * Returns true if the connection is in handshaking stage
	 *
	 * <p>
	 * The handshaking stage applies to client connections only, and
	 * refers to the stage where the client is looking for the remote
	 * server and waiting for a connection accepted/rejected response
	 * (or a timeout).
	 * </p>
	 *
	 * <p>
	 * While handshaking:
	 * </p>
	 * <ul>
	 * 	<li>{@link #isConnected()} returns true!</li>
	 * 	<li>{@link #isHandShaking()} returns true</li>
	 * </ul>
	 *
	 * <p>
	 * After a handshake failed:
	 * </p>
	 * <ul>
	 * 	<li>{@link #isConnected()} returns false</li>
	 * 	<li>{@link #isHandShaking()} is undefined</li>
	 * </ul>
	 *
	 * <p>
	 * After a handshake succeeded:
	 * </p>
	 * <ul>
	 * 	<li>{@link #isConnected()} returns true</li>
	 * 	<li>{@link #isHandShaking()} returns false</li>
	 * </ul>
	 *
	 * <p>
	 * Therefore to wait for handshaking to complete (over-simplified example):
	 * </p>
	 * <pre>
	 * {@code
	 * while (connection.isConnected() && connection.isHandShaking())
	 * 		Thread.Sleep(50);
	 *
	 * if (connection.isConnected()) {
	 * 		// Connection accepted
	 * 		...
	 * }
	 * else {
	 * 		// Connection rejected
	 * 		...
	 * }
	 * }
	 * </pre>
	 * <p>
	 * Note: It is not strictly necessary to wait for the handshaking stage to complete.
	 * You can also simply proceed as if the connection was successful.
	 * No network messages will be received by the connection while in handshaking
	 * stage, and any messages sent will be automatically queued, and then sent
	 * when (and if) the connection is accepted.
	 * </p>
	 *
	 * <p>
	 * The main consequence of simply proceeding is that if the connection fails, it
	 * will appear to have succeeded for a few seconds, and then been disconnected.
	 * </p>
	 *
	 * @return true if the connection is in handshaking stage.
	 */
	public boolean isHandShaking() {
		return m_connection.isHandShaking();
	}

	/**
	 * Attempt to connect to a network address.
	 * <p>
	 * This method is intended for client connections only. Server connections
	 * are automatically connected as soon as they are accepted.
	 * </p>
	 *
	 * <p>
	 * The format of the address string depends on the underlying protocol.
	 * For UDP/IP, the expected format is "address:port#", for example:
	 * </p>
	 * <ul>
	 *   <li>localhost:8000</li>
	 *   <li>www.imadethisup.com:1234</li>
	 *   <li>127.0.0.1:5555</li>
	 * </ul>
	 *
	 * <p>
	 * The request string (optional) is sent to the server as part of the
	 * connection request. It can be accessed using
	 * {@see com.basic4gl.library.netlib4games.NetListenLow#getRequestString()} before the connection is accepted
	 * or rejected.
	 * </p>
	 *
	 * <p>
	 * If the connection successfully contacts the server, it will result in a
	 * pending connection request, which can be checked using
	 * {@see com.basic4gl.library.netlib4games.NetListenLow#isConnectionPending()} on the server side.
	 * </p>
	 */
	public boolean connect(String address, String requestString) {
		synchronized (inQueueLock) {
			m_timingBuffer.clear();
		}
		boolean result = m_connection.connect(address, requestString);
		checkError();
		return result;
	}

	public boolean connect(String address) {
		return connect(address, "");
	}

	/**
	 * Disconnect the network connection.
	 *
	 * @param clean true to attempt to send a clean disconnect notification to the remote PC.
	 *              or false to simply drop the connection.
	 */
	public void disconnect(boolean clean) {
		m_connection.disconnect(clean);
		checkError();
	}

	/**
	 * Returns true if the connection is connected.
	 * Also returns true if the connection is in the handshaking stage (see {@link #isHandShaking()})
	 *
	 * @return true if the connection is connected or is in the handshaking stage
	 */
	public boolean isConnected() {
		return m_connection.isConnected();
	}

	public NetSettingsL1 getL1Settings() {
		return m_connection.getSettings();
	}

	public void setL1Settings(NetSettingsL1 settings) {
		m_connection.setSettings(settings);
	}

	public NetSettingsL2 getSettings() {
		return m_settings;
	}

	public void setSettings(NetSettingsL2 settings) {
		synchronized (inQueueLock) {
			m_settings = settings;
		}
	}

	// Network operations

	/**
	 * Send a message to the remote PC.
	 * <br/>
	 * When the message arrives at the remote connection, it will cause that connection's {@link #hasDataPending()} to return true,
	 * and the message can be read with the {@link #receive} method (or similar).
	 * <br/>
	 * Note:
	 * <br/>
	 * Carefully choosing which messages are reliable and when ordering is important can
	 * make the difference between a game that is playable over internet conditions and
	 * one that isn't.
	 * <br/>
	 * The intention is to minimise the chance of the network stalling when a packet is
	 * dropped or arrives out of order, to maximise the chance that NetLib4Games can continue
	 * processing other packets.
	 * <br/>
	 * For example, if you separate "chat messages" and "gameplay updates" into different channels,
	 * then the gameplay can keep running if a chat channel stalls (waiting for a dropped chat message
	 * to be resent).
	 * <br/>
	 * Or, often position updates do not need to be sent reliably because they supersede each other.
	 * So if one update gets dropped, the game can simply continue and pick up the position from the
	 * next update.
	 *
	 * @param data     the message data.
	 * @param size     the size of the data.
	 * @param channel  is used to specify whether message order is important.
	 *                 <br/>
	 *                 Valid values are 0-31 inclusive:
	 *                 <ul>
	 *                     <li>
	 *                         Channel 0 is the unordered channel, and NetLib4Games will NOT enforce that messages are received in the same order that they are sent.
	 *                     </li>
	 *                     <li>
	 *                         Channels 1-31 are ordered channels. NetLib4Games will ensure that messages within the SAME channel are received in the
	 *                         same order that they were sent, by either dropping out-of-order non-reliable messages, or by stalling the channel while it waits
	 *                         for an out-of-order reliable message to be received and slotted into it's correct position.
	 *                      </li>
	 *                   </ul>
	 * @param reliable true if the message MUST get through.
	 *                 NetLib4Games will automatically keep sending reliable messages until they are confirmed by the remote connection.
	 *                 <br/>
	 *                 Unreliable messages may be dropped.
	 * @param smoothed true to use the latency smoothing algorithm.
	 *                 This attempts to correct fluctuating lag and ensure that network packets that are
	 *                 sent out at regular time intervals are received at regular time intervals.
	 *                 Be careful when using this option, as it effectively works by ADDING EXTRA LAG to messages that
	 *                 arrive quicker than average.
	 */
	public void send(byte[] data, int size, int channel, boolean reliable, boolean smoothed) {
		Assert.assertTrue(data != null);
		Assert.assertTrue(channel >= 0);
		Assert.assertTrue(channel < NETL2_MAXCHANNELS);

		// Create channel if necessary
		// By convention: Channel 0 is unordered, all other channels are ordered.
		if (m_outChannels[channel] == null) {
			netLog("Create outgoing channel #" + channel + (channel == 0 ? ", unordered" : ", ordered"));
			m_outChannels[channel] = new NetOutChannelL2(channel, channel != 0);
		}

		// Send data through channel
		netLog("Send L2 message, "
				+ size
				+ " bytes, channel # "
				+ channel
				+ (reliable ? ", reliable" : ", unreliable")
				+ (smoothed ? ", smoothed" : ", unsmoothed"));
		m_outChannels[channel].send(m_connection, data, size, reliable, smoothed, NetLayer1.getTickCount());
		checkObject(m_outChannels[channel]);
	}

	/**
	 * Returns true if a network message has been received.
	 * The message can then be read with {@link #receive} or {@link #receivePart}.
	 *
	 * @return true if a network message has been received.
	 */
	public boolean hasDataPending() {
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

	/**
	 * Returns the size of the pending network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 *
	 * @return the size of the pending network message.
	 */
	public long getPendingDataSize() {
		Assert.assertTrue(hasDataPending());
		long result;
		synchronized (inQueueLock) {
			result = m_messageQueue.get(0).getDataSize();
		}
		return result;
	}

	/**
	 * Receive part of the pending network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * <p>
	 * Calling code MUST ensure that offset and size refer to a valid range of
	 * data inside the pending message.
	 * Use {@link #getPendingDataSize()} to get the size of the pending message.
	 * </p>
	 *
	 * <p>
	 * After calling {@link #receivePart}, the pending message will still be pending.
	 * If/when you have finished with the pending message, you should call
	 * {@link #onDonePendingData()} to inform NetLib4Games that you have finished with it.
	 * </p>
	 *
	 * @param data   The destination buffer.
	 * @param offset The offset in the pending network message to copy data from.
	 * @param size   The number of bytes to copy.
	 * @return
	 */
	public int receivePart(byte[] data, int offset, int size) {
		Assert.assertTrue(hasDataPending());
		Assert.assertTrue(data != null);
		Assert.assertTrue(offset <= getPendingDataSize());
		synchronized (inQueueLock) {
			size = m_messageQueue.get(0).copyData(data, offset, size);
		}
		return size;
	}

	/**
	 * Receive a network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * Calling code MUST ensure that size is not greater than the size of the
	 * pending network message. Use {@link #getPendingDataSize()} to get the size
	 * of the pending message.
	 * <br/>
	 * Receive automatically discards the network message after it has copied the
	 * data, so calling code MUST NOT CALL {@link #onDonePendingData()} itself.
	 *
	 * @param data The destination buffer.
	 * @param size The number of bytes to copy from the pending network message.
	 * @return size of data received
	 */
	public int receive(byte[] data, int size) {
		synchronized (inQueueLock) {
			size = receivePart(data, 0, size);
			onDonePendingData();
		}
		return size;
	}

	/**
	 * Indicates that we have finished with the pending message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * Use this method if you are reading a message with ReceivePart().
	 * You do not need to call {@link #onDonePendingData()} if you are using Receive() to
	 * read incoming messages (as {@link #receive)} does this automatically).
	 */
	public void onDonePendingData() {
		Assert.assertTrue(hasDataPending());
		NetMessageL2 message;
		synchronized (inQueueLock) {
			message = m_messageQueue.get(0);
			m_messageQueue.remove(0);
		}
		message.dispose();
	}

	// Other pending data functions

	/**
	 * Return the channel # of the pending network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * See {@link #send} for more info.
	 *
	 * @return the channel # of the pending network message.
	 */
	public int getPendingChannel() {
		Assert.assertTrue(hasDataPending());
		int result;
		synchronized (inQueueLock) {
			result = m_messageQueue.get(0).getChannel();
		}
		return result;
	}

	/**
	 * Return the reliable flag of the pending network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * See {@link #send} for more info.
	 *
	 * @return the reliable flag of the pending network message.
	 */
	public boolean isPendingReliable() {
		Assert.assertTrue(hasDataPending());
		boolean result;
		synchronized (inQueueLock) {
			result = m_messageQueue.get(0).isReliable();
		}
		return result;
	}

	/**
	 * Return the smoothed flag of the pending network message.
	 * Call this ONLY if {@link #hasDataPending()} returns true.
	 * See {@link #send} for more info.
	 *
	 * @return the smoothed flag of the pending network message.
	 */
	public boolean isPendingSmoothed() {
		Assert.assertTrue(hasDataPending());
		boolean result;
		synchronized (inQueueLock) {
			result = m_messageQueue.get(0).isSmoothed();
		}
		return result;
	}

	/**
	 * Thread callback
	 */
	public void onProcessThread() {

		// Handle incoming packets
		while (m_connection.isConnected() && m_connection.isDataPending()) {

			int size = m_connection.getPendingDataSize();
			netLog("Receive L2 packet, " + size + " bytes");
			if (size >= NetPacketHeaderL2.SIZE) {

				// Read header

				int headerSize = NetPacketHeaderL2.SIZE;
				byte[] headerBuffer = new byte[headerSize];
				headerSize = m_connection.receivePart(headerBuffer, 0, headerSize);
				NetPacketHeaderL2 header = new NetPacketHeaderL2(headerBuffer);

				netLog("Incoming L2 packet. " + NetLayer2.getDescription(header));

				// Decode header
				byte channelFlags = header.getChannelFlags();
				int channel = NetLayer2.getChannel(channelFlags);
				boolean reliable = (channelFlags & NETL2_RELIABLE) != 0,
						smoothed = (channelFlags & NETL2_SMOOTHED) != 0,
						ordered = (channelFlags & NETL2_ORDERED) != 0;

				// Get resent flag (from layer 1 header)
				boolean resent = m_connection.isPendingResent();

				if (channel >= 0 && channel < NETL2_MAXCHANNELS) {

					// If channel does not exist, create it
					if (m_inChannels[channel] == null) {
						netLog("Create incoming channel #" + channel + (ordered ? ", ordered" : ", unordered"));
						m_inChannels[channel] =
								new NetInChannelL2(channel, ordered, m_settingsStatic.getMaxBufferPackets());
					}

					// Read data
					NetSimplePacket packet = new NetSimplePacket(size - headerSize);
					if (packet.size > 0) {
						packet.size = m_connection.receivePart(packet.data, headerSize, packet.size);
					}

					// Buffer packet
					m_inChannels[channel].buffer(
							packet,
							reliable,
							smoothed,
							resent,
							header.getMessageIndex(),
							header.getReliableIndex(),
							header.getPacketIndex(),
							header.getPacketCount(),
							header.getTickCount());
					checkObject(m_inChannels[channel]);
				}
			}

			m_connection.onDonePendingData();
		}

		// Promote completed messages
		int i;

		synchronized (inQueueLock) {

			// Calculate adjusted tick count, based on smoothing data.
			long tickCount = NetLayer1.getTickCount(), adjustment = 0;
			boolean doSmoothing = false;

			// Timing buffer must be full
			if (m_timingBuffer.isBufferFull()) {

				// Can apply smoothing
				doSmoothing = true;

				// Find sorted position
				Assert.assertTrue(m_settings.smoothingPercentage >= 0);
				int index = (NET_L2TIMINGBUFFERSIZE * m_settings.smoothingPercentage) / 100;
				if (index >= NET_L2TIMINGBUFFERSIZE) {
					index = NET_L2TIMINGBUFFERSIZE - 1;
				}

				// Adjust tick count by sorted difference
				adjustment = m_timingBuffer.getDifference(index);
			}

			for (i = 0; i < NETL2_MAXCHANNELS; i++) {
				if (m_inChannels[i] != null) {
					m_inChannels[i].promoteMessages(this, tickCount, adjustment, doSmoothing);
				}
			}
		}

		// Cull old messages to prevent buffer overflowing
		for (i = 0; i < NETL2_MAXCHANNELS; i++) {
			if (m_inChannels[i] != null) {
				m_inChannels[i].cullMessages();
			}
		}
	}

	// NetInChannelL2Callback
	@Override
	public void queueMessage(NetMessageL2 msg) {
		m_messageQueue.add(msg);
	}

	@Override
	public void registerTickCountDifference(long difference) {
		m_timingBuffer.logDifference(difference);
	}

	@Override
	public void requestWakeup(long millis) {
		m_connection.requestWakeup(millis);
	}

	public String getAddress() {
		return m_connection.getAddress();
	}
}
