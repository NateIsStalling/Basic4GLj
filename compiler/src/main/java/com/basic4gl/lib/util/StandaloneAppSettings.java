package com.basic4gl.lib.util;

public class StandaloneAppSettings implements IAppSettings {
// Standalone applications are NOT sandboxed.
private static final boolean DEFAULT_SANDBOX_MODE = false;
// TODO this needs documentation; 1 is default in original source
private static final int DEFAULT_SYNTAX = 1;

@Override
public boolean isSandboxModeEnabled() {

	return DEFAULT_SANDBOX_MODE;
}

@Override
public int getSyntax() {
	return DEFAULT_SYNTAX;
}
}
