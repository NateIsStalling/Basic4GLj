package com.basic4gl.desktop.spi;

import javax.swing.*;

public interface DialogService {
    //    String title();
    //    JComponent createContent(DialogContext context);
    //    Boolean getResult();
    //    default boolean validate() { return true; }
    public void showDialog(String message);

    String showInputDialog(String message, String title, String initialValue);
}
