package com.basic4gl.compiler.types;

public enum LanguageSyntax {
    LS_TRADITIONAL(0), // As compatible as possible with other BASICs
    /**
     *  Standard Basic4GL syntax for backwards compatibility with existing code.
     */
    LS_BASIC4GL(1),
    /**
     * Traditional mode PRINT, but otherwise standard Basic4GL syntax
     */
    LS_TRADITIONAL_PRINT(2),
    /**
     * Like LS_TRADITIONAL, but also tries to match the variable suffixes of other BASIC types.
     */
    LS_TRADITIONAL_SUFFIX(3);

    private final int type;

    LanguageSyntax(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
