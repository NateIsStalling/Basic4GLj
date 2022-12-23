package com.basic4gl.library.debug.commands;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ContinueHandler {
    @Deprecated
    //"Doesn't work with remote launch"
    public void Continue() {
        //TODO fix this section
        //throw new NotImplementedException();

        // TODO  12/2022 move to editor
        rebuild();

//        // Resume running the current program

        // TODO  12/2022 move to editor
        refreshEditorRunningState();
//
        showGLWindow();
//


        // Kick the virtual machine over the next op-code before patching in the breakpoints.
        // otherwise we would never get past a breakpoint once we hit it, because we would
        // keep on hitting it immediately and returning.
        //TODO Continue
        //DoContinue(1);
        // TODO 12/2022 need to remember what this loop is doing..
        // TODO looks like for mDelayScreenSwitch support, but the `while` clause looks like it's taking over driving the VM completely
        /*do {

            if (counter > 0 && --counter == 0)
                mTarget.show(new DebugCallback());

            //TODO Continue
            //DoContinue(GB_STEPS_UNTIL_REFRESH);

            // Process windows messages (to keep application responsive)
            //Application.ProcessMessages ();
            //mGLWin.ProcessWindowsMessages();
            //TODO implement pausing
            //if (mTarget.PausePressed ())           // Check for pause key. (This allows us to pause when in full screen mode. Useful for debugging.)
            //   Pause();
        } while (       mMode == ApMode.AP_RUNNING
                &&  !mVM.hasError ()
                &&  !mVM.Done ()
                &&  !mVM.Paused ()
                &&  !mTarget.isClosing());
*/

        // Clear temp breakpoints (user ones still remain)
        // This also patches out all breakpoints.
    }

    private void showGLWindow() {
        // TODO add mDelayScreenSwitch continue param
        // Show and activate OpenGL window
//        if (mBuilder.getTarget() != null) {
//            if (!mBuilder.getVMDriver().isVisible()) {
//                reset();
//                mBuilder.getVMDriver().activate();
//                int counter = 0;
//                //if (!mDelayScreenSwitch) {
//                mFiles.setParentDirectory(mCurrentDirectory);
//                show(new MainWindow.DebugCallback());
//            } else {
//                synchronized (mMessage) {
//                    mMessage.status = CallbackMessage.WORKING;
//                    mMessage.notify();
//                }
//            }
//        }


        // TODO old commented out section
        //}
        //else {
        //counter = 2;            // Activate screen second time around main loop.
        //}
        // Run loop
    }


    // TODO 12/2022  move to editor
    private void rebuild() {
        //        //Get current build target
//        if ((mCurrentBuilder > -1 && mCurrentBuilder < mBuilders.size()) &&
//                (mBuilders.get(mCurrentBuilder) > -1 && mBuilders.get(mCurrentBuilder) < mLibraries.size()) &&
//                mLibraries.get(mBuilders.get(mCurrentBuilder)) instanceof Builder) {
//            mBuilder = (Builder) mLibraries.get(mBuilders.get(mCurrentBuilder));
//        } else {
//            mBuilder = null;
//        }
    }
    // TODO 12/2022 move to editor
    private void refreshEditorRunningState() {
        // TODO 12/2022 this should be handled by callbacks
//        // Set running state
//        SetMode(MainWindow.ApMode.AP_RUNNING);
//        if (mMode != MainWindow.ApMode.AP_RUNNING) {
//            return;
//        }
    }

}
