package com.basic4gl.library.netlib4games;

/**
 * NetLib4Games layer 1.
 * Introduces basic connection concepts, plus reliable packets.
 * Specifically:
 * - Handshaking
 * - Clean disconnects
 * - Keep alives
 * - Timeouts
 * - Packet confirmation
 * - General timing
 */
public class NetLayer1 {

//	Constants/flags/bitmasks

public static final int WAITEXTENSION = 2;
public static final int MAX_CON_REQ_SIZE = 4096;

public static final int NETL1_RELIABLE = 0x80;

/**
* Set if this packet is a resend of a previous packet
*/
public static final int NETL1_RESENT = 0x40;

public static final int NETL1_TYPEMASK = 0x3f;

public static NetL1Type getNetLayerType(int x) {
	return NetL1Type.fromInteger(x & NETL1_TYPEMASK);
}

public static long getTickCount() {
	return System.currentTimeMillis();
}

public static String getDescription(NetPacketHeaderL1 header) {
	byte flags = header.getFlags();
	boolean reliable = (flags & NETL1_RELIABLE) != 0;
	boolean resent = (flags & NETL1_RESENT) != 0;
	NetL1Type type = getNetLayerType(flags);
	int id = header.getId();
	String typeStr;
	switch (type) {
	case l1User:
		typeStr = "User";
		break;
	case l1KeepAlive:
		typeStr = "KeepAlive";
		break;
	case l1Confirm:
		typeStr = "Confirm";
		break;
	case l1Connect:
		typeStr = "Connect";
		break;
	case l1Accept:
		typeStr = "Accept";
		break;
	case l1Disconnect:
		typeStr = "Disconnect";
		break;
	default:
		typeStr = "UNKNOWN!?!";
	}
	return (reliable ? "Reliable, " : "Unreliable, ")
		+ (resent ? "Resent, " : "")
		+ typeStr
		+ ", id: "
		+ id;
}

static String getDescription(NetSimplePacket packet) {
	return getDescription(new NetPacketHeaderL1(packet.data));
}
}
