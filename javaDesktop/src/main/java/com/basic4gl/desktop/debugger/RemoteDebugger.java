package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.commands.*;

import com.basic4gl.lib.util.Library;
import org.eclipse.jetty.client.HttpClient;

public class RemoteDebugger implements IDebugger {
    private DebugClientAdapter adapter;


    public RemoteDebugger(DebugClientAdapter adapter) {
        this.adapter = adapter;
    }

    public void debugStart() {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void continueApplication() {
        DebugCommand command = new ContinueCommand();
        adapter.message(command);
    }

    @Override
    public void pauseApplication() {
        DebugCommand command = new PauseCommand();
        adapter.message(command);
    }

    @Override
    public void resumeApplication() {
        DebugCommand command = new ResumeCommand();
        adapter.message(command);
//        ResumeHandler resumeHandler = new ResumeHandler();
//        resumeHandler.resume();
    }

    @Override
    public void runApplication(Library builder, String currentDirectory, String libraryPath) {

    }

    @Override
    public void stopApplication() {
        DebugCommand command = new StopCommand();
        adapter.message(command);
    }

    @Override
    public void step(int type) {
        DebugCommand command = new StepCommand(type);
        adapter.message(command);
    }

    @Override
    public void terminateApplication() {
        DebugCommand command = new TerminateCommand();
        adapter.message(command);
    }

    @Override
    public boolean toggleBreakpoint(String filename, int line) {
        DebugCommand command = new ToggleBreakpointCommand(filename, line);
        adapter.message(command);
        return false;
    }

    @Override
    public int evaluateWatch(String watch, boolean canCallFunc) {
        String context = canCallFunc
            ? EvaluateWatchCommand.EVALUATE_CONTEXT_WATCH
            : EvaluateWatchCommand.EVALUATE_CONTEXT_VARIABLES;
        DebugCommand command = new EvaluateWatchCommand(watch, context);

        int requestId = adapter.message(command);
        return requestId;
    }

    @Override
    public void refreshCallStack() {
        DebugCommand command = new StackTraceCommand();
        adapter.message(command);
    }
}
