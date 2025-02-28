package com.basic4gl.library.netlib4games;


import com.basic4gl.library.netlib4games.internal.Assert;

import static com.basic4gl.library.netlib4games.NetLayer2.*;

/**
 * Layer 2 outgoing network channel.
 */
public class NetOutChannelL2 extends HasErrorState {
    private final int m_channel;
    private final boolean m_ordered;
    private int m_messageIndex;
    private int m_reliableIndex;

    public NetOutChannelL2(int channel, boolean ordered) {
        m_channel = channel;
        m_ordered = ordered;
        m_messageIndex = 0;
        m_reliableIndex = 0;
    }

    @Deprecated
    public void dispose() {

    }

    // Member access
    public int getChannel() {
        return m_channel;
    }

    public boolean isOrdered() {
        return m_ordered;
    }

    public int getMessageIndex() {
        return m_messageIndex;
    }

    public int getReliableIndex() {
        return m_reliableIndex;
    }

    public void send(
            NetConL1 connection,
            byte[] data,
            int size,
            boolean reliable,
            boolean smoothed,
            long tickCount) {
        Assert.assertTrue(data != null);
        Assert.assertTrue(size >= 0);

        // Calculate maximum packet data size, allowing for packet header data.
        int maxPacketSize = connection.getMaxPacketSize();
        int maxDataSize = maxPacketSize - NetPacketHeaderL2.SIZE;
        if (maxDataSize <= 0) {
            setError("Bad max packet size. No room for header and data!");
            return;
        }

        // Allocate room to build each packet
        byte[] buffer = new byte[maxPacketSize];

        // Divide message into packets
        int packets = (size == 0) ? 1 : (size - 1) / maxDataSize + 1;
        int packet = 0, offset = 0;
        while (packet < packets) {
            int packetSize = Math.min(size, maxDataSize);

            // Encode L2 header
            NetPacketHeaderL2 header = new NetPacketHeaderL2(buffer);
            byte channelFlags = (byte) ((reliable ? NETL2_RELIABLE : 0)
                    | (smoothed ? NETL2_SMOOTHED : 0)
                    | (m_ordered ? NETL2_ORDERED : 0)
                    | (m_channel & NETL2_CHANNELMASK));
            header.setChannelFlags(channelFlags);
            header.setMessageIndex(m_messageIndex);
            header.setReliableIndex(m_reliableIndex);
            header.setPacketCount(packets);
            header.setPacketIndex(packet);
            header.setTickCount(tickCount);

            // Append data
            if (packetSize > 0) {
                System.arraycopy(data, offset, buffer, NetPacketHeaderL2.SIZE, packetSize);
            }
            offset += packetSize;
            size -= packetSize;

            // Send L2 packet
            connection.send(buffer, NetPacketHeaderL2.SIZE + packetSize, reliable);
            packet++;
        }

        // Update packet indices
        m_messageIndex++;
        if (reliable) {
            m_reliableIndex++;
        }
    }
}
