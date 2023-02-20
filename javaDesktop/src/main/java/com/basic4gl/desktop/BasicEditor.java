package com.basic4gl.desktop;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.EvaluateWatchCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
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
import com.basic4gl.runtime.InstructionPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class BasicEditor implements MainEditor,
        IApplicationHost,
        IFileProvider {

    private IEditorPresenter mPresenter;
    private Map<Integer, String> mEvaluateRequests = new HashMap<>();

    // Virtual machine and compiler
    private VmWorker mWorker;       // Debugging
    public TomBasicCompiler mComp; // Compiler
    private FileOpener mFiles;
    private final DebuggerCallbackMessage mMessage = new DebuggerCallbackMessage();


    // Preprocessor
    public Preprocessor mPreprocessor;

    // Debugger
    public Debugger mDebugger;

    // State
    TomBasicCompiler.LanguageSyntax mLanguageSyntax = TomBasicCompiler.LanguageSyntax.LS_BASIC4GL;



    //Libraries
    public List<Library> mLibraries = new ArrayList<Library>();
    private List<Integer> mBuilders = new ArrayList<Integer>();   //Indexes of libraries that can be launch targets
    public int mCurrentBuilder = -1;                  //Index of mTarget in mTargets
    private Builder mBuilder;                          //Build target for user's code


    // Editor state
    ApMode mMode = ApMode.AP_STOPPED;

    List<String> mWatches = new ArrayList<String>();

    FileManager mFileManager;

    String mLibraryPath;


    public BasicEditor(
            String libraryPath,
            FileManager fileManager,
            IEditorPresenter presenter,
            Preprocessor preprocessor,
            Debugger debugger,
            TomBasicCompiler comp) {
        mLibraryPath = libraryPath;
        mFileManager = fileManager;
        mPresenter = presenter;
        mPreprocessor = preprocessor;
        mDebugger = debugger;
        mComp = comp;
    }


    public void InitLibraries() {
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

        //TODO Load libraries dynamically
        mLibraries.add(new com.basic4gl.library.standard.Standard());
        mLibraries.add(new com.basic4gl.library.standard.TrigBasicLib());
        mLibraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
        mLibraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
        mLibraries.add(new com.basic4gl.library.desktopgl.TomCompilerBasicLib());
//        mLibraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());
        mLibraries.add(GLTextGridWindow.getInstance(mComp));
        mLibraries.add(BuilderDesktopGL.getInstance(mComp));

        mFiles = new FileOpener(mFileManager.getCurrentDirectory());
        //TODO Add more libraries
        int i = 0;
        for (Library lib : mLibraries) {
            lib.init(mComp); //Allow libraries to register function overloads
            if (lib instanceof IFileAccess) {
                //Allows libraries to read from directories
                ((IFileAccess) lib).init(mFiles);
            }
            if (lib instanceof FunctionLibrary) {
                mComp.AddConstants(((FunctionLibrary) lib).constants());
                mComp.AddFunctions(lib, ((FunctionLibrary) lib).specs());
            }
            if (lib instanceof Builder) {
                mBuilders.add(i);
            }
            i++;
        }
        //Set default target
        if (mBuilders.size() > 0) {
            mCurrentBuilder = 0;
        }

        //Initialize highlighting
        //mKeywords = new HashMap<String,Color>();
        BasicTokenMaker.mReservedWords.clear();
        BasicTokenMaker.mFunctions.clear();
        BasicTokenMaker.mConstants.clear();
        BasicTokenMaker.mOperators.clear();
        for (String s : mComp.m_reservedWords) {
            BasicTokenMaker.mReservedWords.add(s);
        }

        for (String s : mComp.getConstants().keySet()) {
            BasicTokenMaker.mConstants.add(s);
        }

        for (String s : mComp.m_functionIndex.keySet()) {
            BasicTokenMaker.mFunctions.add(s);
        }

        for (String s : mComp.getBinaryOperators()) {
            BasicTokenMaker.mOperators.add(s);
        }
        for (String s : mComp.getUnaryOperators()) {
            BasicTokenMaker.mOperators.add(s);
        }

    }

    public void actionRun() {

//                libraryPath
        //TODO fix run

        if (mMode == ApMode.AP_STOPPED) {
            // Compile and run program from start
            reset();
            show(new DebugCallback());
            Library builder = mLibraries.get(mBuilders.get(mCurrentBuilder));
            RunHandler handler = new RunHandler(this, mComp, mPreprocessor);
            handler.launchRemote(builder, mFileManager.getCurrentDirectory(), mLibraryPath); //12/2020 testing new continue()

        } else {
            // Stop program completely.
            mWorker.stopApplication();
        }
    }

    public void actionPlayPause() {
        switch (mMode) {
            case AP_RUNNING:
                // Pause program
                mWorker.pauseApplication();
                break;

            case AP_STOPPED:
                // When stopped, Play is exactly the same as Run
                reset();
                show(new DebugCallback());
                Library builder = mLibraries.get(mBuilders.get(mCurrentBuilder));
                RunHandler handler = new RunHandler(this, mComp, mPreprocessor);
                handler.launchRemote(builder, mFileManager.getCurrentDirectory(), mLibraryPath); //12/2020 testing new continue()

                break;

            case AP_PAUSED:
                // When paused, play continues from where program was halted.
                mWorker.resumeApplication();

                break;
        }
    }

    public void actionStep() {
        mWorker.step(1);
    }

    public void actionStepInto() {
        mWorker.step(2);
    }

    public void actionStepOutOf() {
        mWorker.step(3);
    }

    @Override
    public void useAppDirectory() {
        mFiles.setParentDirectory(mFileManager.getAppDirectory());
    }

    @Override
    public void useCurrentDirectory() {
        mFiles.setParentDirectory(mFileManager.getCurrentDirectory());
    }


    @Override
    public boolean isVMRunning() {
        return mMode == ApMode.AP_RUNNING;
    }

    @Override
    public int getVMRow(String filename, InstructionPos instructionPos) {
        if (mMode == ApMode.AP_RUNNING || instructionPos == null) {
            return -1;
        }

        // Convert to corresponding position in source file
        return mPreprocessor.getLineNumberMap().getSourceFromMain(filename, instructionPos.getSourceLine());

    }

    @Override
    public int isBreakpt(String filename, int line) {
        return mDebugger.IsUserBreakPt(filename, line)
                ? 1
                : 0;
    }

    @Override
    public boolean toggleBreakpt(String filename, int line) {
        return mWorker.toggleBreakpoint(filename, line);
    }

    @Override
    public String getVariableAt(String line, int x) {
        return EditorUtil.getVariableAt(line, x);
    }

    @Override
    public String evaluateVariable(String variable) {
        int requestId = mWorker.evaluateWatch(variable, false);
        mEvaluateRequests.put(requestId, variable);

        // result will be handled by callback
        return "???";
    }

    @Override
    public void insertDeleteLines(String filename, int fileLineNo, int delta) {
        mDebugger.InsertDeleteLines(filename, fileLineNo, delta);
    }

    @Override
    public void jumpToFile(String filename) {

    }

    @Override
    public void refreshUI() {
        mPresenter.RefreshActions(mMode);
    }


    // Compilation and execution routines
    public boolean LoadProgramIntoCompiler() {
        //TODO Get editor assigned as main file
        int mainFiledIndex = 0;

        return mPreprocessor.preprocess(
                new EditorSourceFile(mFileManager.getEditor(mainFiledIndex), mFileManager.getFilename(mainFiledIndex)),
                mComp.Parser());
    }

    @Override
    public boolean Compile() {
        // Compile the program, and reset the IP to the start.
        // Returns true if program compiled successfully. False if compiler error occurred.
        if (mFileManager.editorCount() == 0) {
            mPresenter.setCompilerStatus("No files are open");
            return false;
        }

        // VM is not running
        final VMStatus vmStatus = null;
        SetMode(ApMode.AP_STOPPED, vmStatus);

        // Compile
        if (!LoadProgramIntoCompiler()) {
            mPresenter.PlaceCursorAtProcessed(mComp.Parser().SourceCode().size() - 1, 0);
            mPresenter.setCompilerStatus(mPreprocessor.getError());
            return false;
        }
        mComp.clearError();
        mComp.compile();

        // Inform virtual machine view that code has changed
        //TODO add VM viewer
        //VMView().RefreshVMView();

        if (mComp.hasError()) {
            mPresenter.PlaceCursorAtProcessed((int) mComp.getTokenLine(), (int) mComp.getTokenColumn());
            mPresenter.setCompilerStatus(mComp.getError());

            return false;
        }

        // TODO Reset Virtual machine
        //mVM.Reset();

        // TODO Reset OpenGL state
        //mGLWin.ResetGL ();
        //mTarget.reset();
        //mTarget.activate();
        //mGLWin.OpenGLDefaults ();

        //TODO Reset file directory
        //SetCurrentDir(mRunDirectory);

        return true;
    }


    // TODO IVMViewInterface
    void ExecuteSingleOpCode() {
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

    public void SetMode(ApMode mode, VMStatus vmStatus) {

        // Set the mMode parameter.
        // Handles sending the appropriate notifications to the plugin DLLs,
        // updating the UI and status messages.
        if (mMode != mode) {
            String statusMsg = "";

            // Send appropriate notifications to libraries and plugin DLLs
            if (mode == ApMode.AP_RUNNING) {
                if (mMode == ApMode.AP_STOPPED) {
                    //if (!mDLLs.ProgramStart()) {
                    //    MessageDlg(mDLLs.Error().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
                    //    return;
                    //}
                }
                //else if (mMode == ApMode.AP_PAUSED)
                //mDLLs.ProgramResume();
                statusMsg = "Running...";
            } else if (mode == ApMode.AP_STOPPED
                    && mMode != ApMode.AP_STOPPED
                    && mMode != ApMode.AP_CLOSED) {
                if (vmStatus != null && vmStatus.isDone() && !vmStatus.hasError()) {
                    statusMsg = "Program completed";
                } else if (vmStatus != null && vmStatus.hasError()) {
                    statusMsg = vmStatus.getError();
                } else {
                    statusMsg = "Program stopped";
                }
            } else if (mode == ApMode.AP_PAUSED && mMode == ApMode.AP_RUNNING) {
                statusMsg = "Program paused. Click play button to resume.";
                //mDLLs.ProgramPause();
            }

            // Set mode
            mMode = mode;

            mPresenter.onModeChanged(mode, statusMsg);
        }
    }

    public void reset() {
        mComp.VM().pause();
        if (mWorker != null) {
            // TODO 1/2023 need to restart the existing app to free up the JVM port used for debugging
            mWorker.terminateApplication();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mWorker.cancel(true);
            //TODO confirm there is no overlap with this thread stopping and starting a new one to avoid GL errors
            try {
                mWorker.getCompletionLatch().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mWorker = new VmWorker(this);

        mWorker.setCompletionLatch(new CountDownLatch(1));

        mMessage.setMessage(new CallbackMessage(), null);
    }

    public void show(DebuggerTaskCallback callbacks) {
        mWorker.setCallbacks(callbacks);
        mWorker.execute();
    }

    public void hide() {
        mBuilder.getVMDriver().hide();
        mWorker.cancel(true);
    }

    public void stop() {
        mBuilder.getVMDriver().stop();
        mWorker.cancel(true);
    }

    public String evaluateWatch(String watch, boolean canCallFunc) {
        int requestId = mWorker.evaluateWatch(watch, canCallFunc);
        mEvaluateRequests.put(requestId, watch);

        // result will be handled by callback
        return "???";
    }

    public void refreshCallStack() {
        mWorker.refreshCallStack();
    }

    public void refreshWatchList() {
        mPresenter.refreshWatchList();

        if (mMode == ApMode.AP_PAUSED) {
            for (String watch : mWatches) {
                evaluateWatch(watch, true);
            }
        }
    }

    public List<String> getWatches() {
        return mWatches;
    }

    public int getWatchListSize() {
        return mWatches.size();
    }

    public String getWatchOrDefault(int index) {
        if (index > -1 && index < mWatches.size()) {
            return mWatches.get(index);
        } else {
            return "";
        }
    }

    public void removeWatchAt(int index) {
        if (index > -1 && index < mWatches.size()) {
            mWatches.remove(index);
        }
        refreshWatchList();
    }

    public void updateWatch(String watch, int index) {
        // Update/insert/delete watch
        if (watch != null) {
            watch = watch.trim();
            if (watch.equals("")) {
                //User entered an empty value
                if (index > -1 && index < mWatches.size()) {
                    mWatches.remove(index);
                }
            } else {
                if (index > -1 && index < mWatches.size()) {
                    mWatches.set(index, watch);
                } else {
                    mWatches.add(watch);
                }
            }
        }
        refreshWatchList();
    }

    //TODO Reimplement callbacks
    public class DebugCallback implements DebuggerTaskCallback {

        @Override
        public void onDebuggerConnected() {

            mWorker.beginSessionConfiguration();

            // TODO handle setting breakpoints in onDebuggerInitialized callback

            // TODO separate breakpoints from file editor interfaces
            for (int i = 0; i < mFileManager.mFileEditors.size(); i++) {
                FileEditor editor = mFileManager.mFileEditors.get(i);
                mWorker.setBreakpoints(
                        editor.getFilePath(),
                        editor.getBreakpoints());
            }

            mWorker.commitSessionConfiguration();
        }

        @Override
        public void message(DebuggerCallbackMessage message) {
            if (message == null) {
                return;
            }
            boolean updated = mMessage.setMessage(message);
            if (!updated) {
                return;
            }

            VMStatus vmStatus = message.getVMStatus();

            InstructionPos instructionPos = message.getInstructionPosition();
            if (instructionPos != null) {
                mPresenter.PlaceCursorAtProcessed(instructionPos.getSourceLine(), instructionPos.getSourceColumn());
            }

            if (message.getStatus() == CallbackMessage.WORKING) {
                SetMode(ApMode.AP_RUNNING, vmStatus);
            }
            //TODO Pause
            if (message.getStatus() == CallbackMessage.PAUSED) {
                mPresenter.onPause();
                refreshCallStack();
                refreshWatchList();
            }

            switch (message.getStatus()) {
                case CallbackMessage.PAUSED:
                    mPresenter.onPause();
                    break;
                case CallbackMessage.FAILED:
                case CallbackMessage.STOPPED:
                case CallbackMessage.SUCCESS:
                    //Program completed
                    SetMode(ApMode.AP_STOPPED, vmStatus);
                    break;
                case CallbackMessage.WORKING:
                    // do nothing;
                    break;
            }

            mPresenter.RefreshActions(mMode);
            mPresenter.RefreshDebugDisplays(mMode);

            // TODO 12/2022 move ClearTempBreakPts
            //mVM.ClearTempBreakPts();

            //TODO Handle GL window
            //handleGLWindow();
        }

        @Override
        public void message(CallbackMessage message) {
            if (message == null) {
                return;
            }
            boolean updated = mMessage.setMessage(message, mMessage.getVMStatus());
            if (message.getStatus() == CallbackMessage.WORKING) {
                // ignore WORKING if no status change
                if (!updated) {
                    return;
                } else {
                    SetMode(ApMode.AP_RUNNING, mMessage.getVMStatus());
                }
            }
            //TODO Pause
            if (message.getStatus() == CallbackMessage.PAUSED) {
                mPresenter.onPause();
            }

            switch (message.getStatus()) {
                case CallbackMessage.PAUSED:
                    mPresenter.onPause();
                    break;
                case CallbackMessage.FAILED:
                case CallbackMessage.STOPPED:
                case CallbackMessage.SUCCESS:
                    //Program completed
                    SetMode(ApMode.AP_STOPPED, mMessage.getVMStatus());
                    break;
                case CallbackMessage.WORKING:
                    // do nothing;
                    break;
            }

            mPresenter.RefreshActions(mMode);
            mPresenter.RefreshDebugDisplays(mMode);

            // TODO 12/2022 move ClearTempBreakPts
            //mVM.ClearTempBreakPts();

            //TODO Handle GL window
            //handleGLWindow();
        }

        @Override
        public void messageObject(Object message) {
            // TODO 12/2022 improve type safety of interface/map callback DTO to domain model
            if (message instanceof StackTraceCallback) {
                mPresenter.updateCallStack((StackTraceCallback) message);
            }

            if (message instanceof EvaluateWatchCallback) {
                EvaluateWatchCallback callback = (EvaluateWatchCallback) message;
                int requestId = callback.getRequestId();
                String watch = mEvaluateRequests.get(requestId);
                mPresenter.updateEvaluateWatch(watch, callback.getResult());
            }
        }

        @Override
        public void onDebuggerDisconnected() {
            SetMode(ApMode.AP_STOPPED, mMessage.getVMStatus());
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
//            if (!(mBuilder.getVMDriver().isVisible() && !mBuilder.getVMDriver().isFullscreen() && mVM.Done())) {  // If program ended cleanly in windowed mode, leave focus on OpenGL window
//                mPresenter.onApplicationClosing();
//            }
//
//            // Place cursor on current instruction
//            //TODO Set as callbacks
//            if (mVM.hasError() || mMode == ApMode.AP_PAUSED && mVM.IPValid()) {
//                Mutable<Integer> line = new Mutable<Integer>(0), col = new Mutable<Integer>(0);
//
//                mVM.GetIPInSourceCode(line, col);
//                mPresenter.PlaceCursorAtProcessed(line.get(), col.get());
//            }
//        }
    }
}
