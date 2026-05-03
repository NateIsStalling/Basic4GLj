package com.basic4gl.desktop.vmview;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.debug.protocol.types.Variable;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class VirtualMachineViewDialog extends JFrame implements IVirtualMachineView {
    private static final Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d+\\s*-\\s*\\d+)]");
    public static final String SCOPE_CODE = "code";
    public static final String SCOPE_CALL_STACK = "callStack";
    public static final String SCOPE_VARIABLES = "variables";
    public static final String SCOPE_REGISTERS = "registers";
    public static final String SCOPE_HEAP = "heap";
    public static final String SCOPE_STACK = "stack";
    public static final String SCOPE_TEMP = "temp";
    public static final String SCOPE_ALLOCATED_STRINGS = "allocatedStrings";

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
    private final JLabel seeValueLabel;
    private final JButton seeValueButton;
    private final JButton playPauseButton;
    private final JButton stepButton;
    private final JButton stepOverButton;
    private final JButton stepOutButton;
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
    private VariableWatchListener variableWatchListener;
    private DebugControlsListener debugControlsListener;

    public VirtualMachineViewDialog(Frame parent) {
        setTitle("Virtual Machine View");
        setSize(1070, 712);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        if (parent != null) {
            setLocationRelativeTo(parent);
        }

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

        // === Top GroupBox Section (Code, Call stack) ===
        JPanel topSection = new JPanel(new GridBagLayout());
        GridBagConstraints topConstraints = new GridBagConstraints();
        topConstraints.gridy = 0;
        topConstraints.fill = GridBagConstraints.BOTH;
        topConstraints.weighty = 1.0;

        codeTableModel = new DefaultTableModel(new Object[] {"Address", "Instruction", "Symbol", "Source"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        callStackTableModel = new DefaultTableModel(new Object[] {"Name"}, 0) {
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
        seeValueLabel = new JLabel("Value not loaded:");
        seeValueLabel.setVisible(false);
        seeValueButton = new JButton("See Value");
        playPauseButton = new JButton(createImageIcon(ICON_PLAY));
        playPauseButton.setToolTipText("Play/Pause");
        playPauseButton.addActionListener(e -> {
            if (debugControlsListener != null) {
                debugControlsListener.onPlayPauseRequested();
            }
        });

        stepButton = new JButton(createImageIcon(ICON_STEP_IN));
        stepButton.setToolTipText("Step");
        stepButton.addActionListener(e -> {
            if (debugControlsListener != null) {
                debugControlsListener.onStepRequested();
            }
        });

        stepOverButton = new JButton(createImageIcon(ICON_STEP_OVER));
        stepOverButton.setToolTipText("Step Over");
        stepOverButton.addActionListener(e -> {
            if (debugControlsListener != null) {
                debugControlsListener.onStepOverRequested();
            }
        });

        stepOutButton = new JButton(createImageIcon(ICON_STEP_OUT));
        stepOutButton.setToolTipText("Step Out");
        stepOutButton.addActionListener(e -> {
            if (debugControlsListener != null) {
                debugControlsListener.onStepOutRequested();
            }
        });

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
        allocatedStringsTableModel = new DefaultTableModel(new Object[] {"Index", "Value"}, 0) {
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

        topConstraints.gridx = 0;
        topConstraints.weightx = 3.0;
        topSection.add(createCodeGroup(codeTableModel), topConstraints);

        topConstraints.gridx = 1;
        topConstraints.weightx = 1.0;
        topSection.add(createGroupWithTable(callStackTableModel, callStackStatusLabel, "Call Stack"), topConstraints);

        contentPanel.add(topSection);

        // === Bottom Split Section (Memory tabs on left, Variables on right) ===
        JTabbedPane memoryTabs = new JTabbedPane();
        SwingUtilities.updateComponentTreeUI(memoryTabs);
        memoryTabs.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        memoryTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        memoryTabs.addTab("Heap", createGroupWithTable(heapTableModel, heapStatusLabel, null));
        memoryTabs.addTab("Stack", createGroupWithTable(stackTableModel, stackStatusLabel, null));
        memoryTabs.addTab("Temp", createGroupWithTable(tempTableModel, tempStatusLabel, null));
        memoryTabs.addTab(
                "Allocated Strings",
                createGroupWithTable(allocatedStringsTableModel, allocatedStringsStatusLabel, null));

        JPanel memoryAndVariablesPanel = new JPanel(new GridLayout(1, 2));
        memoryAndVariablesPanel.add(memoryTabs);
        memoryAndVariablesPanel.add(createVariablesGroup());

        contentPanel.add(memoryAndVariablesPanel);

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

    private JPanel createGroupWithTable(DefaultTableModel model, JLabel statusLabel, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        if (title != null && !title.trim().isEmpty()) {
            panel.setBorder(BorderFactory.createTitledBorder(title));
        }
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
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.add(seeValueLabel);
        actionPanel.add(seeValueButton);
        detailPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, detailPanel);
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

        JToolBar debugToolBar = new JToolBar();
        debugToolBar.setFloatable(false);
        debugToolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        debugToolBar.add(playPauseButton);
        debugToolBar.add(stepButton);
        debugToolBar.add(stepOverButton);
        debugToolBar.add(stepOutButton);
        codePanel.add(debugToolBar);

        codeTable = new JTable(model);
        codeTable.setFillsViewportHeight(true);
        codeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component =
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        tablePane.setAlignmentX(Component.LEFT_ALIGNMENT);
        codePanel.add(tablePane);
        codeStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        codePanel.add(codeStatusLabel);

        return codePanel;
    }

    private void applyMutedErrorRowRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component =
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
            callStackTableModel.addRow(new Object[] {frame.name});
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
                String sourceFileName = instruction.location.path;
                try {
                    File sourceFile = instruction.location.path != null ? new File(instruction.location.path) : null;
                    sourceFileName = sourceFile != null ? sourceFile.getName() : "";
                } catch (Exception ex) {
                    // Ignore any issues with file parsing and just use the raw path.
                    sourceFileName = instruction.location.path;
                }
                String lineInfo = instruction.line != null ? String.valueOf(instruction.line + 1) : "";
                source = sourceFileName + ":" + lineInfo;
            }
            codeTableModel.addRow(
                    new Object[] {instruction.address, instruction.instruction, instruction.symbol, source});

            if (highlightedCodeRow < 0 && matchesCurrentInstruction(instruction)) {
                highlightedCodeRow = i;
            }
        }

        if (codeTable != null) {
            if (highlightedCodeRow < 0) {
                highlightedCodeRow = findBestRowForCurrentSource();
            }
            selectAndScrollCodeRow(highlightedCodeRow);
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
        highlightedCodeRow = findBestRowForCurrentSource();
        selectAndScrollCodeRow(highlightedCodeRow);
        codeTable.repaint();
    }

    private int findBestRowForCurrentSource() {
        if (currentSourceLine < 0) {
            return -1;
        }
        for (int row = 0; row < codeTableModel.getRowCount(); row++) {
            Object sourceValue = codeTableModel.getValueAt(row, 3);
            Integer line = parseSourceLine(sourceValue != null ? sourceValue.toString() : null);
            if (line != null && line == currentSourceLine) {
                return row;
            }
        }
        return -1;
    }

    private void selectAndScrollCodeRow(int row) {
        if (codeTable == null) {
            return;
        }
        if (row < 0 || row >= codeTable.getRowCount()) {
            codeTable.clearSelection();
            return;
        }

        codeTable.setRowSelectionInterval(row, row);
        SwingUtilities.invokeLater(() -> {
            Rectangle rectangle = codeTable.getCellRect(row, 0, true);
            codeTable.scrollRectToVisible(rectangle);
        });
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
            seeValueLabel.setVisible(false);
            return;
        }

        if (row >= currentVariables.size()) {
            variableDataTextArea.setText("");
            seeValueButton.setEnabled(false);
            seeValueLabel.setVisible(false);
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
        seeValueLabel.setVisible(isLazy);
        seeValueButton.setEnabled(isLazy && expression != null && variableWatchListener != null);

        variableDataTextArea.setText(displayedValue);
        variableDataTextArea.setCaretPosition(0);
    }

    public void setSeeValueHandler(VariableWatchListener variableWatchListener) {
        this.variableWatchListener = variableWatchListener;
        updateVariableDataPaneFromSelection();
    }

    public void setDebugControlsHandler(DebugControlsListener debugControlsListener) {
        this.debugControlsListener = debugControlsListener;
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
        if (row < 0 || row >= currentVariables.size() || variableWatchListener == null) {
            return;
        }
        String expression = getExpression(currentVariables.get(row));
        if (expression == null) {
            return;
        }
        variableWatchListener.onSeeValueRequested(expression);
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
        if (SCOPE_ALLOCATED_STRINGS.equals(scope)) {
            for (Variable variable : variables) {
                model.addRow(new Object[] {
                    variable.name, variable.evaluateName != null ? variable.evaluateName : "",
                });
            }
        } else {
            for (Variable variable : variables) {
                model.addRow(new Object[] {
                    variable.name,
                    variable.value,
                    variable.type,
                    variable.evaluateName != null ? variable.evaluateName : ""
                });
            }
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
            if (variables[0] == null || variables[1] == null) {
                return SCOPE_HEAP;
            }
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
            insertPlaceholderRow(codeTableModel, scope, rangeLabel, new Object[] {rangeLabel, "[ERROR]", detail, ""});
            return;
        }

        if (SCOPE_CALL_STACK.equals(scope)) {
            callStackStatusLabel.setText(detail);
            appendPlaceholderRow(callStackTableModel, new Object[] {"[ERROR] " + rangeLabel, detail, 0});
            return;
        }

        if (SCOPE_HEAP.equals(scope)) {
            heapStatusLabel.setText(detail);
            insertPlaceholderRow(heapTableModel, scope, rangeLabel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_STACK.equals(scope)) {
            stackStatusLabel.setText(detail);
            insertPlaceholderRow(stackTableModel, scope, rangeLabel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_TEMP.equals(scope)) {
            tempStatusLabel.setText(detail);
            insertPlaceholderRow(tempTableModel, scope, rangeLabel, new Object[] {rangeLabel, "[ERROR]", "", detail});
            return;
        }

        if (SCOPE_ALLOCATED_STRINGS.equals(scope)) {
            allocatedStringsStatusLabel.setText(detail);
            insertPlaceholderRow(allocatedStringsTableModel, scope, rangeLabel, new Object[] {rangeLabel, detail});
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

    private void insertPlaceholderRow(DefaultTableModel model, String scope, String rangeLabel, Object[] row) {
        if (model == null) {
            return;
        }
        Integer rangeStart = parseRangeStart(rangeLabel);
        if (rangeStart == null) {
            model.addRow(row);
            return;
        }

        int insertAt = model.getRowCount();
        for (int i = 0; i < model.getRowCount(); i++) {
            Integer rowRangeStart = parseRangeStart(String.valueOf(model.getValueAt(i, 0)));
            Integer rowAddress = parseAddressCell(model, i, scope);
            Integer key = rowRangeStart != null ? rowRangeStart : rowAddress;
            if (key == null) {
                continue;
            }

            if (SCOPE_STACK.equals(scope)) {
                if (rangeStart > key) {
                    insertAt = i;
                    break;
                }
            } else {
                if (rangeStart < key) {
                    insertAt = i;
                    break;
                }
            }
        }
        model.insertRow(insertAt, row);
    }

    private Integer parseRangeStart(String label) {
        if (label == null) {
            return null;
        }
        Matcher matcher = RANGE_PATTERN.matcher(label);
        if (!matcher.find()) {
            return null;
        }
        String text = matcher.group(1);
        int dash = text.indexOf('-');
        if (dash < 0) {
            return null;
        }
        try {
            return Integer.parseInt(text.substring(0, dash).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseAddressCell(DefaultTableModel model, int row, String scope) {
        int column = 0;
        if (row < 0 || row >= model.getRowCount()) {
            return null;
        }
        Object value = model.getValueAt(row, column);
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (text.contains("[")) {
            return parseRangeStart(text);
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
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
        playPauseButton.setIcon(createImageIcon(running ? ICON_PAUSE : ICON_PLAY));
        stepButton.setEnabled(!running);
        stepOverButton.setEnabled(!running);
        stepOutButton.setEnabled(!running);
    }
}
