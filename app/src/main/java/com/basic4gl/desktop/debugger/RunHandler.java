package com.basic4gl.desktop.debugger;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.ITargetCommandLineOptions;
import com.basic4gl.lib.util.Library;
import com.basic4gl.library.desktopgl.BuilderDesktopGL;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.SystemUtils;

public class RunHandler {

  private final IApplicationHost host;
  private final TomBasicCompiler compiler;
  private final Preprocessor preprocessor;
  private final IAppSettings appSettings;

  public RunHandler(
      IApplicationHost host,
      IAppSettings appSettings,
      TomBasicCompiler compiler,
      Preprocessor preprocessor) {
    this.host = host;
    this.appSettings = appSettings;
    this.compiler = compiler;
    this.preprocessor = preprocessor;
  }

  public void launchRemote(Library builder, String currentDirectory, String libraryBinPath) {

    // TODO 12/2020 replacing Continue();

    // Compile and run program from start
    if (!host.compile()) {
      return;
    }

    try {
      Path tempFolder =
          Paths.get(
              System.getProperty("java.io.tmpdir")); // Files.createDirectories(Paths.get("temp"));
      File vm = File.createTempFile("basicvm-", "", tempFolder.toFile());
      File config = File.createTempFile("basicconfig-", "", tempFolder.toFile());
      File lineMapping = File.createTempFile("basiclinemapping-", "", tempFolder.toFile());

      try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(vm))) {
        compiler.streamOut(outputStream);
      } catch (Exception e) {
        e.printStackTrace();
      }

      try (OutputStream outputStream = new FileOutputStream(config)) {
        ((BuilderDesktopGL) builder).getTarget().saveConfiguration(outputStream);
      } catch (Exception e) {
        e.printStackTrace();
      }

      try (FileOutputStream outputStream = new FileOutputStream(lineMapping);
          ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
        oos.writeObject(preprocessor.getLineNumberMap());
      } catch (Exception e) {
        e.printStackTrace();
      }

      String[] commandArgs =
          buildCommandArgs(
              builder,
              appSettings,
              currentDirectory,
              libraryBinPath,
              vm.getAbsolutePath(),
              config.getAbsolutePath(),
              lineMapping.getAbsolutePath());

      // Start output window
      final Process process = new ProcessBuilder(commandArgs).start();

      // Automatically close GL window when editor closes
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  new Runnable() {
                    @Override
                    public void run() {
                      System.out.println("Shutdown Hook");
                      process.destroy();
                    }
                  }));

      // Handle output from GL window
      final BufferedReader errinput =
          new BufferedReader(new InputStreamReader(process.getErrorStream()));
      final BufferedReader input =
          new BufferedReader(new InputStreamReader(process.getInputStream()));
      Thread thread =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    String err;
                    while ((err = errinput.readLine()) != null && process.isAlive()) {
                      System.err.println(err);
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              });
      thread.start();
      thread =
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    String err;
                    while ((err = input.readLine()) != null && process.isAlive()) {
                      System.out.println(err);
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              });
      thread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String[] buildCommandArgs(
      Library library,
      IAppSettings appSettings,
      String currentDirectory,
      String libraryBinPath,
      String vmPath,
      String configPath,
      String lineMappingPath) {

    // TODO not sure how to cancel any suspended java apps that fail to connect to a debugger yet
    final String jvmDebugSuspend =
        "n"; // "y"; // y/n whether the JVM should suspend and wait for a debugger to attach or not
    final String jvmDebugPort = "8080"; // TODO make this configurable
    final String jvmDebugArgs =
        "-agentlib:jdwp=transport=dt_socket,"
            + "address="
            + jvmDebugPort
            + ","
            + "server=y,"
            + "suspend="
            + jvmDebugSuspend;

    String applicationStoragePath =
        System.getProperty("user.home") + System.getProperty("file.separator") + "Basic4GLj";

    String logFilePath = new File(applicationStoragePath, "output.log").getAbsolutePath();

    final ArrayList<String> runnerArgs = new ArrayList<>();

    runnerArgs.add(libraryBinPath);

    if (library instanceof ITargetCommandLineOptions target) {
      addTargetOption(runnerArgs, target.getProgramFilePathCommandLineOption(), vmPath);
      addTargetOption(runnerArgs, target.getConfigFilePathCommandLineOption(), configPath);
      addTargetOption(
          runnerArgs, target.getLineMappingFilePathCommandLineOption(), lineMappingPath);
      addTargetOption(runnerArgs, target.getLogFilePathCommandLineOption(), logFilePath);
      addTargetOption(runnerArgs, target.getParentDirectoryCommandLineOption(), currentDirectory);
      addTargetOption(
          runnerArgs,
          target.getDebuggerPortCommandLineOption(),
          DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT);

      if (appSettings.isSandboxModeEnabled()) {
        addTargetOption(runnerArgs, target.getSandboxModeEnabledOption());
      }
    } else {
      System.out.println(
          "Target Library does not implement "
              + ITargetCommandLineOptions.class.getName()
              + ", program may not support debugging.");
    }

    // Output window is being run as a java jar file
    if (libraryBinPath.endsWith(".jar")) {
      ArrayList<String> jvmArgs =
          new ArrayList<String>(
              Arrays.asList(
                  "java", jvmDebugArgs // TODO make this configurable
                  ));

      if (SystemUtils.IS_OS_MAC) {
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
