package com.basic4gl.runtime.plugin;

import java.util.Objects;

public class PluginVersion {
    private long major;
    private long minor;

    public PluginVersion() {

    }

    public PluginVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PluginVersion that = (PluginVersion) o;
        return major == that.major && minor == that.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    public long getMinorVersion() {
        return minor;
    }

    public void setMinorVersion(long minor) {
        this.minor = minor;
    }

    public long getMajorVersion() {
        return major;
    }

    public void setMajorVersion(long major) {
        this.major = major;
    }
}
