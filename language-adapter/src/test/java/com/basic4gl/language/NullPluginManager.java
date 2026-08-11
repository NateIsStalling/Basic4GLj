package com.basic4gl.language;

import com.basic4gl.language.spi.PluginManager;

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
