package com.basic4gl.desktop.spi;

import javax.swing.*;
import java.awt.event.ActionEvent;

public interface MenuActionListener {
    void actionPerformed(JFrame parent, ActionEvent e);
}
