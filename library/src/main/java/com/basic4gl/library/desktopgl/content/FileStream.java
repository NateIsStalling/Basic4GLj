package com.basic4gl.library.desktopgl.content;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * Created by Nate on 11/17/2015.
 */
public class FileStream {
    public InputStream in;
    public OutputStream out;

    public FileStream() {
        this.in = null;
        this.out = null;
    }

    public FileStream(InputStream in) {
        this.in = in;
        this.out = null;
    }

    public FileStream(OutputStream out) {
        this.in = null;
        this.out = out;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FileStream)) {
            return false;
        }
        FileStream s = (FileStream) other;
        return Objects.equals(in, s.in) && Objects.equals(out, s.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(in, out);
    }

    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEof() throws IOException {
        if (in == null) {
            return true;
        }

        if (in instanceof FileInputStream file) {
            FileChannel channel = file.getChannel();
            return channel.position() >= channel.size();
        }

        return in.available() <= 0;
    }
}
