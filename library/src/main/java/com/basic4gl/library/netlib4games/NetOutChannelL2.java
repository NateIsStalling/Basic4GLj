package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLayer2.*;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Layer 2 outgoing network channel.
 */
public class NetOutChannelL2 extends HasErrorState {
    private final int channel;
    private final boolean ordered;
    private int messageIndex;
    private int reliableIndex;

    public NetOutChannelL2(int channel, boolean ordered) {
        this.channel = channel;
        this.ordered = ordered;
        messageIndex = 0;
        reliableIndex = 0;
    }

    @Deprecated
    public void dispose() {}

    // Member access
    public int getChannel() {
        return channel;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public int getReliableIndex() {
        return reliableIndex;
    }

    public void send(NetConL1 connection, byte[] data, int size, boolean reliable, boolean smoothed, long tickCount) {
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
                    | (ordered ? NETL2_ORDERED : 0)
                    | (channel & NETL2_CHANNELMASK));
            header.setChannelFlags(channelFlags);
            header.setMessageIndex(messageIndex);
            header.setReliableIndex(reliableIndex);
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
        messageIndex++;
        if (reliable) {
            reliableIndex++;
        }
    }
}
