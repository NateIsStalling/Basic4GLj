package com.basic4gl.debug.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DebugServer {
    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.err.println("Usage: java KKMultiServer <port number>");
//            System.exit(1);
//        }

        int portNumber = 4444;//Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new KKMultiServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }

    }
}
