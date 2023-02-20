package com.basic4gl.runtime.util;

public class Assert {
    public static void assertTrue(Boolean value) {
        if (!value) {
           //TODO throw new RuntimeException("An internal error occurred");
        }
    }
}
