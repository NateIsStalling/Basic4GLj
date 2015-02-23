package com.basic4gl.vm;

public class HasErrorState {
	// Error status
	boolean mErrorOccurred;
	String mMessage;

	// Error handling
	public HasErrorState() {
		mErrorOccurred = false;
	}

	public String getError() {
		return mErrorOccurred ? mMessage : "";
	}

	public void setError(String message) {
		mErrorOccurred = true;
		mMessage = message;
	}

	public boolean hasError() {
		return mErrorOccurred;
	}

	public void clearError() {
		mErrorOccurred = false;
	}

}
