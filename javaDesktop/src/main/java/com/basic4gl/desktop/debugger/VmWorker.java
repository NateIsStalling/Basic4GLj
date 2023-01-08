package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.types.InstructionPosition;
import com.basic4gl.debug.protocol.types.SourceBreakpoint;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.lib.util.*;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class VmWorker extends SwingWorker<Object, Object>
implements IDebugCallbackListener, IDebugger {

    private final IFileProvider mFiles;
    private final DebuggerCallbackMessage mMessage;

    private RemoteDebugger remoteDebugger;

    DebuggerTaskCallback mCallbacks;
    CountDownLatch mCompletionLatch;

    public VmWorker(
            IFileProvider fileOpener,
            DebuggerCallbackMessage message) {
        mFiles = fileOpener;
        mMessage = message;
    };

    public void setCompletionLatch(CountDownLatch latch) {
        mCompletionLatch = latch;
    }

    public CountDownLatch getCompletionLatch() {
        return mCompletionLatch;
    }

    public void setCallbacks(DebuggerTaskCallback callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    protected void process(List<Object> chunks) {
        super.process(chunks);
        for (Object message : chunks) {
            if (message instanceof DebuggerCallbackMessage) {
                mCallbacks.message((DebuggerCallbackMessage) message);
            } else {
                mCallbacks.messageObject(message);
            }

        }
    }

    @Override
    protected Object doInBackground() throws Exception {
//        IVMDriver driver = mBuilder.getVMDriver();
        boolean noError;
        DebugClientAdapter adapter = null;

        System.out.println("Running...");
        try {
            mFiles.useAppDirectory();
//            driver.onPreExecute();
            mFiles.useCurrentDirectory();

            //Initialize libraries
//            for (Library lib : mComp.getLibraries()) {
//                driver.initLibrary(lib);
//                lib.init(mVM);
//            }
            adapter = new DebugClientAdapter(this);
            adapter.connect();
            remoteDebugger = new RemoteDebugger(adapter);

            mCallbacks.onDebuggerConnected();


            //Debugger is attached
            while (!this.isCancelled()
//TODO 1/2023 need to keep connection alive while debugee is idle
//                    && (mMessage.getStatus() == CallbackMessage.STOPPED
//                    || mMessage.getStatus() == CallbackMessage.WORKING
//                    || mMessage.getStatus() == CallbackMessage.PAUSED)
            ) {
                // idle thread;
                Thread.sleep(100);
            }

            //TODO 12/2022 remove this; handled by socket callbacks
//            //Perform debugger callbacks
//            int success;
//            success = !mVM.hasError()
//                    ? CallbackMessage.SUCCESS
//                    : CallbackMessage.FAILED;
//            publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
//                    ? "Program completed"
//                    : mVM.getError()));
            //TODO 12/2022 is this still needed?
//            driver.onPostExecute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            adapter.stop();
            remoteDebugger = null;

//            driver.onFinally();
            //Confirm this thread has completed before a new one can be executed
            if (mCompletionLatch != null) {
                mCompletionLatch.countDown();
            }
        }
        return null;
    }

    @Override
    public void OnDebugCallbackReceived(com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback) {

        VMStatus vmStatus = null;
        if (callback.getVMStatus() != null) {
            vmStatus = new VMStatus(
                callback.getVMStatus().isDone(),
                callback.getVMStatus().hasError(),
                callback.getVMStatus().getError()
            );
        }
        DebuggerCallbackMessage message = new DebuggerCallbackMessage(callback.status, callback.text, vmStatus);

        InstructionPosition instructionPosition = callback.getSourcePosition();
        if (instructionPosition != null) {
            message.setInstructionPosition(instructionPosition.line, instructionPosition.column);
        }

        publish(message);
    }

    @Override
    public void OnCallbackReceived(Callback callback) {
        // TODO 12/2022 improve type safety of interface/map callback DTO to domain model
        publish(callback);
    }

    @Override
    public void beginSessionConfiguration() {
        if (remoteDebugger != null) {
            remoteDebugger.beginSessionConfiguration();
        }
    }

    @Override
    public void commitSessionConfiguration() {
        if (remoteDebugger != null) {
            remoteDebugger.commitSessionConfiguration();
        }
    }

    @Override
    public void continueApplication() {
        if (remoteDebugger != null) {
            remoteDebugger.continueApplication();
        }
    }

    @Override
    public void pauseApplication() {
        if (remoteDebugger != null) {
            remoteDebugger.pauseApplication();
        }
    }

    @Override
    public void resumeApplication() {
        if (remoteDebugger != null) {
            remoteDebugger.resumeApplication();
        }
    }

    @Override
    public void runApplication(Library builder, String currentDirectory, String libraryPath) {
        if (remoteDebugger != null) {
            remoteDebugger.runApplication(builder, currentDirectory, libraryPath);
        }
    }

    @Override
    public void stopApplication() {
        if (remoteDebugger != null) {
            remoteDebugger.stopApplication();
        }
    }

    @Override
    public void step(int type) {
        if (remoteDebugger != null) {
            remoteDebugger.step(type);
        }
    }

    @Override
    public void terminateApplication() {
        if (remoteDebugger != null) {
            remoteDebugger.terminateApplication();
        }
    }

    @Override
    public boolean setBreakpoints(String filename, List<Integer> breakpoints) {
        if (remoteDebugger != null) {
            return remoteDebugger.setBreakpoints(filename, breakpoints);
        }
        return false;
    }

    @Override
    public boolean toggleBreakpoint(String filename, int line) {
        if (remoteDebugger != null) {
            return remoteDebugger.toggleBreakpoint(filename, line);
        }
        return false;
    }

    @Override
    public int evaluateWatch(String watch, boolean canCallFunc) {
        if (remoteDebugger != null) {
            return remoteDebugger.evaluateWatch(watch, canCallFunc);
        }
        return 0;
    }

    @Override
    public void refreshCallStack() {
        if (remoteDebugger != null) {
            remoteDebugger.refreshCallStack();
        }
    }
}
