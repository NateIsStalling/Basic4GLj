package com.basic4gl.library.netlib4games;

import java.nio.ByteBuffer;

public class NetPacketHeaderL2 {

    private static final int CHANNEL_FLAGS_BYTES = 1;

    /**
     * Message Index size in bytes
     *
     * NetLib4Games specifies unsigned long value
     * Java port only supports integer value of last 4 bytes
     * since array access is limited to int index values in Java
     */
    private static final int MESSAGE_INDEX_BYTES = 8;
    private static final int MESSAGE_INDEX_MOST_SIGNIFICANT_BYTES = 4;
    private static final int MESSAGE_INDEX_LEAST_SIGNIFICANT_BYTES = 4;

    /**
     * Reliable Index size in bytes
     *
     * NetLib4Games specifies unsigned long value
     * Java port only supports integer value of last 4 bytes
     * since array access is limited to int index values in Java
     */
    private static final int RELIABLE_INDEX_BYTES = 8;
    private static final int RELIABLE_INDEX_MOST_SIGNIFICANT_BYTES = 4;
    private static final int RELIABLE_INDEX_LEAST_SIGNIFICANT_BYTES = 4;

    private static final int PACKET_COUNT_BYTES = 4;
    private static final int PACKET_INDEX_BYTES = 4;

    private static final int TICK_COUNT_BYTES = 8;

    public static final int SIZE = CHANNEL_FLAGS_BYTES
            + MESSAGE_INDEX_BYTES
            + RELIABLE_INDEX_BYTES
            + PACKET_COUNT_BYTES
            + PACKET_INDEX_BYTES
            + TICK_COUNT_BYTES;


    private ByteBuffer buffer;

    private static final int CHANNEL_FLAGS_POSITION = 0;
    private static final int MESSAGE_INDEX_POSITION = CHANNEL_FLAGS_POSITION + CHANNEL_FLAGS_BYTES;
    private static final int MESSAGE_INDEX_MOST_SIGNIFICANT_POSITION = MESSAGE_INDEX_POSITION;
    private static final int MESSAGE_INDEX_LEAST_SIGNIFICANT_POSITION = MESSAGE_INDEX_POSITION + MESSAGE_INDEX_MOST_SIGNIFICANT_BYTES;
    private static final int RELIABLE_INDEX_POSITION = MESSAGE_INDEX_POSITION + MESSAGE_INDEX_BYTES;
    private static final int RELIABLE_INDEX_MOST_SIGNIFICANT_POSITION = RELIABLE_INDEX_POSITION;
    private static final int RELIABLE_INDEX_LEAST_SIGNIFICANT_POSITION = RELIABLE_INDEX_POSITION + RELIABLE_INDEX_MOST_SIGNIFICANT_BYTES;
    private static final int PACKET_COUNT_POSITION = RELIABLE_INDEX_POSITION + RELIABLE_INDEX_BYTES;
    private static final int PACKET_INDEX_POSITION = PACKET_COUNT_POSITION + PACKET_COUNT_BYTES;
    private static final int TICK_COUNT_POSITION = PACKET_INDEX_POSITION + PACKET_INDEX_BYTES;

//    byte			channelFlags;
//    int messageIndex;// should be padded as an unsigned long
//    int	reliableIndex; // should be padded as an unsigned long
//    int	packetCount;
//    int	packetIndex;
//    long	tickCount;			//

    public NetPacketHeaderL2() {
        this.buffer = ByteBuffer.wrap(new byte[SIZE]);
    }

    public NetPacketHeaderL2(byte[] buffer) {
        this.buffer = ByteBuffer.wrap(buffer);
    }

    public byte[] getBuffer() {
        return buffer.array();
    }

    public byte getChannelFlags() {
        return buffer.get(CHANNEL_FLAGS_POSITION);
    }

    public int getMessageIndex() {
        // TODO Endianness is not determined; this should be evaluated for potential errors
        // NetLib4Games original source specifies unsigned long for MessageIndex,
        // but only the least significant bytes are supported in this version
        if (buffer.getInt(MESSAGE_INDEX_MOST_SIGNIFICANT_POSITION) != 0) {
            throw new RuntimeException("Unsupported network protocol");
        }
        int value = buffer.getInt(MESSAGE_INDEX_LEAST_SIGNIFICANT_POSITION);

        // Check for unsigned values which are not supported in this version
        if (value < 0) {
            throw new RuntimeException("Unsupported network protocol");
        }

        return value;
    }

    public int getReliableIndex() {
        // TODO Endianness is not determined; this should be evaluated for potential errors
        // NetLib4Games original source specifies unsigned long for ReliableIndex,
        // but only the least significant bytes are supported in this version
        if (buffer.getInt(RELIABLE_INDEX_MOST_SIGNIFICANT_POSITION) != 0) {
            throw new RuntimeException("Unsupported network protocol");
        }
        int value = buffer.getInt(RELIABLE_INDEX_LEAST_SIGNIFICANT_POSITION);

        // Check for unsigned values which are not supported in this version
        if (value < 0) {
            throw new RuntimeException("Unsupported network protocol");
        }

        return value;
    }

    public int getPacketCount() {
        return buffer.getInt(PACKET_COUNT_POSITION);
    }

    public int getPacketIndex() {
        return buffer.getInt(PACKET_INDEX_POSITION);
    }

    /**
     * @return Sender's TickCount at time of sending
     */
    public long getTickCount() {
        return buffer.getLong(TICK_COUNT_POSITION);
    }

    public void setChannelFlags(byte channelFlags) {
        buffer.put(CHANNEL_FLAGS_POSITION, channelFlags);
    }

    public void setMessageIndex(int messageIndex) {
        // original NetLib4Games spec defines Message Index as unsigned long,
        // but we are limited to signed int for array usage.
        // store the int index as the least significant bits of a long value for compatibility.
        buffer.putInt(MESSAGE_INDEX_LEAST_SIGNIFICANT_POSITION, messageIndex);
    }

    public void setReliableIndex(int reliableIndex) {
        // original NetLib4Games spec defines Message Index as unsigned long,
        // but we are limited to signed int for array usage.
        // store the int index as the least significant bits of a long value for compatibility.
        buffer.putInt(RELIABLE_INDEX_LEAST_SIGNIFICANT_POSITION, reliableIndex);
    }

    public void setPacketCount(int packetCount) {
        buffer.putInt(PACKET_COUNT_POSITION, packetCount);
    }

    public void setPacketIndex(int packetIndex) {
        buffer.putInt(PACKET_INDEX_POSITION, packetIndex);
    }

    public void setTickCount(long tickCount) {
        buffer.putLong(TICK_COUNT_POSITION, tickCount);
    }
}