package com.basic4gl.desktop.util;

import com.basic4gl.compiler.util.ISourceFile;
import java.io.File;
import java.io.FileInputStream;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Disk file implementation of ISourceFile
 */
public class DiskFile implements ISourceFile {
  FileInputStream mFile;
  FileChannel mChannel;

  ByteBuffer mBuffer;
  String mFilename;

  long mSize;
  int lineNo;

  public DiskFile(String filename) {
    mFilename = filename;
    if (new File(filename).exists()) {
      try {

        mFile = new FileInputStream(mFilename);
        mChannel = mFile.getChannel();
        mSize = mChannel.size();

        mBuffer = ByteBuffer.allocate((int) mSize);
        mChannel.read(mBuffer);

        mChannel.close();
        mFile.close();

        mBuffer.rewind();

      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        mChannel = null;
        mFile = null;
      }
    } else {
      mChannel = null;
      mFile = null;
    }
    lineNo = 0;
  }

  public boolean Fail() {
    return (mFile == null);
  }

  // ISourceFile methods
  @Override
  public String getFilename() {
    return mFilename;
  }

  @Override
  public int getLineNumber() {
    return lineNo;
  }

  @Override
  public String getNextLine() {
    if (!isEof()) {
      lineNo++;
      byte[] lineBuffer = new byte[65536];
      boolean crFlag = false; // Carriage return flag
      byte b = 0;
      int i = 0;

      // Parse mBuffer until it reaches a new line character
      do {
        b = mBuffer.get();
        crFlag = (b == '\r'); // Set return character flag

        if (b != '\n' && !crFlag) {
          lineBuffer[i] = b;
        }

        // Check if return character is followed by a new line character
        if (crFlag) {
          b = mBuffer.get();
          // If return character was not followed by a new line char or null char
          // then reset the buffer's position to before the last byte read
          if (b != '\n' && b != 0) {
            mBuffer.position(mBuffer.position() - 1);
          }
        }
        i++;
      } while (mBuffer.hasRemaining() && b != '\n' && b != 0 && !crFlag);

      return new String(Arrays.copyOfRange(lineBuffer, 0, i));
    } else {
      return "";
    }
  }

  @Override
  public boolean isEof() {
    return Fail() || !mBuffer.hasRemaining();
  }

  @Override
  public void release() {
    try {

      this.finalize();
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
