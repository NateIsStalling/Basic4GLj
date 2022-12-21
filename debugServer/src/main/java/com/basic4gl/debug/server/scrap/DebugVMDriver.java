package com.basic4gl.debug.server.scrap;

import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.lib.util.CallbackMessage;
import com.basic4gl.lib.util.DebuggerCallbacks;
import com.basic4gl.lib.util.Library;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DebugVMDriver implements IVMDriver {

    @Override
    public void activate() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void start(DebuggerCallbacks debugger) {

    }

    @Override
    public void hide() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean handleEvents() {
        return false;
    }

    @Override
    public CallbackMessage driveVM(int steps) {
        return null;
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void onFinally() {

    }

    @Override
    public void initLibrary(Library lib) {

    }

    public boolean StreamIn(DataInputStream stream) throws IOException {

        return true;
    }

    public void StreamOut(DataOutputStream stream) throws IOException {

    }
}
