package com.basic4gl.desktop.util;

import com.basic4gl.desktop.spi.DialogService;

import javax.swing.*;

public class BasicDialogService implements DialogService {
    private final JFrame frame;

    public BasicDialogService(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void showDialog(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    @Override
    public String showInputDialog(String message, String title, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                frame, message, title, JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }
}
