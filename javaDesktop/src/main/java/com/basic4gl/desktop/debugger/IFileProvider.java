package com.basic4gl.desktop.debugger;

// TODO refactor this; was just trying to keep filepaths out of VmWorker
public interface IFileProvider {
    void useAppDirectory();
    void useCurrentDirectory();
}
