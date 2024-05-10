package com.basic4gl.library.net;

import com.basic4gl.library.netlib4games.NetConL2;
import com.basic4gl.library.netlib4games.udp.NetListenLowUDP;
import com.basic4gl.runtime.util.PointerResourceStore;

/**
 * Stores NetConL2 network connections.
 */
public class NetConnectionStore extends PointerResourceStore<NetConL2> {
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
    public void setValue(int index, NetConL2 value) {
        closeAtIndex(index);
        super.setValue(index, value);
    }

    @Override
    public void remove(int index) {
        closeAtIndex(index);
        super.remove(index);
    }

    private void closeAtIndex(int index) {
        NetConL2 value = getValueAt(index);
        if (value != null) {
            value.dispose();
        }
    }
}
