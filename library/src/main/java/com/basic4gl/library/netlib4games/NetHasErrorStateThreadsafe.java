package com.basic4gl.library.netlib4games;

public class NetHasErrorStateThreadsafe extends HasErrorStateThreadSafe {
	public NetHasErrorStateThreadsafe() {
		super();
	}

	/**
	 * Set network error state.
	 * Also automatically logs the error (if logging is enabled.)
	 */
	@Override
	public void setError(String text) {
		super.setError(text);
	}
}
