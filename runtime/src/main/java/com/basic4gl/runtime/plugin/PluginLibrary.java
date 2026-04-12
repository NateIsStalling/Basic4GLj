package com.basic4gl.compiler.plugin;

import com.basic4gl.runtime.types.Constant;
import com.basic4gl.runtime.types.FunctionSpecification;
import com.basic4gl.runtime.plugin.*;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Assert;
import com.basic4gl.runtime.util.Mutable;

import java.util.*;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * A plugin library of functions, structure types and constants, as the
 * Basic4GL compiler and virtual machine sees it.
 * Can be implemented as a DLL (see PluginDLL), or a local library version
 */
public class PluginLibrary implements Basic4GLFunctionRegistry {

    public static int getBasicValTypeFromPluginTypeCode(Basic4GLTypeCode typeCode) {
        return switch (typeCode) {
            case INT -> BasicValType.VTP_INT;
            case FLOAT -> BasicValType.VTP_REAL;
            case STRING -> BasicValType.VTP_STRING;
            case FUNCPTR -> BasicValType.VTP_UNTYPED_FUNC_PTR;
            default -> BasicValType.VTP_UNDEFINED;
        };
    }
    /// Main plugin manager
    private PluginManager manager;

    // region FunctionRegistry data

    /**
     * True if currently defining a function
     */
    private boolean definingFunc;
    /**
     * Pointer to current function
     */
    private Basic4GLFunction currentFunc;
    /**
     * Current plugin runtime function being defined
     */
    private final FunctionSpecification currentSpec;
    /**
     * Name of current function
     */
    private String             currentName;
    // endregion

    // Plugin constants
    private final HashMap<String, Constant> constants;

    // Plugin functions
    private final Vector<Basic4GLFunction>  functions = new Vector<>();
    private final HashMap<String, List<Integer>> functionLookup = new HashMap<>();     // Maps function name to index

    // Function specifications
    // Note: We maintain 2 versions of each.
    // The first version uses the plugin structure index when referring to structures
    // (for return types or parameter types). The second uses the virtual machine
    // structure index.
    private final Vector<FunctionSpecification>           pluginFunctionSpecs = new Vector<>();
    private final Vector<FunctionSpecification>           vmFunctionSpecs = new Vector<>();

    // Resource stores
    private final Vector<Basic4GLObjectStore> objectStores = new Vector();

    // Structure building
    private PluginStructure currentStructure;

    // Plugin->Plugin references
    /**
     * Plugins that reference this one
     */
    private final Set<PluginLibrary> referencingPlugins = new HashSet<>();

    /// Main plugin interface
    protected Basic4GLPlugin plugin;

    /// True if loading failed. Calling code should not try to use library.
    protected boolean failed;

    protected PluginLibrary(PluginManager manager) {
        this.manager = manager;
        this.plugin = null;
        this.currentStructure = null;
        this.currentSpec = new FunctionSpecification();
        this.constants = new HashMap<>();

        // Set defaults
        definingFunc = false;
        failed = true;			// Descendent class must set to true on successful create
    }

    public PluginLibrary(PluginManager manager, Basic4GLPlugin plugin, boolean isStandaloneExe) {
        this.manager = manager;
        this.plugin = plugin;
        this.currentStructure = null;
        this.currentSpec = new FunctionSpecification();
        this.constants = new HashMap<>();

        // Set defaults
        definingFunc = false;
        failed = true;;

        // Inform plugin it has been loaded. Let it register its functions.
        if (!plugin.load(this, isStandaloneExe)) {
            return;
        }
        completeFunction();

        failed = false;
    }

    // Routines
    protected void completeFunction(){
        if (!definingFunc)
            return;

        // Fix up return and parameter types
        if (currentSpec.isFunction()) {
            fixType(currentSpec.getReturnType());
        }
        for(ValType v: currentSpec.getParamTypes().getParams()) {
            fixType(v);
        }
        // Finish off the current function

        // Store pointer and specification
        currentSpec.setIndex(functions.size());
        functions.add(currentFunc);
        pluginFunctionSpecs.add(currentSpec);

        // Add name->index mapping
        functionLookup
            .computeIfAbsent(currentName, k -> new ArrayList<>())
            .add(currentSpec.getIndex());

        definingFunc = false;
    }

    protected void newFunction(String name, Basic4GLFunction function){

        // Complete any partially defined function
        completeFunction();

        // Save function info
        currentName = name.toLowerCase();
        currentFunc = function;

        // Set function defaults
        currentSpec.getParamTypes().getParams().clear();
        currentSpec.setBrackets(true);
        currentSpec.setFunction(false);
        currentSpec.setReturnType(new ValType(BasicValType.VTP_INT));
        currentSpec.setTimeshare( false);
        currentSpec.setConditionalTimeshare(false);
        currentSpec.setIndex(0);
        currentSpec.setFreeTempData(false);

        definingFunc = true;
    }
    protected void fixType(ValType type) {

        // Fix up a function return type or parameter type.
        // Convert arrays and structures to by-reference.
        if (type.pointerLevel == 0 &&
                (type.basicType >= 0 || type.arrayLevel > 0)) {
            type.pointerLevel = 1;
            type.isByRef = true;
        }
    }
    protected void unload(){
        if (plugin != null) {
            // Clear object stores
            clearObjectStores();

            // Clear structures
            manager.getStructureManager().removeOwnedStructures(this);

            // Unload plugin
            if (plugin != null) {
                plugin.unload();
            }

            // Delete object stores
            for (Basic4GLObjectStore objectStore: objectStores) {
                objectStore.clear();
            }
            objectStores.clear();

            plugin = null;
        }
    }


    public void dispose() {
        unload();
    }

    // Basic4GLFunctionRegistry methods
    public void registerStringConstant(
		String name,
		String value) {
        constants.put(name.toLowerCase(), new Constant("S" + value));
    }
    public  void registerIntConstant(
		String name,
        int value) {
        constants.put(name.toLowerCase(), new Constant(value));
    }
    public  void registerFloatConstant(
		String name,
        float value) {
        constants.put(name.toLowerCase(), new Constant(value));
    }
    public  void registerVoidFunction(
		String name,
        Basic4GLFunction function) {

        // Create new void function
        newFunction(name, function);
    }
    public  void registerFunction(
		String name,
        Basic4GLFunction function,
        Basic4GLTypeCode typeCode) {

        // Create new function
        newFunction(name, function);

        // Set return type
        currentSpec.setFunction(true);
        currentSpec.setReturnType(new ValType(getBasicValTypeFromPluginTypeCode(typeCode)));
    }
    public  void registerArrayFunction(
		String name,
        Basic4GLFunction function,
        Basic4GLTypeCode typeCode,
        int dimensions) {

        // Create new function
        newFunction(name, function);

        // Set return type
        currentSpec.setFunction(true);
        currentSpec.setReturnType(new ValType(getBasicValTypeFromPluginTypeCode(typeCode), (byte) dimensions, (byte) 1, true));
        currentSpec.setFreeTempData(true);
    }
    public  void registerStructureFunction(
		String name,
        Basic4GLFunction function,
        int structureTypeHandle) {

        // Create new function
        newFunction(name, function);

        // Set return type
        currentSpec.setFunction(true);
        currentSpec.setFreeTempData(true);
        currentSpec.setReturnType(new ValType(structureTypeHandle));
    }
    public  void modReturnArray(byte dimensions){
        currentSpec.getReturnType().arrayLevel = dimensions;
        currentSpec.setFreeTempData(true);
    }
    public  void modReturnPointer(byte level) {
        currentSpec.getReturnType().pointerLevel = level;
    }
    public  void modNoBrackets(){
        // Note: Basic4GL parsing doesn't handle functions with no parameters properly.
        // Therefore we only set no-brackets if the function is void.
        if (!currentSpec.isFunction())
            currentSpec.setBrackets(false);
    }

    public  void modTimeshare() {
        currentSpec.setTimeshare(true);
    }

    public  void modConditionalTimeshare(){
        currentSpec.setConditionalTimeshare(true);
    }
    public  void addParam(Basic4GLTypeCode typeCode) {
        currentSpec.getParamTypes().addParam( new ValType(getBasicValTypeFromPluginTypeCode(typeCode)));
    }
    public  void addArrayParam(
            Basic4GLTypeCode typeCode,
            int dimensions){
        currentSpec.getParamTypes().addParam( new ValType(getBasicValTypeFromPluginTypeCode(typeCode), (byte)dimensions, (byte) 1, true));
    }
    public  void addStrucParam(int handle){
        currentSpec.getParamTypes().addParam( new ValType(handle));
    }
    public  void modParamArray(byte dimensions) {
        // Find last added param
        if (currentSpec.getParamTypes().getParams().size() > 0) {
            ValType param = currentSpec.getParamTypes().getParams().lastElement();

            // Convert into array
            param.arrayLevel = dimensions;
        }
    }
    public  void modParamPointer(byte level) {
        // Find last added param
        if (currentSpec.getParamTypes().getParams().size() > 0) {
            ValType param = currentSpec.getParamTypes().getParams().lastElement();

            // Convert into pointer
            param.pointerLevel = level;
        }
    }
    public  void modParamReference() {
        // Find last added param
        if (currentSpec.getParamTypes().getParams().size() > 0) {
            ValType param = currentSpec.getParamTypes().getParams().lastElement();

            // Convert into by-reference param
            if (!param.isByRef) {
                param.isByRef = true;
                param.pointerLevel++;
            }
        }
    }
    public  void registerInterface(
            Object object,
		String name,
            int major,
            int minor){
        manager.registerInterface(object, name, major, minor, this);
    }
    public  Object fetchInterface(
		String name,
        int major,
        int minor) {
        return manager.fetchInterface(name, major, minor, this);
    }
    public Basic4GLObjectStore createObjectStore(Basic4GLObjectStoreListener listener) {

        // Create object store
        Basic4GLObjectStore store = new PluginJARObjectStore(listener);

        // Register it
        objectStores.add(store);

        return store;
    }

    public  int registerStructure(String name, int versionMajor, int versionMinor) {

        // Structure with same name must not already be registered
        if (manager.getStructureManager().findStructure(name) != 0)
            return 0;

        // Create new structure
        currentStructure = new PluginStructure(this, name, versionMajor, versionMinor);

        // Register and retrieve handle
        return manager.getStructureManager().add(currentStructure);
    }
    public  void addStrucPadding(int numBytes) {
        if (currentStructure != null)
            currentStructure.addField(
                    new PluginStructureField("", PluginDataType.padding(numBytes)));
    }
    public  void addStrucField(String name, int type) {
        if (currentStructure != null)
            currentStructure.addField(
                    new PluginStructureField(name, PluginDataType.simpleType(type)));
    }
    public  void addStrucStringField(String name, int size) {
        if (currentStructure != null)
            currentStructure.addField(
                    new PluginStructureField(name, PluginDataType.string(size)));
    }
    public  void  modStrucFieldArray(byte dimensions, int dimension1Size, int... otherSizes){

        Assert.assertTrue(dimensions < TomVM.ARRAY_MAX_DIMENSIONS);
        int[] dimensionArray = new int[TomVM.ARRAY_MAX_DIMENSIONS];
        if (dimensions > 0)
        {
            dimensionArray[0] = dimension1Size;

            for (int i = 1; i < dimensions; i++) {
                dimensionArray[i] = otherSizes[i - 1];
            }
        }

        // Find last defined field
        if (currentStructure != null && currentStructure.getFieldCount() > 0) {
            PluginStructureField field = currentStructure.getField(currentStructure.getFieldCount() - 1);

            // Convert data type to array
            PluginDataType dataType = field.getDataType();
            dataType.setArrayLevel(dimensions);

            // Set array dimension sizes
            for (int srcIndex = 0; srcIndex < dimensions; srcIndex++) {
                int destIndex = dimensions - 1 - srcIndex;
                dataType.getArrayDims()[destIndex] = dimensionArray[srcIndex];
            }
        }
    }
    public  void  modStrucFieldPointer(byte level) {
        // Find last defined field
        if (currentStructure != null && currentStructure.getFieldCount() > 0) {
            PluginStructureField field = currentStructure.getField(currentStructure.getFieldCount() - 1);

            // Convert data type to pointer
            field.getDataType().setPointerLevel(level);
        }
    }
    public  int fetchStructure(
		String name,
        int versionMajor,
        int versionMinor) {
        return manager.fetchStructure(name, versionMajor, versionMinor, this);
    }

    public Basic4GLPlugin getPlugin() { return plugin; }
    public Vector<FunctionSpecification> getFunctionSpecs() { return pluginFunctionSpecs; }

    /// Return true if plugin failed to load and/or initialise
    public boolean hasFailed() { return failed; }

    /// Plugin description for error reporting etc
    public String getDescription() {
        return "Builtin library";
    }

    /// Search for a constant
    public Constant findConstant(String name){
        return constants.getOrDefault(name, null);
    }

    /// Return true if name matches a function in this DLL
    public boolean isFunction(String name) {
        return functionLookup.get(name.toLowerCase()) != null;
    }

    /// Find all functions of a given name within this DLL and add them to the array
    public void findFunctions(
            String name,
            ExtendedFunctionSpecification[] functions,
            Mutable<Integer> count,
            int max,
            int pluginIndex){

        // Find matching functions
        List<Integer> indices = functionLookup.get(name);
        if (indices != null) {
            for (int specIdx : indices) {
                if (count.get() >= max) break;

                if (specIdx >= 0 && specIdx < vmFunctionSpecs.size()) {
                    FunctionSpecification spec = vmFunctionSpecs.get(specIdx);

                    ExtendedFunctionSpecification ext = functions[count.get()];
                    ext.setSpecification(spec);
                    ext.setBuiltin(false);
                    ext.setPluginIndex(pluginIndex);

                    count.set(count.get() + 1);
                }
            }
        }

    }

    // Function retrieval by index
    public int count() { return functions.size(); }

    /// Retrieve function by index
    public Basic4GLFunction getFunction(int index) {
        Assert.assertTrue(index >= 0);
        Assert.assertTrue(index < count());
        return functions.get(index);
    }

    /// Retrieve function name by index
    public String getFunctionName(int index){
        for (Map.Entry<String, List<Integer>> entry : functionLookup.entrySet()) {
            for (Integer value : entry.getValue()) {
                if (value == index) {
                    return entry.getKey();
                }
            }
        }

        return "???";
    }

    /// Iterate constants
    public HashMap<String, Constant> getConstants() { return constants; }

    /// Clear all resource stores allocated by DLL
    public void clearObjectStores() {
        for (Basic4GLObjectStore objectStore: objectStores) {
            objectStore.clear();
        }
    }

    /// Create VM versions of function specifications
    public void createVMFunctionSpecs() {

        // Create virtual machine version of each function definition.
        // These are basically identical to the plugin version, except that
        // plugin structure indices in each data type are replaced with the
        // corresponding virtual machine structure index.
        vmFunctionSpecs.clear();
        for (FunctionSpecification i : pluginFunctionSpecs) {

            // Create function spec (convert return type)
            FunctionSpecification src = i;
            FunctionSpecification dst = new FunctionSpecification(src);
            if (dst.isFunction()) {
                dst.setReturnType(manager.getStructureManager().getVMType(dst.getReturnType()));
            }
            // Convert parameter types
            dst.getParamTypes().getParams().clear();
            for (ValType j : src.getParamTypes().getParams()) {

                dst.getParamTypes().addParam(manager.getStructureManager().getVMType(j));
            }
            vmFunctionSpecs.add(dst);
        }
    }

    /// Error text if failed
    public String getError() {
        return plugin.getError();
    }

    /// Log that we are referenced by another Plugin.
    /// (Thus we cannot be unloaded until the other Plugin has been).
    public void addReferencingPlugin(PluginLibrary plugin){

        // (Ignore self-references)
        if (plugin != this)
            referencingPlugins.add(plugin);
    }

    /// Log that we are no longer referenced by another specific Plugin.
    /// (Typically means that Plugin has been unloaded)
    public void removeReferencingPlugin(PluginLibrary plugin) {
        referencingPlugins.remove(plugin);
    }

    /// Return true if we are referenced by at least one other DLL
    public boolean isReferenced() { return !referencingPlugins.isEmpty(); }

    /// List Plugins that reference this one
    public String describeReferencingPlugins(){

        // Build a comma separated list of plugins which reference this one
        String result = "";
        int i = 0;
        for (PluginLibrary plugin: referencingPlugins) {
            if (i > 0)
                result += ", ";
            result += plugin.getDescription();
            i++;
        }
        return result;
    }

    /// Return true if this is the PluginJar class
    public boolean isPluginJar() { return  false; }

    // Raw function specification access
    HashMap<String, List<Integer>> getFunctionLookup() { return functionLookup; }
    Vector<FunctionSpecification> getVMFunctionSpecs() { return vmFunctionSpecs; }
}
