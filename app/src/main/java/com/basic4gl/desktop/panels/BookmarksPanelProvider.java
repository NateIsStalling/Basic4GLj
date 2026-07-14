package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.BookmarkInfo;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.RoundedCardPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createScaledIcon;

public class BookmarksPanelProvider implements IEditorPanelProvider {
    private static final Dimension HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);

    private final List<BookmarkInfo> allBookmarks = new ArrayList<>();
    private final DefaultListModel<BookmarkInfo> bookmarkListModel = new DefaultListModel<>();
    private final JList<BookmarkInfo> bookmarkList = new JList<>(bookmarkListModel);
    private final JTextField bookmarkSearchField = new JTextField();
    private PluginContext context;

    @Override
    public String id() {
        return "bookmarks";
    }

    @Override
    public String getTitle() {
        return "Bookmarks";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_BOOKMARKS;
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

        JLabel title = new JLabel("Bookmarks");
        Font baseFont = title.getFont();
        title.setFont(new Font(baseFont.getName(), Font.BOLD, baseFont.getSize() + 2));
        title.setForeground(new Color(0x424242));
        title.setBorder(new EmptyBorder(0, 8, 0, 8));

        JButton toggleBookmarkButton = createHeaderIconButton(ICON_BOOKMARK_ADD, "Toggle bookmark");
        toggleBookmarkButton.addActionListener(e -> {
            if (this.context != null && this.context.commands() != null) {
                this.context.commands().toggleBookmark();
                reloadBookmarks();
            }
        });

        JButton nextBookmarkButton = createHeaderIconButton(ICON_ARROW_DOWN, "Next bookmark");
        nextBookmarkButton.addActionListener(e -> {
            if (this.context != null && this.context.commands() != null) {
                this.context.commands().selectNextBookmark();
                reloadBookmarks();
            }
        });

        JButton previousBookmarkButton = createHeaderIconButton(ICON_ARROW_UP, "Previous bookmark");
        previousBookmarkButton.addActionListener(e -> {
            if (this.context != null && this.context.commands() != null) {
                this.context.commands().selectPreviousBookmark();
                reloadBookmarks();
            }
        });

        JToggleButton searchToggle = createHeaderSearchToggleButton();
        header.add(title);
        header.add(Box.createHorizontalGlue());
        header.add(toggleBookmarkButton);
        header.add(Box.createHorizontalStrut(4));
        header.add(nextBookmarkButton);
        header.add(Box.createHorizontalStrut(4));
        header.add(previousBookmarkButton);
        header.add(Box.createHorizontalStrut(4));
        header.add(searchToggle);
        panel.add(header, BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(panelBackground);
        searchBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        bookmarkSearchField.setToolTipText("Search bookmarks");
        searchBar.add(bookmarkSearchField, BorderLayout.CENTER);
        searchBar.setVisible(false);
        searchToggle.addActionListener(e -> {
            boolean visible = searchToggle.isSelected();
            searchBar.setVisible(visible);
            if (visible) {
                bookmarkSearchField.requestFocusInWindow();
            }
            panel.revalidate();
            panel.repaint();
        });
        bookmarkSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterBookmarks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterBookmarks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterBookmarks();
            }
        });

        bookmarkList.setBackground(panelBackground);
        bookmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookmarkList.setFixedCellHeight(22);
        bookmarkList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof BookmarkInfo bookmark) {
                    String preview = bookmark.lineText() == null ? "" : bookmark.lineText();
                    String text = bookmark.fileName() + ":" + (bookmark.lineNumber() + 1);
                    if (!preview.isBlank()) {
                        text += "  " + preview;
                    }
                    label.setText(text);
                    label.setToolTipText(bookmark.filePath());
                }
                return label;
            }
        });
        bookmarkList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    goToSelectedBookmark();
                }
            }
        });
        bookmarkList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "openBookmark");
        bookmarkList.getActionMap().put("openBookmark", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                goToSelectedBookmark();
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBackground(panelBackground);
        content.add(searchBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(bookmarkList);
        scrollPane.setBackground(panelBackground);
        scrollPane.setBorder(null);

        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panelCardHost.add(createRoundedCardHost(panel, panelBackground, "bookmarks-main"), "main");
        ((CardLayout) panelCardHost.getLayout()).show(panelCardHost, "main");

        reloadBookmarks();
        return panelCardHost;
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {
        reloadBookmarks();
    }

    @Override
    public void onFileModified(String filePath) {
        reloadBookmarks();
    }

    @Override
    public void dispose() {}

    @Override
    public void onCompileSucceeded() {}

    private void reloadBookmarks() {
        if (context == null || context.commands() == null) {
            return;
        }
        allBookmarks.clear();
        allBookmarks.addAll(context.commands().listBookmarks());
        filterBookmarks();
    }

    private void filterBookmarks() {
        String needle = bookmarkSearchField.getText() == null
                ? ""
                : bookmarkSearchField.getText().trim().toLowerCase(Locale.ROOT);
        BookmarkInfo previousSelection = bookmarkList.getSelectedValue();
        bookmarkListModel.clear();
        for (BookmarkInfo item : allBookmarks) {
            String haystack = (item.fileName() + " " + item.lineText()).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || haystack.contains(needle)) {
                bookmarkListModel.addElement(item);
            }
        }

        if (!bookmarkListModel.isEmpty()) {
            if (previousSelection != null) {
                bookmarkList.setSelectedValue(previousSelection, true);
            } else {
                bookmarkList.setSelectedIndex(0);
            }
        }
    }

    private void goToSelectedBookmark() {
        if (context == null || context.commands() == null) {
            return;
        }
        BookmarkInfo item = bookmarkList.getSelectedValue();
        if (item == null) {
            return;
        }
        context.commands().goToBookmark(item.filePath(), item.lineNumber());
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

    private Color createLighterPanelBackground() {
        Color base = UIManager.getColor("Panel.background");
        if (base == null) {
            base = new Color(238, 238, 238);
        }
        return new Color(
                Math.min(255, base.getRed() + 8),
                Math.min(255, base.getGreen() + 8),
                Math.min(255, base.getBlue() + 8));
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
}
