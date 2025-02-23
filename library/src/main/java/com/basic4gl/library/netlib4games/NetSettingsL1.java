package com.basic4gl.library.netlib4games;

/**
 * Network settings for layer 1. Can be changed once the connection is active.
 */
public class NetSettingsL1 {
    // NetSettingsL1 defaults
    /**
     * Handshake timeout after 10 seconds inactivity
     */
    public static final int NET_HANDSHAKETIMEOUT_MILLIS = 10000;
    /**
     * Timeout after 30 seconds of inactivity
     */
    public static final int NET_TIMEOUT_MILLIS = 60000;
    /**
     * Keepalives sent every 10 seconds
     */
    public static final int NET_KEEPALIVE_MILLIS = 10000;
    /**
     * Reliable packets resent every .2 seconds (until confirmed)
     */
    public static final int NET_RELIABLERESEND_MILLIS = 200;

    /**
     * # of duplicate packets to send
     */
    public static final int NET_DUP = 1;

    /**
     *  Handshake timeout (ms).
     *  Allows a different (generally shorter) timeout period to be specified
     *  during the handshake sequence
     */
    public long handshakeTimeout;

    /**
     * General connection timeout (ms).
     * If no packets received within stated time, connection is dropped.
     */
    public long timeout;

    /**
     * Keepalive period (ms).
     * If no packets sent within stated time, a keep-alive is automatically sent.
     */
    public long keepAlive;

    /**
     * Period after which an unconfirmed reliable packet is resent (ms)
     */
    public long reliableResend;

    /**
     * # of duplicate packets to send
     */
    public long dup;

    public NetSettingsL1 () {

        // Use default settings
        handshakeTimeout	= NET_HANDSHAKETIMEOUT_MILLIS;
        timeout				= NET_TIMEOUT_MILLIS;
        keepAlive			= NET_KEEPALIVE_MILLIS;
        reliableResend		= NET_RELIABLERESEND_MILLIS;
        dup					= NET_DUP;
    }

    public NetSettingsL1 (final NetSettingsL1 s) {
        handshakeTimeout	= s.handshakeTimeout;
        timeout				= s.timeout;
        keepAlive			= s.keepAlive;
        reliableResend		= s.reliableResend;
        dup					= s.dup;
    }
}
