package com.basic4gl.library.debug.commands;

import com.basic4gl.runtime.TomVM;

public class PauseHandler {
  private final TomVM mVM;

  public PauseHandler(TomVM vm) {
    mVM = vm;
  }

  public void pause() {
    // Pause program
    mVM.pause(); // Rely on callbacks to alert the main window that the VM was paused to update UI
  }
}
