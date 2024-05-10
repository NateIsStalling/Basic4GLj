package com.basic4gl.library.netlib4games;

/**
 * Sorts and stores timing differences on recent messages.
 * 	The timing difference =
 * 		Sender.GetTickCount () at the time of sending the packet
 * 		-
 * 		Receiver.GetTickCount () at the time the packet is received.
 * 
 *  The timing buffer is used to calculate when a message is considered early
 *  or late.
 */

public class NetTimingBufferL2 {
    static final int NET_L2TIMINGBUFFERSIZE = 25;

    long[]		m_receivedIndex = new long [NET_L2TIMINGBUFFERSIZE];
    long[]		m_sortedIndex = new long[NET_L2TIMINGBUFFERSIZE];
    int		m_receivedPos;
    boolean	m_bufferFull;

    int	SortedPos (long difference) {

        // Binary search for insert position
        int bottom = 0;
        int top = m_bufferFull ? NET_L2TIMINGBUFFERSIZE : m_receivedPos;

        while (top > bottom) {
            int mid = (top + bottom) >> 1;
            if (difference > m_sortedIndex [mid]) {
                bottom = mid + 1;
            } else {
                top = mid;
            }
        }
        return bottom;
    }


    public NetTimingBufferL2 () {
        m_bufferFull =false;
        m_receivedPos =0;
         }

    public void Clear () {
        m_bufferFull = false;
        m_receivedPos = 0;
    }
    public void LogDifference (long difference) {

        // If buffer is full, then remove the oldest time
        int removePos;
        if (m_bufferFull) {
            removePos = SortedPos (m_receivedIndex [m_receivedPos]);
        } else {
            removePos = m_receivedPos;
        }

        // Find index to insert new time
        int insertPos = SortedPos (difference);

        // Move items over
        if (insertPos < removePos) {
            System.arraycopy(m_sortedIndex, insertPos + 1, m_sortedIndex, insertPos, removePos - insertPos);
        }
        else if (insertPos > removePos) {
            insertPos--;
            if (insertPos > removePos) {
                System.arraycopy(m_sortedIndex, removePos, m_sortedIndex, removePos + 1, insertPos - removePos);
            }
        }

        // Insert into indices
        m_sortedIndex [insertPos]		= difference;
        m_receivedIndex [m_receivedPos] = difference;

        // Advanced received pointer
        m_receivedPos++;
        if (m_receivedPos >= NET_L2TIMINGBUFFERSIZE) {
            m_receivedPos = 0;
            m_bufferFull = true;
        }
    }
    public long Difference (int i) {
        assert (m_bufferFull);
        assert (i < NET_L2TIMINGBUFFERSIZE);
        return m_sortedIndex [i];
    }
    public boolean BufferFull () {
        return m_bufferFull;
    }
}
