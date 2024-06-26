package com.basic4gl.desktop;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by Nate on 1/30/2015.
 */
public class ReferenceWindow {
    private static final String TITLE = "Function Reference";
    private JFrame mFrame;

    JTextPane mFunctionPane;
    JTextPane mConstantPane;
    JScrollPane mFunctionScrollPane;
    JScrollPane mConstantScrollPane;

    public ReferenceWindow(Frame parent) {
        mFrame = new JFrame(TITLE);

        mFrame.setResizable(true);

        JPanel panel;
        JTabbedPane tabbedPane = new JTabbedPane();

        SwingUtilities.updateComponentTreeUI(tabbedPane);
        tabbedPane.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        tabbedPane.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        //Setup Functions tab
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("This is a raw dump of functions, and their parameter and return types");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        panel.add(label, BorderLayout.NORTH);

        mFunctionPane = new JTextPane();
        mFunctionPane.setEditable(false);
        mFunctionScrollPane = new JScrollPane(mFunctionPane);
        panel.add(mFunctionScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Functions", panel);

        //Setup Constants tab
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        mConstantPane = new JTextPane();
        mConstantPane.setEditable(false);
        mConstantScrollPane = new JScrollPane(mConstantPane);
        panel.add(mConstantScrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Constants", panel);

        mFrame.add(tabbedPane);

        mFrame.pack();
        mFrame.setSize(new Dimension(464, 346));
        mFrame.setLocationRelativeTo(parent);

    }

    public void setVisible(boolean visible) {
        if (mFrame != null) {
            mFrame.setVisible(visible);
        }
    }

    String TypeString(ValType type) {
        String result = "";
        for (int i = 0; i < type.getVirtualPointerLevel(); i++) {
            result = result + "&";
        }

        switch (type.basicType) {
            case BasicValType.VTP_INT:
                result = result + "int";
                break;
            case BasicValType.VTP_REAL:
                result = result + "real";
                break;
            case BasicValType.VTP_STRING:
                result = result + "string";
                break;
            default:
                result = result + "???";
        }

        for (int i = 0; i < type.arrayLevel; i++) {
            result = result + "()";
        }

        return result;
    }

    String TypeString(int type) {
        String result = "";

        switch (type) {
            case BasicValType.VTP_INT:
                result = result + "int";
                break;
            case BasicValType.VTP_REAL:
                result = result + "real";
                break;
            case BasicValType.VTP_STRING:
                result = result + "string";
                break;
            default:
                result = result + "???";
        }

        return result;
    }

    public void populate(TomBasicCompiler comp) {
        String text;
        if (comp == null) {
            return;
        }
        // Populate functions
        text = "";
        for (String key : comp.getFunctionIndex().keySet()) {
            for (Integer index : comp.getFunctionIndex().get(key)) {

                // Find name and function data
                String name = key;
                FunctionSpecification spec = comp.getFunctions().get(index);

                // Build description string
                String line = "";
                if (spec.isFunction())                                      // Return type
                {
                    line = line + TypeString(spec.getReturnType()) + " ";
                }
                line = line + name;                                         // Function name
                if (spec.hasBrackets())                                        // Opening bracket
                {
                    line = line + "(";
                } else {
                    line = line + " ";
                }
                boolean needComma = false;
                Vector<ValType> params = spec.getParamTypes().getParams();
                if (params != null) {
                    for (ValType type : params) {
                        if (needComma) {
                            line = line + ", ";
                        }
                        line = line + TypeString(type);
                        needComma = true;
                    }
                }
                if (spec.hasBrackets()) {
                    line = line + ")";
                }

                // Store description string
                text += line + '\n';
            }
        }
        mFunctionPane.setText(text);
        mFunctionPane.setCaretPosition(0);
        // Populate constants
        text = "";
        for (String key : comp.getConstants().keySet()) {

            // Build description string
            String line = key + " = (" + TypeString(comp.getConstants().get(key).getType()) + ") " + comp.getConstants().get(key).ToString();
            text += line + '\n';
        }
        mConstantPane.setText(text);
        mConstantPane.setCaretPosition(0);
    }

}
