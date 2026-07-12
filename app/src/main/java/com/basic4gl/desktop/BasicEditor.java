package com.basic4gl.desktop;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.ErrorCallback;
import com.basic4gl.debug.protocol.callbacks.EvaluateWatchCallback;
import com.basic4gl.debug.protocol.callbacks.ReadMemoryCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.Variable;
import com.basic4gl.desktop.content.FileManager;
import com.basic4gl.desktop.debugger.*;
import com.basic4gl.desktop.editor.ApMode;
import com.basic4gl.desktop.editor.BasicTokenMaker;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.editor.IEditorPresenter;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.desktop.util.*;
import com.basic4gl.language.adapter.Basic4GLEditorPluginAdapter;
import com.basic4gl.language.core.runtime.CallbackMessage;
import com.basic4gl.language.core.runtime.InstructionPosition;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;

public class BasicEditor implements MainEditor, IApplicationHost, IFileProvider, PluginContext {

    private static final int GLOBAL_VARIABLES_PAGE_SIZE = 128;
    private static final int MEMORY_VARIABLES_PAGE_SIZE = 64;
    private static final int STRING_VARIABLES_PAGE_SIZE = 64;
    private static final int DISASSEMBLY_PAGE_SIZE = 200;
    private static final int HEAP_MEMORY_PAGE_WORDS = 4096;
    private static final int HEAP_MEMORY_PAGE_BYTES = HEAP_MEMORY_PAGE_WORDS * 4;

    private final IEditorPresenter presenter;
    private final Map<Integer, String> evaluateRequests = new HashMap<>();
    private final Map<Integer, String> vmViewEvaluateRequests = new HashMap<>();
    private final Map<Integer, VariablesPageRequest> pendingVariableRequests = new HashMap<>();
    private final Map<Integer, List<Variable>> variablePagesByReference = new HashMap<>();
    private final Map<Integer, Integer> activeVariableRootRequestByReference = new HashMap<>();
    private final Map<Integer, ReadMemoryPageRequest> pendingReadMemoryRequests = new HashMap<>();
    private final List<Byte> heapMemoryPages = new ArrayList<>();
    private final Map<Integer, String> allocatedStringsByIndex = new HashMap<>();
    private Integer activeReadMemoryRootRequestId = null;
    private String activeReadMemoryReference = null;
    private final Map<Integer, DisassemblyPageRequest> pendingDisassemblyRequests = new HashMap<>();
    private final List<DisassembledInstruction> disassemblyPages = new ArrayList<>();
    private Integer activeDisassemblyRootRequestId = null;

    // Virtual machine and compiler
    private VmWorker vmWorker; // Debugging
    private com.basic4gl.desktop.spi.FileOpener fileOpener;
    private final com.basic4gl.language.core.runtime.DebuggerCallbackMessage callbackMessage =
            new com.basic4gl.language.core.runtime.DebuggerCallbackMessage();

    private EditorSettings settings = new EditorSettings();
    private boolean settingsLoaded = false;

    // State

    // Libraries
    private final List<Builder> builders = new ArrayList<>();
    public int currentBuilder = -1; // Index of mTarget in mTargets

    // Editor state
    private ApMode mode = ApMode.AP_STOPPED;
    private RunHandler activeRunHandler;
    private volatile boolean awaitingDebuggerAttach;

    private final List<String> watchList = new ArrayList<>();

    private final FileManager fileManager;
    private final MenuService menuService;

    private String libraryPath;

    private final Basic4GLEditorPluginAdapter basic4gl;

    public BasicEditor(
            String libraryPath, FileManager fileManager, IEditorPresenter presenter, MenuService menuService) {
        this.libraryPath = libraryPath;
        this.fileManager = fileManager;
        this.presenter = presenter;
        this.menuService = menuService;
        this.basic4gl = new Basic4GLEditorPluginAdapter(this);
        this.basic4gl.setOnPluginStateChanged(this::refreshSyntaxHighlighting);
        this.basic4gl.setOnPluginDirectoryHistoryChanged(this::syncPluginDirectorySettings);
    }

    public void initLibraries() {
        fileOpener = new FileOpener(fileManager.getCurrentDirectory());

        basic4gl.onLoad(this);
        builders.clear();
        Collections.addAll(builders, basic4gl.getBuilders());
        // Set default target
        if (!builders.isEmpty()) {
            currentBuilder = 0;
        }
        refreshSyntaxHighlighting();
    }

    public void refreshSyntaxHighlighting() {
        BasicTokenMaker.reservedWords.clear();
        BasicTokenMaker.functions.clear();
        BasicTokenMaker.constants.clear();
        BasicTokenMaker.operators.clear();
        for (String s : basic4gl.getLanguage().getReservedWords()) {
            BasicTokenMaker.reservedWords.add(s);
        }

        for (String s : basic4gl.getLanguage().getConstants()) {
            BasicTokenMaker.constants.add(s);
        }

        for (String s : basic4gl.getLanguage().getFunctions()) {
            BasicTokenMaker.functions.add(s);
        }

        for (String s : basic4gl.getLanguage().getOperators()) {
            BasicTokenMaker.operators.add(s);
        }
        presenter.refreshSyntaxHighlighting();
    }

    public void actionRun() {

        //                libraryPath
        // TODO fix run
        if (mode == ApMode.AP_STOPPED && activeRunHandler == null) {
            // Compile and run program from start
            reset();
            show(new DebugCallback());
            RunHandler handler = new RunHandler(this, basic4gl.getDebug());
            com.basic4gl.desktop.spi.DebugLaunchInfo launchInfo =
                    handler.launchRemote(); // 12/2020 testing new continue()
            activeRunHandler = basic4gl.getDebug().hasLaunchedProcess() ? handler : null;
            if (activeRunHandler == null && vmWorker != null) {
                vmWorker.cancel(true);
            }
            updateWaitingForDebuggerStatus(launchInfo);

        } else {
            // Stop program completely.
            stopOrCancelRunningApplication();
        }
    }

    public void actionPlayPause() {
        switch (mode) {
            case AP_RUNNING:
                // Pause program
                vmWorker.pauseApplication();
                break;

            case AP_STOPPED:
                if (activeRunHandler != null) {
                    stopOrCancelRunningApplication();
                    break;
                }

                // When stopped, Play is exactly the same as Run
                reset();
                show(new DebugCallback());
                RunHandler handler = new RunHandler(this, basic4gl.getDebug());
                com.basic4gl.desktop.spi.DebugLaunchInfo launchInfo =
                        handler.launchRemote(); // 12/2020 testing new continue()
                activeRunHandler = basic4gl.getDebug().hasLaunchedProcess() ? handler : null;
                if (activeRunHandler == null && vmWorker != null) {
                    vmWorker.cancel(true);
                }
                updateWaitingForDebuggerStatus(launchInfo);

                break;

            case AP_WAITING:
                stopOrCancelRunningApplication();
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
        return basic4gl.getLanguage().getSourceFromMain(filename, instructionPosition.getSourceLine());
    }

    @Override
    public int isBreakpt(String filename, int line) {
        return basic4gl.getDebug().isUserBreakPoint(filename, line) ? 1 : 0;
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
        basic4gl.getDebug().insertDeleteLines(filename, fileLineNo, delta);
    }

    @Override
    public void jumpToFile(String filename) {}

    @Override
    public void refreshUI() {
        presenter.refreshActions(mode);
    }

    // Compilation and execution routines
    public boolean loadProgramIntoCompiler() {
        int mainFiledIndex = fileManager.getRunnableFileIndex();
        if (mainFiledIndex < 0) {
            return false;
        }

        return basic4gl.getPreprocessor()
                .preprocess(new EditorSourceFile(
                        fileManager.getEditor(mainFiledIndex), fileManager.getFilename(mainFiledIndex)));
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
        final com.basic4gl.language.core.runtime.VMStatus vmStatus = null;
        setMode(ApMode.AP_STOPPED, vmStatus);

        // Compile
        if (!loadProgramIntoCompiler()) {
            presenter.placeCursorAtProcessed(basic4gl.getCompiler().getParserLinePosition(), 0);
            presenter.setCompilerStatus(basic4gl.getPreprocessor().getError());
            return false;
        }
        basic4gl.getCompiler().clearError();
        basic4gl.getCompiler().compile();

        // Inform virtual machine view that code has changed
        // TODO add VM viewer
        // VMView().RefreshVMView();

        if (basic4gl.getCompiler().hasError()) {
            presenter.placeCursorAtProcessed(
                    basic4gl.getCompiler().getTokenLine().intValue(),
                    basic4gl.getCompiler().getTokenColumn().intValue());
            presenter.setCompilerStatus(basic4gl.getCompiler().getError());

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

        presenter.onCompileSucceeded();
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

    public void setMode(ApMode mode, com.basic4gl.language.core.runtime.VMStatus vmStatus) {

        if (mode != ApMode.AP_WAITING) {
            clearAttachWaitFailureWatch();
        }

        // Runtime transitioned out of pending-launch state.
        if (mode == ApMode.AP_RUNNING || (mode == ApMode.AP_STOPPED && this.mode != ApMode.AP_STOPPED)) {
            activeRunHandler = null;
        }

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
            } else if (mode == ApMode.AP_WAITING) {
                statusMsg = "Waiting for debugger to attach...";
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
        clearAttachWaitFailureWatch();
        if (activeRunHandler != null) {
            activeRunHandler.terminateLaunchedProcess();
            activeRunHandler = null;
        }
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
        pendingDisassemblyRequests.clear();
        disassemblyPages.clear();
        activeDisassemblyRootRequestId = null;
        pendingVariableRequests.clear();
        variablePagesByReference.clear();
        activeVariableRootRequestByReference.clear();
        pendingReadMemoryRequests.clear();
        heapMemoryPages.clear();
        allocatedStringsByIndex.clear();
        activeReadMemoryRootRequestId = null;
        activeReadMemoryReference = null;
    }

    public void show(com.basic4gl.language.core.runtime.DebuggerTaskCallback callbacks) {
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
        activeDisassemblyRootRequestId = null;
        int rootRequestId = queueDisassemblyPage(0, DISASSEMBLY_PAGE_SIZE, null);
        if (rootRequestId > 0) {
            activeDisassemblyRootRequestId = rootRequestId;
        }
    }

    public void refreshVariables() {
        pendingVariableRequests.clear();
        variablePagesByReference.clear();
        activeVariableRootRequestByReference.clear();
        pendingReadMemoryRequests.clear();
        heapMemoryPages.clear();
        allocatedStringsByIndex.clear();
        activeReadMemoryRootRequestId = null;
        activeReadMemoryReference = null;

        int globalsRoot = queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_GLOBALS, 0, GLOBAL_VARIABLES_PAGE_SIZE, null);
        if (globalsRoot > 0) {
            activeVariableRootRequestByReference.put(
                    com.basic4gl.debug.protocol.commands.VariablesCommand.REF_GLOBALS, globalsRoot);
        }

        int registersRoot = queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_REGISTERS,
                0,
                GLOBAL_VARIABLES_PAGE_SIZE,
                null);
        if (registersRoot > 0) {
            activeVariableRootRequestByReference.put(
                    com.basic4gl.debug.protocol.commands.VariablesCommand.REF_REGISTERS, registersRoot);
        }

        refreshHeapMemory();

        int stackRoot = queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_STACK, 0, MEMORY_VARIABLES_PAGE_SIZE, null);
        if (stackRoot > 0) {
            activeVariableRootRequestByReference.put(
                    com.basic4gl.debug.protocol.commands.VariablesCommand.REF_STACK, stackRoot);
        }

        int tempRoot = queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_TEMP, 0, MEMORY_VARIABLES_PAGE_SIZE, null);
        if (tempRoot > 0) {
            activeVariableRootRequestByReference.put(
                    com.basic4gl.debug.protocol.commands.VariablesCommand.REF_TEMP, tempRoot);
        }

        int allocatedStringsRoot = queueVariablesPage(
                com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS,
                0,
                STRING_VARIABLES_PAGE_SIZE,
                null);
        if (allocatedStringsRoot > 0) {
            activeVariableRootRequestByReference.put(
                    com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS, allocatedStringsRoot);
        }
    }

    private void refreshHeapMemory() {
        pendingReadMemoryRequests.clear();
        heapMemoryPages.clear();
        activeReadMemoryRootRequestId = null;
        activeReadMemoryReference = null;

        int heapBase = basic4gl.getDebug().getPermanent();
        String memoryReference = Integer.toString(heapBase);
        int rootRequestId = queueReadMemoryPage(memoryReference, 0, HEAP_MEMORY_PAGE_BYTES, null);
        if (rootRequestId > 0) {
            activeReadMemoryRootRequestId = rootRequestId;
            activeReadMemoryReference = memoryReference;
        }
    }

    private int queueVariablesPage(int reference, int start, int count, Integer rootRequestId) {
        int requestId = vmWorker.requestVariables(reference, start, count);
        if (requestId <= 0) {
            return 0;
        }
        int ownerRequestId = rootRequestId != null ? rootRequestId : requestId;
        pendingVariableRequests.put(requestId, new VariablesPageRequest(reference, start, count, ownerRequestId));
        return requestId;
    }

    private boolean handlePagedVariablesCallback(VariablesCallback callback) {
        VariablesPageRequest pageRequest = pendingVariableRequests.remove(callback.getRequestId());
        if (pageRequest == null) {
            if (!activeVariableRootRequestByReference.isEmpty()) {
                return true;
            }
            return false;
        }

        Integer activeRoot = activeVariableRootRequestByReference.get(pageRequest.reference);
        if (activeRoot == null || pageRequest.rootRequestId != activeRoot) {
            return true;
        }

        Variable[] page = callback.getVariables() != null ? callback.getVariables() : new Variable[0];
        List<Variable> aggregate =
                variablePagesByReference.computeIfAbsent(pageRequest.reference, key -> new ArrayList<>());
        aggregate.addAll(Arrays.asList(page));

        if (page.length >= pageRequest.count) {
            queueVariablesPage(
                    pageRequest.reference,
                    pageRequest.start + pageRequest.count,
                    pageRequest.count,
                    pageRequest.rootRequestId);
            return true;
        }

        VariablesCallback merged = new VariablesCallback();
        merged.setRequestId(callback.getRequestId());
        merged.setVariables(aggregate.toArray(new Variable[0]));

        if (pageRequest.reference == com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS) {
            updateAllocatedStringCache(aggregate);
        }

        presenter.updateVmViewVariables(merged);
        variablePagesByReference.remove(pageRequest.reference);
        activeVariableRootRequestByReference.remove(pageRequest.reference);

        // Heap string-column values depend on current string-store allocations.
        if (pageRequest.reference == com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS) {
            refreshHeapMemory();
        }
        return true;
    }

    private int queueReadMemoryPage(String memoryReference, int offsetBytes, int countBytes, Integer rootRequestId) {
        int requestId = vmWorker.requestReadMemory(memoryReference, offsetBytes, countBytes);
        if (requestId <= 0) {
            return 0;
        }
        int ownerRequestId = rootRequestId != null ? rootRequestId : requestId;
        pendingReadMemoryRequests.put(
                requestId, new ReadMemoryPageRequest(memoryReference, offsetBytes, countBytes, ownerRequestId));
        return requestId;
    }

    private boolean handlePagedReadMemoryCallback(ReadMemoryCallback callback) {
        ReadMemoryPageRequest pageRequest = pendingReadMemoryRequests.remove(callback.getRequestId());
        if (pageRequest == null) {
            if (activeReadMemoryRootRequestId != null) {
                return true;
            }
            return false;
        }

        if (activeReadMemoryRootRequestId == null
                || pageRequest.rootRequestId != activeReadMemoryRootRequestId
                || !Objects.equals(pageRequest.memoryReference, activeReadMemoryReference)) {
            return true;
        }

        byte[] pageBytes = decodeReadMemoryData(callback.getData());
        for (byte b : pageBytes) {
            heapMemoryPages.add(b);
        }

        int unreadableBytes = callback.getUnreadableBytes() != null ? Math.max(0, callback.getUnreadableBytes()) : 0;
        if (unreadableBytes == 0 && pageBytes.length >= pageRequest.countBytes) {
            queueReadMemoryPage(
                    pageRequest.memoryReference,
                    pageRequest.offsetBytes + pageRequest.countBytes,
                    pageRequest.countBytes,
                    pageRequest.rootRequestId);
            return true;
        }

        VariablesCallback mapped = new VariablesCallback();
        mapped.setRequestId(callback.getRequestId());
        mapped.setVariables(mapHeapMemoryToVariables(heapMemoryPages, pageRequest.memoryReference));
        presenter.updateVmViewVariables(mapped);

        heapMemoryPages.clear();
        activeReadMemoryRootRequestId = null;
        activeReadMemoryReference = null;
        return true;
    }

    private Variable[] mapHeapMemoryToVariables(List<Byte> bytes, String memoryReference) {
        int heapBase = parseMemoryAddress(memoryReference, basic4gl.getDebug().getPermanent());
        int rowCount = bytes.size() / 4;
        Variable[] mapped = new Variable[rowCount];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.size()).order(ByteOrder.LITTLE_ENDIAN);
        for (byte b : bytes) {
            buffer.put(b);
        }
        buffer.flip();

        for (int row = 0; row < rowCount; row++) {
            int index = heapBase + row;
            int intValue = buffer.getInt();

            Variable variable = new Variable();
            variable.name = Integer.toString(index);
            variable.value = Integer.toString(intValue);
            variable.type = Float.toString(Float.intBitsToFloat(intValue));
            variable.evaluateName = resolveAllocatedStringAtIntIndex(intValue);
            variable.variablesReference = 0;
            variable.presentationHint = new com.basic4gl.debug.protocol.types.VariablePresentationHint();
            variable.presentationHint.kind = "heap";
            mapped[row] = variable;
        }

        return mapped;
    }

    private byte[] decodeReadMemoryData(String encodedData) {
        if (encodedData == null || encodedData.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(encodedData);
        } catch (IllegalArgumentException ex) {
            return new byte[0];
        }
    }

    private int parseMemoryAddress(String memoryReference, int defaultAddress) {
        if (memoryReference == null || memoryReference.trim().isEmpty()) {
            return defaultAddress;
        }
        try {
            return Integer.parseInt(memoryReference.trim());
        } catch (NumberFormatException ex) {
            return defaultAddress;
        }
    }

    private String resolveAllocatedStringAtIntIndex(int intValue) {
        // Heap "String" column maps through the latest allocated-strings table snapshot.
        if (intValue <= 0) {
            return "";
        }
        return allocatedStringsByIndex.getOrDefault(intValue, "");
    }

    private void updateAllocatedStringCache(List<Variable> allocatedStringRows) {
        allocatedStringsByIndex.clear();
        for (Variable row : allocatedStringRows) {
            if (row == null || row.name == null) {
                continue;
            }
            try {
                int index = Integer.parseInt(row.name.trim());
                if (index <= 0) {
                    continue;
                }
                allocatedStringsByIndex.put(index, row.evaluateName != null ? row.evaluateName : "");
            } catch (NumberFormatException ignored) {
                // Ignore rows that do not carry a numeric string-store index.
            }
        }
    }

    private String formatReadMemoryRange(ReadMemoryPageRequest request) {
        int heapBase =
                parseMemoryAddress(request.memoryReference, basic4gl.getDebug().getPermanent());
        int startAddress = heapBase + Math.max(0, request.offsetBytes / 4);
        int wordCount = Math.max(1, request.countBytes / 4);
        int endAddress = startAddress + wordCount - 1;
        return "[" + startAddress + " - " + endAddress + "]";
    }

    private int queueDisassemblyPage(int instructionOffset, int instructionCount, Integer rootRequestId) {
        int requestId = vmWorker.requestDisassembly(instructionOffset, instructionCount);
        if (requestId <= 0) {
            return 0;
        }
        int ownerRequestId = rootRequestId != null ? rootRequestId : requestId;
        pendingDisassemblyRequests.put(
                requestId, new DisassemblyPageRequest(instructionOffset, instructionCount, ownerRequestId));
        return requestId;
    }

    private boolean handlePagedDisassemblyCallback(DisassembleCallback callback) {
        DisassemblyPageRequest pageRequest = pendingDisassemblyRequests.remove(callback.getRequestId());
        if (pageRequest == null) {
            if (activeDisassemblyRootRequestId != null) {
                return true;
            }
            return false;
        }

        if (activeDisassemblyRootRequestId == null || pageRequest.rootRequestId != activeDisassemblyRootRequestId) {
            return true;
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
            queueDisassemblyPage(
                    pageRequest.instructionOffset + pageRequest.instructionCount,
                    pageRequest.instructionCount,
                    pageRequest.rootRequestId);
            return true;
        }

        DisassembleCallback merged = new DisassembleCallback();
        merged.setRequestId(callback.getRequestId());
        merged.setInstructions(disassemblyPages.toArray(new DisassembledInstruction[0]));
        activeDisassemblyRootRequestId = null;
        presenter.updateVmViewDisassembly(merged);
        return true;
    }

    private boolean handleErrorCallback(ErrorCallback callback) {
        if (callback == null) {
            return false;
        }

        int requestId = callback.getRequestId();
        String detail = "Debug request failed";
        if (callback.error != null
                && callback.error.format != null
                && !callback.error.format.trim().isEmpty()) {
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
            Integer activeRoot = activeVariableRootRequestByReference.get(variableRequest.reference);
            if (activeRoot == null || variableRequest.rootRequestId != activeRoot) {
                return true;
            }
            variablePagesByReference.remove(variableRequest.reference);
            activeVariableRootRequestByReference.remove(variableRequest.reference);
            String scope = variableScope(variableRequest.reference);
            presenter.updateVmViewError(
                    scope,
                    detail + " "
                            + formatRangeForScope(
                                    scope, variableRequest.reference, variableRequest.start, variableRequest.count));
            return true;
        }

        DisassemblyPageRequest disassemblyRequest = pendingDisassemblyRequests.remove(requestId);
        if (disassemblyRequest != null) {
            disassemblyPages.clear();
            if (activeDisassemblyRootRequestId != null
                    && disassemblyRequest.rootRequestId == activeDisassemblyRootRequestId) {
                activeDisassemblyRootRequestId = null;
            }
            presenter.updateVmViewError(
                    "code",
                    detail + " "
                            + formatCodeRange(
                                    disassemblyRequest.instructionOffset, disassemblyRequest.instructionCount));
            return true;
        }

        ReadMemoryPageRequest readMemoryRequest = pendingReadMemoryRequests.remove(requestId);
        if (readMemoryRequest != null) {
            if (activeReadMemoryRootRequestId != null
                    && readMemoryRequest.rootRequestId == activeReadMemoryRootRequestId) {
                heapMemoryPages.clear();
                activeReadMemoryRootRequestId = null;
                activeReadMemoryReference = null;
                presenter.updateVmViewError("heap", detail + " " + formatReadMemoryRange(readMemoryRequest));
            }
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

    private String formatRangeForScope(String scope, int reference, int start, int count) {
        int safeStart = Math.max(0, start);
        int safeCount = Math.max(1, count);

        if ("temp".equals(scope)) {
            int first = 1 + safeStart;
            int last = first + safeCount - 1;
            return "[" + first + " - " + last + "]";
        }

        if ("stack".equals(scope)) {
            int stackEnd = basic4gl.getDebug().getPermanent();
            int first = stackEnd - 1 - safeStart;
            int last = first - safeCount + 1;
            return "[" + first + " - " + last + "]";
        }

        if (reference == com.basic4gl.debug.protocol.commands.VariablesCommand.REF_ALLOCATED_STRINGS) {
            int first = safeStart + 1;
            int last = first + safeCount - 1;
            return "[" + first + " - " + last + "]";
        }

        int first = Math.max(1, safeStart + 1);
        int last = first + safeCount - 1;
        return "[" + first + " - " + last + "]";
    }

    private String formatCodeRange(int instructionOffset, int instructionCount) {
        int first = Math.max(0, instructionOffset);
        int last = first + Math.max(1, instructionCount) - 1;
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

    private void updateWaitingForDebuggerStatus(com.basic4gl.desktop.spi.DebugLaunchInfo launchInfo) {
        boolean isSuspendedAttachLaunch = launchInfo != null
                && launchInfo.isSuspendedUntilDebuggerAttach()
                && launchInfo.getJvmDebugPort() != null;
        if (!isSuspendedAttachLaunch) {
            return;
        }

        RunHandler handler = activeRunHandler;
        if (handler != null) {
            awaitingDebuggerAttach = true;
            handler.setProcessExitListener((sender, exitCode, stderrOutput) ->
                    SwingUtilities.invokeLater(() -> onRunProcessExit(sender, exitCode, stderrOutput)));
        }

        setMode(ApMode.AP_WAITING, null);
        presenter.setCompilerStatus(
                "Waiting for JVM debugger to attach on port " + launchInfo.getJvmDebugPort() + "...");
    }

    private void onRunProcessExit(Object sender, Integer exitCode, String stderr) {
        if (!awaitingDebuggerAttach || mode != ApMode.AP_WAITING || activeRunHandler != sender) {
            return;
        }

        if (stderr != null && !stderr.trim().isEmpty()) {
            System.err.println("Process exited before debugger attach:\n" + stderr);
        }

        if (vmWorker != null) {
            vmWorker.cancel(true);
        }

        clearAttachWaitFailureWatch();
        setMode(ApMode.AP_STOPPED, null);
        presenter.setCompilerStatus(buildAttachWaitFailureMessage(exitCode, stderr));
        presenter.refreshActions(mode);
        presenter.refreshDebugDisplays(mode);
    }

    private void clearAttachWaitFailureWatch() {
        awaitingDebuggerAttach = false;
        if (activeRunHandler != null) {
            activeRunHandler.setProcessExitListener(null);
        }
    }

    private String buildAttachWaitFailureMessage(Integer exitCode, String stderr) {
        String normalizedStderr = stderr != null ? stderr.toLowerCase(Locale.ROOT) : "";

        if (normalizedStderr.contains("jdwp transport dt_socket failed to initialize")
                || normalizedStderr.contains("transport_init")) {
            return "Debug startup failed: JDWP transport could not initialize. Try a different debug port or clear the port override.";
        }

        if (exitCode != null) {
            return "Program exited before debugger attached (exit code " + exitCode + ").";
        }

        return "Program exited before debugger attached.";
    }

    public void stopOrCancelRunningApplication() {
        if (vmWorker != null) {
            vmWorker.stopApplication();
        }

        // AP_STOPPED with an active handler means launch is pending (for example suspend=y before debugger attach).
        if (mode == ApMode.AP_WAITING || (mode == ApMode.AP_STOPPED && activeRunHandler != null)) {
            clearAttachWaitFailureWatch();
            if (activeRunHandler != null) {
                activeRunHandler.terminateLaunchedProcess();
                activeRunHandler = null;
            }
            if (vmWorker != null) {
                vmWorker.cancel(true);
            }
        }

        setMode(ApMode.AP_STOPPED, null);
        presenter.refreshActions(mode);
        presenter.refreshDebugDisplays(mode);
    }

    @Override
    public DialogService dialogs() {

        // TODO implement dialogs for plugins
        return null;
    }

    @Override
    public MenuService menus() {
        return menuService;
    }

    @Override
    public FileOpener files() {
        return fileOpener;
    }

    @Override
    public Builder currentBuilder() {
        if (builders.isEmpty()) {
            throw new IllegalStateException("No builders are available.");
        }
        if (currentBuilder < 0 || currentBuilder >= builders.size()) {
            currentBuilder = 0;
        }
        Builder builder = builders.get(currentBuilder);
        builder.init(fileOpener);
        return builder;
    }

    @Override
    public String currentDirectory() {
        return fileManager.getCurrentDirectory();
    }

    @Override
    public String getLibraryPath() {
        return libraryPath;
    }

    @Override
    public SourceFileService[] fileServices() {
        return new SourceFileService[] {new EditorSourceFileServer(fileManager), new DiskFileServer()};
    }

    @Override
    public boolean isMacOS() {
        return SystemUtils.IS_OS_MAC;
    }

    @Override
    public String getDefaultDebuggerPort() {
        return DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT;
    }

    public void onCloseAll() {
        basic4gl.onCloseAll();
    }

    public void onCurrentDirectoryChanged(String directory) {
        basic4gl.onCurrentDirectoryChanged(directory);
    }

    public PreprocessorService getPreprocessor() {
        return basic4gl.getPreprocessor();
    }

    public CompilerService getCompiler() {
        return basic4gl.getCompiler();
    }

    public LanguageService getLanguageService() {
        return basic4gl.getLanguage();
    }

    public List<Builder> getBuilders() {
        return builders;
    }

    // TODO temporary shim - migrating towards settings being plugin oriented
    public Basic4GLEditorPluginAdapter getBasic4gl() {
        return basic4gl;
    }

    // TODO Reimplement callbacks
    public class DebugCallback implements com.basic4gl.language.core.runtime.DebuggerTaskCallback {

        @Override
        public void onDebuggerConnected() {
            clearAttachWaitFailureWatch();
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
        public void message(com.basic4gl.language.core.runtime.DebuggerCallbackMessage message) {
            if (message == null) {
                return;
            }
            boolean updated = callbackMessage.setMessage(message);
            if (!updated) {
                return;
            }

            com.basic4gl.language.core.runtime.VMStatus vmStatus = message.getVMStatus();

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

            if (message instanceof ReadMemoryCallback) {
                ReadMemoryCallback callback = (ReadMemoryCallback) message;
                handlePagedReadMemoryCallback(callback);
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
            activeRunHandler = null;
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
        private final int rootRequestId;

        private VariablesPageRequest(int reference, int start, int count, int rootRequestId) {
            this.reference = reference;
            this.start = start;
            this.count = count;
            this.rootRequestId = rootRequestId;
        }
    }

    private static class DisassemblyPageRequest {
        private final int instructionOffset;
        private final int instructionCount;
        private final int rootRequestId;

        private DisassemblyPageRequest(int instructionOffset, int instructionCount, int rootRequestId) {
            this.instructionOffset = instructionOffset;
            this.instructionCount = instructionCount;
            this.rootRequestId = rootRequestId;
        }
    }

    private static class ReadMemoryPageRequest {
        private final String memoryReference;
        private final int offsetBytes;
        private final int countBytes;
        private final int rootRequestId;

        private ReadMemoryPageRequest(String memoryReference, int offsetBytes, int countBytes, int rootRequestId) {
            this.memoryReference = memoryReference;
            this.offsetBytes = offsetBytes;
            this.countBytes = countBytes;
            this.rootRequestId = rootRequestId;
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

        basic4gl.restorePluginDirectoryState(
                settings.currentPluginDirectory,
                settings.recentPluginDirectories.stream()
                        .map(File::getAbsolutePath)
                        .toList());
        presenter.setRecentItems(settings.recentFiles);
        settingsLoaded = true;
        syncPluginDirectorySettings();
    }

    public void onFileOpened(com.basic4gl.desktop.editor.FileEditor editor) {
        if (editor == null || editor.getEditorPane() == null) {
            return;
        }
        basic4gl.onFileOpened(editor.getEditorPane().getText());
        refreshSyntaxHighlighting();
    }

    public void onFileSaving(com.basic4gl.desktop.editor.FileEditor editor) {
        if (editor == null || editor.getEditorPane() == null) {
            return;
        }
        String source = editor.getEditorPane().getText();
        String withPluginDirectives = basic4gl.appendEnabledPluginDirectives(source);
        if (!withPluginDirectives.equals(source)) {
            editor.getEditorPane().setText(withPluginDirectives);
        }
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

    private void syncPluginDirectorySettings() {
        settings.currentPluginDirectory = basic4gl.getActivePluginDirectory();
        settings.recentPluginDirectories = new ArrayList<>(
                basic4gl.getRecentPluginDirectories().stream().map(File::new).toList());
        if (!settingsLoaded) {
            return;
        }
        saveSettings();
    }
}
