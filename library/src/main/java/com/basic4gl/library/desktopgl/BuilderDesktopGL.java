package com.basic4gl.library.desktopgl;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.compiler.util.Exporter;
import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.runtime.TomVM;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Nate on 8/15/2015.
 */
public class BuilderDesktopGL extends Builder {

    private GLTextGridWindow mTarget;
    private FileOpener mFiles;

    public static Library getInstance(TomBasicCompiler compiler) {
        BuilderDesktopGL instance = new BuilderDesktopGL();
        instance.mTarget = (GLTextGridWindow) GLTextGridWindow.getInstance(compiler);
        return instance;
    }

    @Override
    public String getFileDescription() {
        return "Java Application (*.zip)";
    }

    @Override
    public String getFileExtension() {
        return "zip";
    }

    @Override
    public Configuration getSettings() {
        return mTarget.getSettings();
    }

    @Override
    public Configuration getConfiguration() {
        return mTarget.getConfiguration();
    }

    @Override
    public void setConfiguration(Configuration config) {
        mTarget.setConfiguration(config);
    }


    @Override
    public boolean export(String filename, OutputStream stream, TaskCallback callback) throws Exception {
        int i;
        File file = new File(filename);
        File jar = new File(file.getName().replaceFirst("[.][^.]+$", "") + ".jar");
        String classpath = ".";
        //TODO set this as a parameter or global constant
        ZipOutputStream output = new ZipOutputStream(stream);
        JarOutputStream target;
        ZipEntry zipEntry;
        JarEntry jarEntry;

        //TODO Add build option for single Jar

        ClassLoader loader = getClass().getClassLoader();
        List<String> dependencies;

        //Generate class path
        dependencies = mTarget.getClassPathObjects();
        i = 0;
        if (dependencies != null) {
            for (String dependency : dependencies) {
                classpath += " " + dependency;
                i++;
            }
        }

        //Add external dependencies
        dependencies = mTarget.getDependencies();
        if (dependencies != null) {
            // pre-validate
            for (String dependency : dependencies) {
                File source = new File(dependency);

                if (!source.exists()
                        || ClassLoader.getSystemClassLoader().getResource(dependency) == null) {

                    System.out.println("Dependency not found: " + dependency);
                    continue;       //TODO throw build error
                }
            }
            // add
            for (String dependency : dependencies) {
                File source = new File(dependency);
                InputStream input;
                if (source.exists()) {
                    input = new FileInputStream(source);
                } else if (ClassLoader.getSystemClassLoader().getResource(dependency) != null) {
                    input = ClassLoader.getSystemClassLoader().getResourceAsStream(dependency);
                } else {
                    continue;
                    //TODO throw build error
                    //throw new Exception("Cannot find dependency: " + dependency);
                }

                zipEntry = new ZipEntry(dependency);
                zipEntry.setTime(source.lastModified());

                output.putNextEntry(zipEntry);
                for (int c = input.read(); c != -1; c = input.read()) {
                    output.write(c);
                }
                output.closeEntry();
            }
        }

        //Add launcher script
        //TODO add additional scripts for each platform
        zipEntry = new ZipEntry("launcher.bat");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                "::Run %1$s; requires Java be installed and in your system path\n" +
                        "::Log console output for debugging\n" +
                        ">output.log (\n" +
                        "\tjava -jar \"%1$s\"\n" +
                        ")",
                jar.getName()).getBytes(StandardCharsets.UTF_8));
        output.closeEntry();

        //TODO update script for better logging
        zipEntry = new ZipEntry("launcher-macos.sh");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                "# Run %1$s; requires Java be installed and in your system path\n" +
                        "# -XstartOnFirstThread is required by LWJGL for window to display on Mac OS\n" +
                        "java -XstartOnFirstThread -jar \"%1$s\"\n",
                jar.getName()).getBytes(StandardCharsets.UTF_8));
        output.closeEntry();

        zipEntry = new ZipEntry("README.txt");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                "Execute launcher.bat to run %1$s on Windows.\n" +
                    "To run %1$s on Mac OS, execute launcher-macos.sh or run the jar with the following additional Java option from the terminal: \n" +
                    "-XstartOnFirstThread",
                jar.getName()).getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
        try {
            //Create application's manifest
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath);
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mTarget.getClass().getName());

            zipEntry = new ZipEntry(jar.getName());
            zipEntry.setTime(System.currentTimeMillis());
            output.putNextEntry(zipEntry);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            target = new JarOutputStream(bytes, manifest);

            //Add Basic4GLj classes to new Jar
            System.out.println("Adding source files");
            List<String> files = new ArrayList<String>();
            //TODO Only add required classes
            files.add("com/basic4gl/compiler");
            files.add("com/basic4gl/lib"); //TODO this namespace should be renamed..
            files.add("com/basic4gl/library");
            files.add("com/basic4gl/util");
            files.add("com/basic4gl/runtime");
            files.add("paulscode"); // sound library
            files.add("org/apache/commons/cli"); // args support
            files.add("org/apache/commons/imaging"); // MD2 support
            files.add("org/lwjgl");
            //TODO this should only be added if target OS
            files.add("macos"); // lwjgl natives

            // exclude any html/javadoc to save on file size
            String excludeRegex = ".*html$";

            Exporter.addSource(files, excludeRegex, target);

            //Save VM's initial state to Jar
            mTarget.reset();
            jarEntry = new JarEntry(GLTextGridWindow.STATE_FILE);
            target.putNextEntry(jarEntry);
            mTarget.saveState(target);
            target.closeEntry();

            //Serialize the build configuration and add to Jar
            jarEntry = new JarEntry(GLTextGridWindow.CONFIG_FILE);
            target.putNextEntry(jarEntry);
            mTarget.saveConfiguration(target);
            target.closeEntry();

            //TODO Implement embedding resources
            target.close();
            bytes.writeTo(output);
            //Writing Jar to archive complete
            output.closeEntry();


        } catch (Exception e) {
            e.printStackTrace();
        }
        /*if (singleJar){
            System.out.println("Adding dependencies");
            if (dependencies != null)
                for (String file: dependencies) {
                    File source = new File(libRoot + file);
                    if (!source.exists())
                        continue;
                    FileInputStream input = new FileInputStream(source);

                    entry = new JarEntry(file);
                    entry.setTime(source.lastModified());

                    target.putNextEntry(entry);
                    for (int c = input.read(); c != -1; c = input.read()) {
                        target.write(c);
                    }
                    target.closeEntry();
                }
        }*/

        output.close();
        return true;
    }

    @Override
    public Target getTarget() {
        return mTarget;
    }

    @Override
    public IVMDriver getVMDriver() {
        return mTarget;
    }

    @Override
    public String name() {
        return "Desktop Application";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public String description() {
        return "Desktop application with OpenGL capabilities.";
    }

    @Override
    public String author() {
        return "Nathaniel Nielsen";
    }

    @Override
    public String contact() {
        return "https://github.com/NateIsStalling/Basic4GLj/issues";
    }

    @Override
    public String id() {
        return "desktopgl";
    }


    @Override
    public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {

    }

    @Override
    public void init(TomBasicCompiler comp, IServiceCollection services) {
    }

    @Override
    public void cleanup() {
        //Do nothing
    }

    @Override
    public void init(FileOpener files) {
        mFiles = files;
        mTarget.init(files);
    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    @Override
    public String getConfigFilePathCommandLineOption() {
        return mTarget.getConfigFilePathCommandLineOption();
    }

    @Override
    public String getLineMappingFilePathCommandLineOption() {
        return mTarget.getLineMappingFilePathCommandLineOption();
    }

    @Override
    public String getLogFilePathCommandLineOption() {
        return mTarget.getLogFilePathCommandLineOption();
    }

    @Override
    public String getParentDirectoryCommandLineOption() {
        return mTarget.getParentDirectoryCommandLineOption();
    }

    @Override
    public String getProgramFilePathCommandLineOption() {
        return mTarget.getProgramFilePathCommandLineOption();
    }

    @Override
    public String getDebuggerPortCommandLineOption() {
        return mTarget.getDebuggerPortCommandLineOption();
    }

    @Override
    public String getSandboxModeEnabledOption() {
        return mTarget.getSandboxModeEnabledOption();
    }
}
