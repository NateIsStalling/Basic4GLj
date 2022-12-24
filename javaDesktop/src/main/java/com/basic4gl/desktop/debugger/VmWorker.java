package com.basic4gl.desktop.debugger;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.lib.util.*;
import com.basic4gl.runtime.TomVM;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class VmWorker extends SwingWorker<Object, CallbackMessage>
implements IDebugCallbackListener, IDebugger {

    private final Builder mBuilder;
    private final TomBasicCompiler mComp;
    private final IFileProvider mFiles;
    private final TomVM mVM;
    private final CallbackMessage mMessage;

    private RemoteDebugger remoteDebugger;

    TaskCallback mCallbacks;
    CountDownLatch mCompletionLatch;

    public VmWorker(
            Builder builder, TomBasicCompiler comp,
            IFileProvider fileOpener, TomVM vm,
            CallbackMessage message) {
        mBuilder = builder;
        mComp = comp;
        mFiles = fileOpener;
        mVM = vm;
        mMessage = message;
    };

    public void setCompletionLatch(CountDownLatch latch) {
        mCompletionLatch = latch;
    }

    public CountDownLatch getCompletionLatch() {
        return mCompletionLatch;
    }

    public void setCallbacks(TaskCallback callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    protected void process(List<CallbackMessage> chunks) {
        super.process(chunks);
        for (CallbackMessage message : chunks) {
            mCallbacks.message(message);
        }
    }

    @Override
    protected Object doInBackground() throws Exception {
//        IVMDriver driver = mBuilder.getVMDriver();
        boolean noError;
        DebugClientAdapter adapter = null;

        System.out.println("Running...");
        if (mVM == null)
            return null;    //TODO Throw exception
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

            //Debugger is attached
            while (!this.isCancelled()
                && (mMessage.getStatus() == CallbackMessage.STOPPED
                || mMessage.getStatus() == CallbackMessage.WORKING
                || mMessage.getStatus() == CallbackMessage.PAUSED)) {
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
    public void OnDebugCallbackReceived(com.basic4gl.debug.protocol.callbacks.CallbackMessage callback) {
//        int success;
//        success = !mVM.hasError()
//                ? CallbackMessage.SUCCESS
//                : CallbackMessage.FAILED;
//        publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
//                ? "Program completed"
//                : mVM.getError()));
        publish(new CallbackMessage(callback.status, callback.text));
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
    public boolean toggleBreakpoint(String filename, int line) {
        if (remoteDebugger != null) {
            return remoteDebugger.toggleBreakpoint(filename, line);
        }
        return false;
    }

    @Override
    public String evaluateWatch(String watch, boolean canCallFunc) {
        if (remoteDebugger != null) {
            return remoteDebugger.evaluateWatch(watch, canCallFunc);
        }
        return "???";
    }
}
