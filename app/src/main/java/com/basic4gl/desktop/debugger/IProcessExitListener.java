package com.basic4gl.desktop.debugger;

public interface IProcessExitListener {
    void onProcessExited(RunHandler source, int exitCode, String stderrOutput);
}
