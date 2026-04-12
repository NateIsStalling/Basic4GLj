package com.basic4gl.desktop.vmview;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.debug.protocol.types.Variable;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VirtualMachineViewDialog extends JFrame implements IVirtualMachineView {
    private final DefaultTableModel codeTableModel;
    private final DefaultTableModel callStackTableModel;
    private final DefaultTableModel variablesTableModel;
    private final DefaultTableModel heapTableModel;
    private final DefaultTableModel stackTableModel;
    private final JTextField reg1IntegerField;
    private final JTextField reg1FloatField;
    private final JTextField reg1StringField;
    private final JTextField reg2IntegerField;
    private final JTextField reg2FloatField;
    private final JTextField reg2StringField;
    private final JLabel statusLabel;

    public VirtualMachineViewDialog(Frame parent) {
        setTitle("Dialog");
        setSize(1070, 712);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // === Registers Section ===
        JPanel registerPanel = new JPanel(new GridLayout(3, 4));
        registerPanel.add(new JLabel(""));
        registerPanel.add(new JLabel("Integer"));
        registerPanel.add(new JLabel("Floating Pt"));
        registerPanel.add(new JLabel("String"));

        registerPanel.add(new JLabel("Register 1"));
        reg1IntegerField = createReadOnlyField();
        reg1FloatField = createReadOnlyField();
        reg1StringField = createReadOnlyField();
        registerPanel.add(reg1IntegerField);
        registerPanel.add(reg1FloatField);
        registerPanel.add(reg1StringField);

        registerPanel.add(new JLabel("Register 2"));
        reg2IntegerField = createReadOnlyField();
        reg2FloatField = createReadOnlyField();
        reg2StringField = createReadOnlyField();
        registerPanel.add(reg2IntegerField);
        registerPanel.add(reg2FloatField);
        registerPanel.add(reg2StringField);

        // === Main Content Panel ===
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(registerPanel);

        // === Top GroupBox Section (Code, Call stack, Variables) ===
        JPanel topSection = new JPanel(new GridLayout(1, 3));

        codeTableModel = new DefaultTableModel(new Object[] {"#", "Address", "Instruction", "Symbol", "Source"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        callStackTableModel = new DefaultTableModel(new Object[] {"Name", "Source", "Line"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variablesTableModel = new DefaultTableModel(new Object[] {"Name", "Value", "Type"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        heapTableModel = new DefaultTableModel(new Object[] {"Index", "Integer", "Floating Pt", "String"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stackTableModel = new DefaultTableModel(new Object[] {"Index", "Integer", "Floating Pt", "String"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        topSection.add(createCodeGroup(codeTableModel));
        topSection.add(createGroupWithTable("Call stack", callStackTableModel));
        topSection.add(createGroupWithTable("Variables", variablesTableModel));

        contentPanel.add(topSection);

        // === Bottom GroupBox Section (Heap, Stack) ===
        JPanel bottomSection = new JPanel(new GridLayout(1, 2));
        bottomSection.add(createGroupWithTable("Heap", heapTableModel));
        bottomSection.add(createGroupWithTable("Stack", stackTableModel));

        contentPanel.add(bottomSection);

        statusLabel = new JLabel("VM: stopped");
        contentPanel.add(statusLabel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        return field;
    }

    private JPanel createGroupWithTable(String title, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCodeGroup(DefaultTableModel model) {
        JPanel codePanel = new JPanel();
        codePanel.setLayout(new BoxLayout(codePanel, BoxLayout.Y_AXIS));
        codePanel.setBorder(BorderFactory.createTitledBorder("Code"));

        JButton stepButton = new JButton("Step");
        stepButton.setIcon(new ImageIcon("icons/Images/StepOver2.png")); // Make sure the path is correct
        stepButton.setPreferredSize(new Dimension(100, 40));
        codePanel.add(stepButton);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane tablePane = new JScrollPane(table);
        codePanel.add(tablePane);

        return codePanel;
    }

    @Override
    public void updateCallStack(StackTraceCallback callback) {
        callStackTableModel.setRowCount(0);
        if (callback == null || callback.stackFrames == null) {
            return;
        }

        for (StackFrame frame : callback.stackFrames) {
            callStackTableModel.addRow(new Object[] {frame.name, frame.source, frame.line});
        }
    }

    @Override
    public void updateDisassembly(DisassembleCallback callback) {
        codeTableModel.setRowCount(0);
        if (callback == null || callback.getInstructions() == null) {
            return;
        }

        DisassembledInstruction[] instructions = callback.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            DisassembledInstruction instruction = instructions[i];
            String source = "";
            if (instruction.location != null) {
                source = instruction.location.path + ":" + (instruction.line != null ? instruction.line : "");
            }
            codeTableModel.addRow(new Object[] {
                    i,
                    instruction.address,
                    instruction.instruction,
                    instruction.symbol,
                    source
            });
        }
    }

    @Override
    public void updateVariables(VariablesCallback callback) {
        if (callback == null || callback.getVariables() == null || callback.getVariables().length == 0) {
            return;
        }

        Variable[] variables = callback.getVariables();
        if (isRegisterPayload(variables)) {
            updateRegisterFields(variables);
            return;
        }
        if (isMemoryPayload(variables)) {
            updateMemoryTable(variables);
            return;
        }

        variablesTableModel.setRowCount(0);
        for (Variable variable : variables) {
            variablesTableModel.addRow(new Object[] {variable.name, variable.value, variable.type});
        }
    }

    private boolean isRegisterPayload(Variable[] variables) {
        return variables.length == 2
                && "Register 1".equals(variables[0].name)
                && "Register 2".equals(variables[1].name);
    }

    private void updateRegisterFields(Variable[] variables) {
        reg1IntegerField.setText(variables[0].value != null ? variables[0].value : "");
        reg1FloatField.setText(variables[0].type != null ? variables[0].type : "");
        reg1StringField.setText(variables[0].evaluateName != null ? variables[0].evaluateName : "");

        reg2IntegerField.setText(variables[1].value != null ? variables[1].value : "");
        reg2FloatField.setText(variables[1].type != null ? variables[1].type : "");
        reg2StringField.setText(variables[1].evaluateName != null ? variables[1].evaluateName : "");
    }

    private boolean isMemoryPayload(Variable[] variables) {
        for (Variable variable : variables) {
            if (!isInteger(variable.name)) {
                return false;
            }
        }
        return true;
    }

    private void updateMemoryTable(Variable[] variables) {
        DefaultTableModel model = isStackPayload(variables) ? stackTableModel : heapTableModel;
        model.setRowCount(0);
        for (Variable variable : variables) {
            model.addRow(new Object[] {
                variable.name,
                variable.value,
                variable.type,
                variable.evaluateName != null ? variable.evaluateName : ""
            });
        }
    }

    private boolean isStackPayload(Variable[] variables) {
        if (variables.length < 2) {
            return false;
        }
        Integer first = parseInteger(variables[0].name);
        Integer second = parseInteger(variables[1].name);
        return first != null && second != null && first > second;
    }

    private boolean isInteger(String value) {
        return parseInteger(value) != null;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setVmRunning(boolean running) {
        statusLabel.setText(running ? "VM: running" : "VM: stopped/paused");
    }
}
