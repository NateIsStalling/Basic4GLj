package com.basic4gl.debug;

public interface ILogger {
    void log(String message);

    void error(String err);

    void error(Throwable err);
}
