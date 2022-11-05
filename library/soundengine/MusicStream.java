package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.library.desktopgl.soundengine.util.ThreadEvent;
import com.basic4gl.runtime.vm.HasErrorState;

import java.util.List;
import java.util.concurrent.locks.Lock;

///////////////////////////////////////////////////////////////////////////////
//	MusicStream
//
///	A polled implementation of a streaming music file.
/// Plays an Ogg Vorbis file using OpenAL.
/// Automatically creates its own service thread to ensure the
/// music keeps playing.
public class MusicStream extends HasErrorState implements Runnable {

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
                    MusicStreamCmd cmd = new MusicStreamCmd();
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
                    if (stream.Playing())
                        wakeEvent.WaitFor(100);
                    else
                        wakeEvent.WaitFor();
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

    class MusicStreamCmd {
        MusicStreamCmdCode code;
        String filename;
        float gain;
        boolean looping;
    }

    // Internal polled music stream
    private MusicStreamPolled stream;

    // Service thread
    private Thread thread;

    // Command queue
    private List<MusicStreamCmd> commandQueue;

    // State
    private boolean playing;

    // Thread synchronisation
    private Lock commandQueueLock, stateLock;
    private ThreadEvent wakeEvent, readyEvent;

    private void SendCmd(MusicStreamCmdCode code) {
        SendCmd(code, "", 1f, false);
    }

    private void SendCmd(MusicStreamCmdCode code, String filename) {
        SendCmd(code, filename, 1f, false);
    }

    private void SendCmd(MusicStreamCmdCode code, String filename, float gain) {
        SendCmd(code, filename, gain, false);
    }

    private void SendCmd(MusicStreamCmdCode code, String filename, float gain, boolean looping) {

        // Build command
        MusicStreamCmd cmd = new MusicStreamCmd();
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




    public MusicStream() {
        playing = false;
        stream = null;
        readyEvent = new ThreadEvent(true);
        // Start service thread
        thread = new Thread(this);
        thread.start();
    }

    public void dispose() {

        // Stop service thread
        SendCmd(MusicStreamCmdCode.MSC_Shutdown);
        try {
            thread.wait();
        } catch (InterruptedException consumed) {
            //Do nothing
        }
    }

    // Control interface
    public void OpenFile(String filename) {
        OpenFile(filename, 1f, false);
    }

    public void OpenFile(String filename, float gain) {
        OpenFile(filename, gain, false);
    }

    public void OpenFile(String filename, float gain, boolean _looping)    // Open file and start playing
    {
        SendCmd(MusicStreamCmdCode.MSC_OpenFile, filename, gain, _looping);
    }

    public void CloseFile() {
        SendCmd(MusicStreamCmdCode.MSC_CloseFile);
    }

    public void SetGain(float gain) {
        SendCmd(MusicStreamCmdCode.MSC_SetGain, "", gain);
    }

    public boolean Playing() {
        readyEvent.WaitFor();
        stateLock.lock();
        boolean result = stream.Playing();
        stateLock.unlock();
        return result;
    }


    // Error status
    public void UpdateErrorState() {
        readyEvent.WaitFor();
        stateLock.lock();
        if (stream.hasError())
            setError(stream.getError());
        else
            clearError();
        stateLock.unlock();
    }
}