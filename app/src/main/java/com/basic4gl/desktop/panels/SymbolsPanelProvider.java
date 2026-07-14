package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.language.SymbolIndexer;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.language.FunctionDefinition;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import com.basic4gl.desktop.spi.language.LabelDefinition;
import com.basic4gl.desktop.spi.language.VariableDefinition;
import com.basic4gl.desktop.util.RoundedCardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
import static com.basic4gl.desktop.util.SwingIconUtil.createScaledIcon;
import static com.basic4gl.desktop.util.SwingUtil.createLighterPanelBackground;
import static com.basic4gl.desktop.util.SwingUtil.hideSplitPaneHandle;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSABLE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK;

public class SymbolsPanelProvider implements IEditorPanelProvider {

    private static final String REFERENCE_NO_MATCHES_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>No matches.</body></html>";
    private static final String REFERENCE_SELECT_PROMPT_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>Select an entry.</body></html>";
    private String referenceDetailsHtml = REFERENCE_SELECT_PROMPT_HTML;
    private final JComboBox<String> referenceLibraryFilter = new JComboBox<>(new String[] {"All libraries"});
    private final JButton referenceFiltersButton = new JButton("All Symbols");
    private final JPopupMenu referenceFiltersPopup = new JPopupMenu();
    private final JLabel referenceSelectionNameLabel = new JLabel("Select an entry.");
    private String referenceSelectionName = "Select an entry.";
    private final JTextPane referenceDetailsPane = new JTextPane();
    private final JButton referenceCopyButton = new JButton("Copy");
    private final JButton referenceInsertButton = new JButton("Insert");
    private final javax.swing.Timer referenceFilterDebounceTimer =
            new javax.swing.Timer(120, e -> filterReferenceItems());

    private SymbolIndexer symbolIndexer;
    private final java.util.List<ReferenceItem> allReferenceItems = new ArrayList<>();
    private final DefaultListModel<ReferenceItem> referenceListModel = new DefaultListModel<>();
    private final JList<ReferenceItem> referenceList = new JList<>(referenceListModel);
    private final JTextField referenceSearchField = new JTextField();
    private final JComboBox<String> referenceKindFilter =
            new JComboBox<>(new String[] {"All Symbols", "Functions", "Constants", "Labels", "Variables", "Structs"});
    private final JComboBox<String> referenceSourceFilter =
            new JComboBox<>(new String[] {"All Sources", "Builtin", "Libraries", "Program"});
    private static final Dimension HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);
    private static final int CARD_ARC = 14;


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
    public String getActiveIconPath() {
        return ICON_MENU_FUNCTIONS;
    }

    @Override
    public String getInactiveIconPath() {
        return ICON_MENU_FUNCTIONS;
    }

    @Override
    public Color getActiveIconTint() {
        return null;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.EAST;
    }

    public JPanel build(PluginContext context) {
        this.context = context;

        symbolIndexer =
                new SymbolIndexer(context.currentEditor().getLanguageSupport(), context.commands()::collectAllSourceText, this::updateProgramSymbols);
        JPanel panelCardHost = new JPanel(new CardLayout());
        JPanel lookupPanel = new JPanel(new BorderLayout(6, 6));
        Color panelBackground = createLighterPanelBackground();
        lookupPanel.setBackground(panelBackground);
        JPanel lookupHeader = new JPanel();
        lookupHeader.setBackground(panelBackground);
        lookupHeader.setLayout(new BoxLayout(lookupHeader, BoxLayout.X_AXIS));

        referenceFiltersButton.setFocusable(false);
        referenceFiltersButton.setIcon(createScaledIcon(ICON_CHEVRON_DOWN, 18));
        referenceFiltersButton.setHorizontalTextPosition(SwingConstants.LEFT);
        referenceFiltersButton.setIconTextGap(6);
        referenceFiltersButton.putClientProperty("JButton.buttonType", "toolBarButton");
        referenceFiltersButton.setOpaque(false);
        referenceFiltersButton.setMargin(new Insets(5, 8, 5, 8));
        Font baseFont = referenceFiltersButton.getFont();
        referenceFiltersButton.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        referenceFiltersButton.setForeground(new Color(0x5B717F));
        referenceFiltersButton.setToolTipText("Open reference filters");
        lookupHeader.add(referenceFiltersButton);
        lookupHeader.add(Box.createHorizontalGlue());
        JToggleButton searchToggle = createHeaderSearchToggleButton();
        lookupHeader.add(searchToggle);

        referenceCopyButton.setFocusable(false);
        referenceCopyButton.setMargin(new Insets(4, 4, 4, 4));
        Font actionButtonFont = referenceCopyButton.getFont();
//        referenceCopyButton.setFont(new Font(actionButtonFont.getName(), Font.BOLD, actionButtonFont.getSize()));
        referenceCopyButton.setForeground(new Color(0x5B717F));
        referenceCopyButton.setEnabled(false);
        referenceInsertButton.setFocusable(false);
        referenceInsertButton.setMargin(new Insets(4, 4, 4, 4));
//        referenceInsertButton.setFont(new Font(actionButtonFont.getName(), Font.BOLD, actionButtonFont.getSize()));
        referenceInsertButton.setForeground(new Color(0x5B717F));
        referenceInsertButton.setEnabled(false);

        referenceSearchField.setToolTipText("Search by name, signature, or library");
        referenceKindFilter.setToolTipText("Filter by kind");
        referenceSourceFilter.setToolTipText("Filter by builtin, libraries, or program symbols");
        referenceLibraryFilter.setToolTipText("Filter by library name");
        referenceKindFilter.setPrototypeDisplayValue("Functions");
        rebuildReferenceFiltersPopup();

        referenceFilterDebounceTimer.setRepeats(false);

        referenceList.setBackground(panelBackground);
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
        referenceDetailsPane.setBorder(null);
        referenceDetailsPane.setBackground(panelBackground);
        setReferenceDetailsHtml(REFERENCE_SELECT_PROMPT_HTML);
        Font nameFont = referenceSelectionNameLabel.getFont();
        referenceSelectionNameLabel.setFont(new Font(nameFont.getName(), Font.BOLD, nameFont.getSize()));
        int titleHeight = referenceSelectionNameLabel.getPreferredSize().height;
        referenceSelectionNameLabel.setMinimumSize(new Dimension(0, titleHeight));
        referenceSelectionNameLabel.setPreferredSize(new Dimension(0, titleHeight));
        setReferenceSelectionName("Select an entry.");

        JSplitPane lookupSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        lookupSplit.setResizeWeight(0.65);
        hideSplitPaneHandle(lookupSplit);
        lookupSplit.putClientProperty("JComponent.style", "showGrip: false; gripColor: #00000000;");
        lookupSplit.putClientProperty("JSplitPane.style", "plain");
        JScrollPane listScrollPane = new JScrollPane(referenceList);
        listScrollPane.setBorder(null);
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 0));
        JPanel detailsHeader = new JPanel(new BorderLayout(8, 0));
        detailsHeader.setBackground(panelBackground);
        detailsHeader.setOpaque(true);
        detailsHeader.setBorder(new EmptyBorder(6, 8, 4, 8));
        detailsHeader.add(referenceSelectionNameLabel, BorderLayout.CENTER);
        JPanel detailsActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        detailsActions.setOpaque(false);
        detailsActions.add(referenceCopyButton);
        detailsActions.add(referenceInsertButton);
        detailsHeader.add(detailsActions, BorderLayout.EAST);
        detailsPanel.add(detailsHeader, BorderLayout.NORTH);
        detailsPanel.add(referenceDetailsPane, BorderLayout.CENTER);
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        detailsScrollPane.setBorder(null);
        lookupSplit.setBottomComponent(createRoundedCardHost(detailsScrollPane, panelBackground, "symbols-details"));

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        searchBar.add(referenceSearchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        searchToggle.addActionListener(e -> {
            boolean visible = searchToggle.isSelected();
            searchBar.setVisible(visible);
            if (visible) {
                referenceSearchField.requestFocusInWindow();
            }
            lookupPanel.revalidate();
            lookupPanel.repaint();
        });
        JPanel symbolsListPanel = new JPanel(new BorderLayout(0, 6));
        symbolsListPanel.setBackground(panelBackground);
        JPanel symbolsListHeader = new JPanel(new BorderLayout(0, 6));
        symbolsListHeader.setOpaque(false);
        symbolsListHeader.add(lookupHeader, BorderLayout.NORTH);
        symbolsListHeader.add(searchBar, BorderLayout.SOUTH);
        symbolsListPanel.add(symbolsListHeader, BorderLayout.NORTH);
        symbolsListPanel.add(listScrollPane, BorderLayout.CENTER);
        lookupSplit.setTopComponent(createRoundedCardHost(symbolsListPanel, panelBackground, "symbols-list"));

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
        referenceCopyButton.addActionListener(e -> copySelectedSymbolName());
        updateReferenceFiltersButtonTooltip();

        panelCardHost.add(lookupPanel, "main");
        ((CardLayout) panelCardHost.getLayout()).show(panelCardHost, "main");
        return panelCardHost;
    }

    private JToggleButton createHeaderSearchToggleButton() {
        JToggleButton button = new JToggleButton(createScaledIcon(ICON_SEARCH, 18));
        button.setToolTipText("Show search");
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setOpaque(false);
        button.setMargin(new Insets(6, 6, 6, 6));
        button.setPreferredSize(HEADER_ICON_BUTTON_SIZE);
        button.setMinimumSize(HEADER_ICON_BUTTON_SIZE);
        button.setMaximumSize(HEADER_ICON_BUTTON_SIZE);
        return button;
    }


    private JComponent createRoundedCardHost(JComponent content, Color panelBackground, String key) {
        Color cardBackground = createLighterPanelBackground();
//                new Color(
//                Math.min(255, panelBackground.getRed() + 10),
//                Math.min(255, panelBackground.getGreen() + 10),
//                Math.min(255, panelBackground.getBlue() + 10));
        JPanel card = new RoundedCardPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(cardBackground);
        card.setBorder(new EmptyBorder(4, 4, 4, 4));
        card.add(content, BorderLayout.CENTER);

        JPanel host = new JPanel(new CardLayout());
        host.setOpaque(false);
        host.add(card, key);
        ((CardLayout) host.getLayout()).show(host, key);
        return host;
    }




    @Override
    public void refresh(EditorPlugin languageProvider) {
        if (context == null) {
            return;
        }
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
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> User Function"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name() + "()";
                    caretOffset = sym.name().length() + 1;
                }
                case "label" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Label"
                            + "<br/><b>Usage:</b> <code>gosub " + escapeHtml(sym.name()) + "</code>"
                            + " / <code>goto " + escapeHtml(sym.name()) + "</code></p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                case "struc" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Struct"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                default -> { // "variable"
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
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
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>" +
                    "<p style='margin:0 0 4px 0;'><b>Type:</b> Function<br/><b>Library:</b> "
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
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>" +
                    "<p style='margin:0 0 4px 0;'><b>Type:</b> Constant<br/><b>Library:</b> "
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
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>" +
                    "<p style='margin:0 0 4px 0;'><b>Type:</b> Label<br/><b>Usage:</b> "
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
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>" +
                    "<p style='margin:0 0 4px 0;'><b>Type:</b> Variable<br/><b>Data type:</b> "
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
        String selected = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All Libraries");
        Set<String> libraries = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ReferenceItem item : allReferenceItems) {
            if (item.library != null) {
                libraries.add(item.library);
            }
        }

        updatingReferenceFilters = true;
        try {
            referenceLibraryFilter.removeAllItems();
            referenceLibraryFilter.addItem("All Libraries");
            for (String library : libraries) {
                referenceLibraryFilter.addItem(library);
            }
            referenceLibraryFilter.setSelectedItem(libraries.contains(selected) ? selected : "All Libraries");
        } finally {
            updatingReferenceFilters = false;
        }
        rebuildReferenceFiltersPopup();
        updateReferenceFiltersButtonTooltip();
    }

    private void rebuildReferenceFiltersPopup() {
        referenceFiltersPopup.removeAll();

        JMenu typeMenu = new JMenu("Type");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "All Symbols", "All Symbols");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Functions", "Functions");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Constants", "Constants");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Labels", "Labels");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Variables", "Variables");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Structs", "Structs");

        JMenu sourceMenu = new JMenu("Source");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "All Sources", "All Sources");
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
                referenceKindFilter.setSelectedItem("All Symbols");
                referenceSourceFilter.setSelectedItem("All Sources");
                referenceLibraryFilter.setSelectedItem("All Libraries");
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
        String type = Objects.toString(referenceKindFilter.getSelectedItem(), "All Symbols");
        String source = Objects.toString(referenceSourceFilter.getSelectedItem(), "All Sources");
        String library = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All Libraries");
        referenceFiltersButton.setText(type);
        referenceFiltersButton.setToolTipText("Type: " + type + " | Source: " + source + " | Library: " + library);
    }

    private void requestFilterReferenceItems() {
        referenceFilterDebounceTimer.restart();
    }

    private void filterReferenceItems() {
        String query = referenceSearchField.getText();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selectedKind = Objects.toString(referenceKindFilter.getSelectedItem(), "All");
        String selectedSource = Objects.toString(referenceSourceFilter.getSelectedItem(), "All Sources");
        String selectedLibrary = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");

        ReferenceItem previousSelection = referenceList.getSelectedValue();
        java.util.List<ReferenceItem> matches = new ArrayList<>();
        for (ReferenceItem item : allReferenceItems) {
            boolean kindMatches = "All Symbols".equals(selectedKind)
                    || ("Functions".equals(selectedKind)
                    && ("function".equals(item.kind) || "userfunc".equals(item.kind)))
                    || ("Constants".equals(selectedKind) && "constant".equals(item.kind))
                    || ("Labels".equals(selectedKind) && "label".equals(item.kind))
                    || ("Variables".equals(selectedKind) && "variable".equals(item.kind))
                    || ("Structs".equals(selectedKind) && "struc".equals(item.kind));
            boolean sourceMatches = "All Sources".equals(selectedSource)
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
            boolean libraryMatches = "All Libraries".equals(selectedLibrary)
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
            setReferenceSelectionName("No selection");
            setReferenceDetailsHtml(REFERENCE_NO_MATCHES_HTML);
            referenceCopyButton.setEnabled(false);
            referenceInsertButton.setEnabled(false);
        }
    }

    private void updateReferenceSelectionDetails() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            setReferenceSelectionName("Select an entry.");
            setReferenceDetailsHtml(REFERENCE_SELECT_PROMPT_HTML);
            referenceCopyButton.setEnabled(false);
            referenceInsertButton.setEnabled(false);
            return;
        }
        setReferenceSelectionName(item.name);
        setReferenceDetailsHtml(item.details);
        referenceCopyButton.setEnabled(true);
        referenceInsertButton.setEnabled(true);
    }

    private void insertSelectedReference() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            return;
        }
        context.commands().insertText(item.insertText, item.caretOffset);
    }

    private void copySelectedSymbolName() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null || item.name == null || item.name.isBlank()) {
            return;
        }
        StringSelection selection = new StringSelection(item.name);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    private void setReferenceSelectionName(String name) {
        referenceSelectionName = (name == null || name.isBlank()) ? "Select an entry." : name;
        referenceSelectionNameLabel.setText(referenceSelectionName);
        referenceSelectionNameLabel.setToolTipText(referenceSelectionName.isBlank() ? null : referenceSelectionName);
    }



}
