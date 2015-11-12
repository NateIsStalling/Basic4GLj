package com.basic4gl.lib.targets.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.util.Exporter;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

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
    public String getFileDescription() { return "Java Application (*.jar)";}

    @Override
    public String getFileExtension() { return "jar";}

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
    public boolean export(OutputStream stream, TaskCallback callback) throws Exception{
        int i;
        String path;
        //TODO set this as a parameter or global constant
        String libRoot = "jar/"; //External folder where dependencies should be located
        JarEntry entry;

        //TODO Add build option for single Jar

        ClassLoader loader = getClass().getClassLoader();
        List<String> dependencies;

        //Create application's manifest
        Manifest manifest = new Manifest();

        path = "";
        //Generate class path
        dependencies = mTarget.getDependencies();
        i = 0;
        if (dependencies != null)
            for (String dependency: dependencies) {
                path += ((i != 0 ) ? " " : "") + libRoot + dependency;
                i++;
            }
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, path);
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
            mTarget.getClass().getName());
        JarOutputStream target = new JarOutputStream(stream, manifest);

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
        entry = new JarEntry(GLTextGridWindow.STATE_FILE);
        target.putNextEntry(entry);
        mTarget.saveState(target);
        target.closeEntry();

        //Serialize the build configuration and add to Jar
        entry = new JarEntry(GLTextGridWindow.CONFIG_FILE);
        target.putNextEntry(entry);
        mTarget.saveConfiguration(target);
        target.closeEntry();

        //Add external libraries
        //TODO embed resource files
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

        target.close();
        return true;
    }

    @Override
    public Target getTarget() {
        return mTarget;
    }

    @Override
    public String name() { return "Desktop Application";}

    @Override
    public String version() { return "0.1";}

    @Override
    public String description() { return "Desktop application with OpenGL capabilities.";}

    @Override
    public String author() { return "Nathaniel Nielsen";}

    @Override
    public String contact() { return "support@crazynatestudios.com";}

    @Override
    public String id() { return "desktopgl";}

    @Override
    public String[] compat() {
        return new String[0];
    }

    @Override
    public void initVM(TomVM vm) {

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

}
