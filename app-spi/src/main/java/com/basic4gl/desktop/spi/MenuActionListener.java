package com.basic4gl.desktop.spi;

import java.awt.event.ActionEvent;
import javax.swing.*;

public interface MenuActionListener {
    void actionPerformed(JFrame parent, ActionEvent e);
}
