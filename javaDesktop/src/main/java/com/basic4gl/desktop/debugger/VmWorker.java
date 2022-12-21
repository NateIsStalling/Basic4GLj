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
        IVMDriver driver = mBuilder.getVMDriver();
        boolean noError;
        DebugClientAdapter adapter = null;

        System.out.println("Running...");
        if (mVM == null)
            return null;    //TODO Throw exception
        try {
            mFiles.useAppDirectory();
            driver.onPreExecute();
            mFiles.useCurrentDirectory();

            //Initialize libraries
            for (Library lib : mComp.getLibraries()) {
                driver.initLibrary(lib);
                lib.init(mVM);
            }
            adapter = new DebugClientAdapter(this);
            adapter.connect();
            remoteDebugger = new RemoteDebugger(adapter);

            //Debugger is attached
            while (!this.isCancelled() && !mVM.hasError() && !mVM.Done() && !driver.isClosing()) {
                // Run the virtual machine for a certain number of steps
                mVM.PatchIn();

                if (mVM.Paused()) {
                    //Breakpoint reached or paused by debugger
                    System.out.println("VM paused");
                    mMessage.setMessage(CallbackMessage.PAUSED, "Reached breakpoint");
                    publish(mMessage);


                    //Resume running
                    if (mMessage.status == CallbackMessage.WORKING) {
                        // Kick the virtual machine over the next op-code before patching in the breakpoints.
                        // otherwise we would never get past a breakpoint once we hit it, because we would
                        // keep on hitting it immediately and returning.
                        publish(driver.driveVM(1));

                        // Run the virtual machine for a certain number of steps
                        mVM.PatchIn();
                    }
                    //Check if program was stopped while paused
                    if (this.isCancelled() || mVM.hasError() || mVM.Done() || driver.isClosing())
                        break;
                }

                //Continue to next OpCode
                publish(driver.driveVM(TomVM.VM_STEPS));

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                driver.handleEvents();

            }    //Program completed

            //Perform debugger callbacks
            int success;
            success = !mVM.hasError()
                    ? CallbackMessage.SUCCESS
                    : CallbackMessage.FAILED;
            publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
                    ? "Program completed"
                    : mVM.getError()));

            driver.onPostExecute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            adapter.stop();
            remoteDebugger = null;

            driver.onFinally();
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
        remoteDebugger.continueApplication();
    }

    @Override
    public void pauseApplication() {
        remoteDebugger.pauseApplication();
    }

    @Override
    public void resumeApplication() {
        remoteDebugger.resumeApplication();
    }

    @Override
    public void runApplication(Library builder, String currentDirectory, String libraryPath) {
        remoteDebugger.runApplication(builder, currentDirectory, libraryPath);
    }

    @Override
    public void stopApplication() {
        remoteDebugger.stopApplication();
    }

    @Override
    public void step(int type) {
        remoteDebugger.step(type);
    }

    @Override
    public boolean toggleBreakpoint(String filename, int line) {
        return remoteDebugger.toggleBreakpoint(filename, line);
    }

    @Override
    public String evaluateWatch(String watch, boolean canCallFunc) {
        return remoteDebugger.evaluateWatch(watch, canCallFunc);
    }
}
