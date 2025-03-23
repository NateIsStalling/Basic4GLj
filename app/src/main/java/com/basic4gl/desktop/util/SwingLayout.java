package com.basic4gl.desktop.util;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Created by Nate on 1/6/2016.
 */
public class SwingLayout {
    private JFrame mMainFrame;
    private JMenuBar mMenuBar;

    private Map<String, Integer> mMenus = new HashMap<>();

    void addMenuItem(String parent, JMenuItem item) {
        int index = mMenus.get(parent);
        JMenu menu = null;

        if (index > -1) {
            // Parent menu already exists
            menu = mMenuBar.getMenu(index);
        } else {
            // Parent menu doesn't exist; create a new one and cache its index
            index = mMenuBar.getMenuCount();
            mMenus.put(parent, index);
            menu = new JMenu(parent);
            mMenuBar.add(menu);
        }

        if (menu != null) {
            menu.add(item);
        }
    }
}
