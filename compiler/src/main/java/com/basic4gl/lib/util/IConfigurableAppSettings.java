package com.basic4gl.lib.util;

public interface IConfigurableAppSettings extends IAppSettings {
    void setSandboxModeEnabled(boolean enabled);
    void setSyntax(int syntax);
}
