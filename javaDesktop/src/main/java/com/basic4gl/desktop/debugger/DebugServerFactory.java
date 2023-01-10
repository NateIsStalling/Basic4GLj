package com.basic4gl.desktop.debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugServerFactory {
    public static void startDebugServer(String debugServerJarPath, String port) {
        try {
            final String[] debugServerArgs = new String[] {
                    port
            };

            final String execCommand = "java "
                    + " -jar " + debugServerJarPath
                    + " " + String.join(" ", debugServerArgs);

            // Start output window
            final Process process = Runtime.getRuntime().exec(execCommand);

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
}
