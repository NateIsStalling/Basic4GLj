package com.basic4gl.language.core.internal;

public class Assert {
    public static void assertTrue(Boolean value) {
        if (!value) {
            throw new RuntimeException("An internal error occurred");
        }
    }

    public static void assertTrue(Boolean value, String message) {
        if (!value) {
            throw new RuntimeException(message);
        }
    }
}
