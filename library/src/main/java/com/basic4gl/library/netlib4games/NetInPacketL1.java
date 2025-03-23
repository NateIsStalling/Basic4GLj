package com.basic4gl.library.netlib4games;

/**
 * Buffered incoming layer 1 network packet
 */
public class NetInPacketL1 {
private boolean resent; // True if packet was resent
private NetSimplePacket packet; // Packet data

NetInPacketL1(int size, boolean resent) {
	packet = new NetSimplePacket(size);
	this.resent = resent;
}

public void dispose() {
	packet.dispose();
}

public NetSimplePacket getPacket() {
	return packet;
}

public boolean isResent() {
	return resent;
}

public void setResent(boolean resent) {
	this.resent = resent;
}
}
