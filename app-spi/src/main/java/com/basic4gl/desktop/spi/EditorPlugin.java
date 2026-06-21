package com.basic4gl.desktop.spi;

public abstract class EditorPlugin {
    public abstract String getName();
    public abstract String getDescription();
    public abstract String getVersion();
    public abstract String getAuthor();

    public void onLoad() {
        // Default implementation does nothing
    }

    public void onUnload() {
        // Default implementation does nothing
    }

    public abstract CompilerService getCompiler();
    public abstract LanguageService getLanguage();
    public abstract DebugService getDebug();
    public abstract Builder[] getBuilder();
    public abstract Target[] getTargets();
}
