package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.vm.HasErrorState;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecIBXM;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
//import paulscode.sound.libraries.LibraryLWJGLOpenAL;
//import paulscode.sound.libraries.LibraryJavaSound;


/**
 * Represents a sound effect. Wrapper for OpenAL buffer
 * Original source: SoundEngine.h
 */
public class Sound extends HasErrorState {

    // The OpenAL buffer
    IntBuffer buffer = BufferUtils.createIntBuffer(1);
    private static SoundSystem system;

    public static void init(){
        // Load some library and codec pluggins:
        try
        {
            SoundSystemConfig.setSoundFilesPackage("");
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            SoundSystemConfig.setCodec( "wav", CodecWav.class );
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
            SoundSystemConfig.setCodec("xm",CodecIBXM.class);
            SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
            SoundSystemConfig.setCodec("mod", CodecIBXM.class);
        }
        catch( SoundSystemException e )
        {
            System.out.println("error linking with the SoundSystem plugins" );
        }

        // Instantiate the SoundSystem:
        try
        {
            system = new SoundSystem( LibraryLWJGLOpenAL.class );
//            mySoundSystem = new SoundSystem( LibraryLWJGLOpenAL.class );
        }
        catch( SoundSystemException e )
        {
            System.out.println( "JavaSound library is not compatible on " +
                    "this computer" );
//            System.out.println( "LWJGL OpenAL library is not compatible on " +
//                                "this computer" );
            e.printStackTrace();
            return;
        }
    }

    public static void cleanup(){
        system.cleanup();
        system = null;
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
        if (error != AL10.AL_NO_ERROR)
            setError(SoundEngine.GetALErrorString(error));
        else
            clearError();
    }
    public Sound() {
/*
            ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
            bb.asIntBuffer().put(buffer).rewind();
            AL10.alDeleteBuffers(1, bb);
            buffer.put(0, bb.asIntBuffer().get(0));*/
    }

    public void dispose(){
            ByteBuffer bb = BufferUtils.createByteBuffer(Integer.SIZE/Byte.SIZE);
            bb.asIntBuffer().put(buffer).rewind();
            if (buffer.get(0) != AL10.AL_NONE)
                AL10.alDeleteBuffers(1, bb);

    }
    // Member access
    public int Buffer() { return buffer.get(0); }

    // Sound properties
    public int Freq() {
        IntBuffer freq = BufferUtils.createIntBuffer(1);
        AL10.alGetBufferi(buffer.get(0), AL10.AL_FREQUENCY, freq);
        return freq.get(0);
    }
    public int Bits() {
        IntBuffer bits = BufferUtils.createIntBuffer(1);
        AL10.alGetBufferi(buffer.get(0), AL10.AL_BITS, bits);
        return bits.get(0);
    }
}
