package com.basic4gl.debug;

public class ConsoleLogger implements ILogger {
@Override
public void log(String message) {
	// TODO implement log levels
	// System.out.println(message);
}

@Override
public void error(String err) {
	System.err.println(err);
}

@Override
public void error(Throwable err) {
	err.printStackTrace(System.err);
}
}
