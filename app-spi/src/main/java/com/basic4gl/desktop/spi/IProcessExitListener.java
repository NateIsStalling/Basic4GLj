package com.basic4gl.desktop.spi;

public interface IProcessExitListener {
    void onProcessExited(Object sender, int exitCode, String stderrOutput);
}
