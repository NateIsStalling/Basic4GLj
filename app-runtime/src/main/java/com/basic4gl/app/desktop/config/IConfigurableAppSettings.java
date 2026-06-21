package com.basic4gl.app.desktop.config;

import java.util.List;

public interface IConfigurableAppSettings extends com.basic4gl.language.core.extensions.IAppSettings {
    void setSandboxModeEnabled(boolean enabled);

    void setSyntax(int syntax);

    void setProgramArguments(List<String> programArguments);

    void setJvmArguments(List<String> jvmArguments);

    void setJvmDebuggingEnabled(boolean enabled);

    void setJvmDebugSuspendUntilAttach(boolean enabled);

    void setJvmDebugPortOverride(Integer port);
}
