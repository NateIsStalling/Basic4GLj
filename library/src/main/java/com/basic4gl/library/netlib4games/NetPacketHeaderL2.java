package com.basic4gl.library.netlib4games;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetPacketHeaderL2 {
private static final ByteOrder BYTE_ORDER =
	ByteOrder.LITTLE_ENDIAN; // for cross-platform/backwards compatibility

private static final int CHANNEL_FLAGS_BYTES = 1;

/**
* Message Index size in bytes
* <p>
* NetLib4Games specifies unsigned long (32-bit) value
* Java port only supports signed, positive values for array index usage.
*/
private static final int MESSAGE_INDEX_BYTES = 4;

/**
* Reliable Index size in bytes
* <p>
* NetLib4Games specifies unsigned long (32-bit) value
* Java port only supports signed, positive values for array index usage.
*/
private static final int RELIABLE_INDEX_BYTES = 4;

private static final int PACKET_COUNT_BYTES = 2;
private static final int PACKET_INDEX_BYTES = 2;

private static final int TICK_COUNT_BYTES = 4;

public static final int SIZE =
	CHANNEL_FLAGS_BYTES
		+ MESSAGE_INDEX_BYTES
		+ RELIABLE_INDEX_BYTES
		+ PACKET_COUNT_BYTES
		+ PACKET_INDEX_BYTES
		+ TICK_COUNT_BYTES;

private static final int CHANNEL_FLAGS_POSITION = 0;
private static final int MESSAGE_INDEX_POSITION = CHANNEL_FLAGS_POSITION + CHANNEL_FLAGS_BYTES;
private static final int RELIABLE_INDEX_POSITION = MESSAGE_INDEX_POSITION + MESSAGE_INDEX_BYTES;
private static final int PACKET_COUNT_POSITION = RELIABLE_INDEX_POSITION + RELIABLE_INDEX_BYTES;
private static final int PACKET_INDEX_POSITION = PACKET_COUNT_POSITION + PACKET_COUNT_BYTES;
private static final int TICK_COUNT_POSITION = PACKET_INDEX_POSITION + PACKET_INDEX_BYTES;

private final ByteBuffer buffer;

public NetPacketHeaderL2() {
	this.buffer = ByteBuffer.wrap(new byte[SIZE]).order(BYTE_ORDER);
}

public NetPacketHeaderL2(byte[] buffer) {
	this.buffer = ByteBuffer.wrap(buffer).order(BYTE_ORDER);
}

public byte[] getBuffer() {
	return buffer.array();
}

public byte getChannelFlags() {
	return buffer.get(CHANNEL_FLAGS_POSITION);
}

public int getMessageIndex() {
	int value = buffer.getInt(MESSAGE_INDEX_POSITION);

	// Check for unsigned values which are not supported in this version
	if (value < 0) {
	throw new RuntimeException("Unsupported network protocol");
	}

	return value;
}

public int getReliableIndex() {
	int value = buffer.getInt(RELIABLE_INDEX_POSITION);

	// Check for unsigned values which are not supported in this version
	if (value < 0) {
	throw new RuntimeException("Unsupported network protocol");
	}

	return value;
}

public int getPacketCount() {
	return Short.toUnsignedInt(buffer.getShort(PACKET_COUNT_POSITION));
}

public int getPacketIndex() {
	return Short.toUnsignedInt(buffer.getShort(PACKET_INDEX_POSITION));
}

/**
* @return Sender's TickCount at time of sending
*/
public long getTickCount() {
	// NOTE: original source is compiled as 32-bit application where unsigned long is 4 bytes
	return Integer.toUnsignedLong(buffer.getInt(TICK_COUNT_POSITION));
}

public void setChannelFlags(byte channelFlags) {
	buffer.put(CHANNEL_FLAGS_POSITION, channelFlags);
}

public void setMessageIndex(int messageIndex) {
	// original NetLib4Games spec defines Message Index as unsigned long,
	// but we are limited to signed int for array usage.
	buffer.putInt(MESSAGE_INDEX_POSITION, messageIndex);
}

public void setReliableIndex(int reliableIndex) {
	// original NetLib4Games spec defines Message Index as unsigned long,
	// but we are limited to signed int for array usage.
	buffer.putInt(RELIABLE_INDEX_POSITION, reliableIndex);
}

public void setPacketCount(int packetCount) {
	// Keeps only the lower 16 bits (unsigned short)
	buffer.putChar(PACKET_COUNT_POSITION, (char) packetCount);
}

public void setPacketIndex(int packetIndex) {
	// Keeps only the lower 16 bits (unsigned short)
	buffer.putChar(PACKET_INDEX_POSITION, (char) packetIndex);
}

public void setTickCount(long tickCount) {
	int truncatedValue = (int) (tickCount & 0xFFFFFFFFL); // Keep lower 4 bytes
	buffer.putInt(TICK_COUNT_POSITION, truncatedValue);
}
}
