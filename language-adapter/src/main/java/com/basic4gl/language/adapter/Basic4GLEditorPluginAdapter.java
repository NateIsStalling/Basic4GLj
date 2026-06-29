package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.EditorAppSettings;
import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.language.adapter.menu.ReferenceWindow;
import com.basic4gl.library.plugin.PluginJARManager;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;

import java.util.Arrays;

public class Basic4GLEditorPluginAdapter extends EditorPlugin {

    private PluginJARManager plugins;

    private final TomVM vm;
    private final TomBasicCompiler compiler;
    private final LanguageService languageService;
    private final CompilerService compilerService;
    private final DebugService debugService;
    private final PreprocessorService preprocessorService;
    // Runtime settings
    private final IConfigurableAppSettings appSettings = new EditorAppSettings();
    private Runnable onPluginStateChanged = () -> {};
    private Builder[] builders = new Builder[0];

    private PluginContext context;

    public Basic4GLEditorPluginAdapter(PluginContext context) {
        plugins = new PluginJARManager(false);
        Preprocessor preprocessor = new Preprocessor(2, Arrays.stream(context.fileServices()).map(FileServiceAdapter::new).toArray(FileServiceAdapter[]::new));
        Debugger debugger = new Debugger(preprocessor.getLineNumberMap());
        vm = new TomVM(plugins, debugger);
        compiler = new TomBasicCompiler(vm, plugins);
        languageService = new Basic4GLLanguageService(compiler, preprocessor, plugins);
        compilerService = new Basic4GLCompilerService(compiler, preprocessor);
        debugService = new Basic4GLDebugService(compiler, preprocessor, appSettings, debugger);
        preprocessorService = new Basic4GLPreprocessorService(compiler, preprocessor);
    }


    @Override
    public String getName() {
        return "Basic4GLj";
    }

    @Override
    public String getDescription() {
        return "Basic4GL for Java support";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getAuthor() {
        return "Nathaniel Nielsen";
    }

    @Override
    public CompilerService getCompiler() {
        return compilerService;
    }

    @Override
    public PreprocessorService getPreprocessor() {
        return preprocessorService;
    }

    @Override
    public LanguageService getLanguage() {
        return languageService;
    }

    @Override
    public DebugService getDebug() {
        return debugService;
    }

    @Override
    public Builder[] getBuilders() {
        if (context == null) {
            return new Builder[0];
        }

        if (builders.length > 0) {
            return builders;
        }

        // TODO DesktopTarget needs implementation
        Builder builder = BuilderDesktopGL.getInstance(new DesktopTarget(compiler));
        builder.init(context.files());

        builders = new Builder[] {
            builder
        };
        return builders;
    }

    @Override
    public Target[] getTargets() {
        return new Target[0];
    }

    @Override
    public void onCloseAll() {
        super.onCloseAll();

        // Clear plugins, breakpoints, bookmarks etc
        this.plugins.clear();
        debugService.clearUserBreakPoints();
    }

    @Override
    public void onCurrentDirectoryChanged(String directory) {
        super.onCurrentDirectoryChanged(directory);
        if (plugins != null) {
            if (appSettings.getPluginDirectory() == null || appSettings.getPluginDirectory().isBlank()) {
                plugins.setDirectory(directory);
            }
        }
    }

    @Override
    public void onLoad(PluginContext context) {
        super.onLoad(context);

        this.context = context;
        this.builders = new Builder[0];

        context.menus().addHelp("Function List",(parent, e) -> {
            ReferenceWindow window = new ReferenceWindow(parent);
            window.populate(compiler);
            window.setVisible(true);
        });
        applyPluginSettingsToManager();
    }

    @Override
    public Configuration getAppSettings() {
        return ConfigurationMapper.toEditorConfiguration(appSettings);
    }

    @Override
    public ProjectSettingsPage[] getProjectSettingsPages() {
        return new ProjectSettingsPage[] {
            new SafeModeProjectSettingsPage(appSettings),
            new JvmProjectSettingsPage(appSettings),
            new PluginManagerProjectSettingsPage(
                    appSettings,
                    plugins,
                    this::getDefaultPluginDirectory,
                    this::applyPluginSettingsToManager,
                    this::notifyPluginStateChanged)
        };
    }

    public IConfigurableAppSettings getConfigurableAppSettings() {
        return appSettings;
    }

    public void setOnPluginStateChanged(Runnable onPluginStateChanged) {
        this.onPluginStateChanged = onPluginStateChanged == null ? () -> {} : onPluginStateChanged;
    }

    private String getDefaultPluginDirectory() {
        return context == null ? "" : context.currentDirectory();
    }

    private void applyPluginSettingsToManager() {
        if (plugins == null) {
            return;
        }
        String configuredPluginDirectory = appSettings.getPluginDirectory();
        if (configuredPluginDirectory != null && !configuredPluginDirectory.isBlank()) {
            plugins.setDirectory(configuredPluginDirectory);
            return;
        }
        plugins.setDirectory(getDefaultPluginDirectory());
    }

    private void notifyPluginStateChanged() {
        onPluginStateChanged.run();
    }
}
