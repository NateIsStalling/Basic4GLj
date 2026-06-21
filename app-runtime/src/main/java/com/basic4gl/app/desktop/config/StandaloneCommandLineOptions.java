package com.basic4gl.app.desktop.config;

public class StandaloneCommandLineOptions {

    public static final String CONFIG_FILE = "config.ser"; // Filename for configuration file
    public static final String STATE_FILE = "state.bin";

    /**
     *  Load VM's state from file
     */
    public String stateFile = "/" + STATE_FILE;

    public String configFile = "/" + CONFIG_FILE;
    public String mappingFile = null;
    public String currentDirectory = null;
    public String debugServerPort = null;
    public String logFilePath = null;

    public boolean isSafeModeEnabled = false;

    private boolean displayHelp = false;

    private boolean displayVersion = false;

    private String[] programArgs = new String[0];

    /**
     * Program should display help
     */
    public boolean shouldDisplayHelp() {
        return displayHelp;
    }

    public void setDisplayHelp(boolean displayHelp) {
        this.displayHelp = displayHelp;
    }

    /**
     * Program should display version and exit
     */
    public boolean shouldDisplayVersion() {
        return displayVersion;
    }

    public void setDisplayVersion(boolean displayVersion) {
        this.displayVersion = displayVersion;
    }

    /**
     * Program args used to initialize function libraries
     */
    public String[] getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(String[] programArgs) {
        this.programArgs = programArgs;
    }
}
