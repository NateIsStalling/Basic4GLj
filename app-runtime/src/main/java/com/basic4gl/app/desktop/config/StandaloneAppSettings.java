package com.basic4gl.app.desktop.config;

import com.basic4gl.language.core.extensions.IAppSettings;

import java.util.Collections;
import java.util.List;

public class StandaloneAppSettings implements IAppSettings {
    // Standalone applications are NOT sandboxed.
    private static final boolean DEFAULT_SANDBOX_MODE = false;
    // TODO this needs documentation; 1 is default in original source
    private static final int DEFAULT_SYNTAX = 1;

    @Override
    public boolean isSandboxModeEnabled() {

        return DEFAULT_SANDBOX_MODE;
    }

    @Override
    public int getSyntax() {
        return DEFAULT_SYNTAX;
    }

    @Override
    public List<String> getProgramArguments() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getJvmArguments() {
        return Collections.emptyList();
    }

    @Override
    public boolean isJvmDebuggingEnabled() {
        return false;
    }

    @Override
    public boolean isJvmDebugSuspendUntilAttach() {
        return false;
    }

    @Override
    public Integer getJvmDebugPortOverride() {
        return null;
    }
}
