package com.basic4gl.compiler.util;

import java.util.List;

/**
 * Optional contract for builders that support embedding plugin JARs.
 */
public interface IPluginExportBuilder {

    void setExportPlugins(List<String> pluginPaths);

    List<String> getExportPlugins();
}
