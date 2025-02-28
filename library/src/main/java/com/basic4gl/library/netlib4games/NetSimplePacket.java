package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Very simple network packet.
 * This is just a helper object. The network implementation may choose to
 * use it or ignore it use their own format internally.
 */
public class NetSimplePacket {
    public byte[] data;
    public int size;

    public NetSimplePacket(int size) {
        this.size = size;
        if (this.size > 0) {
            data = new byte[this.size];
        } else {
            data = null;
        }
    }

    public NetSimplePacket(byte[] src, int size) {
        Assert.assertTrue(src != null || size == 0);
        this.size = size;
        if (this.size > 0) {
            data = new byte[this.size];
            System.arraycopy(src, 0, data, 0, this.size);
        } else {
            data = null;
        }
    }

    @Deprecated()
    public void dispose() {
        if (data != null) {
            data = null;
        }
    }
}
