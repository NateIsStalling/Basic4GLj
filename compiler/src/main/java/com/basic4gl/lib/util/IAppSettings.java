package com.basic4gl.lib.util;

/**
 * Interface to application settings used by routines and function libraries
 */
public interface IAppSettings {
	/**
	 * Returns true if application is in sandbox mode.
	 * This mode limits the amount of damage that a running program can do, and
	 * is intended for running code that may not come from a trusted source.
	 * In sandbox mode, programs may only read and write from the same
	 * directory in which they are running or a subdirectory thereof), and
	 * cannot delete files.
	 */
	boolean isSandboxModeEnabled();

	/**
	 * Returns the selected language dialect.
	 */
	int getSyntax(); // TODO evaluate usage, see TomBasicCompiler.LanguageSyntax.LS_BASIC4GL
}
