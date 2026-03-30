package com.basic4gl.desktop.vmview;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VirtualMachineViewDialog extends JFrame {
    public VirtualMachineViewDialog(Frame parent) {
        setTitle("Dialog");
        setSize(1070, 712);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Registers Section ===
        JPanel registerPanel = new JPanel(new GridLayout(3, 4));
        registerPanel.add(new JLabel(""));
        registerPanel.add(new JLabel("Integer"));
        registerPanel.add(new JLabel("Floating Pt"));
        registerPanel.add(new JLabel("String"));

        registerPanel.add(new JLabel("Register 1"));
        registerPanel.add(createReadOnlyField());
        registerPanel.add(createReadOnlyField());
        registerPanel.add(createReadOnlyField());

        registerPanel.add(new JLabel("Register 2"));
        registerPanel.add(createReadOnlyField());
        registerPanel.add(createReadOnlyField());
        registerPanel.add(createReadOnlyField());

        // === Main Content Panel ===
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(registerPanel);

        // === Top GroupBox Section (Code, Call stack, Variables) ===
        JPanel topSection = new JPanel(new GridLayout(1, 3));

        topSection.add(createCodeGroup());
        topSection.add(createGroupWithTable("Call stack", 1, 3));
        topSection.add(createGroupWithTable("Variables", 3, 3));

        contentPanel.add(topSection);

        // === Bottom GroupBox Section (Heap, Stack) ===
        JPanel bottomSection = new JPanel(new GridLayout(1, 2));
        bottomSection.add(createGroupWithTable("Heap", 4, 3));
        bottomSection.add(createGroupWithTable("Stack", 4, 3));

        contentPanel.add(bottomSection);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        return field;
    }

    private JPanel createGroupWithTable(String title, int columns, int rows) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTable table = new JTable(new DefaultTableModel(rows, columns));
        JScrollPane scrollPane = new JScrollPane(table);

        JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        scrollPane.setVerticalScrollBar(scrollBar);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCodeGroup() {
        JPanel codePanel = new JPanel();
        codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.Y_AXIS));
        codePanel.setBorder(BorderFactory.createTitledBorder("Code"));

        JButton stepButton = new JButton("Step");
        stepButton.setIcon(new ImageIcon("icons/Images/StepOver2.png")); // Make sure the path is correct
        stepButton.setPreferredSize(new Dimension(100, 40));
        codePanel.add(stepButton);

        String[] headers = {"#", "OP", "Type", "Val", "Source"};
        JTable table = new JTable(new DefaultTableModel(5, headers.length));
        JScrollPane tablePane = new JScrollPane(table);
        codePanel.add(tablePane);

        return codePanel;
    }
}
