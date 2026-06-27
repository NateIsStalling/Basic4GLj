package com.basic4gl.desktop.spi;

public abstract class EditorPlugin {
    public abstract String getName();

    public abstract String getDescription();

    public abstract String getVersion();

    public abstract String getAuthor();

    public void onLoad(PluginContext context) {
        CompilerService compilerService = getCompiler();
        if (compilerService != null) {
            compilerService.onLoad(context);
        }
        LanguageService languageService = getLanguage();
        if (languageService != null) {
            languageService.onLoad(context);
        }
        DebugService debugService = getDebug();
        if (debugService != null) {
            debugService.onLoad(context);
        }
        PreprocessorService preprocessorService = getPreprocessor();
        if (preprocessorService != null) {
            preprocessorService.onLoad(context);
        }
    }

    public void onCloseAll() {}

    public void onUnload() {
        // Default implementation does nothing
    }

    public abstract CompilerService getCompiler();

    public abstract PreprocessorService getPreprocessor();

    public abstract LanguageService getLanguage();

    public abstract DebugService getDebug();

    public abstract Builder[] getBuilders();

    public abstract Target[] getTargets();

    public void onCurrentDirectoryChanged(String directory) {}

    public Configuration getAppSettings() {
        return null;
    }

    public ProjectSettingsPage[] getProjectSettingsPages() {
        return new ProjectSettingsPage[0];
    }
}
