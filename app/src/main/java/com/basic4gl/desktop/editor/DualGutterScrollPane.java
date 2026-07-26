package com.basic4gl.desktop.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * An {@link RTextScrollPane} that shows two dedicated icon columns to the left
 * of the standard gutter: a bookmark column (leftmost) and a breakpoint column.
 *
 * <p>The standard gutter's own icon row header is left disabled; instead two
 * standalone {@link HoverIconRowHeader}s are composed into the scroll pane's row
 * header so the two sets of icons can be displayed side by side, each with a
 * hover preview. This replaces the previous {@code MultiHeaderGutter}/
 * {@code RMultiHeaderScrollPane} classes, which duplicated library internals
 * inside the {@code org.fife.ui.rtextarea} package in order to reach
 * package-private members. Everything here is built from the public
 * RSyntaxTextArea API instead.</p>
 */
public class DualGutterScrollPane extends RTextScrollPane {

    private final HoverIconRowHeader bookmarkHeader;
    private final HoverIconRowHeader breakpointHeader;
    private final JPanel iconColumns;
    private final JPanel rowHeader;

    public DualGutterScrollPane(RSyntaxTextArea textArea) {
        super(textArea);

        bookmarkHeader = new HoverIconRowHeader(textArea);
        breakpointHeader = new HoverIconRowHeader(textArea);

        // Bookmarks are leftmost, breakpoints next, then the standard gutter
        // (line numbers and fold indicator). The gutter's built-in icon row
        // header stays disabled so bookmark and breakpoint icons live in these
        // dedicated columns instead.
        iconColumns = new JPanel();
        iconColumns.setLayout(new BoxLayout(iconColumns, BoxLayout.LINE_AXIS));
        iconColumns.add(bookmarkHeader);
        iconColumns.add(breakpointHeader);

        rowHeader = new JPanel(new BorderLayout());
        rowHeader.add(iconColumns, BorderLayout.LINE_START);
        rowHeader.add(getGutter(), BorderLayout.CENTER);
        setRowHeaderView(rowHeader);

        matchGutterAreaBackground();
    }

    /**
     * Makes the whole gutter area (both icon columns) share the text area's
     * background so it doesn't stand out with the default panel color.
     *
     * <p>The standard gutter keeps its own background in sync with the text
     * area, so that color is used as the source. If the gutter has no
     * background of its own, the text area's background is used and applied to
     * the parent panels instead.</p>
     */
    public void matchGutterAreaBackground() {
        Color gutterBg = getGutter().getBackground();
        if (gutterBg != null) {
            bookmarkHeader.setBackground(gutterBg);
            breakpointHeader.setBackground(gutterBg);
        } else if (getTextArea() != null) {
            // Gutter has no background of its own; style the parent panels.
            Color bg = getTextArea().getBackground();
            iconColumns.setBackground(bg);
            rowHeader.setBackground(bg);
        }
        rowHeader.revalidate();
        rowHeader.repaint();
    }

    /**
     * Returns the icon column used for bookmarks.
     *
     * @return The bookmark icon row header.
     */
    public HoverIconRowHeader getBookmarkHeader() {
        return bookmarkHeader;
    }

    /**
     * Returns the icon column used for breakpoints.
     *
     * @return The breakpoint icon row header.
     */
    public HoverIconRowHeader getBreakpointHeader() {
        return breakpointHeader;
    }

    /**
     * Shows or hides the bookmark column. Hiding it collapses the column so it
     * takes no horizontal space, allowing the column to be auto-hidden when
     * there are no bookmarks.
     *
     * @param visible Whether the bookmark column should be shown.
     */
    public void setBookmarkColumnVisible(boolean visible) {
        if (bookmarkHeader.isVisible() != visible) {
            bookmarkHeader.setVisible(visible);
            iconColumns.revalidate();
            iconColumns.repaint();
        }
    }
}
