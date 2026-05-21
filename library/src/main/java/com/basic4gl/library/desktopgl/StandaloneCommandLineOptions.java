package com.basic4gl.library.desktopgl;


public class StandaloneCommandLineOptions {

    static final String CONFIG_FILE = "config.ser"; // Filename for configuration file
    static final String STATE_FILE = "state.bin";

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

    /**
     * Program should display help
     */
    boolean displayHelp = false;

    /**
     * Program should display version and exit
     */
    boolean displayVersion = false;

    /**
     * Program args used to initialize function libraries
     */
    String[] programArgs = new String[0];
}
