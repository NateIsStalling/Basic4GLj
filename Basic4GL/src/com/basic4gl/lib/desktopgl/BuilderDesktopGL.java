package com.basic4gl.lib.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.util.Exporter;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static Library getInstance(TomBasicCompiler compiler){
        BuilderDesktopGL instance = new BuilderDesktopGL();
        instance.mTarget = (GLTextGridWindow) GLTextGridWindow.getInstance(compiler);
        return instance;
    }
    @Override
    public String getFileDescription() { return "Java Application (*.zip)";}

    @Override
    public String getFileExtension() { return "zip";}

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
    public boolean export(String filename, OutputStream stream, TaskCallback callback) throws Exception{
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
        if (dependencies != null)
            for (String dependency: dependencies) {
                classpath += " " + dependency;
                i++;
            }

        //Add external dependencies
        dependencies = mTarget.getDependencies();
        if (dependencies != null)
            for (String dependency : dependencies) {
                File source = new File(dependency);
                if (!source.exists())
                    continue;       //TODO throw build error
                FileInputStream input = new FileInputStream(source);

                zipEntry = new ZipEntry(dependency);
                zipEntry.setTime(source.lastModified());

                output.putNextEntry(zipEntry);
                for (int c = input.read(); c != -1; c = input.read()) {
                    output.write(c);
                }
                output.closeEntry();
            }

        //Add launcher script
        //TODO add additional scripts for each platform
        zipEntry = new ZipEntry("launcher.bat");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                "::Run %1$s; requires Java be installed and in your system path\n" +
                        "::-Djava.library.path=native/ is needed for window to display\n" +
                        "::Log console output for debugging\n" +
                        ">output.log (\n" +
                        "\tjava -jar -Djava.library.path=native/ %1$s\n" +
                        ")",
                jar.getName()).getBytes(StandardCharsets.UTF_8));
        output.closeEntry();

        zipEntry = new ZipEntry("README.txt");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                "Execute launcher.bat to run %1$s, or open the jar from the terminal with the following argument: \n" +
                        "-Djava.library.path=native/",
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
            files.add("com/basic4gl/lib");
            files.add("com/basic4gl/util");
            files.add("com/basic4gl/vm");
            Exporter.addSource(files, target);

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


        } catch (Exception e){
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
    public String name() { return "Desktop Application";}

    @Override
    public String version() { return "1";}

    @Override
    public String description() { return "Desktop application with OpenGL capabilities.";}

    @Override
    public String author() { return "Nathaniel Nielsen";}

    @Override
    public String contact() { return "support@stallingsoftware.com";}

    @Override
    public String id() { return "desktopgl";}


    @Override
    public void init(TomVM vm) {

    }
    @Override
    public void init(TomBasicCompiler comp){

    }
    @Override
    public Map<String, Constant> constants() {
        return null;
    }

    @Override
    public Map<String, FuncSpec[]> specs() {
        return null;
    }

    @Override
    public HashMap<String, String> getTokenTips() {
        return null;
    }

    @Override
    public List<String> getDependencies() {
        return null;
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

}
