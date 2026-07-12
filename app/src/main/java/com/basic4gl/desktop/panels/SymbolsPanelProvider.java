package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.language.SymbolIndexer;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.language.FunctionDefinition;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import com.basic4gl.desktop.spi.language.LabelDefinition;
import com.basic4gl.desktop.spi.language.VariableDefinition;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.Theme.ICON_MENU_FUNCTIONS;
import static com.basic4gl.desktop.Theme.ICON_MENU_HELP;
import static com.basic4gl.desktop.Theme.ICON_STRUCT;
import static com.basic4gl.desktop.util.HtmlUtil.escapeHtml;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSABLE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK;

public class SymbolsPanelProvider implements IEditorPanelProvider {

    private static final String REFERENCE_NO_MATCHES_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>No matches.</body></html>";
    private static final String REFERENCE_SELECT_PROMPT_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>Select an entry.</body></html>";
    private String referenceDetailsHtml = REFERENCE_SELECT_PROMPT_HTML;
    private final JComboBox<String> referenceLibraryFilter = new JComboBox<>(new String[] {"All libraries"});
    private final JButton referenceFiltersButton = new JButton("Filters");
    private final JPopupMenu referenceFiltersPopup = new JPopupMenu();
    private final JTextPane referenceDetailsPane = new JTextPane();
    private final JButton referenceInsertButton = new JButton("Insert");
    private final javax.swing.Timer referenceFilterDebounceTimer =
            new javax.swing.Timer(120, e -> filterReferenceItems());

    private SymbolIndexer symbolIndexer;
    private final java.util.List<ReferenceItem> allReferenceItems = new ArrayList<>();
    private final DefaultListModel<ReferenceItem> referenceListModel = new DefaultListModel<>();
    private final JList<ReferenceItem> referenceList = new JList<>(referenceListModel);
    private final JTextField referenceSearchField = new JTextField();
    private final JComboBox<String> referenceKindFilter =
            new JComboBox<>(new String[] {"All", "Functions", "Constants", "Labels", "Variables", "Structs"});
    private final JComboBox<String> referenceSourceFilter =
            new JComboBox<>(new String[] {"All sources", "Builtin", "Libraries", "Program"});


    private int lastProgramSymbolsFingerprint = Integer.MIN_VALUE;
    private boolean updatingReferenceFilters = false;

    private PluginContext context;

    private static final class ReferenceItem {
        final String kind;
        final String name;
        final String signature;
        final String library;
        final String details;
        final String insertText;
        final int caretOffset;

        ReferenceItem(
                String kind,
                String name,
                String signature,
                String library,
                String details,
                String insertText,
                int caretOffset) {
            this.kind = kind;
            this.name = name;
            this.signature = signature;
            this.library = library;
            this.details = details;
            this.insertText = insertText;
            this.caretOffset = caretOffset;
        }

        @Override
        public String toString() {
            return signature;
        }
    }


    @Override
    public String id() {
        return "symbols";
    }

    @Override
    public String getTitle() {
        return "View Symbols";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_FUNCTIONS;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.WEST;
    }

    public JPanel build(PluginContext context) {
        this.context = context;

        symbolIndexer =
                new SymbolIndexer(context.currentEditor().getLanguage(), context.commands()::collectAllSourceText, this::updateProgramSymbols);
        JPanel lookupPanel = new JPanel(new BorderLayout(6, 6));
        JPanel lookupHeader = new JPanel(new BorderLayout(6, 6));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        referenceFiltersButton.setFocusable(false);
        referenceFiltersButton.setToolTipText("Open reference filters");
        leftHeader.add(referenceFiltersButton);
        lookupHeader.add(leftHeader, BorderLayout.WEST);

        lookupHeader.add(referenceSearchField, BorderLayout.CENTER);
        referenceInsertButton.setFocusable(false);
        referenceInsertButton.setEnabled(false);
        lookupHeader.add(referenceInsertButton, BorderLayout.EAST);

        referenceSearchField.setToolTipText("Search by name, signature, or library");
        referenceKindFilter.setToolTipText("Filter by kind");
        referenceSourceFilter.setToolTipText("Filter by builtin, libraries, or program symbols");
        referenceLibraryFilter.setToolTipText("Filter by library name");
        referenceKindFilter.setPrototypeDisplayValue("Functions");
        rebuildReferenceFiltersPopup();

        referenceFilterDebounceTimer.setRepeats(false);

        referenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        referenceList.setFixedCellHeight(20);
        referenceList.setPrototypeCellValue(
                new SymbolsPanelProvider.ReferenceItem("function", "prototype", "prototype(symbol, arg)", "Builtin", "", "", 0));
        referenceList.setCellRenderer(new DefaultListCellRenderer() {
            private final ImageIcon functionIcon = createImageIcon(ICON_FUNCTION);
            private final ImageIcon variableIcon = createImageIcon(ICON_VARIABLE);
            private final ImageIcon labelIcon = createImageIcon(ICON_LABEL);
            private final ImageIcon structIcon = createImageIcon(ICON_STRUCT);

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SymbolsPanelProvider.ReferenceItem item) {
                    label.setText(item.signature);
                    if ("function".equals(item.kind) || "userfunc".equals(item.kind)) {
                        label.setIcon(functionIcon);
                    } else if ("label".equals(item.kind)) {
                        label.setIcon(labelIcon);
                    } else if ("struc".equals(item.kind)) {
                        label.setIcon(structIcon);
                    } else {
                        label.setIcon(variableIcon);
                    }
                    label.setToolTipText(null);
                }
                return label;
            }
        });

        referenceDetailsPane.setEditable(false);
        referenceDetailsPane.setContentType("text/html");
        setReferenceDetailsHtml(REFERENCE_SELECT_PROMPT_HTML);

        JSplitPane lookupSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        lookupSplit.setResizeWeight(0.65);
        lookupSplit.setTopComponent(new JScrollPane(referenceList));
        lookupSplit.setBottomComponent(new JScrollPane(referenceDetailsPane));

        lookupPanel.add(lookupHeader, BorderLayout.NORTH);
        lookupPanel.add(lookupSplit, BorderLayout.CENTER);

        referenceSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }
        });
        referenceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateReferenceSelectionDetails();
            }
        });
        referenceKindFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceSourceFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceLibraryFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceFiltersButton.addActionListener(e -> {
            rebuildReferenceFiltersPopup();
            referenceFiltersPopup.show(referenceFiltersButton, 0, referenceFiltersButton.getHeight());
        });
        referenceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    insertSelectedReference();
                }
            }
        });
        referenceInsertButton.addActionListener(e -> insertSelectedReference());
        updateReferenceFiltersButtonTooltip();

        return lookupPanel;
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {
        populateDocsFromCompiler();
        symbolIndexer.schedule();
    }

    @Override
    public void onFileModified(String filePath) {
        symbolIndexer.schedule();
    }

    @Override
    public void dispose() {
        symbolIndexer.shutdown();
    }

    @Override
    public void onCompileSucceeded() {

        populateDocsFromCompiler();
        // Also sync the indexer immediately so the debounced background pass
        // reflects the compiled state right away.
        symbolIndexer.indexNow();
    }

    /**
     * Called on the EDT by the {@link SymbolIndexer} callback after each debounce cycle.
     * Replaces all "Program" (user-defined) reference items with the freshly scanned symbols and
     * refreshes the reference panel.
     */
    private void updateProgramSymbols(List<IndexedSymbol> symbols) {
        int fingerprint = 1;
        for (IndexedSymbol symbol : symbols) {
            fingerprint = 31 * fingerprint + Objects.hash(symbol.kind(), symbol.name(), symbol.signature());
        }
        if (fingerprint == lastProgramSymbolsFingerprint) {
            return;
        }
        lastProgramSymbolsFingerprint = fingerprint;

        // Remove all existing Program-sourced items
        allReferenceItems.removeIf(item -> "Program".equals(item.library));

        // Add newly scanned symbols
        for (IndexedSymbol sym : symbols) {
            String details;
            String insertText;
            int caretOffset;
            switch (sym.kind()) {
                case "userfunc" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> User Function"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name() + "()";
                    caretOffset = sym.name().length() + 1;
                }
                case "label" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Label"
                            + "<br/><b>Usage:</b> <code>gosub " + escapeHtml(sym.name()) + "</code>"
                            + " / <code>goto " + escapeHtml(sym.name()) + "</code></p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                case "struc" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Struct"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                default -> { // "variable"
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Variable"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
            }
            allReferenceItems.add(new ReferenceItem(
                    sym.kind(), sym.name(), sym.signature(), "Program", details, insertText, caretOffset));
        }

        allReferenceItems.sort(Comparator.comparing((ReferenceItem item) -> item.name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> item.kind));
        rebuildLibraryFilterOptions();
        filterReferenceItems();

        //TODO handle this or not: refreshAssetsLibrary();
    }

    private void populateDocsFromCompiler() {

        if (context.currentEditor() == null || context.currentEditor().getLanguage() == null) {
            return;
        }
        allReferenceItems.clear();
        allReferenceItems.addAll(buildFunctionReferenceItems(context.currentEditor().getLanguage()));
        allReferenceItems.addAll(buildConstantReferenceItems(context.currentEditor().getLanguage()));
        allReferenceItems.addAll(buildLabelReferenceItems(context.currentEditor().getLanguage()));
        allReferenceItems.addAll(buildVariableReferenceItems(context.currentEditor().getLanguage()));
        allReferenceItems.sort(Comparator.comparing((ReferenceItem item) -> item.name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> item.kind));
        rebuildLibraryFilterOptions();
        filterReferenceItems();
    }

    private java.util.List<ReferenceItem> buildFunctionReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (FunctionDefinition item : comp.getFunctionDefinitions()) {
            if (item == null) {
                continue;
            }
            StringBuilder argsOnly = new StringBuilder();
            if (item.parameters() != null) {
                for (VariableDefinition arg : item.parameters()) {
                    if (argsOnly.length() > 0) {
                        argsOnly.append(", ");
                    }
                    argsOnly.append(arg.signature());
                }
            }
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>"
                    + escapeHtml(item.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Function<br/><b>Library:</b> "
                    + escapeHtml(item.packageName())
                    + "</p><p style='margin:0;'>"
                    + escapeHtml(item.signature())
                    + "</p></body></html>";
            String insertText = item.hasBrackets() ? item.name() + "()" : item.name() + " ";
            int caretOffset = item.hasBrackets() ? item.name().length() + 1 : insertText.length();
            if (item.hasBrackets() && argsOnly.length() > 0) {
                insertText = item.name() + "(" + argsOnly + ")";
                caretOffset = item.name().length() + 1;
            }
            items.add(new ReferenceItem(
                    "function", item.name(), item.signature(), item.packageName(), details, insertText, caretOffset));
        }
        return items;
    }

    private java.util.List<ReferenceItem> buildConstantReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (VariableDefinition item : comp.getConstantDefinitions()) {
            if (item == null) {
                continue;
            }
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>"
                    + escapeHtml(item.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Constant<br/><b>Library:</b> "
                    + escapeHtml(item.packageName())
                    + "</p><p style='margin:0;'>"
                    + escapeHtml(item.signature())
                    + "</p></body></html>";
            items.add(new ReferenceItem(
                    "constant",
                    item.name(),
                    item.signature(),
                    item.packageName(),
                    details,
                    item.name(),
                    item.name().length()));
        }

        return items;
    }

    private java.util.List<ReferenceItem> buildLabelReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (LabelDefinition label : comp.getLabelDefinitions()) {
            if (label == null) {
                continue;
            }
            String signature = label.signature();
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(label.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Label<br/><b>Usage:</b> "
                    + "<code>" + escapeHtml(label.usage()) + "</code>"
                    + "</p></body></html>";
            items.add(new ReferenceItem(
                    "label",
                    label.name(),
                    signature,
                    "Program",
                    details,
                    label.name(),
                    label.name().length()));
        }
        return items;
    }

    private java.util.List<ReferenceItem> buildVariableReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (VariableDefinition variable : comp.getVariableDefinitions()) {
            if (variable == null || variable.name() == null || variable.name().isEmpty()) {
                continue;
            }
            String typeStr = variable.type().name();
            String signature = variable.signature();
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(variable.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Variable<br/><b>Data type:</b> "
                    + escapeHtml(typeStr) + "<br/><b>Source:</b> Program</p></body></html>";
            items.add(new ReferenceItem(
                    "variable",
                    variable.name(),
                    signature,
                    "Program",
                    details,
                    variable.name(),
                    variable.name().length()));
        }
        return items;
    }

    private void rebuildLibraryFilterOptions() {
        String selected = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");
        Set<String> libraries = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ReferenceItem item : allReferenceItems) {
            if (item.library != null) {
                libraries.add(item.library);
            }
        }

        updatingReferenceFilters = true;
        try {
            referenceLibraryFilter.removeAllItems();
            referenceLibraryFilter.addItem("All libraries");
            for (String library : libraries) {
                referenceLibraryFilter.addItem(library);
            }
            referenceLibraryFilter.setSelectedItem(libraries.contains(selected) ? selected : "All libraries");
        } finally {
            updatingReferenceFilters = false;
        }
        rebuildReferenceFiltersPopup();
        updateReferenceFiltersButtonTooltip();
    }

    private void rebuildReferenceFiltersPopup() {
        referenceFiltersPopup.removeAll();

        JMenu typeMenu = new JMenu("Type");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "All", "All");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Functions", "Functions");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Constants", "Constants");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Labels", "Labels");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Variables", "Variables");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Structs", "Structs");

        JMenu sourceMenu = new JMenu("Source");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "All sources", "All sources");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Builtin", "Builtin");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Libraries", "Libraries");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Program", "Program");

        JMenu libraryMenu = new JMenu("Library");
        for (int i = 0; i < referenceLibraryFilter.getItemCount(); i++) {
            String item = referenceLibraryFilter.getItemAt(i);
            if (item != null) {
                addReferenceRadioItems(libraryMenu, referenceLibraryFilter, item, item);
            }
        }

        JMenuItem resetItem = new JMenuItem("Reset filters");
        resetItem.addActionListener(e -> {
            updatingReferenceFilters = true;
            try {
                referenceKindFilter.setSelectedItem("All");
                referenceSourceFilter.setSelectedItem("All sources");
                referenceLibraryFilter.setSelectedItem("All libraries");
            } finally {
                updatingReferenceFilters = false;
            }
            updateReferenceFiltersButtonTooltip();
            filterReferenceItems();
        });

        referenceFiltersPopup.add(typeMenu);
        referenceFiltersPopup.add(sourceMenu);
        referenceFiltersPopup.add(libraryMenu);
        referenceFiltersPopup.addSeparator();
        referenceFiltersPopup.add(resetItem);
    }

    private void addReferenceRadioItems(JMenu menu, JComboBox<String> combo, String label, String value) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label, Objects.equals(combo.getSelectedItem(), value));
        item.addActionListener(e -> combo.setSelectedItem(value));
        menu.add(item);
    }

    private void setReferenceDetailsHtml(String html) {
        String next = html == null ? REFERENCE_SELECT_PROMPT_HTML : html;
        if (Objects.equals(referenceDetailsHtml, next)) {
            return;
        }
        referenceDetailsHtml = next;
        referenceDetailsPane.setText(next);
        referenceDetailsPane.setCaretPosition(0);
    }

    private void updateReferenceFiltersButtonTooltip() {
        String type = Objects.toString(referenceKindFilter.getSelectedItem(), "All");
        String source = Objects.toString(referenceSourceFilter.getSelectedItem(), "All sources");
        String library = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");
        referenceFiltersButton.setToolTipText("Type: " + type + " | Source: " + source + " | Library: " + library);
    }

    private void requestFilterReferenceItems() {
        referenceFilterDebounceTimer.restart();
    }

    private void filterReferenceItems() {
        String query = referenceSearchField.getText();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selectedKind = Objects.toString(referenceKindFilter.getSelectedItem(), "All");
        String selectedSource = Objects.toString(referenceSourceFilter.getSelectedItem(), "All sources");
        String selectedLibrary = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");

        ReferenceItem previousSelection = referenceList.getSelectedValue();
        java.util.List<ReferenceItem> matches = new ArrayList<>();
        for (ReferenceItem item : allReferenceItems) {
            boolean kindMatches = "All".equals(selectedKind)
                    || ("Functions".equals(selectedKind)
                    && ("function".equals(item.kind) || "userfunc".equals(item.kind)))
                    || ("Constants".equals(selectedKind) && "constant".equals(item.kind))
                    || ("Labels".equals(selectedKind) && "label".equals(item.kind))
                    || ("Variables".equals(selectedKind) && "variable".equals(item.kind))
                    || ("Structs".equals(selectedKind) && "struc".equals(item.kind));
            boolean sourceMatches = "All sources".equals(selectedSource)
                    || ("Builtin".equals(selectedSource)
                    && item.library != null
                    && "Builtin".equalsIgnoreCase(item.library))
                    || ("Libraries".equals(selectedSource)
                    && item.library != null
                    && !"Builtin".equalsIgnoreCase(item.library)
                    && !"Program".equalsIgnoreCase(item.library))
                    || ("Program".equals(selectedSource)
                    && item.library != null
                    && "Program".equalsIgnoreCase(item.library));
            boolean libraryMatches = "All libraries".equals(selectedLibrary)
                    || (item.library != null && selectedLibrary.equals(item.library));
            if (needle.isEmpty()
                    || item.name.toLowerCase(Locale.ROOT).contains(needle)
                    || item.signature.toLowerCase(Locale.ROOT).contains(needle)
                    || item.kind.toLowerCase(Locale.ROOT).contains(needle)
                    || (item.library != null
                    && item.library.toLowerCase(Locale.ROOT).contains(needle))) {
                if (kindMatches && sourceMatches && libraryMatches) {
                    matches.add(item);
                }
            }
        }

        referenceListModel.clear();
        for (ReferenceItem match : matches) {
            referenceListModel.addElement(match);
        }

        if (!referenceListModel.isEmpty()) {
            if (previousSelection != null && matches.contains(previousSelection)) {
                referenceList.setSelectedValue(previousSelection, true);
            } else {
                referenceList.setSelectedIndex(0);
            }
        } else {
            setReferenceDetailsHtml(REFERENCE_NO_MATCHES_HTML);
            referenceInsertButton.setEnabled(false);
        }
    }

    private void updateReferenceSelectionDetails() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            setReferenceDetailsHtml(REFERENCE_SELECT_PROMPT_HTML);
            referenceInsertButton.setEnabled(false);
            return;
        }
        setReferenceDetailsHtml(item.details);
        referenceInsertButton.setEnabled(true);
    }

    private void insertSelectedReference() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            return;
        }
        context.commands().insertText(item.insertText, item.caretOffset);
    }


}
