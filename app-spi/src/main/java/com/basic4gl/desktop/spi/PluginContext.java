package com.basic4gl.desktop.spi;

import com.basic4gl.desktop.spi.content.ContentService;

public interface PluginContext {
    //    CommandRegistry commands();
    //    MenuRegistry menus();
    //    ToolWindowRegistry toolWindows();

    EditorCommandsService commands();

    DebugController debugger();

    DialogService dialogs();

    MenuService menus();

    ContentService content();
    //    ProjectService projects();
    //    EditorService editors();
    FileOpener files();

    Builder currentBuilder();

    String currentDirectory();

    EditorPlugin currentEditor();

    String getLibraryPath();

    SourceFileService[] fileServices();

    boolean isMacOS();

    String getDefaultDebuggerPort();
}
