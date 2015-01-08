package com.basic4gl.compiler;

/*  Created 2-Jun-07: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

 Disk file implementation of ISourceFile and ISourceFileServer.
 */

import java.io.FileInputStream;
import java.nio.*;
import java.nio.channels.FileChannel;

import com.basic4gl.compiler.util.ISourceFile;

////////////////////////////////////////////////////////////////////////////////
//  DiskFile
//
/// Disk file implementation of ISourceFile
public class DiskFile extends ISourceFile {
	FileInputStream mFile;
	FileChannel mChannel;

	ByteBuffer mBuffer;
	String mFilename;

	long mSize;
	int lineNo;

	public DiskFile(String filename) {
		mFilename = filename;
		try {
			mFile = new FileInputStream(mFilename);
			mChannel = mFile.getChannel();
			mSize = mChannel.size();

			mBuffer = ByteBuffer.allocate((int) mSize);
			mChannel.read(mBuffer);

			mChannel.close();
			mFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public String Filename() {
		return mFilename;
	}

	@Override
	public int LineNumber() {
		return lineNo;
	}

	@Override
	public String GetNextLine() {
		if (!Eof()) {
			lineNo++;
			byte[] lineBuffer = new byte[65536];
			boolean crFlag = false; //Carriage return flag
			byte b = 0;
			int i = 0;
			
			//Parse mBuffer until it reaches a new line character
			do{
				b = mBuffer.get();
				crFlag = (b == '\r'); //Set return character flag
				
				if (b != '\n' && !crFlag)
					lineBuffer[i] = b;				
								
				//Check if return character is followed by a new line character 
				if (crFlag){
					b = mBuffer.get();
					//If return character was not followed by a new line char
					//then reset the buffer's position to before the last byte read
					if (b != '\n')
						mBuffer.position(mBuffer.position() - 1);
				}
				i++;
			}while (mBuffer.hasRemaining() && b != '\n' && !crFlag);
			
			return String.valueOf(lineBuffer);
		} else
			return "";
	}

	@Override
	public boolean Eof() {
		return Fail() || !mBuffer.hasRemaining();
	}

	@Override
	public void Release() {
		try {

			this.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
