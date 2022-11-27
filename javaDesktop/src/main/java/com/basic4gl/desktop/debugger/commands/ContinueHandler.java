package com.basic4gl.desktop.debugger.commands;

import com.basic4gl.desktop.MainWindow;
import com.basic4gl.lib.util.Builder;
import com.basic4gl.lib.util.CallbackMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ContinueHandler {
    @Deprecated
    //"Doesn't work with remote launch"
    public void Continue() {
        throw new NotImplementedException();
        //TODO fix this section
//        //Get current build target
//        if ((mCurrentBuilder > -1 && mCurrentBuilder < mBuilders.size()) &&
//                (mBuilders.get(mCurrentBuilder) > -1 && mBuilders.get(mCurrentBuilder) < mLibraries.size()) &&
//                mLibraries.get(mBuilders.get(mCurrentBuilder)) instanceof Builder) {
//            mBuilder = (Builder) mLibraries.get(mBuilders.get(mCurrentBuilder));
//        } else {
//            mBuilder = null;
//        }
//        // Resume running the current program
//
//        // Set running state
//        SetMode(MainWindow.ApMode.AP_RUNNING);
//        if (mMode != MainWindow.ApMode.AP_RUNNING) {
//            return;
//        }
//
//        // Show and activate OpenGL window
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

        // Kick the virtual machine over the next op-code before patching in the breakpoints.
        // otherwise we would never get past a breakpoint once we hit it, because we would
        // keep on hitting it immediately and returning.
        //TODO Continue
        //DoContinue(1);
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
}
