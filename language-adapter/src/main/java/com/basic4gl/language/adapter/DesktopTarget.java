package com.basic4gl.language.adapter;

import com.basic4gl.desktop.spi.Configuration;
import com.basic4gl.desktop.spi.Target;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DesktopTarget implements Target {
    @Override
    public String name() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public Configuration getSettings() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public void setConfiguration(Configuration config) {

    }

    @Override
    public void loadConfiguration(InputStream stream) throws Exception {

    }

    @Override
    public void saveConfiguration(OutputStream stream) throws Exception {

    }

    @Override
    public void saveState(OutputStream stream) throws Exception {

    }

    @Override
    public void loadState(InputStream stream) throws Exception {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public List<String> getDependencies() {
        return List.of();
    }

    @Override
    public List<String> getClassPathObjects() {
        return null;
    }

    public void reset() {

    }

    public void init(FileOpenerAdapter fileOpenerAdapter) {
    }
}
