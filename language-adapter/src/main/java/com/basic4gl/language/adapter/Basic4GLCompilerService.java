package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.GLTextGridWindow;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.types.LanguageSyntax;
import com.basic4gl.desktop.spi.Builder;
import com.basic4gl.desktop.spi.CompilerService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.Target;
import com.basic4gl.language.core.extensions.FunctionLibrary;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.runtime.IServiceCollection;
import com.basic4gl.library.desktopgl.content.FileOpener;
import com.basic4gl.library.desktopgl.content.IFileAccess;

import java.util.ArrayList;
import java.util.List;

public class Basic4GLCompilerService implements CompilerService {

    private final Preprocessor preprocessor;
    // TODO this may need to be moved into appSettings
    private LanguageSyntax languageSyntax = LanguageSyntax.LS_BASIC4GL;

    public TomBasicCompiler compiler; // Compiler
    private final List<Library> libraries = new ArrayList<>();
    private final List<Target> targets = new ArrayList<>();
    private final List<Builder> builders = new ArrayList<>();

    Basic4GLCompilerService(TomBasicCompiler compiler, Preprocessor preprocessor) {
        this.compiler = compiler;
        this.preprocessor = preprocessor;
    }

    @Override
    public void onLoad(PluginContext context) {

        // TODO Implement standard libraries
        // Plug in constant and function libraries
        /*
         * InitTomStdBasicLib (mComp); // Standard library
         * InitTomWindowsBasicLib (mComp, &m_files); // Windows specific
         * library InitTomOpenGLBasicLib (mComp, m_glWin, &m_files); // OpenGL
         * InitTomTextBasicLib (mComp, m_glWin, m_glText); // Basic
         * text/sprites InitGLBasicLib_gl (mComp); InitGLBasicLib_glu (mComp);
         * InitTomJoystickBasicLib (mComp, m_glWin); // Joystick support
         * InitTomTrigBasicLib (mComp); // Trigonometry library
         * InitTomFileIOBasicLib (mComp, &m_files); // File I/O library
         * InitTomNetBasicLib (mComp); // Networking
         */

        IServiceCollection tempServices = new com.basic4gl.language.core.runtime.ServiceCollection();

        // TODO Load libraries dynamically
        libraries.add(new com.basic4gl.library.standard.Standard());
        libraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
        libraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
        libraries.add(new com.basic4gl.library.standard.TrigBasicLib());
        libraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
        libraries.add(new com.basic4gl.library.standard.NetBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());
        libraries.add(new com.basic4gl.library.standard.TomCompilerBasicLib());

//        targets.add(GLTextGridWindow.getInstance(compiler));
//        builders.add(BuilderDesktopGL.getInstance(compiler));

        FileOpener fileOpener = new FileOpener(context.files().getParentDirectory());
        // TODO Add more libraries
        int i = 0;
        for (Library lib : libraries) {
            lib.init(compiler, tempServices); // Allow libraries to register function overloads
            if (lib instanceof IFileAccess) {
                // Allows libraries to read from directories
                ((IFileAccess) lib).init(fileOpener);
            }
            if (lib instanceof FunctionLibrary) {
                compiler.addConstants(((FunctionLibrary) lib).constants());
                compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            }
            i++;
        }
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void compile() {
        compiler.compile();
    }

    @Override
    public Builder[] getBuilders() {
        return builders.toArray(new Builder[0]);
    }

    @Override
    public void clearError() {
        compiler.clearError();
    }

    @Override
    public String getError() {
        return compiler.getError();
    }

    @Override
    public Long getTokenColumn() {
        return compiler.getTokenColumn();
    }

    @Override
    public Long getTokenLine() {
        return compiler.getTokenLine();
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public boolean tryCompileForExport() {
        return false;
    }

    @Override
    public void clear() {
        compiler.getParser().getSourceCode().clear();
    }

    @Override
    public int getParserLinePosition() {
        return compiler.getParser().getSourceCode().size() - 1;
    }

    public List<Library> getLibraries() {
        return libraries;
    }
}
