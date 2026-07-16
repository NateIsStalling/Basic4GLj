package com.basic4gl.desktop.panels;

import static com.basic4gl.desktop.Theme.ICON_MENU_ASSETS;
import static com.basic4gl.desktop.Theme.ICON_MENU_FOLDER;
import static com.basic4gl.desktop.Theme.ICON_REFRESH;
import static com.basic4gl.desktop.Theme.ICON_SEARCH;
import static com.basic4gl.desktop.Theme.ICON_VIEW_GRID;
import static com.basic4gl.desktop.Theme.ICON_VIEW_LIST;
import static com.basic4gl.desktop.util.FileUtil.*;
import static com.basic4gl.desktop.util.HtmlUtil.escapeHtml;
import static com.basic4gl.desktop.util.SwingIconUtil.*;
import static com.basic4gl.desktop.util.SwingUtil.configureSmoothScrolling;
import static com.basic4gl.desktop.util.SwingUtil.createLighterPanelBackground;

import com.basic4gl.desktop.content.FileEditor;
import com.basic4gl.desktop.content.FileManager;
import com.basic4gl.desktop.content.FileViewerFactory;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.FileUtil;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.RoundedCardPanel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AssetsPanelProvider implements IEditorPanelProvider {

    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private final JTree assetsTree = new JTree();
    private final DefaultListModel<AssetItem> assetsListModel = new DefaultListModel<>();
    private final JList<AssetItem> assetsGridList = new JList<>(assetsListModel);
    private final JTextField assetsSearchField = new JTextField();
    private final JPanel assetsContentPanel = new JPanel(new CardLayout());
    private final Map<String, Icon> assetThumbnailCache = new HashMap<>();
    private static final String LAYOUT_TREE = "Tree";
    private static final String LAYOUT_GRID = "Grid";
    private static final Dimension HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);
    private static final Dimension HEADER_LAYOUT_BUTTON_SIZE = new Dimension(34, 30);

    private FileManager fileManager;

    private PluginContext context;

    private static final class AssetItem {
        final String title;
        final String subtitle;
        final File file;
        final Icon icon;

        AssetItem(String title, String subtitle, File file, Icon icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.file = file;
            this.icon = icon;
        }

        boolean isOpenable() {
            return file != null && file.isFile();
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public AssetsPanelProvider(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public String id() {
        return "assets";
    }

    @Override
    public String getTitle() {
        return "Assets";
    }

    @Override
    public String getActiveIconPath() {
        return ICON_MENU_ASSETS;
    }

    @Override
    public String getInactiveIconPath() {
        return ICON_MENU_ASSETS;
    }

    @Override
    public Color getActiveIconTint() {
        return null;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.WEST;
    }

    @Override
    public JPanel build(PluginContext context) {
        this.context = context;

        JPanel panelCardHost = new JPanel(new CardLayout());
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        Color panelBackground = createLighterPanelBackground();
        panel.setBackground(panelBackground);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(panelBackground);
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerButtons.setOpaque(false);
        JLabel title = new JLabel("Assets");
        Font baseFont = title.getFont();
        title.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        title.setForeground(new Color(0x424242));
        title.setBorder(new EmptyBorder(0, 8, 0, 8));
        JPanel layoutTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        layoutTabs.setOpaque(false);
        ButtonGroup layoutButtons = new ButtonGroup();
        JToggleButton treeLayoutButton = createAssetsLayoutButton("List View", ICON_VIEW_LIST, LAYOUT_TREE, "first");
        JToggleButton gridLayoutButton = createAssetsLayoutButton("Grid View", ICON_VIEW_GRID, LAYOUT_GRID, "last");
        layoutButtons.add(treeLayoutButton);
        layoutButtons.add(gridLayoutButton);
        treeLayoutButton.setSelected(true);
        layoutTabs.add(treeLayoutButton);
        layoutTabs.add(gridLayoutButton);
        JToggleButton searchToggle = createHeaderSearchToggleButton();
        JButton refresh = createHeaderIconButton(ICON_REFRESH, "Refresh Assets");
        refresh.addActionListener(e -> refresh(context.currentEditor()));
        headerButtons.add(layoutTabs);
        headerButtons.add(searchToggle);
        headerButtons.add(refresh);
        header.add(title, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        assetsTree.setBackground(panelBackground);
        assetsTree.setBorder(null);
        assetsTree.setRootVisible(false);
        assetsTree.setShowsRootHandles(true);
        // Let Swing compute preferred row height so custom/HTML labels do not clip.
        assetsTree.setRowHeight(0);
        assetsTree.setCellRenderer(new DefaultTreeCellRenderer() {
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
                if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof AssetItem item) {
                    boolean isSection = item.file == null;
                    label.setIcon(item.icon);
                    label.setIconTextGap(8);
                    label.setBorder(new EmptyBorder(3, 0, 3, 0));
                    label.setText(formatAssetTreeLabel(item, isSection));
                    label.setIcon(item.icon);
                    label.setToolTipText(item.file != null ? item.file.getAbsolutePath() : item.subtitle);
                }
                return label;
            }
        });

        assetsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                maybeShowAssetsTreePopup(e);
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = assetsTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!(userObject instanceof AssetItem item) || !item.isOpenable()) {
                    return;
                }
                openAssetItem(item);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowAssetsTreePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowAssetsTreePopup(e);
            }
        });

        JScrollPane scrollPane = new JScrollPane(assetsTree);
        scrollPane.setBorder(null);
        configureSmoothScrolling(scrollPane);

        assetsGridList.setBorder(null);
        assetsGridList.setBackground(panelBackground);
        assetsGridList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        assetsGridList.setVisibleRowCount(-1);
        assetsGridList.setFixedCellHeight(112);
        assetsGridList.setFixedCellWidth(120);
        assetsGridList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assetsGridList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AssetItem item) {
                    label.setText("<html><center>" + escapeHtml(item.title) + "</center></html>");
                    label.setIcon(getAssetGridIcon(item));
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setToolTipText(item.file != null ? item.file.getAbsolutePath() : item.subtitle);
                }
                return label;
            }
        });
        assetsGridList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                maybeShowAssetsGridPopup(e);
                if (e.getClickCount() != 2) {
                    return;
                }
                AssetItem item = assetsGridList.getSelectedValue();
                if (item == null || !item.isOpenable()) {
                    return;
                }
                openAssetItem(item);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowAssetsGridPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowAssetsGridPopup(e);
            }
        });

        JScrollPane gridScrollPane = new JScrollPane(assetsGridList);
        gridScrollPane.setBorder(null);
        configureSmoothScrolling(gridScrollPane);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        assetsSearchField.setToolTipText("Search assets");
        searchBar.add(assetsSearchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        assetsSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh(context.currentEditor());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh(context.currentEditor());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh(context.currentEditor());
            }
        });
        searchToggle.addActionListener(e -> {
            boolean visible = searchToggle.isSelected();
            searchBar.setVisible(visible);
            if (visible) {
                assetsSearchField.requestFocusInWindow();
            }
            panel.revalidate();
            panel.repaint();
        });

        assetsContentPanel.add(scrollPane, LAYOUT_TREE);
        assetsContentPanel.add(gridScrollPane, LAYOUT_GRID);
        showAssetsLayout(LAYOUT_TREE);
        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBackground(panelBackground);
        content.add(searchBar, BorderLayout.NORTH);
        content.add(assetsContentPanel, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        panelCardHost.add(createRoundedCardHost(panel, panelBackground, "assets-main"), "main");
        ((CardLayout) panelCardHost.getLayout()).show(panelCardHost, "main");
        return panelCardHost;
    }

    private JToggleButton createAssetsLayoutButton(
            String tooltip, String iconPath, String layoutKey, String segmentPosition) {
        JToggleButton button = new JToggleButton(createScaledIcon(iconPath, 18));
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "segmented");
        button.putClientProperty("JButton.segmentPosition", segmentPosition);
        button.setOpaque(false);
        button.setMargin(new Insets(6, 8, 6, 8));
        button.setPreferredSize(HEADER_LAYOUT_BUTTON_SIZE);
        button.setMinimumSize(HEADER_LAYOUT_BUTTON_SIZE);
        button.setMaximumSize(HEADER_LAYOUT_BUTTON_SIZE);
        button.addActionListener(e -> showAssetsLayout(layoutKey));
        return button;
    }

    private JButton createHeaderIconButton(String iconPath, String tooltip) {
        JButton button = new JButton(createScaledIcon(iconPath, 18));
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setOpaque(false);
        button.setMargin(new Insets(6, 6, 6, 6));
        button.setPreferredSize(HEADER_ICON_BUTTON_SIZE);
        button.setMinimumSize(HEADER_ICON_BUTTON_SIZE);
        button.setMaximumSize(HEADER_ICON_BUTTON_SIZE);
        return button;
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

    private void showAssetsLayout(String layoutKey) {
        CardLayout layout = (CardLayout) assetsContentPanel.getLayout();
        layout.show(assetsContentPanel, layoutKey);
    }

    private String formatAssetTreeLabel(AssetItem item, boolean isSection) {
        if (item == null) {
            return "";
        }
        String title = escapeHtml(item.title == null ? "" : item.title);
        String subtitle = item.subtitle == null ? "" : item.subtitle.trim();
        if (subtitle.isBlank()) {
            return isSection ? "<html><b>" + title + "</b></html>" : title;
        }
        String subtitleHtml = escapeHtml(subtitle);
        if (isSection) {
            return "<html><b>" + title + "</b> <span style='color:gray;'>" + subtitleHtml + "</span></html>";
        }
        return "<html>" + title + " <span style='color:gray;'>" + subtitleHtml + "</span></html>";
    }

    private void openAssetItem(AssetItem item) {
        if (item == null || !item.isOpenable()) {
            return;
        }

        context.commands().openFileWithPreferredViewer(item.file);
    }

    private void maybeShowAssetsTreePopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        TreePath path = assetsTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        assetsTree.setSelectionPath(path);
        Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (!(userObject instanceof AssetItem item)) {
            return;
        }
        showAssetsPopup(item, assetsTree, e.getX(), e.getY());
    }

    private void maybeShowAssetsGridPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int index = assetsGridList.locationToIndex(e.getPoint());
        if (index < 0) {
            return;
        }
        assetsGridList.setSelectedIndex(index);
        AssetItem item = assetsGridList.getModel().getElementAt(index);
        showAssetsPopup(item, assetsGridList, e.getX(), e.getY());
    }

    private void showAssetsPopup(AssetItem item, Component invoker, int x, int y) {
        if (item == null) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem openItem = new JMenuItem("Open");
        openItem.setEnabled(item.isOpenable());
        openItem.addActionListener(evt -> openAssetItem(item));
        popup.add(openItem);

        JMenuItem revealItem = new JMenuItem("Reveal in Finder");
        revealItem.setEnabled(item.file != null);
        revealItem.addActionListener(evt -> revealInFinder(item.file, context.dialogs()));
        popup.add(revealItem);

        JMenuItem systemItem = new JMenuItem("Open with Default App");
        systemItem.setEnabled(item.file != null);
        systemItem.addActionListener(evt -> openWithSystemDefault(item.file, context.dialogs()));
        popup.add(systemItem);

        JMenuItem copyPathItem = new JMenuItem("Copy Path");
        copyPathItem.setEnabled(item.file != null);
        copyPathItem.addActionListener(evt -> {
            StringSelection selection = new StringSelection(item.file.getAbsolutePath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });
        popup.add(copyPathItem);

        popup.addSeparator();
        JMenuItem refreshItem = new JMenuItem("Refresh Assets");
        refreshItem.addActionListener(evt -> refresh(context.currentEditor()));
        popup.add(refreshItem);

        popup.show(invoker, x, y);
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {
        if (context == null) {
            return;
        }
        File rootDir = new File(context.currentDirectory());
        String searchNeedle = assetsSearchField.getText() == null
                ? ""
                : assetsSearchField.getText().trim().toLowerCase(Locale.ROOT);
        assetThumbnailCache.clear();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new AssetItem(
                "Assets",
                "Workspace resources, libraries, and embedded literals",
                null,
                createImageIcon(ICON_MENU_ASSETS)));

        java.util.List<File> workspaceAssets = collectWorkspaceAssets(rootDir, 0, 4);
        workspaceAssets = filterAssetFiles(workspaceAssets, rootDir, searchNeedle);
        DefaultMutableTreeNode workspaceNode = buildMediaTypeSection(
                "Workspace Resources", workspaceAssets, rootDir, createImageIcon(ICON_MENU_FOLDER));
        if (workspaceNode != null) {
            rootNode.add(workspaceNode);
        }

        java.util.List<File> literalAssets =
                detectLiteralAssets(rootDir, context.currentEditor().getLanguage());
        literalAssets = filterAssetFiles(literalAssets, rootDir, searchNeedle);
        DefaultMutableTreeNode literalNode =
                buildMediaTypeSection("Embedded Literals", literalAssets, rootDir, createImageIcon(ICON_MENU_ASSETS));
        if (literalNode != null) {
            rootNode.add(literalNode);
        }

        assetsTree.setModel(new DefaultTreeModel(rootNode));
        for (int i = 0; i < Math.min(4, assetsTree.getRowCount()); i++) {
            assetsTree.expandRow(i);
        }

        assetsListModel.clear();
        for (AssetItem item : collectOpenableAssets(rootNode)) {
            assetsListModel.addElement(item);
        }
    }

    private java.util.List<File> filterAssetFiles(java.util.List<File> files, File baseDir, String searchNeedle) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        if (searchNeedle == null || searchNeedle.isBlank()) {
            return files;
        }
        java.util.List<File> filtered = new ArrayList<>();
        for (File file : files) {
            if (file == null) {
                continue;
            }
            String name = file.getName().toLowerCase(Locale.ROOT);
            String absolute = file.getAbsolutePath().toLowerCase(Locale.ROOT);
            String relative = formatRelativePath(file, baseDir);
            String relativeLower = relative == null ? "" : relative.toLowerCase(Locale.ROOT);
            if (name.contains(searchNeedle)
                    || absolute.contains(searchNeedle)
                    || relativeLower.contains(searchNeedle)) {
                filtered.add(file);
            }
        }
        return filtered;
    }

    @Override
    public void onFileModified(String filePath) {}

    @Override
    public void dispose() {}

    @Override
    public void onCompileSucceeded() {}

    private java.util.List<AssetItem> collectOpenableAssets(DefaultMutableTreeNode rootNode) {
        java.util.List<AssetItem> items = new ArrayList<>();
        if (rootNode == null) {
            return items;
        }
        java.util.Enumeration<?> enumeration = rootNode.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object next = enumeration.nextElement();
            if (!(next instanceof DefaultMutableTreeNode node)) {
                continue;
            }
            if (node.getUserObject() instanceof AssetItem item && item.isOpenable()) {
                items.add(item);
            }
        }
        return items;
    }

    private DefaultMutableTreeNode buildMediaTypeSection(
            String title, java.util.List<File> files, File baseDir, Icon sectionIcon) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        Map<String, List<File>> byMediaType = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (File file : files) {
            String mediaType = getMediaTypeLabel(file);
            byMediaType.computeIfAbsent(mediaType, k -> new ArrayList<>()).add(file);
        }
        DefaultMutableTreeNode section =
                new DefaultMutableTreeNode(new AssetItem(title, files.size() + " file(s)", null, sectionIcon));
        for (String mediaType : List.of("Images", "Audio", "Video", "Text", "Documents", "Other")) {
            java.util.List<File> bucket = byMediaType.get(mediaType);
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(
                    new AssetItem(mediaType, bucket.size() + " file(s)", null, createImageIcon(ICON_MENU_FOLDER)));
            bucket.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File file : bucket) {
                typeNode.add(new DefaultMutableTreeNode(createAssetItem(file, baseDir, mediaType)));
            }
            section.add(typeNode);
        }
        return section;
    }

    private void collectResolvedLibraryAssets(java.util.List<String> paths, File baseDir, java.util.List<File> out) {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        for (String path : paths) {
            if (path == null || path.isBlank()) {
                continue;
            }
            File resolved = resolveAssetReference(path, baseDir, null);
            if (resolved == null || !resolved.exists()) {
                continue;
            }
            if (resolved.isDirectory()) {
                collectWorkspaceAssets(resolved, 0, 2, out);
            } else {
                out.add(resolved);
            }
        }
    }

    private java.util.List<File> collectWorkspaceAssets(File directory, int depth, int maxDepth) {
        java.util.List<File> assets = new ArrayList<>();
        collectWorkspaceAssets(directory, depth, maxDepth, assets);
        return assets;
    }

    private void collectWorkspaceAssets(File directory, int depth, int maxDepth, java.util.List<File> out) {
        if (directory == null || !directory.isDirectory() || depth > maxDepth) {
            return;
        }
        if (shouldSkipAssetDirectory(directory)) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File file : files) {
            if (file == null || file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                collectWorkspaceAssets(file, depth + 1, maxDepth, out);
            } else if (isKnownAssetFile(file)) {
                out.add(file);
            }
        }
    }

    private boolean shouldSkipAssetDirectory(File directory) {
        String name = directory.getName().toLowerCase(Locale.ROOT);
        return name.equals("build")
                || name.equals("out")
                || name.equals("target")
                || name.equals("bin")
                || name.equals("dist")
                || name.equals("node_modules")
                || name.equals(".gradle")
                || name.equals(".git");
    }

    private AssetItem createAssetItem(File file, File baseDir, String subtitlePrefix) {
        String subtitle = subtitlePrefix;
        if (file != null) {
            String relative = formatRelativePath(file, baseDir);
            if (relative != null && !relative.isBlank()) {
                subtitle = subtitle == null || subtitle.isBlank() ? relative : subtitle + " • " + relative;
            }
        }
        return new AssetItem(
                file != null ? file.getName() : "(unknown)",
                subtitle,
                file,
                file != null ? fileSystemView.getSystemIcon(file) : createImageIcon(ICON_MENU_FOLDER));
    }

    private Icon getAssetGridIcon(AssetItem item) {
        if (item == null || item.file == null) {
            return createImageIcon(ICON_MENU_ASSETS);
        }
        String cacheKey = item.file.getAbsolutePath();
        Icon cached = assetThumbnailCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Icon icon = item.icon;
        String lower = item.file.getName().toLowerCase(Locale.ROOT);
        if (FileViewerFactory.isImageFile(lower)) {
            icon = buildImageThumbnailIcon(item.file, 84, 64);
        }
        if (icon == null) {
            icon = createImageIcon(ICON_MENU_ASSETS);
        }
        assetThumbnailCache.put(cacheKey, icon);
        return icon;
    }

    private java.util.List<File> detectLiteralAssets(File baseDir, LanguageService languageService) {
        java.util.LinkedHashSet<File> detected = new java.util.LinkedHashSet<>();
        if (fileManager == null) {
            return new ArrayList<>();
        }

        for (FileEditor editor : fileManager.getFileEditors()) {
            if (editor == null || editor.getEditorPane() == null) {
                continue;
            }
            String text = editor.getEditorPane().getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            File sourceFile = editor.getFile();
            File sourceParent =
                    sourceFile != null ? sourceFile.getAbsoluteFile().getParentFile() : null;
            for (String literal : languageService.extractStringLiterals(text)) {
                if (literal == null || literal.isBlank()) {
                    continue;
                }
                File resolved = resolveAssetReference(literal, baseDir, sourceParent);
                if (resolved != null && resolved.exists() && resolved.isFile()) {
                    detected.add(resolved);
                }
            }
        }
        return new ArrayList<>(detected);
    }

    private File resolveAssetReference(String literal, File baseDir, File sourceParent) {
        if (literal == null || literal.isBlank()) {
            return null;
        }
        String normalized = FileUtil.separatorsToSystem(literal);
        File candidate = new File(normalized);
        if (candidate.isAbsolute()) {
            return candidate;
        }
        if (baseDir != null) {
            File workspace = new File(baseDir, normalized);
            if (workspace.exists()) {
                return workspace;
            }
        }
        if (sourceParent != null) {
            File sibling = new File(sourceParent, normalized);
            if (sibling.exists()) {
                return sibling;
            }
        }
        return candidate;
    }

    private boolean isKnownAssetFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return getMediaTypeLabel(file) != null && !"Other".equals(getMediaTypeLabel(file));
    }
}
