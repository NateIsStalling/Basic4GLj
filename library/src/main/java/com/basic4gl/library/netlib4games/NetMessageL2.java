package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Layer 2 network message
 */
public class NetMessageL2 {
    private final int channel;
    private final boolean reliable;
    private final boolean ordered;
    private boolean smoothed;
    private final int messageIndex; // unsigned long
    private final int reliableIndex; // unsigned long
    private final int packetCount;
    private long tickCount;
    private int receivedCount;
    private int dataSize;
    private boolean tickCountRegistered;
    private NetSimplePacket[] packets;

    NetMessageL2(
            int channel,
            boolean reliable,
            boolean smoothed,
            boolean ordered,
            int messageIndex,
            int reliableIndex,
            int packetCount,
            long tickCount) {
        this.channel = channel;
        this.reliable = reliable;
        this.smoothed = smoothed;
        this.ordered = ordered;
        this.messageIndex = messageIndex;
        this.reliableIndex = reliableIndex;
        this.packetCount = packetCount;
        this.tickCount = tickCount;
        tickCountRegistered = false;
        receivedCount = 0;
        dataSize = 0;

        // Allocate packet space
        if (this.packetCount > 0) {
            packets = new NetSimplePacket[this.packetCount];
            for (int i = 0; i < this.packetCount; i++) {
                packets[i] = null;
            }
        } else {
            packets = null;
        }
    }

    public void dispose() {

        // Delete packets
        if (packets != null) {

            // Delete assigned packets
            for (int i = 0; i < packetCount; i++) {
                if (packets[i] != null) {
                    packets[i].dispose();
                }
            }

            // Delete packet array
            packets = null;
        }
    }

    boolean isComplete() {
        return receivedCount >= packetCount;
    }

    boolean buffer(NetSimplePacket packet, int packetIndex) {
        Assert.assertTrue(packet != null);

        if (packetIndex >= 0 && packetIndex < packetCount && packets[packetIndex] == null) {

            // Add packet to message
            packets[packetIndex] = packet;
            receivedCount++;
            dataSize += packet.size;
            return true;
        } else {
            // Packet already received... or index is bad
            return false;
        }
    }

    /**
     * @param data
     * @param offset
     * @param size
     * @return size
     */
    int copyData(byte[] data, int offset, int size) {
        Assert.assertTrue(data != null);
        Assert.assertTrue(offset <= dataSize);

        // Adjust size
        if (offset + size > dataSize) {
            size = dataSize - offset;
        }

        // Find start packet
        int packet = 0;
        while (offset > packets[packet].size) {
            offset -= packets[packet].size;
            packet++;
        }

        // Copy data from packets
        int destOffset = 0, left = size;
        while (left > 0) {
            int copySize = Math.min(left, packets[packet].size - offset);

            // Copy data
            System.arraycopy(packets[packet].data, offset, data, destOffset, copySize);

            left -= copySize;
            destOffset += copySize;
            packet++;
            offset = 0;
        }
        return size;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isReliable() {
        return reliable;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public boolean isSmoothed() {
        return smoothed;
    }

    public void setSmoothed(boolean smoothed) {
        this.smoothed = smoothed;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public int getReliableIndex() {
        return reliableIndex;
    }

    public int getPacketCount() {
        return packetCount;
    }

    public long getTickCount() {
        return tickCount;
    }

    public void setTickCount(long tickCount) {
        this.tickCount = tickCount;
    }

    public int getReceivedCount() {
        return receivedCount;
    }

    public int getDataSize() {
        return dataSize;
    }

    public boolean isTickCountRegistered() {
        return tickCountRegistered;
    }

    public void setTickCountRegistered(boolean tickCountRegistered) {
        this.tickCountRegistered = tickCountRegistered;
    }

    public NetSimplePacket[] getPackets() {
        return packets;
    }
}
