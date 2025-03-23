package com.basic4gl.library.desktopgl.soundengine;

public interface SoundLibrary {
	void init(int voiceCount);

	void dispose();

	void reset();

	Sound loadSound(String filename);

	void deleteSound(Sound sound);

	int playSound(Sound sound, float volume, boolean looped);

	void stopSounds();

	void playMusic(String filename, float volume, boolean looped);

	void stopMusic();

	boolean isMusicPlaying();

	void setMusicVolume(float volume);

	void stopSoundVoice(int voice);

	void getError(StringBuilder message);
}
