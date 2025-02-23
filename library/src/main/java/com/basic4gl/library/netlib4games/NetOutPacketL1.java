package com.basic4gl.library.netlib4games;


import com.basic4gl.library.netlib4games.internal.Assert;

import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RELIABLE;
import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RESENT;
import static com.basic4gl.library.netlib4games.internal.Assert.assertTrue;

/**
 * Outgoing layer 1 network packet
 */
public class NetOutPacketL1 {
    /**
     * Time at which packet is due to be (re)sent
     */
    long due;
    long id;
    boolean reliable;
    boolean resent;
    NetSimplePacket packet;

    NetOutPacketL1(long due, NetSimplePacket packet) {
        this.due = due;
        this.packet = packet;
        Assert.assertTrue(this.packet != null);

        // Extract packet id
        Assert.assertTrue(this.packet.size >= NetPacketHeaderL1.SIZE);
        NetPacketHeaderL1 header = new NetPacketHeaderL1(this.packet.data);
        byte flags = header.getFlags();
        id = header.getId();
        reliable = (flags & NETL1_RELIABLE) != 0;
        resent = (flags & NETL1_RESENT) != 0;
    }

    public void dispose() {
        packet.dispose();
    }
}
