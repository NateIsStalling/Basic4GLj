package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.EditorAppSettings;
import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.desktop.spi.language.LanguageSupport;
import com.basic4gl.language.adapter.menu.ReferenceWindow;
import com.basic4gl.library.plugin.PluginJAR;
import com.basic4gl.library.plugin.PluginJARDetails;
import com.basic4gl.library.plugin.PluginJARFile;
import com.basic4gl.library.plugin.PluginJARManager;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Basic4GLEditorPluginAdapter extends EditorPlugin {
    private static final int MAX_RECENT_PLUGIN_DIRECTORIES = 10;

    private PluginJARManager plugins;

    private final TomVM vm;
    private final TomBasicCompiler compiler;
    private final LanguageService languageService;
    private final LanguageSupport languageSupport = new Basic4GLLanguageSupport();
    private final CompilerService compilerService;
    private final DebugService debugService;
    private final PreprocessorService preprocessorService;
    // Runtime settings
    private final IConfigurableAppSettings appSettings = new EditorAppSettings();
    private Runnable onPluginStateChanged = () -> {};
    private Runnable onPluginDirectoryHistoryChanged = () -> {};
    private final List<String> recentPluginDirectories = new ArrayList<>();
    private Builder[] builders = new Builder[0];

    private PluginContext context;

    public Basic4GLEditorPluginAdapter(PluginContext context) {
        plugins = new PluginJARManager(false);
        Preprocessor preprocessor = new Preprocessor(
                plugins,
                2,
                Arrays.stream(context.fileServices())
                        .map(FileServiceAdapter::new)
                        .toArray(FileServiceAdapter[]::new));
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
    public LanguageSupport getLanguageSupport() {
        return languageSupport;
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

        builders = new Builder[] {builder};
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
            plugins.setCurrentDirectory(directory);
        }
        if (plugins != null && appSettings.getPluginDirectories().isEmpty()) {
            applyPluginSettingsToManager();
        }
    }

    @Override
    public void onLoad(PluginContext context) {
        super.onLoad(context);

        this.context = context;
        this.builders = new Builder[0];

        context.menus().addHelp("Function List", (parent, e) -> {
            ReferenceWindow window = new ReferenceWindow(parent);
            window.populate(compiler);
            window.setVisible(true);
        });
        plugins.setCurrentDirectory(getDefaultPluginDirectory());
        applyPluginSettingsToManager();
        attemptLoadPluginsFromCurrentDirectory();
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
                    this::getRecentPluginDirectories,
                    this::applyPluginSettingsToManager,
                    this::notifyPluginStateChanged)
        };
    }

    @Override
    public ProjectExportPage[] getProjectExportPages() {
        return new ProjectExportPage[] {new PluginExportProjectPage(plugins, this::getDefaultPluginDirectory)};
    }

    public IConfigurableAppSettings getConfigurableAppSettings() {
        return appSettings;
    }

    public void setOnPluginStateChanged(Runnable onPluginStateChanged) {
        this.onPluginStateChanged = onPluginStateChanged == null ? () -> {} : onPluginStateChanged;
    }

    public void setOnPluginDirectoryHistoryChanged(Runnable onPluginDirectoryHistoryChanged) {
        this.onPluginDirectoryHistoryChanged =
                onPluginDirectoryHistoryChanged == null ? () -> {} : onPluginDirectoryHistoryChanged;
    }

    public void restorePluginDirectoryState(String configuredDirectory, List<String> recentDirectories) {
        recentPluginDirectories.clear();
        if (recentDirectories != null) {
            for (String recentDirectory : recentDirectories) {
                addRecentPluginDirectory(recentDirectory, false);
            }
        }
        String normalizedConfigured = normalizeNullable(configuredDirectory);
        if (normalizedConfigured == null && !recentPluginDirectories.isEmpty()) {
            normalizedConfigured = recentPluginDirectories.get(0);
        }
        appSettings.setPluginDirectory(normalizedConfigured);
        if (appSettings.getPluginDirectory() != null) {
            addRecentPluginDirectory(appSettings.getPluginDirectory(), false);
        }
        plugins.clear();
        applyPluginSettingsToManager();
        attemptLoadPluginsFromCurrentDirectory();
    }

    public List<String> getRecentPluginDirectories() {
        return List.copyOf(recentPluginDirectories);
    }

    public String getConfiguredPluginDirectory() {
        return appSettings.getPluginDirectory();
    }

    public List<String> getConfiguredPluginDirectories() {
        return appSettings.getPluginDirectories();
    }

    public String getActivePluginDirectory() {
        String directory = plugins == null ? null : plugins.getDirectory();
        if (directory == null || directory.isBlank()) {
            return null;
        }
        if (directory.endsWith("\\") || directory.endsWith("/")) {
            return directory.substring(0, directory.length() - 1);
        }
        return directory;
    }

    public void onFileOpened(String sourceText) {
        Set<String> pluginDeclarations = extractPluginDeclarations(sourceText);
        if (pluginDeclarations.isEmpty()) {
            return;
        }
        boolean loadedAny = false;
        for (String declaration : pluginDeclarations) {
            String matchingFilename = findBestMatchingPluginFilename(declaration);
            if (matchingFilename == null || plugins.isLoaded(matchingFilename)) {
                continue;
            }
            if (plugins.loadPlugin(matchingFilename)) {
                loadedAny = true;
            }
        }
        if (loadedAny) {
            notifyPluginStateChanged();
        }
    }

    public String appendEnabledPluginDirectives(String sourceText) {
        String source = sourceText == null ? "" : sourceText;
        Set<String> declaredPlugins = extractPluginDeclarations(source);
        List<String> enabledPluginNames = getEnabledPluginNames();
        if (enabledPluginNames.isEmpty()) {
            return source;
        }

        StringBuilder appended = new StringBuilder(source);
        boolean changed = false;
        for (String pluginName : enabledPluginNames) {
            String normalized = normalizePluginToken(pluginName);
            if (normalized == null || declaredPlugins.contains(normalized)) {
                continue;
            }
            if (appended.length() > 0 && appended.charAt(appended.length() - 1) != '\n') {
                appended.append(System.lineSeparator());
            }
            appended.append("#plugin ").append(pluginName).append(System.lineSeparator());
            declaredPlugins.add(normalized);
            changed = true;
        }
        return changed ? appended.toString() : source;
    }

    private String getDefaultPluginDirectory() {
        String fromContext = context == null ? null : normalizeNullable(context.currentDirectory());
        if (fromContext != null) {
            return fromContext;
        }
        return plugins == null ? "" : normalizeNullable(plugins.getCurrentDirectory());
    }

    private void applyPluginSettingsToManager() {
        if (plugins == null) {
            return;
        }
        String currentDirectory = normalizeNullable(getDefaultPluginDirectory());
        if (currentDirectory == null) {
            currentDirectory = normalizeNullable(plugins.getCurrentDirectory());
        }
        plugins.setCurrentDirectory(currentDirectory);

        List<String> sourceDirectories = buildSourceDirectories(currentDirectory);
        appSettings.setPluginDirectories(sourceDirectories);
        plugins.setDirectories(sourceDirectories);
        for (String directory : sourceDirectories) {
            addRecentPluginDirectory(directory, false);
        }
        onPluginDirectoryHistoryChanged.run();
    }

    private List<String> buildSourceDirectories(String currentDirectory) {
        ArrayList<String> sources = new ArrayList<>();

        List<String> configuredDirectories = appSettings.getPluginDirectories();
        if (configuredDirectories != null && !configuredDirectories.isEmpty()) {
            for (String directory : configuredDirectories) {
                addSourceIfMissing(sources, normalizeNullable(directory));
            }
        } else {
            String configuredDirectory = normalizeNullable(appSettings.getPluginDirectory());
            if (configuredDirectory != null) {
                addSourceIfMissing(sources, configuredDirectory);
            }
        }

        if (sources.isEmpty()) {
            addSourceIfMissing(sources, normalizeNullable(currentDirectory));
        }

        for (String directory : recentPluginDirectories) {
            addSourceIfMissing(sources, normalizeNullable(directory));
        }

        if (sources.isEmpty()) {
            return sources;
        }

        ArrayList<String> prioritized = new ArrayList<>();
        prioritized.add(sources.get(0));
        if (sources.size() > 1) {
            ArrayList<String> remaining = new ArrayList<>(sources.subList(1, sources.size()));
            remaining.sort(String.CASE_INSENSITIVE_ORDER);
            prioritized.addAll(remaining);
        }
        return prioritized;
    }

    private void addSourceIfMissing(List<String> sources, String directory) {
        if (sources == null || directory == null) {
            return;
        }
        for (String existing : sources) {
            if (existing.equalsIgnoreCase(directory)) {
                return;
            }
        }
        sources.add(directory);
    }

    private void notifyPluginStateChanged() {
        onPluginStateChanged.run();
    }

    private void attemptLoadPluginsFromCurrentDirectory() {
        boolean loadedAny = false;
        for (PluginJARFile file : plugins.getJARFiles()) {
            if (!file.isCompatible() || plugins.isLoaded(file.getFilename())) {
                continue;
            }
            if (plugins.loadPlugin(file.getFilename())) {
                loadedAny = true;
            }
        }
        if (loadedAny) {
            notifyPluginStateChanged();
        }
    }

    private void addRecentPluginDirectory(String directory, boolean notify) {
        String normalized = normalizeNullable(directory);
        if (normalized == null) {
            return;
        }
        recentPluginDirectories.removeIf(item -> item.equalsIgnoreCase(normalized));
        recentPluginDirectories.add(0, normalized);
        while (recentPluginDirectories.size() > MAX_RECENT_PLUGIN_DIRECTORIES) {
            recentPluginDirectories.remove(recentPluginDirectories.size() - 1);
        }
        if (notify) {
            onPluginDirectoryHistoryChanged.run();
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<String> extractPluginDeclarations(String sourceText) {
        Set<String> declarations = new LinkedHashSet<>();
        if (sourceText == null || sourceText.isBlank()) {
            return declarations;
        }
        String[] lines = sourceText.split("\\R", -1);
        for (String line : lines) {
            String directiveValue = Preprocessor.extractPluginDirectiveValue(line);
            String normalized = normalizePluginToken(directiveValue);
            if (normalized != null) {
                declarations.add(normalized);
            }
        }
        return declarations;
    }

    private String normalizePluginToken(String declaration) {
        if (declaration == null) {
            return null;
        }
        String normalized = declaration.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (normalized.startsWith("'") && normalized.endsWith("'") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        int separatorIndex = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (separatorIndex >= 0 && separatorIndex < normalized.length() - 1) {
            normalized = normalized.substring(separatorIndex + 1);
        }
        normalized = stripKnownExtension(normalized.toLowerCase(Locale.ROOT));
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                token.append(ch);
            } else {
                break;
            }
        }
        return token.length() == 0 ? null : token.toString();
    }

    private String stripKnownExtension(String value) {
        if (value.endsWith(".dll") || value.endsWith(".jar")) {
            return value.substring(0, value.length() - 4);
        }
        return value;
    }

    private String findBestMatchingPluginFilename(String declaration) {
        int bestScore = -1;
        String bestFilename = null;
        for (PluginJARFile pluginFile : plugins.getJARFiles()) {
            if (!pluginFile.isCompatible()) {
                continue;
            }
            String filename = pluginFile.getFilename();
            if (filename == null || filename.isBlank()) {
                continue;
            }

            int score = scorePluginMatch(declaration, filename);
            if (score < 0) {
                score = scorePluginNameMatch(declaration, filename);
            }
            if (score > bestScore) {
                bestScore = score;
                bestFilename = filename;
            }
        }
        return bestFilename;
    }

    private int scorePluginMatch(String declaration, String filename) {
        String normalizedDeclaration = normalizePluginToken(declaration);
        if (normalizedDeclaration == null) {
            return -1;
        }
        String lowerFilename = filename.toLowerCase(Locale.ROOT);
        if (lowerFilename.equals(normalizedDeclaration)
                || stripKnownExtension(lowerFilename).equals(normalizedDeclaration)) {
            return 3;
        }
        String normalizedFilename = normalizePluginToken(filename);
        if (normalizedFilename != null && normalizedFilename.equals(normalizedDeclaration)) {
            return 3;
        }
        String filenameNoExt = stripKnownExtension(lowerFilename);
        if (filenameNoExt.startsWith(normalizedDeclaration)) {
            return 1;
        }
        return -1;
    }

    private int scorePluginNameMatch(String declaration, String filename) {
        String normalizedDeclaration = normalizePluginToken(declaration);
        String pluginName = resolvePluginName(filename);
        String normalizedPluginName = normalizePluginToken(pluginName);
        if (normalizedDeclaration == null || normalizedPluginName == null) {
            return -1;
        }
        return normalizedPluginName.equals(normalizedDeclaration) ? 2 : -1;
    }

    private String resolvePluginName(String filename) {
        PluginJAR loaded = plugins.find(filename);
        if (loaded != null
                && loaded.getMetadata() != null
                && loaded.getMetadata().name() != null
                && !loaded.getMetadata().name().isBlank()) {
            return loaded.getMetadata().name();
        }

        PluginJARDetails details = plugins.getPluginDetails(filename);
        if (details != null) {
            String metadataDetails = details.getMetadataDetails();
            if (metadataDetails != null) {
                String[] lines = metadataDetails.split("\\R", -1);
                for (String line : lines) {
                    if (line.regionMatches(true, 0, "Name:", 0, "Name:".length())) {
                        String value = line.substring("Name:".length()).trim();
                        if (!value.isEmpty()) {
                            return value;
                        }
                    }
                }
            }
        }
        return filename;
    }

    private List<String> getEnabledPluginNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (PluginJAR jar : plugins.loadedJARs()) {
            String name = jar.getMetadata() != null ? jar.getMetadata().name() : null;
            if (name == null || name.isBlank()) {
                name = stripKnownExtension(jar.getFilename());
            }
            if (name != null && !name.isBlank()) {
                names.add(name.trim());
            }
        }
        return new ArrayList<>(names);
    }
}
