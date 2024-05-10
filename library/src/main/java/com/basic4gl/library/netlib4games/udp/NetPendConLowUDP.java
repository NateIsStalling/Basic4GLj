package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.NetSimplePacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * A pending NetConLowUDP connection.
 */
public class NetPendConLowUDP {
    /// Sender address
    InetSocketAddress addr;

    /// Connection request string
    String		requestString;

    /// Initial packet
    NetSimplePacket packet;

    public NetPendConLowUDP (
            InetSocketAddress _addr,
            String _requestString,
            NetSimplePacket _packet) {
        assert (packet != null);
        addr = _addr;
        requestString = _requestString;
        packet = _packet;
    }
	public void dispose() {
        packet.dispose();
    }
}
