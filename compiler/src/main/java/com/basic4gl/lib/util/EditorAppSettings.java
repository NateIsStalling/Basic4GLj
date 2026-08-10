package com.basic4gl.lib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditorAppSettings implements IConfigurableAppSettings {

    private static final boolean DEFAULT_SANDBOX_MODE = true;
    // TODO this needs documentation; 1 is default in original source
    private static final int DEFAULT_SYNTAX = 1;
    private static final boolean DEFAULT_JVM_DEBUG_ENABLED = false;
    private static final boolean DEFAULT_JVM_DEBUG_SUSPEND = false;

    private boolean isSandboxModeEnabled = DEFAULT_SANDBOX_MODE;

    private int syntax = DEFAULT_SYNTAX;

    private List<String> programArguments = new ArrayList<>();
    private List<String> jvmArguments = new ArrayList<>();
    private boolean isJvmDebuggingEnabled = DEFAULT_JVM_DEBUG_ENABLED;
    private boolean isJvmDebugSuspendUntilAttach = DEFAULT_JVM_DEBUG_SUSPEND;
    private Integer jvmDebugPortOverride = null;

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
    public List<String> getJvmArguments() {
        return Collections.unmodifiableList(jvmArguments);
    }

    @Override
    public boolean isJvmDebuggingEnabled() {
        return isJvmDebuggingEnabled;
    }

    @Override
    public boolean isJvmDebugSuspendUntilAttach() {
        return isJvmDebugSuspendUntilAttach;
    }

    @Override
    public Integer getJvmDebugPortOverride() {
        return jvmDebugPortOverride;
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
        this.programArguments = programArguments == null ? new ArrayList<>() : new ArrayList<>(programArguments);
    }

    @Override
    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments == null ? new ArrayList<>() : new ArrayList<>(jvmArguments);
    }

    @Override
    public void setJvmDebuggingEnabled(boolean enabled) {
        isJvmDebuggingEnabled = enabled;
    }

    @Override
    public void setJvmDebugSuspendUntilAttach(boolean enabled) {
        isJvmDebugSuspendUntilAttach = enabled;
    }

    @Override
    public void setJvmDebugPortOverride(Integer port) {
        jvmDebugPortOverride = port;
    }
}
