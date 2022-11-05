package com.basic4gl.runtime.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class Streaming {

	// Inline streaming functions.
	// Should be used by streaming routines where appropriate, as this
	// provides a central point for dealing with endian issues (should the
	// compiler be ported to a big-endian machine).
	// Long and short integers are stored in little-endian format within the
	// stream.
	public static void WriteLong (DataOutputStream stream, long l) throws IOException {
		//stream.write(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(l).array());
		stream.writeLong(l);
	}
	public static void WriteShort (DataOutputStream stream, short s) throws IOException {
		//stream.write(ByteBuffer.allocate(Short.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(s).array());
		stream.writeShort(s);
	}
	public static void WriteByte (DataOutputStream stream, byte b) throws IOException {
		stream.write(b);
	}
	public static void WriteFloat(DataOutputStream stream, float f) throws IOException {
		//stream.write(ByteBuffer.allocate(Float.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putFloat(f).array());
		stream.writeFloat(f);
	}
	public static long ReadLong (DataInputStream stream) throws IOException {
		long l;
		l = stream.readLong();
	    return l;
	}
	public static short ReadShort (DataInputStream stream) throws IOException {
		short s;
	    s = stream.readShort();
	    return s;
	}
	public static byte ReadByte (DataInputStream stream)  throws IOException{
	    byte b;
	    b = stream.readByte();
	    return b;
	}
	public static float ReadFloat(DataInputStream stream) throws IOException {
	    float f;
	    f = stream.readFloat();
	    return f;
	}
	public static void WriteString (DataOutputStream stream, String s) throws IOException {
	    stream.writeInt(s.length());
		stream.write(s.getBytes(Charset.forName("UTF-8")));
	    //stream.writeBytes(s);
	}
	public static String ReadString (DataInputStream stream) throws IOException {

	    // Get length of string
	    int length = stream.readInt();// * Character.SIZE / Byte.SIZE;
		byte[] buffer = new byte[length];
		int bytesRead = stream.read(buffer);
		String s = new String(buffer, 0, bytesRead, "UTF-8");
		/*char[] buffer = new char[length];
		String s = "";

		InputStreamReader is = new InputStreamReader(stream, "UTF-8");
		StringBuilder sb=new StringBuilder();
		//BufferedReader br = new BufferedReader(is);
		//br.read(buffer, 0, length);
		int l = is.read(buffer, 0, length);
		sb.append(buffer);
		s = sb.toString();*/
		//byte[] b = ByteBuffer.allocate(length).array();
		//stream.read(b);
		//s = String.valueOf(b);
		/*
	    // If longer than 4096 characters, read in chunks
	    byte[] b =  new byte[4097];
	    while (length > 4096) {
	        stream.read(b, 0, 4096);
	        b [4096] = 0;              // Terminate string
	        s = s + b;
	        length -= 4096;
	    }

	    // Read < 4096 bit
	    stream.read(b, 0, (int) length);
	    b [(int)length] = 0;                // Terminate string
	    s = s + stream;
*/
	    return s;
	}
}
