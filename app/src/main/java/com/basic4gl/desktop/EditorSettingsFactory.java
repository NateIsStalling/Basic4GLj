package com.basic4gl.desktop;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class EditorSettingsFactory {
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static final String CONFIG_RECENT_FILES = "RECENT_FILES";
    public static final String CONFIG_PLUGIN_DIRECTORY = "PLUGIN_DIRECTORY";
    public static final String CONFIG_RECENT_PLUGIN_DIRECTORIES = "RECENT_PLUGIN_DIRECTORIES";

    public static final int CONFIG_RECENT_FILES_MAX_COUNT = 10;
    public static final int CONFIG_RECENT_PLUGIN_DIRECTORIES_MAX_COUNT = 10;

    public static EditorSettings loadFrom(String applicationStoragePath) throws FileNotFoundException, IOException {
        File configFile = new File(applicationStoragePath, CONFIG_FILE_NAME);

        EditorSettings settings = new EditorSettings();

        if (configFile.exists()) {
            FileInputStream propsInput = null;
            propsInput = new FileInputStream(configFile);

            Properties prop = new Properties();
            prop.load(propsInput);

            String recentFiles = prop.getProperty(CONFIG_RECENT_FILES, "");
            settings.recentFiles.addAll(Arrays.stream(recentFiles.split(","))
                    .map(File::new)
                    .filter(File::exists)
                    .distinct()
                    .limit(CONFIG_RECENT_FILES_MAX_COUNT)
                    .toList());
            String currentPluginDirectory =
                    prop.getProperty(CONFIG_PLUGIN_DIRECTORY, "").trim();
            settings.currentPluginDirectory = currentPluginDirectory.isEmpty() ? null : currentPluginDirectory;
            String recentPluginDirectories = prop.getProperty(CONFIG_RECENT_PLUGIN_DIRECTORIES, "");
            settings.recentPluginDirectories.addAll(Arrays.stream(recentPluginDirectories.split(","))
                    .map(File::new)
                    .filter(File::exists)
                    .filter(File::isDirectory)
                    .distinct()
                    .limit(CONFIG_RECENT_PLUGIN_DIRECTORIES_MAX_COUNT)
                    .toList());
        } else {
            System.out.println("Settings file not found: " + configFile.getAbsolutePath());
        }

        return settings;
    }

    public static void save(EditorSettings settings, String applicationStoragePath) throws IOException {
        File configFile = new File(applicationStoragePath, CONFIG_FILE_NAME);
        String[] recentFilePaths = settings.recentFiles.stream()
                .map(File::getAbsolutePath)
                .distinct()
                .limit(CONFIG_RECENT_FILES_MAX_COUNT)
                .toArray(String[]::new);
        String recentFiles = String.join(",", recentFilePaths);
        String[] recentPluginDirectoryPaths = settings.recentPluginDirectories.stream()
                .map(File::getAbsolutePath)
                .distinct()
                .limit(CONFIG_RECENT_PLUGIN_DIRECTORIES_MAX_COUNT)
                .toArray(String[]::new);
        String recentPluginDirectories = String.join(",", recentPluginDirectoryPaths);

        Properties prop = new Properties();
        prop.setProperty(CONFIG_RECENT_FILES, recentFiles);
        prop.setProperty(
                CONFIG_PLUGIN_DIRECTORY,
                settings.currentPluginDirectory == null ? "" : settings.currentPluginDirectory.trim());
        prop.setProperty(CONFIG_RECENT_PLUGIN_DIRECTORIES, recentPluginDirectories);
        prop.store(new FileWriter(configFile), "store properties to file");
    }
}
