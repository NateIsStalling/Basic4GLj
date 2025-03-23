package com.basic4gl.library.netlib4games;

/**
 * Network settings for NetConL2. Unlike NetSettingsStaticL2, these can be
 * changed after the NetConL2 has been constructed.
 */
public class NetSettingsL2 {
	// NetSettingsL2 defaults
	static final int NET_L2SMOOTHINGPERCENTAGE = 80;

	/**
	 * Smoothing delay is adjusted so that this percentage of packets are
	 * considered to be on-time or early.
	 */
	public int smoothingPercentage;

	NetSettingsL2() {

		// Use default settings
		smoothingPercentage = NET_L2SMOOTHINGPERCENTAGE;
	}

	NetSettingsL2(NetSettingsL2 s) {
		smoothingPercentage = s.smoothingPercentage;
	}
}
