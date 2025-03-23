package com.basic4gl.library.standard.net;

import static com.basic4gl.library.netlib4games.NetLayer2.NETL2_MAXCHANNELS;

import com.basic4gl.lib.util.FileStream;
import com.basic4gl.library.netlib4games.NetConL2;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A network message wrapped up as an input or output stream.
 * By inheriting from FileStream, we can plug into the file I/O mechanism and
 * allow users to use the same code as for accessing files to read/write network
 * messages.
 */
public class NetMessageStream extends FileStream {
    private NetConnectionStore parent;

    /**
     * Note: We use connection handles rather than
     * references, so that we can fail gracefully
     * if the connection is deleted before we
     * have finalised the message.
     */
    public int connectionHandle;

    public int channel;
    public boolean reliable;
    public boolean smoothed;

    public NetMessageStream(
            NetConnectionStore parent,
            int connectionHandle,
            int channel,
            boolean reliable,
            boolean smoothed,
            InputStream in) {
        super(in);
        if (parent == null) {
            throw new IllegalArgumentException("NetConnectionStore required");
        }
        this.parent = parent;
        this.connectionHandle = connectionHandle;
        this.channel = channel;
        this.reliable = reliable;
        this.smoothed = smoothed;
    }

    public NetMessageStream(
            NetConnectionStore parent,
            int connectionHandle,
            int channel,
            boolean reliable,
            boolean smoothed,
            OutputStream out) {

        super(out);
        if (parent == null) {
            throw new IllegalArgumentException("NetConnectionStore required");
        }
        this.parent = parent;
        this.connectionHandle = connectionHandle;
        this.channel = channel;
        this.reliable = reliable;
        this.smoothed = smoothed;
    }

    @Override
    public void close() {
        if (out != null && parent != null && parent.isIndexStored(connectionHandle)) {

            // Send pending packet
            ByteArrayOutputStream stream = (ByteArrayOutputStream) out; // (Net messages are always string streams)
            NetConL2 connection = parent.getValueAt(connectionHandle);

            String message = stream.toString(StandardCharsets.UTF_8);

            if (channel >= 0 && channel < NETL2_MAXCHANNELS && message != null) {
                connection.send(stream.toByteArray(), message.length(), channel, reliable, smoothed);
            }
        }

        parent = null;

        super.close();
    }
}
