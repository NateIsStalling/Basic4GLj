package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static com.basic4gl.library.desktopgl.soundengine.IOUtil.ioResourceToByteBuffer;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

//import paulscode.sound.SoundSystem;
//import paulscode.sound.SoundSystemException;
//import paulscode.sound.SoundSystemConfig;
//import paulscode.sound.codecs.CodecWav;
//import paulscode.sound.codecs.CodecJOrbis;
//import paulscode.sound.codecs.CodecIBXM;
//import paulscode.sound.libraries.LibraryLWJGLOpenAL;
//import paulscode.sound.libraries.LibraryLWJGLOpenAL;
//import paulscode.sound.libraries.LibraryJavaSound;


/**
 * Represents a sound effect. Wrapper for OpenAL buffer
 * Original source: SoundEngine.h
 */
public class Sound extends HasErrorState {

    // The OpenAL buffer
    IntBuffer buffer = BufferUtils.createIntBuffer(1);
//    private static SoundSystem system;

    public static void init(){
        // Load some library and codec pluggins:
//        try
//        {
//            SoundSystemConfig.setSoundFilesPackage("");
//            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
//            SoundSystemConfig.setCodec( "wav", CodecWav.class );
//            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
//            SoundSystemConfig.setCodec("xm",CodecIBXM.class);
//            SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
//            SoundSystemConfig.setCodec("mod", CodecIBXM.class);
//        }
//        catch( SoundSystemException e )
//        {
//            System.out.println("error linking with the SoundSystem plugins" );
//        }

        // Instantiate the SoundSystem:
//        try
//        {
//            system = new SoundSystem( LibraryLWJGLOpenAL.class );
////            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
//        }
//        catch( SoundSystemException e )
//        {
//            System.out.println( "JavaSound library is not compatible on " +
//                    "this computer" );
////            System.out.println( "LWJGL OpenAL library is not compatible on " +
////                                "this computer" );
//            e.printStackTrace();
//            return;
//        }
    }

    public static void cleanup(){
//        if (system != null) {
//            system.cleanup();
//        }
//        system = null;
    }

    public Sound(String filename){

        // Open file
        //buffer = alutCreateBufferFromFile(filename);
        AL10.alGenBuffers(buffer);

        File file = new File(filename);
        URL url;

        try {
            url = file.toURI().toURL();

            system.newStreamingSource(true, filename, url, filename,
                    true, 0, 0, 0,
                    SoundSystemConfig.ATTENUATION_NONE,
                    0);
            //system.play(url.toString());
                    /*.newStreamingSource( true, "Music", url, "jingle1.mid",
                    true, 0, 0, 0,
                    SoundSystemConfig.ATTENUATION_NONE,
                    0 );*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //WaveData waveFile = WaveData.create("FancyPants.wav");
        //AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        //waveFile.dispose();

        // Check for errors
        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(SoundEngine.getALErrorString(error));
        } else {
            clearError();
        }
    }
    public Sound() {
        IntBuffer b = IntBuffer.allocate(1);
        b.put(buffer).rewind();
        AL10.alDeleteBuffers(b);
        buffer.put(0, b.get(0));
    }

    public void dispose(){
        if (buffer.get(0) != AL10.AL_NONE) {
            buffer.rewind();
            AL10.alDeleteBuffers(buffer);
        }
    }
    // Member access
    public int getBuffer() { return buffer.get(0); }

    // Sound properties
    public int getFreq() {
        IntBuffer freq = BufferUtils.createIntBuffer(1);
        AL10.alGetBufferi(buffer.get(0), AL10.AL_FREQUENCY, freq);
        return freq.get(0);
    }
    public int getBits() {
        IntBuffer bits = BufferUtils.createIntBuffer(1);
        AL10.alGetBufferi(buffer.get(0), AL10.AL_BITS, bits);
        return bits.get(0);
    }

    public static class VorbisSound {
        private final ByteBuffer encodedAudio;

        private final long handle;

        private final int channels;
        private final int sampleRate;

        final int   samplesLength;
        final float samplesSec;

        private final AtomicInteger sampleIndex;
     public VorbisSound(String filePath, AtomicInteger sampleIndex) {
         try {
             encodedAudio = ioResourceToByteBuffer(filePath, 256 * 1024);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

         try (MemoryStack stack = MemoryStack.stackPush()) {
             IntBuffer error = stack.mallocInt(1);
             handle = stb_vorbis_open_memory(encodedAudio, error, null);
             if (handle == NULL) {
                 throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
             }

             STBVorbisInfo info = STBVorbisInfo.malloc(stack);
//             print(info);
             this.channels = info.channels();
             this.sampleRate = info.sample_rate();
         }

         this.samplesLength = stb_vorbis_stream_length_in_samples(handle);
         this.samplesSec = stb_vorbis_stream_length_in_seconds(handle);

         this.sampleIndex = sampleIndex;
         sampleIndex.set(0);
     }
    }
    public class Mp3Sound {
        public Mp3Sound() {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream();//new AudioInputStream();
            AudioSystem.getAudioInputStream(AudioFormat.Encoding, inputStream);
        }
    }
}
