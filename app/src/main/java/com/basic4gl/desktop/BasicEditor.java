package com.basic4gl.desktop;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.ErrorCallback;
import com.basic4gl.debug.protocol.callbacks.EvaluateWatchCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.Variable;
import com.basic4gl.desktop.debugger.*;
import com.basic4gl.desktop.editor.BasicTokenMaker;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.util.EditorSourceFile;
import com.basic4gl.desktop.util.EditorUtil;
import com.basic4gl.desktop.util.MainEditor;
import com.basic4gl.lib.util.*;
import com.basic4gl.library.desktopgl.BuilderDesktopGL;
import com.basic4gl.library.desktopgl.GLTextGridWindow;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.InstructionPosition;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class BasicEditor implements MainEditor, IApplicationHost, IFileProvider {

    private static final int VARIABLES_PAGE_SIZE = 256;
    private static final int DISASSEMBLY_PAGE_SIZE = 200;

    private final IEditorPresenter presenter;
    private final Map<Integer, String> evaluateRequests = new HashMap<>();
    private final Map<Integer, String> vmViewEvaluateRequests = new HashMap<>();
    private final Map<Integer, VariablesPageRequest> pendingVariableRequests = new HashMap<>();
    private final Map<Integer, List<Variable>> variablePagesByReference = new HashMap<>();
    private final Map<Integer, DisassemblyPageRequest> pendingDisassemblyRequests = new HashMap<>();
    private final List<DisassembledInstruction> disassemblyPages = new ArrayList<>();

    // Runtime settings
    private final IConfigurableAppSettings appSettings;

    // Virtual machine and compiler
    private VmWorker vmWorker; // Debugging
    public TomBasicCompiler compiler; // Compiler
    private FileOpener fileOpener;
    private final DebuggerCallbackMessage callbackMessage = new DebuggerCallbackMessage();

    private EditorSettings settings = null;

    // Preprocessor
    public Preprocessor preprocessor;

    // Debugger
    public Debugger debugger;

    // State
    // TODO this may need to be moved into appSettings
    private TomBasicCompiler.LanguageSyntax languageSyntax = TomBasicCompiler.LanguageSyntax.LS_BASIC4GL;

    // Libraries
    private final List<Library> libraries = new ArrayList<>();
    private final List<Integer> builders = new ArrayList<>(); // Indexes of libraries that can be launch targets
    public int currentBuilder = -1; // Index of mTarget in mTargets

    // Editor state
    private ApMode mode = ApMode.AP_STOPPED;

    private final List<String> watchList = new ArrayList<>();

    private final FileManager fileManager;

    private String libraryPath;

    public BasicEditor(
            String libraryPath,
            FileManager fileManager,
            IEditorPresenter presenter,
            IConfigurableAppSettings appSettings,
            Preprocessor preprocessor,
            Debugger debugger,
            TomBasicCompiler compiler) {
        this.libraryPath = libraryPath;
        this.fileManager = fileManager;
        this.presenter = presenter;
        this.appSettings = appSettings;
        this.preprocessor = preprocessor;
        this.debugger = debugger;
        this.compiler = compiler;
    }

    public void initLibraries() {
        // TODO Implement standard libraries
        // Plug in constant and function libraries
        /*
         * InitTomStdBasicLib (mComp); // Standard library
         * InitTomWindowsBasicLib (mComp, &m_files); // Windows specific
         * library InitTomOpenGLBasicLib (mComp, m_glWin, &m_files); // OpenGL
         * InitTomTextBasicLib (mComp, m_glWin, m_glText); // Basic
         * text/sprites InitGLBasicLib_gl (mComp); InitGLBasicLib_glu (mComp);
         * InitTomJoystickBasicLib (mComp, m_glWin); // Joystick support
         * InitTomTrigBasicLib (mComp); // Trigonometry library
         * InitTomFileIOBasicLib (mComp, &m_files); // File I/O library
         * InitTomNetBasicLib (mComp); // Networking
         */

        IServiceCollection tempServices = new ServiceCollection();

        // TODO Load libraries dynamically
        libraries.add(new com.basic4gl.library.standard.Standard());
        libraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
        libraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
        libraries.add(new com.basic4gl.library.standard.TrigBasicLib());
        libraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
        libraries.add(new com.basic4gl.library.standard.NetBasicLib());
        libraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());
        libraries.add(new com.basic4gl.library.standard.TomCompilerBasicLib());

        libraries.add(GLTextGridWindow.getInstance(compiler));
        libraries.add(BuilderDesktopGL.getInstance(compiler));

        fileOpener = new FileOpener(fileManager.getCurrentDirectory());
        // TODO Add more libraries
        int i = 0;
        for (Library lib : libraries) {
            lib.init(compiler, tempServices); // Allow libraries to register function overloads
            if (lib instanceof IFileAccess) {
                // Allows libraries to read from directories
                ((IFileAccess) lib).init(fileOpener);
            }
            if (lib instanceof FunctionLibrary) {
                compiler.addConstants(((FunctionLibrary) lib).constants());
                compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            }
            if (lib instanceof Builder) {
                builders.add(i);
            }
            i++;
        }
        // Set default target
        if (!builders.isEmpty()) {
            currentBuilder = 0;
        }

        // Initialize highlighting
        // mKeywords = new HashMap<String,Color>();
        BasicTokenMaker.reservedWords.clear();
        BasicTokenMaker.functions.clear();
        BasicTokenMaker.constants.clear();
        BasicTokenMaker.operators.clear();
        for (String s : compiler.getReservedWords()) {
            BasicTokenMaker.reservedWords.add(s);
        }

        for (String s : compiler.getConstants().keySet()) {
            BasicTokenMaker.constants.add(s);
        }

        for (String s : compiler.getFunctionIndex().keySet()) {
            BasicTokenMaker.functions.add(s);
        }

        for (String s : compiler.getBinaryOperators()) {
            BasicTokenMaker.operators.add(s);
        }
        for (String s : compiler.getUnaryOperators()) {
            BasicTokenMaker.operators.add(s);
        }
    }

    public void actionRun() {

        //                libraryPath
        // TODO fix run

        if (mode == ApMode.AP_STOPPED) {
            // Compile and run program from start
            reset();
            show(new DebugCallback());
            Library builder = libraries.get(builders.get(currentBuilder));
            RunHandler handler = new RunHandler(this, appSettings, compiler, preprocessor);
            handler.launchRemote(
                    builder, fileManager.getCurrentDirectory(), libraryPath); // 12/2020 testing new continue()

        } else {
            // Stop program completely.
            vmWorker.stopApplication();
        }
    }

    public void actionPlayPause() {
        switch (mode) {
            case AP_RUNNING:
                // Pause program
                vmWorker.pauseApplication();
                break;

            case AP_STOPPED:
                // When stopped, Play is exactly the same as Run
                reset();
                show(new DebugCallback());
                Library builder = libraries.get(builders.get(currentBuilder));
                RunHandler handler = new RunHandler(this, appSettings, compiler, preprocessor);
                handler.launchRemote(
                        builder, fileManager.getCurrentDirectory(), libraryPath); // 12/2020 testing new continue()

                break;

            case AP_PAUSED:
                // When paused, play continues from where program was halted.
                vmWorker.resumeApplication();

                break;
        }
    }

    public void actionStep() {
        vmWorker.step(1);
    }

    public void actionStepInto() {
        vmWorker.step(2);
    }

    public void actionStepOutOf() {
        vmWorker.step(3);
    }

    @Override
    public void useAppDirectory() {
        fileOpener.setParentDirectory(fileManager.getAppDirectory());
    }

    @Override
    public void useCurrentDirectory() {
        fileOpener.setParentDirectory(fileManager.getCurrentDirectory());
    }

    @Override
    public boolean isVMRunning() {
        return mode == ApMode.AP_RUNNING;
    }

    @Override
    public int getVMRow(String filename, InstructionPosition instructionPosition) {
        if (mode == ApMode.AP_RUNNING || instructionPosition == null) {
            return -1;
        }

        // Convert to corresponding position in source file
        return preprocessor.getLineNumberMap().getSourceFromMain(filename, instructionPosition.getSourceLine());
    }

    @Override
    public int isBreakpt(String filename, int line) {
        return debugger.isUserBreakPoint(filename, line) ? 1 : 0;
    }

    @Override
    public boolean toggleBreakpt(String filename, int line) {
        return vmWorker.toggleBreakpoint(filename, line);
    }

    @Override
    public String getVariableAt(String line, int x) {
        return EditorUtil.getVariableAt(line, x);
    }

    @Override
    public String evaluateVariable(String variable) {
        int requestId = vmWorker.evaluateWatch(variable, false);
        evaluateRequests.put(requestId, variable);

        // result will be handled by callback
        return "???";
    }

    @Override
    public void insertDeleteLines(String filename, int fileLineNo, int delta) {
        debugger.insertDeleteLines(filename, fileLineNo, delta);
    }

    @Override
    public void jumpToFile(String filename) {}

    @Override
    public void refreshUI() {
        presenter.refreshActions(mode);
    }

    // Compilation and execution routines
    public boolean loadProgramIntoCompiler() {
        // TODO Get editor assigned as main file
        int mainFiledIndex = 0;

        return preprocessor.preprocess(
                new EditorSourceFile(fileManager.getEditor(mainFiledIndex), fileManager.getFilename(mainFiledIndex)),
                compiler.getParser());
    }

    @Override
    public boolean compile() {
        // Compile the program, and reset the IP to the start.
        // Returns true if program compiled successfully. False if compiler error occurred.
        if (fileManager.editorCount() == 0) {
            presenter.setCompilerStatus("No files are open");
            return false;
        }

        // VM is not running
        final VMStatus vmStatus = null;
        setMode(ApMode.AP_STOPPED, vmStatus);

        // Compile
        if (!loadProgramIntoCompiler()) {
            presenter.placeCursorAtProcessed(
                    compiler.getParser().getSourceCode().size() - 1, 0);
            presenter.setCompilerStatus(preprocessor.getError());
            return false;
        }
        compiler.clearError();
        compiler.compile();

        // Inform virtual machine view that code has changed
        // TODO add VM viewer
        // VMView().RefreshVMView();

        if (compiler.hasError()) {
            presenter.placeCursorAtProcessed((int) compiler.getTokenLine(), (int) compiler.getTokenColumn());
            presenter.setCompilerStatus(compiler.getError());

            return false;
        }

        // TODO Reset Virtual machine
        // mVM.Reset();

        // TODO Reset OpenGL state
        // mGLWin.ResetGL ();
        // mTarget.reset();
        // mTarget.activate();
        // mGLWin.OpenGLDefaults ();

        // TODO Reset file directory
        // SetCurrentDir(mRunDirectory);

        return true;
    }

    // TODO IVMViewInterface
    void executeSingleOpCode() {
        //        if (mVM.InstructionCount() > 0) {
        //            //TODO Continue
        //            //DoContinue(1);
        //            /*
        //            // Invalidate source gutter so that current IP pointer moves
        //            TSourceFileFrm* frame = mSourceFrames[SourcePages.ActivePageIndex];
        //            frame.SourceMemo.InvalidateGutter();
        //            frame.SourceMemo.Invalidate();*/
        //
        //            // Debug displays need refreshing
        //            mPresenter.RefreshDebugDisplays(mMode);
        //        }
    }

    public void setMode(ApMode mode, VMStatus vmStatus) {

        // Set the mMode parameter.
        // Handles sending the appropriate notifications to the plugins,
        // updating the UI and status messages.
        if (this.mode != mode) {
            String statusMsg = "";

            // Send appropriate notifications to libraries and plugins
            if (mode == ApMode.AP_RUNNING) {
                if (this.mode == ApMode.AP_STOPPED) {
                    // if (!mDLLs.ProgramStart()) {
                    //    MessageDlg(mDLLs.Error().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
                    //    return;
                    // }
                }
                // else if (mMode == ApMode.AP_PAUSED)
                // mDLLs.ProgramResume();
                statusMsg = "Running...";
            } else if (mode == ApMode.AP_STOPPED && this.mode != ApMode.AP_STOPPED && this.mode != ApMode.AP_CLOSED) {
                if (vmStatus != null && vmStatus.isDone() && !vmStatus.hasError()) {
                    statusMsg = "Program completed";
                } else if (vmStatus != null && vmStatus.hasError()) {
                    statusMsg = vmStatus.getError();
                } else {
                    statusMsg = "Program stopped";
                }
            } else if (mode == ApMode.AP_PAUSED && this.mode == ApMode.AP_RUNNING) {
                statusMsg = "Program paused. Click play button to resume.";
                // mDLLs.ProgramPause();
            }

            // Set mode
            this.mode = mode;

            presenter.onModeChanged(mode, statusMsg);
        }
    }

    public void reset() {
        compiler.getVM().pause();
        if (vmWorker != null) {
            // TODO 1/2023 need to restart the existing app to free up the JVM port used for debugging
            vmWorker.terminateApplication();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            vmWorker.cancel(true);
            // TODO confirm there is no overlap with this thread stopping and starting a new one to avoid
            // GL errors
            try {
                vmWorker.getCompletionLatch().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        vmWorker = new VmWorker(this);

        vmWorker.setCompletionLatch(new CountDownLatch(1));

        callbackMessage.setMessage(new CallbackMessage(), null);
    }

    public void show(DebuggerTaskCallback callbacks) {
        vmWorker.setCallbacks(callbacks);
        vmWorker.execute();
    }

    //    public void hide() {
    //        mBuilder.getVMDriver().hide();
    //        vmWorker.cancel(true);
    //    }
    //
    //    public void stop() {
    //        mBuilder.getVMDriver().stop();
    //        vmWorker.cancel(true);
    //    }

    public String evaluateWatch(String watch, boolean canCallFunc) {
        int requestId = vmWorker.evaluateWatch(watch, canCallFunc);
        evaluateRequests.put(requestId, watch);

        // result will be handled by callback
        return "???";
    }

    public void evaluateVmViewVariable(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        int requestId = vmWorker.evaluateWatch(expression, false);
        vmViewEvaluateRequests.put(requestId, expression);
    }

    public void refreshCallStack() {
        vmWorker.refreshCallStack();
    }

    public void refreshDisassembly() {
        pendingDisassemblyRequests.clear();
        disassemblyPages.clear();
        queueDisassemblyPage(0, DISASSEMBLY_PAGE_SIZE);
    }

    public void refreshVariables() {
        pendingVariableRequests.clear();
        variablePagesByReference.clear();
        queueVariablesPage(com.basic4gl.debug.protocol.commands.VariablesCommand.REF_GLOBALS, 0, VARIABLES_PAGE_SIZE);
        queueVariablesPage(com.basic4gl.debug.protocol.commands.VariablesCommand.REF_REGISTERS, 0, VARIABLES_PAGE_SIZE);
        queueVariablesPage(com.basic4gl.debug.protocol.commands.VariablesCommand.REF_HEAP, 0, VARIABLES_PAGE_SIZE);
        queueVariablesPage(com.basic4gl.debug.protocol.commands.VariablesCommand.REF_STACK, 0, VARIABLES_PAGE_SIZE);
        queueVariablesPage(com.basic4gl.debug.protocol.commands.VariablesCommand.REF_TEMP, 0, VARIABLES_PAGE_SIZE);
        queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS, 0, VARIABLES_PAGE_SIZE);
    }

    private void queueVariablesPage(int reference, int start, int count) {
        int requestId = vmWorker.requestVariables(reference, start, count);
        if (requestId <= 0) {
            return;
        }
        pendingVariableRequests.put(requestId, new VariablesPageRequest(reference, start, count));
    }

    private boolean handlePagedVariablesCallback(VariablesCallback callback) {
        VariablesPageRequest pageRequest = pendingVariableRequests.remove(callback.getRequestId());
        if (pageRequest == null) {
            return false;
        }

        Variable[] page = callback.getVariables() != null ? callback.getVariables() : new Variable[0];
        List<Variable> aggregate = variablePagesByReference.computeIfAbsent(pageRequest.reference, key -> new ArrayList<>());
        aggregate.addAll(Arrays.asList(page));

        if (page.length >= pageRequest.count) {
            queueVariablesPage(pageRequest.reference, pageRequest.start + pageRequest.count, pageRequest.count);
            return true;
        }

        VariablesCallback merged = new VariablesCallback();
        merged.setRequestId(callback.getRequestId());
        merged.setVariables(aggregate.toArray(new Variable[0]));
        presenter.updateVmViewVariables(merged);
        variablePagesByReference.remove(pageRequest.reference);
        return true;
    }

    private void queueDisassemblyPage(int instructionOffset, int instructionCount) {
        int requestId = vmWorker.requestDisassembly(instructionOffset, instructionCount);
        if (requestId <= 0) {
            return;
        }
        pendingDisassemblyRequests.put(requestId, new DisassemblyPageRequest(instructionOffset, instructionCount));
    }

    private boolean handlePagedDisassemblyCallback(DisassembleCallback callback) {
        DisassemblyPageRequest pageRequest = pendingDisassemblyRequests.remove(callback.getRequestId());
        if (pageRequest == null) {
            return false;
        }

        DisassembledInstruction[] page =
                callback.getInstructions() != null ? callback.getInstructions() : new DisassembledInstruction[0];

        boolean encounteredInvalid = false;
        for (DisassembledInstruction instruction : page) {
            if (instruction == null) {
                continue;
            }
            if ("invalid".equals(instruction.presentationHint)) {
                encounteredInvalid = true;
                break;
            }
            disassemblyPages.add(instruction);
        }

        if (!encounteredInvalid && page.length >= pageRequest.instructionCount) {
            queueDisassemblyPage(pageRequest.instructionOffset + pageRequest.instructionCount, pageRequest.instructionCount);
            return true;
        }

        DisassembleCallback merged = new DisassembleCallback();
        merged.setRequestId(callback.getRequestId());
        merged.setInstructions(disassemblyPages.toArray(new DisassembledInstruction[0]));
        presenter.updateVmViewDisassembly(merged);
        return true;
    }

    private boolean handleErrorCallback(ErrorCallback callback) {
        if (callback == null) {
            return false;
        }

        int requestId = callback.getRequestId();
        String detail = "Debug request failed";
        if (callback.error != null && callback.error.format != null && !callback.error.format.trim().isEmpty()) {
            detail = callback.error.format;
        }

        String vmViewExpression = vmViewEvaluateRequests.remove(requestId);
        if (vmViewExpression != null) {
            presenter.updateVmViewVariableValue(vmViewExpression, "[ERROR] " + detail);
            presenter.updateVmViewError("variables", detail);
            return true;
        }

        String watch = evaluateRequests.remove(requestId);
        if (watch != null) {
            presenter.updateEvaluateWatch(watch, "[ERROR] " + detail);
            presenter.setCompilerStatus(detail);
            return true;
        }

        VariablesPageRequest variableRequest = pendingVariableRequests.remove(requestId);
        if (variableRequest != null) {
            variablePagesByReference.remove(variableRequest.reference);
            presenter.updateVmViewError(
                    variableScope(variableRequest.reference),
                    detail + " " + formatOneBasedRange(variableRequest.start, variableRequest.count));
            return true;
        }

        DisassemblyPageRequest disassemblyRequest = pendingDisassemblyRequests.remove(requestId);
        if (disassemblyRequest != null) {
            disassemblyPages.clear();
            presenter.updateVmViewError(
                    "code",
                    detail + " "
                            + formatOneBasedRange(
                                    disassemblyRequest.instructionOffset, disassemblyRequest.instructionCount));
            return true;
        }

        String inferredScope = inferVmScope(detail);
        if (inferredScope != null) {
            presenter.updateVmViewError(inferredScope, detail);
            return true;
        }

        presenter.setCompilerStatus(detail);
        return true;
    }

    private String variableScope(int reference) {
        switch (reference) {
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_HEAP:
                return "heap";
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_STACK:
                return "stack";
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_TEMP:
                return "temp";
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS:
                return "allocatedStrings";
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_REGISTERS:
                return "registers";
            case com.basic4gl.debug.protocol.commands.VariablesCommand.REF_GLOBALS:
            default:
                return "variables";
        }
    }

    private String inferVmScope(String detail) {
        if (detail == null) {
            return null;
        }
        String normalized = detail.toLowerCase(Locale.ROOT);
        if (normalized.contains("(stacktrace)")) {
            return "callStack";
        }
        if (normalized.contains("(disassemble)")) {
            return "code";
        }
        if (normalized.contains("(variables)")) {
            return "variables";
        }
        return null;
    }

    private String formatOneBasedRange(int start, int count) {
        int first = Math.max(1, start + 1);
        int last = Math.max(first, start + Math.max(1, count));
        return "[" + first + " - " + last + "]";
    }

    public void refreshWatchList() {
        presenter.refreshWatchList();

        if (mode == ApMode.AP_PAUSED) {
            for (String watch : watchList) {
                evaluateWatch(watch, true);
            }
        }
    }

    public List<String> getWatches() {
        return watchList;
    }

    public int getWatchListSize() {
        return watchList.size();
    }

    public String getWatchOrDefault(int index) {
        if (index > -1 && index < watchList.size()) {
            return watchList.get(index);
        } else {
            return "";
        }
    }

    public void removeWatchAt(int index) {
        if (index > -1 && index < watchList.size()) {
            watchList.remove(index);
        }
        refreshWatchList();
    }

    public void updateWatch(String watch, int index) {
        // Update/insert/delete watch
        if (watch != null) {
            watch = watch.trim();
            if (watch.isEmpty()) {
                // User entered an empty value
                if (index > -1 && index < watchList.size()) {
                    watchList.remove(index);
                }
            } else {
                if (index > -1 && index < watchList.size()) {
                    watchList.set(index, watch);
                } else {
                    watchList.add(watch);
                }
            }
        }
        refreshWatchList();
    }

    public ApMode getMode() {
        return mode;
    }

    public void setMode(ApMode mode) {
        this.mode = mode;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    // TODO Reimplement callbacks
    public class DebugCallback implements DebuggerTaskCallback {

        @Override
        public void onDebuggerConnected() {

            vmWorker.beginSessionConfiguration();

            // TODO handle setting breakpoints in onDebuggerInitialized callback

            // TODO separate breakpoints from file editor interfaces
            for (int i = 0; i < fileManager.getFileEditors().size(); i++) {
                FileEditor editor = fileManager.getFileEditors().get(i);
                vmWorker.setBreakpoints(editor.getFilePath(), editor.getBreakpoints());
            }

            vmWorker.commitSessionConfiguration();
        }

        @Override
        public void message(DebuggerCallbackMessage message) {
            if (message == null) {
                return;
            }
            boolean updated = callbackMessage.setMessage(message);
            if (!updated) {
                return;
            }

            VMStatus vmStatus = message.getVMStatus();

            InstructionPosition instructionPosition = message.getInstructionPosition();
            if (instructionPosition != null) {
                presenter.placeCursorAtProcessed(
                        instructionPosition.getSourceLine(), instructionPosition.getSourceColumn());
            }

            if (message.getStatus() == CallbackMessage.WORKING) {
                setMode(ApMode.AP_RUNNING, vmStatus);
            }
            // TODO Pause
            if (message.getStatus() == CallbackMessage.PAUSED) {
                presenter.onPause();
                refreshCallStack();
                refreshDisassembly();
                refreshVariables();
                refreshWatchList();
            }

            switch (message.getStatus()) {
                case CallbackMessage.PAUSED:
                    presenter.onPause();
                    break;
                case CallbackMessage.FAILED:
                case CallbackMessage.STOPPED:
                case CallbackMessage.SUCCESS:
                    // Program completed
                    setMode(ApMode.AP_STOPPED, vmStatus);
                    break;
                case CallbackMessage.WORKING:
                    // do nothing;
                    break;
            }

            presenter.refreshActions(mode);
            presenter.refreshDebugDisplays(mode);

            // TODO 12/2022 move ClearTempBreakPts
            // mVM.ClearTempBreakPts();

            // TODO Handle GL window
            // handleGLWindow();
        }

        @Override
        public void message(CallbackMessage message) {
            if (message == null) {
                return;
            }
            boolean updated = callbackMessage.setMessage(message, callbackMessage.getVMStatus());
            if (message.getStatus() == CallbackMessage.WORKING) {
                // ignore WORKING if no status change
                if (!updated) {
                    return;
                } else {
                    setMode(ApMode.AP_RUNNING, callbackMessage.getVMStatus());
                }
            }
            // TODO Pause
            if (message.getStatus() == CallbackMessage.PAUSED) {
                presenter.onPause();
                refreshCallStack();
                refreshDisassembly();
                refreshVariables();
                refreshWatchList();
            }

            switch (message.getStatus()) {
                case CallbackMessage.PAUSED:
                    presenter.onPause();
                    break;
                case CallbackMessage.FAILED:
                case CallbackMessage.STOPPED:
                case CallbackMessage.SUCCESS:
                    // Program completed
                    setMode(ApMode.AP_STOPPED, callbackMessage.getVMStatus());
                    break;
                case CallbackMessage.WORKING:
                    // do nothing;
                    break;
            }

            presenter.refreshActions(mode);
            presenter.refreshDebugDisplays(mode);

            // TODO 12/2022 move ClearTempBreakPts
            // mVM.ClearTempBreakPts();

            // TODO Handle GL window
            // handleGLWindow();
        }

        @Override
        public void messageObject(Object message) {
            // TODO 12/2022 improve type safety of interface/map callback DTO to domain model
            if (message instanceof StackTraceCallback) {
                StackTraceCallback callback = (StackTraceCallback) message;
                presenter.updateCallStack(callback);
                presenter.updateVmViewCallStack(callback);
            }

            if (message instanceof DisassembleCallback) {
                DisassembleCallback callback = (DisassembleCallback) message;
                if (!handlePagedDisassemblyCallback(callback)) {
                    presenter.updateVmViewDisassembly(callback);
                }
            }

            if (message instanceof VariablesCallback) {
                VariablesCallback callback = (VariablesCallback) message;
                if (!handlePagedVariablesCallback(callback)) {
                    presenter.updateVmViewVariables(callback);
                }
            }

            if (message instanceof EvaluateWatchCallback) {
                EvaluateWatchCallback callback = (EvaluateWatchCallback) message;
                int requestId = callback.getRequestId();
                String vmViewExpression = vmViewEvaluateRequests.remove(requestId);
                if (vmViewExpression != null) {
                    presenter.updateVmViewVariableValue(vmViewExpression, callback.getResult());
                    return;
                }
                String watch = evaluateRequests.get(requestId);
                presenter.updateEvaluateWatch(watch, callback.getResult());
            }

            if (message instanceof ErrorCallback) {
                handleErrorCallback((ErrorCallback) message);
            }
        }

        @Override
        public void onDebuggerDisconnected() {
            setMode(ApMode.AP_STOPPED, callbackMessage.getVMStatus());
        }

        // TODO 12/2022 migrate handleGLWindow to closing callback handling
        //        private void handleGLWindow() {
        //            if (mBuilder.getVMDriver().isClosing())                // Explicitly closed
        //                hide();                   // Hide it
        //
        //
        //            //mTarget.setClosing(false);
        //            //if (!mBuilder.getTarget().isVisible())
        //            //    mBuilder.getTarget().reset();
        //
        //            // Get focus back
        //            if (!(mBuilder.getVMDriver().isVisible() && !mBuilder.getVMDriver().isFullscreen()
        // && mVM.Done())) {  // If program ended cleanly in windowed mode, leave focus on OpenGL window
        //                mPresenter.onApplicationClosing();
        //            }
        //
        //            // Place cursor on current instruction
        //            //TODO Set as callbacks
        //            if (mVM.hasError() || mMode == ApMode.AP_PAUSED && mVM.IPValid()) {
        //                Mutable<Integer> line = new Mutable<Integer>(0), col = new
        // Mutable<Integer>(0);
        //
        //                mVM.GetIPInSourceCode(line, col);
        //                mPresenter.PlaceCursorAtProcessed(line.get(), col.get());
        //            }
        //        }
    }

    private static class VariablesPageRequest {
        private final int reference;
        private final int start;
        private final int count;

        private VariablesPageRequest(int reference, int start, int count) {
            this.reference = reference;
            this.start = start;
            this.count = count;
        }
    }

    private static class DisassemblyPageRequest {
        private final int instructionOffset;
        private final int instructionCount;

        private DisassemblyPageRequest(int instructionOffset, int instructionCount) {
            this.instructionOffset = instructionOffset;
            this.instructionCount = instructionCount;
        }
    }

    public void saveSettings() {
        String applicationStoragePath =
                System.getProperty("user.home") + System.getProperty("file.separator") + BuildInfo.APPLICATION_NAME;
        try {
            EditorSettingsFactory.save(settings, applicationStoragePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        String applicationStoragePath =
                System.getProperty("user.home") + System.getProperty("file.separator") + BuildInfo.APPLICATION_NAME;
        try {
            settings = EditorSettingsFactory.loadFrom(applicationStoragePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        presenter.setRecentItems(settings.recentFiles);
    }

    public void notifyFileOpened(File file) {
        settings.recentFiles.add(0, file);
        settings.recentFiles =
                new ArrayList<>(settings.recentFiles.stream().distinct().toList());

        saveSettings();

        presenter.setRecentItems(settings.recentFiles);
    }

    public void clearRecentFiles() {
        settings.recentFiles.clear();

        saveSettings();

        presenter.setRecentItems(settings.recentFiles);
    }

    public List<File> getRecentFiles() {
        return settings.recentFiles;
    }
}
