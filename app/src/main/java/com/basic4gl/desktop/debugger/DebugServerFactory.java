package com.basic4gl.desktop.debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugServerFactory {
    public static void startDebugServer(String debugServerBinPath, String port) {
        try {
            System.out.println(debugServerBinPath);
            String[] commandArgs = buildCommandArgs(debugServerBinPath, port);

            // Start output window
            final Process process = new ProcessBuilder(commandArgs).start();

            // Automatically stop debug server when editor closes
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Shutdown Hook");
                    process.destroy();
                }
            }));

            // Handle output from debug server
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

    private static String[] buildCommandArgs(String debugServerBinPath, String port) {
        // Run debug server with java if path is a jar file
        if (debugServerBinPath.endsWith(".jar")) {
            return new String[] {
                "java", "-jar", debugServerBinPath, port
            };
        }

        // Debug server is a binary executable; java parameters are not required
        return new String[] {
            debugServerBinPath, port
        };

    }
}
