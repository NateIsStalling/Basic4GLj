package com.basic4gl.util;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.util.List;
import java.util.jar.*;
import java.util.zip.ZipEntry;

public class Exporter {

    /**
     * Copy files from inside the current running Java application to a JarOutputStream
     * @param files List of files and directories to copy
     * @param target Jar to copy files to
     * @throws IOException
     */
    public static void addSource(List<String> files, JarOutputStream target) throws IOException {
        String tempPath = null; //Last valid path in the files list
        boolean copy;           //Should file be copied

        //Get location of application
        CodeSource src = Exporter.class.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL jar = src.getLocation();
            System.out.println("Source jar:");
            System.out.println(jar);
            JarInputStream input = new JarInputStream(jar.openStream());
            while (true) {
                ZipEntry e = input.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();

                if (name.startsWith("META-INF/"))   //Do not copy manifest to destination
                    continue;

                //Check if file should be added
                copy = false;
                //Check if entry name matches cached name
                if (tempPath != null && name.startsWith(tempPath)) {
                    copy = true;
                }
                if (!copy)
                    for (String file : files)    //Check if entry name is in file list
                        if (name.startsWith(file)) {
                            tempPath = file;    //Cache path
                            copy = true;
                            break;
                        }
                //Copy file to destination
                if (copy) {
                    target.putNextEntry(e);
                    for (int c = input.read(); c != -1; c = input.read()) {
                        target.write(c);
                    }
                    target.closeEntry();
                }
            }

        }
        else
        {
            System.err.println("Source not recognized");
        }
    }

}
