package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.stackframe.UserFuncStackFrame;
import com.google.gson.Gson;
import java.util.Vector;
import javax.websocket.Session;

public class StackTraceCommandHandler {
    private final TomVM mVM;
    private final Gson mGson;

    public StackTraceCommandHandler(TomVM vm, Gson gson) {
        mVM = vm;
        mGson = gson;
    }

    public void handle(Session session) {
        Vector<UserFuncStackFrame> callStack = mVM.getUserCallStack();
        StackTraceCallback callback = new StackTraceCallback();

        for (UserFuncStackFrame stackFrame : callStack) {
            StackFrame frame = new StackFrame();

            // TODO 12/2022 consider extending LineNumberMapping to include userFunc names;
            // userFuncIndex is currently resolved by the editor and may not be easily portable
            frame.name = String.valueOf(stackFrame.userFuncIndex);
            // TODO 12/2022 consider extending LineNumberMapping to include GOSUB labels;
            // returnAddr is currently resolved by the editor and may not be easily portable
            frame.instructionPointer = String.valueOf(stackFrame.returnAddr);

            callback.stackFrames.add(frame);
        }

        callback.totalFrames = callback.stackFrames.size();

        String json = mGson.toJson(callback);
        message(session, json);
    }

    private void message(Session session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
