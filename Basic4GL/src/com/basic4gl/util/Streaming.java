package com.basic4gl.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class Streaming {

	// Inline streaming functions.
	// Should be used by streaming routines where appropriate, as this
	// provides a central point for dealing with endian issues (should the
	// compiler be ported to a big-endian machine).
	// Long and short integers are stored in little-endian format within the
	// stream.
	public static void WriteLong (ByteBuffer buffer, long l) throws IOException {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(l);
	}
	public static void WriteShort (ByteBuffer buffer, short s) throws IOException {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		buffer.putShort (s);
	}
	public static void WriteByte (ByteBuffer buffer, byte b) throws IOException {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		buffer.put (b);
	}
	public static void WriteFloat(ByteBuffer buffer, float f) throws IOException {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		buffer.putFloat(f);
	}
	public static long ReadLong (ByteBuffer buffer) throws IOException {
	    long l;
	    buffer.order( ByteOrder.LITTLE_ENDIAN);
	    l = buffer.getLong();
	    return l;
	}
	public static short ReadShort (ByteBuffer buffer) throws IOException {
	    short s;
	    buffer.order( ByteOrder.LITTLE_ENDIAN);
	    s = buffer.getShort();
	    return s;
	}
	public static byte ReadByte (ByteBuffer buffer)  throws IOException{
	    byte b;
	    buffer.order( ByteOrder.LITTLE_ENDIAN);
	    b = buffer.get();
	    return b;
	}
	public static float ReadFloat(ByteBuffer buffer) throws IOException {
	    float f;
	    buffer.order( ByteOrder.LITTLE_ENDIAN);
	    f = buffer.getFloat();
	    return f;
	}
	public static void WriteString (ByteBuffer buffer, String s) throws IOException {
	    buffer.order( ByteOrder.LITTLE_ENDIAN);
	    buffer.putLong(s.length());
	    buffer.put(s.getBytes());
	}
	public static String ReadString (ByteBuffer buffer) throws IOException {
		buffer.order( ByteOrder.LITTLE_ENDIAN);
		    
	    // Get length of string
	    long length = buffer.getLong();
	    String s = "";

	    // If longer than 4096 characters, read in chunks
	    byte[] b =  new byte[4097];
	    while (length > 4096) {
	        buffer.get(b, 0, 4096);
	        b [4096] = 0;              // Terminate string
	        s = s + b;
	        length -= 4096;
	    }

	    // Read < 4096 bit
	    buffer.get(b, 0, (int)length);
	    b [(int)length] = 0;                // Terminate string
	    s = s + buffer;

	    return s;
	}

}
