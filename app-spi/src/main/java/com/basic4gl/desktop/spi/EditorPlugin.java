package com.basic4gl.desktop.spi;

import com.basic4gl.desktop.spi.language.LanguageSupport;

public abstract class EditorPlugin {
    public String getId() {
        String pluginName = getName();
        String normalizedName = pluginName == null
                ? ""
                : pluginName.trim().replaceAll("\\s+", "-").toLowerCase();
        if (normalizedName.isBlank()) {
            return getClass().getName();
        }
        return getClass().getName() + ":" + normalizedName;
    }

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
    // TODO this should be renamed
    public abstract LanguageService getLanguage();
    // TODO this should be renamed
    public abstract LanguageSupport getLanguageSupport();

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

    public ProjectExportPage[] getProjectExportPages() {
        return new ProjectExportPage[0];
    }
}
