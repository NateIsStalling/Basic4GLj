package com.basic4gl.compiler.plugin;

/**
 * Tracks shared interfaces registered by plugins
 */
public class PluginJARSharedInterface {
    /**
     * Interface
     */
    private final Object intf;
    /**
     * DLL that owns the object
     */
    private final PluginLibrary owner;

    public PluginJARSharedInterface(Object intf, PluginLibrary owner) {
        this.intf = intf;
        this.owner = owner;
    }
    public PluginJARSharedInterface(PluginJARSharedInterface s) {
        this.intf = s.intf;
        this.owner = s.owner;
    }
    public PluginJARSharedInterface() {
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
