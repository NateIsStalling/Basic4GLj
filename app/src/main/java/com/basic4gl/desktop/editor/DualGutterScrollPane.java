package com.basic4gl.desktop.editor;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.IconRowHeader;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * An {@link RTextScrollPane} that shows a second, dedicated icon column to the
 * left of the standard gutter.
 *
 * <p>The standard gutter's icon row header is used for bookmarks, while the
 * extra {@link IconRowHeader} is used for breakpoints, allowing the two sets of
 * icons to be displayed side by side.
 */
public class DualGutterScrollPane extends RTextScrollPane {

    private final IconRowHeader breakpointHeader;

    public DualGutterScrollPane(RSyntaxTextArea textArea) {
        super(textArea);

        // The standard gutter (line numbers, fold indicator and the bookmark
        // icon column) is created by the superclass and installed as the row
        // header. Wrap it in a panel together with a dedicated breakpoint
        // column so both icon columns are visible at once.
        breakpointHeader = new IconRowHeader(textArea);

        JPanel rowHeader = new JPanel(new BorderLayout());
        rowHeader.add(breakpointHeader, BorderLayout.LINE_START);
        rowHeader.add(getGutter(), BorderLayout.CENTER);
        setRowHeaderView(rowHeader);
    }

    /**
     * Returns the dedicated icon column used for breakpoints.
     *
     * @return The breakpoint icon row header.
     */
    public IconRowHeader getBreakpointHeader() {
        return breakpointHeader;
    }
}
