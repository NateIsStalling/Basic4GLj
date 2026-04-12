package com.basic4gl.runtime.plugin;

/**
 * Base class for local plugin libraries. Supplies some default functionality.
 * Sub-classes must override the Basic4GLPlugin.load() method as a minimum.
 */
public abstract class LocalPlugin implements Basic4GLPlugin {
    private String errorText;

    /**
     * Report an error (e.g. failure to start()/load())
     */
    protected void setError(String error) { errorText = error; }

    public void dispose() {}

    public  boolean start() { return true;}
    public  void unload() {}
    public  void end(){}
    public  void pause(){}
    public  void resume(){}
    public  void delayedResume(){}
    public  String getError(){
        // Get error text. Truncate to 1024 characters if necessary.
        // Porting note: Original Basic4GL plugin implementation truncates errorText to 1023 to fit a char[1024] with 0 terminating value
        return errorText.substring(0, Math.min(errorText.length(), 1024));
    }
    public  void processMessages(){}
}
