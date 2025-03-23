package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLogger.netLog;
import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetInChannelL2 extends HasErrorState {
    private final int m_channel;
    private final boolean m_ordered;
    private final long m_maxBufferPackets;
    private final List<NetMessageL2> m_messages = new ArrayList<>();
    private int m_messageIndex;
    private int m_reliableIndex;
    private int m_packetCount;

    public NetInChannelL2(int channel, boolean ordered, long maxBufferPackets) {
        m_channel = channel;
        m_ordered = ordered;
        m_maxBufferPackets = maxBufferPackets;
        m_messageIndex = 0;
        m_reliableIndex = 0;
        m_packetCount = 0;
    }

    public void dispose() {
        clear();
    }

    void clear() {
        // Delete pending messages
        Iterator<NetMessageL2> i = m_messages.iterator();
        while (i.hasNext()) {
            NetMessageL2 message = i.next();
            message.dispose();
            i.remove();
        }
    }

    // Member access
    public int getChannel() {
        return m_channel;
    }

    /**
     * Ordered channels guarantee delivery of messages in order sent.
     *
     * @return true if channel is ordered
     */
    public boolean isOrdered() {
        return m_ordered;
    }

    public void buffer(
            NetSimplePacket packet,
            boolean reliable,
            boolean smoothed,
            boolean resent,
            int messageIndex,
            int reliableIndex,
            int packetIndex,
            int packetCount,
            long tickCount) {

        assert packet != null;

        // Ordered channels can reject messages older than the last one promoted.
        if (m_ordered && messageIndex < m_messageIndex) {
            return;
        }

        // Find corresponding message
        NetMessageL2 message = null;
        Iterator<NetMessageL2> i = m_messages.iterator();
        int index = 0;
        while (message == null || message.getMessageIndex() < messageIndex) {
            if (i.hasNext()) {
                message = i.next();
                index++;
            } else {
                message = null;
                break;
            }
        }

        // If none exists, create a new one
        if (message == null || message.getMessageIndex() > messageIndex) {
            message = new NetMessageL2(
                    m_channel, reliable, smoothed, m_ordered, messageIndex, reliableIndex, packetCount, tickCount);

            // Insert at correct position
            m_messages.add(index, message);
        }
        // Add packet to message
        if (message.buffer(packet, packetIndex)) {
            m_packetCount++;

            // Disable smoothing if any part of the message was resent.
            // We don't want to include spikes from dropped packets in our timing
            // calculations.
            if (resent) {
                message.setSmoothed(false);
            }
        } else {

            // Packet already received... or index is bad
            packet.dispose();
        }
    }

    public void promoteMessages(NetInChannelL2Callback callback, long tickCount, long adjustment, boolean doSmoothing) {

        // Register tickCount differences
        long wakeup = INFINITE;
        Iterator<NetMessageL2> it = m_messages.iterator();
        while (it.hasNext()) {
            NetMessageL2 i = it.next();

            if (i.isSmoothed() && i.isComplete()) {

                if (!i.isTickCountRegistered()) {

                    // Register tick count difference
                    callback.registerTickCountDifference(tickCount - i.getTickCount());
                    i.setTickCountRegistered(true);

                    // Adjust tick count on message
                    if (doSmoothing) {
                        i.setTickCount(i.getTickCount() + adjustment);
                    } else {
                        i.setSmoothed(false);
                    }
                }

                // Check if wakeup is required
                if (doSmoothing && i.getTickCount() >= tickCount) {
                    long newWakeup = i.getTickCount() - tickCount;
                    if (wakeup == INFINITE || newWakeup < wakeup) {
                        wakeup = newWakeup;
                    }
                }
            }
        }

        // Request wakeup
        if (wakeup != INFINITE) {
            callback.requestWakeup(wakeup);
        }

        boolean found;
        do {
            found = false;

            // Search for complete message. Oldest first
            it = m_messages.iterator();
            NetMessageL2 i = null;
            while (it.hasNext()) {
                i = it.next();
                boolean complete = i.isComplete(); // Message is complete
                boolean isPacketDue = tickCount >= i.getTickCount();

                // TODO refactor this boolean; NetLayer2.cpp uses this condition to scan the iterator
                boolean notFound = !complete && (!doSmoothing || !i.isSmoothed() || isPacketDue);
                if (!notFound) {

                    // If the channels is ordered, then make sure there aren't any older reliable messages
                    // waiting to be promoted. If there are, then we can't promote this message yet.
                    if (!m_ordered || i.getReliableIndex() == m_reliableIndex) {
                        found = true;
                    }
                    break;
                }
            }
            if (found) {
                // Promote message
                NetMessageL2 msg = i;
                callback.queueMessage(msg);
                m_messages.remove(i);
                m_packetCount -= msg.getReceivedCount();

                netLog("Promote completed L2 message. Channel "
                        + m_channel
                        + ", Msg "
                        + msg.getMessageIndex()
                        + ", "
                        + msg.getPacketCount()
                        + " packets, "
                        + msg.getDataSize()
                        + " bytes");

                // Delete older packets (from ordered channels)
                if (m_ordered) {

                    // Update message index and reliable index
                    m_messageIndex = msg.getMessageIndex();
                    if (msg.isReliable()) {
                        m_reliableIndex++;
                    }

                    // Remove any older messages from queue
                    while (!m_messages.isEmpty() && m_messages.get(0).getMessageIndex() < m_messageIndex) {
                        NetMessageL2 otherMsg = m_messages.get(0);
                        m_messages.remove(0);
                        m_packetCount -= otherMsg.getReceivedCount();

                        netLog("Discard incomplete ordered unreliable L2 message. Channel "
                                + m_channel
                                + ", Msg "
                                + msg.getMessageIndex());

                        otherMsg.dispose();
                    }
                }
            }
        } while (found);
    }

    /**
     * Remove network messages to keep the buffer to a maximum size.
     * (Note: This is not a strictly enforced maximum in that the buffer will periodically
     * grow larger than the maximum. But we enforce that it must be cullable to the
     * maximum size.)
     */
    public void cullMessages() {
        while (m_packetCount > m_maxBufferPackets) {

            // Search for oldest unreliable message
            Iterator<NetMessageL2> it = m_messages.iterator();
            NetMessageL2 message = null;
            boolean reliable = true;
            while (it.hasNext() && reliable) {
                message = it.next();
                reliable = message.isReliable();
            }

            // None found
            if (reliable) {
                setError("Layer 2 network buffer overflow. No unreliable messages could be dropped");
                return;
            }

            // Drop the message
            m_messages.remove(message);
            m_packetCount -= message.getReceivedCount();
            message.dispose();
        }
    }
}
