package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.library.desktopgl.soundengine.util.ThreadEvent;
import com.basic4gl.runtime.HasErrorState;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * A polled implementation of a streaming music file.
 * Plays an Ogg Vorbis file using OpenAL.
 * Automatically creates its own service thread to ensure the
 * music keeps playing.
 */
public class startMusicStream extends HasErrorState implements Runnable {

    @Override
    public void run() {
        {

            // Create stream
            stream = new MusicStreamPolled();

            // Main service loop
            boolean shuttingDown = false;

            // State has been setup
            while (!shuttingDown) {

                // Process any queued commands
                boolean foundCmd;
                do {
                    // Look for command
                    MusicStreamCommand cmd = new MusicStreamCommand();
                    commandQueueLock.lock();
                    if (commandQueue.size() > 0) {

                        // Extract command
                        cmd = commandQueue.get(0);
                        commandQueue.remove(0);
                        foundCmd = true;
                    } else {
                        foundCmd = false;

                        // All commmands have been executed.
                        // Set the "ready" event.
                        // (This means that the object has been updated, and is ready
                        // to be examined. Note that we do this while in the command
                        // queue lock.)
                        readyEvent.Set();
                    }
                    commandQueueLock.unlock();

                    // Process command
                    if (foundCmd) {
                        stateLock.lock();
                        switch (cmd.code) {
                            case MSC_Shutdown:
                                shuttingDown = true;
                                break;

                            case MSC_OpenFile:
                                stream.OpenFile(cmd.filename, cmd.gain, cmd.looping);
                                break;

                            case MSC_CloseFile:
                                stream.CloseFile();
                                break;

                            case MSC_SetGain:
                                stream.SetGain(cmd.gain);
                                break;
                        }
                        stateLock.unlock();
                    }
                } while (foundCmd);

                // Update music stream, to ensure music keeps playing
                stateLock.lock();
                stream.Update();
                stateLock.unlock();

                // Sleep until woken.
                // If the stream is playing, we wake every 100ms regardless, so that we
                // can update the stream and keep the music playing.
                // Otherwise we wait indefinitely.
                if (!shuttingDown) {
                    if (stream.Playing()) {
                        wakeEvent.WaitFor(100);
                    } else {
                        wakeEvent.WaitFor();
                    }
                }
            }

            // Close stream
            stream.dispose();
            stream = null;
        }
    }

    enum MusicStreamCmdCode {
        MSC_Shutdown,
        MSC_OpenFile,
        MSC_CloseFile,
        MSC_SetGain
    }

    ;

    // Internal polled music stream
    private MusicStreamPolled stream;

    // Service thread
    private Thread thread;

    // Command queue
    private List<MusicStreamCommand> commandQueue;

    // State
    private boolean playing;

    // Thread synchronisation
    private Lock commandQueueLock, stateLock;
    private ThreadEvent wakeEvent, readyEvent;

    private void sendCommand(MusicStreamCmdCode code) {
        sendCommand(code, "", 1f, false);
    }

    private void sendCommand(MusicStreamCmdCode code, String filename) {
        sendCommand(code, filename, 1f, false);
    }

    private void sendCommand(MusicStreamCmdCode code, String filename, float gain) {
        sendCommand(code, filename, gain, false);
    }

    private void sendCommand(MusicStreamCmdCode code, String filename, float gain, boolean looping) {

        // Build command
        MusicStreamCommand cmd = new MusicStreamCommand();
        cmd.code = code;
        cmd.filename = filename;
        cmd.gain = gain;
        cmd.looping = looping;

        // Add to command queue
        commandQueueLock.lock();
        commandQueue.add(cmd);

        // Clear the ready event. This indicates that the object has not finished
        // processing commands yet, and is not ready to be examined.
        readyEvent.Reset();
        commandQueueLock.unlock();

        // Wake up service thread
        wakeEvent.Set();
    }




    public startMusicStream() {
        playing = false;
        stream = null;
        readyEvent = new ThreadEvent(true);
        // Start service thread
        thread = new Thread(this);
        thread.start();
    }

    public void dispose() {

        // Stop service thread
        sendCommand(MusicStreamCmdCode.MSC_Shutdown);
        try {
            thread.wait();
        } catch (InterruptedException consumed) {
            //Do nothing
        }
    }

    // Control interface
    public void openFile(String filename) {
        openFile(filename, 1f, false);
    }

    public void openFile(String filename, float gain) {
        openFile(filename, gain, false);
    }

    public void openFile(String filename, float gain, boolean looping)    // Open file and start playing
    {
        sendCommand(MusicStreamCmdCode.MSC_OpenFile, filename, gain, looping);
    }

    public void closeFile() {
        sendCommand(MusicStreamCmdCode.MSC_CloseFile);
    }

    public void setGain(float gain) {
        sendCommand(MusicStreamCmdCode.MSC_SetGain, "", gain);
    }

    public boolean isPlaying() {
        readyEvent.WaitFor();
        stateLock.lock();
        boolean result = stream.Playing();
        stateLock.unlock();
        return result;
    }


    // Error status
    public void updateErrorState() {
        readyEvent.WaitFor();
        stateLock.lock();
        if (stream.hasError()) {
            setError(stream.getError());
        } else {
            clearError();
        }
        stateLock.unlock();
    }
}