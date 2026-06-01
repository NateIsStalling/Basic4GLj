package com.basic4gl.lib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorAppSettings implements IConfigurableAppSettings {

    private static final boolean DEFAULT_SANDBOX_MODE = true;
    // TODO this needs documentation; 1 is default in original source
    private static final int DEFAULT_SYNTAX = 1;

    private boolean isSandboxModeEnabled = DEFAULT_SANDBOX_MODE;

    private int syntax = DEFAULT_SYNTAX;

    private List<String> programArguments = new ArrayList<>();

    @Override
    public boolean isSandboxModeEnabled() {
        return isSandboxModeEnabled;
    }

    @Override
    public int getSyntax() {
        return syntax;
    }

    @Override
    public List<String> getProgramArguments() {
        return Collections.unmodifiableList(programArguments);
    }

    @Override
    public void setSandboxModeEnabled(boolean enabled) {
        isSandboxModeEnabled = enabled;
    }

    @Override
    public void setSyntax(int syntax) {
        this.syntax = syntax;
    }

    @Override
    public void setProgramArguments(List<String> programArguments) {
        this.programArguments =
                programArguments == null ? new ArrayList<>() : new ArrayList<>(programArguments);
    }
}
