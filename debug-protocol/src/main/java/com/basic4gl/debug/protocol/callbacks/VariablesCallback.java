package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.Variable;

public class VariablesCallback extends Callback {
    public static final String COMMAND = "variables";

    public VariablesCallback() {
        super(COMMAND);
    }

    protected Variable[] variables;

    public void setVariables(Variable[] variables) {
        this.variables = variables;
    }

    public Variable[] getVariables() {
        return variables;
    }
}
