package com.basic4gl.library.desktopgl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import com.basic4gl.compiler.LineNumberMapping;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.IVMDriverAccess;
import com.basic4gl.lib.util.*;
import com.basic4gl.library.debug.DebuggerCommandAdapter;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.InstructionPosition;
import com.basic4gl.runtime.TomVM;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.cli.*;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class GLTextGridWindow extends GLWindow implements IFileAccess, ITargetCommandLineOptions {

    // Libraries
    private java.util.List<Library> libraries;
    private IServiceCollection services;
    private static GLTextGridWindow instance;

    private FileOpener fileOpener;

    private TomBasicCompiler compiler;
    private TomVM vm;
    private String[] programArgs;
    private DebuggerCallbacks debuggerCallbacks;
    private DebuggerCommandAdapter debuggerAdapter;

    private CountDownLatch completionLatch;
    //	private TaskCallback mCallbacks;
    private DebuggerCallbackMessage debuggerCallbackMessage;
    private CallbackMessage updatesCallback;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCharCallback charCallback;

    private boolean isClosing;

    private GLTextGrid textGrid;

    private IAppSettings appSettings; // common settings for all libraries
    private Configuration configuration; // runtime configuration for this library

    static final int SETTING_TITLE = 1; // Index of window title setting in config
    static final int SETTING_VERSION = 2; // Index of version string setting in config
    static final int SETTING_WIDTH = 3; // Index of window width setting in config
    static final int SETTING_HEIGHT = 4; // Index of window height setting in config
    static final int SETTING_RESIZABLE = 5; // Index of window resizable setting in config
    static final int SETTING_SCREEN_MODE = 6; // Index of screen mode setting in config
    static final int SETTING_SUPPORT_WINDOWS = 9; // Index of Windows support setting in config
    static final int SETTING_SUPPORT_MAC = 10; // Index of Mac support setting in config
    static final int SETTING_SUPPORT_LINUX = 11; // Index of Linux support setting in config

    static final int SUPPORT_WINDOWS_32_64 = 0;
    static final int SUPPORT_WINDOWS_32 = 1;
    static final int SUPPORT_WINDOWS_64 = 2;
    static final int SUPPORT_WINDOWS_NO = 3;

    static final int SUPPORT_MAC_32_64 = 0;
    static final int SUPPORT_MAC_NO = 1;

    static final int SUPPORT_LINUX_32_64 = 0;

    static final int MODE_WINDOWED = 0;
    static final int MODE_FULLSCREEN = 1;

    static final String CONFIG_FILE = "config.ser"; // Filename for configuration file
    static final String STATE_FILE = "state.bin"; // Filename for stored VM state

    static final String DEFAULT_VERSION = "1.0"; // Default version name for compiled programs

    // Predefined CLI options used by Basic4GLj debugger
    static final String CONFIG_FILE_PATH_OPTION = "c";
    static final String PROGRAM_FILE_PATH_OPTION = "p";
    static final String LINE_MAPPING_FILE_PATH_OPTION = "m";
    static final String LOG_FILE_PATH_OPTION = "logfile";
    static final String PARENT_DIRECTORY_OPTION = "parent";
    static final String DEBUGGER_PORT_OPTION = "d";
    static final String SAFE_MODE_OPTION = "s";

    private String charsetPath = "charset.png"; // Default charset texture

    public void setCharsetPath(String path) {
        charsetPath = path;
    }

    public String getCharsetPath() {
        return charsetPath;
    }

    public GLTextGridWindow() {
        super(
                false,
                true,
                640, // Note: If width = 0, will use screen width
                480,
                0, // Color depth - 0 (use desktop), 16, or 32
                true,
                "Basic4GLj",
                false,
                false);
    }

    public static Library getInstance(TomBasicCompiler compiler) {
        GLTextGridWindow instance = new GLTextGridWindow();
        instance.compiler = compiler;
        instance.vm = compiler.getVM();
        return instance;
    }

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

        // Load VM's state from file
        String stateFile = "/" + STATE_FILE;
        String configFile = "/" + CONFIG_FILE;
        String mappingFile = null;
        String currentDirectory = null;
        String debugServerPort = null;
        String logFilePath = null;

        // Program should display help
        boolean displayHelp = false;

        // Program should display version and exit
        boolean displayVersion = false;

        // Program args used to initialize function libraries
        String[] programArgs = new String[0];

        // create the CLI parser
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        try {
            Option help = Option.builder("h")
                    .longOpt("help")
                    .desc("print this message")
                    .build();

            Option version = Option.builder("v")
                    .longOpt("version")
                    .desc("print the version information and exit")
                    .build();

            Option safeModeOption = Option.builder("s")
                    .longOpt("safe-mode-enabled")
                    .desc("run the program in safe mode")
                    .build();

            Option logFileOption = Option.builder(LOG_FILE_PATH_OPTION)
                    .argName("file")
                    .hasArg()
                    .desc("use given file path for log output")
                    .build();

            Option configFileOption = Option.builder(CONFIG_FILE_PATH_OPTION)
                    .longOpt("config-path")
                    .argName("file")
                    .hasArg()
                    .desc("use given file for program config")
                    .build();

            Option stateFileOption = Option.builder(PROGRAM_FILE_PATH_OPTION)
                    .longOpt("program-path")
                    .argName("file")
                    .hasArg()
                    .desc("use given file path to load compiled program code")
                    .build();

            Option mappingFileOption = Option.builder(LINE_MAPPING_FILE_PATH_OPTION)
                    .longOpt("line-mapping-path")
                    .argName("file")
                    .hasArg()
                    .desc("use given file path for program line mapping")
                    .build();

            Option parentPathOption = Option.builder(PARENT_DIRECTORY_OPTION)
                    .argName("directory")
                    .hasArg()
                    .desc("use given directory path for program parent directory")
                    .build();

            Option debugPortOption = Option.builder(DEBUGGER_PORT_OPTION)
                    .longOpt("debugger-port")
                    .argName("port")
                    .hasArg()
                    .desc("use given port to connect to the debugger")
                    .build();

            options.addOption(help);
            options.addOption(version);
            options.addOption(safeModeOption);
            options.addOption(stateFileOption);
            options.addOption(configFileOption);
            options.addOption(mappingFileOption);
            options.addOption(logFileOption);
            options.addOption(parentPathOption);
            options.addOption(debugPortOption);

            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args, false);

            stateFile = cmd.getOptionValue(stateFileOption, "/" + STATE_FILE);
            configFile = cmd.getOptionValue(configFileOption, "/" + CONFIG_FILE);
            mappingFile = cmd.getOptionValue(mappingFileOption, null);
            logFilePath = cmd.getOptionValue(logFileOption, null);
            currentDirectory = cmd.getOptionValue(parentPathOption, null);
            debugServerPort = cmd.getOptionValue(debugPortOption, null);

            isSafeModeEnabled = cmd.hasOption(safeModeOption);

            displayHelp = cmd.hasOption(help);
            displayVersion = cmd.hasOption(version);

            // Args not recognized may be accessed by function libraries; eg: Standard Library args(),
            // argcount()
            programArgs = cmd.getArgs();
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());

            // attempt to allow program to continue normally - assumes program is compiled as a standalone
            // application
            programArgs = args;
        }

        // Log standard output to file if requested
        if (logFilePath != null) {
            PrintStream out = null;
            try {
                System.out.println("Logging program output to: " + logFilePath);
                File file = new File(logFilePath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                out = new PrintStream(new FileOutputStream(file.getAbsolutePath(), true), true);
                System.setOut(out);
                System.setErr(out);
            } catch (IOException e) {
                System.err.println("Unable to log to file");
                e.printStackTrace();
            }
        }

        // JOptionPane.showMessageDialog(null, "Waiting...");
        instance = new GLTextGridWindow();

        // Load window configuration
        try {
            if (instance.getClass().getResource(configFile) != null) {
                // use bundled config file if running as an exported project
                instance.loadConfiguration(instance.getClass().getResourceAsStream(configFile));
            } else {
                // external config file path was provided by commandline - ie: from the IDE
                instance.loadConfiguration(new FileInputStream(configFile));

                // program is not standalone
                isStandalone = false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Configuration file could not be loaded");
        }

        // Log CLI help documentation
        if (displayHelp) {
            // generate the CLI help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "basic4gl-app [options] [args...]",
                    """
		Arguments following options are passed as user-defined arguments to the program.

		where options include:""",
                    options,
                    "");
        }

        // display program version info and exit if requested by CLI args
        if (displayVersion) {
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
        instance.programArgs = programArgs;

        instance.appSettings = isStandalone ? new StandaloneAppSettings() : new EditorAppSettings();

        if (instance.appSettings instanceof IConfigurableAppSettings appSettings) {
            appSettings.setSandboxModeEnabled(isSafeModeEnabled);
        }

        LineNumberMapping lineNumberMapping = null;
        if (mappingFile != null) {
            try (FileInputStream streamIn = new FileInputStream(mappingFile);
                    ObjectInputStream objectinputstream = new ObjectInputStream(streamIn)) {
                lineNumberMapping = (LineNumberMapping) objectinputstream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Debugger debugger = new Debugger(lineNumberMapping);

        //		instance.mFiles = new FileOpener(); //TODO load embedded files

        // Initialize file opener
        System.out.println("par: " + currentDirectory);
        instance.fileOpener = new FileOpener(""); // TODO load embedded files
        instance.fileOpener.setParentDirectory(currentDirectory);

        instance.compiler = new TomBasicCompiler(new TomVM(debugger));
        instance.vm = instance.compiler.getVM();
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

        // Register library functions
        for (Library lib : instance.libraries) {
            lib.init(instance.compiler, instance.services); // Allow libraries to register function overloads
            if (lib instanceof IFileAccess) {
                // Allows libraries to read from directories
                ((IFileAccess) lib).init(instance.fileOpener);
            }
            if (lib instanceof FunctionLibrary) {
                instance.compiler.addConstants(((FunctionLibrary) lib).constants());
                instance.compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            }
        }

        instance.fileOpener.setParentDirectory(currentDirectory);

        try {
            System.out.println(stateFile);
            if (instance.getClass().getResource(stateFile) != null) {
                // use bundled VM state file if running as an exported project
                instance.loadState(instance.getClass().getResourceAsStream(stateFile));
            } else {
                // external VM state file path was provided by commandline - ie: from the IDE
                instance.loadState(new DataInputStream(new FileInputStream(stateFile)));
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

        if (debugServerPort != null) {

            URI debugServerUri = URI.create("ws://localhost:" + debugServerPort + "/debug/");

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
    public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {
        // TODO Auto-generated method stub

    }

    @Override
    public void init(TomBasicCompiler comp, IServiceCollection services) {}

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
            onPreExecute();
            // Initialize libraries
            for (Library lib : compiler.getLibraries()) {
                initLibrary(lib);
            }
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

                    if (vm.isPaused()) {
                        // Breakpoint reached or paused by debugger
                        System.out.println("VM paused");

                        debuggerCallbacks.pause("Reached breakpoint");

                        // Resume running
                        if (debuggerCallbacks.getMessage().getStatus() == CallbackMessage.WORKING) {
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
            //
            //				glMatrixMode(GL_MODELVIEW);
            //				glLoadIdentity();
            //
            //				//Calculate the aspect ratio of the window
            //				perspectiveGL(90.0f,(float)this.width/(float)this.height,0.1f,100.0f);
            //
            //				glShadeModel(GL_SMOOTH);
            //				glClearDepth(1.0f);                         // Depth Buffer Setup
            //				glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
            //				glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do
            // Keep window responsive until closed
            while (!Thread.currentThread().isInterrupted() && window != 0 && !isClosing()) {
                // System.out.println("idle");
                try {
                    // Go easy on the processor
                    Thread.sleep(10);

                    //						glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
                    //
                    //						glLoadIdentity();//							' Reset The View
                    //
                    //						glColor3f(0.5f,0.5f,1.0f);//						' Set The Color To Blue One Time Only
                    //						glBegin(GL_QUADS);//							' Draw A Quad
                    //						glVertex3f(-1.0f, 1.0f, 0.0f);//					' Top Left
                    //						glVertex3f( 1.0f, 1.0f, 0.0f)	;//				' Top Right
                    //						glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
                    //						glVertex3f(-0.5f,-1.0f, 0.0f);//					' Bottom Left
                    //						glEnd();
                    //						glfwSwapBuffers(this.window);

                    // Poll for window events. The key callback above will only be
                    // invoked during this call.
                    handleEvents();
                } catch (InterruptedException consumed) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onFinally();
        }
    }

    @Override
    public void hide() {}

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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Configuration getSettings() {
        Configuration settings = new Configuration();
        settings.addSetting(new String[] {"Window Config"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(new String[] {"Window Title"}, Configuration.PARAM_STRING, "My Application");
        settings.addSetting(new String[] {"Program Version"}, Configuration.PARAM_STRING, "1.0");
        settings.addSetting(new String[] {"Window Width"}, Configuration.PARAM_INT, "640");
        settings.addSetting(new String[] {"Window Height"}, Configuration.PARAM_INT, "480");
        settings.addSetting(new String[] {"Resizable Window"}, Configuration.PARAM_BOOL, "false");
        settings.addSetting(
                new String[] {"Screen Mode", "Windowed"},
                // "Fullscreen"}, temporarily disabled
                Configuration.PARAM_CHOICE,
                "0");
        settings.addSetting(new String[] {}, Configuration.PARAM_DIVIDER, "");
        settings.addSetting(new String[] {"Platforms"}, Configuration.PARAM_HEADING, "");
        settings.addSetting(
                new String[] {"Windows Support", "32/64-bit", "32-bit", "64-bit", "Do not support"},
                Configuration.PARAM_CHOICE,
                "0");
        settings.addSetting(
                new String[] {"Mac Support", "32/64-bit", "Do not support"}, Configuration.PARAM_CHOICE, "0");
        settings.addSetting(
                new String[] {"Linux Support", "32/64-bit", "Do not support"}, Configuration.PARAM_CHOICE, "0");

        return settings;
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
        int windows = Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_WINDOWS));
        int mac = Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_MAC));
        int linux = Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_LINUX));

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

    GLTextGrid getTextGrid() {
        return textGrid;
    }

    void setTextGrid(GLTextGrid grid) {
        textGrid = grid;
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
        if (lib instanceof IGLRenderer) {
            ((IGLRenderer) lib).setTextGrid(textGrid);
            ((IGLRenderer) lib).setWindow(GLTextGridWindow.this);
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

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        resetGL();

        // Initialize Sprite Engine
        textGrid = new GLSpriteEngine(charsetPath, fileOpener, 25, 40, 16, 16);
        if (textGrid.hasError()) {
            vm.setError(textGrid.getError());
        }
    }

    public void onPostExecute() {
        //		glfwSwapBuffers(this.window); // swap the color buffers
        // Keep window responsive until closed
        while (!Thread.currentThread().isInterrupted() && window != 0 && !isClosing()) {
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
            // mDLLs.ProgramEnd();
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
            glfwDestroyWindow(window);

            System.out.println("destroy callbacks");
            keyCallback.free();
            charCallback.free();
            clearKeyBuffers();

            // Terminate GLFW and release the GLFWerrorfun
            System.out.println("glfwTerminate");
            glfwTerminate();
            GLFWErrorCallback callback = glfwSetErrorCallback(null);
            if (callback != null) {
                callback.free();
            }
            errorCallback = null; // .release();
            // Clear pointer to window
            // An access violation will occur next time this window is launched if this isn't cleared
            window = 0;
        }
        System.out.println("exit");
    }

    public void recreateGLContext() {
        super.recreateGLContext();

        if (textGrid != null) {
            textGrid.uploadCharsetTexture();
        }
    }

    public boolean isClosing() {
        try {
            synchronized (this) {
                return isClosing || (window != 0 && glfwWindowShouldClose(window));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isVisible() {
        try {
            synchronized (this) {
                return window != 0 && !glfwWindowShouldClose(window);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void init() {
        synchronized (GLTextGridWindow.this) {
            // C++ source code for reference
            // this.glWin = null;
            // this.glText = null;

            // Default settings
            // boolean fullScreen = false, border = true;
            // int width = 640, height = 480, bpp = 0;
            // ResetGLModeType resetGLMode = RGM_RESETSTATE;

            // Create window
            /*
            this.glWin = new glTextGridWindow ( fullScreen, border, width, height,
            bpp, "Basic4GL", resetGLMode);

            // Check for errors if (this.glWin.Error ()) { MessageDlg ( (AnsiString)
            this.glWin.GetError().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
            Application.Terminate (); return; } m_glWin.Hide ();

            // Create OpenGL text grid m_glText = new glSpriteEngine (
            (ExtractFilePath (Application.ExeName) + "charset.png").c_str (),
            &m_files, 25, 40, 16, 16);
            // Check for errors if (m_glText.Error ()) { MessageDlg (
            (AnsiString) + m_glText.GetError ().c_str (), mtError,
            TMsgDlgButtons() << mbOK, 0); Application.Terminate (); return; }
            m_glWin.SetTextGrid (m_glText);
            */
            String title = configuration.getValue(SETTING_TITLE);
            width = Integer.valueOf(configuration.getValue(SETTING_WIDTH));
            height = Integer.valueOf(configuration.getValue(SETTING_HEIGHT));

            boolean resizable = Boolean.valueOf(configuration.getValue(SETTING_RESIZABLE));
            int mode = Integer.valueOf(configuration.getValue(SETTING_SCREEN_MODE));

            // Setup an error callback. The default implementation
            // will print the error message in System.err.
            glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

            // Initialize GLFW. Most GLFW functions will not work before doing this.
            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            // Configure our window
            glfwDefaultWindowHints(); // optional, the current window hints are already the default
            glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
            glfwWindowHint(GLFW_RESIZABLE, resizable ? GL_TRUE : GL_FALSE); // the window will be resizable
            glfwWindowHint(GLFW_SCALE_TO_MONITOR, GL_TRUE); // handle monitor resolution scaling

            // Create the window
            window = glfwCreateWindow(width, height, title, NULL, NULL);
            if (window == NULL) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            // TODO implement window icons

            /* //TODO Implement fullscreen and windowless mode
            //Scrap code from previous swing implementation
            if (mode == MODE_FULLSCREEN) {
            	mFrame.setUndecorated(true);
            	mFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
            	mFrame.setUndecorated(false);
            	mFrame.setExtendedState(JFrame.NORMAL);
            }
            mFrame.add(mCanvas);*/

            // Setup a key callback. It will be called every time a key is pressed, repeated or released.
            glfwSetKeyCallback(
                    window,
                    keyCallback = new GLFWKeyCallback() {
                        @Override
                        public void invoke(long window, int key, int scancode, int action, int mods) {
                            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
                            }
                            if (key < 0) {
                                return;
                            }
                            if (action == GLFW_PRESS) {
                                if (key == GLFW_KEY_PAUSE) {
                                    pausePressed = true;
                                } else {
                                    keyDown[key] |= 1;
                                    bufferScanKey((char) key);
                                }
                            } else if (action == GLFW_RELEASE) {
                                keyDown[key] &= ~1;
                            }
                        }
                    });
            // Setup a character key callback
            glfwSetCharCallback(
                    window,
                    charCallback = new GLFWCharCallback() {
                        @Override
                        public void invoke(long window, int codepoint) {

                            if (codepoint == 27) // Esc closes window
                            {
                                closing = true;
                            }

                            int end = bufferEnd;
                            incEnd(); // Check for room in buffer
                            if (bufferEnd != bufferStart) {
                                keyBuffer[end] = (char) codepoint;
                            } else {
                                bufferEnd = end; // No room. Restore buffer pointers
                            }
                        }
                    });

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center our window
            glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
            // Get the thread stack and push a new frame
            //			try ( MemoryStack stack = stackPush() ) {
            //				IntBuffer pWidth = stack.mallocInt(1); // int*
            //				IntBuffer pHeight = stack.mallocInt(1); // int*
            //
            //				// Get the window size passed to glfwCreateWindow
            //				glfwGetWindowSize(m_window, pWidth, pHeight);
            //
            //				// Get the resolution of the primary monitor
            //				GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            //
            //				// Center the window
            //				glfwSetWindowPos(
            //						this.window,
            //						(vidmode.width() - pWidth.get(0)) / 2,
            //						(vidmode.height() - pHeight.get(0)) / 2
            //				);
            //			} // the stack frame is popped automatically

            // Make the OpenGL context current
            glfwMakeContextCurrent(window);
            // Enable v-sync
            glfwSwapInterval(1);

            // Make the window visible
            glfwShowWindow(window);
            //			int err = glGetError();
            //			System.out.println(err);
        }
    }

    @Override
    public String getConfigFilePathCommandLineOption() {
        return CONFIG_FILE_PATH_OPTION;
    }

    @Override
    public String getLineMappingFilePathCommandLineOption() {
        return LINE_MAPPING_FILE_PATH_OPTION;
    }

    @Override
    public String getLogFilePathCommandLineOption() {
        return LOG_FILE_PATH_OPTION;
    }

    @Override
    public String getParentDirectoryCommandLineOption() {
        return PARENT_DIRECTORY_OPTION;
    }

    @Override
    public String getProgramFilePathCommandLineOption() {
        return PROGRAM_FILE_PATH_OPTION;
    }

    @Override
    public String getDebuggerPortCommandLineOption() {
        return DEBUGGER_PORT_OPTION;
    }

    @Override
    public String getSandboxModeEnabledOption() {
        return SAFE_MODE_OPTION;
    }
}
