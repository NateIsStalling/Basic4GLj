package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.GLTextGridWindow;
import com.basic4gl.app.desktop.config.StandaloneCommandLineOptions;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.Exporter;
import com.basic4gl.compiler.util.IAssetExportBuilder;
import com.basic4gl.compiler.util.IPluginExportBuilder;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.language.core.extensions.Basic4GLCompiler;
import com.basic4gl.language.core.extensions.IAppSettings;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.runtime.IServiceCollection;
import com.basic4gl.language.core.runtime.VM;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Nate on 8/15/2015.
 */
public class BuilderDesktopGL extends Builder implements IAssetExportBuilder, IPluginExportBuilder {

    private DesktopTarget target;
    private FileOpener files;
    private final List<String> exportAssets = new ArrayList<>();
    private final List<String> exportPlugins = new ArrayList<>();
    private String exportAssetBaseDirectory;

    public static Builder getInstance(DesktopTarget target) {
        BuilderDesktopGL instance = new BuilderDesktopGL();
        instance.target = target;
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
        return target.getSettings();
    }

    @Override
    public Configuration getConfiguration() {
        return target.getConfiguration();
    }

    @Override
    public void setConfiguration(Configuration config) {
        target.setConfiguration(config);
    }

    @Override
    public boolean export(String filename, OutputStream stream, TaskCallback callback) throws Exception {
        int i;
        File file = new File(filename);
        File jar = new File(file.getName().replaceFirst("[.][^.]+$", "") + ".jar");
        String classpath = ".";
        // TODO set this as a parameter or global constant
        ZipOutputStream output = new ZipOutputStream(stream);
        JarOutputStream target;
        ZipEntry zipEntry;
        JarEntry jarEntry;

        // TODO Add build option for single Jar

        ClassLoader loader = getClass().getClassLoader();
        List<String> dependencies;

        // Generate class path
        dependencies = this.target.getClassPathObjects();
        i = 0;
        if (dependencies != null) {
            for (String dependency : dependencies) {
                classpath += " " + dependency;
                i++;
            }
        }

        // Add external dependencies
        dependencies = this.target.getDependencies();
        if (dependencies != null) {
            // pre-validate
            for (String dependency : dependencies) {
                File source = new File(dependency);

                if (!source.exists() || ClassLoader.getSystemClassLoader().getResource(dependency) == null) {

                    System.out.println("Dependency not found: " + dependency);
                    continue; // TODO throw build error
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
                    // TODO throw build error
                    // throw new Exception("Cannot find dependency: " + dependency);
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

        // Add selected assets to archive (preserve relative layout where possible)
        addExportAssets(output);

        // Add selected plugin JARs (always flattened under plugins/)
        addExportPlugins(output);

        // Add launcher script
        // TODO add additional scripts for each platform
        zipEntry = new ZipEntry("launcher.bat");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                        "::Run %1$s; requires Java be installed and in your system path\n"
                                + "::Log console output for debugging\n"
                                + ">output.log (\n"
                                + "\tjava -jar \"%1$s\"\n"
                                + ")",
                        jar.getName())
                .getBytes(StandardCharsets.UTF_8));
        output.closeEntry();

        // TODO update script for better logging
        zipEntry = new ZipEntry("launcher-macos.sh");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                        "# Run %1$s; requires Java be installed and in your system path\n"
                                + "# -XstartOnFirstThread is required by LWJGL for window to display on Mac"
                                + " OS\n"
                                + "java -XstartOnFirstThread -jar \"%1$s\"\n",
                        jar.getName())
                .getBytes(StandardCharsets.UTF_8));
        output.closeEntry();

        zipEntry = new ZipEntry("README.txt");
        zipEntry.setTime(System.currentTimeMillis());
        output.putNextEntry(zipEntry);
        output.write(String.format(
                        "Execute launcher.bat to run %1$s on Windows.\n"
                                + "To run %1$s on Mac OS, execute launcher-macos.sh or run the jar with the"
                                + " following additional Java option from the terminal: \n"
                                + "-XstartOnFirstThread",
                        jar.getName())
                .getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
        try {
            // Create application's manifest
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classpath);
            manifest.getMainAttributes()
                    .put(Attributes.Name.MAIN_CLASS, this.target.getMainClass().getTypeName());

            zipEntry = new ZipEntry(jar.getName());
            zipEntry.setTime(System.currentTimeMillis());
            output.putNextEntry(zipEntry);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            target = new JarOutputStream(bytes, manifest);

            // Add Basic4GLj classes to new Jar
            System.out.println("Adding source files");
            List<String> files = new ArrayList<>();
            // TODO Only add required classes
            files.add("com/basic4gl/app");
            files.add("com/basic4gl/compiler");
            files.add("com/basic4gl/language");
            files.add("com/basic4gl/lib"); // TODO this namespace should be renamed..
            files.add("com/basic4gl/library");
            files.add("com/basic4gl/util");
            files.add("com/basic4gl/runtime");
            files.add("paulscode"); // sound library
            files.add("org/apache/commons/cli"); // args support
            files.add("org/apache/commons/imaging"); // MD2 support
            files.add("org/lwjgl");
            // TODO this should only be added if target OS
            files.add("macos"); // lwjgl natives

            // exclude any html/javadoc to save on file size
            String excludeRegex = ".*html$";

            Exporter.addSource(files, excludeRegex, target);

            // Save VM's initial state to Jar
            this.target.reset();
            jarEntry = new JarEntry(StandaloneCommandLineOptions.STATE_FILE);
            target.putNextEntry(jarEntry);
            this.target.saveState(target);
            target.closeEntry();

            // Serialize the build configuration and add to Jar
            jarEntry = new JarEntry(StandaloneCommandLineOptions.CONFIG_FILE);
            target.putNextEntry(jarEntry);
            this.target.saveConfiguration(target);
            target.closeEntry();

            // TODO Implement embedding resources
            target.close();
            bytes.writeTo(output);
            // Writing Jar to archive complete
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

    private void addExportAssets(ZipOutputStream output) throws IOException {
        if (exportAssets.isEmpty()) {
            return;
        }

        Set<String> addedEntries = new HashSet<>();
        byte[] buffer = new byte[8192];
        for (String configuredPath : exportAssets) {
            if (configuredPath == null || configuredPath.isBlank()) {
                continue;
            }

            String normalizedConfiguredPath = FileUtil.separatorsToSystem(configuredPath);
            File configuredFile = new File(normalizedConfiguredPath);
            File source = resolveAssetFile(configuredFile);
            if (!source.exists() || !source.isFile()) {
                System.out.println("Asset not found, skipping: " + configuredPath);
                continue;
            }

            String zipPath;
            if (configuredFile.isAbsolute()) {
                zipPath = "assets/" + source.getName();
            } else {
                zipPath = normalizedConfiguredPath.replace('\\', '/');
                while (zipPath.startsWith("./")) {
                    zipPath = zipPath.substring(2);
                }
                while (zipPath.startsWith("/")) {
                    zipPath = zipPath.substring(1);
                }
            }
            if (zipPath.contains("..") || zipPath.isBlank()) {
                System.out.println("Unsafe asset path, skipping: " + configuredPath);
                continue;
            }
            if (!addedEntries.add(zipPath)) {
                continue;
            }

            ZipEntry assetEntry = new ZipEntry(zipPath);
            assetEntry.setTime(source.lastModified());
            output.putNextEntry(assetEntry);
            try (InputStream input = new FileInputStream(source)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            output.closeEntry();
        }
    }

    private File resolveAssetFile(File configuredFile) {
        if (configuredFile.isAbsolute()) {
            return configuredFile;
        }

        if (exportAssetBaseDirectory != null && !exportAssetBaseDirectory.isBlank()) {
            return new File(exportAssetBaseDirectory, configuredFile.getPath());
        }

        if (files != null) {
            String parentDirectory = files.getParentDirectory();
            if (parentDirectory != null && !parentDirectory.isBlank()) {
                return new File(parentDirectory, configuredFile.getPath());
            }
        }

        return configuredFile;
    }

    private void addExportPlugins(ZipOutputStream output) throws IOException {
        if (exportPlugins.isEmpty()) {
            return;
        }

        Set<String> addedEntries = new HashSet<>();
        byte[] buffer = new byte[8192];
        for (String configuredPath : exportPlugins) {
            if (configuredPath == null || configuredPath.isBlank()) {
                continue;
            }

            File source = resolvePluginFile(new File(FileUtil.separatorsToSystem(configuredPath)));
            if (!source.exists() || !source.isFile()) {
                System.out.println("Plugin JAR not found, skipping: " + configuredPath);
                continue;
            }

            String fileName = source.getName();
            if (fileName.isBlank()) {
                continue;
            }
            String zipPath = "plugins/" + fileName;
            if (!addedEntries.add(zipPath)) {
                continue;
            }

            ZipEntry pluginEntry = new ZipEntry(zipPath);
            pluginEntry.setTime(source.lastModified());
            output.putNextEntry(pluginEntry);
            try (InputStream input = new FileInputStream(source)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            output.closeEntry();
        }
    }

    private File resolvePluginFile(File configuredFile) {
        if (configuredFile.isAbsolute()) {
            return configuredFile;
        }

        if (exportAssetBaseDirectory != null && !exportAssetBaseDirectory.isBlank()) {
            return new File(exportAssetBaseDirectory, configuredFile.getPath());
        }

        if (files != null) {
            String parentDirectory = files.getParentDirectory();
            if (parentDirectory != null && !parentDirectory.isBlank()) {
                return new File(parentDirectory, configuredFile.getPath());
            }
        }

        return configuredFile;
    }


    @Override
    public void setExportAssets(List<String> assets) {
        exportAssets.clear();
        if (assets != null) {
            exportAssets.addAll(assets);
        }
    }

    @Override
    public void setExportAssetBaseDirectory(String baseDirectory) {
        exportAssetBaseDirectory = FileUtil.separatorsToSystem(baseDirectory);
    }

    @Override
    public List<String> getExportAssets() {
        return new ArrayList<>(exportAssets);
    }


    @Override
    public void setExportPlugins(List<String> pluginPaths) {
        exportPlugins.clear();
        if (pluginPaths != null) {
            exportPlugins.addAll(pluginPaths);
        }
    }

    @Override
    public List<String> getExportPlugins() {
        return new ArrayList<>(exportPlugins);
    }

    @Override
    public Target getTarget() {
        return target;
    }

//    @Override
//    public IVMDriver getVMDriver() {
//        return target;
//    }

    @Override
    public String getName() {
        return "Desktop Application";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public String getDescription() {
        return "Desktop application with OpenGL capabilities.";
    }

    @Override
    public String getAuthor() {
        return "Nathaniel Nielsen";
    }

    @Override
    public String getContact() {
        return "https://github.com/NateIsStalling/Basic4GLj/issues";
    }

    @Override
    public String getId() {
        return "desktopgl";
    }

    @Override
    public void cleanup() {
        // Do nothing
    }

    @Override
    public void init(FileOpener files) {
        this.files = files;
        target.init(new FileOpenerAdapter(files));
    }
//
//    @Override
//    public List<String> getDependencies() {
//        return null;
//    }
//
//    @Override
//    public List<String> getClassPathObjects() {
//        return null;
//    }
//
//    @Override
//    public String getConfigFilePathCommandLineOption() {
//        return target.getConfigFilePathCommandLineOption();
//    }
//
//    @Override
//    public String getLineMappingFilePathCommandLineOption() {
//        return target.getLineMappingFilePathCommandLineOption();
//    }
//
//    @Override
//    public String getLogFilePathCommandLineOption() {
//        return target.getLogFilePathCommandLineOption();
//    }
//
//    @Override
//    public String getParentDirectoryCommandLineOption() {
//        return target.getParentDirectoryCommandLineOption();
//    }
//
//    @Override
//    public String getProgramFilePathCommandLineOption() {
//        return target.getProgramFilePathCommandLineOption();
//    }
//
//    @Override
//    public String getDebuggerPortCommandLineOption() {
//        return target.getDebuggerPortCommandLineOption();
//    }
//
//    @Override
//    public String getSandboxModeEnabledOption() {
//        return target.getSandboxModeEnabledOption();
//    }
}
