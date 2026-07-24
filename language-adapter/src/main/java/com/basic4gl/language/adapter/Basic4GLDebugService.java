package com.basic4gl.language.adapter;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.spi.Builder;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.Target;
import com.basic4gl.language.core.extensions.IAppSettings;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;
import com.basic4gl.runtime.Debugger;
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Basic4GLDebugService implements com.basic4gl.desktop.spi.DebugService {
    private static final int MAX_CAPTURED_STDERR_LINES = 60;
    private static final String JDWP_BIND_HOST = "127.0.0.1";

    private final TomBasicCompiler compiler;
    private final Preprocessor preprocessor;

    private final Object launchedProcessLock = new Object();
    private final Object stderrLock = new Object();
    private Process launchedProcess;
    private final Deque<String> recentStderrLines = new ArrayDeque<>();
    private volatile com.basic4gl.desktop.spi.IProcessExitListener processExitListener;

    private PluginContext context;
    private IAppSettings appSettings;
    public Debugger debugger;

    Basic4GLDebugService(
            TomBasicCompiler compiler, Preprocessor preprocessor, IAppSettings appSettings, Debugger debugger) {
        this.compiler = compiler;
        this.preprocessor = preprocessor;
        this.appSettings = appSettings;
        this.debugger = debugger;
    }

    @Override
    public void onLoad(PluginContext context) {
        this.context = context;
    }

    @Override
    public Integer getPermanent() {
        return compiler.getProgram().getData().getPermanent();
    }

    @Override
    public com.basic4gl.desktop.spi.DebugLaunchInfo start(Object sender) {
        try {
            Path tempFolder =
                    Paths.get(System.getProperty("java.io.tmpdir")); // Files.createDirectories(Paths.get("temp"));
            File vm = File.createTempFile("basicvm-", "", tempFolder.toFile());
            File config = File.createTempFile("basicconfig-", "", tempFolder.toFile());
            File lineMapping = File.createTempFile("basiclinemapping-", "", tempFolder.toFile());

            String currentDirectory = context.currentDirectory();
            Builder builder = context.currentBuilder();
            String libraryBinPath = context.getLibraryPath();
            boolean isMacOS = context.isMacOS();
            String defaultDebugPort = context.getDefaultDebuggerPort();

            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(vm))) {
                compiler.streamOut(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (OutputStream outputStream = new FileOutputStream(config)) {
                (context.currentBuilder()).getTarget().saveConfiguration(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (FileOutputStream outputStream = new FileOutputStream(lineMapping);
                    ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
                oos.writeObject(preprocessor.getLineNumberMap());
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] commandArgs = buildCommandArgs(
                    builder.getTarget(),
                    appSettings,
                    currentDirectory,
                    libraryBinPath,
                    vm.getAbsolutePath(),
                    config.getAbsolutePath(),
                    lineMapping.getAbsolutePath(),
                    defaultDebugPort,
                    isMacOS);
            String jvmDebugArgs = findJvmDebugArgs(commandArgs);
            clearCapturedStderr();

            // Start output window
            final Process process = new ProcessBuilder(commandArgs).start();
            synchronized (launchedProcessLock) {
                launchedProcess = process;
            }

            process.onExit().thenAccept(exitedProcess -> {
                com.basic4gl.desktop.spi.IProcessExitListener listener = this.processExitListener;
                if (listener != null) {
                    listener.onProcessExited(sender, exitedProcess.exitValue(), getCapturedStderr());
                }
            });

            // Automatically close GL window when editor closes
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Shutdown Hook");
                    process.destroy();
                }
            }));

            // Handle output from GL window
            final BufferedReader errinput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            final BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String err;
                        while ((err = errinput.readLine()) != null) {
                            captureStderrLine(err);
                            System.err.println(err);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String err;
                        while ((err = input.readLine()) != null) {
                            System.out.println(err);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            return new com.basic4gl.desktop.spi.DebugLaunchInfo(
                    extractJvmDebugPort(jvmDebugArgs), isJvmDebugSuspendEnabled(jvmDebugArgs));

        } catch (IOException e) {
            e.printStackTrace();
            return new com.basic4gl.desktop.spi.DebugLaunchInfo(null, false);
        }
    }

    public void terminateLaunchedProcess() {
        Process processToTerminate;
        synchronized (launchedProcessLock) {
            processToTerminate = launchedProcess;
            launchedProcess = null;
        }

        if (processToTerminate == null || !processToTerminate.isAlive()) {
            return;
        }

        processToTerminate.destroy();
        try {
            if (!processToTerminate.waitFor(500, TimeUnit.MILLISECONDS)) {
                processToTerminate.destroyForcibly();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            processToTerminate.destroyForcibly();
        }
    }

    public void setProcessExitListener(com.basic4gl.desktop.spi.IProcessExitListener listener) {
        processExitListener = listener;
    }

    @Override
    public void clearUserBreakPoints() {
        debugger.clearUserBreakPoints();
    }

    @Override
    public boolean hasLaunchedProcess() {
        synchronized (launchedProcessLock) {
            return launchedProcess != null && launchedProcess.isAlive();
        }
    }

    @Override
    public boolean isUserBreakPoint(String filename, int line) {
        return debugger.isUserBreakPoint(filename, line);
    }

    @Override
    public void insertDeleteLines(String filename, int fileLineNo, int delta) {
        debugger.insertDeleteLines(filename, fileLineNo, delta);
    }

    public String getCapturedStderr() {
        synchronized (stderrLock) {
            if (recentStderrLines.isEmpty()) {
                return "";
            }
            return String.join(System.lineSeparator(), recentStderrLines);
        }
    }

    private void clearCapturedStderr() {
        synchronized (stderrLock) {
            recentStderrLines.clear();
        }
    }

    private void captureStderrLine(String line) {
        if (line == null) {
            return;
        }
        synchronized (stderrLock) {
            if (recentStderrLines.size() >= MAX_CAPTURED_STDERR_LINES) {
                recentStderrLines.removeFirst();
            }
            recentStderrLines.addLast(line);
        }
    }

    private static String[] buildCommandArgs(
            Target buildTarget,
            IAppSettings appSettings,
            String currentDirectory,
            String libraryBinPath,
            String vmPath,
            String configPath,
            String lineMappingPath,
            String defaultDebugServerPort,
            boolean isMacOS) {
        String applicationStoragePath =
                System.getProperty("user.home") + System.getProperty("file.separator") + "Basic4GLj";

        String logFilePath = new File(applicationStoragePath, "output.log").getAbsolutePath();

        final ArrayList<String> runnerArgs = new ArrayList<>();

        runnerArgs.add(libraryBinPath);

        if (buildTarget instanceof ITargetCommandLineOptions target) {
            addTargetOption(runnerArgs, target.getProgramFilePathCommandLineOption(), vmPath);
            addTargetOption(runnerArgs, target.getConfigFilePathCommandLineOption(), configPath);
            addTargetOption(runnerArgs, target.getLineMappingFilePathCommandLineOption(), lineMappingPath);
            addTargetOption(runnerArgs, target.getLogFilePathCommandLineOption(), logFilePath);
            addTargetOption(runnerArgs, target.getParentDirectoryCommandLineOption(), currentDirectory);
            addTargetOption(runnerArgs, target.getDebuggerPortCommandLineOption(), defaultDebugServerPort);

            List<String> pluginDirectories = appSettings.getPluginDirectories();
            if (pluginDirectories != null && !pluginDirectories.isEmpty()) {
                for (String pluginDirectory : pluginDirectories) {
                    if (pluginDirectory == null || pluginDirectory.trim().isEmpty()) {
                        continue;
                    }
                    addTargetOption(runnerArgs, target.getPluginDirectoryOption(), pluginDirectory);
                }
            }

            if (appSettings.isSandboxModeEnabled()) {
                addTargetOption(runnerArgs, target.getSandboxModeEnabledOption());
            }
        } else {
            System.out.println("Target Library does not implement "
                    + ITargetCommandLineOptions.class.getName()
                    + ", program may not support debugging.");
        }

        if (appSettings.getProgramArguments() != null
                && !appSettings.getProgramArguments().isEmpty()) {
            // '--' marks the end of target/debug options so user args are always passed through as-is.
            runnerArgs.add("--");
            runnerArgs.addAll(appSettings.getProgramArguments());
        }

        // Output window is being run as a java jar file
        if (libraryBinPath.endsWith(".jar")) {
            ArrayList<String> jvmArgs = new ArrayList<>(Arrays.asList("java"));

            if (appSettings.getJvmArguments() != null
                    && !appSettings.getJvmArguments().isEmpty()) {
                jvmArgs.addAll(appSettings.getJvmArguments());
            }

            if (appSettings.isJvmDebuggingEnabled()) {
                int sessionDebugPort = resolveJvmDebugPort(appSettings);
                jvmArgs.add(buildJvmDebugArgs(sessionDebugPort, appSettings.isJvmDebugSuspendUntilAttach()));
            }

            if (isMacOS) {
                jvmArgs.add("-XstartOnFirstThread"); // needed for GLFW
            }

            // libraryBinPath included in runnerArgs is expected to be a .jar file as the first value
            jvmArgs.add("-jar");
            jvmArgs.addAll(runnerArgs);

            return jvmArgs.toArray(new String[0]);
        }

        // Output window is an executable binary; java parameters are not required
        return runnerArgs.toArray(new String[0]);
    }

    private static String buildJvmDebugArgs(int debugPort, boolean suspendUntilAttach) {
        return "-agentlib:jdwp=transport=dt_socket,address="
                + JDWP_BIND_HOST
                + ":"
                + debugPort
                + ",server=y,suspend="
                + (suspendUntilAttach ? "y" : "n");
    }

    private static int resolveJvmDebugPort(IAppSettings appSettings) {
        Integer override = appSettings.getJvmDebugPortOverride();
        if (isValidPort(override)) {
            return override;
        }

        Integer sessionPort = findAvailableTcpPort();
        if (isValidPort(sessionPort)) {
            return sessionPort;
        }

        // Last-resort fallback keeps launch behavior predictable even if ephemeral allocation fails.
        return 8080;
    }

    private static Integer findAvailableTcpPort() {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(true);
            socket.bind(new java.net.InetSocketAddress(0));
            return socket.getLocalPort();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static boolean isValidPort(Integer port) {
        return port != null && port >= 1 && port <= 65535;
    }

    private static String findJvmDebugArgs(String[] commandArgs) {
        if (commandArgs == null) {
            return null;
        }
        for (String arg : commandArgs) {
            if (arg != null && arg.startsWith("-agentlib:jdwp=")) {
                return arg;
            }
        }
        return null;
    }

    private static Integer extractJvmDebugPort(String jvmDebugArgs) {
        if (jvmDebugArgs == null) {
            return null;
        }

        int addressIndex = jvmDebugArgs.indexOf("address=");
        if (addressIndex < 0) {
            return null;
        }

        int valueStartIndex = addressIndex + "address=".length();
        int valueEndIndex = jvmDebugArgs.indexOf(',', valueStartIndex);
        if (valueEndIndex < 0) {
            valueEndIndex = jvmDebugArgs.length();
        }

        String addressValue =
                jvmDebugArgs.substring(valueStartIndex, valueEndIndex).trim();
        if (addressValue.isEmpty()) {
            return null;
        }

        int lastColonIndex = addressValue.lastIndexOf(':');
        String portValue = lastColonIndex >= 0 ? addressValue.substring(lastColonIndex + 1) : addressValue;
        try {
            int port = Integer.parseInt(portValue);
            return isValidPort(port) ? port : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean isJvmDebugSuspendEnabled(String jvmDebugArgs) {
        return jvmDebugArgs != null && jvmDebugArgs.contains("suspend=y");
    }

    private static void addTargetOption(ArrayList<String> args, String option) {
        if (option != null) {
            args.add("-" + option);
        }
    }

    private static void addTargetOption(ArrayList<String> args, String option, String value) {
        if (option != null) {
            args.add("-" + option);
            args.add(value);
        }
    }
}
