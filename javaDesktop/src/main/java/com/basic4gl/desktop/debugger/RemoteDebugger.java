package com.basic4gl.desktop.debugger;

import com.basic4gl.desktop.debugger.commands.*;

import com.basic4gl.lib.util.Library;
import org.eclipse.jetty.client.HttpClient;

public class RemoteDebugger implements IDebugger {
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
        ResumeHandler handler = new ResumeHandler();
        handler.resume();
    }

    @Override
    public void pauseApplication() {
//        PauseHandler pauseHandler = new PauseHandler(mVM);
//        pauseHandler.pause();

//        WebSocketClient client = new WebSocketClient();
//        URI uri = URI.create("ws://localhost:8080/events/");
//        try
//        {
//            client.start();
//            // The socket that receives events
//            EventSocket socket = new EventSocket();
//            // Attempt Connect
//
//            Future<Session> fut = client.connect(socket, uri);
//            // Wait for Connect
//            Session session = fut.get();
//
//            // Send a message
//            session.getRemote().sendString("Hello");
//
//            // Send another message
//            session.getRemote().sendString("Goodbye");
//
//            // Wait for other side to close
//            socket.awaitClosure();
//
//            // Close session
//            session.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally
//        {
//            try {
//                client.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void resumeApplication() {
        ResumeHandler resumeHandler = new ResumeHandler();
        resumeHandler.resume();
    }

    @Override
    public void runApplication(Library builder, String currentDirectory, String libraryPath) {

    }

    @Override
    public void stopApplication() {

    }

    @Override
    public void step(int type) {
//        StepHandler handler = new StepHandler(this, mVM);
//        handler.DoStep(type);
    }

    @Override
    public boolean toggleBreakpoint(String filename, int line) {
//        ToggleBreakPointHandler handler = new ToggleBreakPointHandler(this, mDebugger, mVM);
//        boolean isBreakpoint = handler.toggleBreakPoint(filename, line);
//        return isBreakpoint;
        return false;
    }

    @Override
    public String evaluateWatch(String watch, boolean canCallFunc) {
//        EvaluateWatchHandler handler = new EvaluateWatchHandler(this, mComp, mVM);
//        return handler.EvaluateWatch(watch, canCallFunc);
        return "???";
    }
}
