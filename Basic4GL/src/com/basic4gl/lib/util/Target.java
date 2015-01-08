package com.basic4gl.lib.util;

/**
 * Launch target for a Basic4GL application; should include the main function 
 * or other entry point - depending on target platform.  
 * @author Nate
 *
 */
public interface Target {
	public abstract void reset();
	public abstract void activate();
	
	public abstract void show(TaskCallback callbacks);
	public abstract void hide();
	
	public abstract boolean isFullscreen();
	public abstract boolean isVisible();
	public abstract boolean isClosing();
	
	/**
	 * 
	 * @return Window for callbacks
	 */
	public Object getContext();
	/**
	 * 
	 * @return GL Context for callbacks
	 */
	public Object getContextGL();	
}
