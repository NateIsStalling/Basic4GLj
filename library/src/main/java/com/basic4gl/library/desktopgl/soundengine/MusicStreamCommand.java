package com.basic4gl.library.desktopgl.soundengine;

class MusicStreamCommand {
    private MusicStreamCommandCode code;
    private String filename;
    private float gain;
    private boolean looping;

    public MusicStreamCommandCode getCode() {
        return code;
    }

    public void setCode(MusicStreamCommandCode code) {
        this.code = code;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}
