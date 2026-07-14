package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.FileUtil;
import com.basic4gl.desktop.util.RoundedCardPanel;
import com.basic4gl.desktop.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import static com.basic4gl.desktop.Theme.ICON_DOTS_VERTICAL;
import static com.basic4gl.desktop.Theme.ICON_MENU_FOLDER;
import static com.basic4gl.desktop.Theme.ICON_REFRESH;
import static com.basic4gl.desktop.Theme.ICON_SEARCH;
import static com.basic4gl.desktop.util.SwingIconUtil.createScaledIcon;
import static com.basic4gl.desktop.util.SwingUtil.configureSmoothScrolling;
import static com.basic4gl.desktop.util.SwingUtil.createLighterPanelBackground;

public class FileBrowserPanelProvider implements IEditorPanelProvider {

    private PluginContext context;

    private final JTree fileBrowserTree = new JTree();
    private final JTextField fileSearchField = new JTextField();
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private boolean showHiddenFiles = false;
    private static final Dimension HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);

    @Override
    public String id() {
        return "files";
    }

    @Override
    public String getTitle() {
        return "Workspace";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_FOLDER;
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
        panel.setOpaque(true);
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(panelBackground);
        header.setOpaque(true);
        JLabel title = new JLabel("Workspace");
        Font baseFont = title.getFont();
        title.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        title.setForeground(new Color(0x424242));
        title.setBorder(new EmptyBorder(0, 8, 0, 8));
        JButton refresh = createHeaderIconButton(ICON_REFRESH, "Refresh Workspace");
        refresh.addActionListener(e -> refresh(context.currentEditor()));
        JToggleButton searchToggle = createHeaderSearchToggleButton();

        JPopupMenu overflowMenu = new JPopupMenu();
        JMenuItem openFolderItem = new JMenuItem("Open Folder");
        openFolderItem.addActionListener(e -> context.commands().actionOpenFolder());
        overflowMenu.add(openFolderItem);

        JCheckBoxMenuItem showHiddenItem = new JCheckBoxMenuItem("Show Hidden Files", showHiddenFiles);
        showHiddenItem.addActionListener(e -> {
            showHiddenFiles = showHiddenItem.isSelected();
            refresh(context.currentEditor());
        });
        overflowMenu.add(showHiddenItem);

        JButton overflowButton = createHeaderIconButton(ICON_DOTS_VERTICAL, "More Actions");
        overflowButton.addActionListener(e -> {
            showHiddenItem.setSelected(showHiddenFiles);
            overflowMenu.show(overflowButton, 0, overflowButton.getHeight());
        });

        header.add(title);
        header.add(Box.createHorizontalGlue());
        header.add(searchToggle);
        header.add(refresh);
        header.add(overflowButton);
        panel.add(header, BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        fileSearchField.setToolTipText("Search workspace files");
        searchBar.add(fileSearchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        fileSearchField.getDocument().addDocumentListener(new DocumentListener() {
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
                fileSearchField.requestFocusInWindow();
            }
            panel.revalidate();
            panel.repaint();
        });

        fileBrowserTree.setBackground(panelBackground);
        fileBrowserTree.setRootVisible(true);
        fileBrowserTree.setShowsRootHandles(true);
        fileBrowserTree.setRowHeight(22);
        fileBrowserTree.setCellRenderer(new DefaultTreeCellRenderer() {
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
                if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof File file) {
                    label.setText(file == null ? "" : fileSystemView.getSystemDisplayName(file));
                    if (label.getText() == null || label.getText().isBlank()) {
                        label.setText(file.getName().isBlank() ? file.getPath() : file.getName());
                    }
                    label.setIcon(fileSystemView.getSystemIcon(file));
                    label.setToolTipText(file.getAbsolutePath());
                    boolean isHidden = file.getName().startsWith(".");
                    if (isHidden && !selected) {
                        label.setForeground(new Color(160, 160, 160));
                    }
                }
                return label;
            }
        });
        fileBrowserTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                maybeShowWorkspaceBrowserPopup(e);
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = fileBrowserTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!(userObject instanceof File file) || !file.isFile()) {
                    return;
                }
                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                    context.commands().openMarkdownInDocsTab(file);
                } else {
                    context.commands().openFileWithPreferredViewer(file);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowWorkspaceBrowserPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowWorkspaceBrowserPopup(e);
            }
        });
        JScrollPane scrollPane = new JScrollPane(fileBrowserTree);
        scrollPane.setBorder(null);
        scrollPane.setBackground(panelBackground);

        configureSmoothScrolling(scrollPane);
        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBackground(panelBackground);
        content.add(searchBar, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        panelCardHost.add(createRoundedCardHost(panel, panelBackground, "workspace-main"), "main");
        ((CardLayout) panelCardHost.getLayout()).show(panelCardHost, "main");
        return panelCardHost;
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
        host.setBackground(panelBackground);
        host.setOpaque(false);
        host.add(card, key);
        ((CardLayout) host.getLayout()).show(host, key);
        return host;
    }

    private void maybeShowWorkspaceBrowserPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }

        TreePath path = fileBrowserTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        fileBrowserTree.setSelectionPath(path);

        Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (!(userObject instanceof File selectedFile)) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem openItem = new JMenuItem(selectedFile.isDirectory() ? "Open Folder" : "Open");
        openItem.addActionListener(evt -> {
            if (selectedFile.isDirectory()) {
                context.commands().setWorkspaceDirectory(selectedFile);
            } else if (selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                context.commands().openMarkdownInDocsTab(selectedFile);
            } else {
                context.commands().openFileWithPreferredViewer(selectedFile);
            }
        });
        popup.add(openItem);

        JMenuItem revealItem = new JMenuItem("Reveal in Finder");
        revealItem.addActionListener(evt -> FileUtil.revealInFinder(selectedFile, context.dialogs()));
        popup.add(revealItem);

        JMenuItem openSystemItem = new JMenuItem("Open with Default App");
        openSystemItem.addActionListener(evt -> FileUtil.openWithSystemDefault(selectedFile, context.dialogs()));
        popup.add(openSystemItem);

        JMenuItem copyPathItem = new JMenuItem("Copy Path");
        copyPathItem.addActionListener(evt -> {
            StringSelection selection = new StringSelection(selectedFile.getAbsolutePath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        });
        popup.add(copyPathItem);

        popup.addSeparator();
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(evt -> refresh(context.currentEditor()));
        popup.add(refreshItem);

        popup.show(fileBrowserTree, e.getX(), e.getY());
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {
        if (context == null) {
            return;
        }
        File root = new File(context.currentDirectory());
        String searchNeedle = fileSearchField.getText() == null
                ? ""
                : fileSearchField.getText().trim().toLowerCase(Locale.ROOT);
        DefaultMutableTreeNode rootNode = buildFileTreeNode(root, 0, searchNeedle);
        if (rootNode == null) {
            rootNode = new DefaultMutableTreeNode(root);
        }
        fileBrowserTree.setModel(new DefaultTreeModel(rootNode));
        if (fileBrowserTree.getRowCount() > 0) {
            fileBrowserTree.expandRow(0);
        }
    }

    @Override
    public void onFileModified(String filePath) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void onCompileSucceeded() {

    }

    private DefaultMutableTreeNode buildFileTreeNode(File file, int depth, String searchNeedle) {
        boolean hasSearch = searchNeedle != null && !searchNeedle.isBlank();
        boolean nameMatches = !hasSearch || file.getName().toLowerCase(Locale.ROOT).contains(searchNeedle);
        boolean pathMatches = !hasSearch || file.getAbsolutePath().toLowerCase(Locale.ROOT).contains(searchNeedle);
        boolean matches = nameMatches || pathMatches;
        if (!file.isDirectory()) {
            return matches ? new DefaultMutableTreeNode(file) : null;
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        File[] children = file.listFiles();
        if (children == null) {
            return (depth == 0 || matches) ? node : null;
        }
        Arrays.sort(children, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File child : children) {
            if (!showHiddenFiles && child.getName().startsWith(".")) {
                continue;
            }
            DefaultMutableTreeNode childNode = buildFileTreeNode(child, depth + 1, searchNeedle);
            if (childNode != null) {
                node.add(childNode);
            }
        }
        return (depth == 0 || matches || node.getChildCount() > 0) ? node : null;
    }
}
