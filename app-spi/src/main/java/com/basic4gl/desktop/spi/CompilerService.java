package com.basic4gl.desktop.spi;

public interface CompilerService {
    public void onLoad(PluginContext context);
    public void onUnload();
    public void compile();
    public Builder[] getBuilders();

    void clearError();

    String getError();

    Long getTokenColumn();

    Long getTokenLine();

    boolean hasError();

    boolean tryCompileForExport();

    void clear();

    int getParserLinePosition();
}
