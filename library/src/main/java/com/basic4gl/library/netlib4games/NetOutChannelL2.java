package com.basic4gl.library.netlib4games;


/**
 * Layer 2 outgoing network channel.
 */
public class NetOutChannelL2 extends HasErrorState {
    int				m_channel;
    boolean			m_ordered;
    int m_messageIndex,
            m_reliableIndex;
    public NetOutChannelL2 (int channel, boolean ordered)
     {
         m_channel	=	(channel);
                 m_ordered	=	(ordered);
                 m_messageIndex=	0;
                 m_reliableIndex = 0;
    }
    @Deprecated
    public void dispose() {

    }

    // Member access
    public int				Channel ()			{ return m_channel; }
    public boolean			Ordered ()			{ return m_ordered; }
    public int	MessageIndex ()		{ return m_messageIndex; }
    public int	ReliableIndex ()	{ return m_reliableIndex; }

    public void Send (
            NetConL1		connection,
		    byte[] data,
            int				size,
            boolean			reliable,
            boolean			smoothed,
            long	tickCount) {

    }
}
