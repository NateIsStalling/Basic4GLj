package com.basic4gl.library.netlib4games;

public abstract class AbstractHasErrorState {
	protected abstract void setError(String text);

	/**
	 * @return True if object is in error state
	 */
	public abstract boolean hasError();

	/**
	 * @return Return error text
	 */
	public abstract String getError();

	public abstract void clearError();
}
