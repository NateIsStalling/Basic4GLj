package com.basic4gl.library.standard;

import java.nio.ByteBuffer;

/**
 * Represents a loaded windows .wav file.
 */
class WindowsWav {
    private ByteBuffer sound;
    private int len;

    public ByteBuffer getSound() {
        return sound;
    }

    public void setSound(ByteBuffer sound) {
        this.sound = sound;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
    /*
    public WindowsWav (String filename) {
    	sound   = null;
    	len     = 0;

    	// Attempt to load sound file
    	GenericIStream file = files.OpenRead(filename, false, len);

    	if (file != null && !file.fail ()) {
    		if (!file.fail () && len > 0 && len < 0xa00000) {       // File must be 10meg or less in size

    			// Allocate data storage
    			sound = new char [len];

    			// Read in data
    			file.read (sound, len);
    		}
    	}
    	if (file != null)
    		delete file;
    }
    public boolean Loaded ()  { return sound != null; }
    public void Play ()    { PlaySound (sound, null, SND_MEMORY | SND_ASYNC | SND_NODEFAULT); }
    */
}
