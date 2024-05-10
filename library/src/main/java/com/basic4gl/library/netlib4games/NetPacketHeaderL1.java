package com.basic4gl.library.netlib4games;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Layer 1 network packet header
 */
public class NetPacketHeaderL1 {
    private static final int FLAGS_BYTES = 1;

    /**
     * ID size in bytes
     *
     * NetLib4Games specifies unsigned long value
     * Java port only supports integer value of last 4 bytes
     * since array access is limited to int index values in Java
     */
    private static final int ID_BYTES = 8;
    private static final int ID_MOST_SIGNIFICANT_BYTES = 4;
    private static final int ID_LEAST_SIGNIFICANT_BYTES = 4;

    public static final int SIZE = FLAGS_BYTES + ID_BYTES;

    private static final int FLAGS_POSITION = 0;
    private static final int ID_POSITION = FLAGS_POSITION + FLAGS_BYTES;
    private static final int ID_MOST_SIGNIFICANT_POSITION = ID_POSITION;
    private static final int ID_LEAST_SIGNIFICANT_POSITION = ID_POSITION + ID_MOST_SIGNIFICANT_BYTES;

    private ByteBuffer buffer;

//    byte flags;
//    int id; // Porting note: this was unsigned long

    public NetPacketHeaderL1() {
        buffer = ByteBuffer.wrap(new byte[SIZE]);
    }
    public NetPacketHeaderL1(byte[] buffer) {
        if (buffer.length < SIZE) {
            throw new IllegalArgumentException("Buffer size too small");
        }
        this.buffer = ByteBuffer.wrap(buffer);
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
        // TODO Endianness is not determined; this should be evaluated for potential errors
        // NetLib4Games original source specifies unsigned long for Id,
        // but only the least significant bytes are supported in this version
        if (buffer.getInt(ID_MOST_SIGNIFICANT_POSITION) != 0) {
            throw new RuntimeException("Unsupported network protocol");
        }
        int value = buffer.getInt(ID_LEAST_SIGNIFICANT_POSITION);

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

        // TODO Endianness is not determined; this should be evaluated for potential errors
        // store signed int ID in least significant portion of header
        // to remain compatible with protocol versions that support unsigned long ID values
        buffer.putInt(ID_LEAST_SIGNIFICANT_POSITION, id);
    }

}
