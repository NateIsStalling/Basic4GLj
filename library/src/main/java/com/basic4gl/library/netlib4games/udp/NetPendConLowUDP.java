package com.basic4gl.library.netlib4games.udp;

import com.basic4gl.library.netlib4games.NetSimplePacket;
import com.basic4gl.library.netlib4games.internal.Assert;
import java.net.InetSocketAddress;

/**
 * A pending NetConLowUDP connection.
 */
public class NetPendConLowUDP {
  private final InetSocketAddress address;

  private final String requestString;

  private final NetSimplePacket packet;

  public NetPendConLowUDP(InetSocketAddress address, String requestString, NetSimplePacket packet) {
    Assert.assertTrue(packet != null);
    this.address = address;
    this.requestString = requestString;
    this.packet = packet;
  }

  public void dispose() {
    packet.dispose();
  }

  /**
   * Sender address
   */
  public InetSocketAddress getAddress() {
    return address;
  }

  /**
   * Connection request string
   */
  public String getRequestString() {
    return requestString;
  }

  /**
   * Initial packet
   */
  public NetSimplePacket getPacket() {
    return packet;
  }
}
