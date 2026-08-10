package com.basic4gl.desktop.spi;

public interface DebugService {
    public void onLoad(PluginContext context);

    public Integer getPermanent();

    DebugLaunchInfo start(Object sender);

    boolean hasLaunchedProcess();

    boolean isUserBreakPoint(String filename, int line);

    void insertDeleteLines(String filename, int fileLineNo, int delta);

    void terminateLaunchedProcess();

    void setProcessExitListener(IProcessExitListener listener);

    void clearUserBreakPoints();
}
