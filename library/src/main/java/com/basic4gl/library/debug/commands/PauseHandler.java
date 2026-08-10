package com.basic4gl.library.debug.commands;

import com.basic4gl.language.core.runtime.VM;

public class PauseHandler {
    private final VM vm;

    public PauseHandler(VM vm) {
        this.vm = vm;
    }

    public void pause() {
        // Pause program
        vm.pause(); // Rely on callbacks to alert the main window that the VM was paused to update UI
    }
}
