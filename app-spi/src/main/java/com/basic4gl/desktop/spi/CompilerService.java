package com.basic4gl.desktop.spi;

public interface CompilerService {
    public void onLoad();
    public void onUnload();
    public void compile();
}
