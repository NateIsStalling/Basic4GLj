package com.basic4gl.desktop.spi;

public interface PluginContext {
//    CommandRegistry commands();
//    MenuRegistry menus();
//    ToolWindowRegistry toolWindows();
    DialogService dialogs();
    MenuService menus();
//    ProjectService projects();
//    EditorService editors();
    FileOpener files();
    Builder currentBuilder();
    String currentDirectory();
    String getLibraryPath();
    SourceFileService[] fileServices();
    boolean isMacOS();
    String getDefaultDebuggerPort();

}