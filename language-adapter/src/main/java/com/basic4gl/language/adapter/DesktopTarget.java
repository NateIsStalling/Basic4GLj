package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.GLTextGridWindow;
import com.basic4gl.app.desktop.config.IStandaloneSettings;
import com.basic4gl.app.desktop.config.StandaloneCommandLineOptionsParser;
import com.basic4gl.app.desktop.config.StandaloneSettings;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.Configuration;
import com.basic4gl.desktop.spi.Target;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.basic4gl.app.desktop.config.StandaloneSettings.*;

public class DesktopTarget implements Target, ITargetCommandLineOptions {
    private final StandaloneCommandLineOptionsParser cliParser = new StandaloneCommandLineOptionsParser();
    private TomBasicCompiler compiler;

    public DesktopTarget(TomBasicCompiler compiler) {
        this.compiler =  compiler;
    }
    private final IStandaloneSettings settings =
            new StandaloneSettings(); // settings specific to standalone application
    private com.basic4gl.language.core.runtime.Configuration configuration; // runtime configuration for this library

    @Override
    public String name() {
        return "GLFW Window";
    }

    @Override
    public String description() {
        return "Desktop application with OpenGL capabilities.";
    }

    @Override
    public Configuration getSettings() {
        // TODO confirm this is updated as expected
        return ConfigurationMapper.toEditorConfiguration(settings.getSettings());
    }

    @Override
    public Configuration getConfiguration() {
        if (configuration == null) {
            return getSettings();
        }
        return ConfigurationMapper.toEditorConfiguration(configuration);
    }

    @Override
    public void setConfiguration(Configuration config) {
        configuration = ConfigurationMapper.toRuntimeConfiguration(config);
    }

    @Override
    public void loadConfiguration(InputStream stream) throws Exception {
        InputStream buffer = new BufferedInputStream(stream);
        ObjectInput input = new ObjectInputStream(buffer);
        configuration = (com.basic4gl.language.core.runtime.Configuration) input.readObject();
        input.close();
        buffer.close();
    }

    @Override
    public void saveConfiguration(OutputStream stream) throws Exception {
        // Serialize configuration
        ObjectOutput output = new ObjectOutputStream(stream);
        output.writeObject(configuration);
    }

    @Override
    public void saveState(OutputStream stream) throws Exception {
        DataOutputStream output = new DataOutputStream(stream);
        compiler.streamOut(output);
    }

    @Override
    public void loadState(InputStream stream) throws Exception {
        DataInputStream input = new DataInputStream(stream);
        compiler.streamIn(input);
    }

    @Override
    public void cleanup() {

    }

    public Type getMainClass() {
        return GLTextGridWindow.class;
    }


    @Override
    public List<String> getDependencies() {

        // Get settings
        com.basic4gl.language.core.runtime.Configuration config = ConfigurationMapper.toRuntimeConfiguration(getConfiguration());

        List<String> list = new ArrayList<>();

        // Get supported platforms
        int windows = Integer.valueOf(config.getValue(SETTING_SUPPORT_WINDOWS));
        int mac = Integer.valueOf(config.getValue(SETTING_SUPPORT_MAC));
        int linux = Integer.valueOf(config.getValue(SETTING_SUPPORT_LINUX));

        // Common
        list.add("charset.png");

        // TODO reevaluate these dependencies - these may no longer be valid after switching project
        // structure to gradle
        // TODO need to remember/document why this list is needed.. I think it has to do with external
        // libs, but the current export creates a fat JAR
        //		list.add("jar/lwjgl.jar");
        //
        //        list.add("jar/lwjgl-assimp-natives-macos.jar");
        //        list.add("jar/lwjgl-assimp.jar");
        //        list.add("jar/lwjgl-glfw-natives-macos.jar");
        //        list.add("jar/lwjgl-glfw.jar");
        //        list.add("jar/lwjgl-natives-macos.jar");
        //        list.add("jar/lwjgl-openal-natives-macos.jar");
        //        list.add("jar/lwjgl-openal.jar");
        //        list.add("jar/lwjgl-opengl-natives-macos.jar");
        //        list.add("jar/lwjgl-opengl.jar");
        //        list.add("jar/lwjgl-stb-natives-macos.jar");
        //        list.add("jar/lwjgl-stb.jar");
        //        list.add("jar/lwjgl.jar");

        // Sound engine
        //		list.add("jar/SoundSystem.jar");
        //		list.add("jar/LibraryLWJGLOpenAL.jar");
        //		list.add("jar/CodecIBXM.jar");
        //		list.add("jar/CodecJOrbis.jar");
        //		list.add("jar/CodecWav.jar");

        // Windows
        //		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows ==
        // GLTextGridWindow.SUPPORT_WINDOWS_64) {
        //			//64-bit JOGL Windows libraries
        //			list.add("native/lwjgl.dll");
        //			list.add("native/OpenAL.dll");
        //			list.add("native/jemalloc.dll");
        //			list.add("native/glfw.dll");
        //		}
        //		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows ==
        // GLTextGridWindow.SUPPORT_WINDOWS_32) {
        //			//32-bit lwjgl Windows libraries
        //			list.add("native/lwjgl32.dll");
        //			list.add("native/OpenAL32.dll");
        //			list.add("native/jemalloc32.dll");
        //			list.add("native/glfw32.dll");
        //		}
        //		//Mac
        //		if (mac == GLTextGridWindow.SUPPORT_MAC_32_64) {
        //			//Universal lwjgl Mac libraries
        //			list.add("native/liblwjgl.dylib");
        //			list.add("native/libopenal.dylib");
        //			list.add("native/libjemalloc.dylib");
        //			list.add("native/libglfw.dylib");
        //		}
        //		//Linux
        //		if (linux == GLTextGridWindow.SUPPORT_LINUX_32_64) {
        //			//lwjgl Linux libraries
        //			list.add("native/liblwjgl.so");
        //			list.add("native/libopenal.so");
        //			list.add("native/libjemalloc.so");
        //			list.add("native/libglfw.so");
        //		}

        return list;
    }

    @Override
    public List<String> getClassPathObjects() {
        return Arrays.asList(
                "lwjgl.jar",
                "lwjgl-assimp.jar",
                "lwjgl-assimp-natives-linux.jar",
                "lwjgl-assimp-natives-linux-arm32.jar",
                "lwjgl-assimp-natives-linux-arm64.jar",
                "lwjgl-assimp-natives-macos.jar",
                "lwjgl-assimp-natives-macos-arm64.jar",
                "lwjgl-assimp-natives-windows.jar",
                "lwjgl-assimp-natives-windows-arm64.jar",
                "lwjgl-assimp-natives-windows-x86.jar",
                "lwjgl-glfw.jar",
                "lwjgl-glfw-natives-linux.jar",
                "lwjgl-glfw-natives-linux-arm32.jar",
                "lwjgl-glfw-natives-linux-arm64.jar",
                "lwjgl-glfw-natives-macos.jar",
                "lwjgl-glfw-natives-macos-arm64.jar",
                "lwjgl-glfw-natives-windows.jar",
                "lwjgl-glfw-natives-windows-arm64.jar",
                "lwjgl-glfw-natives-windows-x86.jar",
                "lwjgl-natives-linux.jar",
                "lwjgl-natives-linux-arm32.jar",
                "lwjgl-natives-linux-arm64.jar",
                "lwjgl-natives-macos.jar",
                "lwjgl-natives-macos-arm64.jar",
                "lwjgl-natives-windows.jar",
                "lwjgl-natives-windows-arm64.jar",
                "lwjgl-natives-windows-x86.jar",
                "lwjgl-openal.jar",
                "lwjgl-openal-natives-linux.jar",
                "lwjgl-openal-natives-linux-arm32.jar",
                "lwjgl-openal-natives-linux-arm64.jar",
                "lwjgl-openal-natives-macos.jar",
                "lwjgl-openal-natives-macos-arm64.jar",
                "lwjgl-openal-natives-windows.jar",
                "lwjgl-openal-natives-windows-arm64.jar",
                "lwjgl-openal-natives-windows-x86.jar",
                "lwjgl-opengl.jar",
                "lwjgl-opengl-natives-linux.jar",
                "lwjgl-opengl-natives-linux-arm32.jar",
                "lwjgl-opengl-natives-linux-arm64.jar",
                "lwjgl-opengl-natives-macos.jar",
                "lwjgl-opengl-natives-macos-arm64.jar",
                "lwjgl-opengl-natives-windows.jar",
                "lwjgl-opengl-natives-windows-arm64.jar",
                "lwjgl-opengl-natives-windows-x86.jar",
                "lwjgl-stb.jar",
                "lwjgl-stb-natives-linux.jar",
                "lwjgl-stb-natives-linux-arm32.jar",
                "lwjgl-stb-natives-linux-arm64.jar",
                "lwjgl-stb-natives-macos.jar",
                "lwjgl-stb-natives-macos-arm64.jar",
                "lwjgl-stb-natives-windows.jar",
                "lwjgl-stb-natives-windows-arm64.jar",
                "lwjgl-stb-natives-windows-x86.jar",

                // Sound engine
                "paulscode-soundsystem-lwjgl3.jar");
    }

    public void reset() {

    }

    public void init(FileOpenerAdapter fileOpenerAdapter) {
    }

    @Override
    public String getConfigFilePathCommandLineOption() {
        return cliParser.getConfigFilePathCommandLineOption();
    }

    @Override
    public String getLineMappingFilePathCommandLineOption() {
        return cliParser.getLineMappingFilePathCommandLineOption();
    }

    @Override
    public String getLogFilePathCommandLineOption() {
        return cliParser.getLogFilePathCommandLineOption();
    }

    @Override
    public String getParentDirectoryCommandLineOption() {
        return cliParser.getParentDirectoryCommandLineOption();
    }

    @Override
    public String getProgramFilePathCommandLineOption() {
        return cliParser.getProgramFilePathCommandLineOption();
    }

    @Override
    public String getDebuggerPortCommandLineOption() {
        return cliParser.getDebuggerPortCommandLineOption();
    }

    @Override
    public String getSandboxModeEnabledOption() {
        return cliParser.getSandboxModeEnabledOption();
    }

    @Override
    public String getPluginDirectoryOption() {
        return cliParser.getPluginDirectoryOption();
    }
}
