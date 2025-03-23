package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RELIABLE;
import static com.basic4gl.library.netlib4games.NetLayer1.NETL1_RESENT;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Outgoing layer 1 network packet
 */
public class NetOutPacketL1 {
  private long due;

  private long id;
  private boolean reliable;
  private boolean resent;
  private NetSimplePacket packet;

  NetOutPacketL1(long due, NetSimplePacket packet) {
    Assert.assertTrue(packet != null);
    Assert.assertTrue(packet.size >= NetPacketHeaderL1.SIZE);

    this.due = due;
    this.packet = packet;

    // Extract packet id
    NetPacketHeaderL1 header = new NetPacketHeaderL1(this.packet.data);
    byte flags = header.getFlags();
    id = header.getId();
    reliable = (flags & NETL1_RELIABLE) != 0;
    resent = (flags & NETL1_RESENT) != 0;
  }

  public void dispose() {
    packet.dispose();
  }

  /**
   * Time at which packet is due to be (re)sent
   */
  public long getDue() {
    return due;
  }

  public void setDue(long due) {
    this.due = due;
  }


  public long getId() {
    return id;
  }

  public boolean isReliable() {
    return reliable;
  }

  public boolean isResent() {
    return resent;
  }

  public NetSimplePacket getPacket() {
    return packet;
  }
}
