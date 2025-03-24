package com.basic4gl.desktop.util;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Created by Nate on 1/6/2016.
 */
public class SwingLayout {
    private JFrame mainFrame;
    private JMenuBar menuBar;

    private final Map<String, Integer> menus = new HashMap<>();

    void addMenuItem(String parent, JMenuItem item) {
        int index = menus.get(parent);
        JMenu menu = null;

        if (index > -1) {
            // Parent menu already exists
            menu = menuBar.getMenu(index);
        } else {
            // Parent menu doesn't exist; create a new one and cache its index
            index = menuBar.getMenuCount();
            menus.put(parent, index);
            menu = new JMenu(parent);
            menuBar.add(menu);
        }

        if (menu != null) {
            menu.add(item);
        }
    }
}
