package com.basic4gl.desktop.spi;

public interface PreprocessorService {
    public void onLoad(PluginContext context);
    boolean hasError();
    String getError();
    public boolean preprocess(ISourceFile sourceFile);
}
