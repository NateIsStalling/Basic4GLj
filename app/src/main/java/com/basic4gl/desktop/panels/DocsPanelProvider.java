package com.basic4gl.desktop.panels;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

import com.basic4gl.desktop.content.FileManager;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DocsPanelProvider implements IEditorPanelProvider {

    private final JTabbedPane docsTabs = new JTabbedPane();
    private final JTree docsExplorerTree = new JTree();
    private final JTextField docsExplorerSearchField = new JTextField();
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    private final FileManager fileManager;

    public DocsPanelProvider(FileManager fileManager) {
        this.fileManager = fileManager;
    }

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
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        Color panelBackground = com.basic4gl.desktop.util.SwingUtil.createLighterPanelBackground();
        panel.setBackground(panelBackground);
        panel.setOpaque(true);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(panelBackground);
        JLabel title = new JLabel("Docs Explorer");
        Font baseFont = title.getFont();
        title.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        title.setForeground(new Color(0x424242));
        title.setBorder(new EmptyBorder(0, 8, 0, 8));

        JButton refresh = new JButton(createImageIcon(ICON_REFRESH));
        refresh.setToolTipText("Refresh Docs Explorer");
        refresh.setFocusable(false);
        refresh.putClientProperty("JButton.buttonType", "toolBarButton");
        refresh.setOpaque(false);
        refresh.addActionListener(e -> refreshDocsExplorerTree());

        JToggleButton searchToggle = new JToggleButton(createImageIcon(ICON_SEARCH));
        searchToggle.setToolTipText("Show search");
        searchToggle.setFocusable(false);
        searchToggle.putClientProperty("JButton.buttonType", "toolBarButton");
        searchToggle.setOpaque(false);

        header.add(title);
        header.add(Box.createHorizontalGlue());
        header.add(searchToggle);
        header.add(refresh);
        panel.add(header, BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        docsExplorerSearchField.setToolTipText("Search markdown files");
        searchBar.add(docsExplorerSearchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        searchToggle.addActionListener(e -> {
            boolean visible = searchToggle.isSelected();
            searchBar.setVisible(visible);
            if (visible) {
                docsExplorerSearchField.requestFocusInWindow();
            }
            panel.revalidate();
            panel.repaint();
        });
        docsExplorerSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshDocsExplorerTree();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshDocsExplorerTree();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshDocsExplorerTree();
            }
        });

        docsExplorerTree.setBackground(panelBackground);
        docsExplorerTree.setRootVisible(true);
        docsExplorerTree.setShowsRootHandles(true);
        docsExplorerTree.setRowHeight(22);
        docsExplorerTree.setCellRenderer(new DefaultTreeCellRenderer() {
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
                    label.setText(fileSystemView.getSystemDisplayName(file));
                    if (label.getText() == null || label.getText().isBlank()) {
                        label.setText(file.getName().isBlank() ? file.getPath() : file.getName());
                    }
                    label.setIcon(fileSystemView.getSystemIcon(file));
                    label.setToolTipText(file.getAbsolutePath());
                }
                return label;
            }
        });
        docsExplorerTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = docsExplorerTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!(userObject instanceof File selectedFile) || !selectedFile.isFile()) {
                    return;
                }
                if (selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                    context.commands().openFileWithPreferredViewer(selectedFile);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(docsExplorerTree);
        scrollPane.setBorder(null);
        scrollPane.setBackground(panelBackground);
        com.basic4gl.desktop.util.SwingUtil.configureSmoothScrolling(scrollPane);

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBackground(panelBackground);
        content.add(searchBar, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {}

    @Override
    public void onFileModified(String filePath) {}

    @Override
    public void dispose() {}

    @Override
    public void onCompileSucceeded() {}

    private void refreshDocsExplorerTree() {
        if (fileManager == null) {
            return;
        }
        File root = new File(fileManager.getCurrentDirectory());
        String searchNeedle = docsExplorerSearchField.getText() == null
                ? ""
                : docsExplorerSearchField.getText().trim().toLowerCase(Locale.ROOT);
        DefaultMutableTreeNode rootNode = buildDocsTreeNode(root, 0, searchNeedle);
        if (rootNode == null) {
            rootNode = new DefaultMutableTreeNode(root);
        }
        docsExplorerTree.setModel(new DefaultTreeModel(rootNode));
        if (docsExplorerTree.getRowCount() > 0) {
            docsExplorerTree.expandRow(0);
        }
    }

    private DefaultMutableTreeNode buildDocsTreeNode(File file, int depth, String searchNeedle) {
        boolean hasSearch = searchNeedle != null && !searchNeedle.isBlank();
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        String absolutePath = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        boolean matchesSearch = !hasSearch || fileName.contains(searchNeedle) || absolutePath.contains(searchNeedle);

        if (!file.isDirectory()) {
            boolean isMarkdown = fileName.endsWith(".md");
            return (isMarkdown && matchesSearch) ? new DefaultMutableTreeNode(file) : null;
        }

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        File[] children = file.listFiles();
        if (children == null) {
            return (depth == 0 || matchesSearch) ? node : null;
        }
        Arrays.sort(children, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File child : children) {
            if (child.getName().startsWith(".")) {
                continue;
            }
            DefaultMutableTreeNode childNode = buildDocsTreeNode(child, depth + 1, searchNeedle);
            if (childNode != null) {
                node.add(childNode);
            }
        }
        return (depth == 0 || node.getChildCount() > 0 || matchesSearch) ? node : null;
    }
}
