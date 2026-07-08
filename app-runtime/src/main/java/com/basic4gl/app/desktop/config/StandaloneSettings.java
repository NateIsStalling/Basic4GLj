package com.basic4gl.app.desktop.config;

import com.basic4gl.language.core.runtime.Configuration;

public class StandaloneSettings implements IStandaloneSettings {

    public static final int SETTING_TITLE = 1; // Index of window title setting in config
    public static final int SETTING_VERSION = 2; // Index of version string setting in config
    public static final int SETTING_WIDTH = 3; // Index of window width setting in config
    public static final int SETTING_HEIGHT = 4; // Index of window height setting in config
    public static final int SETTING_RESIZABLE = 5; // Index of window resizable setting in config
    public static final int SETTING_SCREEN_MODE = 6; // Index of screen mode setting in config

    public static final int SETTING_USE_DESKTOP_RES = 7; // Index of desktop resolution toggle in config
    public static final int SETTING_COLOUR_DEPTH = 8; // Index of fullscreen color depth setting in config
    public static final int SETTING_BORDER = 9; // Index of bordered window setting in config
    public static final int SETTING_STENCIL = 10; // Index of stencil buffer setting in config
    public static final int SETTING_STARTUP_WINDOW_OPTION = 11; // Index of startup window creation mode

    public static final int SETTING_PROGRAM_END_OPTION = 14; // Index of behavior when program finishes
    public static final int SETTING_RUNTIME_ERROR_OPTION = 15; // Index of behavior when runtime errors occur
    public static final int SETTING_ESC_KEY_QUITS = 16; // Index of esc-key quit toggle
    public static final int SETTING_CLOSING_WINDOW_QUITS = 17; // Index of close-window quit toggle
    public static final int SETTING_APP_DATA_DIRECTORY = 18; // Index of app data folder override setting
    public static final int SETTING_SUPPORT_WINDOWS = 21; // Index of Windows support setting in config
    public static final int SETTING_SUPPORT_MAC = 22; // Index of Mac support setting in config
    public static final int SETTING_SUPPORT_LINUX = 23; // Index of Linux support setting in config

    public static final int SUPPORT_WINDOWS_32_64 = 0;
    public static final int SUPPORT_WINDOWS_32 = 1;
    public static final int SUPPORT_WINDOWS_64 = 2;
    public static final int SUPPORT_WINDOWS_NO = 3;

    public static final int SUPPORT_MAC_32_64 = 0;
    public static final int SUPPORT_MAC_NO = 1;

    public static final int SUPPORT_LINUX_32_64 = 0;

    public static final int MODE_WINDOWED = 0;
    public static final int MODE_FULLSCREEN = 1;

    public static final int COLOUR_DEPTH_DEFAULT = 0;
    public static final int COLOUR_DEPTH_16BIT = 1;
    public static final int COLOUR_DEPTH_32BIT = 2;

    public static final int STARTUP_WINDOW_CREATE_IMMEDIATELY = 0;
    public static final int STARTUP_WINDOW_DEFERRED = 1;

    public static final int PROGRAM_END_CLOSE_IMMEDIATELY = 0;
    public static final int PROGRAM_END_WAIT_KEYPRESS = 1;
    public static final int PROGRAM_END_WAIT_WINDOW_CLOSE = 2;

    public static final int RUNTIME_ERROR_JUST_CLOSE = 0;
    public static final int RUNTIME_ERROR_SHOW_GENERAL_MESSAGE = 1;
    public static final int RUNTIME_ERROR_SHOW_DETAILED_MESSAGE = 2;

    @Override
    public Configuration getSettings() {
        Configuration settings = new Configuration();
        settings.addSetting(new String[] {"Window Config"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(new String[] {"Window Title"}, Configuration.PARAM_STRING, "My Application");
        settings.addSetting(new String[] {"Program Version"}, Configuration.PARAM_STRING, "1.0");
        settings.addSetting(new String[] {"Window Width"}, Configuration.PARAM_INT, "640");
        settings.addSetting(new String[] {"Window Height"}, Configuration.PARAM_INT, "480");
        settings.addSetting(new String[] {"Resizable Window"}, Configuration.PARAM_BOOL, "false");
        settings.addSetting(new String[] {"Screen Mode", "Windowed", "Fullscreen"}, Configuration.PARAM_CHOICE, "0");
        settings.addSetting(new String[] {"Use Desktop Resolution"}, Configuration.PARAM_BOOL, "false");
        settings.addSetting(
                new String[] {"Colour Depth", "Default", "16-bit", "32-bit"},
                Configuration.PARAM_CHOICE,
                String.valueOf(COLOUR_DEPTH_DEFAULT));
        settings.addSetting(new String[] {"Bordered Window"}, Configuration.PARAM_BOOL, "true");
        settings.addSetting(new String[] {"Require Stencil Buffer"}, Configuration.PARAM_BOOL, "false");
        settings.addSetting(
                new String[] {
                    "Startup Window",
                    "Create window when application starts",
                    "Don't create window until UpdateWindow() is called"
                },
                Configuration.PARAM_CHOICE,
                String.valueOf(STARTUP_WINDOW_CREATE_IMMEDIATELY));

        settings.addSetting(new String[] {}, Configuration.PARAM_DIVIDER, "");
        settings.addSetting(new String[] {"Standalone App Settings"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(
                new String[] {
                    "When program finishes",
                    "Close immediately",
                    "Wait for keypress then close",
                    "Wait for window to be closed"
                },
                Configuration.PARAM_CHOICE,
                String.valueOf(PROGRAM_END_WAIT_WINDOW_CLOSE));
        settings.addSetting(
                new String[] {
                    "If a runtime error occurs",
                    "Just close",
                    "Show a general \"An error has occurred\" message",
                    "Show error complete with line number"
                },
                Configuration.PARAM_CHOICE,
                String.valueOf(RUNTIME_ERROR_SHOW_DETAILED_MESSAGE));
        settings.addSetting(
                new String[] {"\"Esc\" key quits"},
                Configuration.PARAM_BOOL,
                "true",
                "If you disable this option, make sure you build a QUIT function into your program!");
        settings.addSetting(
                new String[] {"Closing Window quits"},
                Configuration.PARAM_BOOL,
                "true",
                "If you disable this option, make sure you build a QUIT function into your program!");
        settings.addSetting(
                new String[] {"AppData Directory"},
                Configuration.PARAM_STRING,
                "Basic4GL",
                "Override the root AppData folder name used by OpenAppDataRead/OpenAppDataWrite in exported programs.");
        settings.addSetting(new String[] {}, Configuration.PARAM_DIVIDER, "");
        settings.addSetting(new String[] {"Platforms"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(
                new String[] {"Windows Support", "32/64-bit", "32-bit", "64-bit", "Do not support"},
                Configuration.PARAM_CHOICE,
                "0");
        settings.addSetting(
                new String[] {"Mac Support", "32/64-bit", "Do not support"}, Configuration.PARAM_CHOICE, "0");
        settings.addSetting(
                new String[] {"Linux Support", "32/64-bit", "Do not support"}, Configuration.PARAM_CHOICE, "0");

        return settings;
    }
}
