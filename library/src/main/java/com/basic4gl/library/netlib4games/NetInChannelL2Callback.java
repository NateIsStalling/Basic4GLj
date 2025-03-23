package com.basic4gl.library.netlib4games;

/**
 * Layer 2 incoming network channel.
 */
public interface NetInChannelL2Callback {
void queueMessage(NetMessageL2 msg);

void registerTickCountDifference(long difference);

void requestWakeup(long msec);
}
