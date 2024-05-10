package com.basic4gl.library.netlib4games;

/**
 * Buffered incoming layer 1 network packet
 */
public class NetInPacketL1 {
    boolean           resent;     // True if packet was resent
    NetSimplePacket packet;     // Packet data
    NetInPacketL1(int size, boolean _resent) {
        packet = new NetSimplePacket(size);
        resent = _resent;
    }

    public void dispose() {
        packet.dispose();
    }
}
