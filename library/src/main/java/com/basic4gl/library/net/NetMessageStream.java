package com.basic4gl.library.net;

import com.basic4gl.lib.util.FileStream;
import com.basic4gl.library.netlib4games.NetConL2;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.basic4gl.library.netlib4games.NetLayer2.NETL2_MAXCHANNELS;

/**
 * A network message wrapped up as an input or output stream.
 * By inheriting from FileStream, we can plug into the file I/O mechanism and
 * allow users to use the same code as for accessing files to read/write network
 * messages.
 */
public class NetMessageStream extends FileStream {
    private NetConnectionStore parent;
    public int connectionHandle;   // Note: We use connection handles rather than
    // references, so that we can fail gracefully
    // if the connection is deleted before we
    // have finalised the message.
    public int channel;
    public boolean reliable, smoothed;

    public NetMessageStream(
            NetConnectionStore parent,
            int _connectionHandle,
            int _channel,
            boolean _reliable,
            boolean _smoothed,
            InputStream _in) {

        super(_in);
        this.parent = parent;
        this.connectionHandle = _connectionHandle;
        this.channel = _channel;
        this.reliable = _reliable;
        this.smoothed = _smoothed;
    }

    public NetMessageStream(
            NetConnectionStore parent,
            int _connectionHandle,
            int _channel,
            boolean _reliable,
            boolean _smoothed,
            OutputStream _out) {

        super(_out);
        this.connectionHandle = _connectionHandle;
        this.channel = _channel;
        this.reliable = _reliable;
        this.smoothed = _smoothed;
    }

    //virtual ~NetMessageStream ();

    @Override
    public void close() {
        if (out != null && parent.isIndexStored(connectionHandle)) {

            // Send pending packet
            ByteArrayOutputStream stream = (ByteArrayOutputStream) out;                // (Net messages are always string streams)
            NetConL2 connection = parent.getValueAt(connectionHandle);

            String message = stream.toString(StandardCharsets.UTF_8);

            if (channel >= 0 && channel < NETL2_MAXCHANNELS && message != null)
                connection.Send(
                        stream.toByteArray(),
                        message.length(),
                    channel,
                    reliable,
                    smoothed);
        }

        parent = null;

        super.close();
    }
}