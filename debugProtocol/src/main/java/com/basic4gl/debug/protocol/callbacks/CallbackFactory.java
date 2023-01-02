package com.basic4gl.debug.protocol.callbacks;
import com.google.gson.Gson;

import java.util.Objects;

public class CallbackFactory {
    private final Gson gson;

    public CallbackFactory(Gson gson){
        this.gson = gson;
    }

    public Callback FromJson(String commandJson) {
        Callback callback = null;

        try {
            callback = gson.fromJson(commandJson, Callback.class);
            if (!Objects.equals(callback.type, Callback.TYPE)) {
                return null;
            }

            switch (callback.command) {
                case StackTraceCallback.COMMAND:
                    return gson.fromJson(commandJson, StackTraceCallback.class);
                case EvaluateWatchCallback.COMMAND:
                    return gson.fromJson(commandJson, EvaluateWatchCallback.class);
            }
        } catch (Exception e) {
        }
        return null;
    }
}

