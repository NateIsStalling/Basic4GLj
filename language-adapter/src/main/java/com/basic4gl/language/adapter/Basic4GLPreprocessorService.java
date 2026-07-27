package com.basic4gl.language.adapter;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.ISourceFile;

public class Basic4GLPreprocessorService implements com.basic4gl.desktop.spi.PreprocessorService {
    private final Preprocessor preprocessor;
    private final TomBasicCompiler compiler;

    Basic4GLPreprocessorService(TomBasicCompiler compiler, Preprocessor preprocessor) {
        this.compiler = compiler;
        this.preprocessor = preprocessor;
    }

    @Override
    public void onLoad(com.basic4gl.desktop.spi.PluginContext context) {}

    @Override
    public boolean hasError() {
        return preprocessor.hasError();
    }

    @Override
    public String getError() {
        return preprocessor.getError();
    }

    @Override
    public boolean preprocess(ISourceFile sourceFile) {
        return preprocessor.preprocess(new FileServiceAdapter.SourceFile(sourceFile), compiler.getParser());
    }
}
