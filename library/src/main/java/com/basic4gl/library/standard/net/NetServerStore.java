package com.basic4gl.library.standard.net;

import com.basic4gl.library.netlib4games.udp.NetListenLowUDP;
import com.basic4gl.runtime.util.PointerResourceStore;

/**
 * Stores NetListenLow network servers.
 */
public class NetServerStore extends PointerResourceStore<NetListenLowUDP> {
	@Override
	public void free(int index) {
		closeAtIndex(index);
		super.free(index);
	}

	@Override
	public void deleteElement(int index) {
		closeAtIndex(index);
		super.deleteElement(index);
	}

	@Override
	public void setValue(int index, NetListenLowUDP value) {
		closeAtIndex(index);
		super.setValue(index, value);
	}

	@Override
	public void remove(int index) {
		closeAtIndex(index);
		super.remove(index);
	}

	private void closeAtIndex(int index) {
		NetListenLowUDP value = getValueAt(index);
		if (value != null) {
			value.dispose();
		}
	}
}
