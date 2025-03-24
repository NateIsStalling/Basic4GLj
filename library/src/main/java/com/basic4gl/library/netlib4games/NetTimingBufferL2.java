package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Sorts and stores timing differences on recent messages.
 * The timing difference =
 * Sender.GetTickCount () at the time of sending the packet
 * -
 * Receiver.GetTickCount () at the time the packet is received.
 * <p>
 * The timing buffer is used to calculate when a message is considered early
 * or late.
 */
public class NetTimingBufferL2 {
    static final int NET_L2TIMINGBUFFERSIZE = 25;

    private final long[] receivedIndex = new long[NET_L2TIMINGBUFFERSIZE];
    private final long[] sortedIndex = new long[NET_L2TIMINGBUFFERSIZE];
    private int receivedPos;
    private boolean bufferFull;

    public NetTimingBufferL2() {
        bufferFull = false;
        receivedPos = 0;
    }

    private int getSortedPosition(long difference) {

        // Binary search for insert position
        int bottom = 0;
        int top = bufferFull ? NET_L2TIMINGBUFFERSIZE : receivedPos;

        while (top > bottom) {
            int mid = (top + bottom) >> 1;
            if (difference > sortedIndex[mid]) {
                bottom = mid + 1;
            } else {
                top = mid;
            }
        }
        return bottom;
    }

    public void clear() {
        bufferFull = false;
        receivedPos = 0;
    }

    public void logDifference(long difference) {

        // If buffer is full, then remove the oldest time
        int removePos;
        if (bufferFull) {
            removePos = getSortedPosition(receivedIndex[receivedPos]);
        } else {
            removePos = receivedPos;
        }

        // Find index to insert new time
        int insertPos = getSortedPosition(difference);

        // Move items over
        if (insertPos < removePos) {
            System.arraycopy(sortedIndex, insertPos + 1, sortedIndex, insertPos, removePos - insertPos);
        } else if (insertPos > removePos) {
            insertPos--;
            if (insertPos > removePos) {
                System.arraycopy(sortedIndex, removePos, sortedIndex, removePos + 1, insertPos - removePos);
            }
        }

        // Insert into indices
        sortedIndex[insertPos] = difference;
        receivedIndex[receivedPos] = difference;

        // Advanced received pointer
        receivedPos++;
        if (receivedPos >= NET_L2TIMINGBUFFERSIZE) {
            receivedPos = 0;
            bufferFull = true;
        }
    }

    public long getDifference(int i) {
        Assert.assertTrue(bufferFull);
        Assert.assertTrue(i < NET_L2TIMINGBUFFERSIZE);
        return sortedIndex[i];
    }

    public boolean isBufferFull() {
        return bufferFull;
    }
}
