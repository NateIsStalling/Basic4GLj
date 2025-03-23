package com.basic4gl.library.netlib4games;

/**
 * Static network settings for layer 2. Must be set before the NetConL2
 * connection is created. (Defaults will be used if not explicitly supplied.)
 */
public class NetSettingsStaticL2 {
  // NetSettingsStaticL2 defaults
  static final int NET_L2BUFFERPACKETS = 1024;

  /**
   * Size of Layer 2 buffer in packets.
   * Once the buffer reaches this size, unreliable messages will be removed
   * from the oldest first. If there are no unreliable messages in the buffer
   * then we have a network error.
   * Due to the nature of unreliable packets, we expect broken unreliable
   * packets to persist in the buffer until this mechanism deletes them.
   */
  long maxBufferPackets;

  /**
   * Layer one settings. (Every NetConL2 contains an internal NetConL1.
   * these are the settings specific to that layer.)
   */
  NetSettingsStaticL1 l1Settings;

  NetSettingsStaticL2() {
    // Use default settings
    maxBufferPackets = NET_L2BUFFERPACKETS;
  }

  NetSettingsStaticL2(NetSettingsStaticL2 s) {
    maxBufferPackets = s.maxBufferPackets;
    l1Settings = s.l1Settings;
  }
}
