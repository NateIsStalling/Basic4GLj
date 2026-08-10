package com.basic4gl.desktop.editor;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.IconRowHeader;
import org.fife.ui.rtextarea.RTextArea;

/**
 * An {@link IconRowHeader} that previews the bookmark icon as a semi-transparent
 * "ghost" at the line under the mouse cursor, giving a hint that clicking will
 * add an icon there.
 */
public class HoverIconRowHeader extends IconRowHeader {

    private static final float GHOST_ALPHA = 0.4f;

    private int hoveredLine = -1;

    public HoverIconRowHeader(RTextArea textArea) {
        super(textArea);

        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredLine(e.getPoint());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                updateHoveredLine(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHoveredLine(-1);
            }
        };
        addMouseListener(hoverListener);
        addMouseMotionListener(hoverListener);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // The base class toggles a bookmark on any press; restrict that to the
        // left button so right-clicks are free to open a context menu.
        if (e.isPopupTrigger() || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        super.mousePressed(e);
    }

    private void updateHoveredLine(Point p) {
        if (textArea == null) {
            setHoveredLine(-1);
            return;
        }
        try {
            int offs = textArea.viewToModel(p);
            setHoveredLine(offs > -1 ? textArea.getLineOfOffset(offs) : -1);
        } catch (BadLocationException ex) {
            setHoveredLine(-1);
        }
    }

    private void setHoveredLine(int line) {
        if (line != hoveredLine) {
            hoveredLine = line;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hoveredLine < 0 || textArea == null) {
            return;
        }

        Icon icon = getBookmarkIcon();
        if (icon == null) {
            return;
        }

        try {
            // Don't draw a ghost if this line already shows the icon.
            for (GutterIconInfo info : getTrackingIcons(hoveredLine)) {
                if (info.getIcon() == icon) {
                    return;
                }
            }

            int offs = textArea.getLineStartOffset(hoveredLine);
            Rectangle r = textArea.modelToView(offs);
            if (r == null) {
                return;
            }

            int y = r.y + (textArea.getLineHeight() - icon.getIconHeight()) / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GHOST_ALPHA));
                icon.paintIcon(this, g2, 0, y);
            } finally {
                g2.dispose();
            }
        } catch (BadLocationException ex) {
            // Line no longer valid; nothing to preview.
        }
    }
}
