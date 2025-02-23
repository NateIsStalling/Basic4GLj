package com.basic4gl.library.netlib4games;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Layer 1 network packet header
 */
public class NetPacketHeaderL1 {
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN; // for cross-platform/backwards compatibility
    private static final int FLAGS_BYTES = 1;

    /**
     * ID size in bytes
     *
     * NetLib4Games specifies unsigned long (32-bit) value;
     * Java port only supports signed, positive values for array index usage.
     */
    private static final int ID_BYTES = 4;

    public static final int SIZE = FLAGS_BYTES + ID_BYTES;

    private static final int FLAGS_POSITION = 0;
    private static final int ID_POSITION = FLAGS_POSITION + FLAGS_BYTES;

    private final ByteBuffer buffer;

    public NetPacketHeaderL1() {
        buffer = ByteBuffer.wrap(new byte[SIZE])
                .order(BYTE_ORDER);
    }
    public NetPacketHeaderL1(byte[] buffer) {
        if (buffer.length < SIZE) {
            throw new IllegalArgumentException("Buffer size too small");
        }
        this.buffer = ByteBuffer.wrap(buffer)
                .order(BYTE_ORDER);
    }

    public static NetPacketHeaderL1 with(byte[] buffer) {
        return new NetPacketHeaderL1(buffer);
    }

    public byte[] getBuffer() {
        return buffer.array();
    }

    public byte getFlags() {
        return buffer.get(FLAGS_POSITION);
    }

    public void setFlags(byte flags) {
        buffer.put(FLAGS_POSITION, flags);
    }

    public int getId() {
        int value = buffer.getInt(ID_POSITION);
        // Check for unsigned values which are not supported in this version of NetLib4Games
        if (value < 0) {
            throw new RuntimeException("Unsupported network protocol");
        }

        return value;
    }

    public void setId(int id) {
        // Check for unsigned values which are not supported in this version of NetLib4Games
        if (id < 0) {
            throw new RuntimeException("Unsupported network protocol");
        }

        buffer.putInt(ID_POSITION, id);
    }

}
