package com.basic4gl.desktop.debugger.commands;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.desktop.debugger.IApplicationHost;
import com.basic4gl.lib.util.Library;
import com.basic4gl.library.desktopgl.BuilderDesktopGL;

import java.io.*;
import java.nio.file.Paths;

public class RunHandler {

    private final IApplicationHost mHost;
    private final TomBasicCompiler mComp;

    public RunHandler(IApplicationHost host, TomBasicCompiler comp) {
        mHost = host;
        mComp = comp;
    }

    public void run() {
        // TODO called from stopped
//        if (Compile()) {
//            Continue();
//        }
    }
    public void launchRemote(Library builder, String currentDirectory, String libraryPath) {
        //TODO 12/2020 replacing Continue();

        // Compile and run program from start
        if (!mHost.Compile()) {
            return;
        }

        try {
            File vm = File.createTempFile("basicvm-", "", Paths.get(".").toFile());
            File config = File.createTempFile("basicconfig-", "", Paths.get(".").toFile());
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(vm))) {
//                mComp.VM().Stre
                mComp.StreamOut(outputStream);
            }

            try (OutputStream outputStream = new FileOutputStream(config)) {
                ((BuilderDesktopGL) builder).getTarget().saveConfiguration(outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO not sure how to cancel any suspended java apps that fail to connect to a debugger yet
            final String jvmDebugSuspend = "n";//"y"; // y/n whether the JVM should suspend and wait for a debugger to attach or not
            final String jvmArgs = "-agentlib:jdwp=transport=dt_socket,address=8080,server=y,suspend=" + jvmDebugSuspend + " " +
                    "-XstartOnFirstThread"; // needed for GLFW
            final String execCommand = "java " + jvmArgs + " -jar " + libraryPath
                    + " " + vm.getAbsolutePath()
                    + " " + config.getAbsolutePath()
                    + " " + currentDirectory;

            final Process process = Runtime.getRuntime().exec(execCommand);
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
//            InputStream errorStream = process.getErrorStream();
//            while (line = errorStream.s)
//            while (p)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
