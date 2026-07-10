package com.basic4gl.app.desktop;

import static com.basic4gl.app.desktop.config.StandaloneSettings.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_messageBox;

import com.basic4gl.app.desktop.config.*;
import com.basic4gl.app.desktop.glfw.GLFWKeyboard;
import com.basic4gl.app.desktop.glfw.GLFWMouse;
import com.basic4gl.app.desktop.glfw.GLFWWindowManager;
import com.basic4gl.compiler.LineNumberMapping;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.language.core.extensions.Basic4GLCompiler;
import com.basic4gl.language.core.extensions.FunctionLibrary;
import com.basic4gl.language.core.extensions.IAppSettings;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.runtime.*;
import com.basic4gl.language.core.runtime.Configuration;
import com.basic4gl.library.debug.DebuggerCommandAdapter;
import com.basic4gl.library.desktopgl.content.Content2DManager;
import com.basic4gl.library.desktopgl.content.FileOpener;
import com.basic4gl.library.desktopgl.content.GLTextGrid;
import com.basic4gl.library.desktopgl.content.IFileAccess;
import com.basic4gl.library.desktopgl.input.OpenGLKeyboard;
import com.basic4gl.library.desktopgl.input.OpenGLMouse;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;
import com.basic4gl.library.desktopgl.util.Target;
import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;
import com.basic4gl.library.plugin.PluginJARManager;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.lwjgl.glfw.GLFWErrorCallback;

public class GLTextGridWindow extends HasErrorState
        implements Target, IVMDriver, IFileAccess, ITargetCommandLineOptions {

    // Libraries
    private java.util.List<Library> libraries;
    private IServiceCollection services;
    private static GLTextGridWindow instance;

    private GLFWWindowManager windowManager;
    private GLFWKeyboard keyboard;
    private FileOpener fileOpener;

    private PluginJARManager plugins;
    private Basic4GLCompiler compiler;
    private VM vm;

    private final StandaloneCommandLineOptionsParser cliParser = new StandaloneCommandLineOptionsParser();
    private String[] programArgs;
    private DebuggerCallbacks debuggerCallbacks;
    private DebuggerCommandAdapter debuggerAdapter;

    private CountDownLatch completionLatch;
    //	private TaskCallback mCallbacks;
    private DebuggerCallbackMessage debuggerCallbackMessage;
    private CallbackMessage updatesCallback;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;

    private boolean isClosing;

    private GLTextGrid textGrid;

    private IAppSettings appSettings; // common settings for all libraries
    private boolean standaloneMode = true;
    private final IStandaloneSettings settings =
            new StandaloneSettings(); // settings specific to standalone application
    private Configuration configuration; // runtime configuration for this library
    private boolean closingWindowQuits = true;
    // Filename for stored VM state

    static final String DEFAULT_VERSION = "1.0"; // Default version name for compiled programs

    private String charsetPath = "charset.png"; // Default charset texture

    public void setCharsetPath(String path) {
        charsetPath = path;
    }

    public String getCharsetPath() {
        return charsetPath;
    }

    public GLTextGridWindow() {}

    //    public static Library getInstance(TomBasicCompiler compiler) {
    //        GLTextGridWindow instance = new GLTextGridWindow();
    //        instance.compiler = compiler;
    //        instance.vm = compiler.getProgram(); // TODO 6/12/2026 review this
    //        return instance;
    //    }

    /*
    //Constructor from C++ source for reference
    glTextGridWindow (  bool fullScreen,
    					bool border,
    					int width,          // Note: If width = 0, will use screen width
    					int height,
    					int bpp,
    					bool stencil,
    					std::string title,
    					bool allowResizing,
    					bool fitToWorkArea,
    					ResetGLModeType resetGLMode);*/

    public static void main(String[] args) {
        boolean isStandalone = true;
        boolean isSafeModeEnabled = true; // default safe mode to true (only applied if isStandalone=false)

        // JOptionPane.showMessageDialog(null, "Waiting...");
        instance = new GLTextGridWindow();

        StandaloneCommandLineOptions options = instance.cliParser.parse(args);

        // Load window configuration
        try {
            if (instance.getClass().getResource(options.configFile) != null) {
                // use bundled config file if running as an exported project
                instance.loadConfiguration(instance.getClass().getResourceAsStream(options.configFile));
            } else {
                // external config file path was provided by commandline - ie: from the IDE
                instance.loadConfiguration(new FileInputStream(options.configFile));

                // program is not standalone
                isStandalone = false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Configuration file could not be loaded");
        }

        // Log CLI help documentation
        if (options.shouldDisplayHelp()) {
            instance.cliParser.printHelp();
        }

        // display program version info and exit if requested by CLI args
        if (options.shouldDisplayVersion()) {
            String versionName = "";
            String title = instance.configuration.getValue(SETTING_TITLE);
            String version = instance.configuration.getValue(SETTING_VERSION);
            if (version == null || version.trim().isEmpty()) {
                version = DEFAULT_VERSION;
            }

            if (title != null && !title.trim().isEmpty()) {
                versionName = title + " " + version;
            } else {
                versionName = version;
            }

            System.out.println(versionName);
            return;
        }

        // provide program args for function libraries
        instance.programArgs = options.getProgramArgs();
        instance.standaloneMode = isStandalone;

        instance.appSettings = isStandalone ? new StandaloneAppSettings() : new EditorAppSettings();

        if (instance.appSettings instanceof IConfigurableAppSettings appSettings) {
            appSettings.setSandboxModeEnabled(isSafeModeEnabled);
        }

        LineNumberMapping lineNumberMapping = null;
        if (options.mappingFile != null) {
            try (FileInputStream streamIn = new FileInputStream(options.mappingFile);
                    ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)) {
                lineNumberMapping = (LineNumberMapping) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Debugger debugger = new Debugger(lineNumberMapping);

        //		instance.mFiles = new FileOpener(); //TODO load embedded files

        // Initialize file opener
        System.out.println("par: " + options.currentDirectory);
        instance.fileOpener = new FileOpener(""); // TODO load embedded files
        instance.fileOpener.setParentDirectory(options.currentDirectory);
        instance.fileOpener.setAppDataFolderName(instance.getAppDataFolderNameOverride());

        instance.plugins = new PluginJARManager(isStandalone);
        LinkedHashSet<String> pluginDirectorySet = new LinkedHashSet<>();
        for (String configuredDirectory : options.getPluginDirectories()) {
            if (configuredDirectory != null && !configuredDirectory.isBlank()) {
                pluginDirectorySet.add(configuredDirectory);
            }
        }
        pluginDirectorySet.add(instance.fileOpener.getWorkingDirectory().getAbsolutePath());
        pluginDirectorySet.add(Path.of(instance.fileOpener.getWorkingDirectory().getAbsolutePath(), "plugins")
                .toString());
        List<String> pluginDirectories = new ArrayList<>(pluginDirectorySet);
        if (instance.appSettings instanceof IConfigurableAppSettings configurableAppSettings) {
            configurableAppSettings.setPluginDirectories(pluginDirectories);
        }
        instance.plugins.setCurrentDirectory(
                instance.fileOpener.getWorkingDirectory().getAbsolutePath());
        instance.plugins.setDirectories(pluginDirectories);

        TomVM vm = new TomVM(instance.plugins, debugger);
        instance.compiler = new TomBasicCompiler(vm, instance.plugins);

        instance.vm = vm;
        instance.services = new ServiceCollection();
        instance.libraries = new ArrayList<>();

        // TODO Load libraries dynamically
        // TODO Save/Load list of libraries in order they should be added
        instance.libraries.add(new com.basic4gl.library.standard.Standard());
        instance.libraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
        instance.libraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
        instance.libraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
        instance.libraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
        instance.libraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
        instance.libraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
        instance.libraries.add(new com.basic4gl.library.standard.TrigBasicLib());
        instance.libraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
        instance.libraries.add(new com.basic4gl.library.standard.NetBasicLib());
        instance.libraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());
        instance.libraries.add(new com.basic4gl.library.standard.TomCompilerBasicLib());

        // Standard services
        GLFWWindowManager windowManager = new GLFWWindowManager();
        instance.windowManager = windowManager;
        instance.textGrid = new GLTextGrid(
                instance.fileOpener.getFilenameForRead("charset.png", false), instance.fileOpener, 25, 40, 16, 16);
        instance.keyboard = new GLFWKeyboard(windowManager);
        instance.services.registerService(OpenGLWindowManager.class, windowManager);
        instance.services.registerService(OpenGLKeyboard.class, instance.keyboard);
        instance.services.registerService(OpenGLMouse.class, new GLFWMouse(windowManager));
        instance.services.registerService(Content2DManager.class, new Content2DManager(windowManager));
        instance.services.registerService(GLTextGrid.class, instance.textGrid);

        // Register library functions
        for (Library lib : instance.libraries) {
            if (lib instanceof IFileAccess) {
                // Allows libraries to read from directories
                ((IFileAccess) lib).init(instance.fileOpener);
            }

            lib.init(instance.compiler, instance.services); // Allow libraries to register function overloads

            if (lib instanceof FunctionLibrary) {
                instance.compiler.addConstants(((FunctionLibrary) lib).constants());
                instance.compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            }
        }

        instance.fileOpener.setParentDirectory(options.currentDirectory);

        try {
            System.out.println(options.stateFile);
            if (instance.getClass().getResource(options.stateFile) != null) {
                // use bundled VM state file if running as an exported project
                instance.loadState(instance.getClass().getResourceAsStream(options.stateFile));
            } else {
                // external VM state file path was provided by commandline - ie: from the IDE
                instance.loadState(new DataInputStream(new FileInputStream(options.stateFile)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("VM state could not be loaded");
        }

        // Initialize window and setup VM
        instance.vm.pause();
        instance.vm.resetVM();
        instance.activate();

        // TODO this message has too much power
        instance.debuggerCallbackMessage = new DebuggerCallbackMessage(CallbackMessage.WORKING, "", null);

        if (options.debugServerPort != null) {

            URI debugServerUri = URI.create("ws://localhost:" + options.debugServerPort + "/debug/");

            instance.debuggerAdapter = new DebuggerCommandAdapter(
                    instance.debuggerCallbackMessage, debugger, instance, instance.compiler, instance.vm);

            instance.debuggerAdapter.connect(debugServerUri);

            instance.debuggerCallbacks =
                    new DebuggerCallbacks(
                            instance.debuggerAdapter, instance.debuggerCallbackMessage, instance.vm, instance) {
                        @Override
                        public void onPreLoad() {
                            // say hi
                            instance.debuggerAdapter.message(instance.debuggerCallbackMessage);
                        }

                        @Override
                        public void onPostLoad() {}
                    };
        }
        instance.start();
    }

    @Override
    public String name() {
        return "GLFW Window";
    }

    @Override
    public String description() {
        return "Desktop application with OpenGL capabilities.";
    }

    @Override
    public void cleanup() {
        if (debuggerAdapter != null) {
            debuggerAdapter.stop();
        }
        services.clear();
    }

    @Override
    public void init(FileOpener files) {
        fileOpener = files;
    }

    public void pause() {
        vm.pause();
    }

    @Override
    public void reset() {}

    @Override
    public void activate() {
        isClosing = false;

        // Get settings
        if (configuration == null) {
            configuration = getSettings();
        }
    }

    @Override
    public void start() {
        System.out.println("Running...");
        if (vm == null) {
            return; // TODO Throw exception
        }
        try {
            if (debuggerCallbacks != null) {
                debuggerCallbacks.onPreLoad();
            }
            charsetPath = fileOpener.getFilenameForRead("charset.png", false);
            if (debuggerCallbacks != null) {
                debuggerCallbacks.onPostLoad();
            }
            // Initialize libraries
            for (Library lib : compiler.getLibraries()) {
                initLibrary(lib);
            }

            onPreExecute();

            plugins.onProgramResume();

            // Debugger is not attached
            if (debuggerCallbacks == null) {
                while (!Thread.currentThread().isInterrupted() && !vm.hasError() && !vm.isDone() && !isClosing()) {
                    // Continue to next OpCode
                    driveVM(TomVM.VM_STEPS);

                    // Poll for window events. The key callback above will only be
                    // invoked during this call.
                    handleEvents();
                } // Program completed
            } else // Debugger is attached
            {
                while (!Thread.currentThread().isInterrupted() && !vm.hasError() && !vm.isDone() && !isClosing()) {
                    // Run the virtual machine for a certain number of steps
                    vm.patchIn();

                    // Check for pause key. (This allows us to pause when in full screen mode. Useful for debugging.)
                    if (keyboard.isPausePressed()) {
                        vm.pause();
                    }

                    if (vm.isPaused()) {
                        // Breakpoint reached or paused by debugger
                        System.out.println("VM paused");

                        debuggerCallbacks.pause("Reached breakpoint");

                        // Resume running
                        if (debuggerCallbacks.getMessage().getStatus() == CallbackMessage.WORKING) {
                            plugins.onProgramResume();

                            // Kick the virtual machine over the next op-code before patching in the breakpoints.
                            // otherwise we would never get past a breakpoint once we hit it, because we would
                            // keep on hitting it immediately and returning.
                            debuggerCallbacks.message(driveVM(1));

                            // Run the virtual machine for a certain number of steps
                            vm.patchIn();
                        }
                        // Check if program was stopped while paused
                        if (Thread.currentThread().isInterrupted() || vm.hasError() || vm.isDone() || isClosing()) {
                            break;
                        }
                    }

                    // Continue to next OpCode
                    debuggerCallbacks.message(driveVM(TomVM.VM_STEPS));

                    // Poll for window events. The key callback above will only be
                    // invoked during this call.
                    handleEvents();
                } // Program completed
            }

            // Perform debugger callbacks
            int success;
            if (debuggerCallbacks != null) {
                success = !vm.hasError() ? CallbackMessage.SUCCESS : CallbackMessage.FAILED;

                VMStatus vmStatus = new VMStatus(vm.isDone(), vm.hasError(), vm.getError());

                DebuggerCallbackMessage message = new DebuggerCallbackMessage(
                        success, success == CallbackMessage.SUCCESS ? "Program completed" : vm.getError(), vmStatus);

                // Set instruction position for editor to place cursor at error
                if (vm.hasError()) {
                    InstructionPosition ip = vm.getIPInSourceCode();
                    message.setInstructionPosition(ip);
                }

                debuggerCallbacks.message(message);
            }

            handleProgramExit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onFinally();
        }
    }

    @Override
    public void hide() {
        windowManager.deactivateWindow();
    }

    @Override
    public void stop() {
        // Clear stepping breakpoints
        vm.clearTempBreakPoints();

        vm.stop();
    }

    @Override
    public void terminate() {
        isClosing = true;

        stop();
    }

    @Override
    public boolean isFullscreen() {
        return windowManager.getActiveParams().isFullscreen;
    }

    @Override
    public Configuration getSettings() {
        return settings.getSettings();
    }

    @Override
    public Configuration getConfiguration() {
        if (configuration == null) {
            return getSettings();
        }
        return configuration;
    }

    @Override
    public void setConfiguration(Configuration config) {
        configuration = config;
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

    @Override
    public List<String> getDependencies() {

        // Get settings
        Configuration config = getConfiguration();

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
    public void loadConfiguration(InputStream stream) throws Exception {
        InputStream buffer = new BufferedInputStream(stream);
        ObjectInput input = new ObjectInputStream(buffer);
        configuration = (Configuration) input.readObject();
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
    public void saveState(OutputStream stream) throws IOException {
        DataOutputStream output = new DataOutputStream(stream);
        compiler.streamOut(output);
    }

    @Override
    public void loadState(InputStream stream) throws IOException {
        DataInputStream input = new DataInputStream(stream);
        compiler.streamIn(input);
    }

    public CallbackMessage driveVM(int steps) {

        // Drive the virtual machine

        // Execute a number of VM steps
        try {
            vm.continueVM(steps);

        } catch (Exception e) {
            // TODO get error type
            // Need to screen out numeric errors, as these can be generated by some
            // OpenGL implementations...
            /*switch (GetExceptionCode()) {

            // Skip mathematics errors (overflows, divide by 0 etc).
            // This is quite important!, as some OpenGL drivers will trigger
            // divide-by-zero and other conditions if geometry happens to
            // be aligned in certain ways. The appropriate behaviour is to
            // ignore these errors, and keep running, and NOT to stop the
            // program!
            case EXCEPTION_FLT_DENORMAL_OPERAND:
            case EXCEPTION_FLT_DIVIDE_BY_ZERO:
            case EXCEPTION_FLT_INEXACT_RESULT:
            case EXCEPTION_FLT_INVALID_OPERATION:
            case EXCEPTION_FLT_OVERFLOW:
            case EXCEPTION_FLT_STACK_CHECK:
            case EXCEPTION_FLT_UNDERFLOW:
            case EXCEPTION_INT_DIVIDE_BY_ZERO:
            case EXCEPTION_INT_OVERFLOW:
            	mVM.SkipInstruction();*/
            /*break;

            // All other exceptions will stop the program.
            default:*/
            e.printStackTrace();
            vm.miscError("An exception occured!");
        }

        // Check for error
        if (vm.hasError() || vm.isDone() || isClosing()) {
            int success;
            if (debuggerCallbacks != null) {
                success = !vm.hasError() ? CallbackMessage.SUCCESS : CallbackMessage.FAILED;

                return new CallbackMessage(
                        success, success == CallbackMessage.SUCCESS ? "Program completed" : vm.getError());
            }

            if (isClosing() || isFullscreen()) {
                hide(); // Stop program and close window
            } else {
                // TODO handle program completion options
                // stop(); //Just stop the worker thread;
            }
        }
        return null;
    }

    @Override
    public void initLibrary(Library lib) {
        if (lib instanceof IVMDriverAccess) {
            // Allows libraries to access VM driver/keep it responsive
            ((IVMDriverAccess) lib).init(this);
        }
        if (lib instanceof IFileAccess) {
            ((IFileAccess) lib).init(fileOpener);
        }

        lib.init(vm, services, appSettings, programArgs);
    }

    public boolean handleEvents() {

        // Notify debugger process is still alive
        if (debuggerAdapter != null) {
            debuggerAdapter.message(debuggerCallbackMessage);
        }

        // Keep window responsive during loops
        glfwPollEvents();
        return true; // all went well
    }

    public void onPreExecute() {
        // Create window
        init();
    }

    public void onPostExecute() {

        // Keep window responsive until closed
        while (!Thread.currentThread().isInterrupted() && windowManager.getGLFWWindow() != 0 && !isClosing()) {
            try {
                // Go easy on the processor
                Thread.sleep(10);

            } catch (InterruptedException e) {
            }

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public void onFinally() {
        synchronized (GLTextGridWindow.this) {
            // TODO 12/2022 consolidate below; moved from main editor worker thread
            plugins.onProgramEnd();
            vm.clearResources();

            // Inform libraries
            // StopTomSoundBasicLib();

            // TODO 12/2022 consolidate with above

            // Do any library cleanup
            for (Library lib : libraries) {
                System.out.println("cleanup " + lib.name());
                lib.cleanup();
            }
            System.out.println("cleanup " + name());
            cleanup();

            // Free text grid image
            System.out.println("destroy textgrid");
            textGrid.destroy();

            // Release window and window callbacks
            System.out.println("destroy window");
            windowManager.destroyWindow();

            keyboard.clearKeyBuffers();

            // Terminate GLFW and release the GLFWerrorfun
            System.out.println("glfwTerminate");
            glfwTerminate();
            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) {
                callback.free();
            }
            errorCallback = null;
            windowManager.destroyWindow();
        }
        System.out.println("exit");
    }

    public boolean isClosing() {
        try {
            synchronized (this) {
                long window = windowManager.getGLFWWindow();
                if (window != 0 && glfwWindowShouldClose(window)) {
                    if (closingWindowQuits) {
                        return true;
                    }
                    glfwSetWindowShouldClose(window, false);
                }
                return isClosing;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isVisible() {
        try {
            synchronized (this) {
                long window = windowManager.getGLFWWindow();
                return window != 0 && !glfwWindowShouldClose(window);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void init() {
        synchronized (GLTextGridWindow.this) {
            String title = configuration.getValue(SETTING_TITLE);
            int width = configuration.getIntValue(SETTING_WIDTH);
            int height = configuration.getIntValue(SETTING_HEIGHT);

            boolean resizable = configuration.getBooleanValue(SETTING_RESIZABLE);
            int mode = configuration.getIntValue(SETTING_SCREEN_MODE);
            boolean useDesktopResolution = configuration.getBooleanValueOrDefault(SETTING_USE_DESKTOP_RES, false);
            int colourDepth = configuration.getIntValueOrDefault(SETTING_COLOUR_DEPTH, COLOUR_DEPTH_DEFAULT);

            boolean bordered = configuration.getBooleanValueOrDefault(SETTING_BORDER, true);
            boolean stencilRequired = configuration.getBooleanValueOrDefault(SETTING_STENCIL, false);
            int startupWindowOption = configuration.getIntValueOrDefault(
                    SETTING_STARTUP_WINDOW_OPTION, STARTUP_WINDOW_CREATE_IMMEDIATELY);
            boolean createWindowOnStart = startupWindowOption == STARTUP_WINDOW_CREATE_IMMEDIATELY;
            closingWindowQuits = getClosingWindowQuitsOption();
            keyboard.setEscapeKeyQuits(getEscKeyQuitsOption());

            // Setup an error callback. The default implementation
            // will print the error message in System.err.
            glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

            // Initialize GLFW. Most GLFW functions will not work before doing this.
            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            // Config determines whether we create the actual window now or wait until
            // UpdateWindow() is executed in the Basic4GL code.

            // Apply startup configuration to pending window params.
            // If startup is deferred these are consumed by UpdateWindow().
            windowManager.pendingParams.title = title;
            windowManager.pendingParams.isFullscreen = mode == MODE_FULLSCREEN;
            windowManager.pendingParams.width = useDesktopResolution ? 0 : width;
            windowManager.pendingParams.height = useDesktopResolution ? 0 : height;
            windowManager.pendingParams.isResizable = resizable;
            if (windowManager.pendingParams.isFullscreen && colourDepth == COLOUR_DEPTH_16BIT) {
                windowManager.pendingParams.bpp = 16;
            } else if (windowManager.pendingParams.isFullscreen && colourDepth == COLOUR_DEPTH_32BIT) {
                windowManager.pendingParams.bpp = 32;
            } else {
                windowManager.pendingParams.bpp = 0;
            }
            windowManager.pendingParams.isBordered = bordered;
            windowManager.pendingParams.isStencilBufferRequired = stencilRequired;

            if (createWindowOnStart) {
                windowManager.recreateWindow();
            }

            long window = windowManager.getGLFWWindow();
            if (createWindowOnStart && window == NULL) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            // TODO implement window icons

            // Make the window visible
            if (createWindowOnStart) {
                windowManager.activateWindow();
            }
        }
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

    private String getAppDataFolderNameOverride() {
        String configured = null;
        if (!standaloneMode) {
            return null;
        }
        if (configuration != null && SETTING_APP_DATA_DIRECTORY < configuration.getSettingCount()) {
            configured = configuration.getValue(SETTING_APP_DATA_DIRECTORY);
        }
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        if (configuration != null && SETTING_TITLE < configuration.getSettingCount()) {
            String title = configuration.getValue(SETTING_TITLE);
            if (title != null && !title.isBlank()) {
                return title.trim();
            }
        }
        return null;
    }

    private void handleProgramExit() {
        if (vm.hasError()) {
            showRuntimeErrorMessage();
            hide();
            return;
        }
        waitForProgramCompletionOption();
    }

    private void showRuntimeErrorMessage() {
        int runtimeErrorOption = getRuntimeErrorOption();
        if (runtimeErrorOption == RUNTIME_ERROR_JUST_CLOSE) {
            return;
        }

        String message = "An error has occurred";
        if (runtimeErrorOption == RUNTIME_ERROR_SHOW_DETAILED_MESSAGE) {
            StringBuilder detailed = new StringBuilder();
            detailed.append(vm.getError());
            InstructionPosition instructionPosition = vm.getIPInSourceCode();
            if (instructionPosition != null && instructionPosition.getSourceLine() > 0) {
                detailed.append(System.lineSeparator());
                detailed.append("Line: ").append(instructionPosition.getSourceLine());
            }
            message = detailed.toString();
        }
        showRuntimeErrorDialog(message);
    }

    private void showRuntimeErrorDialog(String message) {
        tinyfd_messageBox("Runtime Error", message, "ok", "error", true);
    }

    private void waitForProgramCompletionOption() {
        int completionOption = getProgramEndOption();
        switch (completionOption) {
            case PROGRAM_END_CLOSE_IMMEDIATELY:
                hide();
                return;
            case PROGRAM_END_WAIT_KEYPRESS:
                waitForAnyKeyThenClose();
                return;
            case PROGRAM_END_WAIT_WINDOW_CLOSE:
            default:
                waitForWindowCloseThenExit();
                return;
        }
    }

    private void waitForAnyKeyThenClose() {
        if (windowManager.getGLFWWindow() == 0) {
            return;
        }
        keyboard.clearKeyBuffers();
        while (!Thread.currentThread().isInterrupted()
                && windowManager.getGLFWWindow() != 0
                && !isWindowCloseRequested()) {
            if (isClosing) {
                break;
            }
            if (keyboard.getNextKey() != 0 || keyboard.getNextScanKey() != 0) {
                break;
            }
            sleepAndPollEvents();
        }
        hide();
    }

    private void waitForWindowCloseThenExit() {
        while (!Thread.currentThread().isInterrupted()
                && windowManager.getGLFWWindow() != 0
                && !isWindowCloseRequested()) {
            sleepAndPollEvents();
        }
        hide();
    }

    private boolean isWindowCloseRequested() {
        long window = windowManager.getGLFWWindow();
        return window != 0 && glfwWindowShouldClose(window);
    }

    private void sleepAndPollEvents() {
        try {
            Thread.sleep(10);
            handleEvents();
        } catch (InterruptedException consumed) {
            Thread.currentThread().interrupt();
        }
    }

    private int getProgramEndOption() {
        if (!standaloneMode) {
            return PROGRAM_END_WAIT_WINDOW_CLOSE;
        }
        return configuration.getIntValueOrDefault(SETTING_PROGRAM_END_OPTION, PROGRAM_END_WAIT_WINDOW_CLOSE);
    }

    private int getRuntimeErrorOption() {
        if (!standaloneMode) {
            return RUNTIME_ERROR_SHOW_DETAILED_MESSAGE;
        }
        return configuration.getIntValueOrDefault(SETTING_RUNTIME_ERROR_OPTION, RUNTIME_ERROR_SHOW_DETAILED_MESSAGE);
    }

    private boolean getEscKeyQuitsOption() {
        if (!standaloneMode) {
            return true;
        }
        return configuration.getBooleanValueOrDefault(SETTING_ESC_KEY_QUITS, true);
    }

    private boolean getClosingWindowQuitsOption() {
        if (!standaloneMode) {
            return true;
        }
        return configuration.getBooleanValueOrDefault(SETTING_CLOSING_WINDOW_QUITS, true);
    }
}
