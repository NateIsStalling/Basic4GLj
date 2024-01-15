package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;

import java.io.OutputStream;

/**
 * Created by Nate on 7/26/2015.
 *
 * Export module for libraries
 * Bundles necessary files with a compiled Basic4GL program to launch
 * Libraries should know nothing about the Builder interface,
 * but a class that implements Builder can know about any library
 */
public abstract class Builder implements Library, IFileAccess, ITargetCommandLineOptions {


    public abstract String version();
    public abstract String author();
    public abstract String contact();
    public abstract String id();

    /**
     * Bundle the target in a stream to store a standalone copy on the filesystem.
     * Used by the compiler.
     * @return
     */
    public abstract boolean export(String filename, OutputStream stream, TaskCallback callback) throws Exception;

    public abstract Target getTarget();
    public abstract IVMDriver getVMDriver();
    public abstract String getFileDescription();
    public abstract String getFileExtension();

    /**
     * Get list of properties the compiler can configure for
     * building or running the application.
     * @example Window size or target OS
     * @return Read-only list of properties for application
     */
    public abstract Configuration getSettings();

    /**
     *
     * @return Instance of target's property collection
     */
    public abstract Configuration getConfiguration();

    /**
     *
     * @param config
     */
    public abstract void setConfiguration(Configuration config);
}
