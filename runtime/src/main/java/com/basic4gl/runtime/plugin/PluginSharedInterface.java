package com.basic4gl.runtime.plugin;

/**
 * Tracks shared interfaces registered by plugins
 */
public class PluginSharedInterface {
    /**
     * Interface
     */
    private final Object intf;
    /**
     * DLL that owns the object
     */
    private final PluginLibrary owner;

    public PluginSharedInterface(Object intf, PluginLibrary owner) {
        this.intf = intf;
        this.owner = owner;
    }
    public PluginSharedInterface(PluginSharedInterface s) {
        this.intf = s.intf;
        this.owner = s.owner;
    }
    public PluginSharedInterface() {
        this.intf = null;
        this.owner = null;
    }

    public Object getInterface() {
        return intf;
    }

    public PluginLibrary getOwner() {
        return owner;
    }
}
