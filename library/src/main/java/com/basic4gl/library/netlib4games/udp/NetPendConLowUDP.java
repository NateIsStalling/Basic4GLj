package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.NetSimplePacket;
import com.basic4gl.library.netlib4games.internal.Assert;

import java.net.InetSocketAddress;

/**
 * A pending NetConLowUDP connection.
 */
public class NetPendConLowUDP {
    /**
     * Sender address
     */
    InetSocketAddress addr;

    /**
     * Connection request string
     */
    String requestString;

    /**
     * Initial packet
     */
    NetSimplePacket packet;

    public NetPendConLowUDP(
            InetSocketAddress addr,
            String requestString,
            NetSimplePacket packet) {
        Assert.assertTrue(packet != null);
        this.addr = addr;
        this.requestString = requestString;
        this.packet = packet;
    }

    public void dispose() {
        packet.dispose();
    }
}
