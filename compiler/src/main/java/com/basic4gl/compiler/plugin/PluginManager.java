package com.basic4gl.compiler.plugin;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.plugin.sdk.plugin.Basic4GLPlugin;
import com.basic4gl.runtime.util.Mutable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class PluginManager
{

    protected Vector<PluginLibrary> plugins = new Vector<>();
    protected  String error;
    protected HashMap< String, PluginJARSharedInterface> sharedInterfaces = new HashMap<>();
    protected boolean isStandaloneExe;

    // Data structures defined by plugins
    protected PluginStructureManager structureManager;

    protected  String getSharedInterfaceKey(
         String name,
        int major,
        int minor) {
        return name + "_" + major + "_" + minor;
    }


    public PluginManager(boolean isStandaloneExe){
        this.isStandaloneExe = isStandaloneExe;
    }
    public void dispose() {
    clear();
    }

    // Member access
    public PluginStructureManager getStructureManager() { return structureManager; }

    // Library registration
    public boolean addPlugin(Basic4GLPlugin plugin){

        // Wrap in library object.
        // This will also initialise the plugin and report back any error
        PluginLibrary library = new PluginLibrary(this, plugin, isStandaloneExe);
        if (library.hasFailed()) {
            error = library.getError();
            library.dispose();
            return false;
        }

        // Add to list
        plugins.add(library);
        return true;
    }

    // DLL loading/unloading

    /// Iterate loaded libraries
    public Vector<PluginLibrary> getLoadedLibraries() { return plugins; }

    /// Return text of last error.
    public  String getError() { return error; }

    /// Unload all files
    public void clear(){

        // Unload all DLLs
        for (PluginLibrary library : plugins) {
            library.dispose();
        }

        // Clear list
        plugins.clear();
    }

    // Find DLL by name

    /// Return true if name matches a DLL function
    public boolean isPluginFunction(String name){

        // Look for matching function name
        for(PluginLibrary library: plugins) {
            if (library.isFunction(name)) {
                return true;
            }
        }
        // (None found)
        return false;
    }

    /// Find constant with a given name within all loaded DLLs
    public Constant findConstant(String name) {

        // Search all DLLs for constant
        for (int i = 0; i < plugins.size(); i++){
            Constant value = plugins.get(i).findConstant(name);
            if (value != null){
                return value;
            }
        }

        return null;
    }

    /// Find functions of a given name within all loaded DLLs and append to array
    public void findFunctions(
            String name,
            ArrayList<ExtendedFunctionSpecification> functions,
            Mutable<Integer> count,
            int max){

        // Pass call through to all loaded plugin dlls.
        for (int i = 0; i < plugins.size(); i++) {
            plugins.get(i).findFunctions(name, functions, count, max, i);
        }
    }

    public  String getFunctionName(int pluginIndex, int functionIndex){
        if (pluginIndex >= 0 && pluginIndex < plugins.size()) {
            return plugins.get(pluginIndex).getDescription() + " " + plugins.get(pluginIndex).getFunctionName(functionIndex);
        }else {
            return "???";
        }
    }

    /// Create virtual machine version of function specifications.
    /// Called immediately before a compile, but AFTER the VM versions of the
    /// plugin structures have been created.
    public void createVMFunctionSpecs(){
        for (PluginLibrary library: plugins) {
            library.createVMFunctionSpecs();
        }
    }


    // DLL events

    /**
     *
     * @return Returns true if all plugins started successfully. Otherwise program should not proceed.
     */
    public boolean programStart()
    {
        for (int i = 0; i < plugins.size(); i++) {
        // Attempt to start plugin
        if (!plugins.get(i).getPlugin().start()) {
            // Plugin failed!

            // Get error
            error = "Error initialising plugin " + plugins.get(i).getDescription() + ": " + plugins.get(i).getError();

            // Stop all plugins that have just been started
            for (int j = i - 1; j >= 0; j--) {
                plugins.get(j).getPlugin().end();
            }

            // Abort program
            return false;
        }
    }

        // All plugins started successfully
        error = "";
        return true;
    }
    public void onProgramEnd(){
        for (PluginLibrary library: plugins) {
            library.clearObjectStores();           // Also clear object stores.
            library.getPlugin().end();
        }
    }
    public void onProgramPause() {
        for (PluginLibrary library: plugins) {
            library.getPlugin().pause();
        }
    }

    public void onProgramResume() {
        for (PluginLibrary library: plugins) {
            library.getPlugin().resume();
        }
    }
    public void onProgramDelayedResume() {
        for (PluginLibrary library: plugins) {
            library.getPlugin().delayedResume();
        }
    }

    public void onProcessMessages() {
        for (PluginLibrary library: plugins) {
            library.getPlugin().processMessages();
        }
    }

    // Interface sharing
    public void registerInterface(
            Object intf,
             String name,
            int major,
            int minor,
            PluginLibrary owner) {

        // Construct unique string key
        String key = getSharedInterfaceKey(name, major, minor);

        // Save interface to key
        sharedInterfaces.put(key, new PluginJARSharedInterface(intf, owner));
    }

    public Object fetchInterface(
         String name,
        int major,
        int minor,
        PluginLibrary requester) {

    // Construct unique string key
    String key = getSharedInterfaceKey(name, major, minor);

    // Fetch object
        if (!sharedInterfaces.containsKey(key)) {
            return null;
        }

        PluginJARSharedInterface obj = sharedInterfaces.get(key);

        // Add a dependency between the requester and the owner.
    if (requester != null && obj.getOwner() != null) {
        obj.getOwner().addReferencingPlugin(requester);
    }

    // Return the interface
    return obj.getInterface();
}

    public int fetchStructure(
             String name,
            int major,
            int minor,
            PluginLibrary requester) {

        // Find handle
        int handle = getStructureManager().findStructure(name);
        if (handle != 0) {

            // Find structure and check version
            PluginStructure structure = getStructureManager().getStructure(handle);
            if (structure.getVersionMajor() == major &&
                    structure.getVersionMinor() == minor) {

                // Add dependency between structure owner and requester
                if (structure.getOwner() != null && structure.getOwner() != requester) {
                    ((PluginLibrary) structure.getOwner()).addReferencingPlugin(requester);
                }
                return handle;
            }
        }

        // Not found or version mismatch
        return 0;
    }


     void streamOut(DataOutputStream stream) throws IOException {}
     boolean streamIn(DataInputStream stream) throws IOException {
         return true;
     }
};
