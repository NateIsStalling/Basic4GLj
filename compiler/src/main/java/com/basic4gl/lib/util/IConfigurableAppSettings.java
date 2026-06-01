package com.basic4gl.lib.util;

import java.util.List;

public interface IConfigurableAppSettings extends IAppSettings {
    void setSandboxModeEnabled(boolean enabled);

    void setSyntax(int syntax);

    void setProgramArguments(List<String> programArguments);
}
