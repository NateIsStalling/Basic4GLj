package com.basic4gl.vm;

public class HasErrorState {
	// Error status
	boolean m_error;
	String m_errorString;

	protected void SetError(String text) {
		m_error = true;
		m_errorString = text;
	}

	// Error handling
	public HasErrorState() {
		m_error = false;
	}

	public boolean Error() {
		return m_error;
	}

	public String GetError() {
		assert (m_error);
		return m_errorString;
	}

	public void ClearError() {
		m_error = false;
	}

}
