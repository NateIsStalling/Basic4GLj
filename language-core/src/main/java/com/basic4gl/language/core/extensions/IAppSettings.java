package com.basic4gl.language.core.extensions;

import java.util.List;

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

    /**
     * Returns user-defined command-line arguments passed to programs run from the IDE.
     */
    List<String> getProgramArguments();

    /**
     * Returns user-defined JVM arguments passed to java when the run target is a jar.
     */
    List<String> getJvmArguments();

    /**
     * Returns true if launched jar targets should include JDWP debug agent parameters.
     */
    boolean isJvmDebuggingEnabled();

    /**
     * Returns true if JDWP should suspend the launched JVM until a debugger attaches.
     */
    boolean isJvmDebugSuspendUntilAttach();

    /**
     * Returns an optional JDWP port override. Null means use an internal session-selected port.
     */
    Integer getJvmDebugPortOverride();

    /**
     * Returns an optional plugin directory used to discover/load plugin jars.
     * Null/blank means use the active program parent directory.
     */
    String getPluginDirectory();

    /**
     * Returns an optional Maven repository/link value for plugin sourcing metadata.
     * This is informational unless the host explicitly implements remote resolution.
     */
    String getPluginMavenLink();
}
