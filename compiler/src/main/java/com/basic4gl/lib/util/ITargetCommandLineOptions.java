package com.basic4gl.lib.util;

/**
 * Predefined commandline options required by Basic4GLj debugger
 * used to launch build targets
 */
public interface ITargetCommandLineOptions {

	/**
	 * "Config file" command line option
	 * Value used to load user defined program configuration
	 * @return Command line option; null if build target does not support this functionality
	 */
	String getConfigFilePathCommandLineOption();

	/**
	 * "Line mapping file path" command line option
	 * Value used to specify debugger line mapping file
	 * @return Command line option; null if build target does not support this functionality
	 */
	String getLineMappingFilePathCommandLineOption();

	/**
	 * "Log file path" command line option
	 * Value used to log output to the specified file
	 * @return Command line option; null if build target does not support this functionality
	 */
	String getLogFilePathCommandLineOption();

	/**
	 * "Parent directory" command line option
	 * Value used to override the parent directory of the running program
	 * @return Command line option
	 */
	String getParentDirectoryCommandLineOption();

	/**
	 * "Program file path" command line option
	 * Value used to load compiled VM state
	 * @return Command line option
	 */
	String getProgramFilePathCommandLineOption();

	/**
	 * "Debugger port" command line option
	 * Value used to connect to debug server port on localhost.
	 * @return Command line option; null if build target does not support this functionality
	 */
	String getDebuggerPortCommandLineOption();

	/**
	 * "Sandbox mode enabled" command line option
	 * Flag to run program in safe mode.
	 * @return Command line option; null if build target does not support this functionality
	 */
	String getSandboxModeEnabledOption();
}
