package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

import static com.basic4gl.desktop.Theme.ICON_MENU_FOLDER;
import static com.basic4gl.desktop.Theme.ICON_MENU_HELP;
import static com.basic4gl.desktop.util.SwingUtil.configureSmoothScrolling;

public class FileBrowserPanelProvider implements IEditorPanelProvider {

    private PluginContext context;

    private final JTree fileBrowserTree = new JTree();
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private boolean showHiddenFiles = false;


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
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        JPanel header = new JPanel(new BorderLayout());
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JLabel title = new JLabel("Workspace Browser");
        title.setBorder(new EmptyBorder(4, 8, 0, 8));
        JButton openFolder = new JButton("Open Folder");
        openFolder.setFocusable(false);
        openFolder.addActionListener(e -> context.commands().actionOpenFolder());
        JButton refresh = new JButton("Refresh");
        refresh.setFocusable(false);
        refresh.addActionListener(e -> refresh(context.currentEditor()));
        JToggleButton showHiddenToggle = new JToggleButton("Show Hidden");
        showHiddenToggle.setFocusable(false);
        showHiddenToggle.setSelected(showHiddenFiles);
        showHiddenToggle.addActionListener(e -> {
            showHiddenFiles = showHiddenToggle.isSelected();
            refresh(context.currentEditor());
        });
        headerButtons.add(showHiddenToggle);
        headerButtons.add(openFolder);
        headerButtons.add(refresh);
        header.add(title, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

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
        configureSmoothScrolling(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
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
        File root = new File(context.currentDirectory());
        DefaultMutableTreeNode rootNode = buildFileTreeNode(root, 0);
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

    private DefaultMutableTreeNode buildFileTreeNode(File file, int depth) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        if (!file.isDirectory()) {
            return node;
        }

        File[] children = file.listFiles();
        if (children == null) {
            return node;
        }
        Arrays.sort(children, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File child : children) {
            if (!showHiddenFiles && child.getName().startsWith(".")) {
                continue;
            }
            node.add(buildFileTreeNode(child, depth + 1));
        }
        return node;
    }
}
