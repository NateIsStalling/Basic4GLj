package com.basic4gl.library.standard;

import com.basic4gl.language.core.runtime.ResourceStore;

/**
 * Used to track WindowsWavObjects
 */
class WindowsWavStore extends ResourceStore<WindowsWav> {
    protected void deleteElement(int index) {
        setValue(index, null);
    }

    public WindowsWavStore() {
        super(null);
    }
}
