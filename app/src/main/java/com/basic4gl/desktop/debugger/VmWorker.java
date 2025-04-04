package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.types.InstructionPosition;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.lib.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

public class VmWorker extends SwingWorker<Object, Object> implements IDebugCallbackListener, IDebugger {

    private final IFileProvider files;

    private RemoteDebugger remoteDebugger;

    private DebuggerTaskCallback debuggerTaskCallback;
    private CountDownLatch completionLatch;

    public VmWorker(IFileProvider fileOpener) {
        files = fileOpener;
    }

    public void setCompletionLatch(CountDownLatch latch) {
        completionLatch = latch;
    }

    public CountDownLatch getCompletionLatch() {
        return completionLatch;
    }

    public void setCallbacks(DebuggerTaskCallback callbacks) {
        debuggerTaskCallback = callbacks;
    }

    @Override
    protected void process(List<Object> chunks) {
        super.process(chunks);
        for (Object message : chunks) {
            if (message instanceof DebuggerCallbackMessage) {
                debuggerTaskCallback.message((DebuggerCallbackMessage) message);
            } else {
                debuggerTaskCallback.messageObject(message);
            }
        }
    }

    @Override
    protected Object doInBackground() throws Exception {
        //        IVMDriver driver = this.builder.getVMDriver();
        boolean noError;
        DebugClientAdapter adapter = null;

        System.out.println("Running...");
        try {
            files.useAppDirectory();
            //            driver.onPreExecute();
            files.useCurrentDirectory();

            // Initialize libraries
            //            for (Library lib : mComp.getLibraries()) {
            //                driver.initLibrary(lib);
            //                lib.init(mVM);
            //            }
            adapter = new DebugClientAdapter(this, DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT);
            adapter.connect();
            remoteDebugger = new RemoteDebugger(adapter);

            debuggerTaskCallback.onDebuggerConnected();

            // Debugger is attached
            while (!this.isCancelled()
            // TODO 1/2023 need to keep connection alive while debugee is idle
            //                    && (mMessage.getStatus() == CallbackMessage.STOPPED
            //                    || mMessage.getStatus() == CallbackMessage.WORKING
            //                    || mMessage.getStatus() == CallbackMessage.PAUSED)
            ) {
                // idle thread;
                Thread.sleep(100);
            }

            // TODO 12/2022 remove this; handled by socket callbacks
            //            //Perform debugger callbacks
            //            int success;
            //            success = !mVM.hasError()
            //                    ? CallbackMessage.SUCCESS
            //                    : CallbackMessage.FAILED;
            //            publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
            //                    ? "Program completed"
            //                    : mVM.getError()));
            // TODO 12/2022 is this still needed?
            //            driver.onPostExecute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            adapter.stop();
            remoteDebugger = null;

            debuggerTaskCallback.onDebuggerDisconnected();
            //            driver.onFinally();
            // Confirm this thread has completed before a new one can be executed
            if (completionLatch != null) {
                completionLatch.countDown();
            }
        }
        return null;
    }

    public void onDebugCallbackReceived(com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage callback) {

        VMStatus vmStatus = null;
        if (callback.getVMStatus() != null) {
            vmStatus = new VMStatus(
                    callback.getVMStatus().isDone(),
                    callback.getVMStatus().hasError(),
                    callback.getVMStatus().getError());
        }
        DebuggerCallbackMessage message =
                new DebuggerCallbackMessage(callback.getStatus(), callback.getText(), vmStatus);

        InstructionPosition instructionPosition = callback.getSourcePosition();
        if (instructionPosition != null) {
            message.setInstructionPosition(instructionPosition.line, instructionPosition.column);
        }

        publish(message);
    }

    public void onCallbackReceived(Callback callback) {
        // TODO 12/2022 improve type safety of interface/map callback DTO to domain model
        publish(callback);
    }

    public void onDisconnected() {
        debuggerTaskCallback.onDebuggerDisconnected();
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
