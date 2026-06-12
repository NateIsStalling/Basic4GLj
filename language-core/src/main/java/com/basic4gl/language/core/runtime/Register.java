package com.basic4gl.language.core.runtime;

public class Register {
    Value value;
    String stringValue;

    public Register(Value value, String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public Value getValue() {
        return value;
    }

    public String getStringValue() {
        return stringValue;
    }
}
