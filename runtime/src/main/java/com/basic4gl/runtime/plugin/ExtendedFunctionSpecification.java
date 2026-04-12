package com.basic4gl.runtime.plugin;

import com.basic4gl.runtime.types.FunctionSpecification;

/**
 * Extended function specification including information about where the
 * function is stored (whether it's built in, or stored in a plugin and which one.)
 * Used by the plugin manager to pass info to the compiler.
 */
public class ExtendedFunctionSpecification {

    private FunctionSpecification specification;

    private boolean builtin;

    private int pluginIndex;

    /**
     * main specification
     */
    public FunctionSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(FunctionSpecification specification) {
        this.specification = specification;
    }

    /**
     * True = builtin, false = plugin function
     */
    public boolean isBuiltin() {
        return builtin;
    }

    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    /**
     * Index of plugin (if applicable). Note specification.index holds index of function WITHIN the plugin.
     */
    public int getPluginIndex() {
        return pluginIndex;
    }

    public void setPluginIndex(int pluginIndex) {
        this.pluginIndex = pluginIndex;
    }
}
