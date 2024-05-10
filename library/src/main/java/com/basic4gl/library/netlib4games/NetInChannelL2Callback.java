package com.basic4gl.library.netlib4games;

/**
 * Layer 2 incoming network channel.
 */
public interface NetInChannelL2Callback {
    void QueueMessage (NetMessageL2 msg);
    void RegisterTickCountDifference (long difference);
    void RequestWakeup (long msec);
}
