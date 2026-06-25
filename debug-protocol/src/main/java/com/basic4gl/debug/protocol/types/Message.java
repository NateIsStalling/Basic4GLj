package com.basic4gl.debug.protocol.types;

import java.util.HashMap;

/**
 * Structured message used to return errors from commands.
 */
public class Message {
    public Integer id;
    public String format;
    public HashMap<String, String> variables = new HashMap<>();
    public Boolean showUser = false;
    public String url;
    public String urlLabel;
}
