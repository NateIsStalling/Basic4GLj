package com.basic4gl.app.desktop.config;

import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.cli.*;

public class StandaloneCommandLineOptionsParser implements ITargetCommandLineOptions {
    // Predefined CLI options used by Basic4GLj debugger
    static final String CONFIG_FILE_PATH_OPTION = "c";
    static final String PROGRAM_FILE_PATH_OPTION = "p";
    static final String LINE_MAPPING_FILE_PATH_OPTION = "m";
    static final String LOG_FILE_PATH_OPTION = "logfile";
    static final String PARENT_DIRECTORY_OPTION = "parent";
    static final String DEBUGGER_PORT_OPTION = "d";
    static final String SAFE_MODE_OPTION = "s";

    private Options options;
    private Option help;
    private Option version;
    private Option safeModeOption;
    private Option logFileOption;
    private Option configFileOption;
    private Option mappingFileOption;
    private Option stateFileOption;
    private Option parentPathOption;
    private Option debugPortOption;

    public StandaloneCommandLineOptionsParser() {
        options = new Options();
        help = Option.builder("h").longOpt("help").desc("print this message").build();

        version = Option.builder("v")
                .longOpt("version")
                .desc("print the version information and exit")
                .build();

        safeModeOption = Option.builder("s")
                .longOpt("safe-mode-enabled")
                .desc("run the program in safe mode")
                .build();

        logFileOption = Option.builder(LOG_FILE_PATH_OPTION)
                .argName("file")
                .hasArg()
                .desc("use given file path for log output")
                .build();

        configFileOption = Option.builder(CONFIG_FILE_PATH_OPTION)
                .longOpt("config-path")
                .argName("file")
                .hasArg()
                .desc("use given file for program config")
                .build();

        stateFileOption = Option.builder(PROGRAM_FILE_PATH_OPTION)
                .longOpt("program-path")
                .argName("file")
                .hasArg()
                .desc("use given file path to load compiled program code")
                .build();

        mappingFileOption = Option.builder(LINE_MAPPING_FILE_PATH_OPTION)
                .longOpt("line-mapping-path")
                .argName("file")
                .hasArg()
                .desc("use given file path for program line mapping")
                .build();

        parentPathOption = Option.builder(PARENT_DIRECTORY_OPTION)
                .argName("directory")
                .hasArg()
                .desc("use given directory path for program parent directory")
                .build();

        debugPortOption = Option.builder(DEBUGGER_PORT_OPTION)
                .longOpt("debugger-port")
                .argName("port")
                .hasArg()
                .desc("use given port to connect to the debugger")
                .build();

        options.addOption(help);
        options.addOption(version);
        options.addOption(safeModeOption);
        options.addOption(stateFileOption);
        options.addOption(configFileOption);
        options.addOption(mappingFileOption);
        options.addOption(logFileOption);
        options.addOption(parentPathOption);
        options.addOption(debugPortOption);
    }

    public StandaloneCommandLineOptions parse(String[] args) {
        StandaloneCommandLineOptions result = new StandaloneCommandLineOptions();
        // create the CLI parser
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args, false);

            result.stateFile = cmd.getOptionValue(stateFileOption, "/" + StandaloneCommandLineOptions.STATE_FILE);
            result.configFile = cmd.getOptionValue(configFileOption, "/" + StandaloneCommandLineOptions.CONFIG_FILE);
            result.mappingFile = cmd.getOptionValue(mappingFileOption, null);
            result.logFilePath = cmd.getOptionValue(logFileOption, null);
            result.currentDirectory = cmd.getOptionValue(parentPathOption, null);
            result.debugServerPort = cmd.getOptionValue(debugPortOption, null);

            result.isSafeModeEnabled = cmd.hasOption(safeModeOption);

            result.setDisplayHelp(cmd.hasOption(help));
            result.setDisplayVersion(cmd.hasOption(version));

            // Args not recognized may be accessed by function libraries; eg: Standard Library args(),
            // argcount()
            result.setProgramArgs(cmd.getArgs());
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());

            // attempt to allow program to continue normally - assumes program is compiled as a standalone
            // application
            result.setProgramArgs(args);
        }

        // Log standard output to file if requested
        if (result.logFilePath != null) {
            PrintStream out = null;
            try {
                System.out.println("Logging program output to: " + result.logFilePath);
                File file = new File(result.logFilePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                out = new PrintStream(new FileOutputStream(file.getAbsolutePath(), true), true);
                System.setOut(out);
                System.setErr(out);
            } catch (IOException e) {
                System.err.println("Unable to log to file");
                e.printStackTrace();
            }
        }
        return result;
    }

    public Options getOptions() {
        return options;
    }

    public void printHelp() {
        // generate the CLI help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                "basic4gl-app [options] [args...]",
                """
    Arguments following options are passed as user-defined arguments to the program.

    where options include:""",
                options,
                "");
    }

    @Override
    public String getConfigFilePathCommandLineOption() {
        return CONFIG_FILE_PATH_OPTION;
    }

    @Override
    public String getLineMappingFilePathCommandLineOption() {
        return LINE_MAPPING_FILE_PATH_OPTION;
    }

    @Override
    public String getLogFilePathCommandLineOption() {
        return LOG_FILE_PATH_OPTION;
    }

    @Override
    public String getParentDirectoryCommandLineOption() {
        return PARENT_DIRECTORY_OPTION;
    }

    @Override
    public String getProgramFilePathCommandLineOption() {
        return PROGRAM_FILE_PATH_OPTION;
    }

    @Override
    public String getDebuggerPortCommandLineOption() {
        return DEBUGGER_PORT_OPTION;
    }

    @Override
    public String getSandboxModeEnabledOption() {
        return SAFE_MODE_OPTION;
    }
}
