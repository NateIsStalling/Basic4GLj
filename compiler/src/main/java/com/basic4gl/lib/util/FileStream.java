package com.basic4gl.lib.util;

import java.io.*;

/**
 * Created by Nate on 11/17/2015.
 */
public class FileStream {
    public FileInputStream in;
    public FileOutputStream  out;
    public FileStream () {
        this.in		= null;
        this.out	= null;
    }
    public FileStream (FileInputStream in) {
        this.in	    = in;
        this.out    = null;
    }
    public FileStream (FileOutputStream  out) {
        this.in	    = null;
        this.out	= out;
    }

    boolean equals(FileStream s){ return this.in.equals(s.in) && this.out.equals(s.out);}

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
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
