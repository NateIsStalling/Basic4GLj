package com.basic4gl.library.desktopgl;

public interface VMHostApplication {
    // Process windows messages and stay responsive.
    // Return true if virtual machine can continue, or false if interrupted.
    // (E.g. a "stop" button clicked..)
    boolean ProcessMessages() ;
}