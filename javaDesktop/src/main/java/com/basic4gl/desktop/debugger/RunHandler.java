package com.basic4gl.desktop.debugger;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.Library;
import com.basic4gl.library.desktopgl.BuilderDesktopGL;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunHandler {

    private final IApplicationHost mHost;
    private final TomBasicCompiler mComp;
    private final Preprocessor mPreprocessor;

    public RunHandler(IApplicationHost host, TomBasicCompiler comp, Preprocessor preprocessor) {
        mHost = host;
        mComp = comp;
        mPreprocessor = preprocessor;
    }

    public void launchRemote(Library builder, String currentDirectory, String libraryPath) {
        //TODO 12/2020 replacing Continue();

        // Compile and run program from start
        if (!mHost.Compile()) {
            return;
        }

        try {
            Path tempFolder = Files.createDirectories(Paths.get("temp"));
            File vm = File.createTempFile("basicvm-", "", tempFolder.toFile());
            File config = File.createTempFile("basicconfig-", "", tempFolder.toFile());
            File lineMapping = File.createTempFile("basiclinemapping-", "", tempFolder.toFile());

            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(vm))) {
                mComp.StreamOut(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (OutputStream outputStream = new FileOutputStream(config)) {
                ((BuilderDesktopGL) builder).getTarget().saveConfiguration(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (
                FileOutputStream outputStream = new FileOutputStream(lineMapping);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            ) {
                oos.writeObject(mPreprocessor.LineNumberMap());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO not sure how to cancel any suspended java apps that fail to connect to a debugger yet
            final String jvmDebugSuspend = "n";//"y"; // y/n whether the JVM should suspend and wait for a debugger to attach or not
            final String jvmDebugPort = "8080";
            final String jvmDebugArgs = "-agentlib:jdwp=transport=dt_socket," +
                    "address=" + jvmDebugPort + "," +
                    "server=y," +
                    "suspend=" + jvmDebugSuspend;

            final String jvmAdditionalArgs ="-XstartOnFirstThread"; // needed for GLFW

            final String[] runnerArgs = new String[] {
                vm.getAbsolutePath(),
                config.getAbsolutePath(),
                lineMapping.getAbsolutePath(),
                currentDirectory
            };

            final String execCommand = "java " + jvmDebugArgs
                    + " " + jvmAdditionalArgs
                    + " -jar " + libraryPath
                    + " " + String.join(" ", runnerArgs);

            // Start output window
            final Process process = Runtime.getRuntime().exec(execCommand);

            // Automatically close GL window when editor closes
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Shutdown Hook");
                    process.destroy();
                }
            }));

            // Handle output from GL window
            final BufferedReader errinput = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            final BufferedReader input = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            Thread thread = new Thread(new Runnable() {
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
            thread = new Thread(new Runnable() {
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
}
