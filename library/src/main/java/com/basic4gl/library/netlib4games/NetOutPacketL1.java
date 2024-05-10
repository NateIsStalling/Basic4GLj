package com.basic4gl.library.netlib4games;


import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RELIABLE;
import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RESENT;

/**
 * Outgoing layer 1 network packet
 */
public class NetOutPacketL1 {
    long	due;		// Time at which packet is due to be (re)sent
    long				id;
    boolean			reliable;
    boolean           resent;
    NetSimplePacket	packet;

    NetOutPacketL1 (long _due, NetSimplePacket _packet) {
    due = _due;
    packet = _packet;
        assert (packet != null);

        // Extract packet id
        assert (packet.size >= NetPacketHeaderL1.SIZE);
        NetPacketHeaderL1 header = new NetPacketHeaderL1(packet.data);
        byte flags = header.getFlags();
        id			= header.getId();
        reliable	= (flags & NETL1_RELIABLE) != 0;
        resent	    = (flags & NETL1_RESENT) != 0;
    }
	public void dispose () {
        packet.dispose();
    }
}
