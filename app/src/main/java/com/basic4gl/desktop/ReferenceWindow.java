package com.basic4gl.desktop;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import java.awt.*;
import java.util.Vector;
import javax.swing.*;

/**
 * Created by Nate on 1/30/2015.
 */
public class ReferenceWindow {
	private static final String TITLE = "Function Reference";
	private JFrame mFrame;

	private JTextPane mFunctionPane;
	private JTextPane mConstantPane;
	private JScrollPane mFunctionScrollPane;
	private JScrollPane mConstantScrollPane;

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

		// Setup Functions tab
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

		// Setup Constants tab
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

	String getTypeString(ValType type) {
		String result = "";
		for (int i = 0; i < type.getVirtualPointerLevel(); i++) {
			result += "&";
		}

		switch (type.basicType) {
			case BasicValType.VTP_INT:
				result += "int";
				break;
			case BasicValType.VTP_REAL:
				result += "real";
				break;
			case BasicValType.VTP_STRING:
				result += "string";
				break;
			default:
				result += "???";
		}

		for (int i = 0; i < type.arrayLevel; i++) {
			result += "()";
		}

		return result;
	}

	String getTypeString(int type) {
		String result = "";

		switch (type) {
			case BasicValType.VTP_INT:
				result += "int";
				break;
			case BasicValType.VTP_REAL:
				result += "real";
				break;
			case BasicValType.VTP_STRING:
				result += "string";
				break;
			default:
				result += "???";
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
				if (spec.isFunction()) // Return type
				{
					line = line + getTypeString(spec.getReturnType()) + " ";
				}
				line += name; // Function name
				if (spec.hasBrackets()) // Opening bracket
				{
					line += "(";
				} else {
					line += " ";
				}
				boolean needComma = false;
				Vector<ValType> params = spec.getParamTypes().getParams();
				if (params != null) {
					for (ValType type : params) {
						if (needComma) {
							line += ", ";
						}
						line += getTypeString(type);
						needComma = true;
					}
				}
				if (spec.hasBrackets()) {
					line += ")";
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
			String line = key
					+ " = ("
					+ getTypeString(comp.getConstants().get(key).getType())
					+ ") "
					+ comp.getConstants().get(key).toString();
			text += line + '\n';
		}
		mConstantPane.setText(text);
		mConstantPane.setCaretPosition(0);
	}
}
