package com.basic4gl.library.netlib4games;

public class NetSettingsStaticL1 {
// NetSettingsStaticL1 defaults
public static final int NET_PREFMINPACKETSIZE = 64;
public static final int NET_PREFMAXPACKETSIZE = 512;

/**
* Size of receive buffer in packets
*/
public static final int NET_MAXSENDBUFSIZE = 256;

/**
* Size of send buffer in packets
*/
public static final int NET_MAXRECVBUFSIZE = 256;

public static final int NET_RELIABLEBITBUFSIZE = 4096;
public static final int NET_UNRELIABLEBITBUFSIZE = 1024;

private long prefMinPacketSize;

private int prefMaxPacketSize;

private int maxSendBufSize;

private int maxRecvBufSize;

private int reliableBitBufSize;

private int unreliableBitBufSize;

NetSettingsStaticL1() {

	// Use default settings
	prefMinPacketSize = NET_PREFMINPACKETSIZE;
	prefMaxPacketSize = NET_PREFMAXPACKETSIZE;
	maxSendBufSize = NET_MAXSENDBUFSIZE;
	maxRecvBufSize = NET_MAXRECVBUFSIZE;
	reliableBitBufSize = NET_RELIABLEBITBUFSIZE;
	unreliableBitBufSize = NET_UNRELIABLEBITBUFSIZE;
}

NetSettingsStaticL1(NetSettingsStaticL1 s) {
	prefMinPacketSize = s.prefMinPacketSize;
	prefMaxPacketSize = s.prefMaxPacketSize;
	maxSendBufSize = s.maxSendBufSize;
	maxRecvBufSize = s.maxRecvBufSize;
	reliableBitBufSize = s.reliableBitBufSize;
	unreliableBitBufSize = s.unreliableBitBufSize;
}

/**
* Preferred minimum packet size.
* Used by packet merging code (when its implemented).
*/
public long getPrefMinPacketSize() {
	return prefMinPacketSize;
}

public void setPrefMinPacketSize(long prefMinPacketSize) {
	this.prefMinPacketSize = prefMinPacketSize;
}

/**
* Preferred maximum packet size.
* Used to restrict packet sizes.
*/
public int getPrefMaxPacketSize() {
	return prefMaxPacketSize;
}

public void setPrefMaxPacketSize(int prefMaxPacketSize) {
	this.prefMaxPacketSize = prefMaxPacketSize;
}

/**
* Size of send buffer in packets. Used to queue reliable packets until
* delivery is confirmed.
*/
public int getMaxSendBufSize() {
	return maxSendBufSize;
}

public void setMaxSendBufSize(int maxSendBufSize) {
	this.maxSendBufSize = maxSendBufSize;
}

/**
* Size of receive buffer in packets.
*/
public int getMaxRecvBufSize() {
	return maxRecvBufSize;
}

public void setMaxRecvBufSize(int maxRecvBufSize) {
	this.maxRecvBufSize = maxRecvBufSize;
}

/**
* Size of reliable bit buffer. (Used to track reliable packets).
*/
public int getReliableBitBufSize() {
	return reliableBitBufSize;
}

public void setReliableBitBufSize(int reliableBitBufSize) {
	this.reliableBitBufSize = reliableBitBufSize;
}

/**
* Size of unreliable bit buffer. (Used to track unreliable packets).
*/
public int getUnreliableBitBufSize() {
	return unreliableBitBufSize;
}

public void setUnreliableBitBufSize(int unreliableBitBufSize) {
	this.unreliableBitBufSize = unreliableBitBufSize;
}
}
