//package com.basic4gl.desktop;
//
//import com.basic4gl.desktop.spi.DialogService;
//import com.basic4gl.desktop.spi.FileOpener;
//import com.basic4gl.desktop.spi.SourceFileService;
//import com.basic4gl.language.adapter.EditorAdapter;
//import com.basic4gl.language.core.runtime.IFileOpener;
//
//public class EditorPluginContext implements com.basic4gl.desktop.spi.PluginContext {
//    private EditorAdapter editorAdapter;
//    private IStandaloneSettings settings;
//    private IFileOpener fileOpener;
//
//    public EditorPluginContext(EditorAdapter editorAdapter, IStandaloneSettings settings, IFileOpener fileOpener) {
//        this.editorAdapter = editorAdapter;
//        this.settings = settings;
//        this.fileOpener = fileOpener;
//    }
//
//    @Override
//    public EditorAdapter getEditorAdapter() {
//        return editorAdapter;
//    }
//
//    @Override
//    public IStandaloneSettings getSettings() {
//        return settings;
//    }
//
//    @Override
//    public IFileOpener getFileOpener() {
//        return fileOpener;
//    }
//
//    @Override
//    public DialogService dialogs() {
//        return null;
//    }
//
//    @Override
//    public FileOpener files() {
//        return null;
//    }
//
//    @Override
//    public SourceFileService[] fileServices() {
//        return new SourceFileService[0];
//    }
//}
