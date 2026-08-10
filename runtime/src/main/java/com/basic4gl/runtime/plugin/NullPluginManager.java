package com.basic4gl.runtime.plugin;

public final class NullPluginManager extends PluginManager {
    public NullPluginManager() {
        super(false);
    }

    public boolean loadPlugin(String filename) {
        return false;
    }

    public boolean unloadPlugin(String filename) {
        return false;
    }
}
