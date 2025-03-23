package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetL1Type.l1Connect;
import static com.basic4gl.library.netlib4games.NetLayer1.MAX_CON_REQ_SIZE;

import com.basic4gl.library.netlib4games.internal.Assert;

public class NetConReqValidatorL1 extends NetConReqValidator {
	public boolean isConnectionRequest(NetSimplePacket packet, String[] requestStringBuffer) {
		Assert.assertTrue(packet != null);

		// Is packet large enough?
		if (packet.size >= NetPacketHeaderL1.SIZE) {

			// Map to header
			NetPacketHeaderL1 header = new NetPacketHeaderL1(packet.data);

			// Check type
			if (NetL1Type.fromInteger(header.getFlags()) == l1Connect) {

				// Extract request string
				byte[] buf = new byte[MAX_CON_REQ_SIZE];
				int size = packet.size - NetPacketHeaderL1.SIZE;
				if (size >= MAX_CON_REQ_SIZE) {
					size = MAX_CON_REQ_SIZE - 1;
				}
				if (size > 0) {
					System.arraycopy(packet.data, NetPacketHeaderL1.SIZE, buf, 0, size);
				}
				buf[size] = 0;
				requestStringBuffer[0] = String.valueOf(buf);

				return true;
			}
		}

		return false;
	}
}
