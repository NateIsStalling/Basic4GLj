package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.EditorAppSettings;
import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.Configuration;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationMapper {
    private static final int APP_SETTING_SANDBOX_MODE = 1;
    private static final int APP_SETTING_SYNTAX = 2;
    private static final int APP_SETTING_PROGRAM_ARGUMENTS = 3;
    private static final int APP_SETTING_JVM_ARGUMENTS = 4;
    private static final int APP_SETTING_JVM_DEBUG_ENABLED = 5;
    private static final int APP_SETTING_JVM_DEBUG_SUSPEND = 6;
    private static final int APP_SETTING_JVM_DEBUG_PORT = 7;
    private static final int APP_SETTING_PLUGIN_DIRECTORY = 8;

    public static Configuration toEditorConfiguration(com.basic4gl.language.core.runtime.Configuration config) {
        if (config == null) {
            return null;
        }

        Configuration mapped = new Configuration();
        for (int i = 0; i < config.getSettingCount(); i++) {
            mapped.addSetting(
                    config.getField(i).clone(),
                    config.getParamType(i),
                    config.getValue(i),
                    config.getFieldInfoText(i));
        }
        return mapped;
    }

    public static Configuration toEditorConfiguration(IConfigurableAppSettings config) {
        if (config == null) {
            return null;
        }

        Configuration settings = new Configuration();
        settings.addSetting(new String[] {"Editor"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(new String[] {"Sandbox Mode"}, Configuration.PARAM_BOOL, Boolean.toString(config.isSandboxModeEnabled()));
        settings.addSetting(new String[] {"Syntax"}, Configuration.PARAM_INT, Integer.toString(config.getSyntax()));
        settings.addSetting(
                new String[] {"Program Arguments"}, Configuration.PARAM_STRING, serializeArguments(config.getProgramArguments()));
        settings.addSetting(new String[] {"JVM Arguments"}, Configuration.PARAM_STRING, serializeArguments(config.getJvmArguments()));
        settings.addSetting(
                new String[] {"Enable JVM Debugger"}, Configuration.PARAM_BOOL, Boolean.toString(config.isJvmDebuggingEnabled()));
        settings.addSetting(
                new String[] {"Suspend Until Attach"},
                Configuration.PARAM_BOOL,
                Boolean.toString(config.isJvmDebugSuspendUntilAttach()));
        Integer debugPort = config.getJvmDebugPortOverride();
        settings.addSetting(new String[] {"Debug Port Override"}, Configuration.PARAM_STRING, debugPort == null ? "" : debugPort.toString());
        settings.addSetting(
                new String[] {"Plugin Directories"},
                Configuration.PARAM_STRING,
                serializeArguments(config.getPluginDirectories()));
        return settings;
    }

    public static com.basic4gl.language.core.runtime.Configuration toRuntimeConfiguration(Configuration config) {
        if (config == null) {
            return null;
        }

        com.basic4gl.language.core.runtime.Configuration mapped = new com.basic4gl.language.core.runtime.Configuration();
        for (int i = 0; i < config.getSettingCount(); i++) {
            mapped.addSetting(
                    config.getField(i).clone(),
                    config.getParamType(i),
                    config.getValue(i),
                    config.getFieldInfoText(i));
        }
        return mapped;
    }

    public static IConfigurableAppSettings toAppSettings(Configuration config) {
        EditorAppSettings settings = new EditorAppSettings();
        if (config == null || config.getSettingCount() == 0) {
            return settings;
        }

        settings.setSandboxModeEnabled(config.getBooleanValueOrDefault(APP_SETTING_SANDBOX_MODE, settings.isSandboxModeEnabled()));
        settings.setSyntax(config.getIntValueOrDefault(APP_SETTING_SYNTAX, settings.getSyntax()));
        settings.setProgramArguments(parseArguments(getValueOrNull(config, APP_SETTING_PROGRAM_ARGUMENTS)));
        settings.setJvmArguments(parseArguments(getValueOrNull(config, APP_SETTING_JVM_ARGUMENTS)));
        settings.setJvmDebuggingEnabled(
                config.getBooleanValueOrDefault(APP_SETTING_JVM_DEBUG_ENABLED, settings.isJvmDebuggingEnabled()));
        settings.setJvmDebugSuspendUntilAttach(
                config.getBooleanValueOrDefault(APP_SETTING_JVM_DEBUG_SUSPEND, settings.isJvmDebugSuspendUntilAttach()));

        String debugPortValue = getValueOrNull(config, APP_SETTING_JVM_DEBUG_PORT);
        if (debugPortValue != null && !debugPortValue.trim().isEmpty()) {
            try {
                settings.setJvmDebugPortOverride(Integer.parseInt(debugPortValue.trim()));
            } catch (NumberFormatException ignored) {
                settings.setJvmDebugPortOverride(null);
            }
        } else {
            settings.setJvmDebugPortOverride(null);
        }

        settings.setPluginDirectories(parseArguments(getValueOrNull(config, APP_SETTING_PLUGIN_DIRECTORY)));
        return settings;
    }

    private static String serializeArguments(List<String> args) {
        if (args == null || args.isEmpty()) {
            return "";
        }
        return String.join(System.lineSeparator(), args);
    }

    private static List<String> parseArguments(String value) {
        List<String> args = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return args;
        }

        String[] lines = value.split("\\R", -1);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            args.add(line.trim());
        }
        return args;
    }

    private static String getValueOrNull(Configuration config, int index) {
        if (config == null || index < 0 || index >= config.getSettingCount()) {
            return null;
        }
        return config.getValue(index);
    }
}
