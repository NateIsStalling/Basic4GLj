package com.basic4gl.library.netlib4games;

public class NetLayer2 {

public static final int NETL2_RELIABLE	=	0x80;
public static final int NETL2_SMOOTHED	=	0x40;
public static final int NETL2_ORDERED	=	0x20;
public static final int NETL2_CHANNELMASK=	0x1f;
public static final int NETL2_MAXCHANNELS	=32;
    public static int getChannel(int x) {
        return x & NETL2_CHANNELMASK;
    }

    static String Desc (NetPacketHeaderL2 header) {
        byte channelFlags = header.getChannelFlags();
        int		channel		= getChannel (channelFlags);
        boolean	reliable	= (channelFlags & NETL2_RELIABLE) != 0,
                smoothed	= (channelFlags & NETL2_SMOOTHED) != 0,
                ordered		= (channelFlags & NETL2_ORDERED) != 0;
        return "Channel " + channel
                + ", Packet " + (header.getPacketIndex()) + " of " +  (header.getPacketCount())
                + (ordered  ? ", Ordered "	: ", Unordered")
                + (reliable ? ", Reliable " : ", Unreliable")
                + (smoothed ? ", Smoothed " : ", Unsmoothed")
                + ", # " + (header.getMessageIndex())
                + ", Reliable # " + (header.getReliableIndex());
    }
}
