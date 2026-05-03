package com.basic4gl.runtime.plugin;

/**
 * This object must be implemented by the Plugin.
 * Basic4GL will ask for it when it loads the Plugin (via the PluginInit function),
 * and will call its methods to extract information about the Plugin, such as its
 * function list.
 * It will also call various methods when certain events occur, such as the
 * program starts or stops.
 */
public interface Basic4GLPlugin {

    /**
     * Called ONCE when the Plugin loads.
     * Perform any initialisation and register any functions to the registery
     * object passed in.
     * Return true if the Plugin loaded correctly, or false if not (see getError()
     * for how to report the error.)
     *
     * @param registry
     * @param isStandaloneExe
     * @return
     */
    boolean load(Basic4GLFunctionRegistry registry, boolean isStandaloneExe);

    /**
     * Called when the Plugin is about to unload.
     * Perform any final cleanup here.
     * Note: Basic4GL will not attempt to access the object after this function
     * is called so the object can safely destroy itself.
     */
    void unload();

    /**
     * Called when a Basic4GL program is about to run.
     * This is a good place to initialise any data that must be reset each time
     * the program runs.
     * Return true if the program can proceed, or false if it cannot (see
     * getError() for how to report the error.)
     */
    boolean start();

    /**
     * Called when a Basic4GL program stops, either from reaching the end or
     * if it is interrupted.
     * This is a good place to clean up any resources used by the Plugin while the
     * program is running.
     * DO NOT LEAVE IT UP TO THE BASIC4GL PROGRAMMER TO PREVENT RESOURCE AND
     * MEMORY LEAKS. BASIC4GL IS DESIGNED TO BE A FORGIVING LANGUAGE, THAT
     * PEOPLE CAN PLAY AND LEARN WITH, WHICH MEANS THE PLUGINS AND RUNTIME
     * FUNCTIONS MUST DO A BIT OF EXTRA WORK TO MAKE SURE THEY KEEP TRACK OF
     * EVERYTHING.
     */
    void end();

    /**
     * Called when the program has paused.
     * This applies to programs running in the Basic4GL IDE only.
     * Plugins that respond to this event generally do so to make debugging easier.
     * E.g. a full screen graphics library might switch back to the desktop, so
     * that the programmer can see the Basic4GL IDE again.
     */
    void pause();

    /**
     * Called when the program resumes after it has been paused.
     */
    void resume();

    /**
     * Called when the program resumes after it has been paused and 1000 VM op-codes
     * have been executed.
     * Plugins might respond to this method (instead of resume()) to avoid switching
     * between running and paused mode unnecessarily during short executions (e.g.
     * when the programmer is stepping through the program).
     * For example, a full screen graphics library might do this so that the monitor
     * doesn't switch resolutions and back every time the programmer hits the "step"
     * button.
     */
    void delayedResume();

    /**
     * Return the text of the last error.
     * Called whenever load() or start() returns false to fetch the text of the
     * error. The method is passed a pointer to a 1024 character array into
     * which it should write the text of the message (0 terminated).
     * If getError() does not modify "error", a generic "Plugin could not load/start"
     * message will be displayed to the user.
     * //TODO review javadoc
     */
    String getError();

    /**
     * This is called periodically by Basic4GL to keep the application responsive.
     * If your plugin requires any periodic house-keeping (e.g. if it creates
     * a window, and needs to respond to windows messages), this is a good place
     * to put your code. (In fact Basic4GL calls this method immediately after
     * it processes its own windows messages).
     */
    void processMessages();
}
