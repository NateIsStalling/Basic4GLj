package com.basic4gl.desktop.panels;

import static com.basic4gl.desktop.Theme.ICON_CHEVRON_DOWN;
import static com.basic4gl.desktop.Theme.ICON_DOCUMENT;
import static com.basic4gl.desktop.Theme.ICON_MENU_DOCS;
import static com.basic4gl.desktop.Theme.ICON_MENU_DOCS_SOLID;
import static com.basic4gl.desktop.Theme.ICON_SEARCH;
import static com.basic4gl.desktop.Theme.ICON_TEMPLATE;
import static com.basic4gl.desktop.util.HtmlUtil.escapeHtml;
import static com.basic4gl.desktop.util.SwingIconUtil.createScaledIcon;
import static com.basic4gl.desktop.util.SwingUtil.configureSmoothScrolling;
import static com.basic4gl.desktop.util.SwingUtil.createLighterPanelBackground;
import static com.basic4gl.desktop.util.SwingUtil.hideSplitPaneHandle;

import com.basic4gl.desktop.BasicEditor;
import com.basic4gl.desktop.content.catalog.ContentBrowseNode;
import com.basic4gl.desktop.content.catalog.ContentCatalogListener;
import com.basic4gl.desktop.content.catalog.ContentPanelItem;
import com.basic4gl.desktop.content.catalog.ContentPanelModel;
import com.basic4gl.desktop.content.catalog.ContentScope;
import com.basic4gl.desktop.content.catalog.ContentSelectionSummary;
import com.basic4gl.desktop.content.catalog.TemplateCatalogEntry;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.RoundedCardPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DocsPanelProvider implements IEditorPanelProvider {

    private static final Dimension HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);

    private final ContentPanelModel contentModel = new ContentPanelModel();
    private final JButton scopeButton = new JButton();
    private final JTextField searchField = new JTextField();
    private final JTree browseTree = new JTree();
    private final DefaultListModel<ContentPanelItem> searchListModel = new DefaultListModel<>();
    private final JList<ContentPanelItem> searchList = new JList<>(searchListModel);
    private final JPanel resultsCards = new JPanel(new CardLayout());
    private final JTextPane summaryPane = new JTextPane();
    private final JLabel selectionNameLabel = new JLabel("Select an item.");
    private final JButton primaryAction = new JButton("Open");
    private final Icon documentRowIcon = createScaledIcon(ICON_DOCUMENT, 18);
    private final Icon templateRowIcon = createScaledIcon(ICON_TEMPLATE, 18);
    private final ContentCatalogListener catalogListener = this::refreshContent;

    private BasicEditor editor;
    private ContentPanelItem selectedItem;
    private ContentScope currentScope = ContentScope.ALL;

    @Override
    public String id() {
        return "docs";
    }

    @Override
    public String getTitle() {
        return "Documentation";
    }

    @Override
    public String getActiveIconPath() {
        return ICON_MENU_DOCS_SOLID;
    }

    @Override
    public String getInactiveIconPath() {
        return ICON_MENU_DOCS;
    }

    @Override
    public Color getActiveIconTint() {
        return null;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.EAST;
    }

    @Override
    public JPanel build(PluginContext context) {
        if (context instanceof BasicEditor basicEditor) {
            this.editor = basicEditor;
            this.editor.contentCatalog().addListener(catalogListener);
        }

        JPanel panelCardHost = new JPanel(new CardLayout());
        JPanel lookupPanel = new JPanel(new BorderLayout(6, 6));
        Color panelBackground = createLighterPanelBackground();
        panelCardHost.setBackground(panelBackground);
        lookupPanel.setBackground(panelBackground);

        configureScopeButton();
        JToggleButton searchToggle = createHeaderSearchToggleButton();
        JPanel lookupHeader = new JPanel();
        lookupHeader.setBackground(panelBackground);
        lookupHeader.setLayout(new BoxLayout(lookupHeader, BoxLayout.X_AXIS));
        lookupHeader.add(scopeButton);
        lookupHeader.add(Box.createHorizontalGlue());
        lookupHeader.add(searchToggle);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        searchField.setToolTipText("Search documentation and samples");
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        searchToggle.addActionListener(e -> {
            boolean visible = searchToggle.isSelected();
            searchBar.setVisible(visible);
            if (visible) {
                searchField.requestFocusInWindow();
            }
            lookupPanel.revalidate();
            lookupPanel.repaint();
        });

        configureBrowseTree(panelBackground);
        configureSearchList(panelBackground);
        JScrollPane browseScrollPane = new JScrollPane(browseTree);
        JScrollPane searchScrollPane = new JScrollPane(searchList);
        browseScrollPane.setBorder(null);
        searchScrollPane.setBorder(null);
        configureSmoothScrolling(browseScrollPane);
        configureSmoothScrolling(searchScrollPane);
        resultsCards.add(browseScrollPane, "browse");
        resultsCards.add(searchScrollPane, "search");

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 6));
        resultsPanel.setBackground(panelBackground);
        JPanel resultsHeader = new JPanel(new BorderLayout(0, 6));
        resultsHeader.setOpaque(false);
        resultsHeader.add(lookupHeader, BorderLayout.NORTH);
        resultsHeader.add(searchBar, BorderLayout.SOUTH);
        resultsPanel.add(resultsHeader, BorderLayout.NORTH);
        resultsPanel.add(resultsCards, BorderLayout.CENTER);

        configureSummary(panelBackground);
        JScrollPane detailsScrollPane = new JScrollPane(createDetailsPanel(panelBackground));
        detailsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        detailsScrollPane.setBorder(null);
        configureSmoothScrolling(detailsScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.66);
        splitPane.setBorder(null);
        hideSplitPaneHandle(splitPane);
        splitPane.putClientProperty("JComponent.style", "showGrip: false; gripColor: #00000000;");
        splitPane.putClientProperty("JSplitPane.style", "plain");
        splitPane.setTopComponent(createRoundedCardHost(resultsPanel, panelBackground, "docs-results"));
        splitPane.setBottomComponent(createRoundedCardHost(detailsScrollPane, panelBackground, "docs-details"));
        lookupPanel.add(splitPane, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshContent();
            }
        });

        refreshContent();
        panelCardHost.add(lookupPanel, "main");
        ((CardLayout) panelCardHost.getLayout()).show(panelCardHost, "main");
        return panelCardHost;
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {
        refreshContent();
    }

    @Override
    public void onFileModified(String filePath) {}

    @Override
    public void dispose() {
        if (editor != null) {
            editor.contentCatalog().removeListener(catalogListener);
        }
    }

    @Override
    public void onCompileSucceeded() {}

    private void configureScopeButton() {
        scopeButton.setFocusable(false);
        scopeButton.setIcon(createScaledIcon(ICON_CHEVRON_DOWN, 18));
        scopeButton.setHorizontalTextPosition(SwingConstants.LEFT);
        scopeButton.setIconTextGap(6);
        scopeButton.putClientProperty("JButton.buttonType", "toolBarButton");
        scopeButton.setOpaque(false);
        scopeButton.setMargin(new Insets(5, 8, 5, 8));
        Font baseFont = scopeButton.getFont();
        scopeButton.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        scopeButton.setForeground(new Color(0x5B717F));
        scopeButton.setToolTipText("Choose content scope");
        updateScopeButtonText();
        scopeButton.addActionListener(e -> createScopePopup().show(scopeButton, 0, scopeButton.getHeight()));
    }

    private JPopupMenu createScopePopup() {
        JPopupMenu popup = new JPopupMenu();
        for (ContentScope scope : availableScopes()) {
            JMenuItem item = new JMenuItem(scope.displayName());
            item.setEnabled(!scope.equals(currentScope));
            item.addActionListener(e -> {
                currentScope = scope;
                updateScopeButtonText();
                refreshContent();
            });
            popup.add(item);
        }
        return popup;
    }

    private void updateScopeButtonText() {
        scopeButton.setText(currentScope.displayName());
    }

    private List<ContentScope> availableScopes() {
        if (editor == null) {
            return List.of(ContentScope.ALL);
        }
        return contentModel.scopes(editor.contentCatalog());
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

    private void configureBrowseTree(Color panelBackground) {
        browseTree.setBackground(panelBackground);
        browseTree.setRootVisible(false);
        browseTree.setShowsRootHandles(true);
        browseTree.setRowHeight(20);
        browseTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean selected,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {
                JLabel label = (JLabel)
                        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                Object userObject = value instanceof DefaultMutableTreeNode treeNode ? treeNode.getUserObject() : value;
                if (userObject instanceof ContentPanelItem item) {
                    label.setText(item.displayName());
                    label.setIcon(rowIcon(item));
                    label.setToolTipText(itemTooltip(item));
                } else {
                    label.setText(String.valueOf(userObject));
                    label.setToolTipText(null);
                }
                return label;
            }
        });
        browseTree.addTreeSelectionListener(e -> {
            Object selectedNode = browseTree.getLastSelectedPathComponent();
            if (!(selectedNode instanceof DefaultMutableTreeNode treeNode)) {
                return;
            }
            Object selected = treeNode.getUserObject();
            if (selected instanceof ContentPanelItem item) {
                selectItem(item);
            }
        });
        browseTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    runPrimaryAction();
                }
            }
        });
    }

    private void configureSearchList(Color panelBackground) {
        searchList.setBackground(panelBackground);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setFixedCellHeight(20);
        searchList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ContentPanelItem item) {
                    label.setText(item.displayName());
                    label.setIcon(rowIcon(item));
                    label.setToolTipText(itemTooltip(item));
                }
                return label;
            }
        });
        searchList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectItem(searchList.getSelectedValue());
            }
        });
        searchList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    runPrimaryAction();
                }
            }
        });
    }

    private Icon rowIcon(ContentPanelItem item) {
        return item.template() ? templateRowIcon : documentRowIcon;
    }

    private String itemTooltip(ContentPanelItem item) {
        String subtitle = item.displaySubtitle();
        return subtitle.isBlank()
                ? item.displayName()
                : "<html><b>" + escapeHtml(item.displayName()) + "</b><br>" + escapeHtml(subtitle) + "</html>";
    }

    private void configureSummary(Color panelBackground) {
        summaryPane.setEditable(false);
        summaryPane.setContentType("text/html");
        summaryPane.setBorder(null);
        summaryPane.setBackground(panelBackground);
        primaryAction.setEnabled(false);
        primaryAction.setFocusable(false);
        primaryAction.setMargin(new Insets(4, 8, 4, 8));
        primaryAction.setForeground(new Color(0x5B717F));
        primaryAction.addActionListener(e -> runPrimaryAction());
        Font nameFont = selectionNameLabel.getFont();
        selectionNameLabel.setFont(new Font(nameFont.getName(), Font.BOLD, nameFont.getSize()));
        int titleHeight = selectionNameLabel.getPreferredSize().height;
        selectionNameLabel.setMinimumSize(new Dimension(0, titleHeight));
        selectionNameLabel.setPreferredSize(new Dimension(0, titleHeight));
        setSelectionName("Select an item.");
    }

    private JPanel createDetailsPanel(Color panelBackground) {
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 0));
        detailsPanel.setBackground(panelBackground);
        JPanel detailsHeader = new JPanel(new BorderLayout(8, 0));
        detailsHeader.setBackground(panelBackground);
        detailsHeader.setOpaque(true);
        detailsHeader.setBorder(new EmptyBorder(6, 8, 4, 8));
        detailsHeader.add(selectionNameLabel, BorderLayout.CENTER);
        JPanel detailsActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        detailsActions.setOpaque(false);
        detailsActions.add(primaryAction);
        detailsHeader.add(detailsActions, BorderLayout.EAST);
        detailsPanel.add(detailsHeader, BorderLayout.NORTH);
        detailsPanel.add(summaryPane, BorderLayout.CENTER);
        return detailsPanel;
    }

    private void refreshContent() {
        if (editor == null) {
            setSummary(null);
            return;
        }
        ensureCurrentScopeAvailable();
        String query =
                searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isBlank()) {
            ContentBrowseNode root = browseForCurrentScope();
            DefaultMutableTreeNode rootNode = toTreeNode(root);
            browseTree.setModel(new DefaultTreeModel(rootNode));
            browseTree.expandPath(new TreePath(rootNode.getPath()));
            browseTree.expandRow(0);
            ((CardLayout) resultsCards.getLayout()).show(resultsCards, "browse");
        } else {
            searchListModel.clear();
            for (ContentPanelItem item : contentModel.items(editor.contentCatalog(), currentScope, query)) {
                searchListModel.addElement(item);
            }
            ((CardLayout) resultsCards.getLayout()).show(resultsCards, "search");
        }
        setSummary(selectedItem);
    }

    private ContentBrowseNode browseForCurrentScope() {
        return contentModel.browse(editor.contentCatalog(), currentScope, this::categoryPathForCurrentScope);
    }

    private List<String> categoryPathForCurrentScope(ContentPanelItem item) {
        List<String> categoryPath = item.categoryPath();
        if (currentScope.all() || categoryPath.isEmpty()) {
            return categoryPath;
        }
        return categoryPath.stream()
                .filter(segment -> !isRedundantScopeSegment(segment))
                .toList();
    }

    private boolean isRedundantScopeSegment(String segment) {
        String normalizedSegment = normalizeScopeText(segment);
        String normalizedTag = currentScope.tag();
        return normalizedTag != null
                && (normalizedSegment.equals(normalizedTag)
                        || normalizedSegment.equals(normalizedTag + "s")
                        || normalizedSegment.equals(normalizeScopeText(currentScope.displayName())));
    }

    private String normalizeScopeText(String text) {
        return text == null
                ? ""
                : text.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace('_', '-')
                        .replace(' ', '-');
    }

    private void ensureCurrentScopeAvailable() {
        if (currentScope.all() || availableScopes().contains(currentScope)) {
            updateScopeButtonText();
            return;
        }
        currentScope = ContentScope.ALL;
        updateScopeButtonText();
    }

    private DefaultMutableTreeNode toTreeNode(ContentBrowseNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node.name());
        for (ContentBrowseNode child : node.children()) {
            treeNode.add(toTreeNode(child));
        }
        for (ContentPanelItem item : node.items()) {
            treeNode.add(new DefaultMutableTreeNode(item));
        }
        return treeNode;
    }

    private void selectItem(ContentPanelItem item) {
        selectedItem = item;
        setSummary(item);
    }

    private void setSummary(ContentPanelItem item) {
        if (item == null) {
            setSelectionName("Select an item.");
            summaryPane.setText("<html><body style='font-family:sans-serif;'>Select an item.</body></html>");
            primaryAction.setText("Open");
            primaryAction.setEnabled(false);
            return;
        }
        ContentSelectionSummary summary = contentModel.summary(item);
        setSelectionName(summary.title());
        summaryPane.setText("<html><body style='font-family:sans-serif;'>"
                + "<p><b>" + escapeHtml(summary.kindLabel()) + "</b></p>"
                + "<p>" + escapeHtml(summary.description()) + "</p>"
                + "<p><b>Category:</b> " + escapeHtml(summary.category()) + "</p>"
                + relatedHtml(summary.relatedIds())
                + "</body></html>");
        summaryPane.setCaretPosition(0);
        primaryAction.setText(summary.primaryAction());
        primaryAction.setEnabled(true);
    }

    private void setSelectionName(String name) {
        String text = name == null ? "" : name;
        selectionNameLabel.setText(text);
        selectionNameLabel.setToolTipText(text.isBlank() ? null : text);
    }

    private String relatedHtml(List<String> relatedIds) {
        if (relatedIds == null || relatedIds.isEmpty()) {
            return "";
        }
        return "<p><b>Related:</b> " + escapeHtml(String.join(", ", relatedIds)) + "</p>";
    }

    private void runPrimaryAction() {
        if (editor == null || selectedItem == null) {
            return;
        }
        if (!selectedItem.template()) {
            editor.contentCatalog().documents().stream()
                    .filter(entry -> entry.globalId().value().equals(selectedItem.globalId()))
                    .findFirst()
                    .ifPresent(editor::openContentDocument);
            return;
        }
        editor.contentCatalog().templates().stream()
                .filter(entry -> entry.globalId().value().equals(selectedItem.globalId()))
                .findFirst()
                .ifPresent(this::instantiateTemplate);
    }

    private void instantiateTemplate(TemplateCatalogEntry entry) {
        JFileChooser chooser = new JFileChooser(editor.currentDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose destination folder");
        int result = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(primaryAction));
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        editor.instantiateTemplate(entry, destination, entry.descriptor().title());
    }
}
