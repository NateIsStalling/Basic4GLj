package com.basic4gl.desktop.vmview;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.debug.protocol.types.Variable;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualMachineViewDialog extends JFrame implements IVirtualMachineView {
    private static final Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d+\\s*-\\s*\\d+)\\]");
    public static final String SCOPE_CODE = "code";
    public static final String SCOPE_CALL_STACK = "callStack";
    public static final String SCOPE_VARIABLES = "variables";
    public static final String SCOPE_REGISTERS = "registers";
    public static final String SCOPE_HEAP = "heap";
    public static final String SCOPE_STACK = "stack";
    public static final String SCOPE_TEMP = "temp";
    public static final String SCOPE_ALLOCATED_STRINGS = "allocatedStrings";

    public interface SeeValueHandler {
        void onSeeValueRequested(String expression);
    }

    private static final Color ERROR_ROW_BG = new Color(242, 242, 242);
    private static final Color ERROR_ROW_FG = new Color(120, 120, 120);

    private final DefaultTableModel codeTableModel;
    private final DefaultTableModel callStackTableModel;
    private final DefaultTableModel variablesTableModel;
    private final DefaultTableModel heapTableModel;
    private final DefaultTableModel stackTableModel;
    private final DefaultTableModel tempTableModel;
    private final DefaultTableModel allocatedStringsTableModel;
    private final JTable variablesTable;
    private JTable codeTable;
    private final JTextArea variableDataTextArea;
    private final JButton seeValueButton;
    private final JLabel codeStatusLabel;
    private final JLabel callStackStatusLabel;
    private final JLabel variablesStatusLabel;
    private final JLabel heapStatusLabel;
    private final JLabel stackStatusLabel;
    private final JLabel tempStatusLabel;
    private final JLabel allocatedStringsStatusLabel;
    private final JTextField reg1IntegerField;
    private final JTextField reg1FloatField;
    private final JTextField reg1StringField;
    private final JTextField reg2IntegerField;
    private final JTextField reg2FloatField;
    private final JTextField reg2StringField;
    private final JLabel statusLabel;
    private int currentSourceLine = -1;
    private int currentSourceColumn = -1;
    private int highlightedCodeRow = -1;
    private final java.util.List<Variable> currentVariables = new ArrayList<>();
    private final Map<String, String> resolvedVariableValues = new HashMap<>();
    private SeeValueHandler seeValueHandler;

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
        variablesTable = new JTable(variablesTableModel);
        variablesTable.setFillsViewportHeight(true);
        variablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applyMutedErrorRowRenderer(variablesTable);
        variableDataTextArea = new JTextArea(6, 20);
        variableDataTextArea.setEditable(false);
        variableDataTextArea.setLineWrap(true);
        variableDataTextArea.setWrapStyleWord(true);
        seeValueButton = new JButton("See Value");
        seeValueButton.setEnabled(false);
        seeValueButton.addActionListener(e -> requestSeeValueForSelection());
        variablesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateVariableDataPaneFromSelection();
            }
        });
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
        tempTableModel = new DefaultTableModel(new Object[] {"Index", "Integer", "Floating Pt", "String"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        allocatedStringsTableModel =
                new DefaultTableModel(new Object[] {"Index", "Integer", "Floating Pt", "String"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        codeStatusLabel = createSectionStatusLabel();
        callStackStatusLabel = createSectionStatusLabel();
        variablesStatusLabel = createSectionStatusLabel();
        heapStatusLabel = createSectionStatusLabel();
        stackStatusLabel = createSectionStatusLabel();
        tempStatusLabel = createSectionStatusLabel();
        allocatedStringsStatusLabel = createSectionStatusLabel();

        topSection.add(createCodeGroup(codeTableModel));
        topSection.add(createGroupWithTable("Call stack", callStackTableModel, callStackStatusLabel));
        topSection.add(createVariablesGroup());

        contentPanel.add(topSection);

        // === Bottom GroupBox Section (Heap, Stack, Temp, Allocated Strings) ===
        JPanel bottomSection = new JPanel(new GridLayout(2, 2));
        bottomSection.add(createGroupWithTable("Heap", heapTableModel, heapStatusLabel));
        bottomSection.add(createGroupWithTable("Stack", stackTableModel, stackStatusLabel));
        bottomSection.add(createGroupWithTable("Temp", tempTableModel, tempStatusLabel));
        bottomSection.add(createGroupWithTable(
                "Allocated Strings", allocatedStringsTableModel, allocatedStringsStatusLabel));

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

    private JLabel createSectionStatusLabel() {
        JLabel label = new JLabel("");
        label.setForeground(new Color(170, 0, 0));
        return label;
    }

    private JPanel createGroupWithTable(String title, DefaultTableModel model, JLabel statusLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        applyMutedErrorRowRenderer(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVariablesGroup() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Variables"));

        JScrollPane tableScrollPane = new JScrollPane(variablesTable);
        JScrollPane detailScrollPane = new JScrollPane(variableDataTextArea);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("Variable Data"));

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.add(seeValueButton);
        detailPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setOneTouchExpandable(true);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(variablesStatusLabel, BorderLayout.SOUTH);
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

        codeTable = new JTable(model);
        codeTable.setFillsViewportHeight(true);
        codeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == highlightedCodeRow) {
                    component.setBackground(new Color(255, 220, 220));
                    component.setForeground(Color.BLACK);
                } else if (isErrorRow(table, row)) {
                    component.setBackground(ERROR_ROW_BG);
                    component.setForeground(ERROR_ROW_FG);
                } else if (isSelected) {
                    component.setBackground(table.getSelectionBackground());
                    component.setForeground(table.getSelectionForeground());
                } else {
                    component.setBackground(table.getBackground());
                    component.setForeground(table.getForeground());
                }
                return component;
            }
        });
        JScrollPane tablePane = new JScrollPane(codeTable);
        codePanel.add(tablePane);
        codePanel.add(codeStatusLabel);

        return codePanel;
    }

    private void applyMutedErrorRowRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isErrorRow(table, row)) {
                    component.setBackground(ERROR_ROW_BG);
                    component.setForeground(ERROR_ROW_FG);
                } else if (isSelected) {
                    component.setBackground(table.getSelectionBackground());
                    component.setForeground(table.getSelectionForeground());
                } else {
                    component.setBackground(table.getBackground());
                    component.setForeground(table.getForeground());
                }
                return component;
            }
        });
    }

    private boolean isErrorRow(JTable table, int row) {
        if (table == null || row < 0 || row >= table.getRowCount()) {
            return false;
        }
        for (int col = 0; col < table.getColumnCount(); col++) {
            Object value = table.getValueAt(row, col);
            if (value == null) {
                continue;
            }
            String text = value.toString();
            if ("!".equals(text) || text.contains("[ERROR]")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateCallStack(StackTraceCallback callback) {
        callStackStatusLabel.setText("");
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
        codeStatusLabel.setText("");
        codeTableModel.setRowCount(0);
        highlightedCodeRow = -1;
        if (callback == null || callback.getInstructions() == null) {
            if (codeTable != null) {
                codeTable.clearSelection();
                codeTable.repaint();
            }
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

            if (highlightedCodeRow < 0 && matchesCurrentInstruction(instruction)) {
                highlightedCodeRow = i;
            }
        }

        if (codeTable != null) {
            if (highlightedCodeRow >= 0) {
                codeTable.setRowSelectionInterval(highlightedCodeRow, highlightedCodeRow);
                Rectangle rectangle = codeTable.getCellRect(highlightedCodeRow, 0, true);
                codeTable.scrollRectToVisible(rectangle);
            } else {
                codeTable.clearSelection();
            }
            codeTable.repaint();
        }
    }

    public void setCurrentSourcePosition(int sourceLine, int sourceColumn) {
        currentSourceLine = sourceLine;
        currentSourceColumn = sourceColumn;
        repaintCodeHighlightFromCurrentSource();
    }

    private boolean matchesCurrentInstruction(DisassembledInstruction instruction) {
        if (instruction == null || currentSourceLine < 0 || instruction.line == null) {
            return false;
        }
        if (instruction.line != currentSourceLine) {
            return false;
        }
        // Prefer column when available, but tolerate missing or mismatched column mappings.
        return instruction.column == null || currentSourceColumn < 0 || instruction.column == currentSourceColumn;
    }

    private void repaintCodeHighlightFromCurrentSource() {
        if (codeTable == null) {
            return;
        }
        highlightedCodeRow = -1;
        for (int row = 0; row < codeTableModel.getRowCount(); row++) {
            Object sourceValue = codeTableModel.getValueAt(row, 4);
            Integer line = parseSourceLine(sourceValue != null ? sourceValue.toString() : null);
            if (line != null && line == currentSourceLine) {
                highlightedCodeRow = row;
                break;
            }
        }

        if (highlightedCodeRow >= 0) {
            codeTable.setRowSelectionInterval(highlightedCodeRow, highlightedCodeRow);
            Rectangle rectangle = codeTable.getCellRect(highlightedCodeRow, 0, true);
            codeTable.scrollRectToVisible(rectangle);
        }
        codeTable.repaint();
    }

    private Integer parseSourceLine(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        int colonIndex = source.lastIndexOf(':');
        if (colonIndex < 0 || colonIndex >= source.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(source.substring(colonIndex + 1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public void updateVariables(VariablesCallback callback) {
        variablesStatusLabel.setText("");
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
        currentVariables.clear();
        for (Variable variable : variables) {
            variablesTableModel.addRow(new Object[] {variable.name, variable.value, variable.type});
            currentVariables.add(variable);
        }
        if (variablesTableModel.getRowCount() > 0) {
            variablesTable.setRowSelectionInterval(0, 0);
            updateVariableDataPaneFromSelection();
        } else {
            variableDataTextArea.setText("");
        }
    }

    private void updateVariableDataPaneFromSelection() {
        int row = variablesTable.getSelectedRow();
        if (row < 0) {
            variableDataTextArea.setText("");
            seeValueButton.setEnabled(false);
            return;
        }

        if (row >= currentVariables.size()) {
            variableDataTextArea.setText("");
            seeValueButton.setEnabled(false);
            return;
        }

        Variable variable = currentVariables.get(row);
        String name = String.valueOf(variablesTableModel.getValueAt(row, 0));
        String value = String.valueOf(variablesTableModel.getValueAt(row, 1));
        String type = String.valueOf(variablesTableModel.getValueAt(row, 2));
        String expression = getExpression(variable);

        String displayedValue = value;
        if (expression != null && resolvedVariableValues.containsKey(expression)) {
            displayedValue = resolvedVariableValues.get(expression);
        }

        boolean isLazy = variable.presentationHint != null && Boolean.TRUE.equals(variable.presentationHint.lazy);
        seeValueButton.setEnabled(isLazy && expression != null && seeValueHandler != null);
        String lazyText = isLazy ? "yes" : "no";

        variableDataTextArea.setText("Name: " + name + "\nType: " + type + "\nLazy: " + lazyText
                + "\n\nValue:\n" + displayedValue);
        variableDataTextArea.setCaretPosition(0);
    }

    public void setSeeValueHandler(SeeValueHandler seeValueHandler) {
        this.seeValueHandler = seeValueHandler;
        updateVariableDataPaneFromSelection();
    }

    public void applySeeValueResult(String expression, String value) {
        if (expression == null) {
            return;
        }
        resolvedVariableValues.put(expression, value != null ? value : "");
        updateVariableDataPaneFromSelection();
    }

    private void requestSeeValueForSelection() {
        int row = variablesTable.getSelectedRow();
        if (row < 0 || row >= currentVariables.size() || seeValueHandler == null) {
            return;
        }
        String expression = getExpression(currentVariables.get(row));
        if (expression == null) {
            return;
        }
        seeValueHandler.onSeeValueRequested(expression);
    }

    private String getExpression(Variable variable) {
        if (variable == null) {
            return null;
        }
        if (variable.evaluateName != null && !variable.evaluateName.trim().isEmpty()) {
            return variable.evaluateName;
        }
        if (variable.name != null && !variable.name.trim().isEmpty()) {
            return variable.name;
        }
        return null;
    }

    private boolean isRegisterPayload(Variable[] variables) {
        return variables.length == 2
                && "Register 1".equals(variables[0].name)
                && "Register 2".equals(variables[1].name);
    }

    private void updateRegisterFields(Variable[] variables) {
        variablesStatusLabel.setText("");
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
        String scope = getMemoryScope(variables);
        DefaultTableModel model = getMemoryTableModel(scope);
        clearMemoryScopeStatus(scope);

        if (model == null) {
            return;
        }

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

    private String getMemoryScope(Variable[] variables) {
        if (variables.length > 0 && variables[0] != null && variables[0].presentationHint != null) {
            String kind = variables[0].presentationHint.kind;
            if (SCOPE_HEAP.equals(kind)
                    || SCOPE_STACK.equals(kind)
                    || SCOPE_TEMP.equals(kind)
                    || SCOPE_ALLOCATED_STRINGS.equals(kind)) {
                return kind;
            }
        }

        // Fallback compatibility heuristic for older payloads that didn't include a scope kind.
        if (variables.length >= 2) {
            Integer first = parseInteger(variables[0].name);
            Integer second = parseInteger(variables[1].name);
            if (first != null && second != null && first > second) {
                return SCOPE_STACK;
            }
        }
        return SCOPE_HEAP;
    }

    private DefaultTableModel getMemoryTableModel(String scope) {
        if (SCOPE_STACK.equals(scope)) {
            return stackTableModel;
        }
        if (SCOPE_TEMP.equals(scope)) {
            return tempTableModel;
        }
        if (SCOPE_ALLOCATED_STRINGS.equals(scope)) {
            return allocatedStringsTableModel;
        }
        return heapTableModel;
    }

    private void clearMemoryScopeStatus(String scope) {
        if (SCOPE_STACK.equals(scope)) {
            stackStatusLabel.setText("");
            return;
        }
        if (SCOPE_TEMP.equals(scope)) {
            tempStatusLabel.setText("");
            return;
        }
        if (SCOPE_ALLOCATED_STRINGS.equals(scope)) {
            allocatedStringsStatusLabel.setText("");
            return;
        }
        heapStatusLabel.setText("");
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

    public void showError(String scope, String message) {
        String detail = message != null && !message.trim().isEmpty() ? message : "Unknown VM viewer error";
        String rangeLabel = extractRangeLabel(detail);

        if (SCOPE_CODE.equals(scope)) {
            codeStatusLabel.setText(detail);
            appendPlaceholderRow(codeTableModel, new Object[] {"!", rangeLabel, "[ERROR]", detail, ""});
            return;
        }

        if (SCOPE_CALL_STACK.equals(scope)) {
            callStackStatusLabel.setText(detail);
            appendPlaceholderRow(callStackTableModel, new Object[] {"[ERROR] " + rangeLabel, detail, 0});
            return;
        }

        if (SCOPE_HEAP.equals(scope)) {
            heapStatusLabel.setText(detail);
            appendPlaceholderRow(heapTableModel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_STACK.equals(scope)) {
            stackStatusLabel.setText(detail);
            appendPlaceholderRow(stackTableModel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_TEMP.equals(scope)) {
            tempStatusLabel.setText(detail);
            appendPlaceholderRow(tempTableModel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_ALLOCATED_STRINGS.equals(scope)) {
            allocatedStringsStatusLabel.setText(detail);
            appendPlaceholderRow(allocatedStringsTableModel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        // Variables + registers default to variable panel status.
        variablesStatusLabel.setText(detail);
        appendPlaceholderRow(variablesTableModel, new Object[] {"[ERROR] " + rangeLabel, detail, ""});
    }

    private void appendPlaceholderRow(DefaultTableModel model, Object[] row) {
        if (model == null) {
            return;
        }
        model.addRow(row);
    }

    private String extractRangeLabel(String detail) {
        if (detail == null) {
            return "[? - ?]";
        }
        Matcher matcher = RANGE_PATTERN.matcher(detail);
        if (matcher.find()) {
            return "[" + matcher.group(1) + "]";
        }
        return "[? - ?]";
    }

    @Override
    public void setVmRunning(boolean running) {
        statusLabel.setText(running ? "VM: running" : "VM: stopped/paused");
    }
}
